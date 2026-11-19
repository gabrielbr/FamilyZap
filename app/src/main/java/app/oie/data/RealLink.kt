package app.oie.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import app.oie.logic.Contact
import app.oie.logic.Kind
import java.io.File
import java.util.concurrent.Executors
import org.json.JSONObject

/**
 * The real WhatsApp link (link/, whatsmeow). It serves two things:
 *  - since v0.8, once the parent turns it on (AppState.realMode), the kid's side: it sends what the
 *    kid sends, at a person's pace (SendPace, rule L3) with one retry after a reconnect (L4), and
 *    hands every incoming message to RealInbox, then Inbox.decide (product law).
 * Everything it hears is handled on the main thread. The session lives in the app's private
 * no-backup storage and is deleted on Unlink (rule L8).
 */
class RealLink(private val context: Context, private val state: AppState) : oielink.Listener, WhatsAppLink {
    /** Notifications for the kid (set by the app). */
    var notifier: Notifier? = null
    /** Ids the tablet sent, so their echo from WhatsApp isn't shown twice. */
    private val ours = java.util.Collections.synchronizedSet(HashSet<String>())
    /** When sends went out (io thread only), for SendPace. */
    private val sentAt = ArrayList<Long>()
    /** Sends that failed while disconnected: retried once on the next "connected" (L4). */
    private val retryOnConnect = java.util.concurrent.ConcurrentLinkedQueue<app.oie.logic.Message>()

    /**
     * Runs link work off the main thread. An error there is written to the problem report and the
     * work stops; it never closes the app (v0.16: an uncaught error here crashed Oiê mid-link).
     */
    private fun bg(block: () -> Unit) = io.execute {
        try { block() } catch (e: Throwable) { Diagnostics.log("link: background error: ${e.javaClass.simpleName}: ${e.message}\n${e.stackTraceToString().take(1500)}") }
    }

    override val connected: Boolean get() = ls.status == LinkState.Status.Connected
    override val profileName: String? = null
    override fun contacts(): List<Contact> = ls.available.toList()

    override fun send(message: app.oie.logic.Message) {
        ours += message.id
        bg { deliver(message, retried = false) }
    }

    private fun signed(text: String): String = app.oie.logic.Signature.sign(state.kidName, text)

    private fun deliver(m: app.oie.logic.Message, retried: Boolean) {
        val wait = app.oie.logic.SendPace.waitMs(sentAt, System.currentTimeMillis())
        if (wait > 0) Thread.sleep(wait)
        val l = link
        val err: Throwable? = if (l == null) IllegalStateException("not linked") else runCatching {
            when (m.kind) {
                // The kid writes from the parent's number, so the family sees who it is: "Tito: Oi, vó" (setup promises this).
                Kind.TEXT -> l.sendTextWithID(m.chatId, signed(m.text), m.replyTo ?: "", m.id)
                Kind.PHOTO -> l.sendImage(m.chatId, m.media?.let { Photos.openBytes(context, it) } ?: error("photo missing"), signed(""), m.id)
                // A family sticker the kid saved goes as a real WhatsApp sticker; Oiê's own go as pictures.
                Kind.STICKER -> if (m.media != null) {
                    l.sendSticker(m.chatId, Photos.openBytes(context, m.media) ?: error("sticker missing"), state.stickers.any { it.media == m.media && it.animated }, m.id)
                } else {
                    l.sendImage(m.chatId, Photos.stickerJpeg(context, app.oie.ui.kid.STICKERS[m.text] ?: error("sticker")), signed(""), m.id)
                }
                Kind.VOICE -> {
                    val bytes = m.media?.let { Photos.openBytes(context, it) } ?: error("voice missing")
                    l.sendVoice(m.chatId, bytes, m.durationSec.toLong(), Voice.mimetype(bytes), m.id)
                }
                else -> error("not supported yet")
            }
            Unit
        }.exceptionOrNull()
        sentAt += System.currentTimeMillis()
        Diagnostics.log("send ${m.kind}${if (retried) " (retry)" else ""}: ${err?.let { "failed: ${it.message}" } ?: "ok"}")
        while (sentAt.size > app.oie.logic.SendPace.MAX_IN_WINDOW * 2) sentAt.removeAt(0)
        if (err == null) { main.post { state.failed.remove(m.id) }; return }
        val wasDown = !connected || l == null
        if (!retried && wasDown) retryOnConnect += m else main.post { if (m.id !in state.failed) state.failed += m.id }
    }
    private val db = File(context.noBackupFilesDir, "whatsapp-link.db")
    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()
    private var link: oielink.Link? = null
    private val ls get() = state.linkState

    /** Connects (showing a QR the first time). Called when the parent starts the test and whenever the app opens while it's on. */
    fun start() {
        if (ls.status is LinkState.Status.Banned) return // L9: never retry around a ban
        // v0.20: a QR on screen is a link in progress; coming back to the app (or turning the tablet) must not restart it.
        if (ls.status is LinkState.Status.Qr && link != null) return
        // v0.14: after WhatsApp logged the tablet out, the old connection is spent; start a fresh one (a new QR).
        val fresh = ls.status == LinkState.Status.LoggedOut
        val shown = ls.status == LinkState.Status.Connected
        // v0.20: keep "connected" on screen while checking; only a real reconnect shows "connecting".
        if (!shown) ls.status = LinkState.Status.Connecting
        bg {
            if (fresh) { runCatching { link?.disconnect() }; link = null; Diagnostics.log("link: starting fresh after a logout") }
            val l = try {
                link ?: oielink.Oielink.open(db.path, this).also { link = it }
            } catch (e: Throwable) {
                // The library can't run here (a tablet it wasn't built for, such as an x86 emulator).
                Diagnostics.log("link: can't open: ${e.javaClass.simpleName}: ${e.message}")
                main.post { ls.status = LinkState.Status.Unavailable }
                return@bg
            }
            // v0.20: an up connection is left alone. Asking it to connect again answered "already
            // connected", which read as a failure: the tablet said "disconnected" while WhatsApp showed it connected.
            when (app.oie.logic.LinkStart.decide(banned = false, connectedShown = shown, socketUp = !fresh && runCatching { l.isConnected }.getOrDefault(false))) {
                app.oie.logic.LinkStart.Do.NOTHING -> { runCatching { l.setReadOnly(!state.realMode) }; return@bg }
                app.oie.logic.LinkStart.Do.MARK_CONNECTED -> {
                    runCatching { l.setReadOnly(!state.realMode) }
                    Diagnostics.log("link: already connected")
                    main.post { if (ls.status == LinkState.Status.Connecting || ls.status == LinkState.Status.Disconnected) ls.status = LinkState.Status.Connected; loadContacts() }
                    return@bg
                }
                app.oie.logic.LinkStart.Do.CONNECT -> {}
            }
            try {
                l.setReadOnly(!state.realMode)
                l.connect()
            } catch (e: Throwable) {
                // No network, or WhatsApp refused: say disconnected, and LinkService tries again when the network returns.
                Diagnostics.log("link: connect failed: ${e.message}")
                main.post { ls.status = LinkState.Status.Disconnected }
            }
        }
    }

    /** The parent starts the test or links for real: the link listens from now on, in the background too (LinkService). */
    fun turnOn() { ls.on = true; LinkService.start(context) }

    /** The parent turns the kid's side to the real WhatsApp (Conexão or setup): sending is allowed from now on. */
    fun goReal() {
        state.goReal()
        bg { runCatching { link?.setReadOnly(false) } }
    }

    /** After a network drop: connect again, unless the parent stopped the test or WhatsApp banned the number. */
    fun reconnectIfDropped() {
        main.post { if (ls.on && (ls.status == LinkState.Status.Disconnected || ls.status == LinkState.Status.Off)) start() }
    }

    /** Looks a phone number up on WhatsApp, for "Adicionar pelo número". Calls back on the main thread with the chat id, "" if it isn't on WhatsApp, or null on an error. */
    fun lookUp(phone: String, done: (String?) -> Unit) {
        bg {
            val r = runCatching { link?.lookUp(phone) }.getOrNull()
            main.post { done(r) }
        }
    }

    fun stop() {
        bg { runCatching { link?.disconnect() } }
        if (ls.status !is LinkState.Status.Banned) ls.status = LinkState.Status.Off
    }

    /** Logs out of WhatsApp and deletes the session and the test from the tablet. */
    fun unlink() {
        bg {
            runCatching { link?.unlink() }
            link = null
            listOf(db, File(db.path + "-wal"), File(db.path + "-shm")).forEach { it.delete() }
        }
        ls.clear()
        LinkService.stop(context)
    }

    /** Reloads the parent's contacts (Contatos calls it when it opens; the link calls it as they arrive). */
    fun loadContacts() {
        bg {
            val list = app.oie.logic.ContactsJson.parse(runCatching { link?.contacts() }.getOrNull())
            main.post { ls.available.clear(); ls.available.addAll(list) }
        }
    }

    /** The kid's side: RealInbox first (allowed chats only, never the parent's own), then Inbox.decide. */
    private fun receiveForKid(o: JSONObject) {
        val kind = runCatching { Kind.valueOf(o.optString("kind")) }.getOrDefault(Kind.UNSUPPORTED)
        if (kind == Kind.TEXT && command(o)) return
        // v0.10: a sticker the parent sends to their own chat is offered to the kid. Only stickers:
        // nothing else from that chat is read into Oiê.
        if (o.optBoolean("selfChat")) {
            if (kind == Kind.STICKER) offerSticker(o.optString("id"), o.optBoolean("animated"), o.optLong("at"))
            return
        }
        val m = app.oie.logic.RealInbox.toMessage(
            o.optString("id"), o.optString("chat"), o.optString("chatAlt").ifEmpty { null }, o.optBoolean("fromMe"), ours,
            state.allowed, o.optLong("at"), kind, o.optString("text"), o.optString("replyTo").ifEmpty { null }, o.optInt("durationSec"),
        ) ?: return
        val d = state.receive(m)
        if (d == app.oie.logic.Inbox.Decision.NOTIFY) state.allowed.firstOrNull { it.id == m.chatId }?.let { notifier?.message(m, it.name) }
        val kept = d == app.oie.logic.Inbox.Decision.NOTIFY || d == app.oie.logic.Inbox.Decision.QUIET
        if (kept && (kind == Kind.PHOTO || kind == Kind.STICKER || kind == Kind.VOICE)) bg {
            val bytes = runCatching { link?.download(m.id) }.getOrNull() ?: return@bg
            val name = Photos.keepBytes(context, bytes)
            main.post {
                val i = state.messages.indexOfFirst { it.id == m.id }
                if (i >= 0) state.messages[i] = state.messages[i].copy(media = name) else Photos.forget(context, name)
            }
        }
    }

    /** Command ids already acted on, so a redelivery doesn't answer twice. */
    private val commandsDone = java.util.LinkedHashSet<String>()

    /**
     * A command from the parent's own WhatsApp ("/Tito parar", docs/flows/parent-commands.md, v0.21).
     * Only "from me": anyone else's "/Tito" is an ordinary message. Acts on the chat it was typed in;
     * the answer goes to the parent's own chat. Returns true when the message was a command (never
     * stored, never shown to the kid).
     */
    private fun command(o: JSONObject): Boolean {
        if (!o.optBoolean("fromMe")) return false
        val id = o.optString("id")
        if (id in ours) return false
        val p = app.oie.logic.ParentCommand.parse(o.optString("text"), state.kidNames()) ?: return false
        if (!commandsDone.add(id)) return true
        while (commandsDone.size > 100) commandsDone.remove(commandsDone.first())
        if (!app.oie.logic.ParentCommand.fresh(o.optLong("at"), state.clock())) { Diagnostics.log("command: too old, ignored"); return true }
        val chat = o.optString("chat"); val alt = o.optString("chatAlt").ifEmpty { null }
        val ids = listOfNotNull(chat, alt)
        // The chat as a new contact (for "adicionar"): from the WhatsApp list when it's there, else its number.
        val known = state.wa.contacts().firstOrNull { it.id in ids }
        val pn = ids.firstOrNull { it.endsWith("@s.whatsapp.net") }
        val candidate = known ?: app.oie.logic.Contact(
            pn ?: chat, app.oie.logic.ContactNames.display(pn?.substringBefore('@') ?: "").ifEmpty { chat.substringBefore('@') },
            isGroup = chat.endsWith("@g.us"),
        )
        val r = app.oie.logic.ParentCommand.apply(p.action, ids, o.optBoolean("selfChat"), candidate, state.allowed.toList())
        if (r.allowed != state.allowed.toList()) { state.allowed.clear(); state.allowed.addAll(r.allowed) }
        Diagnostics.log("command: ${p.action.name.lowercase()} → ${r.outcome.name.lowercase()}")
        val tx = if (context.resources.configuration.locales[0].language == "en") app.oie.ui.text.En else app.oie.ui.text.Pt
        val kid = state.kidName.ifBlank { p.name }
        val cmd = "/" + p.name
        val name = (r.contact ?: candidate).name
        val here = when {
            o.optBoolean("selfChat") -> null
            r.contact == null -> tx.cmdHereNotIn(kid)
            r.contact.paused -> tx.cmdHerePaused(name, kid)
            else -> tx.cmdHereReceives(name, kid)
        }
        val answer = when (r.outcome) {
            app.oie.logic.ParentCommand.Outcome.HELP -> tx.cmdHelp(kid, cmd, here)
            app.oie.logic.ParentCommand.Outcome.UNKNOWN -> tx.cmdUnknown(p.word, kid, cmd)
            app.oie.logic.ParentCommand.Outcome.PAUSED -> tx.cmdPaused(name, kid, cmd)
            app.oie.logic.ParentCommand.Outcome.RESUMED -> tx.cmdResumed(name, kid)
            app.oie.logic.ParentCommand.Outcome.ADDED -> tx.cmdAdded(name, kid, candidate.isGroup)
            app.oie.logic.ParentCommand.Outcome.ALREADY_IN -> tx.cmdAlready(name, kid)
            app.oie.logic.ParentCommand.Outcome.REMOVED -> tx.cmdRemoved(name, kid)
            app.oie.logic.ParentCommand.Outcome.NOT_IN_LIST -> tx.cmdNotIn(kid, cmd)
            app.oie.logic.ParentCommand.Outcome.OWN_CHAT -> tx.cmdOwnChat(kid, cmd)
        }
        noteToSelf(answer)
        return true
    }

    /** Oiê's answer in the parent's own chat, at a person's pace like every send (rules L2, L3). */
    private fun noteToSelf(text: String) {
        bg {
            val wait = app.oie.logic.SendPace.waitMs(sentAt, System.currentTimeMillis())
            if (wait > 0) Thread.sleep(wait)
            val r = runCatching { link?.sendSelfNote(text) }
            r.getOrNull()?.let { ours += it }
            sentAt += System.currentTimeMillis()
            Diagnostics.log("send answer: ${r.exceptionOrNull()?.let { "failed: ${it.message}" } ?: "ok"}")
        }
    }

    private fun offerSticker(id: String, animated: Boolean, at: Long) {
        bg {
            val bytes = runCatching { link?.download(id) }.getOrNull() ?: return@bg
            val name = Photos.keepBytes(context, bytes)
            main.post { state.offerSticker(app.oie.logic.FamilySticker(id, name, animated, saved = false, at = at)).forEach { Photos.forget(context, it) } }
        }
    }


    override fun onEvent(kind: String, payload: String) {
        val o = runCatching { JSONObject(payload) }.getOrDefault(JSONObject())
        // The problem report: the library's warnings, errors and panics, and every change of the link's state.
        when (kind) {
            "log" -> { Diagnostics.log("wa ${o.optString("level")}: ${o.optString("msg")}"); return }
            "message", "qr", "contactsChanged" -> {}
            else -> Diagnostics.log("link: $kind ${o.toString().takeIf { it != "{}" } ?: ""}")
        }
        main.post {
            when (kind) {
                "qr" -> ls.status = LinkState.Status.Qr(o.optString("code"))
                "paired", "connected" -> {
                    // A fresh pairing starts the contact list from nothing (v0.17).
                    if (kind == "paired") { ls.contactsSynced = false; ls.linkedAt = state.clock() }
                    else if (ls.linkedAt == 0L) ls.linkedAt = state.clock()
                    ls.status = LinkState.Status.Connected; loadContacts()
                    // L4: what failed while the link was down goes once more, and only once.
                    generateSequence { retryOnConnect.poll() }.toList().forEach { m -> bg { deliver(m, retried = true) } }
                }
                "disconnected" -> if (ls.status == LinkState.Status.Connected) ls.status = LinkState.Status.Disconnected
                "replaced", "loggedOut" -> ls.status = LinkState.Status.LoggedOut
                "temporaryBan" -> { ls.status = LinkState.Status.Banned(((o.optLong("expiresInSeconds") + 3599) / 3600).toInt()); LinkService.stop(context) }
                "pairError" -> ls.status = LinkState.Status.Off
                "message" -> if (state.realMode) receiveForKid(o)
                "contactsChanged" -> loadContacts()
                "contactsSynced" -> { ls.contactsSynced = true; loadContacts() }
            }
        }
    }
}
