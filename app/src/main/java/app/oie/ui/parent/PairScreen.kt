package app.oie.ui.parent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import app.oie.R
import app.oie.data.AppState
import app.oie.data.LinkState
import app.oie.data.Route
import app.oie.ui.common.Bar
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/** The real link, when the app runs on a device; null in screenshot tests. */
val LocalRealLink = androidx.compose.runtime.staticCompositionLocalOf<app.oie.data.RealLink?> { null }

/**
 * Linking the parent's WhatsApp, shared by setup (step 2) and Conexão › Conectar o WhatsApp: the
 * button, then the QR and WhatsApp's own steps, then [onLinked] once the phone has scanned it.
 */
@Composable
fun Pairing(state: AppState, onLinked: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val link = LocalRealLink.current
    when (val s = state.linkState.status) {
        is LinkState.Status.Qr -> {
            tx.linkSteps.forEachIndexed { i, step -> Text("${i + 1}. $step", style = OieType.body, color = c.ink) }
            Qr(s.code, tx.linkQrA11y)
        }
        // v0.34 audit: the wait shows it's working (a spinner, still with "Remove animations" on as a ring).
        LinkState.Status.Connecting -> androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            if (app.oie.ui.common.animationsOn()) androidx.compose.material3.CircularProgressIndicator(Modifier.size(app.oie.ui.common.ICON), color = c.ink, trackColor = c.line, strokeWidth = OTokens.Size.hairline * 2)
            Text(tx.linkWaiting, style = OieType.body, color = c.ink)
        }
        LinkState.Status.Connected -> LaunchedEffect(Unit) { onLinked() }
        LinkState.Status.Unavailable -> Text(tx.linkUnavailable, style = OieType.body, color = c.ink)
        is LinkState.Status.Banned -> Text(tx.bannedFor(s.hours), style = OieType.body, color = c.ink)
        else -> {
            // v0.34 audit: a failed try or no internet says so, and the button tries again.
            val why = when {
                s == LinkState.Status.Disconnected -> tx.linkOffline
                state.linkState.pairFailed -> tx.linkFailed
                else -> null
            }
            if (why != null) Text(why, style = OieType.body, color = c.coralText, modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
            PrimaryButton(tx.linkConnect, { state.linkState.pairFailed = false; link?.turnOn() })
        }
    }
}

/** Conexão › Conectar o WhatsApp. Once linked, the kid's side uses it; with no contacts yet, the parent lands in Contatos. */
@Composable
fun PairScreen(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val real = LocalRealLink.current
    Column(Modifier.fillMaxSize()) {
        Bar {
            QuietButton(null, { state.route = Route.Parent(2) }, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.linkConnect, style = OieType.title, color = c.ink)
        }
        Column(Modifier.fillMaxWidth().padding(OTokens.Spacing.space8), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                // v0.34 audit: the risk in ink, never softened (product law).
                Text(tx.riskBody, style = OieType.body, color = c.ink)
                Pairing(state) {
                    if (!state.realMode) real?.goReal() ?: state.goReal()
                    state.route = Route.Parent(if (state.allowed.isEmpty()) 0 else 2)
                }
            }
        }
    }
}

/** The QR, drawn module by module in the QR tokens (always ink on white, both themes). */
@Composable
private fun Qr(code: String, a11y: String) {
    val c = OieTheme.colors
    val matrix = remember(code) {
        QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, 0, 0, mapOf(EncodeHintType.MARGIN to 0))
    }
    Box(
        Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusM)).background(c.qrGround).padding(OTokens.Spacing.space6)
            .semantics { contentDescription = a11y },
    ) {
        Canvas(Modifier.size(OTokens.Size.contactTile * 3)) {
            val cell = size.width / matrix.width
            for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
                if (matrix.get(x, y)) drawRect(c.qrInk, Offset(x * cell, y * cell), Size(cell + 0.5f, cell + 0.5f))
            }
        }
    }
}
