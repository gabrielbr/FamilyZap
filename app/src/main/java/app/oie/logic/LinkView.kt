package app.oie.logic

/**
 * The link as the screens show it (docs/flows/parent-setup-link.md, v0.24). Reconnecting after an
 * update, a restart or a short drop takes a few seconds; for that time the screens say nothing to
 * the kid and "Conectando…" to the parent, instead of "desconectado" with a Connect button (the
 * owner's report).
 */
object LinkView {
    enum class Shown { CONNECTED, CONNECTING, DOWN }

    /** How long "not up yet" counts as connecting before it's shown as down. */
    const val GRACE_MS = 30_000L

    /**
     * [wanted]: the parent linked WhatsApp. [trying]: the link is starting, connecting, or dropped and
     * reconnecting (not logged out, banned or showing a QR). [since]: when it stopped being up.
     */
    fun of(connected: Boolean, wanted: Boolean, trying: Boolean, since: Long, now: Long): Shown = when {
        connected -> Shown.CONNECTED
        wanted && trying && now - since < GRACE_MS -> Shown.CONNECTING
        else -> Shown.DOWN
    }
}
