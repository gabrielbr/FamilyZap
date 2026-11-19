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
import androidx.compose.ui.graphics.toArgb
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

    // ---- v0.29: a bubble per active chat (docs/flows/bubble.md) ----

    /** The chat id a [app.oie.BubbleActivity] opens, when it's a chat's bubble. */
    const val EXTRA_CHAT = "app.oie.bubble.CHAT"

    /** The contact's face for the bubble: their picture, or their letter on the brand's blush. */
    private fun face(context: Context, c: app.oie.logic.Contact): IconCompat {
        val side = 192
        val photo = c.photo.takeIf { it.isNotEmpty() }?.let { Photos.load(context, it, side) }
        val out = android.graphics.Bitmap.createBitmap(side, side, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        val light = app.oie.ui.theme.OTokens.Light
        if (photo != null) {
            val shader = android.graphics.BitmapShader(photo, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
            val scale = side.toFloat() / minOf(photo.width, photo.height)
            shader.setLocalMatrix(android.graphics.Matrix().apply {
                setScale(scale, scale); postTranslate((side - photo.width * scale) / 2, (side - photo.height * scale) / 2)
            })
            paint.shader = shader
            canvas.drawCircle(side / 2f, side / 2f, side / 2f, paint)
        } else {
            paint.color = light.selected.toArgb()
            canvas.drawCircle(side / 2f, side / 2f, side / 2f, paint)
            paint.color = light.onSelected.toArgb(); paint.textAlign = android.graphics.Paint.Align.CENTER; paint.textSize = side * 0.45f
            runCatching { androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nunito_800) }.getOrNull()?.let { paint.typeface = it }
            val y = side / 2f - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(c.name.firstOrNull()?.uppercase() ?: "?", side / 2f, y, paint)
        }
        return IconCompat.createWithBitmap(out)
    }

    /**
     * Makes [builder] (a chat's notification) a conversation that can bubble: a long-lived shortcut for
     * the contact, a MessagingStyle with their name and face, and bubble metadata that opens only that
     * chat. [silent]: the kid's own send keeps the bubble without notifying.
     */
    fun decorate(context: Context, builder: NotificationCompat.Builder, c: app.oie.logic.Contact, text: String, at: Long, silent: Boolean) {
        if (!supported()) return
        val icon = face(context, c)
        val person = Person.Builder().setName(c.name).setIcon(icon).setKey(c.id).setImportant(true).build()
        val id = app.oie.logic.ChatBubbles.shortcutId(c.id)
        ShortcutManagerCompat.pushDynamicShortcut(context, ShortcutInfoCompat.Builder(context, id)
            .setLongLived(true).setShortLabel(c.name).setIcon(icon).setPerson(person)
            .setIntent(Intent(context, app.oie.MainActivity::class.java).setAction(Intent.ACTION_VIEW)).build())
        val open = PendingIntent.getActivity(
            context, id.hashCode(),
            Intent(context, app.oie.BubbleActivity::class.java).putExtra(EXTRA_CHAT, c.id)
                .setData(android.net.Uri.parse("oie://chat/" + android.net.Uri.encode(c.id))),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val meta = NotificationCompat.BubbleMetadata.Builder(open, icon)
            .setDesiredHeight(app.oie.ui.theme.OTokens.Size.contentMax.value.toInt()).setSuppressNotification(silent).build()
        builder.setShortcutId(id).setBubbleMetadata(meta).setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.MessagingStyle(person).addMessage(text, at, person))
        if (silent) builder.setSilent(true)
    }

    /** Removes the bubbles of chats that are no longer allowed, or are paused. */
    fun prune(context: Context, reachable: Set<String>) {
        if (!supported()) return
        runCatching {
            val nm = context.getSystemService(NotificationManager::class.java)
            val posted = nm.activeNotifications.toList()
            val stale = app.oie.logic.ChatBubbles.stale(posted.map { it.notification.shortcutId }, reachable)
            posted.filter { app.oie.logic.ChatBubbles.contactOf(it.notification.shortcutId) in stale }.forEach { nm.cancel(it.id) }
            if (stale.isNotEmpty()) ShortcutManagerCompat.removeLongLivedShortcuts(context, stale.map { app.oie.logic.ChatBubbles.shortcutId(it) })
        }
    }
}
