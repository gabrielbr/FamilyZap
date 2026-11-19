package app.oie.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.oie.R
import app.oie.ui.text.Strings

/**
 * Oiê in an Android bubble (docs/flows/bubble.md, v0.25): one silent conversation notification with
 * bubble metadata, whose window is [app.oie.BubbleActivity]. Android draws and moves the bubble;
 * the window uses Oiê's phone-sized layout. Needs Android 11+ and the parent's permission in Android.
 */
object Bubble {
    private const val CHANNEL = "bubble"
    private const val ID = 9
    private const val SHORTCUT = "oie-bubble"

    fun supported(): Boolean = Build.VERSION.SDK_INT >= app.oie.logic.BubblePolicy.MIN_SDK

    fun notificationsAllowed(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Whether Android lets Oiê's conversation bubble (all, or this one selected). */
    fun allowed(context: Context): Boolean {
        if (!supported()) return false
        val nm = context.getSystemService(NotificationManager::class.java)
        return if (Build.VERSION.SDK_INT >= 31) nm.bubblePreference != NotificationManager.BUBBLE_PREFERENCE_NONE
        else @Suppress("DEPRECATION") nm.areBubblesAllowed()
    }

    /** Android's own screen for Oiê's bubbles (or its notifications, when those are off). */
    fun settingsIntent(context: Context, notifications: Boolean): Intent =
        Intent(if (!notifications || Build.VERSION.SDK_INT < 29) android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS else android.provider.Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Puts the bubble on screen (again). Quiet: no sound, and its notification hides behind the bubble. */
    @android.annotation.SuppressLint("MissingPermission") // areNotificationsEnabled is checked first
    fun show(context: Context, tx: Strings) {
        if (!supported() || !notificationsAllowed(context)) return
        runCatching {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CHANNEL, tx.bubbleChannel, NotificationManager.IMPORTANCE_DEFAULT).apply {
                setAllowBubbles(true); setSound(null, null); enableVibration(false)
            })
            val icon = IconCompat.createWithResource(context, R.mipmap.ic_launcher)
            val oie = Person.Builder().setName("Oiê").setIcon(icon).setImportant(true).build()
            ShortcutManagerCompat.pushDynamicShortcut(context, ShortcutInfoCompat.Builder(context, SHORTCUT)
                .setLongLived(true).setShortLabel("Oiê").setIcon(icon).setPerson(oie)
                .setIntent(Intent(context, app.oie.MainActivity::class.java).setAction(Intent.ACTION_VIEW)).build())
            // Bubbles need a mutable PendingIntent (Android fills in the window's details).
            val open = PendingIntent.getActivity(context, 0, Intent(context, app.oie.BubbleActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            val meta = NotificationCompat.BubbleMetadata.Builder(open, icon)
                // The window's height: the column's max, a phone-sized window.
                .setDesiredHeight(app.oie.ui.theme.OTokens.Size.contentMax.value.toInt()).setSuppressNotification(true).build()
            val n = NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Oiê").setContentText(tx.bubbleText)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).setShortcutId(SHORTCUT).setSilent(true)
                .setStyle(NotificationCompat.MessagingStyle(oie).addMessage(tx.bubbleText, System.currentTimeMillis(), oie))
                .setBubbleMetadata(meta).build()
            NotificationManagerCompat.from(context).notify(ID, n)
        }.onFailure { Diagnostics.log("bubble: couldn't show (${it.javaClass.simpleName})") }
    }

    fun hide(context: Context) { NotificationManagerCompat.from(context).cancel(ID) }
}
