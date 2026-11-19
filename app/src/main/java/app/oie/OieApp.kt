package app.oie

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.snapshots.Snapshot
import app.oie.data.AppState
import app.oie.data.Codec
import app.oie.data.KeystoreSealer
import app.oie.data.RealLink
import app.oie.data.Store
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The app's one state and one WhatsApp link, owned by the process rather than the screen, so the
 * link can keep listening with the screen off (LinkService, docs/whatsapp-link.md, v0.7). Saved on
 * the tablet only, sealed with a Keystore key (data/Persistence.kt), whenever it changes.
 */
class OieApp : Application() {
    lateinit var state: AppState
        private set
    lateinit var realLink: RealLink
        private set

    override fun onCreate() {
        super.onCreate()
        val store = Store(File(filesDir, "state.sealed"), KeystoreSealer())
        state = AppState().also { s -> store.read()?.let { Codec.decode(it, s) } }
        // v0.12: no more sample. A tablet still on the sample drops its sample chats and contacts,
        // and the parent links the real WhatsApp from Conexão.
        if (!state.realMode) state.goReal()
        realLink = RealLink(this, state)
        state.real = realLink
        val tx = if (resources.configuration.locales[0].language == "en") app.oie.ui.text.En else app.oie.ui.text.Pt
        realLink.notifier = app.oie.data.Notifier(this, tx)
        // Changes made with no screen open (a message arriving through LinkService) still reach
        // observers: tell the snapshot system about global writes, as Compose does when a screen is up.
        val main = Handler(Looper.getMainLooper())
        var pending = false
        Snapshot.registerGlobalWriteObserver {
            if (!pending) { pending = true; main.post { pending = false; Snapshot.sendApplyNotifications() } }
        }
        MainScope().launch {
            androidx.compose.runtime.snapshotFlow { Codec.encode(state) }.distinctUntilChanged().collectLatest { json ->
                // A failed write (a full disk) keeps the last good copy; the next change tries again.
                withContext(Dispatchers.IO) { runCatching { store.write(json) } }
            }
        }
    }
}
