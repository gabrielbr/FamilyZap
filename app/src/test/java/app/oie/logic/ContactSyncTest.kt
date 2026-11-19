package app.oie.logic

import app.oie.logic.ContactSync.State
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactSyncTest {
    private val t = 1_000_000L
    private fun s(connected: Boolean = true, synced: Boolean = false, people: Int = 0, linkedAt: Long = t, now: Long = t + 5_000) =
        ContactSync.state(connected, synced, people, linkedAt, now)

    @Test fun rightAfterLinkingItSaysItIsBringingThem() = assertEquals(State.SYNCING, s())
    @Test fun someArrivedButNotAllStillSyncing() = assertEquals(State.SYNCING, s(people = 40))
    @Test fun completeWithPeopleIsReady() = assertEquals(State.READY, s(synced = true, people = 120))
    @Test fun completeWithNobodyIsEmpty() = assertEquals(State.EMPTY, s(synced = true))
    @Test fun aMinuteWithNobodyIsSlow() = assertEquals(State.SLOW, s(now = t + ContactSync.SLOW_AFTER_MS))
    @Test fun aMinuteWithSomePeopleIsReady() = assertEquals(State.READY, s(people = 3, now = t + ContactSync.SLOW_AFTER_MS))
    @Test fun offlineWithNothingSaysNothing() = assertEquals(State.OFFLINE, s(connected = false, linkedAt = 0))
    @Test fun offlineWithContactsFromBeforeIsReady() = assertEquals(State.READY, s(connected = false, people = 10, linkedAt = 0))
}
