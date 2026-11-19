package app.oie.logic

/**
 * The rule test (docs/flows/rule-test.md): the parent links their real WhatsApp read-only, picks a few
 * chats, and sees which of their real messages the message rule would pass to the kid. It sends
 * nothing. It keeps the text only of messages that would reach the kid (they'd be on the tablet in
 * real use anyway); for the rest it keeps who, when and why, so the parent's private chats stay private.
 */
object RuleTest {
    /** The newest entries kept; older ones fall off. */
    const val MAX = 200

    enum class Why { REPLY, NAME, WINDOW, WHOLE_GROUP, NOT_FOR_KID, PARENT_OWN }

    data class Entry(
        val chatId: String,
        val chatName: String,
        val at: Long,
        val kind: Kind,
        val shown: Boolean,
        val why: Why,
        /** Only for messages that would reach the kid. */
        val text: String?,
    )

    /**
     * [fromParent] is a message sent from the parent's own phone: in the test the tablet sends
     * nothing, so every message from the parent's account is the parent's own and stays private.
     * [kidSpokeAt] is when the parent pretended the kid sent a message in this chat ("Fingir que
     * {kid} falou agora"), which opens the rule's 24-hour window as a real message would.
     */
    fun judge(m: Message, chat: Contact, fromParent: Boolean, kidSpokeAt: Long?, kidName: String): Entry {
        if (fromParent) return Entry(chat.id, chat.name, m.at, m.kind, false, Why.PARENT_OWN, null)
        val history = kidSpokeAt?.let { listOf(Message("pretend", m.chatId, true, it, Kind.TEXT)) } ?: emptyList()
        val why = when (MessageRule.reason(m, chat, history, kidName)) {
            MessageRule.Reason.REPLY -> Why.REPLY
            MessageRule.Reason.NAME -> Why.NAME
            MessageRule.Reason.WINDOW -> Why.WINDOW
            MessageRule.Reason.WHOLE_GROUP -> Why.WHOLE_GROUP
            MessageRule.Reason.FROM_KID, null -> Why.NOT_FOR_KID
        }
        val shown = why != Why.NOT_FOR_KID
        return Entry(chat.id, chat.name, m.at, m.kind, shown, why, if (shown) m.text else null)
    }
}
