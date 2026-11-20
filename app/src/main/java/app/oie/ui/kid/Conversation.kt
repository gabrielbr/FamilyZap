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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.foundation.lazy.itemsIndexed
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
fun Conversation(
    state: AppState, contactId: String, openStickers: Boolean = false, inBubble: Boolean = false, onLeave: (() -> Unit)? = null,
    start: app.oie.data.Start = app.oie.data.Start.NONE,
    pendingPreview: Message? = null, recordingPreviewSec: Int = -1, slipPreview: Boolean = false, tapPreview: Boolean = false,
) {
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
    // The *Preview parameters let the screenshot tests show the recording and slip states.
    var recordingSince by remember { mutableLongStateOf(if (recordingPreviewSec >= 0) state.clock() - recordingPreviewSec * 1000L else 0L) }
    /** The kid slid off the record button: releasing now cancels (v0.34). */
    var away by remember { mutableStateOf(false) }
    val context = LocalContext.current
    /** v0.38: the Anexar sheet (photo, video, gallery, drawing and the stickers) is open. */
    var choosingPhoto by remember { mutableStateOf(openStickers) }
    /** v0.38: a recording started by a tap (Gravar áudio on the home, or TalkBack): tap the button to send, no holding. */
    var byTap by remember { mutableStateOf(tapPreview) }
    /** v0.38: Gravar áudio on the home asked for the microphone; when it's allowed, recording starts. */
    var recordWhenAllowed by remember { mutableStateOf(false) }
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
    // v0.38: leaving the chat (Back, Home) mid-recording throws the recording away, so the microphone never stays on.
    androidx.compose.runtime.DisposableEffect(contactId) { onDispose { app.oie.data.ReadAloud.stop(); app.oie.data.Voice.stop(context, keep = false) } }
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
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // v0.38: from the home's Gravar áudio, the recording starts once the microphone is allowed.
        if (granted && recordWhenAllowed) {
            if (app.oie.data.Voice.start(context)) { micError = false; byTap = true; recordingSince = state.clock() } else micError = true
        }
        recordWhenAllowed = false
    }
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
            choosingPhoto = true
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
    // v0.34 audit: the visible list is worked out when messages change, not on every keystroke.
    val visible by remember(contactId) { androidx.compose.runtime.derivedStateOf { state.visible(contactId) } }
    // v0.34 audit (P1): what was unread when the chat opened, for the "Novas" line and where the chat opens.
    val seenAtOpen = remember(contactId) { state.lastSeen[contactId] ?: 0L }
    val firstUnread = remember(contactId, visible.size) { visible.indexOfFirst { !it.fromKid && it.at > seenAtOpen } }
    LaunchedEffect(contactId, visible.size) { state.lastSeen[contactId] = state.clock() }
    var slip by remember { mutableStateOf(slipPreview) }
    LaunchedEffect(slip) { if (slip) { delay(NOTE_MS); slip = false } }
    LaunchedEffect(micError, photoError) { if (micError || photoError) { delay(NOTE_MS); micError = false; photoError = false } }

    fun queue(m: Message) { pending = m }
    // v0.38: a card on the home opened this chat to record: it starts at once, by tap (the mic sends, Cancelar drops it).
    // The route forgets the request, so coming back to the chat later doesn't record again.
    LaunchedEffect(Unit) {
        if (start != app.oie.data.Start.RECORD) return@LaunchedEffect
        if (state.route == Route.Conversation(contactId, start)) state.route = Route.Conversation(contactId)
        if (!canSend) return@LaunchedEffect
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (app.oie.data.Voice.start(context)) { micError = false; byTap = true; recordingSince = state.clock() } else micError = true
        } else { recordWhenAllowed = true; askMic.launch(android.Manifest.permission.RECORD_AUDIO) }
    }
    /** Ends a recording: sends it (if it's a second or more) or throws it away. */
    fun stopRecording(send: Boolean) {
        if (recordingSince == 0L) return
        val secs = ((state.clock() - recordingSince) / 1000).toInt()
        recordingSince = 0; away = false; byTap = false
        // Under 1 s is a slip, not a message (flow branch 3a); v0.34 audit: it says how to record.
        if (send && secs < 1) slip = true
        val media = app.oie.data.Voice.stop(context, keep = send && secs >= 1)
        if (media != null) queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.VOICE, durationSec = secs, media = media))
    }
    // v0.33: listening to a voice message before it goes (its ▶ in the chat) pauses the 5 seconds; they start again when it ends.
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
                // Fresh review: the same letters as on the home tile ("Vó", "Vô").
                app.oie.ui.common.Avatar(contact, OTokens.Size.avatar, letters = app.oie.logic.Initials.of(state.reachable.map { it.name })[contact.name])
                Text(contact.name, style = OieType.title, color = c.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                // v0.33: the photos, videos and GIFs in this chat. A word on the tablet, the icon alone on a phone.
                // v0.34 audit round 2: "Fotos" keeps its word on a phone too (README §9; decision 0010 covers the composer only).
                QuietButton(tx.photos, { showPhotos = true }, icon = R.drawable.gg_ic_image, contentDescription = tx.photosWith(contact.name))
            }
            val list = rememberLazyListState()
            // v0.34 audit (P1): the chat opens on the first unread message (under a "Novas" line), else on the newest;
            // a new message or the kid's own send brings the newest into view.
            LaunchedEffect(contactId) {
                androidx.compose.runtime.withFrameNanos { }
                list.scrollToItem(if (firstUnread >= 0) firstUnread + 1 else (list.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
            }
            LaunchedEffect(visible.size, pending) {
                androidx.compose.runtime.withFrameNanos { }
                val last = list.layoutInfo.totalItemsCount - 1
                val nearEnd = (list.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= last - 3
                if (last >= 0 && pending != null) list.scrollToItem(last) else if (last >= 0 && nearEnd) list.animateScrollToItem(last)
            }
            // v0.34 audit round 2: while the kid is at the end, the end stays in view as pictures arrive and grow.
            // Round 3: a chat that opened on "Novas" keeps its place; only the kid's own drag decides whether to follow the end.
            var atEnd by remember { mutableStateOf(firstUnread < 0) }
            LaunchedEffect(list) {
                list.interactionSource.interactions.collect { i ->
                    if (i is androidx.compose.foundation.interaction.DragInteraction.Stop || i is androidx.compose.foundation.interaction.DragInteraction.Cancel) {
                        kotlinx.coroutines.delay(MOTION_SETTLE_MS); atEnd = !list.canScrollForward
                    }
                }
            }
            LaunchedEffect(list) {
                androidx.compose.runtime.snapshotFlow { list.canScrollForward }.collect { more ->
                    if (more && atEnd && !list.isScrollInProgress) list.scrollToItem((list.layoutInfo.totalItemsCount - 1).coerceAtLeast(0), Int.MAX_VALUE / 2)
                }
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin),
                state = list, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
                // Room under the last message for the undo snackbar, so it never covers what's being sent.
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = if (pending != null) OTokens.Spacing.space16 + OTokens.Spacing.space12 else OTokens.Spacing.space4),
            ) {
                item { Box(Modifier.height(OTokens.Spacing.space6)) }
                if (visible.isEmpty() && pending == null) item { Text(tx.firstHi(contact.name), style = OieType.body, color = c.muted) }
                itemsIndexed(visible, key = { _, m -> m.id }) { i, m ->
                    if (i == firstUnread) NewLine()
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
                // v0.34 audit: the snackbar says "Enviando para…", so the message itself keeps just its time.
                pending?.let { p -> item(key = "pending") { Bubble(p, contact.name) } }
                visible.lastOrNull { it.fromKid }?.takeIf { pending == null && visible.last().fromKid && it.id !in state.failed }?.let { last ->
                    // v0.33: "Enviada", then "Entregue", then "Vovó viu" (in a group, "Alguém viu").
                    val group = contact.isGroup && !contact.parentLine
                    item { Text(tx.receiptLine(last.receipt, contact.name, group), style = OieType.caption, color = c.muted, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
                }
                // v0.34 audit: reactions were found only by chance; a one-line hint until the kid has reacted once,
                // at the end of the chat (round 2: not between messages).
                if (!state.reactTipUsed && visible.any { !it.fromKid && it.kind != Kind.DELETED }) item(key = "react-tip") {
                    Text(tx.reactHint, style = OieType.caption, color = c.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
            // Round 4: the slip line sits above the composer, so the kid can still type or tap meanwhile.
            if (slip) Text(tx.holdToRecord, style = OieType.body, color = c.ink,
                modifier = Modifier.fillMaxWidth().padding(start = app.oie.ui.common.sideMargin, end = app.oie.ui.common.sideMargin, top = OTokens.Spacing.space3)
                    .semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
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
                    // v0.34 audit: a running clock while recording, and what releasing will do (send, or cancel when slid away).
                    var now by remember { mutableLongStateOf(state.clock()) }
                    LaunchedEffect(recordingSince) {
                        while (true) {
                            now = state.clock()
                            // The flow's limit: 2 minutes, then it goes by itself.
                            if (now - recordingSince >= MAX_RECORDING_MS) { stopRecording(send = true); break }
                            delay(TICK_MS)
                        }
                    }
                    val left = ((MAX_RECORDING_MS - (now - recordingSince)) / 1000).toInt()
                    // The last 10 seconds count down, in the error colour (with the word, never colour alone).
                    Text(if (left <= 10) tx.recordingEnds(left.coerceAtLeast(0)) else tx.recording + " · " + clockOf(((now - recordingSince) / 1000).toInt().coerceAtLeast(0)),
                        style = OieType.label, color = if (left <= 10) c.coralText else c.ink, modifier = Modifier.weight(1f))
                    // Round 3: on a phone the hint shows only when it matters (slid off), so the clock and Cancelar fit.
                    // v0.38: started by a tap, nothing is held: the hint says to tap the button.
                    if (away || !app.oie.ui.common.isCompact) Text(if (away) tx.releaseToCancel else if (byTap) tx.tapToSend else tx.releaseToSend, style = OieType.bodySmall, color = if (away) c.coralText else c.muted)
                    // v0.34 audit round 2: a recording started by a tap (TalkBack) has a way out; with a finger on
                    // the button, sliding off cancels.
                    QuietButton(tx.cancel, { app.oie.data.Voice.stop(context, keep = false); recordingSince = 0; away = false; byTap = false })
                } else {
                    // v0.38: one Anexar button (photo, video, gallery, drawing, stickers) instead of Foto and Figurinha.
                    // v0.29 audit: on a narrow screen (a phone, a bubble) it steps aside while the kid types, so the
                    // field has room; it comes back when the field is empty. A word on the tablet; on a phone the icon
                    // alone, labelled for TalkBack (decision 0010).
                    val compact = app.oie.ui.common.isCompact
                    if (!compact || draft.isBlank()) {
                        QuietButton(if (compact) null else tx.attach, { choosingPhoto = true }, icon = R.drawable.gg_ic_plus, contentDescription = tx.attachA11y)
                    }
                    // v0.29: in a chat's bubble the field is focused at once, so the keyboard comes up to answer.
                    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
                    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    // v0.38: Escrever on the home's card opens the chat the same way.
                    if (inBubble || start == app.oie.data.Start.WRITE) LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
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
                    onAway = { away = it },
                    onStart = { tap ->
                        byTap = tap
                        // v0.11: a real recording. Without the microphone permission, ask for it (the parent was asked at setup).
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            if (app.oie.data.Voice.start(context)) { micError = false; recordingSince = state.clock() } else micError = true
                        } else askMic.launch(android.Manifest.permission.RECORD_AUDIO)
                    },
                    onStop = { send -> stopRecording(send) },
                )
            }
        }
        // v0.34 audit: the design system's snackbar, one action (Desfazer). Hearing a voice message before it goes
        // is its ▶ in the chat (it was a second action here, a duplicate).
        pending?.let {
            app.oie.ui.common.Snackbar(tx.sendingTo(contact.name), Modifier.align(Alignment.BottomCenter).padding(bottom = OTokens.Size.record + OTokens.Spacing.space8), action = tx.undo) {
                // Undo puts the content back, so nothing is lost; for a voice message, undoing is not sending it.
                if (it.kind == Kind.TEXT) field.edit { replace(0, length, it.text) }
                if (it.kind == Kind.PHOTO) preview = it.media
                if (it.kind == Kind.VOICE) app.oie.data.Voice.stopPlaying()
                pending = null
            }
        }
        if (choosingPhoto) AttachSheet(state, first = state.lastPhotoSource, onPick = ::open, onMake = { makeSticker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, onSticker = { w ->
            choosingPhoto = false
            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.STICKER, w))
        }, onFamilySticker = { s ->
            choosingPhoto = false
            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.STICKER, media = s.media))
        }, onClose = { choosingPhoto = false })
        // v0.34 audit: problems are announced, go by themselves, and a tap puts them away.
        if (micError || photoError) Text(
            if (micError) tx.micError else tx.photoError, style = OieType.body, color = c.coralText,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = OTokens.Size.header + OTokens.Spacing.space4, start = app.oie.ui.common.sideMargin, end = app.oie.ui.common.sideMargin)
                .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.coralSoft)
                .clickable(onClickLabel = tx.close) { micError = false; photoError = false }
                .semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }
                .padding(OTokens.Spacing.space3),
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
        menuFor?.let { m ->
            ReactionSheet(m,
                onReact = { e -> state.react(m.id, e); menuFor = null },
                onRead = if (m.kind == Kind.TEXT && m.text.isNotBlank()) ({ app.oie.data.ReadAloud.toggle(context, m.id, m.text); menuFor = null }) else null,
                onClose = { menuFor = null }, lastUsed = state.lastReaction)
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
private fun RecordButton(recording: Boolean, onStart: (tap: Boolean) -> Unit, onStop: (send: Boolean) -> Unit, onAway: (Boolean) -> Unit) {
    val c = OieTheme.colors
    val tx = str
    // v0.34 audit: one gesture for the whole press (it was restarted mid-press when recording began, which could
    // lose the release). Hold to record, release to send, slide off the button to cancel (README §10); a second
    // tap stops a recording started by TalkBack's tap.
    val isRecording by androidx.compose.runtime.rememberUpdatedState(recording)
    val start by androidx.compose.runtime.rememberUpdatedState(onStart)
    val stop by androidx.compose.runtime.rememberUpdatedState(onStop)
    val away by androidx.compose.runtime.rememberUpdatedState(onAway)
    Box(
        Modifier.size(OTokens.Size.record).scale(if (recording) 0.97f else 1f).clip(CircleShape).background(c.mint)
            .semantics {
                role = Role.Button
                contentDescription = if (recording) tx.recordingA11y else tx.recordA11y
                onClick { if (recording) onStop(true) else onStart(true); true }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (isRecording) { stop(true); return@awaitEachGesture }
                    start(false)
                    val slop = viewConfiguration.touchSlop * 2
                    var off = false
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val p = change.position
                        val outside = p.x < -slop || p.y < -slop || p.x > size.width + slop || p.y > size.height + slop
                        if (outside != off) { off = outside; away(off) }
                    }
                    stop(!off)
                }
            },
        contentAlignment = Alignment.Center,
    ) { OIcon(R.drawable.gg_ic_mic, tint = c.onMint, size = OTokens.Spacing.space8) }
}

/** "Novas": where the unread messages start (v0.34 audit, flow kid-receives-messages.md). */
@Composable
private fun NewLine() {
    val c = OieTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = OTokens.Spacing.space2), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
        Box(Modifier.weight(1f).height(OTokens.Size.hairline).background(c.line))
        Text(str.newMessages, style = OieType.caption, color = c.muted)
        Box(Modifier.weight(1f).height(OTokens.Size.hairline).background(c.line))
    }
}

/** How long a short note (a slip, a problem) stays; and the recording clock's tick. */
private const val NOTE_MS = 4_000L
private const val TICK_MS = 500L
/** A fling's settle time before reading where the list ended up. */
private const val MOTION_SETTLE_MS = 600L
/** The longest voice message (flow kid-sends-message, 3a). */
private const val MAX_RECORDING_MS = 120_000L

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
            // v0.34 audit: beside a picture or a video the time sits at the bottom, like a caption.
            verticalAlignment = if (m.kind in setOf(Kind.PHOTO, Kind.VIDEO, Kind.GIF, Kind.STICKER)) Alignment.Bottom else Alignment.CenterVertically,
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
                    ) { OIcon(if (on) R.drawable.gg_ic_pause else R.drawable.gg_ic_play, tint = ink, contentDescription = if (on) tx.pauseA11y else tx.listenA11y(m.durationSec)) }
                    Text("%d:%02d".format(m.durationSec / 60, m.durationSec % 60), style = OieType.caption, color = c.muted)
                }
                // A sticker from family's WhatsApp arrives as an image (media); Oiê's own are drawn from their word.
                // v0.27: an animated sticker moves (MovingImage), unless "Remove animations" is on.
                Kind.STICKER -> if (m.media != null) MovingImage(m.media, OTokens.Size.contactTile + OTokens.Spacing.space8, tx.notifSticker) else STICKERS[m.text]?.let {
                    Image(painterResource(it), contentDescription = tx.stickerA11y(m.text), modifier = Modifier.size(OTokens.Size.contactTile + OTokens.Spacing.space8))
                } ?: Text(m.text, style = OieType.title, color = ink)
                // v0.34 audit round 2: media columns give way like text does, so the time beside them is never cut.
                Kind.PHOTO -> Column(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    // v0.33: tap to see it big.
                    Box(if (m.media != null) Modifier.clickable(role = Role.Button, onClickLabel = tx.photo) { onPhoto(m.media) } else Modifier) { PhotoThumb(m.media) }
                    if (m.text.isNotBlank()) Text(m.text, style = OieType.body, color = ink)
                }
                // v0.27: videos (tap to watch) and GIFs (a silent loop).
                Kind.VIDEO -> Column(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    VideoBubble(m.media, m.durationSec, tooBig = !app.oie.logic.Moving.download(m.kind, m.size), parent = parent) { onWatch(m) }
                    if (m.text.isNotBlank()) Text(m.text, style = OieType.body, color = ink)
                }
                Kind.GIF -> Column(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    GifBubble(m.media) { onWatch(m) }
                    if (m.text.isNotBlank()) Text(m.text, style = OieType.body, color = ink)
                }
                Kind.UNSUPPORTED -> Text(tx.onlyInWhatsApp(name), style = OieType.body, color = c.muted)
                Kind.DELETED -> Text(tx.deletedMessage, style = OieType.body.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = c.muted)
                // v0.36: an answer to a status says so above the text.
                else -> Column(Modifier.weight(1f, fill = false)) {
                    if (m.statusQuote.isNotEmpty()) Text(m.statusQuote, style = OieType.caption, color = c.muted)
                    Text(m.text, style = OieType.body, color = ink)
                }
            }
            Text(status ?: hour(tx, m.at), style = OieType.caption, color = c.muted, maxLines = 1, softWrap = false)
        }
    }
}

/** v0.38: the stickers, inside the Anexar sheet: Oiê's set, the family ones kept, new ones to keep, and making one. */
@Composable
private fun StickerRows(state: AppState, onMake: () -> Unit, onPick: (String) -> Unit, onPickFamily: (app.oie.logic.FamilySticker) -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.stickers, style = OieType.label, color = c.ink)
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
internal fun AttachSheet(
    state: AppState, first: String, onPick: (String) -> Unit, onMake: () -> Unit, onSticker: (String) -> Unit,
    onFamilySticker: (app.oie.logic.FamilySticker) -> Unit, onClose: () -> Unit,
) {
    val c = OieTheme.colors
    val tx = str
    // v0.28: a photo, a video, or the gallery (photos, videos and GIFs); the last one used first.
    // v0.33: Desenhar comes last, after the usual ones.
    // v0.38: one sheet for everything the kid can attach, the stickers below: a sticker is still two taps.
    val options = listOf("camera" to tx.takePhoto, "video" to tx.recordVideo, "gallery" to tx.galleryShort).sortedBy { if (it.first == first) 0 else 1 } + ("draw" to tx.draw)
    app.oie.ui.common.BottomSheet(onClose) {
        // It scrolls: at large text, or with new family stickers, it can be taller than a phone.
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
            Text(tx.attach, style = OieType.title, color = c.ink)
            app.oie.ui.setup.ButtonRow {
                options.forEach { (key, label) ->
                    // v0.34 audit round 2: words only, the same for all four (the set has no video or pencil icon,
                    // and a camera on "Gravar vídeo" misled).
                    QuietButton(label, { onPick(key) })
                }
            }
            // less-is-more: no line before the stickers; their "Figurinhas" label groups them.
            StickerRows(state, onMake, onSticker, onFamilySticker)
        }
    }
}

/** Prototype screen 17: the photo big, then Trocar or Enviar. Back drops it. */
@Composable
internal fun PhotoPreview(name: String, to: String, onSwap: () -> Unit, onSend: () -> Unit, onBack: () -> Unit, action: String? = null) {
    val c = OieTheme.colors
    val tx = str
    val context = LocalContext.current
    val bmp = rememberPhoto(name, Photos.MAX_SIDE)
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
            // Fresh review: the main button first, as on every other screen.
            Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                PrimaryButton(action ?: tx.send, onSend, icon = R.drawable.gg_ic_send)
                QuietButton(tx.swap, onSwap)
            }
        }
    }
}

@Composable
private fun PhotoThumb(media: String?) {
    val c = OieTheme.colors
    val tx = str
    val context = LocalContext.current
    val bmp = rememberPhoto(media, Photos.MAX_SIDE / 4)
    // v0.34 audit round 2: while it loads, a tile of the photo's size holds its place (so the chat doesn't jump).
    if (bmp == null && media != null) {
        Box(Modifier.size(OTokens.Size.contactTile * 2).clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.line).semantics { contentDescription = tx.photo })
    } else if (bmp == null) {
        Text(tx.photo, style = OieType.body, color = c.muted)
    } else {
        Image(
            bmp.asImageBitmap(), contentDescription = tx.photo,
            modifier = Modifier.size(OTokens.Size.contactTile * 2).clip(RoundedCornerShape(OTokens.Radius.radiusS)),
            contentScale = ContentScale.Crop,
        )
    }
}
