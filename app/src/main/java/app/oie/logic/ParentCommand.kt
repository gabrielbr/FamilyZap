package app.oie.logic

/**
 * Commands the parent sends from their own WhatsApp ("/Tito parar"), acting on the chat they're
 * typed in (docs/flows/parent-commands.md, v0.21). Only messages from the parent's own account are
 * read as commands (the caller checks "from me"): no one else can change who reaches the kid.
 */
object ParentCommand {
    enum class Action { HELP, PAUSE, RESUME, ADD, REMOVE, UNKNOWN }

    /** [name]: the kid's name as the parent typed it after the slash; [word]: what came after, for "não entendi". */
    data class Parsed(val action: Action, val name: String, val word: String)

    /** A command older than this is ignored: it may no longer be what the parent wants. */
    const val MAX_AGE_MS: Long = 24L * 60 * 60 * 1000

    private val WORDS: Map<String, Action> = buildMap {
        listOf("", "ajuda", "help", "?", "comandos", "commands").forEach { put(it, Action.HELP) }
        listOf("parar", "pausar", "pare", "pausa", "stop", "pause").forEach { put(it, Action.PAUSE) }
        listOf("voltar", "volta", "continuar", "retomar", "start", "resume").forEach { put(it, Action.RESUME) }
        listOf("adicionar", "incluir", "add").forEach { put(it, Action.ADD) }
        listOf("remover", "tirar", "remove").forEach { put(it, Action.REMOVE) }
    }

    /** "/Tito parar" → PAUSE with "Tito". Not a command (null) unless it's a slash right before one of [names]. */
    fun parse(text: String, names: List<String>): Parsed? {
        val t = text.trim()
        if (!t.startsWith("/")) return null
        val rest = MessageRule.fold(t.drop(1))
        val name = names.filter { it.isNotBlank() }.sortedByDescending { it.length }.firstOrNull { n ->
            val f = MessageRule.fold(n.trim())
            rest.startsWith(f) && rest.getOrNull(f.length).let { it == null || it.isWhitespace() }
        } ?: return null
        val word = rest.drop(MessageRule.fold(name.trim()).length).trim().substringBefore(' ').substringBefore('\n')
        return Parsed(WORDS[word] ?: Action.UNKNOWN, name.trim(), word)
    }

    enum class Outcome { HELP, UNKNOWN, PAUSED, RESUMED, ADDED, ALREADY_IN, REMOVED, NOT_IN_LIST, OWN_CHAT }

    /** The new allowed list, what happened, and the contact it happened to (for the answer). */
    data class Result(val allowed: List<Contact>, val outcome: Outcome, val contact: Contact?)

    /**
     * Applies [action] to the chat ([chatIds]: its addresses, by number and by private id). [candidate] is
     * the chat as a new contact, used by ADD. [ownChat]: typed in the parent's own chat, where only help works.
     */
    fun apply(action: Action, chatIds: List<String>, ownChat: Boolean, candidate: Contact, allowed: List<Contact>): Result {
        val i = allowed.indexOfFirst { it.id in chatIds }
        val ct = allowed.getOrNull(i)
        fun with(c: Contact) = allowed.toMutableList().also { it[i] = c }
        return when {
            action == Action.HELP -> Result(allowed, Outcome.HELP, ct)
            action == Action.UNKNOWN -> Result(allowed, Outcome.UNKNOWN, ct)
            ownChat -> Result(allowed, Outcome.OWN_CHAT, null)
            action == Action.ADD && ct == null -> Result(allowed + candidate, Outcome.ADDED, candidate)
            action == Action.ADD && ct!!.paused -> ct.copy(paused = false).let { Result(with(it), Outcome.RESUMED, it) }
            action == Action.ADD -> Result(allowed, Outcome.ALREADY_IN, ct)
            ct == null -> Result(allowed, Outcome.NOT_IN_LIST, null)
            action == Action.PAUSE -> ct.copy(paused = true).let { Result(with(it), Outcome.PAUSED, it) }
            action == Action.RESUME -> ct.copy(paused = false).let { Result(with(it), Outcome.RESUMED, it) }
            else -> Result(allowed - ct, Outcome.REMOVED, ct)
        }
    }

    /** Whether a command sent at [at] is still acted on at [now]. */
    fun fresh(at: Long, now: Long): Boolean = now - at <= MAX_AGE_MS
}
