package app.oie.logic

import app.oie.logic.LinkStart.Do
import org.junit.Assert.assertEquals
import org.junit.Test

class LinkStartTest {
    @Test fun anUpConnectionIsLeftAloneAndShownAsConnected() {
        assertEquals(Do.NOTHING, LinkStart.decide(banned = false, connectedShown = true, socketUp = true))
        // The owner's case: the screen said "disconnected" while the connection was up.
        assertEquals(Do.MARK_CONNECTED, LinkStart.decide(banned = false, connectedShown = false, socketUp = true))
    }

    @Test fun aDownConnectionConnects() {
        assertEquals(Do.CONNECT, LinkStart.decide(banned = false, connectedShown = false, socketUp = false))
        // Shown as connected but actually down (Android cut it while the tablet slept): connect again.
        assertEquals(Do.CONNECT, LinkStart.decide(banned = false, connectedShown = true, socketUp = false))
    }

    @Test fun aBanIsNeverRetried() {
        assertEquals(Do.NOTHING, LinkStart.decide(banned = true, connectedShown = false, socketUp = false))
        assertEquals(Do.NOTHING, LinkStart.decide(banned = true, connectedShown = false, socketUp = true))
    }
}
