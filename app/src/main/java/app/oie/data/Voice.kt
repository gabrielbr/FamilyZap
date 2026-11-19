package app.oie.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/**
 * Real voice messages (v0.11). Recording writes Opus in Ogg, the format of WhatsApp's own voice
 * notes, on Android 10 and newer; older tablets record AAC, which WhatsApp also plays. The file is
 * sealed into private storage like a photo the moment recording stops (Photos.keepBytes) and the
 * plain one deleted. Playing opens the sealed file into a temporary copy that's deleted afterwards.
 */
object Voice {
    private var recorder: MediaRecorder? = null
    private var recording: File? = null

    fun start(context: Context): Boolean {
        stopRecorder()
        val f = File(context.cacheDir, "voice-recording")
        f.delete()
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        return runCatching {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            if (Build.VERSION.SDK_INT >= 29) {
                r.setOutputFormat(MediaRecorder.OutputFormat.OGG); r.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            } else {
                r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
            r.setAudioChannels(1); r.setAudioSamplingRate(48_000); r.setAudioEncodingBitRate(32_000)
            r.setOutputFile(f.path); r.prepare(); r.start()
            recorder = r; recording = f
        }.onFailure { r.release() }.isSuccess
    }

    /** Stops. With [keep], seals the recording and returns its stored name; otherwise throws it away. */
    fun stop(context: Context, keep: Boolean): String? {
        val f = recording
        stopRecorder()
        if (f == null || !f.exists()) return null
        return try { if (keep && f.length() > 0) Photos.keepBytes(context, f.readBytes()) else null } finally { f.delete() }
    }

    private fun stopRecorder() {
        recorder?.let { runCatching { it.stop() }; it.release() }
        recorder = null; recording = null
    }

    /** WhatsApp's voice note type for these bytes ("OggS" starts an Ogg file). */
    fun mimetype(bytes: ByteArray): String =
        if (bytes.size >= 4 && bytes[0] == 'O'.code.toByte() && bytes[1] == 'g'.code.toByte() && bytes[2] == 'g'.code.toByte() && bytes[3] == 'S'.code.toByte())
            "audio/ogg; codecs=opus" else "audio/mp4"

    /** The message id playing now, or null. Compose reads it to show play or pause. */
    var playing: String? by mutableStateOf(null)
        private set
    private var player: MediaPlayer? = null
    private var temp: File? = null

    /** Plays [media] for message [id]; tapping the one playing stops it. */
    fun toggle(context: Context, id: String, media: String) {
        val same = playing == id
        stopPlaying()
        if (same) return
        val bytes = Photos.openBytes(context, media) ?: return
        val f = File(context.cacheDir, "voice-play").apply { writeBytes(bytes) }
        val p = MediaPlayer()
        runCatching {
            p.setDataSource(f.path); p.prepare()
            p.setOnCompletionListener { stopPlaying() }
            p.start()
            player = p; temp = f; playing = id
        }.onFailure { p.release(); f.delete() }
    }

    fun stopPlaying() {
        player?.let { runCatching { it.stop() }; it.release() }
        player = null; temp?.delete(); temp = null; playing = null
    }
}
