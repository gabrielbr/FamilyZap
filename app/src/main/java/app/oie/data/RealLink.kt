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
 * The real WhatsApp link (link/, whatsmeow), used today only by the rule test, always read-only:
 * it listens and sends nothing (docs/flows/rule-test.md). Everything it hears goes to [LinkTest] on the
 * main thread. The session lives in the app's private no-backup storage and is deleted on Unlink
 * (rule L8).
 */
class RealLink(private val context: Context, private val state: AppState) : oielink.Listener {
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
                l.setReadOnly(true)
                l.connect()
            } catch (e: Throwable) {
                // Includes a tablet the library wasn't built for (an x86 emulator).
                main.post { test.status = LinkTest.Status.Unavailable }
            }
        }
    }

    /** The parent starts the test: the link listens from now on, in the background too (LinkService). */
    fun turnOn() { test.on = true; LinkService.start(context) }

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
                "paired", "connected" -> { test.status = LinkTest.Status.Connected; loadContacts() }
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
                }
            }
        }
    }
}
