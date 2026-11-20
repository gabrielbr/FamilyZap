package app.oie.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * "Ouvir" on a message (v0.33): Android's own voice reads it on the tablet. Nothing is sent anywhere
 * by Oiê; the voice is whatever the tablet has installed, in the app's language.
 */
object ReadAloud {
    /** The message id being read now, or null. Compose reads it to show Ouvir or Parar. */
    var reading: String? by mutableStateOf(null)
        private set
    private var tts: TextToSpeech? = null
    private var ready = false
    private var waiting: Pair<String, String>? = null

    fun toggle(context: Context, id: String, text: String) {
        if (reading == id) { stop(); return }
        stop()
        reading = id
        val t = tts
        if (t != null && ready) { say(t, id, text); return }
        waiting = id to text
        if (t == null) tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            val engine = tts ?: return@TextToSpeech
            if (!ready) { Diagnostics.log("read aloud: no voice on this tablet"); reading = null; return@TextToSpeech }
            val pt = context.resources.configuration.locales[0].language != "en"
            engine.language = if (pt) Locale("pt", "BR") else Locale.ENGLISH
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { if (reading == utteranceId) reading = null }
                @Deprecated("Deprecated in Java") override fun onError(utteranceId: String?) { if (reading == utteranceId) reading = null }
            })
            waiting?.let { (i, s) -> if (reading == i) say(engine, i, s) }
            waiting = null
        }
    }

    private fun say(t: TextToSpeech, id: String, text: String) {
        if (t.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) reading = null
    }

    fun stop() {
        runCatching { tts?.stop() }
        reading = null; waiting = null
    }
}
