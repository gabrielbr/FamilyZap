package app.oie.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.ui.common.Bar
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/**
 * Esqueci o PIN (docs/flows/parent-setup-link.md, v0.34). A new PIN only after the parent links the
 * same WhatsApp again: that's the proof, so nobody holding the tablet (the kid included) can make one.
 * Stage 0 explains, then shows the QR; stage 1 is the new PIN. If another WhatsApp links, Oiê starts
 * over (names, PIN, contacts), since the old contacts belong to the old account.
 */
@Composable
fun ForgotPin(state: AppState, stage: Int) {
    val c = OieTheme.colors
    val tx = str
    val real = LocalRealLink.current
    val kid = state.kidName.ifBlank { tx.theKid }
    Column(Modifier.fillMaxSize()) {
        Bar {
            QuietButton(null, { state.route = Route.ParentPin }, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.forgotTitle, style = OieType.title, color = c.ink)
        }
        Box(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space8),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
                if (stage >= 1) {
                    app.oie.ui.setup.CreatePinStep(state) { state.pinResetFrom = ""; state.route = Route.Parent(0) }
                    return@Column
                }
                Text(tx.forgotBody(kid), style = OieType.body, color = c.ink)
                if (state.pinResetFrom.isEmpty()) {
                    app.oie.ui.setup.ButtonRow {
                        PrimaryButton(tx.forgotShowCode, { real?.relinkForPin() })
                        QuietButton(tx.cancel, { state.route = Route.ParentPin })
                    }
                } else {
                    Pairing(state) {
                        val before = state.pinResetFrom
                        real?.ownChat { now ->
                            if (now.isNotEmpty() && now == before) state.route = Route.ForgotPin(1)
                            else {
                                // Another WhatsApp: its owner sets Oiê up from the start; nothing of the old account stays.
                                state.pinResetFrom = ""
                                real.goReal()
                                state.route = Route.Setup(3)
                            }
                        }
                    }
                }
            }
        }
    }
}
