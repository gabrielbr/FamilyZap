package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatBubblesTest {
    private val vovo = "5511987650001@s.whatsapp.net"

    @Test fun aChatsBubbleIsFoundAgain() {
        assertEquals(vovo, ChatBubbles.contactOf(ChatBubbles.shortcutId(vovo)))
        assertNull(ChatBubbles.contactOf("oie-bubble"))
        assertNull(ChatBubbles.contactOf(null))
    }

    @Test fun pausedOrRemovedChatsLoseTheirBubble() {
        val bubbled = listOf(ChatBubbles.shortcutId(vovo), ChatBubbles.shortcutId("mae"), "oie-bubble", null)
        assertEquals(setOf("mae"), ChatBubbles.stale(bubbled, reachable = setOf(vovo)))
        assertEquals(emptySet<String>(), ChatBubbles.stale(bubbled, reachable = setOf(vovo, "mae")))
    }
}
