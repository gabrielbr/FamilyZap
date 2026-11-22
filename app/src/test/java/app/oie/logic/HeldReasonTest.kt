package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** v0.56: why a message from an allowed chat stayed with the parent, for the problem report. */
class HeldReasonTest {
    private val cousin = Contact("5511900000009@s.whatsapp.net", "Primo João")
    private val names = listOf("Tito")
    private val hour = 60L * 60 * 1000
    private fun kid(at: Long) = Message("k$at", cousin.id, true, at, Kind.TEXT, "Oi primo")
    private fun from(at: Long, text: String = "Fiz um gol hoje") = Message("c$at", cousin.id, false, at, Kind.TEXT, text)

    @Test fun anAnswerTheSameDayReachesTheKid() =
        assertNull(MessageRule.held(from(9 * hour), cousin, listOf(kid(1 * hour)), names))

    @Test fun eachReasonIsTold() {
        assertEquals(MessageRule.Held.NO_KID_MESSAGE, MessageRule.held(from(9 * hour), cousin, emptyList(), names))
        assertEquals(MessageRule.Held.AFTER_24_HOURS, MessageRule.held(from(30 * hour), cousin, listOf(kid(1 * hour)), names))
        assertEquals(MessageRule.Held.ABOUT_KID, MessageRule.held(from(9 * hour, "o Tito já chegou?"), cousin, listOf(kid(1 * hour)), names))
        assertEquals(MessageRule.Held.PARENT_WROTE, MessageRule.held(from(9 * hour), cousin, listOf(kid(1 * hour)), names, parentWroteAt = 2 * hour))
    }

    // The owner's case: the morning message, the intro a few seconds after it (not the parent), the answer at 17:00.
    @Test fun theIntroAfterAReLinkNoLongerClosesTheWindow() {
        val intro = Intro.MARK + " Tito está escrevendo pelo WhatsApp de Ana, com o app Oiê, no tablet."
        assertNull(RealInbox.parentWriting("3EB0X", cousin.id, null, true, emptySet(), listOf(cousin), setOf("k1"), intro, "😎*Tito*"))
        assertNull(MessageRule.held(from(17 * hour), cousin, listOf(kid(8 * hour)), names, parentWroteAt = null))
    }
}
