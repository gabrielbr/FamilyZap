package app.oie.logic

/**
 * What to do when Oiê asks the link to connect: every time it comes back to the screen, and when the
 * network returns (v0.20). Before, it always connected again; an already-up connection answered
 * "already connected", which read as a failure, so the tablet said "disconnected" while WhatsApp
 * showed it connected (the owner's report, 2026-11-19).
 */
object LinkStart {
    enum class Do { NOTHING, MARK_CONNECTED, CONNECT }

    /** [banned]: never retry around a ban (L9). [socketUp]: the link says it's connected and logged in. */
    fun decide(banned: Boolean, connectedShown: Boolean, socketUp: Boolean): Do = when {
        banned -> Do.NOTHING
        socketUp -> if (connectedShown) Do.NOTHING else Do.MARK_CONNECTED
        else -> Do.CONNECT
    }
}
