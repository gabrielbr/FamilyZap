package app.oie.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.oie.BuildConfigInfo
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.logic.Contact
import app.oie.logic.Pin
import app.oie.ui.common.Bar
import app.oie.ui.common.Initial
import app.oie.ui.common.OIcon
import app.oie.ui.common.PinPad
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.setup.QuietList
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import kotlinx.coroutines.delay

/** The parent area closes after 2 minutes without a touch (owner, 2026-11-18). */
const val IDLE_MS = 120_000L

/** Prototype screen 09. Flow: contacts-quiet-hours.md, steps 1–2. */
@Composable
fun ParentPin(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    var entered by remember { mutableStateOf("") }
    var message by remember { mutableStateOf(tx.enterPin) }
    Column(Modifier.fillMaxSize()) {
        Bar {
            QuietButton(null, { state.route = Route.KidHome }, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.parentArea, style = OieType.title, color = c.ink)
        }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4, Alignment.CenterVertically)) {
            Text(message, style = OieType.body, color = c.muted)
            PinPad(entered) { v ->
                entered = v
                if (v.length == Pin.LENGTH) {
                    val now = System.currentTimeMillis()
                    when {
                        state.checkPin(v, now) -> state.route = Route.Parent(0)
                        state.pinGuard.lockedFor(now) > 0 -> message = tx.tooMany
                        else -> message = tx.wrongPin
                    }
                    entered = ""
                }
            }
            // v0.1: forgetting the PIN means relinking WhatsApp (flow), which the sample link can't do yet.
            TextLink(tx.forgotPin, { state.route = Route.Setup(2) })
        }
    }
}

/** Prototype screens 07, 08, 15. */
@Composable
fun ParentArea(state: AppState, section: Int) {
    val c = OieTheme.colors
    val tx = str
    var lastTouch by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lastTouch) { delay(IDLE_MS); state.route = Route.KidHome }
    Column(Modifier.fillMaxSize().clickable(enabled = false) {}) {
        Bar {
            Text(tx.parentArea, style = OieType.title, color = c.ink)
            Box(Modifier.weight(1f))
            QuietButton(tx.leave, { state.route = Route.KidHome }, icon = R.drawable.gg_ic_close)
        }
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(OTokens.Size.contentMax / 2).fillMaxHeight().padding(OTokens.Spacing.space4), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                listOf(tx.sectionContacts to R.drawable.gg_ic_people, tx.sectionQuiet to R.drawable.gg_ic_clock, tx.sectionConnection to R.drawable.gg_ic_link, tx.sectionHelp to R.drawable.gg_ic_info)
                    .forEachIndexed { i, (label, ic) ->
                        val on = i == section
                        Row(
                            Modifier.fillMaxWidth().height(OTokens.Size.rowMin).clip(RoundedCornerShape(OTokens.Radius.radiusS))
                                .background(if (on) c.selected else c.page)
                                .clickable(role = Role.Tab) { lastTouch = System.currentTimeMillis(); state.route = Route.Parent(i) }
                                .padding(horizontal = OTokens.Spacing.space4),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
                        ) { OIcon(ic, tint = if (on) c.onSelected else c.ink); Text(label, style = OieType.label, color = if (on) c.onSelected else c.ink) }
                    }
            }
            Box(Modifier.width(OTokens.Size.hairline).fillMaxHeight().background(c.line))
            Column(
                Modifier.widthIn(max = OTokens.Size.contentMax * 3 / 2).verticalScroll(rememberScrollState())
                    .padding(horizontal = OTokens.Spacing.space12, vertical = OTokens.Spacing.space8),
                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) {
                when (section) {
                    0 -> Contacts(state) { lastTouch = System.currentTimeMillis() }
                    1 -> QuietSection(state) { lastTouch = System.currentTimeMillis() }
                    2 -> {
                        Text(tx.connection, style = OieType.title, color = c.ink)
                        Text(if (state.link.connected) tx.connectedTo(state.parentName) else tx.disconnected, style = OieType.body, color = c.ink)
                        if (state.link.connected) TextLink(tx.simulateDrop, { state.link.pretendDropped(); state.route = Route.KidHome })
                        else PrimaryButton(tx.reconnect, { state.route = Route.Setup(2) })
                    }
                    else -> AboutAndHelp(state)
                }
            }
        }
    }
}

@Composable
private fun Contacts(state: AppState, touched: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    var removed by remember { mutableStateOf<Pair<Int, Contact>?>(null) }
    LaunchedEffect(removed) { if (removed != null) { delay(5_000); removed = null } }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(tx.whoCanCall(state.kidName), style = OieType.title, color = c.ink)
            Text(tx.peopleAndGroups(state.allowed.count { !it.isGroup }, state.allowed.count { it.isGroup }), style = OieType.caption, color = c.muted)
        }
    }
    state.allowed.toList().forEachIndexed { i, ct ->
        Row(Modifier.fillMaxWidth().height(OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
            Initial(ct.name, OTokens.Size.avatar)
            Text(ct.name, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
            if (ct.quietException) Text(tx.canTalkInQuiet, style = OieType.caption, color = c.muted)
            Switch(
                modifier = Modifier.semantics { contentDescription = tx.canTalkInQuietA11y(ct.name) },
                checked = ct.quietException,
                onCheckedChange = { touched(); state.allowed[i] = ct.copy(quietException = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
            // Undo instead of confirm (flow step 6).
            TextLink(tx.remove, { touched(); state.allowed.removeAt(i); removed = i to ct })
        }
    }
    removed?.let { (i, ct) ->
        Row(
            Modifier.padding(top = OTokens.Spacing.space4).clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.snackbar).padding(start = OTokens.Spacing.space6),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
        ) {
            Text(tx.leftTheList(ct.name), style = OieType.body, color = c.page)
            Box(Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) { state.allowed.add(i.coerceAtMost(state.allowed.size), ct); removed = null }.padding(horizontal = OTokens.Spacing.space3), contentAlignment = Alignment.Center) {
                Text(tx.undo, style = OieType.label, color = c.page)
            }
        }
    }
    val addable = state.link.contacts().filter { a -> state.allowed.none { it.id == a.id } }
    if (addable.isNotEmpty()) {
        Text(tx.add, style = OieType.label, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
        addable.forEach { ct ->
            Row(
                Modifier.fillMaxWidth().height(OTokens.Size.rowMin).clickable(role = Role.Button) { touched(); state.allowed.add(ct) },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            ) {
                Initial(ct.name, OTokens.Size.avatar)
                Text(ct.name, style = OieType.label, color = c.ink)
                if (ct.isGroup) Text(tx.groupWarning, style = OieType.caption, color = c.muted)
                Box(Modifier.weight(1f))
                OIcon(R.drawable.gg_ic_plus, contentDescription = tx.addA11y(ct.name))
            }
        }
    }
}


/** Quiet hours in the parent area: the periods, add/edit, delete with undo, and who can talk anyway. */
@Composable
private fun QuietSection(state: AppState, touched: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    var editing by remember { mutableStateOf<Int?>(null) } // -1 = new
    var deleted by remember { mutableStateOf<Pair<Int, app.oie.logic.QuietPeriod>?>(null) }
    LaunchedEffect(deleted) { if (deleted != null) { delay(5_000); deleted = null } }
    val e = editing
    if (e != null) {
        PeriodEditor(
            initial = state.periods.getOrNull(e),
            onSave = { p -> touched(); if (e == -1) state.periods.add(p) else state.periods[e] = p; editing = null },
            onDelete = if (e >= 0) ({ touched(); deleted = e to state.periods.removeAt(e); editing = null }) else null,
            onCancel = { editing = null },
        )
        return
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(tx.quietTitle, style = OieType.title, color = c.ink)
            Text(tx.quietParentBody(state.kidName), style = OieType.caption, color = c.muted)
        }
        PrimaryButton(tx.addPeriod, { touched(); editing = -1 }, icon = R.drawable.gg_ic_plus)
    }
    if (state.periods.isEmpty()) Text(tx.noQuiet(state.kidName), style = OieType.body, color = c.muted)
    state.periods.forEachIndexed { i, p ->
        Row(
            Modifier.fillMaxWidth().height(OTokens.Size.rowMin).clickable(role = Role.Button, onClickLabel = tx.editA11y(p.name)) { touched(); editing = i },
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(p.name, style = OieType.label, color = c.ink)
            Text(if (p.on) "${daysLabel(tx, p.days)} · ${hoursLabel(tx, p)}" else tx.paused, style = OieType.bodySmall, color = c.muted, modifier = Modifier.weight(1f))
            Switch(
                modifier = Modifier.semantics { contentDescription = tx.switchA11y(p.name, p.on) },
                checked = p.on, onCheckedChange = { touched(); state.periods[i] = p.copy(on = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
        }
    }
    deleted?.let { (i, p) ->
        Row(
            Modifier.padding(top = OTokens.Spacing.space4).clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.snackbar).padding(start = OTokens.Spacing.space6),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
        ) {
            Text(tx.deleted(p.name), style = OieType.body, color = c.page)
            Box(Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) { state.periods.add(i.coerceAtMost(state.periods.size), p); deleted = null }.padding(horizontal = OTokens.Spacing.space3), contentAlignment = Alignment.Center) {
                Text(tx.undo, style = OieType.label, color = c.page)
            }
        }
    }
    Text(tx.talkAnyway(state.allowed.filter { it.quietException }.joinToString { it.name }.ifEmpty { tx.nobodyChooseInContacts }),
        style = OieType.body, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space4))
}


/** Ajuda e sobre: short answers to the questions the flows raise, the risk said again, version and licences. */
@Composable
private fun AboutAndHelp(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    var open by remember { mutableStateOf<Int?>(null) }
    val help = tx.helpItems(state.kidName.ifBlank { tx.theKid }, state.parentName)
    Text(tx.help, style = OieType.title, color = c.ink)
    help.forEachIndexed { i, (q, a) ->
        Column(Modifier.fillMaxWidth().clickable(role = Role.Button) { open = if (open == i) null else i }.padding(vertical = OTokens.Spacing.space3)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                Text(q, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
                OIcon(R.drawable.gg_ic_chevron, tint = c.muted, modifier = Modifier.rotate(if (open == i) 90f else 0f))
            }
            if (open == i) Text(a, style = OieType.body, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space2))
        }
    }
    Text(tx.about, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    Text(tx.aboutVersion(BuildConfigInfo.VERSION), style = OieType.body, color = c.ink)
    Text(tx.notWhatsApp, style = OieType.body, color = c.ink)
    Text(tx.credits, style = OieType.caption, color = c.muted)
}
