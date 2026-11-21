package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** v0.46: the parent's own writing in an allowed group is part of the group's talk; anywhere else it stays private. */
class RealInboxGroupTest {
    private val primos = Contact("g@g.us", "Primos", isGroup = true)
    private val vovo = Contact("vo@s.whatsapp.net", "Vovó")
    private val allowed = listOf(primos, vovo)
    private fun gate(id: String, chat: String, text: String, kidIds: Set<String> = emptySet(), ours: Set<String> = emptySet()) =
        RealInbox.toMessage(id, chat, null, fromMe = true, ours = ours, allowed = allowed, at = 0, kind = Kind.TEXT, text = text,
            replyTo = null, durationSec = 0, kidIds = kidIds, kidTag = "😎*Tito*")

    @Test fun theParentInAGroupIsShownAsTheParent() {
        val m = gate("p1", primos.id, "Domingo tem bolo")
        assertTrue(m!!.fromParent)
        assertEquals(primos.id, m.chatId)
    }

    @Test fun theParentInAOneToOneChatStaysPrivate() = assertNull(gate("p2", vovo.id, "Mãe, te ligo já"))

    @Test fun theKidsOwnSendsComingBackAreNeverShownTwice() {
        assertNull(gate("k1", primos.id, "oi", ours = setOf("k1")))
        assertNull(gate("k2", primos.id, "oi", kidIds = setOf("k2")))
        assertNull(gate("k3", primos.id, "😎*Tito*: oi"))
        assertNull(gate("i1", primos.id, "Oiê · Tito vai participar deste grupo")) // v0.47: the intro's echo
    }
}
