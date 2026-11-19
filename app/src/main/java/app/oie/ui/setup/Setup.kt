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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
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
                .padding(if (app.oie.ui.common.isCompact) OTokens.Spacing.space6 else OTokens.Spacing.space12),
            contentAlignment = Alignment.Center,
        ) {
            Column(Modifier.widthIn(max = OTokens.Size.contentMax), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
                when (step) {
                    0 -> {
                        Text(app.oie.ui.theme.OTokens.Brand.name, style = OieType.display, color = c.ink)
                        Text(tx.welcomeBody, style = OieType.body, color = c.ink)
                        PrimaryButton(tx.start, { go(1) })
                    }
                    1 -> {
                        Text(tx.riskTitle, style = OieType.display, color = c.ink)
                        // The approved risk notice (01 Brand/voice.md), in full, before anything is linked.
                        Text(
                            tx.riskBody,
                            style = OieType.body, color = c.ink,
                        )
                        ButtonRow {
                            PrimaryButton(tx.riskAccept, { go(2) })
                            QuietButton(tx.riskDecline, onClose, Modifier.height(OTokens.Size.button))
                        }
                    }
                    2 -> LinkStep(state, back = { go(1) }, next = { if (!state.realMode) state.parentName = state.link.profileName; go(3) })
                    3 -> NamesStep(state, back = { go(2) }, next = { go(4) })
                    4 -> CreatePinStep(state, next = { go(5) })
                    5 -> ContactsStep(state, next = { go(6) })
                    6 -> QuietStep(state, next = { go(7) })
                    else -> {
                        Text(tx.done, style = OieType.display, color = c.ink)
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
        modifier = Modifier.height(OTokens.Spacing.space8),
    )
}

@Composable
private fun LinkStep(state: AppState, back: () -> Unit, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.linkTitle, style = OieType.title, color = c.ink)
    // v0.8: the real link. Once the phone scans the QR, the kid's side uses the real WhatsApp.
    val real = app.oie.ui.parent.LocalRealLink.current
    app.oie.ui.parent.Pairing(state) { real?.goReal() ?: state.goReal(); next() }
    ButtonRow {
        QuietButton(tx.back, back, Modifier.height(OTokens.Size.button))
    }
}

@Composable
private fun NamesStep(state: AppState, back: () -> Unit, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.namesTitle, style = OieType.title, color = c.ink)
    Field(tx.yourName, state.parentName, { state.parentName = it }, tx.yourNameHelp(state.kidName.ifBlank { tx.theKid }, state.parentName))
    Field(tx.kidNameLabel, state.kidName, { state.kidName = it }, tx.kidNameHelp(state.kidName.ifBlank { "Tito" }))
    ButtonRow {
        PrimaryButton(tx.cont, next, enabled = state.kidName.isNotBlank() && state.parentName.isNotBlank())
        QuietButton(tx.back, back, Modifier.height(OTokens.Size.button))
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, help: String) {
    val c = OieTheme.colors
    val tx = str
    Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
        Text(label, style = OieType.label, color = c.ink)
        OutlinedTextField(
            value, onChange, Modifier.fillMaxWidth(), textStyle = OieType.body, singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = c.ink, unfocusedTextColor = c.ink, focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface, focusedBorderColor = c.ink, unfocusedBorderColor = c.line, cursorColor = c.ink,
            ),
        )
        Text(help, style = OieType.caption, color = c.muted)
    }
}

@Composable
private fun CreatePinStep(state: AppState, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    var first by remember { mutableStateOf<String?>(null) }
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
        Text(if (first == null) tx.pinCreate else tx.pinAgain, style = OieType.title, color = c.ink)
        Text(error ?: tx.pinCreateHelp, style = OieType.body, color = if (error != null) c.coralText else c.muted, textAlign = TextAlign.Center)
        PinPad(entered) { v ->
            entered = v
            if (v.length == Pin.LENGTH) {
                val f = first
                when {
                    f == null && Pin.problem(v) == Pin.Problem.EASY -> { error = tx.pinEasy; entered = "" }
                    f == null -> { first = v; entered = ""; error = null }
                    f == v -> { state.setPin(v); next() }
                    else -> { error = tx.pinMismatch; entered = "" } // only the second entry clears
                }
            }
        }
    }
}

@Composable
private fun ContactsStep(state: AppState, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val picked = remember { mutableStateListOf<String>().apply { addAll(state.allowed.map { it.id }) } }
    // People added by number here (v0.17), shown with the rest.
    val added = remember { mutableStateListOf<app.oie.logic.Contact>() }
    var query by remember { mutableStateOf("") }
    val all = (added + state.wa.contacts().filter { a -> added.none { it.id == a.id } })
        .sortedWith(compareBy({ it.isGroup }, { it.name.lowercase() })) // people first, then groups
    Text(tx.canTalkTo(state.kidName), style = OieType.title, color = c.ink)
    // v0.17: the contacts arrive a little after linking; say so, with the count, until they're all here.
    val sync = app.oie.ui.parent.contactSyncState(state)
    app.oie.ui.parent.ContactSyncLine(state, sync)
    // A real WhatsApp has many contacts: search them.
    if (all.size > SEARCH_FROM) Field(tx.search, query, { query = it }, tx.pickRealContacts(state.kidName))
    val shown = all.filter { it.id in picked || query.isBlank() || it.name.contains(query, ignoreCase = true) }.take(if (all.size > SEARCH_FROM) SEARCH_MAX else all.size)
    // Plain rows (at most 30, see SEARCH_MAX) in the scrolling page, not a fixed-height box (v0.17 audit).
    Column {
        shown.forEach { ct ->
            val on = ct.id in picked
            Row(
                Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin)
                    .clickable(role = Role.Checkbox) { if (on) picked.remove(ct.id) else picked.add(ct.id) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            ) {
                Initial(ct.name, OTokens.Size.avatar)
                // The group note sits under the name, so it fits a phone (v0.17 audit).
                Column(Modifier.weight(1f)) {
                    Text(ct.name, style = OieType.label, color = c.ink)
                    if (ct.isGroup) Text(tx.groupWarning, style = OieType.caption, color = c.muted)
                }
                if (on) OIcon(R.drawable.gg_ic_check, contentDescription = tx.chosen)
            }
        }
    }
    if (state.realMode) app.oie.ui.parent.AddByNumber(open = sync == app.oie.logic.ContactSync.State.SLOW || sync == app.oie.logic.ContactSync.State.EMPTY) { ct ->
        if (added.none { it.id == ct.id }) added.add(ct)
        if (ct.id !in picked) picked.add(ct.id)
    }
    ButtonRow {
        PrimaryButton(tx.continueWith(picked.size), {
            state.allowed.clear(); state.allowed.addAll(all.filter { it.id in picked }); next()
        })
        QuietButton(tx.later, next, Modifier.height(OTokens.Size.button))
    }
}

@Composable
private fun QuietStep(state: AppState, next: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.quietTitle, style = OieType.title, color = c.ink)
    Text(tx.quietSetupBody(state.kidName), style = OieType.body, color = c.muted)
    QuietList(state)
    ButtonRow {
        PrimaryButton(tx.cont, next)
        QuietButton(tx.skip, next, Modifier.height(OTokens.Size.button))
    }
}

/** The periods with their switches (setup step 6 and the parent area). */
@Composable
fun QuietList(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    state.periods.forEachIndexed { i, p ->
        Row(Modifier.fillMaxWidth().height(OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
            Text(p.name, style = OieType.label, color = c.ink)
            Text("${app.oie.ui.parent.daysLabel(tx, p.days)} · ${app.oie.ui.parent.hoursLabel(tx, p)}", style = OieType.bodySmall, color = c.muted, modifier = Modifier.weight(1f))
            Switch(
                checked = p.on, onCheckedChange = { state.periods[i] = p.copy(on = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
        }
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
