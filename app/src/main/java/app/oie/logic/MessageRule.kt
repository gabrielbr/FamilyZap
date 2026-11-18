package app.oie.logic

import java.text.Normalizer

/**
 * Which incoming messages reach the kid's tablet (docs/flows/kid-receives-messages.md).
 * Approved by the owner on 2026-11-18, to be tested with real family chats before the real link ships.
 *
 * A message from an allowed chat reaches the kid when it:
 *  1. replies to (quotes) one of the kid's messages, at any time; or
 *  2. starts with the kid's name ("Tito, …"); or
 *  3. arrives within 24 hours after the kid's last message in that chat; or
 *  4. comes from a group the parent set to "Mostrar tudo do grupo".
 * Everything else stays private to the parent's WhatsApp. Messages from chats that aren't allowed
 * never reach this function (product law): the caller drops them first.
 */
object MessageRule {
    const val WINDOW_MS: Long = 24L * 60 * 60 * 1000

    fun isForKid(message: Message, chat: Contact, history: List<Message>, kidName: String): Boolean {
        if (message.fromKid) return true
        if (chat.isGroup && chat.showWholeGroup) return true
        val kidMessages = history.filter { it.fromKid && it.chatId == message.chatId && it.at <= message.at }
        if (message.replyTo != null && kidMessages.any { it.id == message.replyTo }) return true
        if (startsWithName(message.text, kidName)) return true
        val lastKid = kidMessages.maxOfOrNull { it.at } ?: return false
        return message.at - lastKid <= WINDOW_MS
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

    private fun fold(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()
}
