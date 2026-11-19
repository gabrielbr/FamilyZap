package app.oie.ui.parent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import app.oie.R
import app.oie.data.AppState
import app.oie.data.LinkTest
import app.oie.data.Route
import app.oie.logic.Contact
import app.oie.logic.Kind
import app.oie.logic.RuleTest
import app.oie.ui.common.Bar
import app.oie.ui.common.OIcon
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.setup.Field
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.time.Instant
import java.time.ZoneId

/** The real link, when the app runs on a device; null in screenshot tests. */
val LocalRealLink = androidx.compose.runtime.staticCompositionLocalOf<app.oie.data.RealLink?> { null }

/**
 * Conexão › Testar a regra (docs/flows/rule-test.md). Parent area only,
 * behind the PIN. One coral action per state: Conectar para testar, then nothing filled: the
 * results are what the parent came for.
 */
@Composable
fun RuleTestScreen(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val t = state.linkTest
    val link = LocalRealLink.current
    val kid = state.kidName.ifBlank { tx.theKid }
    Column(Modifier.fillMaxSize()) {
        Bar {
            QuietButton(null, { state.route = Route.Parent(2) }, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.ruleTestTitle, style = OieType.title, color = c.ink)
        }
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(OTokens.Spacing.space8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                when (val s = t.status) {
                    LinkTest.Status.Off -> if (t.chats.isEmpty() && t.entries.isEmpty()) {
                        Text(tx.ruleTestIntro(kid), style = OieType.body, color = c.ink)
                        Text(tx.ruleTestRisk, style = OieType.body, color = c.muted)
                        PrimaryButton(tx.ruleTestStart, { t.on = true; link?.start() })
                    } else {
                        Text(tx.disconnected, style = OieType.body, color = c.ink)
                        PrimaryButton(tx.reconnect, { t.on = true; link?.start() })
                        Results(t, kid)
                    }
                    LinkTest.Status.Connecting -> Text(tx.ruleTestConnecting, style = OieType.body, color = c.muted)
                    is LinkTest.Status.Qr -> {
                        tx.ruleTestQrSteps.forEachIndexed { i, step -> Text("${i + 1}. $step", style = OieType.body, color = c.ink) }
                        Qr(s.code, tx.ruleTestQrA11y)
                        Text(tx.ruleTestRisk, style = OieType.bodySmall, color = c.muted)
                    }
                    LinkTest.Status.Connected, LinkTest.Status.Disconnected -> {
                        if (s == LinkTest.Status.Disconnected) Text(tx.disconnected, style = OieType.body, color = c.ink)
                        Picker(t)
                        Results(t, kid)
                    }
                    is LinkTest.Status.Banned -> { Text(tx.bannedFor(s.hours), style = OieType.body, color = c.ink); Results(t, kid) }
                    LinkTest.Status.LoggedOut -> {
                        Text(tx.loggedOutTest, style = OieType.body, color = c.ink)
                        PrimaryButton(tx.reconnect, { link?.unlink(); t.on = true; link?.start() })
                    }
                    LinkTest.Status.Unavailable -> Text(tx.ruleTestUnavailable, style = OieType.body, color = c.ink)
                }
                if (t.on || t.chats.isNotEmpty() || t.entries.isNotEmpty()) TextLink(tx.ruleTestStop, { link?.unlink() ?: t.clear() })
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

/** The chats in the test, and the parent's WhatsApp list to pick more from. */
@Composable
private fun Picker(t: LinkTest) {
    val c = OieTheme.colors
    val tx = str
    var query by rememberSaveable { mutableStateOf("") }
    var open by rememberSaveable { mutableStateOf(t.chats.isEmpty()) }
    Text(tx.ruleTestChats, style = OieType.label, color = c.ink)
    t.chats.forEach { chat ->
        Row(Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically) {
            Text(chat.name, style = OieType.body, color = c.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            TextLink(tx.remove, { t.chats.remove(chat) })
        }
    }
    if (!open) TextLink(tx.ruleTestPick, { open = true })
    else {
        Field(tx.ruleTestSearch, query, { query = it }, tx.ruleTestPickHelp)
        val picked = t.chats.map { it.id }.toSet()
        t.available.filter { it.id !in picked && (query.isBlank() || it.name.contains(query, ignoreCase = true)) }.take(30).forEach { contact ->
            PickRow(contact) { t.chats += Contact(contact.id, contact.name, contact.isGroup); open = false; query = "" }
        }
    }
}

@Composable
private fun PickRow(contact: Contact, onPick: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Row(
        Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin).clickable(role = Role.Button, onClick = onPick)
            .padding(horizontal = OTokens.Spacing.space2),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
    ) {
        OIcon(if (contact.isGroup) R.drawable.gg_ic_people else R.drawable.gg_ic_plus)
        Column(Modifier.weight(1f)) {
            Text(contact.name, style = OieType.body, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (contact.isGroup) Text(tx.groupWarning, style = OieType.caption, color = c.muted)
        }
    }
}

/** Per chat: pretend the kid spoke. Then every message judged, newest first. */
@Composable
private fun Results(t: LinkTest, kid: String) {
    val c = OieTheme.colors
    val tx = str
    if (t.chats.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
        Column {
            t.chats.forEach { chat -> TextLink("${tx.kidSpokeNow(kid)} · ${chat.name}", { t.kidSpoke[chat.id] = System.currentTimeMillis() }) }
        }
    }
    if (t.entries.isEmpty()) Text(tx.ruleTestEmpty, style = OieType.body, color = c.muted)
    t.entries.forEach { e -> EntryRow(e, kid) }
    if (t.ignored > 0) Text(tx.ignoredCount(t.ignored), style = OieType.caption, color = c.muted)
}

@Composable
private fun EntryRow(e: RuleTest.Entry, kid: String) {
    val c = OieTheme.colors
    val tx = str
    val time = Instant.ofEpochMilli(e.at).atZone(ZoneId.systemDefault()).toLocalTime()
    val what = when (e.kind) {
        Kind.TEXT -> e.text
        Kind.VOICE -> if (e.durationSec > 0) "${tx.notifVoice} · ${e.durationSec} s" else tx.notifVoice
        Kind.PHOTO -> tx.notifPhoto
        Kind.STICKER -> tx.notifSticker
        Kind.DELETED -> tx.deletedMessage
        Kind.UNSUPPORTED -> tx.otherMessage
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.surface)
            .border(OTokens.Size.hairline, c.line, RoundedCornerShape(OTokens.Radius.radiusS)).padding(OTokens.Spacing.space4),
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4), verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
            Text("${e.chatName} · ${tx.clock(time.hour, time.minute)}", style = OieType.label, color = c.ink)
            if (what != null) Text(what, style = OieType.body, color = c.ink)
            e.media?.let { MediaThumb(it) }
            Text(tx.why(kid, e.why), style = OieType.caption, color = c.muted)
        }
        // The verdict in words; the fill only repeats it (colour is never alone).
        Text(
            if (e.shown) tx.wouldReach else tx.staysWithYou, style = OieType.caption,
            color = if (e.shown) c.onSelected else c.ink,
            modifier = Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(if (e.shown) c.selected else c.page)
                .padding(horizontal = OTokens.Spacing.space2, vertical = OTokens.Spacing.space1),
        )
    }
}

/** A photo or sticker that would reach the kid, opened from its sealed file. */
@Composable
private fun MediaThumb(name: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val side = OTokens.Size.contactTile * 2
    val px = with(androidx.compose.ui.platform.LocalDensity.current) { side.roundToPx() }
    val bmp = remember(name) { app.oie.data.Photos.load(context, name, px) } ?: return
    androidx.compose.foundation.Image(
        bmp.asImageBitmap(), contentDescription = null,
        modifier = Modifier.size(side).clip(RoundedCornerShape(OTokens.Radius.radiusS)),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
    )
}
