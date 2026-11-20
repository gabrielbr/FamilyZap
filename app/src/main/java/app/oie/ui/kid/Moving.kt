package app.oie.ui.kid

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import app.oie.data.BytesSource
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import app.oie.R
import app.oie.data.Diagnostics
import app.oie.data.Photos
import app.oie.ui.common.Bar
import app.oie.ui.common.OIcon
import app.oie.ui.common.QuietButton
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/*
 * Moving pictures in the chat (docs/flows/kid-receives-messages.md, v0.27): animated stickers,
 * GIFs (short silent videos) and videos. Files stay sealed on the tablet; they're opened into
 * memory to play, never written out in plain form.
 */

/** A sealed file opened into memory, off the main thread. Null while loading or if missing. */
@Composable
internal fun rememberBytes(media: String?): ByteArray? {
    val context = LocalContext.current
    var bytes by remember(media) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(media) { bytes = if (media == null) null else withContext(app.oie.data.Background.dispatcher) { Photos.openBytes(context, media) } }
    return bytes
}


/**
 * A sticker or picture that moves when its file is animated (an animated WebP, a GIF), in a loop.
 * Still on its first frame when the system's "Remove animations" is on, or before Android 9.
 */
@Composable
fun MovingImage(media: String, side: Dp, contentDescription: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val animate = app.oie.ui.common.animationsOn()
    val bytes = rememberBytes(media)
    // v0.31: WhatsApp's own animated stickers are Lottie animations (a zip), drawn with Lottie (decision 0008).
    if (bytes != null && app.oie.logic.Moving.isLottie(bytes)) {
        val composition = remember(media) {
            runCatching { com.airbnb.lottie.LottieCompositionFactory.fromZipStreamSync(java.util.zip.ZipInputStream(bytes.inputStream()), "sticker-$media").value }
                .getOrNull().also { if (it == null) Diagnostics.log("sticker: lottie couldn't be read") }
        }
        if (composition != null) {
            com.airbnb.lottie.compose.LottieAnimation(
                composition, isPlaying = animate, iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever,
                modifier = modifier.size(side).semantics { if (contentDescription != null) this.contentDescription = contentDescription },
            )
            return
        }
    }
    val moving = bytes != null && Build.VERSION.SDK_INT >= 28 && (app.oie.logic.Moving.isAnimatedWebp(bytes) || app.oie.logic.Moving.isGif(bytes))
    if (moving && Build.VERSION.SDK_INT >= 28) {
        val drawable = remember(bytes) {
            runCatching { ImageDecoder.decodeDrawable(ImageDecoder.createSource(java.nio.ByteBuffer.wrap(bytes))) }
                .onFailure { Diagnostics.log("sticker: couldn't decode (${it.javaClass.simpleName})") }.getOrNull()
        }
        if (drawable != null) {
            AndroidView(
                factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                update = { iv ->
                    iv.setImageDrawable(drawable); iv.contentDescription = contentDescription
                    (drawable as? AnimatedImageDrawable)?.let { if (animate) it.start() else it.stop() }
                },
                modifier = modifier.size(side),
            )
            return
        }
    }
    val px = with(androidx.compose.ui.platform.LocalDensity.current) { side.roundToPx() }
    // v0.34 audit: decoded off the main thread.
    val still = rememberPhoto(media, px)
    if (still != null) Image(still.asImageBitmap(), contentDescription = contentDescription, modifier = modifier.size(side))
    else Box(modifier.size(side))
}

/**
 * A video (or GIF) drawn into a TextureView by MediaPlayer, from memory. [play] starts and pauses
 * it; a GIF loops without sound. [onProgress] gets the position and the length in ms.
 */
@Composable
private fun VideoSurface(
    bytes: ByteArray, loop: Boolean, muted: Boolean, play: Boolean, modifier: Modifier = Modifier,
    onProgress: (Int, Int) -> Unit = { _, _ -> }, onEnd: () -> Unit = {},
) {
    var ratio by remember { mutableFloatStateOf(16f / 9f) }
    var prepared by remember(bytes) { mutableStateOf(false) }
    val player = remember(bytes) { MediaPlayer() }
    DisposableEffect(player) { onDispose { runCatching { player.release() } } }
    LaunchedEffect(play, prepared) {
        if (!prepared) return@LaunchedEffect
        runCatching { if (play) player.start() else if (player.isPlaying) player.pause() }
        while (play && prepared) { runCatching { onProgress(player.currentPosition, player.duration) }; delay(250) }
    }
    AndroidView(
        modifier = modifier.aspectRatio(ratio),
        factory = { ctx ->
            TextureView(ctx).apply {
                var opened = false
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: android.graphics.SurfaceTexture, w: Int, h: Int) {
                        runCatching {
                            player.setSurface(Surface(st))
                            if (opened) return
                            opened = true
                            player.setDataSource(BytesSource(bytes))
                            player.isLooping = loop
                            if (muted) player.setVolume(0f, 0f)
                            player.setOnVideoSizeChangedListener { _, vw, vh -> if (vw > 0 && vh > 0) ratio = vw.toFloat() / vh }
                            player.setOnCompletionListener { onEnd() }
                            player.setOnPreparedListener { prepared = true; runCatching { onProgress(0, it.duration) } }
                            player.prepareAsync()
                        }.onFailure { Diagnostics.log("video: couldn't play (${it.javaClass.simpleName})") }
                    }
                    override fun onSurfaceTextureSizeChanged(st: android.graphics.SurfaceTexture, w: Int, h: Int) {}
                    override fun onSurfaceTextureDestroyed(st: android.graphics.SurfaceTexture): Boolean { runCatching { player.setSurface(null) }; return true }
                    override fun onSurfaceTextureUpdated(st: android.graphics.SurfaceTexture) {}
                }
            }
        },
    )
}

/** A video's first frame, from memory (null if Android can't read it). */
@Composable
internal fun rememberFirstFrame(bytes: ByteArray?): android.graphics.Bitmap? {
    var frame by remember(bytes) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(bytes) {
        val b = bytes ?: return@LaunchedEffect
        frame = withContext(app.oie.data.Background.dispatcher) {
            runCatching { MediaMetadataRetriever().run { setDataSource(BytesSource(b)); val f = getFrameAtTime(0); release(); f } }.getOrNull()
        }
    }
    return frame
}

/** "0:42" */
fun clockOf(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/**
 * A video in the chat: its first frame with ▶ and the length. Tapping opens the player. Without the
 * file (still downloading, or too big to keep) it says so in words.
 */
@Composable
fun VideoBubble(media: String?, seconds: Int, tooBig: Boolean, parent: String, onWatch: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    if (media == null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
            // No ▶ on a video that can't play yet (v0.34 audit): it says it's on its way instead.
            Text(if (tooBig) tx.videoTooBig(parent) else tx.downloading(clockOf(seconds)), style = OieType.body, color = c.muted)
        }
        return
    }
    val frame = rememberVideoFrame(media)
    Box(
        // Half the column on a tablet; narrower when a phone has less room (v0.27 audit).
        Modifier.widthIn(max = OTokens.Size.contentMax / 2).fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(OTokens.Radius.radiusS))
            .background(c.line).clickable(role = Role.Button, onClick = onWatch)
            .semantics(mergeDescendants = true) { contentDescription = tx.playVideoA11y(seconds) },
        contentAlignment = Alignment.Center,
    ) {
        frame?.let { Image(it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.size(OTokens.Spacing.space12).clip(CircleShape).background(c.surface), contentAlignment = Alignment.Center) {
            OIcon(R.drawable.gg_ic_play, tint = c.ink)
        }
        Text(
            clockOf(seconds), style = OieType.caption, color = c.ink,
            modifier = Modifier.align(Alignment.BottomEnd).padding(OTokens.Spacing.space2)
                .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.surface).padding(horizontal = OTokens.Spacing.space2),
        )
    }
}

/**
 * A GIF in the chat: plays in a silent loop, with a small "GIF" label. With "Remove animations" on,
 * it shows its first frame and plays when tapped. Tapping opens it big either way.
 */
@Composable
fun GifBubble(media: String?, onWatch: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val bytes = rememberBytes(media)
    val animate = app.oie.ui.common.animationsOn()
    Box(
        Modifier.widthIn(max = OTokens.Size.contentMax / 2).fillMaxWidth().clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.line)
            .clickable(role = Role.Button, onClick = onWatch).semantics(mergeDescendants = true) { contentDescription = tx.notifGif },
    ) {
        // Round 3: while it arrives, the placeholder says so (as a video does).
        if (bytes == null) Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentAlignment = Alignment.Center) {
            Text(tx.arriving, style = OieType.body, color = c.muted)
        }
        else if (animate) VideoSurface(bytes, loop = true, muted = true, play = true, modifier = Modifier.fillMaxWidth())
        else rememberFirstFrame(bytes)?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth) }
            ?: Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        Text(
            tx.gifLabel, style = OieType.caption, color = c.ink,
            modifier = Modifier.align(Alignment.TopStart).padding(OTokens.Spacing.space2)
                .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.surface).padding(horizontal = OTokens.Spacing.space2),
        )
    }
}

/**
 * The full-screen player: the video as big as fits, ▶/❚❚ and the time. A GIF loops silently. Back
 * returns to the chat. Videos never play on their own.
 */
@Composable
fun VideoScreen(media: String?, gif: Boolean, from: String, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val bytes = rememberBytes(media)
    var playing by remember(media) { mutableStateOf(gif) }
    var position by remember { mutableIntStateOf(0) }
    var length by remember { mutableIntStateOf(0) }
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            // Fresh review: one title pattern for media ("Vídeo de Vovó", "Foto de Vovó").
            Text(tx.mediaFrom(if (gif) tx.gifLabel else tx.video, from), style = OieType.title, color = c.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        Column(
            Modifier.fillMaxSize().padding(OTokens.Spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6, Alignment.CenterVertically),
        ) {
            Box(Modifier.weight(1f, fill = false).widthIn(max = OTokens.Size.contentMax * 2).clip(RoundedCornerShape(OTokens.Radius.radiusM)), contentAlignment = Alignment.Center) {
                if (bytes != null) VideoSurface(
                    bytes, loop = gif, muted = gif, play = playing, modifier = Modifier.fillMaxWidth(),
                    onProgress = { p, l -> position = p; length = l }, onEnd = { playing = false; position = 0 },
                ) else Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(c.line))
            }
            if (!gif) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                QuietButton(if (playing) tx.pause else tx.play, { playing = !playing }, Modifier.height(OTokens.Size.button),
                    icon = if (playing) R.drawable.gg_ic_pause else R.drawable.gg_ic_play)
                Text("${clockOf(position / 1000)} / ${clockOf(length / 1000)}", style = OieType.body, color = c.muted)
            }
        }
    }
}

/** A video or GIF the kid picked or recorded, while it's prepared and previewed (v0.28). */
data class VideoDraft(
    val uri: android.net.Uri,
    val gif: Boolean,
    val progress: Float = 0f,
    val ready: app.oie.data.VideoPrep.Result.Ready? = null,
    val problem: app.oie.data.VideoPrep.Why? = null,
)

/**
 * "Vídeo para Vovó": while it's prepared, "Preparando o vídeo…" with a bar; then its first frame and
 * length with Trocar and Enviar (the one coral action). A problem is said in words, with Trocar.
 * Back drops it (docs/flows/kid-sends-message.md, v0.28).
 */
@Composable
fun VideoPreview(draft: VideoDraft, to: String, onSwap: () -> Unit, onSend: () -> Unit, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    // v0.28 audit: the ▶ on the preview plays it, so the kid can watch before sending.
    var watch by remember(draft.ready) { mutableStateOf(false) }
    draft.ready?.let { r -> if (watch) { VideoScreen(r.media, r.gif, to, onBack = { watch = false }); return } }
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(if (draft.gif) tx.gifFor(to) else tx.videoFor(to), style = OieType.title, color = c.ink)
        }
        Column(
            Modifier.fillMaxSize().padding(OTokens.Spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6, Alignment.CenterVertically),
        ) {
            val ready = draft.ready
            val problem = draft.problem
            when {
                problem != null -> {
                    Text(
                        when (problem) {
                            app.oie.data.VideoPrep.Why.TOO_LONG -> tx.videoTooLong
                            app.oie.data.VideoPrep.Why.TOO_BIG -> tx.videoTooBigToSend
                            app.oie.data.VideoPrep.Why.UNREADABLE -> tx.videoError
                        },
                        style = OieType.body, color = c.ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    QuietButton(tx.swap, onSwap, Modifier.height(OTokens.Size.button))
                }
                ready != null -> {
                    if (ready.gif) GifBubble(ready.media) { watch = true } else VideoBubble(ready.media, ready.seconds, tooBig = false, parent = "") { watch = true }
                    // Fresh review: the main button first, as on every other screen.
                    app.oie.ui.setup.ButtonRow {
                        app.oie.ui.common.PrimaryButton(tx.send, onSend, icon = R.drawable.gg_ic_send)
                        QuietButton(tx.swap, onSwap)
                    }
                }
                else -> {
                    Text(tx.preparingVideo, style = OieType.body, color = c.muted)
                    // The bar: ink filling a hairline track, at the column's width.
                    Box(Modifier.widthIn(max = OTokens.Size.contentMax / 2).fillMaxWidth().height(OTokens.Spacing.space1).clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.line)) {
                        Box(Modifier.fillMaxWidth(draft.progress.coerceIn(0f, 1f)).height(OTokens.Spacing.space1).background(c.ink))
                    }
                }
            }
        }
    }
}

/** A stored picture, opened and decoded off the main thread at [maxSide] px (v0.34 audit); null until ready. */
@Composable
internal fun rememberPhoto(media: String?, maxSide: Int): android.graphics.Bitmap? {
    val context = LocalContext.current
    var bmp by remember(media, maxSide) { mutableStateOf<android.graphics.Bitmap?>(null) }
    androidx.compose.runtime.LaunchedEffect(media, maxSide) {
        bmp = media?.let { kotlinx.coroutines.withContext(app.oie.data.Background.dispatcher) { Photos.load(context, it, maxSide) } }
    }
    return bmp
}

/** First frames of videos, by file name (v0.34 audit round 2): decoded once, the video itself not kept in memory. */
private val frames = android.util.LruCache<String, android.graphics.Bitmap>(FRAMES_KEPT)
private const val FRAMES_KEPT = 24

/** A stored video's first frame, read off the main thread and cached; null until ready. */
@Composable
internal fun rememberVideoFrame(media: String?): android.graphics.Bitmap? {
    val context = LocalContext.current
    var frame by remember(media) { mutableStateOf(media?.let { frames.get(it) }) }
    LaunchedEffect(media) {
        val m = media ?: return@LaunchedEffect
        if (frame != null) return@LaunchedEffect
        frame = withContext(app.oie.data.Background.dispatcher) {
            runCatching {
                val bytes = Photos.openBytes(context, m) ?: return@runCatching null
                MediaMetadataRetriever().run { setDataSource(BytesSource(bytes)); val f = getFrameAtTime(0); release(); f }
            }.getOrNull()?.also { frames.put(m, it) }
        }
    }
    return frame
}
