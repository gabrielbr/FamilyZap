package app.oie.logic

/**
 * Human pace for everything the tablet sends (docs/whatsapp-link.md, rule L3). The real link asks
 * before each send and waits the time this returns. A kid never gets near these limits; they stop a
 * bug (a loop, a retry storm after reconnecting) from looking like a bot to WhatsApp.
 */
object SendPace {
    // v0.41 (the owner's low-risk pass): 4 s apart and 20 in 10 minutes, from 2 s and 30.
    const val MIN_GAP_MS = 4_000L
    const val WINDOW_MS = 10 * 60_000L
    const val MAX_IN_WINDOW = 20

    /** How long to wait before the next send, given when earlier sends went out. 0 means now. */
    fun waitMs(sentAt: List<Long>, now: Long): Long {
        val last = sentAt.maxOrNull() ?: return 0
        val gap = (last + MIN_GAP_MS - now).coerceAtLeast(0)
        val recent = sentAt.filter { now - it < WINDOW_MS }.sorted()
        val window = if (recent.size >= MAX_IN_WINDOW) recent[recent.size - MAX_IN_WINDOW] + WINDOW_MS - now else 0
        return maxOf(gap, window, 0)
    }
}
