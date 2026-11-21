package app.oie.logic

/**
 * Commands the parent sends from their own WhatsApp ("/Tito parar"), acting on the chat they're
 * typed in (docs/flows/parent-commands.md, v0.21). Only messages from the parent's own account are
 * read as commands (the caller checks "from me"): no one else can change who reaches the kid.
 */
object ParentCommand {
    enum class Action { HELP, PAUSE, RESUME, ADD, REMOVE, LIST, KIDS_OWN, NOT_KIDS_OWN, QUIET_ON, QUIET_OFF, UNKNOWN }

    /**
     * [name]: the kid's name as the parent typed it after the slash; [word]: what came after, for "não entendi";
     * [arg]: the rest (v0.43), a number from "/Tito contatos" or a name, for commands typed in the parent's own chat.
     */
    data class Parsed(val action: Action, val name: String, val word: String, val arg: String = "")

    /** A command older than this is ignored: it may no longer be what the parent wants. */
    const val MAX_AGE_MS: Long = 24L * 60 * 60 * 1000

    private val WORDS: Map<String, Action> = buildMap {
        listOf("", "ajuda", "help", "?", "comandos", "commands").forEach { put(it, Action.HELP) }
        listOf("parar", "pausar", "pare", "pausa", "stop", "pause").forEach { put(it, Action.PAUSE) }
        listOf("voltar", "volta", "continuar", "retomar", "start", "resume").forEach { put(it, Action.RESUME) }
        listOf("adicionar", "incluir", "add").forEach { put(it, Action.ADD) }
        listOf("remover", "tirar", "remove").forEach { put(it, Action.REMOVE) }
        // v0.43: from the parent's own chat ("Mensagem para você").
        listOf("contatos", "contato", "lista", "listar", "contacts", "list").forEach { put(it, Action.LIST) }
        listOf("so", "exclusivo", "own").forEach { put(it, Action.KIDS_OWN) }
        listOf("normal").forEach { put(it, Action.NOT_KIDS_OWN) }
        listOf("silencio", "quiet").forEach { put(it, Action.QUIET_ON) }
        listOf("liberar", "release").forEach { put(it, Action.QUIET_OFF) }
    }

    /** "/Tito só dele 3": the word after "só" says who, it's not the target. */
    private val OWN_FILLERS = setOf("dele", "dela", "delu", "do", "da")

    /** "/Tito parar" → PAUSE with "Tito". Not a command (null) unless it's a slash right before one of [names]. */
    fun parse(text: String, names: List<String>): Parsed? {
        val t = text.trim()
        if (!t.startsWith("/")) return null
        val rest = MessageRule.fold(t.drop(1))
        val name = names.filter { it.isNotBlank() }.sortedByDescending { it.length }.firstOrNull { n ->
            val f = MessageRule.fold(n.trim())
            rest.startsWith(f) && rest.getOrNull(f.length).let { it == null || it.isWhitespace() }
        } ?: return null
        val after = rest.drop(MessageRule.fold(name.trim()).length).trim()
        val word = after.substringBefore(' ').substringBefore('\n')
        val action = WORDS[word] ?: Action.UNKNOWN
        // The argument keeps the parent's own letters (accents, capitals) for matching names.
        var arg = t.drop(1).trim().drop(name.trim().length).trim().let { r -> r.drop(r.substringBefore(' ').substringBefore('\n').length).trim() }
        if (action == Action.KIDS_OWN) arg.substringBefore(' ').let { if (MessageRule.fold(it) in OWN_FILLERS) arg = arg.drop(it.length).trim() }
        return Parsed(action, name.trim(), word, arg)
    }

    enum class Outcome {
        HELP, UNKNOWN, PAUSED, RESUMED, ADDED, ALREADY_IN, REMOVED, NOT_IN_LIST, OWN_CHAT,
        // v0.43
        LIST, KIDS_OWN_ON, KIDS_OWN_OFF, QUIET_ON, QUIET_OFF, SEND_CARD, NEED_WHO, NO_SUCH, NO_WHATSAPP,
    }

    /** The new allowed list, what happened, and the contact it happened to (for the answer). */
    data class Result(val allowed: List<Contact>, val outcome: Outcome, val contact: Contact?)

    /**
     * Applies [action] to the chat ([chatIds]: its addresses, by number and by private id). [candidate] is
     * the chat as a new contact, used by ADD. [ownChat]: typed in the parent's own chat, where only help works.
     */
    fun apply(action: Action, chatIds: List<String>, ownChat: Boolean, candidate: Contact, allowed: List<Contact>, arg: String = ""): Result {
        // v0.43: these work anywhere (the answer always goes to the parent's own chat).
        when (action) {
            Action.LIST -> return Result(allowed, Outcome.LIST, null)
            Action.QUIET_ON -> return Result(allowed, Outcome.QUIET_ON, null)
            Action.QUIET_OFF -> return Result(allowed, Outcome.QUIET_OFF, null)
            else -> {}
        }
        // v0.43: in the parent's own chat, the target is a number from "/Tito contatos" or a name.
        if (ownChat && action in TARGETED) {
            if (arg.isBlank()) return Result(allowed, Outcome.NEED_WHO, null)
            val ct = target(arg, allowed) ?: return Result(allowed, Outcome.NO_SUCH, null)
            return apply(action, listOf(ct.id), ownChat = false, candidate = ct, allowed = allowed)
        }
        if (ownChat && action == Action.ADD) return Result(allowed, Outcome.SEND_CARD, null)
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
            // The line to the parent is never the kid's own (its notes stay private).
            action == Action.KIDS_OWN && ct.parentLine -> Result(allowed, Outcome.NOT_IN_LIST, null)
            action == Action.KIDS_OWN -> ct.copy(showWholeGroup = true).let { Result(with(it), Outcome.KIDS_OWN_ON, it) }
            action == Action.NOT_KIDS_OWN -> ct.copy(showWholeGroup = false).let { Result(with(it), Outcome.KIDS_OWN_OFF, it) }
            else -> Result(allowed - ct, Outcome.REMOVED, ct)
        }
    }

    private val TARGETED = setOf(Action.PAUSE, Action.RESUME, Action.REMOVE, Action.KIDS_OWN, Action.NOT_KIDS_OWN)

    /** The contacts as "/Tito contatos" numbers them: the kid's people and groups, never the line to the parent. */
    fun numbered(allowed: List<Contact>): List<Contact> = allowed.filter { !it.parentLine }

    /** "3" is the third in [numbered]; otherwise a name (or WhatsApp name) that matches one contact only. */
    fun target(arg: String, allowed: List<Contact>): Contact? {
        val list = numbered(allowed)
        arg.trim().toIntOrNull()?.let { n -> return list.getOrNull(n - 1) }
        val q = MessageRule.fold(arg.trim())
        list.filter { MessageRule.fold(it.name) == q || MessageRule.fold(it.waName) == q }.singleOrNull()?.let { return it }
        return list.filter { MessageRule.fold(it.name).contains(q) || MessageRule.fold(it.waName).contains(q) }.singleOrNull()
    }

    /** How long after "/Tito adicionar" in the parent's own chat a shared contact card is added (the owner's choice). */
    const val CARD_WINDOW_MS: Long = 10L * 60 * 1000

    /** One contact from a shared card: its name and its WhatsApp number's digits ("" when it has no WhatsApp). */
    data class Card(val name: String, val digits: String)

    /** Adds a shared card's person, like "/Tito adicionar" in their chat. A card with no WhatsApp number is refused. */
    fun addCard(card: Card, allowed: List<Contact>): Result {
        if (card.digits.isEmpty()) return Result(allowed, Outcome.NO_WHATSAPP, Contact("", card.name))
        val id = "${card.digits}@s.whatsapp.net"
        val c = Contact(id, card.name.ifBlank { ContactNames.display(card.digits) }, phone = card.digits)
        val i = allowed.indexOfFirst { it.id == id || (it.phone.isNotEmpty() && it.phone == card.digits) }
        if (i < 0) return Result(allowed + c, Outcome.ADDED, c)
        val ct = allowed[i]
        return if (ct.paused) ct.copy(paused = false).let { Result(allowed.toMutableList().also { l -> l[i] = it }, Outcome.RESUMED, it) }
        else Result(allowed, Outcome.ALREADY_IN, ct)
    }

    /** Whether a command sent at [at] is still acted on at [now]. */
    fun fresh(at: Long, now: Long): Boolean = now - at <= MAX_AGE_MS
}
