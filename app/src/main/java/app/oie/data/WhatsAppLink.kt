package app.oie.data

import app.oie.logic.Contact
import app.oie.logic.Message

/**
 * The link to the parent's WhatsApp (decision 0001: a linked device on the kid's tablet).
 * v0.1 ships only [SampleLink]: no real connection, no network. The real implementation comes after
 * the message rule is tested with real chats (STATUS.md). Whatever implements this keeps the product
 * law: nothing is stored on our servers, and only allowed chats ever reach the app.
 */
interface WhatsAppLink {
    val connected: Boolean
    /** The parent's WhatsApp profile name, once linked. */
    val profileName: String?
    /** The parent's contacts and groups, for the parent to choose from. */
    fun contacts(): List<Contact>
    fun send(message: Message)
    /** The kid's reaction on [target] (v0.33); an empty [emoji] takes it away. */
    fun react(target: Message, emoji: String) {}
    /** A family member's status was seen on the tablet (v0.36). */
    fun markStatusViewed(status: app.oie.logic.Status) {}
    /** The kid's reaction to a status, sent to the poster's own chat; an empty [emoji] takes it away. */
    fun reactToStatus(status: app.oie.logic.Status, emoji: String) {}
    /** v0.37: the kid's status, to [Status.audience] only; the link answers with AppState.statusPosted. */
    fun postStatus(status: app.oie.logic.Status) {}
    /** v0.37: the kid's status taken back from the people it went to. */
    fun deleteStatus(status: app.oie.logic.Status) {}
}

/** Sample family data for the v0.1 shell. Names are examples. */
class SampleLink : WhatsAppLink {
    override var connected: Boolean = false
        private set
    override val profileName: String = "Ana"

    fun pretendLinked() { connected = true }

    fun pretendDropped() { connected = false }

    override fun contacts(): List<Contact> = listOf(
        Contact("vovo", "Vovó", phone = "5511987650001"), Contact("mae", "Mãe", phone = "5511987650002"),
        Contact("vovo2", "Vovô", phone = "5511987650003"), Contact("bia", "Tia Bia", phone = "5521998760004"),
        Contact("primos", "Primos", isGroup = true), Contact("marina", "Vizinha Marina", phone = "551134567890"),
    )

    val sent = mutableListOf<Message>()
    override fun send(message: Message) { sent += message }
    val reacted = mutableListOf<Pair<String, String>>()
    override fun react(target: Message, emoji: String) { reacted += target.id to emoji }
    val statusViews = mutableListOf<String>()
    override fun markStatusViewed(status: app.oie.logic.Status) { statusViews += status.id }
    override fun reactToStatus(status: app.oie.logic.Status, emoji: String) { reacted += status.id to emoji }
    val posted = mutableListOf<app.oie.logic.Status>()
    val deleted = mutableListOf<String>()
    override fun postStatus(status: app.oie.logic.Status) { posted += status }
    override fun deleteStatus(status: app.oie.logic.Status) { deleted += status.id }
}
