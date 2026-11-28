package app.oie.logic

import app.oie.data.AppState
import app.oie.data.Codec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * v0.59, issue #90: the same WhatsApp message reached the tablet twice (WhatsApp sends again after a
 * reconnect), was kept twice, and the chat crashed on open: "Key … was already used" in the chat's list.
 */
@RunWith(RobolectricTestRunner::class)
class DuplicateMessageTest {
    private fun ready() = AppState().apply {
        parentName = "Ana"; kidName = "Tito"
        link.pretendLinked()
        allowed.addAll(link.contacts().take(5))
    }
    private val vovo = "vovo"

    @Test fun aMessageThatArrivesTwiceIsKeptOnce() {
        val s = ready()
        val m = Message("AC8B09F24F28E339D43C8B00802E6228", vovo, false, System.currentTimeMillis(), Kind.TEXT, "Tito, tem bolo")
        val first = s.receive(m)
        val again = s.receive(m)
        assertEquals(Inbox.Decision.NOTIFY, first)
        assertEquals(Inbox.Decision.DROP, again)
        assertEquals(1, s.messages.count { it.id == m.id })
    }

    @Test fun duplicatesAlreadySavedOnTheTabletAreCleanedWhenOieLoads() {
        val a = ready()
        val m = Message("X1", vovo, false, 1000, Kind.TEXT, "Tito, oi")
        a.messages += m; a.messages += m.copy(); a.messages += Message("X2", vovo, true, 2000, Kind.TEXT, "oi vó")
        val b = AppState().also { Codec.decode(Codec.encode(a), it) }
        assertEquals(listOf("X1", "X2"), b.messages.map { it.id })
    }

    @Test fun theSameIdInTwoChatsIsTwoMessages() {
        val a = ready()
        a.messages += Message("X1", vovo, false, 1000, Kind.TEXT, "Tito, oi")
        a.messages += Message("X1", "primos", false, 1000, Kind.TEXT, "Tito, oi")
        val b = AppState().also { Codec.decode(Codec.encode(a), it) }
        assertEquals(2, b.messages.size)
    }

    @Test fun theChatNeverListsAnIdTwice() {
        val s = ready()
        val m = Message("X1", vovo, false, System.currentTimeMillis(), Kind.TEXT, "Tito, oi")
        s.messages += m; s.messages += m.copy(text = "Tito, oi (again)")
        assertEquals(1, s.visible(vovo).count { it.id == "X1" })
    }
}
