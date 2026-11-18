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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.ui.kid.Conversation
import app.oie.ui.kid.KidHome
import app.oie.ui.parent.ParentArea
import app.oie.ui.parent.ParentPin
import app.oie.ui.setup.Setup
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme

object BuildConfigInfo { const val VERSION = "0.1.0" }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state = remember { AppState() }
            OieTheme {
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
                        }
                    }
                }
                // System Back always works (android.md): one step back, never trapped.
                BackHandler(enabled = state.route !is Route.KidHome && state.route != Route.Setup(0)) {
                    state.route = when (val r = state.route) {
                        is Route.Setup -> Route.Setup(r.step - 1)
                        else -> Route.KidHome
                    }
                }
            }
        }
    }
}
