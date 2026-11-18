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
    fun message(m: Message, from: String) {
        if (!allowed()) return
        val body = when (m.kind) {
            Kind.VOICE -> tx.notifVoice; Kind.PHOTO -> tx.notifPhoto; Kind.STICKER -> tx.notifSticker
            Kind.UNSUPPORTED -> tx.onlyInWhatsApp(from); else -> m.text
        }
        NotificationManagerCompat.from(context).notify(
            m.chatId.hashCode(),
            NotificationCompat.Builder(context, channel).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(from).setContentText(body).setAutoCancel(true).setContentIntent(open(m.chatId)).build(),
        )
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
