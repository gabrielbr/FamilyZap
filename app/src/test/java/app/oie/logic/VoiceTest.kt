package app.oie.logic

import app.oie.data.Voice
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceTest {
    @Test fun oggIsAWhatsAppVoiceNote() = assertEquals("audio/ogg; codecs=opus", Voice.mimetype("OggS....".toByteArray()))
    @Test fun anythingElseIsAac() = assertEquals("audio/mp4", Voice.mimetype(byteArrayOf(0, 0, 0, 24, 'f'.code.toByte())))
}
