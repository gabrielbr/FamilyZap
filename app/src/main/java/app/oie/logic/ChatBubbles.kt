package app.oie.logic

/** A bubble per active chat (docs/flows/bubble.md, v0.29): its Android shortcut id, and which to drop. */
object ChatBubbles {
    private const val PREFIX = "chat:"

    fun shortcutId(contactId: String): String = PREFIX + contactId

    /** The chat a bubble's shortcut belongs to, or null for any other shortcut (the Oiê bubble). */
    fun contactOf(shortcutId: String?): String? = shortcutId?.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)

    /** The chats whose bubbles must go: no longer allowed, or paused. */
    fun stale(bubbled: Collection<String?>, reachable: Set<String>): Set<String> =
        bubbled.mapNotNull { contactOf(it) }.filter { it !in reachable }.toSet()
}
