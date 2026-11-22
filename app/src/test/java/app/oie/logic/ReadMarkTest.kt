package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** v0.55 (the owner: "messages sent to the kid can be marked as read on the parent's device"). */
class ReadMarkTest {
    private val vovo = Contact("vovo", "Vovó")
    private val line = Contact("line", "Ana", parentLine = true)
    private fun msg(fromKid: Boolean = false, fromParent: Boolean = false, kind: Kind = Kind.TEXT) =
        Message("m1", "vovo", fromKid, 0L, kind, "Tito, oi", fromParent = fromParent)

    @Test fun whatReachesTheKidIsMarkedRead() {
        assertTrue(ReadMark.should(true, Inbox.Decision.NOTIFY, vovo, msg()))
        // In quiet hours it still reached the kid (quietly), so it's for them too.
        assertTrue(ReadMark.should(true, Inbox.Decision.QUIET, vovo, msg()))
    }

    @Test fun whatStaysWithTheParentStaysUnread() {
        assertFalse(ReadMark.should(true, Inbox.Decision.DROP, vovo, msg()))
        assertFalse(ReadMark.should(true, Inbox.Decision.DELETE, vovo, msg()))
    }

    @Test fun neverWhenTheParentTurnedItOff() {
        assertFalse(ReadMark.should(false, Inbox.Decision.NOTIFY, vovo, msg()))
    }

    @Test fun neverTheParentsLineTheirOwnMessagesOrAnUnknownChat() {
        assertFalse(ReadMark.should(true, Inbox.Decision.NOTIFY, line, msg()))
        assertFalse(ReadMark.should(true, Inbox.Decision.NOTIFY, vovo, msg(fromParent = true)))
        assertFalse(ReadMark.should(true, Inbox.Decision.NOTIFY, vovo, msg(fromKid = true)))
        assertFalse(ReadMark.should(true, Inbox.Decision.NOTIFY, null, msg()))
        assertFalse(ReadMark.should(true, Inbox.Decision.NOTIFY, vovo, msg(kind = Kind.DELETED)))
    }
}
