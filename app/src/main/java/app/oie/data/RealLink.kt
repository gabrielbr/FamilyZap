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
 *  - the rule test (docs/flows/rule-test.md), read-only;
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

    override val connected: Boolean get() = test.status == LinkTest.Status.Connected
    override val profileName: String? = null
    override fun contacts(): List<Contact> = test.available.toList()

    override fun send(message: app.oie.logic.Message) {
        ours += message.id
        io.execute { deliver(message, retried = false) }
    }

    private fun deliver(m: app.oie.logic.Message, retried: Boolean) {
        val wait = app.oie.logic.SendPace.waitMs(sentAt, System.currentTimeMillis())
        if (wait > 0) Thread.sleep(wait)
        val l = link
        val err: Throwable? = if (l == null) IllegalStateException("not linked") else runCatching {
            when (m.kind) {
                Kind.TEXT -> l.sendTextWithID(m.chatId, m.text, m.replyTo ?: "", m.id)
                Kind.PHOTO -> l.sendImage(m.chatId, m.media?.let { Photos.openBytes(context, it) } ?: error("photo missing"), "", m.id)
                Kind.STICKER -> l.sendImage(m.chatId, Photos.stickerJpeg(context, app.oie.ui.kid.STICKERS[m.text] ?: error("sticker")), "", m.id)
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
    private val test get() = state.linkTest

    /** Connects (showing a QR the first time). Called when the parent starts the test and whenever the app opens while it's on. */
    fun start() {
        if (test.status is LinkTest.Status.Banned) return // L9: never retry around a ban
        test.status = LinkTest.Status.Connecting
        io.execute {
            try {
                val l = link ?: oielink.Oielink.open(db.path, this).also { link = it }
                // Read-only for the rule test; sending only once the parent turned the kid's side to the real WhatsApp.
                l.setReadOnly(!state.realMode)
                l.connect()
            } catch (e: Throwable) {
                // Includes a tablet the library wasn't built for (an x86 emulator).
                main.post { test.status = LinkTest.Status.Unavailable }
            }
        }
    }

    /** The parent starts the test or links for real: the link listens from now on, in the background too (LinkService). */
    fun turnOn() { test.on = true; LinkService.start(context) }

    /** The parent turns the kid's side to the real WhatsApp (Conexão or setup): sending is allowed from now on. */
    fun goReal() {
        state.goReal()
        io.execute { runCatching { link?.setReadOnly(false) } }
    }

    /** After a network drop: connect again, unless the parent stopped the test or WhatsApp banned the number. */
    fun reconnectIfDropped() {
        main.post { if (test.on && (test.status == LinkTest.Status.Disconnected || test.status == LinkTest.Status.Off)) start() }
    }

    fun stop() {
        io.execute { runCatching { link?.disconnect() } }
        if (test.status !is LinkTest.Status.Banned) test.status = LinkTest.Status.Off
    }

    /** Logs out of WhatsApp and deletes the session and the test from the tablet. */
    fun unlink() {
        io.execute {
            runCatching { link?.unlink() }
            link = null
            listOf(db, File(db.path + "-wal"), File(db.path + "-shm")).forEach { it.delete() }
        }
        test.entries.mapNotNull { it.media }.forEach { Photos.forget(context, it) }
        test.clear()
        LinkService.stop(context)
    }

    private fun loadContacts() {
        io.execute {
            val json = runCatching { link?.contacts() }.getOrNull() ?: return@execute
            val list = JSONArray(json).let { a -> (0 until a.length()).map { a.getJSONObject(it).run { Contact(getString("id"), getString("name"), optBoolean("isGroup")) } } }
            main.post { test.available.clear(); test.available.addAll(list) }
        }
    }

    /** The kid's side: RealInbox first (allowed chats only, never the parent's own), then Inbox.decide. */
    private fun receiveForKid(o: JSONObject) {
        val kind = runCatching { Kind.valueOf(o.optString("kind")) }.getOrDefault(Kind.UNSUPPORTED)
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

    private fun fetch(id: String) {
        io.execute {
            val bytes = runCatching { link?.download(id) }.getOrNull() ?: return@execute
            val name = Photos.keepBytes(context, bytes)
            main.post { test.attach(id, name) }
        }
    }

    override fun onEvent(kind: String, payload: String) {
        val o = runCatching { JSONObject(payload) }.getOrDefault(JSONObject())
        main.post {
            when (kind) {
                "qr" -> test.status = LinkTest.Status.Qr(o.optString("code"))
                "paired", "connected" -> {
                    test.status = LinkTest.Status.Connected; loadContacts()
                    // L4: what failed while the link was down goes once more, and only once.
                    generateSequence { retryOnConnect.poll() }.toList().forEach { m -> io.execute { deliver(m, retried = true) } }
                }
                "disconnected" -> if (test.status == LinkTest.Status.Connected) test.status = LinkTest.Status.Disconnected
                "replaced", "loggedOut" -> test.status = LinkTest.Status.LoggedOut
                "temporaryBan" -> { test.status = LinkTest.Status.Banned(((o.optLong("expiresInSeconds") + 3599) / 3600).toInt()); LinkService.stop(context) }
                "pairError" -> test.status = LinkTest.Status.Off
                "message" -> {
                    val e = test.onIncoming(
                        o.optString("id"), o.optString("chat"), o.optString("chatAlt").ifEmpty { null }, o.optBoolean("fromMe"),
                        o.optLong("at"), runCatching { Kind.valueOf(o.optString("kind")) }.getOrDefault(Kind.UNSUPPORTED),
                        o.optString("text"), o.optString("replyTo").ifEmpty { null }, state.kidName, o.optInt("durationSec"),
                    ) { gone -> gone.media?.let { Photos.forget(context, it) } }
                    // A photo or sticker that would reach the kid is downloaded and sealed, as it would be in real use.
                    if (e != null && e.shown && (e.kind == Kind.PHOTO || e.kind == Kind.STICKER)) fetch(e.id)
                    if (state.realMode) receiveForKid(o)
                }
            }
        }
    }
}
