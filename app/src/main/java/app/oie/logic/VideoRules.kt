package app.oie.logic

/** The kid's videos and GIFs before sending (docs/flows/kid-sends-message.md, v0.28). */
object VideoRules {
    /** The owner's limit: 3 minutes (a second of slack for the camera's rounding). */
    const val MAX_MS: Long = 3 * 60 * 1000L + 1_000
    /** After shrinking, a video must be at most this to send (and to hold in memory to seal). */
    const val MAX_BYTES: Long = 64L * 1024 * 1024
    /** Videos are shrunk so their short side is at most this (720p). */
    const val SHORT_SIDE = 720
    /** A GIF becomes a short silent MP4 at this rate, at most [GIF_MAX_SIDE] px, up to [GIF_MAX_MS] long. */
    const val GIF_FPS = 15
    const val GIF_MAX_SIDE = 480
    const val GIF_MAX_MS = 30_000

    enum class Check { OK, TOO_LONG, TOO_BIG }

    fun length(durationMs: Long): Check = if (durationMs > MAX_MS) Check.TOO_LONG else Check.OK
    fun size(bytes: Long): Check = if (bytes > MAX_BYTES) Check.TOO_BIG else Check.OK

    /**
     * The size to shrink a video to, as shown (rotation applied): its short side at [SHORT_SIDE], both
     * sides even (encoders need it). Null when it's already that small: it's not scaled up.
     */
    fun target(width: Int, height: Int, rotation: Int): Pair<Int, Int>? {
        val (w, h) = if (rotation % 180 != 0) height to width else width to height
        if (w <= 0 || h <= 0 || minOf(w, h) <= SHORT_SIDE) return null
        val scale = SHORT_SIDE.toDouble() / minOf(w, h)
        return even(w * scale) to even(h * scale)
    }

    /** A GIF's size in its MP4: at most [GIF_MAX_SIDE] on the long side, even, at least 2. */
    fun gifSize(width: Int, height: Int): Pair<Int, Int> {
        val scale = minOf(1.0, GIF_MAX_SIDE.toDouble() / maxOf(width, height, 1))
        return maxOf(2, even(width * scale)) to maxOf(2, even(height * scale))
    }

    /** How many frames a GIF of [durationMs] becomes (one loop, at least one frame, capped). */
    fun gifFrames(durationMs: Int): Int {
        val d = durationMs.coerceIn(1, GIF_MAX_MS)
        return maxOf(1, (d.toLong() * GIF_FPS + 999) / 1000).toInt()
    }

    private fun even(x: Double): Int = (Math.round(x).toInt() / 2) * 2
}
