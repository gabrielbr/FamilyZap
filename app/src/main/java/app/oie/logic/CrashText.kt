package app.oie.logic

/**
 * v0.58: a crash, written for the problem report. A Compose crash's stack trace is hundreds of
 * frames deep, so the whole trace cut at 4,000 characters could lose the cause. This keeps what
 * explains it: the error and its first frames, then each cause with its first frames.
 */
object CrashText {
    const val FRAMES = 14
    const val CAUSE_FRAMES = 8
    const val MAX_CAUSES = 4

    fun of(e: Throwable): String {
        val out = StringBuilder()
        fun part(t: Throwable, frames: Int, prefix: String) {
            out.append(prefix).append(t.toString())
            val st = t.stackTrace
            st.take(frames).forEach { out.append("\n\tat ").append(it) }
            if (st.size > frames) out.append("\n\t... ").append(st.size - frames).append(" more")
        }
        part(e, FRAMES, "")
        var cause = e.cause
        var n = 0
        val seen = mutableSetOf<Throwable>(e)
        while (cause != null && n < MAX_CAUSES && seen.add(cause)) {
            out.append('\n')
            part(cause, CAUSE_FRAMES, "Caused by: ")
            cause = cause.cause; n++
        }
        return out.toString()
    }
}
