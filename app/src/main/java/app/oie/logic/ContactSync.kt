package app.oie.logic

/**
 * Where the parent's contacts are, after linking (docs/flows/parent-setup-link.md, v0.17).
 * WhatsApp sends them a little after the link, so the contacts list says what it's waiting for.
 */
object ContactSync {
    /** After this long with no people and no word from WhatsApp, say it's slow and offer the number. */
    const val SLOW_AFTER_MS = 60_000L

    enum class State { SYNCING, READY, SLOW, EMPTY, OFFLINE }

    /**
     * [synced]: WhatsApp said the contact list is complete. [people]: contacts that aren't groups.
     * [linkedAt]: when the link connected (0 = not yet).
     */
    fun state(connected: Boolean, synced: Boolean, people: Int, linkedAt: Long, now: Long): State = when {
        synced && people > 0 -> State.READY
        synced -> State.EMPTY
        !connected || linkedAt == 0L -> if (people > 0) State.READY else State.OFFLINE
        now - linkedAt < SLOW_AFTER_MS -> State.SYNCING
        people > 0 -> State.READY
        else -> State.SLOW
    }
}
