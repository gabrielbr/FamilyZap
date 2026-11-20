package app.oie.ui.kid

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import app.oie.data.Photos
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import app.oie.data.Diagnostics
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.consume
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.ui.common.Bar
import app.oie.ui.common.Initial
import app.oie.ui.common.OIcon
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay

/** The 5-second undo before a message leaves the tablet (owner, 2026-11-18). */
private const val UNDO_MS = 5_000L

/** The Oiê sticker set (01 Brand/stickers, outlined _02 masters → res/drawable), by word. */
val STICKERS = linkedMapOf(
    "Oiê!" to R.drawable.sticker_oie, "Saudade" to R.drawable.sticker_saudade, "Boa noite" to R.drawable.sticker_boa_noite,
    "Te amo" to R.drawable.sticker_te_amo, "Tô chegando" to R.drawable.sticker_to_chegando, "Obrigado" to R.drawable.sticker_obrigado,
)

/** Prototype screens 03, 16–20. Flows: kid-sends-message.md, kid-receives-messages.md. */
// v0.31: receiving GIFs and stickers from the keyboard uses Compose's content receiver, still marked experimental.
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun Conversation(state: AppState, contactId: String, openStickers: Boolean = false, inBubble: Boolean = false, onLeave: (() -> Unit)? = null, pendingPreview: Message? = null) {
    val c = OieTheme.colors
    val tx = str
    // v0.21: paused or removed while open (a command from the parent's WhatsApp): back to the home screen.
    val contact = state.reachable.firstOrNull { it.id == contactId } ?: run {
        androidx.compose.runtime.LaunchedEffect(contactId) { if (onLeave != null) onLeave() else state.route = app.oie.data.Route.KidHome }
        return
    }
    // v0.24: while the link reconnects the kid can still write; messages wait and go out on connect (L4).
    val linkDown = app.oie.ui.common.linkShown(state) == app.oie.logic.LinkView.Shown.DOWN
    val canSend = state.canSend(contact) && !linkDown
    // v0.31: a text field that also takes GIFs, stickers and pictures from the keyboard, and pasted media.
    val field = androidx.compose.foundation.text.input.rememberTextFieldState(state.drafts[contactId] ?: "")
    val draft = field.text.toString()
    LaunchedEffect(field) { androidx.compose.runtime.snapshotFlow { field.text.toString() }.collect { state.drafts[contactId] = it } }
    // pendingPreview: the screenshot tests show the undo bar (v0.33: with Ouvir for a voice message).
    var pending by remember { mutableStateOf(pendingPreview) }
    var stickers by remember { mutableStateOf(openStickers) }
    var recordingSince by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    var choosingPhoto by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<String?>(null) }
    var photoError by remember { mutableStateOf(false) }
    var micError by remember { mutableStateOf(false) }
    /** The video or GIF open full screen (v0.27). */
    var watching by remember { mutableStateOf<Message?>(null) }
    /** v0.33: the message whose reactions sheet is open, the Fotos grid, a photo open big, the drawing pad. */
    var menuFor by remember { mutableStateOf<Message?>(null) }
    var showPhotos by remember { mutableStateOf(false) }
    var bigPhoto by remember { mutableStateOf<String?>(null) }
    var drawing by remember { mutableStateOf(false) }
    var stickerNote by remember { mutableStateOf<String?>(null) }
    androidx.compose.runtime.DisposableEffect(contactId) { onDispose { app.oie.data.ReadAloud.stop() } }
    /** A video or GIF being prepared to send (v0.28); its preview shows over the chat. */
    var videoDraft by remember { mutableStateOf<VideoDraft?>(null) }
    fun received(uri: Uri?) {
        if (uri == null) return // the kid backed out of the picker: nothing changes
        // v0.28: a video or a GIF from the gallery or the camera goes to the video preview.
        val type = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
        if (type.startsWith("video/") || type == "image/gif") { photoError = false; videoDraft = VideoDraft(uri, gif = type == "image/gif"); return }
        val kept = Photos.keep(context, uri)
        photoError = kept == null
        preview?.let { Photos.forget(context, it) } // Trocar: the old pick is dropped
        preview = kept
    }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    /**
     * A GIF, sticker or picture from the keyboard, or a pasted or dropped file (v0.31): copied at once
     * (the keyboard's permission to read it is short), then to the photo, GIF or video preview.
     */
    val receiver = remember(contactId) {
        androidx.compose.foundation.content.ReceiveContentListener { content ->
            var taken = false
            content.consume { item ->
                val uri = item.uri ?: return@consume false
                if (taken) return@consume true
                val goes = app.oie.logic.Pasted.route(runCatching { context.contentResolver.getType(uri) }.getOrNull())
                if (goes == app.oie.logic.Pasted.Goes.NOTHING) return@consume false
                taken = true
                val copy = runCatching {
                    val dir = java.io.File(context.cacheDir, "pasted").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
                    val f = java.io.File(dir, java.util.UUID.randomUUID().toString() + "." + app.oie.logic.Pasted.extension(goes))
                    context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { input.copyTo(it) } } ?: error("unreadable")
                    android.net.Uri.fromFile(f)
                }.onFailure { Diagnostics.log("paste: couldn't read it (${it.javaClass.simpleName})") }.getOrNull()
                if (copy == null) { photoError = true; return@consume true }
                scope.launch {
                    when (goes) {
                        app.oie.logic.Pasted.Goes.GIF -> videoDraft = VideoDraft(copy, gif = true)
                        app.oie.logic.Pasted.Goes.VIDEO -> videoDraft = VideoDraft(copy, gif = false)
                        else -> {
                            val kept = withContext(kotlinx.coroutines.Dispatchers.IO) { Photos.keep(context, copy) }
                            photoError = kept == null
                            preview?.let { Photos.forget(context, it) }
                            preview = kept
                        }
                    }
                }
                true
            }
        }
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { received(it) }
    // v0.33: "Criar figurinha": a photo from the gallery becomes a round sticker with the family ones.
    val makeSticker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val made = withContext(kotlinx.coroutines.Dispatchers.IO) {
                Photos.keep(context, uri)?.let { photo -> Photos.stickerFrom(context, photo).also { Photos.forget(context, photo) } }
            }
            if (made == null) { stickerNote = tx.stickerError; return@launch }
            state.stickers.add(0, app.oie.logic.FamilySticker("own-${System.nanoTime()}", made, animated = false, saved = true, at = state.clock()))
            stickerNote = tx.stickerMade
            stickers = true
        }
    }
    // v0.28: the camera app records a video, at most 3 minutes; it's shrunk before sending.
    var clipUri by remember { mutableStateOf<Uri?>(null) }
    val recorder = rememberLauncherForActivityResult(object : ActivityResultContracts.CaptureVideo() {
        override fun createIntent(context: android.content.Context, input: Uri) =
            super.createIntent(context, input).putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, (app.oie.logic.VideoRules.MAX_MS / 1000).toInt())
    }) { ok -> if (ok) clipUri?.let { videoDraft = VideoDraft(it, gif = false) } }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) received(cameraUri) }
    fun open(source: String) {
        choosingPhoto = false
        if (source == "draw") { drawing = true; return } // v0.33: not remembered as the first choice: the camera stays the usual one
        state.lastPhotoSource = source
        if (source == "video") {
            val uri = runCatching { FileProvider.getUriForFile(context, context.packageName + ".photos", Photos.newVideoTarget(context)) }.getOrNull()
            if (uri == null) { photoError = true; return }
            clipUri = uri
            recorder.launch(uri)
        } else if (source == "camera") {
            // The camera app writes into one temporary file shared through the FileProvider; made only when needed.
            val uri = runCatching { FileProvider.getUriForFile(context, context.packageName + ".photos", Photos.newCameraTarget(context)) }.getOrNull()
            if (uri == null) { photoError = true; return }
            cameraUri = uri
            camera.launch(uri)
        } else {
            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
    }
    val visible = state.visible(contactId)
    LaunchedEffect(contactId, visible.size) { state.lastSeen[contactId] = state.clock() }

    fun queue(m: Message) { pending = m }
    // v0.33: listening to a voice message before it goes pauses the 5 seconds; they start again when it ends.
    val hearing = pending?.let { app.oie.data.Voice.playing == it.id } ?: false
    LaunchedEffect(pending, hearing) {
        val m = pending ?: return@LaunchedEffect
        if (hearing) return@LaunchedEffect
        delay(UNDO_MS)
        state.messages += m
        state.wa.send(m)
        pending = null
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Bar {
                QuietButton(null, { state.drafts[contactId] = draft; if (onLeave != null) onLeave() else state.route = Route.KidHome }, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
                app.oie.ui.common.Avatar(contact, OTokens.Size.avatar)
                Text(contact.name, style = OieType.title, color = c.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                // v0.33: the photos, videos and GIFs in this chat. A word on the tablet, the icon alone on a phone.
                QuietButton(if (app.oie.ui.common.isCompact) null else tx.photos, { showPhotos = true }, icon = R.drawable.gg_ic_image, contentDescription = tx.photosWith(contact.name))
            }
            val list = rememberLazyListState()
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin),
                state = list, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            ) {
                item { Box(Modifier.height(OTokens.Spacing.space6)) }
                if (visible.isEmpty() && pending == null) item { Text(tx.firstHi(contact.name), style = OieType.body, color = c.muted) }
                items(visible, key = { it.id }) { m ->
                    val failed = m.id in state.failed
                    Bubble(m, contact.name, status = if (failed) tx.notSent else null, parent = state.parentName,
                        onMenu = { menuFor = m }, onPhoto = { bigPhoto = it }) { watching = it }
                    ReactionChips(m, alignEnd = m.fromKid)
                    if (app.oie.data.ReadAloud.reading == m.id) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        TextLink(tx.stopReading, { app.oie.data.ReadAloud.stop() })
                    }
                    // Rule L4: one automatic retry already happened; now the kid can try again.
                    if (failed) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextLink(tx.tryAgain, { state.failed.remove(m.id); state.wa.send(m) })
                    }
                }
                pending?.let { p -> item(key = "pending") { Bubble(p, contact.name, status = tx.sending) } }
                visible.lastOrNull { it.fromKid }?.takeIf { pending == null && visible.last().fromKid && it.id !in state.failed }?.let { last ->
                    // v0.33: "Enviada", then "Entregue", then "Vovó viu" (in a group, "Alguém viu").
                    val group = contact.isGroup && !contact.parentLine
                    item { Text(tx.receiptLine(last.receipt, contact.name, group, hour(tx, last.at)), style = OieType.caption, color = c.muted, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
                }
            }
            Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            ) {
                if (!canSend) {
                    val q = state.quietNow()
                    Text(
                        if (linkDown) tx.askReconnect(state.parentName) else tx.quietUntilShort(q?.end?.hour ?: 0),
                        style = OieType.body, color = c.muted,
                    )
                } else if (recordingSince > 0) {
                    Text(tx.recording, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
                    Text(tx.releaseToSend, style = OieType.bodySmall, color = c.muted)
                } else {
                    // v0.29 audit: on a narrow screen (a phone, a bubble) the camera and stickers step aside while the
                    // kid types, so the field has room; they come back when it's empty.
                    val compact = app.oie.ui.common.isCompact
                    if (!compact || draft.isBlank()) {
                        QuietButton(null, { choosingPhoto = true }, icon = R.drawable.gg_ic_camera, contentDescription = tx.photoButton)
                        QuietButton(null, { stickers = true }, icon = R.drawable.gg_ic_sticker, contentDescription = tx.stickerButton)
                    }
                    // v0.29: in a chat's bubble the field is focused at once, so the keyboard comes up to answer.
                    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
                    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    if (inBubble) LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
                    val focused by interaction.collectIsFocusedAsState()
                    val shape = RoundedCornerShape(OTokens.Radius.radiusS)
                    androidx.compose.foundation.text.BasicTextField(
                        state = field,
                        modifier = Modifier.weight(1f).focusRequester(focus).contentReceiver(receiver),
                        textStyle = OieType.body.copy(color = c.ink),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(c.ink),
                        interactionSource = interaction,
                        lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
                        decorator = { inner ->
                            // The look of the field before v0.31: surface, a hairline (ink and thicker when focused).
                            Box(
                                Modifier.heightIn(min = OTokens.Spacing.space12 + OTokens.Spacing.space2).clip(shape).background(c.surface)
                                    .border(if (focused) OTokens.Size.hairline * 2 else OTokens.Size.hairline, if (focused) c.ink else c.line, shape)
                                    .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (field.text.isEmpty()) Text(if (compact) tx.writeShort else tx.writeTo(contact.name), style = OieType.body, color = c.muted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                inner()
                            }
                        },
                    )
                    if (draft.isNotBlank()) {
                        PrimaryButton(tx.send, {
                            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.TEXT, draft.trim()))
                            field.edit { replace(0, length, "") }; state.drafts.remove(contactId)
                        }, icon = R.drawable.gg_ic_send, iconOnly = compact)
                    }
                }
                if (canSend && draft.isBlank()) RecordButton(
                    recording = recordingSince > 0,
                    onStart = {
                        // v0.11: a real recording. Without the microphone permission, ask for it (the parent was asked at setup).
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            if (app.oie.data.Voice.start(context)) { micError = false; recordingSince = state.clock() } else micError = true
                        } else askMic.launch(android.Manifest.permission.RECORD_AUDIO)
                    },
                    onStop = { send ->
                        val secs = ((state.clock() - recordingSince) / 1000).toInt()
                        recordingSince = 0
                        // Under 1 s is a slip, not a message (flow branch 3a).
                        val media = app.oie.data.Voice.stop(context, keep = send && secs >= 1)
                        if (media != null) queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.VOICE, durationSec = secs, media = media))
                    },
                )
            }
        }
        pending?.let {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = OTokens.Size.record + OTokens.Spacing.space12)
                    .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.snackbar)
                    .padding(start = OTokens.Spacing.space6, end = OTokens.Spacing.space2),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
            ) {
                // v0.33 audit: with Ouvir on a phone, the line gives way so Desfazer always shows.
                Text(tx.sendingTo(contact.name), style = OieType.body, color = c.page, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                // v0.33: hear a voice message before it goes. No extra tap on the usual path: it still goes by itself.
                if (it.kind == Kind.VOICE && it.media != null) Box(
                    Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) { app.oie.data.Voice.toggle(context, it.id, it.media) }
                        .padding(horizontal = OTokens.Spacing.space3),
                    contentAlignment = Alignment.Center,
                ) { Text(if (hearing) tx.stopReading else tx.readAloud, style = OieType.label, color = c.page) }
                Box(
                    Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) {
                        // Undo puts the content back, so nothing is lost.
                        if (it.kind == Kind.TEXT) field.edit { replace(0, length, it.text) }
                        if (it.kind == Kind.PHOTO) preview = it.media // back to the preview, nothing lost
                        if (it.kind == Kind.VOICE) app.oie.data.Voice.stopPlaying()
                        pending = null
                    }.padding(horizontal = OTokens.Spacing.space3),
                    contentAlignment = Alignment.Center,
                ) { Text(tx.undo, style = OieType.label, color = c.page) }
            }
        }
        if (choosingPhoto) PhotoSourceSheet(first = state.lastPhotoSource, onPick = ::open, onClose = { choosingPhoto = false })
        if (micError) Text(
            tx.micError, style = OieType.body, color = c.coralText,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = OTokens.Size.header + OTokens.Spacing.space4)
                .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.coralSoft).padding(OTokens.Spacing.space3),
        )
        if (photoError) Text(
            tx.photoError, style = OieType.body, color = c.coralText,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = OTokens.Size.header + OTokens.Spacing.space4)
                .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.coralSoft).padding(OTokens.Spacing.space3),
        )
        preview?.let { name ->
            PhotoPreview(name, contact.name,
                onSwap = { choosingPhoto = true },
                onSend = {
                    preview = null
                    queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.PHOTO, media = name))
                },
                onBack = { Photos.forget(context, name); preview = null },
            )
        }
        if (!showPhotos) watching?.let { m -> VideoScreen(m.media, gif = m.kind == Kind.GIF, from = contact.name, onBack = { watching = null }) }
        videoDraft?.let { d ->
            LaunchedEffect(d.uri) {
                val r = app.oie.data.VideoPrep.prepare(context, d.uri, d.gif) { p -> videoDraft = videoDraft?.copy(progress = p) }
                videoDraft = when (r) {
                    is app.oie.data.VideoPrep.Result.Ready -> videoDraft?.copy(ready = r, progress = 1f)
                    is app.oie.data.VideoPrep.Result.Problem -> videoDraft?.copy(problem = r.why)
                }
            }
            VideoPreview(d, contact.name,
                onSwap = { d.ready?.let { Photos.forget(context, it.media) }; videoDraft = null; choosingPhoto = true },
                onSend = {
                    val r = d.ready ?: return@VideoPreview
                    videoDraft = null
                    queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), if (r.gif) Kind.GIF else Kind.VIDEO, media = r.media, durationSec = r.seconds))
                },
                onBack = { d.ready?.let { Photos.forget(context, it.media) }; videoDraft = null },
            )
        }
        stickerNote?.let { note ->
            LaunchedEffect(note) { delay(4_000); stickerNote = null }
            Text(
                note, style = OieType.body, color = c.ink,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = OTokens.Size.header + OTokens.Spacing.space4)
                    .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.surface)
                    .border(BorderStroke(OTokens.Size.hairline, c.line), RoundedCornerShape(OTokens.Radius.radiusS)).padding(OTokens.Spacing.space3),
            )
        }
        if (stickers) StickerSheet(state, onMake = { makeSticker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, onPick = { w ->
            stickers = false
            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.STICKER, w))
        }, onPickFamily = { s ->
            stickers = false
            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.STICKER, media = s.media))
        }, onClose = { stickers = false })
        menuFor?.let { m ->
            ReactionSheet(m,
                onReact = { e -> state.react(m.id, e); menuFor = null },
                onRead = if (m.kind == Kind.TEXT && m.text.isNotBlank()) ({ app.oie.data.ReadAloud.toggle(context, m.id, m.text); menuFor = null }) else null,
                onClose = { menuFor = null })
        }
        if (showPhotos) PhotosScreen(state, contact, onOpen = { m -> if (m.kind == Kind.PHOTO) bigPhoto = m.media else watching = m }, onBack = { showPhotos = false })
        bigPhoto?.let { PhotoScreen(it, contact.name) { bigPhoto = null } }
        // v0.29 put watching above; a video opened from Fotos shows over the grid.
        if (showPhotos) watching?.let { m -> VideoScreen(m.media, gif = m.kind == Kind.GIF, from = contact.name, onBack = { watching = null }) }
        if (drawing) DrawPad(contact.name, onDone = { bmp ->
            drawing = false
            val name = runCatching { Photos.store(context, bmp) }.getOrNull()
            photoError = name == null
            preview = name
        }, onBack = { drawing = false })
    }
}

/** Hold to record, release to send; tap to start and tap again to send for anyone who can't hold (WCAG 2.5.7). */
@Composable
private fun RecordButton(recording: Boolean, onStart: () -> Unit, onStop: (send: Boolean) -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Box(
        Modifier.size(OTokens.Size.record).scale(if (recording) 0.97f else 1f).clip(CircleShape).background(c.mint)
            .semantics {
                contentDescription = if (recording) tx.recordingA11y else tx.recordA11y
                onClick { if (recording) onStop(true) else onStart(); true }
            }
            .pointerInput(recording) {
                detectTapGestures(
                    onPress = {
                        if (recording) { onStop(true); return@detectTapGestures }
                        onStart()
                        val released = tryAwaitRelease()
                        onStop(released)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) { OIcon(R.drawable.gg_ic_mic, tint = c.onMint, size = OTokens.Spacing.space8) }
}

@Composable
private fun Bubble(m: Message, name: String, status: String? = null, parent: String = "", onMenu: (() -> Unit)? = null, onPhoto: (String) -> Unit = {}, onWatch: (Message) -> Unit = {}) {
    val c = OieTheme.colors
    val tx = str
    // v0.33: a long press opens the reactions (and Ouvir); TalkBack gets the same as its long-press action.
    val menu = if (onMenu == null || m.kind == Kind.DELETED) Modifier else Modifier
        .pointerInput(m.id) { detectTapGestures(onLongPress = { onMenu() }) }
        .semantics { onLongClick(label = tx.react) { onMenu(); true } }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromKid) Arrangement.End else Arrangement.Start) {
        Row(
            Modifier.widthIn(max = OTokens.Size.contentMax).then(menu)
                .clip(RoundedCornerShape(OTokens.Radius.radiusM))
                .background(if (m.fromKid) c.selected else c.surface)
                .then(if (m.fromKid) Modifier else Modifier.border(BorderStroke(OTokens.Size.hairline, c.line), RoundedCornerShape(OTokens.Radius.radiusM)))
                .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            val ink = if (m.fromKid) c.onSelected else c.ink
            when (m.kind) {
                Kind.VOICE -> {
                    // v0.11: plays the real recording when there is one; tap again to stop.
                    val context = LocalContext.current
                    val on = app.oie.data.Voice.playing == m.id
                    Box(
                        Modifier.size(OTokens.Spacing.space12).clip(androidx.compose.foundation.shape.CircleShape)
                            .clickable(enabled = m.media != null, role = Role.Button) { m.media?.let { app.oie.data.Voice.toggle(context, m.id, it) } },
                        contentAlignment = Alignment.Center,
                    ) { OIcon(if (on) R.drawable.gg_ic_pause else R.drawable.gg_ic_play, tint = ink, contentDescription = tx.listenA11y(m.durationSec)) }
                    Text("%d:%02d".format(m.durationSec / 60, m.durationSec % 60), style = OieType.caption, color = c.muted)
                }
                // A sticker from family's WhatsApp arrives as an image (media); Oiê's own are drawn from their word.
                // v0.27: an animated sticker moves (MovingImage), unless "Remove animations" is on.
                Kind.STICKER -> if (m.media != null) MovingImage(m.media, OTokens.Size.contactTile + OTokens.Spacing.space8, tx.notifSticker) else STICKERS[m.text]?.let {
                    Image(painterResource(it), contentDescription = tx.stickerA11y(m.text), modifier = Modifier.size(OTokens.Size.contactTile + OTokens.Spacing.space8))
                } ?: Text(m.text, style = OieType.title, color = ink)
                Kind.PHOTO -> Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    // v0.33: tap to see it big.
                    Box(if (m.media != null) Modifier.clickable(role = Role.Button, onClickLabel = tx.photo) { onPhoto(m.media) } else Modifier) { PhotoThumb(m.media) }
                    if (m.text.isNotBlank()) Text(m.text, style = OieType.body, color = ink)
                }
                // v0.27: videos (tap to watch) and GIFs (a silent loop).
                Kind.VIDEO -> Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    VideoBubble(m.media, m.durationSec, tooBig = !app.oie.logic.Moving.download(m.kind, m.size), parent = parent) { onWatch(m) }
                    if (m.text.isNotBlank()) Text(m.text, style = OieType.body, color = ink)
                }
                Kind.GIF -> Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    GifBubble(m.media) { onWatch(m) }
                    if (m.text.isNotBlank()) Text(m.text, style = OieType.body, color = ink)
                }
                Kind.UNSUPPORTED -> Text(tx.onlyInWhatsApp(name), style = OieType.body, color = c.muted)
                Kind.DELETED -> Text(tx.deletedMessage, style = OieType.body.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = c.muted)
                else -> Text(m.text, style = OieType.body, color = ink, modifier = Modifier.weight(1f, fill = false))
            }
            Text(status ?: hour(tx, m.at), style = OieType.caption, color = c.muted, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun StickerSheet(state: AppState, onMake: () -> Unit, onPick: (String) -> Unit, onPickFamily: (app.oie.logic.FamilySticker) -> Unit, onClose: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Box(Modifier.fillMaxSize().background(c.ink.copy(alpha = 0.45f)).clickable(onClick = onClose), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.widthIn(max = OTokens.Size.contentMax + OTokens.Spacing.space16 * 4)
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).clickable(enabled = false) {}.padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(tx.stickers, style = OieType.title, color = c.ink)
            val tile = OTokens.Size.contactTile + OTokens.Spacing.space6
            val saved = state.stickers.filter { it.saved }
            val fresh = state.stickers.filter { !it.saved }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                STICKERS.forEach { (w, res) ->
                    Image(
                        painterResource(res), contentDescription = tx.sendSticker(w),
                        modifier = Modifier.size(tile).clip(RoundedCornerShape(OTokens.Radius.radiusL))
                            .clickable(role = Role.Button) { onPick(w) },
                    )
                }
            }
            // Family stickers the kid saved (v0.10): tap to send, like Oiê's own.
            if (saved.isNotEmpty()) {
                Text(tx.familyStickers, style = OieType.label, color = c.ink)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                    saved.forEach { s ->
                        FamilyStickerTile(s.media, tile, Modifier.clickable(role = Role.Button) { onPickFamily(s) }.semantics { contentDescription = tx.sendFamilySticker })
                    }
                }
            }
            // New ones from the parent's own chat: the kid chooses which to keep.
            if (fresh.isNotEmpty()) {
                Text(tx.newStickersFrom(state.parentName), style = OieType.label, color = c.ink)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                    fresh.forEach { s ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FamilyStickerTile(s.media, tile)
                            TextLink(tx.keepSticker, { state.saveSticker(s.id) })
                        }
                    }
                }
            }
            // v0.33: a sticker from a photo, kept with the family ones.
            QuietButton(tx.makeSticker, onMake, Modifier.height(OTokens.Size.button), icon = R.drawable.gg_ic_plus)
        }
    }
}

/** A family sticker on the tile Oiê's own use (page colour, large radius). Empty if the file is missing. */
@Composable
private fun FamilyStickerTile(media: String, side: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // v0.27: an animated family sticker moves in the sheet too.
    Box(modifier.size(side).clip(RoundedCornerShape(OTokens.Radius.radiusL)).background(OieTheme.colors.page), contentAlignment = Alignment.Center) {
        MovingImage(media, side - OTokens.Spacing.space4, contentDescription = null)
    }
}

private fun hour(tx: app.oie.ui.text.Strings, at: Long): String {
    val t = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalTime()
    return tx.clock(t.hour, t.minute).let { if (t.minute == 0 && it.endsWith("h")) it + "00" else it }
}


/** "Câmera" or "Galeria", the last one used first (flow: Remembers). */
@Composable
internal fun PhotoSourceSheet(first: String, onPick: (String) -> Unit, onClose: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    // v0.28: a photo, a video, or the gallery (photos, videos and GIFs); the last one used first.
    // v0.33: Desenhar comes last, after the usual ones.
    val options = listOf("camera" to tx.takePhoto, "video" to tx.recordVideo, "gallery" to tx.gallery).sortedBy { if (it.first == first) 0 else 1 } + ("draw" to tx.draw)
    Box(Modifier.fillMaxSize().background(c.ink.copy(alpha = 0.45f)).clickable(onClick = onClose), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.widthIn(max = OTokens.Size.contentMax)
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).clickable(enabled = false) {}.padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(tx.photoOrVideo, style = OieType.title, color = c.ink)
            app.oie.ui.setup.ButtonRow {
                options.forEach { (key, label) ->
                    QuietButton(label, { onPick(key) }, Modifier.height(OTokens.Size.button), icon = when (key) { "gallery" -> R.drawable.gg_ic_image; "draw" -> null; else -> R.drawable.gg_ic_camera })
                }
            }
        }
    }
}

/** Prototype screen 17: the photo big, then Trocar or Enviar. Back drops it. */
@Composable
private fun PhotoPreview(name: String, to: String, onSwap: () -> Unit, onSend: () -> Unit, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val context = LocalContext.current
    val bmp = remember(name) { Photos.load(context, name, Photos.MAX_SIDE) }
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.photoFor(to), style = OieType.title, color = c.ink)
        }
        Column(
            Modifier.fillMaxSize().padding(OTokens.Spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6, Alignment.CenterVertically),
        ) {
            if (bmp != null) Image(
                bmp.asImageBitmap(), contentDescription = tx.photoPreviewA11y,
                modifier = Modifier.weight(1f, fill = false).clip(RoundedCornerShape(OTokens.Radius.radiusM)),
                contentScale = ContentScale.Fit,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                QuietButton(tx.swap, onSwap, Modifier.height(OTokens.Size.button))
                PrimaryButton(tx.send, onSend, icon = R.drawable.gg_ic_send)
            }
        }
    }
}

@Composable
private fun PhotoThumb(media: String?) {
    val c = OieTheme.colors
    val tx = str
    val context = LocalContext.current
    val bmp = remember(media) { media?.let { Photos.load(context, it, Photos.MAX_SIDE / 4) } }
    if (bmp == null) {
        Text(tx.photo, style = OieType.body, color = c.muted)
    } else {
        Image(
            bmp.asImageBitmap(), contentDescription = tx.photo,
            modifier = Modifier.size(OTokens.Size.contactTile * 2).clip(RoundedCornerShape(OTokens.Radius.radiusS)),
            contentScale = ContentScale.Crop,
        )
    }
}
