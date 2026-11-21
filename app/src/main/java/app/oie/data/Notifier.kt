package app.oie.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.oie.MainActivity
import app.oie.R
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.ui.text.Strings

/**
 * Notifications on the kid's tablet (docs/flows/kid-receives-messages.md, step 1): Oiê's own channel,
 * one notification per family member (newer ones replace older), the kind said in words, and one
 * summary when quiet hours end. The parent can silence the channel in Android's settings.
 */
class Notifier(private val context: Context, private val tx: Strings) {
    private val channel = "family"

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channel, tx.notifChannel, NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    private fun allowed() = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun open(contactId: String?): PendingIntent = PendingIntent.getActivity(
        context, contactId?.hashCode() ?: 0,
        Intent(context, MainActivity::class.java).setAction(ACTION_OPEN_CHAT).putExtra(EXTRA_CONTACT, contactId)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    @android.annotation.SuppressLint("MissingPermission") // checked in allowed()
    fun message(m: Message, from: String) = message(m, app.oie.logic.Contact(m.chatId, from), bubble = false)

    /**
     * One notification per family member. [bubble] (v0.29, "Bolha na tela" on): it's also that chat's
     * bubble, which opens only that chat.
     */
    /** [who]: in a group, who wrote it (v0.46), so the line reads "Vovó: Domingo tem bolo" under the group's name. */
    fun message(m: Message, from: app.oie.logic.Contact, bubble: Boolean, who: String = "") {
        if (!allowed()) return
        val text = if (who.isNotEmpty()) "$who: ${body(m, from.name)}" else body(m, from.name)
        val b = NotificationCompat.Builder(context, channel).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(from.name).setContentText(text).setAutoCancel(true).setContentIntent(open(m.chatId))
        if (bubble) Bubble.decorate(context, b, from, text, m.at, silent = false)
        post(m.chatId.hashCode(), b)
    }

    /** The kid wrote to [to]: keep that chat's bubble on screen, silently (v0.29). */
    fun keepChatBubble(m: Message, to: app.oie.logic.Contact) {
        if (!allowed() || !Bubble.supported()) return
        val b = NotificationCompat.Builder(context, channel).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(to.name).setContentText(body(m, to.name)).setContentIntent(open(m.chatId))
        Bubble.decorate(context, b, to, body(m, to.name), m.at, silent = true)
        post(m.chatId.hashCode(), b)
    }

    private fun body(m: Message, from: String): String = when (m.kind) {
        Kind.VOICE -> tx.notifVoice; Kind.PHOTO -> tx.notifPhoto; Kind.STICKER -> tx.notifSticker
        Kind.VIDEO -> tx.notifVideo; Kind.GIF -> tx.notifGif
        Kind.UNSUPPORTED -> tx.onlyInWhatsApp(from); else -> m.text
    }

    @android.annotation.SuppressLint("MissingPermission") // checked in allowed()
    private fun post(id: Int, b: NotificationCompat.Builder) {
        runCatching { NotificationManagerCompat.from(context).notify(id, b.build()) }
            .onFailure { Diagnostics.log("notify: failed (${it.javaClass.simpleName})") }
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun quietSummary(count: Int) {
        if (!allowed() || count <= 0) return
        NotificationManagerCompat.from(context).notify(
            SUMMARY_ID,
            NotificationCompat.Builder(context, channel).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(tx.quietSummary(count)).setAutoCancel(true).setContentIntent(open(null)).build(),
        )
    }

    companion object {
        const val ACTION_OPEN_CHAT = "app.oie.OPEN_CHAT"
        const val EXTRA_CONTACT = "contact"
        private const val SUMMARY_ID = 1
    }
}
