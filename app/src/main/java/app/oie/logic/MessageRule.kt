package app.oie.logic

import java.text.Normalizer

/**
 * Which incoming messages reach the kid's tablet (docs/flows/kid-receives-messages.md).
 * Approved by the owner on 2026-11-18, to be tested with real family chats before the real link ships.
 *
 * A message from an allowed chat reaches the kid when it:
 *  1. replies to (quotes) one of the kid's messages, at any time; or
 *  2. starts with one of the kid's names ("Tito, …"), after at most one short greeting ("Oi Tito", v0.21); or
 *  3. arrives within 24 hours after the kid's last message in that chat; or
 *  4. comes from a group the parent set to "Mostrar tudo do grupo".
 * Everything else stays private to the parent's WhatsApp. Messages from chats that aren't allowed
 * never reach this function (product law): the caller drops them first.
 */
object MessageRule {
    const val WINDOW_MS: Long = 24L * 60 * 60 * 1000

    /** Why a message reaches the kid, or null when it stays private to the parent's WhatsApp. */
    enum class Reason { FROM_KID, WHOLE_GROUP, REPLY, NAME, WINDOW }

    fun isForKid(message: Message, chat: Contact, history: List<Message>, kidName: String): Boolean =
        reason(message, chat, history, listOf(kidName)) != null

    fun isForKid(message: Message, chat: Contact, history: List<Message>, names: List<String>): Boolean =
        reason(message, chat, history, names) != null

    fun reason(message: Message, chat: Contact, history: List<Message>, kidName: String): Reason? = reason(message, chat, history, listOf(kidName))

    /** [names]: the kid's name and nicknames (v0.21, KidNames.all). */
    fun reason(message: Message, chat: Contact, history: List<Message>, names: List<String>): Reason? {
        if (message.fromKid) return Reason.FROM_KID
        if (chat.isGroup && chat.showWholeGroup) return Reason.WHOLE_GROUP
        val kidMessages = history.filter { it.fromKid && it.chatId == message.chatId && it.at <= message.at }
        if (message.replyTo != null && kidMessages.any { it.id == message.replyTo }) return Reason.REPLY
        if (addressed(message.text, names)) return Reason.NAME
        val lastKid = kidMessages.maxOfOrNull { it.at } ?: return null
        return if (message.at - lastKid <= WINDOW_MS) Reason.WINDOW else null
    }

    /** "Tito, …", "tito!", "Títo:" all count; "Titozão" doesn't (the name must end the word). */
    fun startsWithName(text: String, kidName: String): Boolean {
        val name = fold(kidName.trim())
        if (name.isEmpty()) return false
        val t = fold(text.trimStart())
        if (!t.startsWith(name)) return false
        val next = t.getOrNull(name.length) ?: return true
        return !next.isLetterOrDigit()
    }

    /**
     * Starts with one of [names], after at most one short greeting (v0.21, the owner: "Oi Tito" is how
     * people start). "Oi, o Tito está aí?" doesn't count: the name must come right after the greeting.
     */
    fun addressed(text: String, names: List<String>): Boolean {
        val t = fold(text.trimStart())
        val afterGreeting = GREETINGS.firstOrNull { g -> t.startsWith(g) && t.getOrNull(g.length)?.isLetterOrDigit() != true }
            ?.let { g -> t.substring(g.length).trimStart(' ', ',', '!', '.', '\n') }
        return names.any { n -> startsWithName(t, n) || (afterGreeting != null && startsWithName(afterGreeting, n)) }
    }

    /** One short greeting, folded (no accents, lower case), longest first so "bom dia" wins over "bom". */
    private val GREETINGS = listOf(
        "good morning", "boa tarde", "boa noite", "bom dia", "e ai", "hello", "oiii", "oii", "oie", "eai", "ola", "alo", "hey", "oi", "ei", "hi",
    ).sortedByDescending { it.length }

    internal fun fold(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()
}
