package app.oie.logic

/**
 * The kid's name and nicknames (v0.21, the owner: "Francisco, but also Tito, Chico, Fran"). Any of
 * them starts a message for the kid and works in the parent's commands ("/Chico"). The kid's own
 * messages are signed with the main name.
 */
object KidNames {
    /** Most nicknames kept, and the longest one. */
    const val MAX = 10
    const val MAX_LENGTH = 30

    /** "Tito, Chico; Fran" → [Tito, Chico, Fran]: trimmed, no repeats (ignoring capitals and accents), no blanks. */
    fun parse(text: String): List<String> =
        text.split(',', ';', '/', '\n').map { it.trim().replace(Regex("\\s+"), " ").take(MAX_LENGTH) }.filter { it.isNotEmpty() }
            .distinctBy { MessageRule.fold(it) }.take(MAX)

    /** The main name first, then the nicknames that aren't the same name. */
    fun all(main: String, nicknames: List<String>): List<String> =
        (listOf(main.trim()) + nicknames).filter { it.isNotBlank() }.distinctBy { MessageRule.fold(it) }
}
