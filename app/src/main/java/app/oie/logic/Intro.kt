package app.oie.logic

/**
 * The first message in a chat (v0.47, docs/flows/intro-message.md; the owner, 2026-11-21: "when the kid messages the
 * first time in an individual chat or in a group, Oiê should send a message automatically before … saying that the
 * kid is using the parent's phone via one app, and a link"). The wording was approved by the owner on 2026-11-21.
 *  - Sent once per chat, ever, before the kid's next message, in every chat (the owner chose "every chat, once").
 *  - Never on the line to the parent (it's their own chat), and not when the parent turned it off in Ajustes.
 *  - It goes out at the usual human pace (SendPace) and its echo never shows on the tablet.
 */
object Intro {
    /** Every intro starts with this, like Oiê's answers to commands, so its echo in a group is never shown as the parent's. */
    const val MARK = "Oiê ·"
    const val LINK_PT = "https://gabrielbr.github.io/oie-releases/falar-com-a-crianca/"
    const val LINK_EN = "https://gabrielbr.github.io/oie-releases/talk-to-a-child/"

    fun needed(on: Boolean, chat: Contact, introduced: Set<String>): Boolean = on && !chat.parentLine && chat.id !in introduced
}
