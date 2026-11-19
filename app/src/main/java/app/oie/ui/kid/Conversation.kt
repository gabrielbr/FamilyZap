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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
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
@Composable
fun Conversation(state: AppState, contactId: String, openStickers: Boolean = false) {
    val c = OieTheme.colors
    val tx = str
    // v0.21: paused or removed while open (a command from the parent's WhatsApp): back to the home screen.
    val contact = state.reachable.firstOrNull { it.id == contactId } ?: run {
        androidx.compose.runtime.LaunchedEffect(contactId) { state.route = app.oie.data.Route.KidHome }
        return
    }
    // v0.24: while the link reconnects the kid can still write; messages wait and go out on connect (L4).
    val linkDown = app.oie.ui.common.linkShown(state) == app.oie.logic.LinkView.Shown.DOWN
    val canSend = state.canSend(contact) && !linkDown
    var draft by remember { mutableStateOf(state.drafts[contactId] ?: "") }
    var pending by remember { mutableStateOf<Message?>(null) }
    var stickers by remember { mutableStateOf(openStickers) }
    var recordingSince by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    var choosingPhoto by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<String?>(null) }
    var photoError by remember { mutableStateOf(false) }
    var micError by remember { mutableStateOf(false) }
    /** The video or GIF open full screen (v0.27). */
    var watching by remember { mutableStateOf<Message?>(null) }
    fun received(uri: Uri?) {
        if (uri == null) return // the kid backed out of the picker: nothing changes
        val kept = Photos.keep(context, uri)
        photoError = kept == null
        preview?.let { Photos.forget(context, it) } // Trocar: the old pick is dropped
        preview = kept
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { received(it) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) received(cameraUri) }
    fun open(source: String) {
        choosingPhoto = false
        state.lastPhotoSource = source
        if (source == "camera") {
            // The camera app writes into one temporary file shared through the FileProvider; made only when needed.
            val uri = runCatching { FileProvider.getUriForFile(context, context.packageName + ".photos", Photos.newCameraTarget(context)) }.getOrNull()
            if (uri == null) { photoError = true; return }
            cameraUri = uri
            camera.launch(uri)
        } else {
            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
    val visible = state.visible(contactId)
    LaunchedEffect(contactId, visible.size) { state.lastSeen[contactId] = state.clock() }

    fun queue(m: Message) { pending = m }
    LaunchedEffect(pending) {
        val m = pending ?: return@LaunchedEffect
        delay(UNDO_MS)
        state.messages += m
        state.wa.send(m)
        pending = null
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Bar {
                QuietButton(null, { state.drafts[contactId] = draft; state.route = Route.KidHome }, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
                app.oie.ui.common.Avatar(contact, OTokens.Size.avatar)
                Text(contact.name, style = OieType.title, color = c.ink)
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
                    Bubble(m, contact.name, status = if (failed) tx.notSent else null, parent = state.parentName) { watching = it }
                    // Rule L4: one automatic retry already happened; now the kid can try again.
                    if (failed) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextLink(tx.tryAgain, { state.failed.remove(m.id); state.wa.send(m) })
                    }
                }
                pending?.let { p -> item(key = "pending") { Bubble(p, contact.name, status = tx.sending) } }
                visible.lastOrNull { it.fromKid }?.takeIf { pending == null && visible.last().fromKid && it.id !in state.failed }?.let { last ->
                    item { Text(tx.sentAt(hour(tx, last.at)), style = OieType.caption, color = c.muted, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
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
                    QuietButton(null, { choosingPhoto = true }, icon = R.drawable.gg_ic_camera, contentDescription = tx.photoButton)
                    QuietButton(null, { stickers = true }, icon = R.drawable.gg_ic_sticker, contentDescription = tx.stickerButton)
                    OutlinedTextField(
                        draft, { draft = it; state.drafts[contactId] = it }, Modifier.weight(1f),
                        placeholder = { Text(tx.writeTo(contact.name), style = OieType.body, color = c.muted) },
                        textStyle = OieType.body,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.ink, unfocusedTextColor = c.ink, focusedContainerColor = c.surface,
                            unfocusedContainerColor = c.surface, focusedBorderColor = c.ink, unfocusedBorderColor = c.line, cursorColor = c.ink,
                        ),
                    )
                    if (draft.isNotBlank()) {
                        PrimaryButton(tx.send, {
                            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.TEXT, draft.trim()))
                            draft = ""; state.drafts.remove(contactId)
                        }, icon = R.drawable.gg_ic_send)
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
                Text(tx.sendingTo(contact.name), style = OieType.body, color = c.page)
                Box(
                    Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) {
                        // Undo puts the content back, so nothing is lost.
                        if (it.kind == Kind.TEXT) draft = it.text
                        if (it.kind == Kind.PHOTO) preview = it.media // back to the preview, nothing lost
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
        watching?.let { m -> VideoScreen(m.media, gif = m.kind == Kind.GIF, from = contact.name, onBack = { watching = null }) }
        if (stickers) StickerSheet(state, onPick = { w ->
            stickers = false
            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.STICKER, w))
        }, onPickFamily = { s ->
            stickers = false
            queue(Message("k${System.nanoTime()}", contactId, true, state.clock(), Kind.STICKER, media = s.media))
        }, onClose = { stickers = false })
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
private fun Bubble(m: Message, name: String, status: String? = null, parent: String = "", onWatch: (Message) -> Unit = {}) {
    val c = OieTheme.colors
    val tx = str
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromKid) Arrangement.End else Arrangement.Start) {
        Row(
            Modifier.widthIn(max = OTokens.Size.contentMax)
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
                    PhotoThumb(m.media)
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
private fun StickerSheet(state: AppState, onPick: (String) -> Unit, onPickFamily: (app.oie.logic.FamilySticker) -> Unit, onClose: () -> Unit) {
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
private fun PhotoSourceSheet(first: String, onPick: (String) -> Unit, onClose: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val options = listOf("camera" to tx.camera, "gallery" to tx.gallery).sortedBy { if (it.first == first) 0 else 1 }
    Box(Modifier.fillMaxSize().background(c.ink.copy(alpha = 0.45f)).clickable(onClick = onClose), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.widthIn(max = OTokens.Size.contentMax)
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).clickable(enabled = false) {}.padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(tx.photo, style = OieType.title, color = c.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                options.forEach { (key, label) ->
                    QuietButton(label, { onPick(key) }, Modifier.height(OTokens.Size.button), icon = if (key == "camera") R.drawable.gg_ic_camera else R.drawable.gg_ic_image)
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
