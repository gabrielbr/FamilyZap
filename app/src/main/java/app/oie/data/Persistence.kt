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
 * Saving on the tablet (product law: Oiê has no servers). The whole state is one JSON document,
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
        put("kidNicknames", JSONArray(s.kidNicknames.toList()))
        put("kidEmoji", s.kidEmoji)
        put("bubbleOn", s.bubbleOn)
        put("pinResetFrom", s.pinResetFrom).put("reactTipUsed", s.reactTipUsed).put("lastReaction", s.lastReaction ?: "")
        put("statusesOn", s.statusesOn).put("linkedSince", s.linkedSince)
        put("statuses", JSONArray(s.statuses.map {
            JSONObject().put("id", it.id).put("contact", it.contactId).put("poster", it.poster).put("at", it.at).put("kind", it.kind.name)
                .put("text", it.text).put("media", it.media ?: JSONObject.NULL).put("size", it.size).put("dur", it.durationSec)
                .put("viewed", it.viewed).put("reaction", it.reaction)
                .put("audience", JSONArray(it.audience)).put("posting", it.posting).put("failed", it.failed)
        }))
        put("postingOn", s.postingOn)
        put("nudgeOn", s.nudgeOn).put("nudgeDismissed", JSONObject(s.nudgeDismissed.toMap())).put("needYouAt", s.needYouAt)
        put("pinHash", s.pinHash ?: JSONObject.NULL)
        put("kidNoticeSeen", s.kidNoticeSeen)
        put("addRequested", s.addRequested).put("contactTipUsed", s.contactTipUsed)
        put("quietCount", s.quietCount)
        put("linked", s.link.connected)
        put("realMode", s.realMode)
        put("stickers", JSONArray(s.stickers.map {
            JSONObject().put("id", it.id).put("media", it.media).put("animated", it.animated).put("saved", it.saved).put("at", it.at)
        }))
        put("lastPhotoSource", s.lastPhotoSource)
        put("allowed", JSONArray(s.allowed.map {
            JSONObject().put("id", it.id).put("name", it.name).put("group", it.isGroup)
                .put("quietException", it.quietException).put("wholeGroup", it.showWholeGroup)
                .put("phone", it.phone).put("waName", it.waName).put("paused", it.paused)
                .put("photo", it.photo).put("photoId", it.photoId).put("photoAt", it.photoAt).put("parentLine", it.parentLine)
                .put("birthday", it.birthday).put("needYou", it.needYou)
        }))
        put("periods", JSONArray(s.periods.map {
            JSONObject().put("name", it.name).put("days", JSONArray(it.days.map { d -> d.value }))
                .put("start", it.start.toString()).put("end", it.end.toString()).put("on", it.on)
        }))
        put("messages", JSONArray(s.messages.map {
            JSONObject().put("id", it.id).put("chat", it.chatId).put("kid", it.fromKid).put("at", it.at)
                .put("kind", it.kind.name).put("text", it.text).put("replyTo", it.replyTo ?: JSONObject.NULL)
                .put("dur", it.durationSec).put("media", it.media ?: JSONObject.NULL).put("size", it.size)
                .put("sender", it.sender).put("receipt", it.receipt.name).put("reactions", JSONObject(it.reactions)).put("statusQuote", it.statusQuote)
        }))
        put("drafts", JSONObject(s.drafts.toMap()))
        put("lastSeen", JSONObject(s.lastSeen.toMap()))
        put("ruleTest", JSONObject().put("on", s.linkState.on)) // "ruleTest" kept as the key so older saves still read
    }.toString()

    /** Restores into [s]. Unknown or newer versions are ignored rather than half-read. */
    fun decode(json: String, s: AppState) {
        val o = JSONObject(json)
        if (o.optInt("v") != VERSION) return
        s.parentName = o.optString("parentName")
        s.kidName = o.optString("kidName")
        s.kidEmoji = app.oie.logic.Signature.emojiOf(o.optString("kidEmoji"))
        s.bubbleOn = o.optBoolean("bubbleOn")
        s.nudgeOn = o.optBoolean("nudgeOn")
        s.statusesOn = o.optBoolean("statusesOn")
        s.postingOn = o.optBoolean("postingOn")
        s.linkedSince = o.optLong("linkedSince")
        s.statuses.clear()
        o.optJSONArray("statuses")?.let { a ->
            for (i in 0 until a.length()) a.getJSONObject(i).run {
                s.statuses += app.oie.logic.Status(getString("id"), getString("contact"), optString("poster"), getLong("at"),
                    runCatching { Kind.valueOf(getString("kind")) }.getOrDefault(Kind.UNSUPPORTED), optString("text"),
                    if (isNull("media")) null else getString("media"), optLong("size"), optInt("dur"), optBoolean("viewed"), optString("reaction"),
                    optJSONArray("audience")?.let { l -> List(l.length()) { k -> l.getString(k) } } ?: emptyList(),
                    // A post the app was closed in the middle of is shown as not posted, with Tentar de novo.
                    posting = false, failed = optBoolean("failed") || optBoolean("posting"))
            }
        }
        s.pinResetFrom = o.optString("pinResetFrom")
        s.reactTipUsed = o.optBoolean("reactTipUsed")
        s.lastReaction = o.optString("lastReaction").ifEmpty { null }
        s.nudgeDismissed.clear(); o.optJSONObject("nudgeDismissed")?.let { d -> d.keys().forEach { k -> s.nudgeDismissed[k] = d.getLong(k) } }
        s.needYouAt = o.optLong("needYouAt")
        s.kidNicknames.clear(); o.optJSONArray("kidNicknames")?.let { a -> for (i in 0 until a.length()) s.kidNicknames += a.getString(i) }
        s.pinHash = if (o.isNull("pinHash")) null else o.getString("pinHash")
        s.kidNoticeSeen = o.optBoolean("kidNoticeSeen")
        s.addRequested = o.optBoolean("addRequested")
        s.contactTipUsed = o.optBoolean("contactTipUsed")
        s.quietCount = o.optInt("quietCount")
        s.lastPhotoSource = o.optString("lastPhotoSource", "camera")
        if (o.optBoolean("linked")) s.link.pretendLinked()
        s.realMode = o.optBoolean("realMode")
        s.stickers.clear()
        o.optJSONArray("stickers")?.let { a ->
            for (i in 0 until a.length()) a.getJSONObject(i).run {
                s.stickers += app.oie.logic.FamilySticker(getString("id"), getString("media"), optBoolean("animated"), optBoolean("saved"), optLong("at"))
            }
        }
        s.allowed.clear()
        o.optJSONArray("allowed")?.let { a ->
            for (i in 0 until a.length()) a.getJSONObject(i).run {
                s.allowed += Contact(getString("id"), getString("name"), optBoolean("group"), optBoolean("quietException"), optBoolean("wholeGroup"), optString("phone"), optString("waName"), optBoolean("paused"),
                    optString("photo"), optString("photoId"), optLong("photoAt"), optBoolean("parentLine"), optString("birthday"), optBoolean("needYou"))
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
                    if (isNull("media")) null else getString("media"), optLong("size"), optString("sender"),
                    runCatching { app.oie.logic.Receipt.valueOf(optString("receipt")) }.getOrDefault(app.oie.logic.Receipt.NONE),
                    optJSONObject("reactions")?.let { r -> r.keys().asSequence().associateWith { k -> r.getString(k) } } ?: emptyMap(),
                    optString("statusQuote"),
                )
            }
        }
        s.drafts.clear(); o.optJSONObject("drafts")?.let { d -> d.keys().forEach { k -> s.drafts[k] = d.getString(k) } }
        s.lastSeen.clear(); o.optJSONObject("lastSeen")?.let { d -> d.keys().forEach { k -> s.lastSeen[k] = d.getLong(k) } }
        s.linkState.clear()
        s.linkState.on = o.optJSONObject("ruleTest")?.optBoolean("on") ?: false
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
