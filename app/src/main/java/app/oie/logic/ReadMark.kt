package app.oie.logic

/**
 * v0.55 (the owner: "messages sent to the kid can be marked as read on the parent's device, so it avoids bombarding
 * the parent with notifications that are not for them"; they chose "as soon as it arrives", on by default). A message
 * that reached the kid is marked read on the parent's WhatsApp a moment after it arrives, so the phone's chat shows
 * read and its notification goes. The sender sees the blue ticks.
 *  - Only what reached the kid (the message rule said notify, or quiet in quiet hours). What stays with the parent
 *    (talk about the kid, a message without the kid's name) is never touched: it's for them.
 *  - Never the line to the parent, never the parent's own messages, never a deleted message.
 *  - Off when the parent turned it off in Ajustes.
 */
object ReadMark {
    fun should(on: Boolean, decision: Inbox.Decision, chat: Contact?, m: Message): Boolean =
        on && chat != null && !chat.parentLine && !m.fromKid && !m.fromParent && m.kind != Kind.DELETED &&
            (decision == Inbox.Decision.NOTIFY || decision == Inbox.Decision.QUIET)
}
