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
    /** Esqueci o PIN (v0.34): 0 = link WhatsApp again (the proof), 1 = the new PIN. */
    data class ForgotPin(val stage: Int) : Route
}

/**
 * The whole app state, in memory for v0.1. Next: persist on the tablet (encrypted), never on a server.
 */
/** [clock] is the real time in the app; tests pin it so screenshots stay the same on every run. */
class AppState(val link: SampleLink = SampleLink(), val clock: () -> Long = System::currentTimeMillis) {
    var route: Route by mutableStateOf(Route.Setup(0))
    var parentName by mutableStateOf("")
    var kidName by mutableStateOf("")
    /** The kid's nicknames (v0.21): any of them, or [kidName], starts a message for the kid and works in commands. */
    val kidNicknames = mutableStateListOf<String>()
    /** The emoji before the kid's bold name on WhatsApp (v0.22): "😎*Tito*: Oi, vó". Empty = none. */
    var kidEmoji by mutableStateOf("")
    /** The parent turned on "Bolha na tela" (v0.25). */
    var bubbleOn by mutableStateOf(false)
    /** The gentle nudge (v0.33): off until the parent turns it on in Ajustes e ajuda. */
    var nudgeOn by mutableStateOf(false)
    /** "Agora não" on a nudge: the contact id and when (v0.33). That person isn't nudged again for a week. */
    val nudgeDismissed = mutableStateMapOf<String, Long>()
    /**
     * Esqueci o PIN (v0.34): the WhatsApp the tablet was linked to before the parent asked for a new
     * PIN, kept until the same WhatsApp links again (the proof it's the parent). "" when no reset is running.
     */
    var pinResetFrom by mutableStateOf("")
    /** The kid has reacted once (v0.34): the "hold a message" hint goes. */
    var reactTipUsed by mutableStateOf(false)
    /** Family statuses (v0.36): off until the parent turns them on. */
    var statusesOn by mutableStateOf(false)
    /** When the kid's side was linked to the real WhatsApp: nothing from before it is shown (rule L6). */
    var linkedSince by mutableStateOf(0L)
    /** The family's live statuses. */
    val statuses = mutableStateListOf<app.oie.logic.Status>()

    /** A status from the link (v0.36). Kept only by the rules in Statuses.keep; returns whether it was. */
    fun receiveStatus(s: app.oie.logic.Status, now: Long = clock()): Boolean {
        val from = allowed.firstOrNull { it.id == s.contactId }
        if (!app.oie.logic.Statuses.keep(statusesOn, from, s.at, linkedSince, now) || statuses.any { it.id == s.id }) return false
        statuses += s
        return true
    }

    /** Its owner deleted it. Returns its media file, for the caller to delete. */
    fun removeStatus(id: String): String? {
        val i = statuses.indexOfFirst { it.id == id }
        return if (i < 0) null else statuses.removeAt(i).media
    }

    /** A day has passed (or statuses were turned off): they go. Returns their media files to delete. */
    fun expireStatuses(now: Long = clock()): List<String> {
        val gone = statuses.filter { !statusesOn || !app.oie.logic.Statuses.alive(it, now) || allowed.none { c -> c.id == it.contactId && !c.paused } }
        statuses.removeAll(gone.toSet())
        return gone.mapNotNull { it.media }
    }

    /** The kid saw it: marked here and on WhatsApp (the owner's choice: the poster sees the parent's account). */
    fun viewStatus(id: String) {
        val i = statuses.indexOfFirst { it.id == id }
        if (i < 0 || statuses[i].viewed) return
        statuses[i] = statuses[i].copy(viewed = true)
        wa.markStatusViewed(statuses[i])
    }

    /** The kid's reaction; the same one again takes it away. */
    fun reactToStatus(id: String, emoji: String) {
        val i = statuses.indexOfFirst { it.id == id }
        if (i < 0) return
        val send = if (statuses[i].reaction == emoji) "" else emoji
        statuses[i] = statuses[i].copy(reaction = send)
        wa.reactToStatus(statuses[i], send)
    }

    /**
     * The kid's answer to a status: a message in the poster's chat, quoting the status, shown in the kid's
     * chat with them too. Returns the message, or null when the status is gone.
     */
    fun replyToStatus(id: String, text: String, quote: String, now: Long = clock()): Message? {
        val s = statuses.firstOrNull { it.id == id } ?: return null
        if (text.isBlank()) return null
        val m = Message("k${System.nanoTime()}", s.contactId, true, now, Kind.TEXT, text.trim(), replyTo = s.id, sender = s.poster, statusQuote = quote)
        messages += m
        wa.send(m)
        return m
    }

    /** The reaction the kid used last (v0.34): shown first in the sheet. */
    var lastReaction by mutableStateOf<String?>(null)
    /** When the kid last sent "Preciso de você" (v0.33), for the 5-minute gap. */
    var needYouAt by mutableStateOf(0L)
    /** The kid's name first, then the nicknames. */
    fun kidNames(): List<String> = app.oie.logic.KidNames.all(kidName, kidNicknames)
    /** When this run of the app started: right after an update the link is still on its way (v0.24). */
    val startedAt: Long = clock()

    /** The link as the screens show it: connected, connecting (for 30 s), or down (LinkView, v0.24). */
    fun linkShown(now: Long = clock()): app.oie.logic.LinkView.Shown {
        if (!realMode) return if (wa.connected) app.oie.logic.LinkView.Shown.CONNECTED else app.oie.logic.LinkView.Shown.DOWN
        val s = linkState.status
        val trying = s == LinkState.Status.Off || s == LinkState.Status.Connecting || s == LinkState.Status.Disconnected
        return app.oie.logic.LinkView.of(wa.connected, linkState.on, trying, maxOf(linkState.downSince, startedAt), now)
    }
    /** The contacts on the kid's side: the allowed ones the parent hasn't paused from their WhatsApp (v0.21). */
    val reachable: List<Contact> get() = allowed.filter { !it.paused }
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

    /** The kid taps a reaction on [id] (v0.33): it shows at once and goes to WhatsApp; the same one again takes it away. */
    fun react(id: String, emoji: String) {
        val i = messages.indexOfFirst { it.id == id }
        if (i < 0) return
        reactTipUsed = true
        lastReaction = emoji
        val (send, after) = app.oie.logic.Reactions.kidTaps(messages[i].reactions, emoji)
        messages[i] = messages[i].copy(reactions = after)
        wa.react(messages[i], send)
    }

    /** A family member's reaction from WhatsApp (v0.33). Only on a message the kid already has. */
    fun reacted(targetId: String, chatId: String, who: String, emoji: String) {
        val i = messages.indexOfFirst { it.id == targetId && it.chatId == chatId }
        if (i >= 0) messages[i] = messages[i].copy(reactions = app.oie.logic.Reactions.set(messages[i].reactions, who, emoji))
    }

    /** Receipts from WhatsApp (v0.33): the kid's messages move to delivered or read, never back. */
    fun heard(ids: Collection<String>, r: app.oie.logic.Receipt) {
        for (i in messages.indices) {
            val m = messages[i]
            if (m.fromKid && m.id in ids) app.oie.logic.Receipts.raise(m.receipt, r).let { if (it != m.receipt) messages[i] = m.copy(receipt = it) }
        }
    }

    /**
     * "Preciso de você" (v0.33): "Preciso de você." to every marked person, as the kid's own message, so
     * it shows in each chat. Returns false while the 5-minute gap runs, or with nobody marked. It goes
     * in quiet hours too: the parent chose these people for this.
     */
    fun sendNeedYou(text: String, now: Long = clock()): Boolean {
        val to = app.oie.logic.NeedYou.to(allowed)
        if (to.isEmpty() || app.oie.logic.NeedYou.waitMs(needYouAt, now) > 0) return false
        needYouAt = now
        to.forEachIndexed { n, c ->
            val m = Message("n${System.nanoTime()}$n", c.id, true, now, Kind.TEXT, text)
            messages += m
            wa.send(m)
        }
        return true
    }

    /** Messages the kid sent that didn't go out (rule L4: one retry, then the kid is told). */
    val failed = mutableStateListOf<String>()

    /** Turns the kid's side to the real WhatsApp: the sample chats and contacts go; the parent picks real ones. */
    fun goReal() {
        realMode = true
        linkedSince = clock()
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
        return history.filter { MessageRule.isForKid(it, chat, history, kidNames()) }
    }

    fun unread(contactId: String): Int = visible(contactId).count { !it.fromKid && it.at > lastSeen.getOrDefault(contactId, 0L) }
    val lastSeen = mutableStateMapOf<String, Long>()

    /** Messages that arrived quietly during quiet hours and haven't been summed up yet. */
    var quietCount by mutableStateOf(0)

    /** Every message from the WhatsApp link comes in here (logic/Inbox.kt decides). Returns what happened. */
    fun receive(m: Message, now: LocalDateTime = LocalDateTime.now()): app.oie.logic.Inbox.Decision {
        val history = messages.filter { it.chatId == m.chatId }
        val d = app.oie.logic.Inbox.decide(m, allowed, history, kidNames(), periods, now)
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
