package app.oie.logic

/** The parent PIN (docs/flows/parent-setup-link.md, contacts-quiet-hours.md). */
object Pin {
    const val LENGTH = 6
    const val TRIES = 5
    /** The owner's choice, 2026-11-18: wait 1 minute after 5 wrong tries. */
    const val LOCK_MS = 60_000L

    enum class Problem { LENGTH, NOT_DIGITS, EASY }

    /** Why a new PIN is refused, or null when it's fine. Easy: all the same, or a straight run up or down. */
    fun problem(pin: String): Problem? {
        if (pin.length != LENGTH) return Problem.LENGTH
        if (!pin.all { it in '0'..'9' }) return Problem.NOT_DIGITS
        val d = pin.map { it - '0' }
        val steps = d.zipWithNext { a, b -> b - a }.toSet()
        if (steps == setOf(0) || steps == setOf(1) || steps == setOf(-1)) return Problem.EASY
        return null
    }

    /** Counts wrong tries and locks for [LOCK_MS] after [TRIES]. Times in epoch millis. */
    class Guard {
        private var wrong = 0
        private var lockedUntil = 0L

        fun lockedFor(now: Long): Long = (lockedUntil - now).coerceAtLeast(0)

        /** Returns true when [ok]; a wrong try counts, and the 5th starts the lock. */
        fun attempt(ok: Boolean, now: Long): Boolean {
            if (lockedFor(now) > 0) return false
            if (ok) { wrong = 0; return true }
            wrong++
            if (wrong >= TRIES) { wrong = 0; lockedUntil = now + LOCK_MS }
            return false
        }
    }
}
