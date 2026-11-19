package app.oie.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import app.oie.logic.Contact
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.logic.QuietPeriod
import java.io.File
import java.security.KeyStore
import java.time.DayOfWeek
import java.time.LocalTime
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONArray
import org.json.JSONObject

/**
 * Saving on the tablet (product law: nothing on our servers). The whole state is one JSON document,
 * sealed with AES-GCM under a key that never leaves the Android Keystore, written atomically to the
 * app's private files. The parent area is never restored open: it always asks for the PIN again.
 */
object Codec {
    const val VERSION = 1

    fun encode(s: AppState): String = JSONObject().apply {
        put("v", VERSION)
        put("step", (s.route as? Route.Setup)?.step ?: -1)
        put("parentName", s.parentName)
        put("kidName", s.kidName)
        put("pinHash", s.pinHash ?: JSONObject.NULL)
        put("kidNoticeSeen", s.kidNoticeSeen)
        put("addRequested", s.addRequested)
        put("quietCount", s.quietCount)
        put("linked", s.link.connected)
        put("realMode", s.realMode)
        put("lastPhotoSource", s.lastPhotoSource)
        put("allowed", JSONArray(s.allowed.map {
            JSONObject().put("id", it.id).put("name", it.name).put("group", it.isGroup)
                .put("quietException", it.quietException).put("wholeGroup", it.showWholeGroup)
        }))
        put("periods", JSONArray(s.periods.map {
            JSONObject().put("name", it.name).put("days", JSONArray(it.days.map { d -> d.value }))
                .put("start", it.start.toString()).put("end", it.end.toString()).put("on", it.on)
        }))
        put("messages", JSONArray(s.messages.map {
            JSONObject().put("id", it.id).put("chat", it.chatId).put("kid", it.fromKid).put("at", it.at)
                .put("kind", it.kind.name).put("text", it.text).put("replyTo", it.replyTo ?: JSONObject.NULL)
                .put("dur", it.durationSec).put("media", it.media ?: JSONObject.NULL)
        }))
        put("drafts", JSONObject(s.drafts.toMap()))
        put("lastSeen", JSONObject(s.lastSeen.toMap()))
        put("ruleTest", s.linkTest.let { t ->
            JSONObject().put("on", t.on).put("ignored", t.ignored)
                .put("chats", JSONArray(t.chats.map { JSONObject().put("id", it.id).put("name", it.name).put("group", it.isGroup) }))
                .put("kidSpoke", JSONObject(t.kidSpoke.toMap()))
                .put("entries", JSONArray(t.entries.map {
                    JSONObject().put("chat", it.chatId).put("name", it.chatName).put("at", it.at).put("kind", it.kind.name)
                        .put("shown", it.shown).put("why", it.why.name).put("text", it.text ?: JSONObject.NULL)
                        .put("id", it.id).put("dur", it.durationSec).put("media", it.media ?: JSONObject.NULL)
                }))
        })
    }.toString()

    /** Restores into [s]. Unknown or newer versions are ignored rather than half-read. */
    fun decode(json: String, s: AppState) {
        val o = JSONObject(json)
        if (o.optInt("v") != VERSION) return
        s.parentName = o.optString("parentName")
        s.kidName = o.optString("kidName")
        s.pinHash = if (o.isNull("pinHash")) null else o.getString("pinHash")
        s.kidNoticeSeen = o.optBoolean("kidNoticeSeen")
        s.addRequested = o.optBoolean("addRequested")
        s.quietCount = o.optInt("quietCount")
        s.lastPhotoSource = o.optString("lastPhotoSource", "camera")
        if (o.optBoolean("linked")) s.link.pretendLinked()
        s.realMode = o.optBoolean("realMode")
        s.allowed.clear()
        o.optJSONArray("allowed")?.let { a ->
            for (i in 0 until a.length()) a.getJSONObject(i).run {
                s.allowed += Contact(getString("id"), getString("name"), optBoolean("group"), optBoolean("quietException"), optBoolean("wholeGroup"))
            }
        }
        o.optJSONArray("periods")?.let { a ->
            s.periods.clear()
            for (i in 0 until a.length()) a.getJSONObject(i).run {
                val days = getJSONArray("days").let { d -> (0 until d.length()).map { DayOfWeek.of(d.getInt(it)) }.toSet() }
                s.periods += QuietPeriod(getString("name"), days, LocalTime.parse(getString("start")), LocalTime.parse(getString("end")), optBoolean("on"))
            }
        }
        s.messages.clear()
        o.optJSONArray("messages")?.let { a ->
            for (i in 0 until a.length()) a.getJSONObject(i).run {
                s.messages += Message(
                    getString("id"), getString("chat"), getBoolean("kid"), getLong("at"), Kind.valueOf(getString("kind")),
                    optString("text"), if (isNull("replyTo")) null else getString("replyTo"), optInt("dur"),
                    if (isNull("media")) null else getString("media"),
                )
            }
        }
        s.drafts.clear(); o.optJSONObject("drafts")?.let { d -> d.keys().forEach { k -> s.drafts[k] = d.getString(k) } }
        s.lastSeen.clear(); o.optJSONObject("lastSeen")?.let { d -> d.keys().forEach { k -> s.lastSeen[k] = d.getLong(k) } }
        s.linkTest.clear()
        o.optJSONObject("ruleTest")?.let { r ->
            val t = s.linkTest
            t.on = r.optBoolean("on"); t.ignored = r.optInt("ignored")
            r.optJSONArray("chats")?.let { a -> for (i in 0 until a.length()) a.getJSONObject(i).run { t.chats += Contact(getString("id"), getString("name"), optBoolean("group")) } }
            r.optJSONObject("kidSpoke")?.let { d -> d.keys().forEach { k -> t.kidSpoke[k] = d.getLong(k) } }
            r.optJSONArray("entries")?.let { a ->
                for (i in 0 until a.length()) a.getJSONObject(i).run {
                    t.entries += app.oie.logic.RuleTest.Entry(
                        getString("chat"), getString("name"), getLong("at"), Kind.valueOf(getString("kind")), getBoolean("shown"),
                        app.oie.logic.RuleTest.Why.valueOf(getString("why")), if (isNull("text")) null else getString("text"),
                        optString("id"), optInt("dur"), if (isNull("media")) null else optString("media").ifEmpty { null },
                    )
                }
            }
        }
        val step = o.optInt("step", -1)
        s.route = if (step in 0..7) Route.Setup(step) else Route.KidHome
    }
}

/** Seals bytes before they touch the disk. */
interface Sealer {
    fun seal(plain: ByteArray): ByteArray
    fun open(sealed: ByteArray): ByteArray
}

/** AES-256-GCM with a key generated inside the Android Keystore; the key can't be exported. */
class KeystoreSealer(private val alias: String = "oie-state") : Sealer {
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256).build(),
            )
        }.generateKey()
    }

    override fun seal(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        return byteArrayOf(c.iv.size.toByte()) + c.iv + c.doFinal(plain)
    }

    override fun open(sealed: ByteArray): ByteArray {
        val n = sealed[0].toInt()
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, sealed, 1, n))
        }
        return c.doFinal(sealed, 1 + n, sealed.size - 1 - n)
    }
}

/** One sealed file in the app's private storage, written atomically (write to .tmp, then rename). */
class Store(private val file: File, private val sealer: Sealer) {
    fun read(): String? = try {
        if (file.exists()) String(sealer.open(file.readBytes()), Charsets.UTF_8) else null
    } catch (e: Exception) {
        null // a corrupt or unreadable file starts fresh rather than crashing the kid's app
    }

    fun write(json: String) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeBytes(sealer.seal(json.toByteArray(Charsets.UTF_8)))
        if (!tmp.renameTo(file)) { file.delete(); tmp.renameTo(file) }
    }
}
