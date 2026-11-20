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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    // v0.34 audit round 2 (P1): only a QR shown on this screen, this time, counts as the proof. A link left
    // over from an earlier visit (the new-PIN step left with Back) never jumps to a new PIN by itself.
    var sawQr by remember { mutableStateOf(false) }
    val status = state.linkState.status
    LaunchedEffect(status) { if (status is app.oie.data.LinkState.Status.Qr) sawQr = true }
    var why by remember { mutableStateOf(false) }
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
                // Help where it's needed (Nielsen 10): the risk, one tap away.
                app.oie.ui.common.TextLink(tx.whatCanHappen, { why = !why })
                if (why) Text(tx.riskBody, style = OieType.body, color = c.ink)
                val stale = status == app.oie.data.LinkState.Status.Connected && !sawQr
                // Round 3: while the tablet is getting ready (after the tap), the wait shows, not the button again.
                if (state.pinResetFrom.isEmpty() && status == app.oie.data.LinkState.Status.Connecting) {
                    Text(tx.linkPreparing, style = OieType.body, color = c.ink)
                } else if (state.pinResetFrom.isEmpty() || stale) {
                    if (state.linkState.pairFailed) Text(tx.linkFailed, style = OieType.body, color = c.coralText)
                    app.oie.ui.setup.ButtonRow {
                        PrimaryButton(tx.forgotShowCode, { sawQr = false; real?.relinkForPin() })
                        QuietButton(tx.cancel, { state.route = Route.ParentPin })
                    }
                } else {
                    Pairing(state, back = { state.route = Route.ParentPin }) {
                        if (!sawQr) return@Pairing
                        val before = state.pinResetFrom
                        real?.ownChat { now ->
                            if (now.isNotEmpty() && now == before) state.route = Route.ForgotPin(1)
                            // Round 3: not knowing which WhatsApp linked is a failed try, never "another account".
                            else if (now.isEmpty()) state.linkState.pairFailed = true
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
