package app.oie.logic

/** What the "Bolha na tela" setting shows (docs/flows/bubble.md, v0.25). */
object BubblePolicy {
    enum class State { UNSUPPORTED, OFF, NEEDS_NOTIFICATIONS, NEEDS_PERMISSION, ON }

    /** Android's Bubbles came in Android 11 (API 30). */
    const val MIN_SDK = 30

    fun state(sdk: Int, switchOn: Boolean, notificationsAllowed: Boolean, bubblesAllowed: Boolean): State = when {
        sdk < MIN_SDK -> State.UNSUPPORTED
        !switchOn -> State.OFF
        !notificationsAllowed -> State.NEEDS_NOTIFICATIONS
        !bubblesAllowed -> State.NEEDS_PERMISSION
        else -> State.ON
    }
}
