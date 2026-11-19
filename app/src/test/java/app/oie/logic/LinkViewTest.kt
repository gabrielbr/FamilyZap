package app.oie.logic

import app.oie.logic.LinkView.Shown
import org.junit.Assert.assertEquals
import org.junit.Test

class LinkViewTest {
    @Test fun upIsConnected() = assertEquals(Shown.CONNECTED, LinkView.of(true, true, false, 0, 0))

    @Test fun rightAfterAnUpdateItIsConnectingNotDown() {
        // The owner's case: Oiê just opened after updating and the link is on its way.
        assertEquals(Shown.CONNECTING, LinkView.of(false, true, true, since = 1_000, now = 3_000))
    }

    @Test fun stillNotUpAfterTheGraceIsDown() {
        assertEquals(Shown.DOWN, LinkView.of(false, true, true, since = 0, now = LinkView.GRACE_MS))
    }

    @Test fun notLinkedOrNotTryingIsDown() {
        assertEquals(Shown.DOWN, LinkView.of(false, wanted = false, trying = true, since = 0, now = 1))
        // Logged out, banned or a QR: they have their own screens.
        assertEquals(Shown.DOWN, LinkView.of(false, wanted = true, trying = false, since = 0, now = 1))
    }
}
