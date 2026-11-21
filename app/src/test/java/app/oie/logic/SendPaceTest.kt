package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SendPaceTest {
    private val now = 1_000_000_000L

    @Test fun firstSendGoesNow() = assertEquals(0L, SendPace.waitMs(emptyList(), now))

    @Test fun fourSecondsBetweenSends() {
        assertEquals(3_500L, SendPace.waitMs(listOf(now - 500), now))
        assertEquals(0L, SendPace.waitMs(listOf(now - 4_000), now))
    }

    @Test fun twentyInTenMinutesThenWait() {
        val sent = (0 until 20).map { now - 60_000L - it * 5_000L } // 20 sends in the last ~2.5 min
        val oldest = sent.min()
        assertEquals(oldest + SendPace.WINDOW_MS - now, SendPace.waitMs(sent, now))
        assertEquals(0L, SendPace.waitMs(sent.drop(1), now))
    }
}
