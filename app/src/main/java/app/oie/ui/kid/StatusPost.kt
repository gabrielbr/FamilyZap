package app.oie.ui.kid

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Photos
import app.oie.logic.Kind
import app.oie.logic.Statuses
import app.oie.ui.common.Bar
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/**
 * The kid posts a status (docs/flows/statuses.md, v0.37): a sheet that says who sees it, then a photo,
 * a video (a minute at most) or a text, then Postar. It goes only to the people in Contatos.
 * [writing] lets the screenshot tests open the text writer.
 */
@Composable
internal fun StatusPoster(state: AppState, onClose: () -> Unit, writing: Boolean = false) {
    val tx = str
    val context = LocalContext.current
    var write by remember { mutableStateOf(writing) }
    var photo by remember { mutableStateOf<String?>(null) }
    var video by remember { mutableStateOf<VideoDraft?>(null) }
    var photoError by remember { mutableStateOf(false) }
    var choosing by remember { mutableStateOf(!writing) }

    fun received(uri: Uri?) {
        if (uri == null) { choosing = true; return } // backed out of the picker: the sheet again
        val type = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
        if (type.startsWith("video/") || type == "image/gif") { video = VideoDraft(uri, gif = type == "image/gif"); return }
        val kept = Photos.keep(context, uri)
        photoError = kept == null
        if (kept == null) choosing = true
        photo = kept
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { received(it) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) received(cameraUri) else choosing = true }
    var clipUri by remember { mutableStateOf<Uri?>(null) }
    val recorder = rememberLauncherForActivityResult(object : ActivityResultContracts.CaptureVideo() {
        override fun createIntent(context: android.content.Context, input: Uri) =
            super.createIntent(context, input).putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, Statuses.VIDEO_MAX_SEC)
    }) { ok -> if (ok) clipUri?.let { video = VideoDraft(it, gif = false) } else choosing = true }

    fun pick(source: String) {
        choosing = false
        photoError = false
        when (source) {
            "write" -> write = true
            "camera" -> runCatching { FileProvider.getUriForFile(context, context.packageName + ".photos", Photos.newCameraTarget(context)) }.getOrNull()
                ?.let { cameraUri = it; camera.launch(it) } ?: run { photoError = true; choosing = true }
            "video" -> runCatching { FileProvider.getUriForFile(context, context.packageName + ".photos", Photos.newVideoTarget(context)) }.getOrNull()
                ?.let { clipUri = it; recorder.launch(it) } ?: run { photoError = true; choosing = true }
            else -> gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
    }

    when {
        write -> StatusWriter(state, onPost = { text -> state.postStatus(Kind.TEXT, text = text); onClose() }, onBack = { write = false; choosing = true })
        photo != null -> PhotoPreview(photo!!, tx.forFamily,
            onSwap = { photo?.let { Photos.forget(context, it) }; photo = null; choosing = true },
            onSend = { state.postStatus(Kind.PHOTO, media = photo); photo = null; onClose() },
            onBack = { photo?.let { Photos.forget(context, it) }; photo = null; onClose() },
            action = tx.postStatus)
        video != null -> {
            val d = video!!
            LaunchedEffect(d.uri) {
                val r = app.oie.data.VideoPrep.prepare(context, d.uri, d.gif) { p -> video = video?.copy(progress = p) }
                video = when (r) {
                    is app.oie.data.VideoPrep.Result.Ready -> video?.copy(ready = r, progress = 1f)
                    is app.oie.data.VideoPrep.Result.Problem -> video?.copy(problem = r.why)
                }
            }
            VideoPreview(d, tx.forFamily,
                onSwap = { d.ready?.let { Photos.forget(context, it.media) }; video = null; choosing = true },
                onSend = {
                    val r = d.ready ?: return@VideoPreview
                    video = null
                    state.postStatus(Kind.VIDEO, media = r.media, durationSec = r.seconds)
                    onClose()
                },
                onBack = { d.ready?.let { Photos.forget(context, it.media) }; video = null; onClose() },
                action = tx.postStatus, maxSec = Statuses.VIDEO_MAX_SEC)
        }
        choosing -> PostSheet(state, photoError, onPick = ::pick, onClose = onClose)
    }
}

/** Why the kid can't post right now, or null: nobody can see it, quiet hours, or the link is down. */
@Composable
private fun blocked(state: AppState): String? {
    val tx = str
    return when {
        state.statusAudience().isEmpty() -> tx.nobodyToSee
        app.oie.ui.common.linkShown(state) == app.oie.logic.LinkView.Shown.DOWN -> tx.askReconnect(state.parentName)
        else -> state.quietNow()?.let { tx.quietUntilShort(it.end.hour) }
    }
}

/** "Postar uma novidade": who sees it, then the four ways (or why not now). */
@Composable
internal fun PostSheet(state: AppState, photoError: Boolean, onPick: (String) -> Unit, onClose: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val why = blocked(state)
    app.oie.ui.common.BottomSheet(onClose) {
        Text(tx.postTitle, style = OieType.title, color = c.ink)
        if (why != null) {
            Text(why, style = OieType.body, color = c.ink)
            QuietButton(tx.close, onClose)
        } else {
            Text(tx.whoSees(tx.and(state.statusAudience().map { it.name })), style = OieType.body, color = c.muted)
            if (photoError) Text(tx.photoError, style = OieType.body, color = c.coralText)
            app.oie.ui.setup.ButtonRow {
                listOf("camera" to tx.takePhoto, "video" to tx.recordVideo, "gallery" to tx.gallery, "write" to tx.writeStatus)
                    .forEach { (key, label) -> QuietButton(label, { onPick(key) }) }
            }
        }
    }
}

/** A text status: as it will look (on the `selected` ground), the field, who sees it, and Postar. */
@Composable
internal fun StatusWriter(state: AppState, onPost: (String) -> Unit, onBack: () -> Unit, start: String = "") {
    val c = OieTheme.colors
    val tx = str
    var text by remember { mutableStateOf(start) }
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.postTitle, style = OieType.title, color = c.ink)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
        ) {
            Column(Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
                // What the family will see; the field below writes it.
                Box(
                    Modifier.fillMaxWidth().heightIn(min = OTokens.Size.contactTile * 2).clip(RoundedCornerShape(OTokens.Radius.radiusM))
                        .background(c.selected).padding(OTokens.Spacing.space6),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text.ifBlank { tx.statusLabel }, style = OieType.title, color = if (text.isBlank()) c.muted else c.ink, textAlign = TextAlign.Center)
                }
                app.oie.ui.setup.Field(tx.statusLabel, text, { text = it.take(Statuses.TEXT_MAX) }, tx.whoSees(tx.and(state.statusAudience().map { it.name })))
                app.oie.ui.setup.ButtonRow {
                    PrimaryButton(tx.postStatus, { onPost(text) }, enabled = text.isNotBlank(), icon = R.drawable.gg_ic_send)
                    QuietButton(tx.cancel, onBack)
                }
            }
        }
    }
}
