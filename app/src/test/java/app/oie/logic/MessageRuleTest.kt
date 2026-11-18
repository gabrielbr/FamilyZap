package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageRuleTest {
    private val vovo = Contact("vovo", "Vovó")
    private val h = 60L * 60 * 1000
    private fun kid(id: String, at: Long) = Message(id, "vovo", fromKid = true, at = at, kind = Kind.TEXT, text = "Oi vó")
    private fun fam(id: String, at: Long, text: String = "ok", replyTo: String? = null) =
        Message(id, "vovo", fromKid = false, at = at, kind = Kind.TEXT, text = text, replyTo = replyTo)

    @Test fun parentsPrivateChatStaysPrivate() {
        // Grandma writes to the parent; the kid never wrote in this chat.
        assertFalse(MessageRule.isForKid(fam("a", 10 * h, "Filha, me liga"), vovo, emptyList(), "Tito"))
    }

    @Test fun replyToKidReachesKidAnyTime() {
        val k = kid("k1", 0)
        assertTrue(MessageRule.isForKid(fam("a", 100 * h, replyTo = "k1"), vovo, listOf(k), "Tito"))
    }

    @Test fun startingWithKidsNameReachesKid() {
        assertTrue(MessageRule.isForKid(fam("a", 0, "Tito, domingo tem bolo"), vovo, emptyList(), "Tito"))
        assertTrue(MessageRule.isForKid(fam("a", 0, "títo! saudade"), vovo, emptyList(), "Tito"))
        assertFalse(MessageRule.isForKid(fam("a", 0, "Titozão é o cachorro"), vovo, emptyList(), "Tito"))
    }

    @Test fun within24hAfterKidsLastMessage() {
        val history = listOf(kid("k1", 0))
        assertTrue(MessageRule.isForKid(fam("a", 23 * h), vovo, history, "Tito"))
        assertFalse(MessageRule.isForKid(fam("b", 25 * h), vovo, history, "Tito"))
    }

    @Test fun windowCountsOnlyThisChat() {
        val otherChat = Message("k9", "mae", fromKid = true, at = 0, kind = Kind.TEXT)
        assertFalse(MessageRule.isForKid(fam("a", h), vovo, listOf(otherChat), "Tito"))
    }

    @Test fun wholeGroupWhenParentSaysSo() {
        val group = Contact("g", "Primos", isGroup = true, showWholeGroup = true)
        assertTrue(MessageRule.isForKid(fam("a", 0).copy(chatId = "g"), group, emptyList(), "Tito"))
    }
}
