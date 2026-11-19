package app.oie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import app.oie.data.Codec
import app.oie.data.KeystoreSealer
import app.oie.data.Store
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.ui.kid.Conversation
import app.oie.ui.kid.KidHome
import app.oie.ui.parent.ParentArea
import app.oie.ui.parent.ParentPin
import app.oie.ui.setup.Setup
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme

object BuildConfigInfo { const val VERSION = "0.9.0" }

val LocalUpdater = androidx.compose.runtime.staticCompositionLocalOf<app.oie.update.Updater?> { null }
val LocalNotifier = androidx.compose.runtime.staticCompositionLocalOf<app.oie.data.Notifier?> { null }

class MainActivity : ComponentActivity() {
    private val updater by lazy { app.oie.update.Updater(applicationContext, this.lifecycleScope) }

    private val tx by lazy { if (resources.configuration.locales[0].language == "en") app.oie.ui.text.En else app.oie.ui.text.Pt }
    private val notifier by lazy { app.oie.data.Notifier(applicationContext, tx) }
    private var appState: AppState? = null
    /** The real WhatsApp link, used only by the rule test for now (docs/flows/rule-test.md). */
    private var realLink: app.oie.data.RealLink? = null

    /** Every return to the foreground: an update check (at most every 15 minutes), and the quiet-hours summary if they ended. */
    override fun onStart() {
        super.onStart()
        updater.onForeground()
        appState?.let { notifier.quietSummary(it.takeQuietSummary()) }
        // The link listens in the background while the test is on (LinkService); make sure it runs.
        if (appState?.linkTest?.on == true) app.oie.data.LinkService.start(this)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (intent.action == app.oie.update.Updater.ACTION_INSTALL_STATUS) app.oie.update.Updater.onInstallStatus(this, intent)
        // A tapped notification opens that family member's chat (kid side only; never into the parent area).
        if (intent.action == app.oie.data.Notifier.ACTION_OPEN_CHAT) appState?.let { s ->
            val id = intent.getStringExtra(app.oie.data.Notifier.EXTRA_CONTACT)
            if (s.route !is Route.Setup) s.route = if (id != null && s.allowed.any { it.id == id }) Route.Conversation(id) else Route.KidHome
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // The state and the link belong to the app process (OieApp), so the link can listen in the background.
            val oie = application as OieApp
            val state = remember { oie.state.also { appState = it } }
            val real = remember { oie.realLink.also { realLink = it } }
            OieTheme {
              androidx.compose.runtime.CompositionLocalProvider(app.oie.ui.text.LocalStrings provides app.oie.ui.text.stringsFor(), LocalUpdater provides updater, LocalNotifier provides notifier, app.oie.ui.parent.LocalRealLink provides real) {
                val c = OieTheme.colors
                // The 4dp frame round every screen (the core rule), in coral.
                Box(
                    Modifier.fillMaxSize().background(c.page).windowInsetsPadding(WindowInsets.safeDrawing)
                        .border(OTokens.Size.frame, c.frameLine),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        when (val r = state.route) {
                            is Route.Setup -> Setup(state, r.step, onClose = { finish() })
                            Route.KidHome -> KidHome(state)
                            is Route.Conversation -> Conversation(state, r.contactId)
                            Route.ParentPin -> ParentPin(state)
                            is Route.Parent -> ParentArea(state, r.section)
                            Route.RuleTest -> app.oie.ui.parent.RuleTestScreen(state)
                            Route.Pair -> app.oie.ui.parent.PairScreen(state)
                        }
                        // The update sheet shows only in the parent area: the kid isn't asked to update the app.
                        if (state.route is Route.Parent || state.route == Route.ParentPin || state.route == Route.RuleTest || state.route == Route.Pair) app.oie.update.UpdateSheet(updater)
                    }
                }
                // System Back always works (android.md): one step back, never trapped.
                BackHandler(enabled = state.route !is Route.KidHome && state.route != Route.Setup(0)) {
                    state.route = when (val r = state.route) {
                        is Route.Setup -> Route.Setup(r.step - 1)
                        Route.RuleTest, Route.Pair -> Route.Parent(2)
                        else -> Route.KidHome
                    }
                }
              }
            }
        }
    }
}
