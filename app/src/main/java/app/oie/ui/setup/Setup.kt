package app.oie.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.error
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.style.TextAlign
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.logic.Pin
import app.oie.ui.common.Bar
import app.oie.ui.common.Initial
import app.oie.ui.common.OIcon
import app.oie.ui.common.PinPad
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** Flow: docs/flows/parent-setup-link.md. Steps 0–7; "Passo x de 6" on 1–6. */
@Composable
fun Setup(state: AppState, step: Int, onClose: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val go = { s: Int -> state.route = Route.Setup(s) }
    Column(Modifier.fillMaxSize()) {
        Bar {
            Wordmark()
            Box(Modifier.weight(1f))
            if (step in 1..6) Text(tx.stepOf(step), style = OieType.caption, color = c.muted)
        }
        // Every step scrolls, so its buttons are always reachable, at phone width and large text too (v0.17 audit).
        Box(
            Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())
                // v0.34 audit: the app's side margin, and one left edge on every step (the column fills its 600dp).
                .padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space8),
            contentAlignment = Alignment.Center,
        ) {
            Column(Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
                // v0.34 audit: every step's heading is a title (display is the kid's greeting alone).
                when (step) {
                    0 -> {
                        // The brand's touching rounds, once (README §7: the splash and empty states); the logo
                        // is in the bar, so the name isn't retyped here.
                        app.oie.ui.common.Rounds()
                        Text(tx.welcomeTitle, style = OieType.title, color = c.ink)
                        Text(tx.welcomeBody, style = OieType.body, color = c.ink)
                        PrimaryButton(tx.start, { go(1) })
                    }
                    1 -> {
                        val real = app.oie.ui.parent.LocalRealLink.current
                        Text(tx.riskTitle, style = OieType.title, color = c.ink)
                        // The approved risk notice (01 Brand/voice.md), in full, before anything is linked.
                        Text(
                            tx.riskBody,
                            style = OieType.body, color = c.ink,
                        )
                        ButtonRow {
                            // v0.34 audit: "Entendi, conectar" starts the link, so step 2 opens on the QR (no second "Conectar").
                            PrimaryButton(tx.riskAccept, { real?.turnOn(); go(2) })
                            // Agora não: Oiê opens on the welcome next time, as the flow says.
                            QuietButton(tx.riskDecline, { go(0); onClose() })
                        }
                    }
                    // v0.34 audit: the parent's name comes from their WhatsApp profile when they haven't typed one (never asked twice).
                    2 -> LinkStep(state, back = { go(1) }, next = { if (state.parentName.isBlank()) state.parentName = state.wa.profileName.orEmpty(); go(3) })
                    3 -> NamesStep(state, back = { go(2) }, next = { go(4) })
                    // v0.34 audit round 2: Voltar centred under the centred keypad.
                    4 -> { CreatePinStep(state, next = { go(5) }); Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { QuietButton(tx.back, { go(3) }) } }
                    5 -> ContactsStep(state, next = { go(6) }, back = { go(4) })
                    6 -> QuietStep(state, next = { go(7) }, back = { go(5) })
                    else -> {
                        Text(tx.done, style = OieType.title, color = c.ink)
                        Text(tx.handOver(state.kidName), style = OieType.body, color = c.ink)
                        // Notifications and the microphone are asked for here, with the parent, after saying why (ux-user-flows §8).
                        val ask = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
                        ) { state.finishSetup() }
                        Text(tx.allowNotifications, style = OieType.body, color = c.muted)
                        PrimaryButton(tx.openFor(state.kidName), {
                            val asks = buildList {
                                add(android.Manifest.permission.RECORD_AUDIO)
                                if (android.os.Build.VERSION.SDK_INT >= 33) add(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                            ask.launch(asks.toTypedArray())
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun Wordmark() {
    androidx.compose.foundation.Image(
        painterResource(R.drawable.oie_wordmark), contentDescription = "Oiê",
        // v0.34 audit: the trimmed mark at 48dp tall is about 64dp wide, the logo's minimum, inside the 56dp bar.
        modifier = Modifier.height(OTokens.Spacing.space12),
    )
}

@Composable
private fun LinkStep(state: AppState, back: () -> Unit, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.linkTitle, style = OieType.title, color = c.ink)
    // v0.8: the real link. Once the phone scans the QR, the kid's side uses the real WhatsApp.
    val real = app.oie.ui.parent.LocalRealLink.current
    // v0.34 audit: coming back to this step when it's already linked shows that, with Continuar; it no longer
    // jumps forward by itself (Back from the names bounced), and it never starts the kid's side over.
    val already = androidx.compose.runtime.remember { state.realMode && state.linkState.status == app.oie.data.LinkState.Status.Connected }
    if (already) {
        Text(tx.connectedTo(state.wa.profileName ?: state.parentName), style = OieType.body, color = c.ink)
        ButtonRow {
            PrimaryButton(tx.cont, next)
            QuietButton(tx.back, back)
        }
        return
    }
    app.oie.ui.parent.Pairing(state, back = back) { if (!state.realMode) real?.goReal() ?: state.goReal(); next() }
    // Help where it's needed (Nielsen 10): the risk, one tap away.
    var why by remember { mutableStateOf(false) }
    TextLink(tx.whatCanHappen, { why = !why })
    if (why) Text(tx.riskBody, style = OieType.body, color = c.ink)
}

@Composable
private fun NamesStep(state: AppState, back: () -> Unit, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.namesTitle, style = OieType.title, color = c.ink)
    Field(tx.yourName, state.parentName, { state.parentName = it }, tx.yourNameHelp(state.kidName.ifBlank { tx.theKid }, state.parentName), names = true)
    Field(tx.kidNameLabel, state.kidName, { state.kidName = it }, tx.kidNameHelp(state.kidName.ifBlank { tx.theKid }), names = true)
    // Help where it's needed (Nielsen 10) is on the link step; here the hints under each field do it.
    ButtonRow {
        PrimaryButton(tx.cont, next, enabled = state.kidName.isNotBlank() && state.parentName.isNotBlank())
        QuietButton(tx.back, back)
    }
}

/**
 * A labelled text field (README §10). v0.34 audit: TalkBack reads the label with the field; the
 * border is `muted` (3:1 against the page, so the field can be found); an [error] replaces the help in
 * the error colour, with an error border, and is announced. Names start with a capital.
 */
@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, help: String, error: String? = null,
          keyboard: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text, names: Boolean = false) {
    val c = OieTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
        Text(label, style = OieType.label, color = c.ink)
        OutlinedTextField(
            value, onChange,
            Modifier.fillMaxWidth().semantics {
                contentDescription = label
                if (error != null) error(error)
            },
            textStyle = OieType.body, singleLine = true, isError = error != null,
            // Names start each word with a capital ("Maria Silva"); a number gets the phone keypad.
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = if (names) androidx.compose.ui.text.input.KeyboardCapitalization.Words else androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                keyboardType = keyboard,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = c.ink, unfocusedTextColor = c.ink, focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface, focusedBorderColor = c.ink, unfocusedBorderColor = c.muted, cursorColor = c.ink,
                errorBorderColor = c.coralText, errorContainerColor = c.surface, errorTextColor = c.ink, errorCursorColor = c.ink,
            ),
        )
        if (error != null) Text(error, style = OieType.caption, color = c.coralText, modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
        else if (help.isNotBlank()) Text(help, style = OieType.caption, color = c.muted)
    }
}

@Composable
internal fun CreatePinStep(state: AppState, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    var first by remember { mutableStateOf<String?>(null) }
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
        Text(if (first == null) tx.pinCreate else tx.pinAgain, style = OieType.title, color = c.ink)
        // v0.34 audit: an error is announced by TalkBack.
        Text(error ?: tx.pinCreateHelp, style = OieType.body, color = if (error != null) c.coralText else c.muted, textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
        PinPad(entered) { v ->
            entered = v
            if (v.length == Pin.LENGTH) {
                val f = first
                when {
                    f == null && Pin.problem(v) == Pin.Problem.EASY -> { error = tx.pinEasy; entered = "" }
                    f == null -> { first = v; entered = ""; error = null }
                    f == v -> { state.setPin(v); next() }
                    // v0.34 audit: a typo could be in either entry, so a mismatch starts over from the first (Android's way).
                    else -> { error = tx.pinMismatch; entered = ""; first = null }
                }
            }
        }
    }
}

@Composable
private fun ContactsStep(state: AppState, next: () -> Unit, back: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val picked = remember { mutableStateListOf<String>().apply { addAll(state.allowed.map { it.id }) } }
    // People added by number here (v0.17), shown with the rest.
    val added = remember { mutableStateListOf<app.oie.logic.Contact>() }
    var query by remember { mutableStateOf("") }
    // v0.19: names the parent gave the kid here, by id; kept while picking, applied on Continue.
    val renamed = remember { mutableStateMapOf<String, app.oie.logic.Contact>().apply { state.allowed.forEach { put(it.id, it) } } }
    var editing by remember { mutableStateOf<String?>(null) }
    val fresh = state.wa.contacts()
    // v0.34 audit round 2: sorted once per change, not on every recomposition.
    val all = remember(fresh, added.toList(), renamed.toMap()) {
        (added + fresh.filter { a -> added.none { it.id == a.id } }).map { renamed[it.id] ?: it }
            .sortedWith(compareBy({ it.isGroup }, { it.name.lowercase() })) // people first, then groups
    }
    // v0.34 audit round 2: "Mudar nome" on the person just picked, not repeated on every picked row (names can
    // also be changed any time in Contatos).
    var lastPicked by remember { mutableStateOf<String?>(null) }
    all.firstOrNull { it.id == editing }?.let { ct ->
        app.oie.ui.parent.ContactEditor(ct, state.kidName.ifBlank { tx.theKid }, fresh,
            onSave = { renamed[it.id] = it; editing = null }, onCancel = { editing = null })
        return
    }
    Text(tx.canTalkTo(state.kidName), style = OieType.title, color = c.ink)
    // v0.17: the contacts arrive a little after linking; say so, with the count, until they're all here.
    val sync = app.oie.ui.parent.contactSyncState(state)
    app.oie.ui.parent.ContactSyncLine(state, sync)
    // A real WhatsApp has many contacts: search them.
    if (all.size > SEARCH_FROM) Field(tx.search, query, { query = it }, tx.pickRealContacts(state.kidName))
    val shown = all.filter { it.id in picked || app.oie.logic.ContactNames.matches(it, query) }.take(if (all.size > SEARCH_FROM) SEARCH_MAX else all.size)
    // Plain rows (at most 30, see SEARCH_MAX) in the scrolling page, not a fixed-height box (v0.17 audit).
    // v0.34 audit: a small gap, so two picked rows don't run together.
    Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
        shown.forEach { ct ->
            val on = ct.id in picked
            // v0.34 audit: a picked row is `selected` (README: picked things), and TalkBack hears "checked".
            Row(
                Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(OTokens.Radius.radiusM)).background(if (on) c.selected else androidx.compose.ui.graphics.Color.Transparent)
                    .toggleable(on, role = Role.Checkbox) { if (on) picked.remove(ct.id) else { picked.add(ct.id); lastPicked = ct.id } }
                    .padding(horizontal = OTokens.Spacing.space2, vertical = OTokens.Spacing.space1),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            ) {
                app.oie.ui.common.Avatar(ct, OTokens.Size.avatar)
                // The group note sits under the name, so it fits a phone (v0.17 audit).
                Column(Modifier.weight(1f)) {
                    Text(ct.name, style = OieType.label, color = c.ink)
                    // v0.19: the number, so the parent picks the right person.
                    if (ct.isGroup) Text(tx.groupWarning, style = OieType.caption, color = c.muted) else app.oie.ui.parent.ContactLines(ct, fresh)
                    // v0.19: a picked contact can get the name the kid will see. Its own target (the row picks), under
                    // the lines so a phone keeps the width for the name (v0.19 audit).
                    if (on && ct.id == lastPicked) TextLink(tx.changeName, { editing = ct.id }, Modifier.semantics { contentDescription = tx.changeNameA11y(ct.name) })
                }
                if (on) OIcon(R.drawable.gg_ic_check, contentDescription = tx.chosen)
            }
        }
    }
    if (state.realMode) app.oie.ui.parent.AddByNumber(open = sync == app.oie.logic.ContactSync.State.SLOW || sync == app.oie.logic.ContactSync.State.EMPTY) { ct ->
        if (added.none { it.id == ct.id }) added.add(ct)
        if (ct.id !in picked) picked.add(ct.id)
    }
    // v0.34 audit: one way on. With nobody picked yet it's "Depois"; once someone is, "Continuar com N".
    ButtonRow {
        if (picked.isEmpty()) QuietButton(tx.later, next)
        else PrimaryButton(tx.continueWith(picked.size), {
            state.allowed.clear(); state.allowed.addAll(all.filter { it.id in picked }); next()
        })
        QuietButton(tx.back, back)
    }
}

@Composable
private fun QuietStep(state: AppState, next: () -> Unit, back: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.quietTitle, style = OieType.title, color = c.ink)
    Text(tx.quietSetupBody(state.kidName), style = OieType.body, color = c.ink)
    QuietList(state)
    // v0.34 audit: Pular did what Continuar does (the switches already say what's on), so it went.
    ButtonRow {
        PrimaryButton(tx.cont, next)
        QuietButton(tx.back, back)
    }
}

/** The periods with their switches (setup step 6 and the parent area). */
@Composable
fun QuietList(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    // v0.34 audit: each period is one switch row (the whole row toggles, TalkBack reads its name and state),
    // with the days and hours under the name so it fits a phone at large text.
    state.periods.forEachIndexed { i, p ->
        app.oie.ui.common.SwitchRow(
            tx.periodName(p.name), p.on, { state.periods[i] = p.copy(on = it) },
            help = "${app.oie.ui.parent.daysLabel(tx, p.days)} · ${app.oie.ui.parent.hoursLabel(tx, p)}",
        )
    }
}

/** Lists longer than this get a search field; it then shows at most [SEARCH_MAX] at a time. */
const val SEARCH_FROM = 12
const val SEARCH_MAX = 30

/** Buttons side by side, wrapping to the next line on a narrow screen instead of squeezing (v0.13 audit). */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ButtonRow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) { content() }
}
