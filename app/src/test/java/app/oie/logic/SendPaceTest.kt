package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SendPaceTest {
    private val now = 1_000_000_000L

    @Test fun firstSendGoesNow() = assertEquals(0L, SendPace.waitMs(emptyList(), now))

    @Test fun twoSecondsBetweenSends() {
        assertEquals(1_500L, SendPace.waitMs(listOf(now - 500), now))
        assertEquals(0L, SendPace.waitMs(listOf(now - 2_000), now))
    }

    @Test fun thirtyInTenMinutesThenWait() {
        val sent = (0 until 30).map { now - 60_000L - it * 3_000L } // 30 sends in the last ~2.5 min
        val oldest = sent.min()
        assertEquals(oldest + SendPace.WINDOW_MS - now, SendPace.waitMs(sent, now))
        assertEquals(0L, SendPace.waitMs(sent.drop(1), now))
    }
}
