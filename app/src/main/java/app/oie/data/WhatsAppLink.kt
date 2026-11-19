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
}

/** Sample family data for the v0.1 shell. Names are examples. */
class SampleLink : WhatsAppLink {
    override var connected: Boolean = false
        private set
    override val profileName: String = "Ana"

    fun pretendLinked() { connected = true }

    fun pretendDropped() { connected = false }

    override fun contacts(): List<Contact> = listOf(
        Contact("vovo", "Vovó"), Contact("mae", "Mãe"), Contact("vovo2", "Vovô"),
        Contact("bia", "Tia Bia"), Contact("primos", "Primos", isGroup = true), Contact("marina", "Vizinha Marina"),
    )

    val sent = mutableListOf<Message>()
    override fun send(message: Message) { sent += message }
}
