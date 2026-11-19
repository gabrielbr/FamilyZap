package app.oie.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import app.oie.MainActivity
import app.oie.OieApp
import app.oie.R
import app.oie.ui.text.En
import app.oie.ui.text.Pt

/**
 * Keeps the WhatsApp link listening when Oiê isn't on screen (v0.7). Android requires a visible,
 * quiet notification for this ("O Oiê está conectado."), on its own low channel the parent can hide.
 * When the network comes back after a drop, it reconnects (rule L5: whatsmeow's own retries are
 * bounded; this is the "app tries again when the network comes back" half). A ban stops it (L9).
 */
class LinkService : Service() {
    private val app get() = application as OieApp
    private var callback: ConnectivityManager.NetworkCallback? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!app.state.linkTest.on) { stopSelf(); return START_NOT_STICKY }
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, notification(),
            if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING else 0,
        )
        app.realLink.start()
        if (callback == null) {
            val cm = getSystemService(ConnectivityManager::class.java)
            callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) { app.realLink.reconnectIfDropped() }
            }.also { runCatching { cm.registerDefaultNetworkCallback(it) } }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        callback?.let { c -> runCatching { getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(c) } }
        callback = null
        app.realLink.stop()
        super.onDestroy()
    }

    private fun notification(): android.app.Notification {
        val tx = if (resources.configuration.locales[0].language == "en") En else Pt
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel(CHANNEL, tx.notifLinkChannel, NotificationManager.IMPORTANCE_MIN))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(tx.notifLinkOn).setOngoing(true).setSilent(true).setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_MIN).build()
    }

    companion object {
        private const val CHANNEL = "link"
        private const val NOTIFICATION_ID = 7

        /** Starts listening in the background. Call from the screen (Android allows it only from the foreground). */
        fun start(context: Context) {
            runCatching { ContextCompat.startForegroundService(context, Intent(context, LinkService::class.java)) }
        }

        fun stop(context: Context) { context.stopService(Intent(context, LinkService::class.java)) }
    }
}
