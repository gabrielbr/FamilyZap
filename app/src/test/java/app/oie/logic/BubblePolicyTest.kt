package app.oie.logic

import app.oie.logic.BubblePolicy.State
import org.junit.Assert.assertEquals
import org.junit.Test

class BubblePolicyTest {
    @Test fun olderTabletsHaveNoBubbles() = assertEquals(State.UNSUPPORTED, BubblePolicy.state(29, true, true, true))
    @Test fun offUntilTheParentTurnsItOn() = assertEquals(State.OFF, BubblePolicy.state(36, false, true, true))
    @Test fun notificationsComeFirst() = assertEquals(State.NEEDS_NOTIFICATIONS, BubblePolicy.state(36, true, false, false))
    @Test fun thenAndroidsBubblePermission() = assertEquals(State.NEEDS_PERMISSION, BubblePolicy.state(36, true, true, false))
    @Test fun onWhenEverythingAllowsIt() = assertEquals(State.ON, BubblePolicy.state(30, true, true, true))
}
