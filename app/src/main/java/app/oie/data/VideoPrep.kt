package app.oie.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaDataSource
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import app.oie.logic.VideoRules
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Bytes in memory as a source for MediaPlayer and MediaMetadataRetriever (sealed files never touch disk in plain form). */
class BytesSource(private val b: ByteArray) : MediaDataSource() {
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= b.size) return -1
        val n = minOf(size.toLong(), b.size - position).toInt()
        System.arraycopy(b, position.toInt(), buffer, offset, n)
        return n
    }
    override fun getSize(): Long = b.size.toLong()
    override fun close() {}
}

/**
 * The kid's videos and GIFs before sending (docs/flows/kid-sends-message.md, v0.28):
 * a video is shrunk to 720p with Media3 (decision 0006); a GIF becomes WhatsApp's GIF form (a short
 * silent MP4). The result is sealed on the tablet like a photo.
 *
 * Media3 marks Transformer as "unstable API" (it may change between versions); the version is pinned
 * in libs.versions.toml, so the opt-in is deliberate.
 */
@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
object VideoPrep {
    sealed interface Result {
        data class Ready(val media: String, val seconds: Int, val gif: Boolean) : Result
        data class Problem(val why: Why) : Result
    }
    enum class Why { TOO_LONG, TOO_BIG, UNREADABLE }

    private fun work(context: Context): File = File(context.cacheDir, "video").apply { mkdirs() }

    /** A gallery pick or a camera recording. [progress] gets 0..1 while it's being shrunk. */
    suspend fun prepare(context: Context, uri: Uri, progress: (Float) -> Unit): Result {
        val type = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
        return try {
            if (type == "image/gif") gif(context, uri) else video(context, uri, progress)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            Diagnostics.log("video: couldn't prepare (${e.javaClass.simpleName})")
            Result.Problem(Why.UNREADABLE)
        } finally {
            work(context).listFiles()?.forEach { it.delete() }
        }
    }

    private suspend fun video(context: Context, uri: Uri, progress: (Float) -> Unit): Result {
        val meta = withContext(Dispatchers.IO) {
            MediaMetadataRetriever().run {
                try {
                    setDataSource(context, uri)
                    Triple(
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                        (extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0) to
                            (extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0,
                    )
                } finally { release() }
            }
        }
        val (durationMs, size, rotation) = meta
        if (durationMs <= 0 || size.first <= 0) return Result.Problem(Why.UNREADABLE)
        if (VideoRules.length(durationMs) == VideoRules.Check.TOO_LONG) return Result.Problem(Why.TOO_LONG)
        val out = File(work(context), UUID.randomUUID().toString() + ".mp4")
        val target = VideoRules.target(size.first, size.second, rotation)
        val item = EditedMediaItem.Builder(MediaItem.fromUri(uri)).apply {
            if (target != null) setEffects(Effects(listOf(), listOf(Presentation.createForWidthAndHeight(target.first, target.second, Presentation.LAYOUT_SCALE_TO_FIT))))
        }.build()
        val ok = withContext(Dispatchers.Main) { export(context, item, out, progress) }
        if (!ok || !out.exists()) return Result.Problem(Why.UNREADABLE)
        if (VideoRules.size(out.length()) == VideoRules.Check.TOO_BIG) return Result.Problem(Why.TOO_BIG)
        val name = withContext(Dispatchers.IO) { Photos.keepBytes(context, out.readBytes()) }
        return Result.Ready(name, ((durationMs + 500) / 1000).toInt(), gif = false)
    }

    /** Media3's export, on the main thread as it requires; cancelled with the coroutine. */
    private suspend fun export(context: Context, item: EditedMediaItem, out: File, progress: (Float) -> Unit): Boolean = coroutineScope {
        var transformer: Transformer? = null
        val ticker = launch {
            val holder = ProgressHolder()
            while (true) {
                delay(300)
                transformer?.let { if (it.getProgress(holder) == Transformer.PROGRESS_STATE_AVAILABLE) progress(holder.progress / 100f) }
            }
        }
        val ok = suspendCancellableCoroutine { cont ->
            val t = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264).setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) { if (cont.isActive) cont.resume(true) }
                    override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                        Diagnostics.log("video: shrinking failed (${exportException.errorCodeName})")
                        if (cont.isActive) cont.resume(false)
                    }
                }).build()
            transformer = t
            cont.invokeOnCancellation { runCatching { t.cancel() } }
            t.start(item, out.path)
        }
        ticker.cancel()
        progress(1f)
        ok
    }

    private suspend fun gif(context: Context, uri: Uri): Result = withContext(Dispatchers.Default) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext Result.Problem(Why.UNREADABLE)
        if (!app.oie.logic.Moving.isGif(bytes)) return@withContext Result.Problem(Why.UNREADABLE)
        val out = File(work(context), UUID.randomUUID().toString() + ".mp4")
        val seconds = GifToMp4.convert(bytes, out) ?: return@withContext Result.Problem(Why.UNREADABLE)
        if (VideoRules.size(out.length()) == VideoRules.Check.TOO_BIG) return@withContext Result.Problem(Why.TOO_BIG)
        Result.Ready(Photos.keepBytes(context, out.readBytes()), seconds, gif = true)
    }

    /** What WhatsApp wants with a video: its size as shown and a small preview picture. */
    data class Meta(val width: Int, val height: Int, val thumb: ByteArray)

    fun meta(mp4: ByteArray): Meta = runCatching {
        MediaMetadataRetriever().run {
            try {
                setDataSource(BytesSource(mp4))
                val w = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                val h = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                val r = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                val frame = getFrameAtTime(0)
                val thumb = frame?.let { f ->
                    val s = THUMB_SIDE.toFloat() / maxOf(f.width, f.height)
                    val small = Bitmap.createScaledBitmap(f, maxOf(1, (f.width * s).toInt()), maxOf(1, (f.height * s).toInt()), true)
                    java.io.ByteArrayOutputStream().use { small.compress(Bitmap.CompressFormat.JPEG, 60, it); it.toByteArray() }
                } ?: ByteArray(0)
                if (r % 180 != 0) Meta(h, w, thumb) else Meta(w, h, thumb)
            } finally { release() }
        }
    }.getOrDefault(Meta(0, 0, ByteArray(0)))

    private const val THUMB_SIDE = 96
}

/**
 * A GIF file to WhatsApp's GIF form: a silent H.264 MP4 of one loop (v0.28). Media3 doesn't read
 * animated GIFs, so frames are drawn with Android's Movie and encoded with MediaCodec. Returns the
 * length in seconds, or null if the GIF can't be read.
 */
@Suppress("DEPRECATION") // android.graphics.Movie: the one platform API that seeks inside a GIF.
object GifToMp4 {
    fun convert(gif: ByteArray, out: File): Int? {
        val movie = android.graphics.Movie.decodeByteArray(gif, 0, gif.size) ?: return null
        if (movie.width() <= 0 || movie.height() <= 0) return null
        val durationMs = movie.duration().let { if (it <= 0) 1000 else it }.coerceAtMost(VideoRules.GIF_MAX_MS)
        val (w, h) = VideoRules.gifSize(movie.width(), movie.height())
        val fps = VideoRules.GIF_FPS
        val frameUs = 1_000_000L / fps
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, 1_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = codec.createInputSurface()
        codec.start()
        val muxer = MediaMuxer(out.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var track = -1
        var written = 0L
        val info = MediaCodec.BufferInfo()
        fun drain(end: Boolean) {
            if (end) codec.signalEndOfInputStream()
            while (true) {
                val i = codec.dequeueOutputBuffer(info, 10_000)
                when {
                    i == MediaCodec.INFO_TRY_AGAIN_LATER -> if (!end) return
                    i == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> { track = muxer.addTrack(codec.outputFormat); muxer.start() }
                    i >= 0 -> {
                        val buf = codec.getOutputBuffer(i)
                        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                        if (buf != null && info.size > 0 && track >= 0) {
                            // Timestamps by frame count: the frames are drawn faster than real time.
                            info.presentationTimeUs = written * frameUs
                            buf.position(info.offset); buf.limit(info.offset + info.size)
                            muxer.writeSampleData(track, buf, info)
                            written++
                        }
                        codec.releaseOutputBuffer(i, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                }
            }
        }
        try {
            // Movie draws in software: into a bitmap first, then the bitmap onto the encoder's surface.
            val frame = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888)
            val frameCanvas = Canvas(frame)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            val dst = android.graphics.Rect(0, 0, w, h)
            for (f in 0 until VideoRules.gifFrames(durationMs)) {
                movie.setTime(((f * 1000L / fps) % durationMs).toInt())
                frameCanvas.drawColor(Color.WHITE)
                movie.draw(frameCanvas, 0f, 0f)
                val c = surface.lockHardwareCanvas()
                c.drawColor(Color.WHITE)
                c.drawBitmap(frame, null, dst, paint)
                surface.unlockCanvasAndPost(c)
                drain(false)
                Thread.sleep(FRAME_PAUSE_MS) // lets the encoder keep up, so no frame is dropped
            }
            drain(true)
        } finally {
            runCatching { codec.stop() }; runCatching { codec.release() }; runCatching { surface.release() }
            runCatching { if (track >= 0) muxer.stop() }; runCatching { muxer.release() }
        }
        if (written == 0L) return null
        return ((durationMs + 999) / 1000).coerceAtLeast(1)
    }

    private const val FRAME_PAUSE_MS = 15L
}
