package app.oie.logic

/**
 * The first gate for a message from the real WhatsApp link, before Inbox.decide (product law):
 *  - a chat the parent didn't allow gives null, so nothing from it is ever stored;
 *  - a message from the parent's account is either the tablet's own send coming back (in [ours]),
 *    already shown, or the parent writing from their phone, which stays private (null), except in an
 *    allowed group, where it's part of the group's talk (v0.46, fromParent);
 *  - otherwise it becomes a Message on the allowed contact, matched by either of its addresses.
 */
object RealInbox {
    fun toMessage(
        id: String, chat: String, chatAlt: String?, fromMe: Boolean, ours: Set<String>, allowed: List<Contact>,
        at: Long, kind: Kind, text: String, replyTo: String?, durationSec: Int, size: Long = 0,
        kidIds: Set<String> = emptySet(), kidTag: String = "",
    ): Message? {
        val contact = allowed.firstOrNull { it.id == chat || (chatAlt != null && it.id == chatAlt) } ?: return null
        if (id in ours) return null
        // v0.30: on the line to the parent who owns the WhatsApp, the parent writes "from me" (it's their
        // account); that's the parent talking to the kid.
        // v0.46: in an allowed group, what the parent writes from their phone is part of the group's talk, shown
        // under the parent's name. The tablet's own sends coming back never count (ours, the kid's history, the
        // kid's signature). Anywhere else, the parent's own writing stays private.
        if (fromMe && !contact.parentLine) {
            // v0.47: Oiê's own first message ("Oiê · …") is never the parent's either.
            if (!contact.isGroup || id in kidIds || (kidTag.isNotEmpty() && text.startsWith(kidTag)) || text.startsWith(Intro.MARK)) return null
            return Message(id, contact.id, false, at, kind, text, replyTo, durationSec, size = size, fromParent = true)
        }
        return Message(id, contact.id, false, at, kind, text, replyTo, durationSec, size = size)
    }

    /**
     * v0.40: the parent writing from their phone in an allowed chat (not the line to the kid). Returns that
     * contact's id, so the 24-hour window can close; null for anything else. The tablet's own sends never count:
     * not one in [ours], not one already in the kid's history ([kidIds], for after a restart), and not a text
     * that starts with the kid's signature ([kidTag], e.g. "😎*Tito*"). Only the time is kept, never the text.
     */
    fun parentWriting(
        id: String, chat: String, chatAlt: String?, fromMe: Boolean, ours: Set<String>, allowed: List<Contact>,
        kidIds: Set<String>, text: String, kidTag: String,
    ): String? {
        if (!fromMe || id in ours || id in kidIds) return null
        val contact = allowed.firstOrNull { it.id == chat || (chatAlt != null && it.id == chatAlt) } ?: return null
        if (contact.parentLine) return null
        if (kidTag.isNotEmpty() && text.startsWith(kidTag)) return null
        return contact.id
    }
}
