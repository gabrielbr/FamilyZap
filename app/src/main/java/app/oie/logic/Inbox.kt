package app.oie.logic

import java.time.LocalDateTime

/**
 * What happens to a message the WhatsApp link hands over (docs/flows/kid-receives-messages.md).
 * Applied before anything is stored, so a message that isn't for the kid never touches the tablet.
 */
object Inbox {
    enum class Decision {
        /** Not from an allowed chat, or not meant for the kid: never stored, never shown (product law). */
        DROP,
        /** Shown, with a notification. */
        NOTIFY,
        /** Shown, but quietly: quiet hours are on and the sender isn't an exception. Summed up when they end. */
        QUIET,
        /** Removes an earlier message ("delete for everyone"). */
        DELETE,
    }

    fun decide(message: Message, allowed: List<Contact>, history: List<Message>, kidName: String, periods: List<QuietPeriod>, now: LocalDateTime): Decision =
        decide(message, allowed, history, listOf(kidName), periods, now)

    fun decide(
        message: Message,
        allowed: List<Contact>,
        history: List<Message>,
        names: List<String>,
        periods: List<QuietPeriod>,
        now: LocalDateTime,
    ): Decision {
        val chat = allowed.firstOrNull { it.id == message.chatId } ?: return Decision.DROP
        // v0.21: a chat the parent paused from their WhatsApp ("/Tito parar") reaches the kid in no way.
        if (chat.paused) return Decision.DROP
        if (message.kind == Kind.DELETED) return if (history.any { it.id == message.replyTo }) Decision.DELETE else Decision.DROP
        if (!MessageRule.isForKid(message, chat, history, names)) return Decision.DROP
        if (message.fromKid) return Decision.QUIET // the kid's own messages never notify
        return if (QuietHours.canSend(periods, chat, now)) Decision.NOTIFY else Decision.QUIET
    }
}
