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
 *  4. comes from a group the parent set to "Mostrar tudo do grupo" (since v0.46: from any allowed group).
 * v0.40 (the owner, 2026-11-21: "o Tito já chegou?" reached the tablet): the 24-hour window no longer lets in
 *  - a message that talks about the kid ("o Tito já chegou?", "manda beijo pro Tito"), see [aboutKid];
 *  - anything after the parent wrote in that chat, until the kid writes again (the parent took over).
 * Everything else stays private to the parent's WhatsApp. Messages from chats that aren't allowed
 * never reach this function (product law): the caller drops them first.
 */
object MessageRule {
    const val WINDOW_MS: Long = 24L * 60 * 60 * 1000

    /** Why a message reaches the kid, or null when it stays private to the parent's WhatsApp. */
    enum class Reason { FROM_KID, WHOLE_GROUP, REPLY, NAME, WINDOW }

    fun isForKid(message: Message, chat: Contact, history: List<Message>, kidName: String): Boolean =
        reason(message, chat, history, listOf(kidName)) != null

    fun isForKid(message: Message, chat: Contact, history: List<Message>, names: List<String>, parentWroteAt: Long? = null): Boolean =
        reason(message, chat, history, names, parentWroteAt) != null

    fun reason(message: Message, chat: Contact, history: List<Message>, kidName: String): Reason? = reason(message, chat, history, listOf(kidName))

    /**
     * [names]: the kid's name and nicknames (v0.21, KidNames.all). [parentWroteAt]: when the parent last wrote
     * in this chat from their phone (v0.40), or null if not known.
     */
    fun reason(message: Message, chat: Contact, history: List<Message>, names: List<String>, parentWroteAt: Long? = null): Reason? {
        if (message.fromKid) return Reason.FROM_KID
        // v0.42: a conversation the parent marked as the kid's own (a school friend, a cousins' group): all of it
        // reaches the kid, with no name check and no 24-hour window. Never the line to the parent (its notes stay private).
        if (chat.showWholeGroup && !chat.parentLine) return Reason.WHOLE_GROUP
        // v0.46 (the owner: "a group is a group … for the kids, I want to show all the content that happens in a group";
        // decision 0016): everything in a group the parent allowed reaches the kid, from everyone in it.
        if (chat.isGroup) return Reason.WHOLE_GROUP
        val kidMessages = history.filter { it.fromKid && it.chatId == message.chatId && it.at <= message.at }
        // v0.30 (decision 0007): the line to the parent who owns the WhatsApp. In its own group, all of it
        // is for the kid. In the parent's "Mensagem para você" (the fallback), their notes stay private:
        // only replies to the kid and messages that start with the kid's name.
        if (chat.parentLine) {
            if (chat.isGroup) return Reason.WHOLE_GROUP
            if (message.replyTo != null && kidMessages.any { it.id == message.replyTo }) return Reason.REPLY
            return if (addressed(message.text, names)) Reason.NAME else null
        }
        if (message.replyTo != null && kidMessages.any { it.id == message.replyTo }) return Reason.REPLY
        if (addressed(message.text, names)) return Reason.NAME
        // v0.40: talk about the kid is for the parent, whatever the window says.
        if (aboutKid(message.text, names)) return null
        val lastKid = kidMessages.maxOfOrNull { it.at } ?: return null
        // v0.40: the parent wrote after the kid, so what comes next answers the parent.
        if (parentWroteAt != null && parentWroteAt >= lastKid && parentWroteAt <= message.at) return null
        return if (message.at - lastKid <= WINDOW_MS) Reason.WINDOW else null
    }

    /**
     * v0.40: the message talks about the kid rather than to him: one of [names] comes right after a word that
     * makes it the third person ("o Tito já chegou?", "como está a Lia?", "manda beijo pro Tito", "falei com o
     * Tito", "is Tito home?"). Speaking to him isn't this: "Tito, …", "Oi Tito", "Te amo, Tito", "Beijo Tito", "Boa noite, Ana e Tito".
     */
    fun aboutKid(text: String, names: List<String>): Boolean {
        val words = WORD.findAll(fold(text)).map { it.value }.toList()
        for (n in names) {
            val name = WORD.findAll(fold(n.trim())).map { it.value }.toList()
            if (name.isEmpty()) continue
            for (i in 1..words.size - name.size) {
                if (words.subList(i, i + name.size) == name && words[i - 1] in THIRD_PERSON) return true
            }
        }
        return false
    }

    private val WORD = Regex("[\\p{L}\\p{N}]+")

    /** Words that put a name in the third person (folded). Portuguese first; a few for English. */
    private val THIRD_PERSON = setOf(
        "o", "a", "os", "as", "do", "da", "dos", "das", "no", "na", "nos", "nas", "ao", "aos",
        "pro", "pra", "pros", "pras", "pelo", "pela", "com", "para", "sobre", "de", "sem", "ate",
        "is", "was", "does", "did", "has", "with", "for", "about", "tell", "ask",
    )

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
