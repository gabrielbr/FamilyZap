package app.oie.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.oie.logic.Contact
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.logic.MessageRule
import app.oie.logic.Pin
import app.oie.logic.QuietHours
import app.oie.logic.QuietPeriod
import java.security.MessageDigest
import java.time.LocalDateTime

/** Where the app is (the files in docs/flows). System Back walks this backwards. */
sealed interface Route {
    data class Setup(val step: Int) : Route // 0 welcome, 1 risk, 2 link, 3 names, 4 pin, 5 contacts, 6 quiet, 7 done
    data object KidHome : Route
    data class Conversation(val contactId: String) : Route
    data object ParentPin : Route
    data class Parent(val section: Int) : Route // 0 contacts, 1 quiet hours, 2 connection, 3 about
    data object Pair : Route // Conexão › Conectar o WhatsApp
}

/**
 * The whole app state, in memory for v0.1. Next: persist on the tablet (encrypted), never on a server.
 */
/** [clock] is the real time in the app; tests pin it so screenshots stay the same on every run. */
class AppState(val link: SampleLink = SampleLink(), val clock: () -> Long = System::currentTimeMillis) {
    var route: Route by mutableStateOf(Route.Setup(0))
    var parentName by mutableStateOf("")
    var kidName by mutableStateOf("")
    /** SHA-256 of the PIN, never the PIN itself. */
    internal var pinHash: String? = null
    val pinGuard = Pin.Guard()
    var kidNoticeSeen by mutableStateOf(false)
    /** The kid asked the parent to add someone (owner, 2026-11-18). No names, no typing: just a flag. */
    var addRequested by mutableStateOf(false)
    /** The parent opened a contact once (v0.19): the "tap someone" hint in Contatos goes (less-is-more: no permanent hints). */
    var contactTipUsed by mutableStateOf(false)
    val allowed = mutableStateListOf<Contact>()
    val periods = mutableStateListOf<QuietPeriod>().apply { addAll(QuietHours.presets) }
    val messages = mutableStateListOf<Message>()
    /** The rule test with the parent's real WhatsApp (docs/flows/rule-test.md). */
    val linkState = LinkState()
    /** The kid's side uses the parent's real WhatsApp (v0.8). Off until the parent turns it on; there's no way back but unlinking. */
    var realMode by mutableStateOf(false)
    /** The real link, set by the app (OieApp); null in tests. */
    var real: WhatsAppLink? = null
    /** The link the kid's side talks through: the real one once the parent turned it on, else the sample. */
    val wa: WhatsAppLink get() = if (realMode) real ?: link else link
    /** Stickers from the parent's own chat: new ones the kid can save, and saved ones (v0.10). */
    val stickers = mutableStateListOf<app.oie.logic.FamilySticker>()

    /** A new sticker from the parent. Returns the file names that dropped off (for the caller to delete). */
    fun offerSticker(s: app.oie.logic.FamilySticker): List<String> {
        if (stickers.any { it.id == s.id }) return listOf(s.media)
        stickers.add(0, s)
        val dropped = mutableListOf<String>()
        while (stickers.count { !it.saved } > NEW_STICKERS_MAX) {
            val old = stickers.last { !it.saved }; stickers.remove(old); dropped += old.media
        }
        return dropped
    }

    fun saveSticker(id: String) {
        val i = stickers.indexOfFirst { it.id == id }
        if (i >= 0) { val s = stickers.removeAt(i).copy(saved = true); stickers.add(stickers.count { !it.saved }, s) }
    }

    /** Messages the kid sent that didn't go out (rule L4: one retry, then the kid is told). */
    val failed = mutableStateListOf<String>()

    /** Turns the kid's side to the real WhatsApp: the sample chats and contacts go; the parent picks real ones. */
    fun goReal() {
        realMode = true
        allowed.clear(); messages.clear(); drafts.clear(); lastSeen.clear(); failed.clear(); quietCount = 0
    }
    /** Unsent drafts per contact (flow: Remembers). */
    val drafts = mutableStateMapOf<String, String>()
    /** The last photo source the kid used, opened first next time (flow: Remembers). */
    var lastPhotoSource by mutableStateOf("camera")

    fun setPin(pin: String) { pinHash = hash(pin) }
    fun checkPin(pin: String, now: Long): Boolean = pinGuard.attempt(pinHash == hash(pin), now)

    fun quietNow(now: LocalDateTime = LocalDateTime.now()): QuietPeriod? = QuietHours.active(periods, now)
    fun canSend(contact: Contact, now: LocalDateTime = LocalDateTime.now()) = QuietHours.canSend(periods, contact, now)

    /** Messages the kid sees in a chat: the approved rule (MessageRule) over the chat's history. */
    fun visible(contactId: String): List<Message> {
        val chat = allowed.firstOrNull { it.id == contactId } ?: return emptyList()
        val history = messages.filter { it.chatId == contactId }.sortedBy { it.at }
        return history.filter { MessageRule.isForKid(it, chat, history, kidName) }
    }

    fun unread(contactId: String): Int = visible(contactId).count { !it.fromKid && it.at > lastSeen.getOrDefault(contactId, 0L) }
    val lastSeen = mutableStateMapOf<String, Long>()

    /** Messages that arrived quietly during quiet hours and haven't been summed up yet. */
    var quietCount by mutableStateOf(0)

    /** Every message from the WhatsApp link comes in here (logic/Inbox.kt decides). Returns what happened. */
    fun receive(m: Message, now: LocalDateTime = LocalDateTime.now()): app.oie.logic.Inbox.Decision {
        val history = messages.filter { it.chatId == m.chatId }
        val d = app.oie.logic.Inbox.decide(m, allowed, history, kidName, periods, now)
        when (d) {
            app.oie.logic.Inbox.Decision.DROP -> {}
            app.oie.logic.Inbox.Decision.DELETE -> {
                val i = messages.indexOfFirst { it.id == m.replyTo }
                if (i >= 0) messages[i] = messages[i].copy(kind = Kind.DELETED, text = "", media = null, durationSec = 0)
            }
            app.oie.logic.Inbox.Decision.QUIET -> { messages += m; if (!m.fromKid) quietCount++ }
            app.oie.logic.Inbox.Decision.NOTIFY -> messages += m
        }
        return d
    }

    /** When quiet hours end, the quiet messages become one summary. Returns how many to announce (0 = nothing). */
    fun takeQuietSummary(now: LocalDateTime = LocalDateTime.now()): Int {
        if (quietCount == 0 || quietNow(now) != null) return 0
        return quietCount.also { quietCount = 0 }
    }

    companion object {
        /** New stickers waiting for the kid; older ones drop off. Saved ones stay. */
        const val NEW_STICKERS_MAX = 12
    }

    fun finishSetup() {
        route = Route.KidHome
        seedSampleChat()
    }

    /** Sample chats so the shell shows real states. One private message to the parent stays hidden by the rule. */
    private fun seedSampleChat() {
        if (messages.isNotEmpty() || allowed.none { it.id == "vovo" }) return
        val now = clock()
        val h = 60L * 60 * 1000
        messages += Message("p1", "vovo", false, now - 30 * h, Kind.TEXT, "Filha, me liga quando puder") // private: hidden
        messages += Message("s1", "vovo", false, now - 2 * h, Kind.TEXT, "$kidName, como foi a escola?")
        messages += Message("k1", "vovo", true, now - 90 * 60 * 1000, Kind.VOICE, durationSec = 12)
        messages += Message("s2", "vovo", false, now - 80 * 60 * 1000, Kind.VOICE, durationSec = 9)
        messages += Message("s3", "vovo", false, now - 20 * 60 * 1000, Kind.TEXT, "Que saudade. Domingo tem bolo aqui em casa.")
    }

    private fun hash(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(("oie:" + s).toByteArray()).joinToString("") { "%02x".format(it) }
}
