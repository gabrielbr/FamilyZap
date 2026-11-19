package app.oie.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import app.oie.logic.Contact
import app.oie.logic.Kind
import java.io.File
import java.util.concurrent.Executors
import org.json.JSONArray
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

    override val connected: Boolean get() = ls.status == LinkState.Status.Connected
    override val profileName: String? = null
    override fun contacts(): List<Contact> = ls.available.toList()

    override fun send(message: app.oie.logic.Message) {
        ours += message.id
        io.execute { deliver(message, retried = false) }
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
        ls.status = LinkState.Status.Connecting
        io.execute {
            try {
                val l = link ?: oielink.Oielink.open(db.path, this).also { link = it }
                // Read-only for the rule test; sending only once the parent turned the kid's side to the real WhatsApp.
                l.setReadOnly(!state.realMode)
                l.connect()
            } catch (e: Throwable) {
                // Includes a tablet the library wasn't built for (an x86 emulator).
                main.post { ls.status = LinkState.Status.Unavailable }
            }
        }
    }

    /** The parent starts the test or links for real: the link listens from now on, in the background too (LinkService). */
    fun turnOn() { ls.on = true; LinkService.start(context) }

    /** The parent turns the kid's side to the real WhatsApp (Conexão or setup): sending is allowed from now on. */
    fun goReal() {
        state.goReal()
        io.execute { runCatching { link?.setReadOnly(false) } }
    }

    /** After a network drop: connect again, unless the parent stopped the test or WhatsApp banned the number. */
    fun reconnectIfDropped() {
        main.post { if (ls.on && (ls.status == LinkState.Status.Disconnected || ls.status == LinkState.Status.Off)) start() }
    }

    /** Looks a phone number up on WhatsApp, for "Adicionar pelo número". Calls back on the main thread with the chat id, "" if it isn't on WhatsApp, or null on an error. */
    fun lookUp(phone: String, done: (String?) -> Unit) {
        io.execute {
            val r = runCatching { link?.lookUp(phone) }.getOrNull()
            main.post { done(r) }
        }
    }

    fun stop() {
        io.execute { runCatching { link?.disconnect() } }
        if (ls.status !is LinkState.Status.Banned) ls.status = LinkState.Status.Off
    }

    /** Logs out of WhatsApp and deletes the session and the test from the tablet. */
    fun unlink() {
        io.execute {
            runCatching { link?.unlink() }
            link = null
            listOf(db, File(db.path + "-wal"), File(db.path + "-shm")).forEach { it.delete() }
        }
        ls.clear()
        LinkService.stop(context)
    }

    /** Reloads the parent's contacts (Contatos calls it when it opens; the link calls it as they arrive). */
    fun loadContacts() {
        io.execute {
            val json = runCatching { link?.contacts() }.getOrNull() ?: return@execute
            val list = JSONArray(json).let { a -> (0 until a.length()).map { a.getJSONObject(it).run { Contact(getString("id"), getString("name"), optBoolean("isGroup")) } } }
            main.post { ls.available.clear(); ls.available.addAll(list) }
        }
    }

    /** The kid's side: RealInbox first (allowed chats only, never the parent's own), then Inbox.decide. */
    private fun receiveForKid(o: JSONObject) {
        val kind = runCatching { Kind.valueOf(o.optString("kind")) }.getOrDefault(Kind.UNSUPPORTED)
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
        if (kept && (kind == Kind.PHOTO || kind == Kind.STICKER || kind == Kind.VOICE)) io.execute {
            val bytes = runCatching { link?.download(m.id) }.getOrNull() ?: return@execute
            val name = Photos.keepBytes(context, bytes)
            main.post {
                val i = state.messages.indexOfFirst { it.id == m.id }
                if (i >= 0) state.messages[i] = state.messages[i].copy(media = name) else Photos.forget(context, name)
            }
        }
    }

    private fun offerSticker(id: String, animated: Boolean, at: Long) {
        io.execute {
            val bytes = runCatching { link?.download(id) }.getOrNull() ?: return@execute
            val name = Photos.keepBytes(context, bytes)
            main.post { state.offerSticker(app.oie.logic.FamilySticker(id, name, animated, saved = false, at = at)).forEach { Photos.forget(context, it) } }
        }
    }


    override fun onEvent(kind: String, payload: String) {
        val o = runCatching { JSONObject(payload) }.getOrDefault(JSONObject())
        main.post {
            when (kind) {
                "qr" -> ls.status = LinkState.Status.Qr(o.optString("code"))
                "paired", "connected" -> {
                    ls.status = LinkState.Status.Connected; loadContacts()
                    // L4: what failed while the link was down goes once more, and only once.
                    generateSequence { retryOnConnect.poll() }.toList().forEach { m -> io.execute { deliver(m, retried = true) } }
                }
                "disconnected" -> if (ls.status == LinkState.Status.Connected) ls.status = LinkState.Status.Disconnected
                "replaced", "loggedOut" -> ls.status = LinkState.Status.LoggedOut
                "temporaryBan" -> { ls.status = LinkState.Status.Banned(((o.optLong("expiresInSeconds") + 3599) / 3600).toInt()); LinkService.stop(context) }
                "pairError" -> ls.status = LinkState.Status.Off
                "message" -> if (state.realMode) receiveForKid(o)
                "contactsChanged" -> loadContacts()
            }
        }
    }
}
