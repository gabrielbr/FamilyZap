package app.oie.logic

/**
 * The first gate for a message from the real WhatsApp link, before Inbox.decide (product law):
 *  - a chat the parent didn't allow gives null, so nothing from it is ever stored;
 *  - a message from the parent's account is either the tablet's own send coming back (in [ours]),
 *    already shown, or the parent writing from their phone, which stays private: null either way;
 *  - otherwise it becomes a Message on the allowed contact, matched by either of its addresses.
 */
object RealInbox {
    fun toMessage(
        id: String, chat: String, chatAlt: String?, fromMe: Boolean, ours: Set<String>, allowed: List<Contact>,
        at: Long, kind: Kind, text: String, replyTo: String?, durationSec: Int, size: Long = 0,
    ): Message? {
        val contact = allowed.firstOrNull { it.id == chat || (chatAlt != null && it.id == chatAlt) } ?: return null
        if (fromMe) return null
        if (id in ours) return null
        return Message(id, contact.id, false, at, kind, text, replyTo, durationSec, size = size)
    }
}
