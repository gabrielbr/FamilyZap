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

    // v0.40: the owner, 2026-11-21: "o Tito já chegou?" reached the tablet inside the 24-hour window.
    @Test fun talkAboutTheKidStaysPrivateEvenInTheWindow() {
        val history = listOf(kid("k1", 0))
        for (t in listOf("o Tito já chegou?", "Como está o Tito?", "manda beijo pro Tito", "falei com o Tito hoje", "saudade do Tito", "is Tito home?"))
            assertFalse(t, MessageRule.isForKid(fam("a", h, t), vovo, history, "Tito"))
    }

    @Test fun speakingToTheKidStillReachesHim() {
        val history = listOf(kid("k1", 0))
        for (t in listOf("Tito, já chegou?", "Oi Tito", "Te amo, Tito", "Beijo Tito", "Boa noite, Ana e Tito", "também te amo", "Domingo tem bolo"))
            assertTrue(t, MessageRule.isForKid(fam("a", h, t), vovo, history, "Tito"))
    }

    @Test fun aReplyToTheKidWinsEvenIfItTalksAboutHim() =
        assertTrue(MessageRule.isForKid(fam("a", h, "o Tito é demais", replyTo = "k1"), vovo, listOf(kid("k1", 0)), "Tito"))

    @Test fun aboutKidMatchesNicknamesAndAccents() {
        assertTrue(MessageRule.aboutKid("cadê a Lili?", listOf("Lia", "Lili")))
        assertTrue(MessageRule.aboutKid("e o Títo?", listOf("Tito")))
        assertTrue(MessageRule.aboutKid("a Ana Clara dormiu?", listOf("Ana Clara")))
        assertFalse(MessageRule.aboutKid("o Titozão latiu", listOf("Tito")))
        assertFalse(MessageRule.aboutKid("Tito", listOf("Tito")))
        assertFalse(MessageRule.aboutKid("o Tito", listOf("")))
    }

    @Test fun afterTheParentWritesTheWindowCloses() {
        val history = listOf(kid("k1", 0))
        assertFalse(MessageRule.isForKid(fam("a", 2 * h, "tudo bem sim"), vovo, history, listOf("Tito"), parentWroteAt = h))
        // A reply to the kid, or his name, still reaches him.
        assertTrue(MessageRule.isForKid(fam("b", 2 * h, "Tito, te amo"), vovo, history, listOf("Tito"), parentWroteAt = h))
        assertTrue(MessageRule.isForKid(fam("c", 2 * h, "ok", replyTo = "k1"), vovo, history, listOf("Tito"), parentWroteAt = h))
    }

    @Test fun whenTheKidWritesAgainTheWindowReopens() {
        val history = listOf(kid("k1", 0), kid("k2", 3 * h))
        assertTrue(MessageRule.isForKid(fam("a", 4 * h, "tudo bem sim"), vovo, history, listOf("Tito"), parentWroteAt = h))
    }

    @Test fun aParentMessageAfterThisOneDoesNotCloseItRetroactively() =
        assertTrue(MessageRule.isForKid(fam("a", h, "oi meu bem"), vovo, listOf(kid("k1", 0)), listOf("Tito"), parentWroteAt = 2 * h))

    // v0.42: a conversation the parent marked as the kid's own: all of it, no name, no window.
    @Test fun aKidsOwnConversationAlwaysReachesTheKid() {
        val friend = Contact("lu", "Lucas", showWholeGroup = true)
        val m = Message("a", "lu", fromKid = false, at = 100 * h, kind = Kind.TEXT, text = "o Tito vem hoje?")
        assertTrue(MessageRule.isForKid(m, friend, emptyList(), listOf("Tito"), parentWroteAt = 99 * h))
    }

    @Test fun theLineToTheParentIsNeverWhole() {
        val ownChat = Contact("me", "Ana", parentLine = true, showWholeGroup = true)
        val note = Message("a", "me", fromKid = false, at = h, kind = Kind.TEXT, text = "comprar pão")
        assertFalse(MessageRule.isForKid(note, ownChat, emptyList(), "Tito"))
    }

    /** The examples on the website's page for family and friends (site/falar-com-a-crianca, site/talk-to-a-child). */
    @Test fun theWebsitesExamplesHold() {
        val starts = listOf(
            "Tito, sábado é o aniversário do Lucas. Quer vir?", "Oi Tito, boa noite. Dorme bem.", "Bom dia, Tito. Como foi a prova?",
            "Tito, it's Lucas's birthday on Saturday. Want to come?", "Hi Tito, good night. Sleep well.", "Good morning, Tito. How was the test?",
        )
        for (t in starts) assertTrue(t, MessageRule.isForKid(fam("a", 0, t), vovo, emptyList(), "Tito"))
        // Talk about the kid stays with the parent, even right after the kid wrote (inside the 24-hour window).
        val about = listOf(
            "O Tito pode vir sábado?", "Manda um beijo pro Tito.", "Como está o Tito?",
            "Is Tito free on Saturday?", "Tell Tito I said hi.", "How is Tito doing?",
        )
        for (t in about) {
            assertFalse(t, MessageRule.isForKid(fam("a", 0, t), vovo, emptyList(), "Tito"))
            assertFalse(t, MessageRule.isForKid(fam("b", 2 * h, t), vovo, listOf(kid("k", h)), "Tito"))
        }
    }
}
