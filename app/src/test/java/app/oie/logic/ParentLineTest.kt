package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** v0.30, decision 0007: the line between the kid and the parent who owns the WhatsApp. */
class ParentLineTest {
    private val group = Contact("1203630@g.us", "Ana", isGroup = true, parentLine = true)
    private val ownChat = Contact("5511900000000@s.whatsapp.net", "Ana", parentLine = true)
    private fun parent(text: String, replyTo: String? = null) = Message("p", group.id, false, 10, Kind.TEXT, text, replyTo)

    @Test fun inTheGroupEverythingTheParentWritesReachesTheKid() {
        assertTrue(MessageRule.isForKid(parent("vem jantar"), group, emptyList(), "Tito"))
    }

    @Test fun inTheOwnChatTheParentsNotesStayPrivate() {
        val kid = Message("k1", ownChat.id, true, 5, Kind.TEXT, "oi pai")
        assertFalse(MessageRule.isForKid(parent("comprar pão").copy(chatId = ownChat.id), ownChat, listOf(kid), "Tito"))
        assertTrue(MessageRule.isForKid(parent("oi filho", replyTo = "k1").copy(chatId = ownChat.id), ownChat, listOf(kid), "Tito"))
        assertTrue(MessageRule.isForKid(parent("Tito, vem jantar").copy(chatId = ownChat.id), ownChat, listOf(kid), "Tito"))
    }

    @Test fun theParentWritingFromTheirPhoneIsTheParentTalking() {
        val m = RealInbox.toMessage("p1", group.id, null, fromMe = true, ours = emptySet(), allowed = listOf(group), at = 1, kind = Kind.TEXT, text = "oi", replyTo = null, durationSec = 0)
        assertNotNull(m); assertFalse(m!!.fromKid)
        // The tablet's own send coming back is not repeated.
        assertNull(RealInbox.toMessage("k1", group.id, null, fromMe = true, ours = setOf("k1"), allowed = listOf(group), at = 1, kind = Kind.TEXT, text = "Tito: oi", replyTo = null, durationSec = 0))
        // Anywhere else, the parent's own writing stays private.
        val vovo = Contact("5511987650001@s.whatsapp.net", "Vovó")
        assertNull(RealInbox.toMessage("p2", vovo.id, null, fromMe = true, ours = emptySet(), allowed = listOf(vovo), at = 1, kind = Kind.TEXT, text = "oi mãe", replyTo = null, durationSec = 0))
        assertEquals(group.id, m.chatId)
    }
}
