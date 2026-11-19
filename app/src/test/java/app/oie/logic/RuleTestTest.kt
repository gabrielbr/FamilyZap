package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleTestTest {
    private val vovo = Contact("5511999990000@s.whatsapp.net", "Vovó")
    private val t0 = 1_795_020_000_000L
    private val hour = 60L * 60 * 1000
    private fun msg(text: String, at: Long = t0, replyTo: String? = null) =
        Message("m$at", vovo.id, false, at, Kind.TEXT, text, replyTo)

    @Test fun startingWithTheNameReachesTheKidAndKeepsTheText() {
        val e = RuleTest.judge(msg("Tito, já jantou?"), vovo, fromParent = false, kidSpokeAt = null, kidName = "Tito")
        assertTrue(e.shown); assertEquals(RuleTest.Why.NAME, e.why); assertEquals("Tito, já jantou?", e.text)
    }

    @Test fun aMessageForTheParentStaysPrivateAndDropsTheText() {
        val e = RuleTest.judge(msg("Ana, me liga"), vovo, false, null, "Tito")
        assertFalse(e.shown); assertEquals(RuleTest.Why.NOT_FOR_KID, e.why); assertNull(e.text)
    }

    @Test fun theParentsOwnMessagesStayPrivate() {
        val e = RuleTest.judge(msg("Tito chegou"), vovo, fromParent = true, kidSpokeAt = null, kidName = "Tito")
        assertFalse(e.shown); assertEquals(RuleTest.Why.PARENT_OWN, e.why); assertNull(e.text)
    }

    @Test fun pretendingTheKidSpokeOpensTheWindowFor24Hours() {
        val inWindow = RuleTest.judge(msg("que bom", t0 + 2 * hour), vovo, false, kidSpokeAt = t0, kidName = "Tito")
        assertTrue(inWindow.shown); assertEquals(RuleTest.Why.WINDOW, inWindow.why)
        val after = RuleTest.judge(msg("e aí", t0 + 25 * hour), vovo, false, kidSpokeAt = t0, kidName = "Tito")
        assertFalse(after.shown)
    }

    @Test fun reasonsMatchIsForKid() {
        assertEquals(MessageRule.Reason.NAME, MessageRule.reason(msg("tito!"), vovo, emptyList(), "Tito"))
        assertNull(MessageRule.reason(msg("Titozão"), vovo, emptyList(), "Tito"))
    }
}
