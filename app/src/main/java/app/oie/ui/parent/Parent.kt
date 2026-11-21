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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.liveRegion
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
        // v0.34 audit: it scrolls (a phone held sideways), and a wrong PIN is in the error colour and announced.
        Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(vertical = OTokens.Spacing.space8), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4, Alignment.CenterVertically)) {
            Text(message, style = OieType.body, color = if (message == tx.enterPin) c.muted else c.coralText,
                modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
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
            // v0.34 audit (P0): a new PIN only after the same WhatsApp links again; it never goes through setup.
            TextLink(tx.forgotPin, { state.route = Route.ForgotPin(0) })
        }
    }
}

/** Prototype screens 07, 08, 15. */
@Composable
fun ParentArea(state: AppState, section: Int, names: Boolean = false) {
    val c = OieTheme.colors
    val tx = str
    var lastTouch by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lastTouch) { delay(IDLE_MS); state.route = Route.KidHome }
    val touched = { lastTouch = System.currentTimeMillis() }
    // v0.34 audit: one undo snackbar for the whole area, pinned to the bottom (it was inline, off screen on a phone).
    // v0.34 audit round 2: what's undone is put back; what isn't is committed when the undo goes (its 5 s run
    // out, another undo replaces it, or the parent leaves the area), so "deleted" is always true.
    var undo by remember { mutableStateOf<Offer?>(null) }
    LaunchedEffect(undo) { val u = undo ?: return@LaunchedEffect; delay(UNDO_MS); u.commit(); undo = null }
    val latest by androidx.compose.runtime.rememberUpdatedState(undo)
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { latest?.commit?.invoke() } }
    val offerUndo: Undo = { text, restore, commit -> undo?.commit?.invoke(); undo = Offer(text, restore, commit) }
    // Round 3: the report's "deleted" state lives here, so switching sections during the undo can't bring it back.
    val report = remember { ReportView() }
    Box(Modifier.fillMaxSize()) {
    // v0.34 audit: any touch anywhere in the area (scrolling too) keeps it open; typing counts through touched().
    Column(Modifier.fillMaxSize().pointerInput(Unit) {
        awaitPointerEventScope { while (true) { awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial); touched() } }
    }) {
        Bar {
            Text(tx.parentArea, style = OieType.title, color = c.ink)
            Box(Modifier.weight(1f))
            QuietButton(tx.leave, { state.route = Route.KidHome }, icon = R.drawable.gg_ic_close)
        }
        val sectionContent: @Composable () -> Unit = {
            when (section) {
                0 -> Contacts(state, touched, offerUndo)
                1 -> QuietSection(state, touched, offerUndo)
                // v0.44 (the owner: "settings is mixed with help and everything is crowded"): changing things in
                // Ajustes, reading and upkeep in Ajuda. The numbers keep their meaning: 2 holds the connection, 3 the report.
                2 -> Settings(state, touched, names)
                else -> Help(state, offerUndo, report)
            }
        }
        val compact = app.oie.ui.common.isCompact
        // v0.34 audit: on a phone the four sections are a 2 x 2 grid with short names, all in view at once.
        val sections = listOf(tx.sectionContacts to R.drawable.gg_ic_people, (if (compact) tx.sectionQuietShort else tx.sectionQuiet) to R.drawable.gg_ic_clock,
            tx.sectionSettings to R.drawable.gg_ic_gear, tx.sectionHelp to R.drawable.gg_ic_info)
        @Composable
        fun tab(i: Int, label: String, ic: Int, modifier: Modifier) {
            val on = i == section
            Row(
                modifier.height(OTokens.Size.rowMin).clip(RoundedCornerShape(OTokens.Radius.radiusS))
                    .background(if (on) c.selected else c.page)
                    .selectable(on, role = Role.Tab) { touched(); state.route = Route.Parent(i) }
                    .padding(horizontal = OTokens.Spacing.space4),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            ) { OIcon(ic, tint = if (on) c.onSelected else c.ink); Text(label, style = OieType.label, color = if (on) c.onSelected else c.ink, maxLines = 1) }
        }
        // A tablet: sections on the left, the section on the right. A phone: the sections on top, the section below.
        val body: @Composable (Modifier) -> Unit = { m ->
            Column(
                m.verticalScroll(rememberScrollState())
                    .padding(horizontal = if (compact) OTokens.Spacing.space4 else OTokens.Spacing.space12, vertical = if (compact) OTokens.Spacing.space6 else OTokens.Spacing.space8)
                    .padding(bottom = OTokens.Spacing.space16), // room for the undo snackbar
                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) { sectionContent() }
        }
        if (compact) Column(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(OTokens.Spacing.space2), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                listOf(0, 2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                        (row..row + 1).forEach { i -> tab(i, sections[i].first, sections[i].second, Modifier.weight(1f)) }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
            body(Modifier.fillMaxWidth())
        } else Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(OTokens.Size.contentMax / 2).fillMaxHeight().padding(OTokens.Spacing.space4), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                sections.forEachIndexed { i, (label, ic) -> tab(i, label, ic, Modifier.fillMaxWidth()) }
            }
            Box(Modifier.width(OTokens.Size.hairline).fillMaxHeight().background(c.line))
            // v0.34 audit: one column up to 600dp (README §6), so help lines stay readable.
            body(Modifier.widthIn(max = OTokens.Size.contentMax + OTokens.Spacing.space12 * 2))
        }
    }
    undo?.let { u ->
        app.oie.ui.common.Snackbar(u.text, Modifier.align(Alignment.BottomCenter), action = tx.undo) { u.restore(); undo = null }
    }
    }
}

/** The problem report as the parent area shows it: hidden while its delete can still be undone, and re-read after. */
private class ReportView {
    var hidden by mutableStateOf(false)
    var version by mutableStateOf(0)
}

/** How long an undo stays offered. */
private const val UNDO_MS = 5_000L

/** An undo on offer: its line, how to put the thing back, and what to finish if nobody undoes it. */
private class Offer(val text: String, val restore: () -> Unit, val commit: () -> Unit)

/** Offers an undo: the line, the restore, and the commit (default: nothing left to do). */
private typealias Undo = (String, () -> Unit, () -> Unit) -> Unit
@Composable
private fun Contacts(state: AppState, touched: () -> Unit, offerUndo: Undo) {
    val c = OieTheme.colors
    val tx = str
    // v0.19: the name the kid sees, edited in place of the list, like a quiet period.
    var editing by remember { mutableStateOf<String?>(null) }
    val fresh = state.wa.contacts()
    // Whole-app review: one set of letters for everyone on this screen (allowed and addable), as in setup.
    val letters = remember(state.allowed.toList(), fresh) { app.oie.logic.Initials.of((state.allowed + fresh).map { it.name }) }
    state.allowed.indexOfFirst { it.id == editing }.takeIf { it >= 0 }?.let { i ->
        ContactEditor(state.allowed[i], state.kidName.ifBlank { tx.theKid }, fresh,
            onSave = { touched(); state.allowed[i] = it; editing = null }, onCancel = { editing = null },
            onRemove = {
                touched(); val ct = state.allowed.removeAt(i); editing = null
                offerUndo(tx.leftTheList(ct.name), { state.allowed.add(i.coerceAtMost(state.allowed.size), ct) }, {})
            }, touched = touched)
        return
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(tx.whoCanCall(state.kidName), style = OieType.title, color = c.ink)
            Text(tx.peopleAndGroups(state.allowed.count { !it.isGroup || it.parentLine }, state.allowed.count { it.isGroup && !it.parentLine }), style = OieType.caption, color = c.muted)
            if (state.allowed.isNotEmpty() && !state.contactTipUsed) Text(tx.renameHint(state.kidName.ifBlank { tx.theKid }), style = OieType.caption, color = c.muted)
        }
    }
    if (state.realMode && state.allowed.none { it.parentLine }) ParentLineCard(state, touched)
    if (state.addRequested) Row(
        Modifier.fillMaxWidth().padding(vertical = OTokens.Spacing.space3).clip(RoundedCornerShape(OTokens.Radius.radiusM)).background(c.selected)
            .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) {
        Text(tx.kidAsked(state.kidName), style = OieType.label, color = c.onSelected, modifier = Modifier.weight(1f))
        TextLink(tx.understood, { touched(); state.addRequested = false })
    }
    state.allowed.toList().forEachIndexed { i, ct ->
        Row(Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
            // v0.19: the person (avatar and lines) opens the name editor; the switch and Remover keep their own targets.
            Row(
                Modifier.weight(1f).heightIn(min = OTokens.Size.rowMin).clickable(role = Role.Button, onClickLabel = tx.changeNameA11y(ct.name)) { touched(); editing = ct.id; state.contactTipUsed = true },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            ) {
            app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters[ct.name])
            // The switch is always labelled, on or off, right under the name, so it fits a phone too (v0.13 audit).
            Column(Modifier.weight(1f).padding(vertical = OTokens.Spacing.space2)) {
                Text(ct.name, style = OieType.label, color = c.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                // v0.19: the number, so the parent knows it's the right person.
                ContactLines(ct, fresh, state.kidName.ifBlank { tx.theKid })
                // The line says the current setting, so it's true whichever way the switch is.
                // v0.21: a chat paused from the parent's WhatsApp says so, in ink, instead of the quiet-hours line.
                if (ct.paused) Text(tx.pausedLine, style = OieType.caption, color = c.ink, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                // v0.34 audit: with no quiet period on, the line (like the switch) would say nothing.
                else if (state.periods.any { it.on }) Text(if (ct.quietException) tx.canTalkInQuiet else tx.onlyOutsideQuiet, style = OieType.caption, color = c.muted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            }
            // v0.34 audit: the switch shows only when there's a quiet period to talk through; with none, a column
            // of "off" switches read as "blocked".
            if (state.periods.any { it.on }) Switch(
                modifier = Modifier.semantics { contentDescription = tx.canTalkInQuietA11y(ct.name) },
                checked = ct.quietException,
                onCheckedChange = { touched(); state.allowed[i] = ct.copy(quietException = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
            // v0.19 audit: Remover moved into the contact's editor (undo instead of confirm, flow step 6), so a phone row fits the number.
        }
    }
    // v0.12: the parent's contacts arrive a little after linking; reload them each time Contatos opens.
    val real = LocalRealLink.current
    LaunchedEffect(Unit) { real?.loadContacts() }
    // v0.26: someone just added gets their picture now, not tomorrow (each contact at most once a day).
    LaunchedEffect(state.allowed.size) { real?.refreshPhotos() }
    val all = fresh.filter { a -> state.allowed.none { it.id == a.id } }
    var query by remember { mutableStateOf("") }
    // People first, then groups; a search narrows them, 30 at a time.
    val addable = all.filter { app.oie.logic.ContactNames.matches(it, query) }
        .sortedWith(compareBy({ it.isGroup }, { it.name.lowercase() })).take(app.oie.ui.setup.SEARCH_MAX)
    if (state.allowed.isEmpty()) Text(tx.pickRealContacts(state.kidName.ifBlank { tx.theKid }), style = OieType.body, color = c.ink)
    Text(tx.add, style = OieType.label, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    // Right after linking only groups have arrived; say the people are on their way (v0.13 audit).
    // v0.17: what the contact list is waiting for, the same line as in setup.
    val sync = contactSyncState(state)
    ContactSyncLine(state, sync)
    if (all.size > app.oie.ui.setup.SEARCH_FROM) app.oie.ui.setup.Field(tx.search, query, { query = it; touched() }, tx.searchHelp)
    run {
        addable.forEach { ct ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin).clickable(role = Role.Button) { touched(); state.allowed.add(ct) },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            ) {
                app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters[ct.name])
                // The group note under the name, so it fits a phone (v0.17 audit).
                Column(Modifier.weight(1f)) {
                    Text(ct.name, style = OieType.label, color = c.ink)
                    // v0.19: the number before adding, so the right Maria is picked.
                    if (ct.isGroup) Text(tx.groupWarning, style = OieType.caption, color = c.muted) else ContactLines(ct, fresh)
                }
                OIcon(R.drawable.gg_ic_plus, contentDescription = tx.addA11y(ct.name))
            }
        }
    }
    if (state.realMode) AddByNumber(open = sync == app.oie.logic.ContactSync.State.SLOW || sync == app.oie.logic.ContactSync.State.EMPTY, touched = touched) { ct ->
        if (state.allowed.none { it.id == ct.id }) state.allowed.add(ct)
    }
}



/** Quiet hours in the parent area: the periods, add/edit, delete with undo, and who can talk anyway. */
@Composable
private fun QuietSection(state: AppState, touched: () -> Unit, offerUndo: Undo) {
    val c = OieTheme.colors
    val tx = str
    var editing by remember { mutableStateOf<Int?>(null) } // -1 = new
    val e = editing
    if (e != null) {
        PeriodEditor(
            initial = state.periods.getOrNull(e),
            onSave = { p -> touched(); if (e == -1) state.periods.add(p) else state.periods[e] = p; editing = null },
            onDelete = if (e >= 0) ({
                touched(); val p = state.periods.removeAt(e); editing = null
                offerUndo(tx.deleted(p.name), { state.periods.add(e.coerceAtMost(state.periods.size), p) }, {})
            }) else null,
            onCancel = { editing = null },
            touched = touched,
        )
        return
    }
    // The title and its button side by side on a tablet; stacked on a phone, so the title doesn't wrap (v0.13 audit).
    val header: @Composable () -> Unit = {
        Text(tx.quietTitle, style = OieType.title, color = c.ink)
        Text(tx.quietParentBody(state.kidName), style = OieType.caption, color = c.muted)
    }
    // v0.43: quiet turned on from the parent's WhatsApp ("/Tito silêncio"): said here, with a way to lift it.
    if (state.quietManual) Row(
        Modifier.fillMaxWidth().padding(bottom = OTokens.Spacing.space4).clip(RoundedCornerShape(OTokens.Radius.radiusM)).background(c.selected)
            .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) {
        Text(tx.quietManualOn, style = OieType.label, color = c.onSelected, modifier = Modifier.weight(1f))
        TextLink(tx.quietLift, { touched(); state.quietManual = false })
    }
    if (app.oie.ui.common.isCompact) {
        header()
        PrimaryButton(tx.addPeriod, { touched(); editing = -1 }, icon = R.drawable.gg_ic_plus, modifier = Modifier.padding(top = OTokens.Spacing.space2))
    } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) { header() }
        PrimaryButton(tx.addPeriod, { touched(); editing = -1 }, icon = R.drawable.gg_ic_plus)
    }
    if (state.periods.isEmpty()) Text(tx.noQuiet(state.kidName), style = OieType.body, color = c.muted)
    state.periods.forEachIndexed { i, p ->
        Row(
            Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin).clickable(role = Role.Button, onClickLabel = tx.editA11y(p.name)) { touched(); editing = i },
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            // v0.34 audit: the name over its days and hours, so a phone at large text keeps both readable.
            Column(Modifier.weight(1f).padding(vertical = OTokens.Spacing.space2)) {
                Text(tx.periodName(p.name), style = OieType.label, color = c.ink)
                // Fresh review: the schedule always shows (the switch says on or off; "Pausado" is a contact's word).
                Text("${daysLabel(tx, p.days)} · ${hoursLabel(tx, p)}", style = OieType.bodySmall, color = c.muted)
            }
            Switch(
                modifier = Modifier.semantics { contentDescription = tx.switchA11y(p.name, p.on) },
                checked = p.on, onCheckedChange = { touched(); state.periods[i] = p.copy(on = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
        }
    }
    // v0.34 audit round 2: only while a period is on (otherwise Contatos has nothing to choose).
    if (state.periods.any { it.on }) Text(tx.talkAnyway(state.allowed.filter { it.quietException }.joinToString { it.name }.ifEmpty { tx.nobodyChooseInContacts }),
        style = OieType.body, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space4))
}


/**
 * Ajustes (v0.44): what changes how Oiê works, in the order a parent needs it. The connection first (when it drops,
 * it's the first thing to see), with the unread mark that belongs to the parent's WhatsApp; the names as one row
 * that opens [NamesEditor] (set once in setup, rarely changed); then what the tablet does.
 */
@Composable
private fun Settings(state: AppState, touched: () -> Unit, startInNames: Boolean) {
    val c = OieTheme.colors
    val tx = str
    var names by remember { mutableStateOf(startInNames) }
    if (names) { NamesEditor(state, touched) { touched(); names = false }; return }
    Connection(state)
    UnreadMarkSetting(state)
    IntroSetting(state)
    Text(tx.names, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    NamesRow(state) { touched(); names = true }
    BubbleSetting(state)
    NudgeSetting(state)
    StatusSetting(state)
}

/** The names as one line, "Ana · Tito (Chico, Fran) · 😎", a button that opens [NamesEditor]. */
@Composable
private fun NamesRow(state: AppState, onOpen: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val kid = state.kidName.ifBlank { tx.theKid } + state.kidNicknames.takeIf { it.isNotEmpty() }?.joinToString(", ", " (", ")").orEmpty()
    val line = listOf(state.parentName, kid, state.kidEmoji).filter { it.isNotBlank() }.joinToString(" · ")
    Row(
        Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin).clickable(role = Role.Button, onClickLabel = tx.editNames, onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
    ) {
        Text(line, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f).padding(vertical = OTokens.Spacing.space2))
        OIcon(R.drawable.gg_ic_chevron, tint = c.muted)
    }
}

/** Nomes (v0.44, a level under Ajustes): the four fields and the preview, saved as they're typed; Voltar returns. */
@Composable
private fun NamesEditor(state: AppState, touched: () -> Unit, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    // Android's Back returns to Ajustes, like Voltar (as the period editor does).
    androidx.activity.compose.BackHandler(onBack = onBack)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
        QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
        Text(tx.names, style = OieType.title, color = c.ink)
    }
    // v0.9: the names from setup can be fixed here. A blank field keeps the last name, so nothing is ever left nameless.
    var parent by remember { mutableStateOf(state.parentName) }
    var kid by remember { mutableStateOf(state.kidName) }
    var nicks by remember { mutableStateOf(state.kidNicknames.joinToString(", ")) }
    // Fresh review: the fields stand apart like in setup (24dp), so each help line belongs to its own field.
    Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
        app.oie.ui.setup.Field(tx.yourName, parent, { parent = it; touched(); if (it.isNotBlank()) state.parentName = it.trim() }, tx.yourNameHelp(state.kidName.ifBlank { tx.theKid }, state.parentName), names = true)
        app.oie.ui.setup.Field(tx.kidNameLabel, kid, { kid = it; touched(); if (it.isNotBlank()) state.kidName = it.trim() }, tx.kidNameHelp(state.kidName.ifBlank { tx.theKid }), names = true)
        // v0.21: as many names as the family uses ("Tito, Chico, Fran"); any of them reaches the kid and works in commands.
        app.oie.ui.setup.Field(tx.nicknamesLabel(state.kidName.ifBlank { tx.theKid }), nicks, {
            nicks = it; touched(); state.kidNicknames.clear(); state.kidNicknames.addAll(app.oie.logic.KidNames.parse(it))
        }, tx.nicknamesHelp(state.kidName.ifBlank { tx.theKid }), names = true)
        // v0.22: the kid's name goes out in WhatsApp's bold, after an emoji the parent may choose; the line below shows the result.
        Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
            app.oie.ui.setup.Field(tx.emojiLabel, state.kidEmoji, { touched(); state.kidEmoji = app.oie.logic.Signature.emojiOf(it) }, tx.emojiHelp)
            SignaturePreview(state.kidName.ifBlank { tx.theKid }, state.kidEmoji)
        }
    }
}

/**
 * Ajuda (v0.44; was the second half of Ajustes e ajuda): short answers to the questions the flows raise, the problem
 * report, and Sobre (version, updates, the notices, licences). After Oiê closed by itself, the report leads.
 */
@Composable
private fun Help(state: AppState, offerUndo: Undo, report: ReportView) {
    val c = OieTheme.colors
    val tx = str
    var open by remember { mutableStateOf<Int?>(null) }
    val help = tx.helpItems(state.kidName.ifBlank { tx.theKid }, state.parentName)
    // v0.14: after Oiê closed by itself, the report leads the section (it's what the parent needs now); otherwise it sits after the answers.
    val crashed = remember { app.oie.data.Diagnostics.lastCrashAt() != null }
    if (crashed) Problems(first = true, offerUndo, report)
    Text(tx.questions, style = OieType.title, color = c.ink, modifier = if (crashed) Modifier.padding(top = OTokens.Spacing.space6) else Modifier)
    help.forEachIndexed { i, (q, a) ->
        // v0.34 audit: a 56dp row, and TalkBack hears whether it's open.
        Column(Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin)
            .clickable(role = Role.Button) { open = if (open == i) null else i }
            .semantics { stateDescription = if (open == i) tx.expanded else tx.collapsed }
            .padding(vertical = OTokens.Spacing.space3), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                Text(q, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
                OIcon(R.drawable.gg_ic_chevron, tint = c.muted, modifier = Modifier.rotate(if (open == i) 90f else 0f))
            }
            if (open == i) Text(a, style = OieType.body, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space2))
        }
    }
    if (!crashed) Problems(first = false, offerUndo, report)
    Text(tx.about, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    // v0.34 audit round 2: the version once (the update rows carry it when there's an updater).
    val updater = app.oie.LocalUpdater.current
    if (updater == null) Text(tx.aboutVersion(BuildConfigInfo.VERSION), style = OieType.body, color = c.ink)
    else app.oie.update.UpdateAboutRows(updater)
    Text(tx.notWhatsApp, style = OieType.body, color = c.ink)
    Text(tx.credits, style = OieType.caption, color = c.muted)
}

/** Conexão (v0.12, real WhatsApp only): the status, link or link again, and unlink. */
@Composable
private fun Connection(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val real = LocalRealLink.current
    Text(tx.connection, style = OieType.title, color = c.ink)
    // v0.24: "Conectando…" while the link comes back by itself, with no button to press for nothing.
    val link = app.oie.ui.common.linkShown(state)
    // v0.34 audit round 2: "Conectando…" with the spinner (when animations are on), like every other wait.
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
        if (link == app.oie.logic.LinkView.Shown.CONNECTING && app.oie.ui.common.animationsOn())
            androidx.compose.material3.CircularProgressIndicator(Modifier.size(app.oie.ui.common.ICON), color = c.ink, trackColor = c.line, strokeWidth = OTokens.Size.hairline * 2)
        Text(when (link) {
            app.oie.logic.LinkView.Shown.CONNECTED -> tx.connectedTo(state.parentName)
            app.oie.logic.LinkView.Shown.CONNECTING -> tx.connectingTo(state.parentName)
            app.oie.logic.LinkView.Shown.DOWN -> tx.disconnected
        }, style = OieType.body, color = c.ink)
    }
    if (link == app.oie.logic.LinkView.Shown.DOWN) PrimaryButton(tx.linkConnect, { state.route = Route.Pair })
    // v0.14: a parent who saw Oiê close comes here first; point to the report while there is one.
    if (remember { app.oie.data.Diagnostics.lastCrashAt() != null }) {
        Text(tx.crashPointer, style = OieType.body, color = c.muted, modifier = Modifier.padding(top = OTokens.Spacing.space4))
        TextLink(tx.openReport, { state.route = Route.Parent(3) })
    }
    // Unlinking can't be undone (it takes a new QR scan), so it asks once (v0.13 audit).
    var confirming by remember { mutableStateOf(false) }
    if (state.linkState.on) {
        if (!confirming) TextLink(tx.unlinkReal, { confirming = true })
        else {
            Text(tx.unlinkConfirm(state.kidName.ifBlank { tx.theKid }), style = OieType.body, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space4))
            app.oie.ui.setup.ButtonRow {
                // v0.34 audit: the one destructive action wears the stop fill, with a word (README §3).
                app.oie.ui.common.StopButton(tx.unlinkReal, { confirming = false; real?.unlink() })
                QuietButton(tx.cancel, { confirming = false })
            }
        }
    }
}

/** Problemas (docs/flows/problem-report.md, v0.14): when Oiê last closed by itself, and the report to copy. */
@Composable
private fun Problems(first: Boolean, offerUndo: Undo, report: ReportView) {
    val c = OieTheme.colors
    val tx = str
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) { if (copied) { delay(2_000); copied = false } }
    val crashAt = remember(report.version) { app.oie.data.Diagnostics.lastCrashAt() }
    val hidden = report.hidden
    val empty = remember(report.version, hidden) { app.oie.data.Diagnostics.isEmpty() } || hidden
    Text(tx.problems, style = OieType.title, color = c.ink, modifier = if (first) Modifier else Modifier.padding(top = OTokens.Spacing.space6))
    when {
        crashAt != null && !hidden -> {
            val t = java.time.Instant.ofEpochMilli(crashAt).atZone(java.time.ZoneId.systemDefault())
            Text(tx.closedByItself(t.dayOfMonth, t.monthValue, tx.clock(t.hour, t.minute)), style = OieType.body, color = c.ink)
        }
        empty -> Text(tx.noProblems, style = OieType.body, color = c.muted)
    }
    if (!empty) {
        app.oie.ui.setup.ButtonRow {
            // v0.18: GitHub's own new-issue page, filled in; the parent submits it as themselves.
            val context = androidx.compose.ui.platform.LocalContext.current
            QuietButton(tx.sendToGitHub, {
                val stamp = java.time.LocalDateTime.now()
                val title = tx.issueTitle(app.oie.BuildConfigInfo.VERSION, stamp.dayOfMonth, stamp.monthValue, crashAt != null)
                val url = app.oie.logic.IssueLink.build(title, app.oie.data.Diagnostics.header(), app.oie.data.Diagnostics.lines(), tx::reportCut)
                runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
            })
            QuietButton(if (copied) tx.copied else tx.copyReport, {
                clipboard.setText(androidx.compose.ui.text.AnnotatedString(app.oie.data.Diagnostics.report())); copied = true
            })
            // v0.34 audit: the report is hidden at once and deleted only once the undo is gone.
            TextLink(tx.clearReport, {
                report.hidden = true
                offerUndo(tx.reportCleared, { report.hidden = false }, { app.oie.data.Diagnostics.clear(); report.hidden = false; report.version++ })
            })
        }
        Text(tx.reportPrivacy, style = OieType.caption, color = c.muted)
    }
}

/** "Fica assim no WhatsApp:" and a sample message, with the name in bold as WhatsApp shows it (v0.22). */
@Composable
private fun SignaturePreview(kid: String, emoji: String) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.looksLikeOnWhatsApp, style = OieType.caption, color = c.muted)
    Text(
        androidx.compose.ui.text.buildAnnotatedString {
            append(emoji)
            pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)); append(kid.replace("*", "").trim()); pop()
            append(": " + tx.sampleMessage)
        },
        style = OieType.body, color = c.ink,
        modifier = Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusM)).background(c.surface).padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
    )
}

/** Family statuses (v0.36, docs/flows/statuses.md): off until the parent turns them on. */
@Composable
internal fun StatusSetting(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val kid = state.kidName.ifBlank { tx.theKid }
    Text(tx.statusesTitle, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    app.oie.ui.common.SwitchRow(tx.statusesSwitch(kid), state.statusesOn, { state.statusesOn = it }, help = tx.statusesBody(kid))
    // v0.37: posting, only while Novidades is on.
    if (state.statusesOn) app.oie.ui.common.SwitchRow(tx.postingSwitch(kid), state.postingOn, { state.postingOn = it }, help = tx.postingBody(kid))
}

/** v0.47: the first message in each chat introduces Oiê (on by default); docs/flows/intro-message.md. */
@Composable
internal fun IntroSetting(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val kid = state.kidName.ifBlank { tx.theKid }
    Text(tx.introTitle, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    app.oie.ui.common.SwitchRow(tx.introSwitch, state.introOn, { state.introOn = it }, help = tx.introBody(kid))
}

/** v0.41: the unread mark on the parent's own line, only once that line exists; off unless the parent turns it on. */
@Composable
internal fun UnreadMarkSetting(state: AppState) {
    if (state.allowed.none { it.parentLine }) return
    val c = OieTheme.colors
    val tx = str
    val kid = state.kidName.ifBlank { tx.theKid }
    Text(tx.unreadMarkTitle(kid), style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    app.oie.ui.common.SwitchRow(tx.unreadMarkSwitch, state.markUnreadOn, { state.markUnreadOn = it }, help = tx.unreadMarkBody(kid))
}

/** The gentle nudge (v0.33, docs/flows/family-features.md): off until the parent turns it on. */
@Composable
internal fun NudgeSetting(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val kid = state.kidName.ifBlank { tx.theKid }
    Text(tx.nudgeTitle, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    Text(tx.nudgeBody(kid), style = OieType.caption, color = c.muted)
    app.oie.ui.common.SwitchRow(tx.nudgeSwitch(kid), state.nudgeOn, { state.nudgeOn = it })
}

/**
 * Bolha na tela (docs/flows/bubble.md, v0.25): the switch, and what Android still needs before the
 * bubble can show. Re-read each time Oiê comes back from Android's settings.
 */
@Composable
internal fun BubbleSetting(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val context = androidx.compose.ui.platform.LocalContext.current
    var resumed by remember { mutableStateOf(0) }
    val owner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(owner) {
        val o = androidx.lifecycle.LifecycleEventObserver { _, e -> if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) resumed++ }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }
    val notifications = remember(resumed, state.bubbleOn) { app.oie.data.Bubble.notificationsAllowed(context) }
    val allowed = remember(resumed, state.bubbleOn) { app.oie.data.Bubble.allowed(context) }
    val shown = app.oie.logic.BubblePolicy.state(android.os.Build.VERSION.SDK_INT, state.bubbleOn, notifications, allowed)
    Text(tx.bubbleTitle, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    Text(tx.bubbleBody, style = OieType.caption, color = c.muted)
    if (shown == app.oie.logic.BubblePolicy.State.UNSUPPORTED) { Text(tx.bubbleUnsupported, style = OieType.body, color = c.ink); return }
    // v0.34 audit: the whole row toggles; while Android still has to allow it, the row says so (it's on, not showing yet).
    val waiting = shown == app.oie.logic.BubblePolicy.State.NEEDS_NOTIFICATIONS || shown == app.oie.logic.BubblePolicy.State.NEEDS_PERMISSION
    app.oie.ui.common.SwitchRow(tx.bubbleSwitch, state.bubbleOn,
        { on -> state.bubbleOn = on; if (on) app.oie.data.Bubble.show(context, tx) else app.oie.data.Bubble.hide(context) },
        help = when (shown) {
            app.oie.logic.BubblePolicy.State.NEEDS_NOTIFICATIONS -> tx.bubbleNeedsNotifications
            app.oie.logic.BubblePolicy.State.NEEDS_PERMISSION -> tx.bubbleNeedsPermission
            else -> null
        })
    when (shown) {
        app.oie.logic.BubblePolicy.State.NEEDS_NOTIFICATIONS, app.oie.logic.BubblePolicy.State.NEEDS_PERMISSION -> {
            val forNotifications = shown == app.oie.logic.BubblePolicy.State.NEEDS_NOTIFICATIONS
            // v0.34 audit round 2: the reason is the switch row's own line now (it was said twice).
            QuietButton(tx.bubbleAllow, { runCatching { context.startActivity(app.oie.data.Bubble.settingsIntent(context, notifications)) } })
            // Samsung tablets hide bubbles behind their own "floating notifications" setting.
            if (!forNotifications && android.os.Build.MANUFACTURER.equals("samsung", ignoreCase = true)) Text(tx.bubbleSamsung, style = OieType.caption, color = c.muted)
        }
        app.oie.logic.BubblePolicy.State.ON -> Text(tx.bubbleOnLine, style = OieType.caption, color = c.muted)
        else -> {}
    }
}

/**
 * "Você (Ana)": the line between the kid and the parent who owns the WhatsApp (docs/flows/parent-line.md,
 * v0.30, decision 0007). Says what it creates and WhatsApp's notification limit before the one tap.
 */
@Composable
private fun ParentLineCard(state: AppState, touched: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val real = LocalRealLink.current
    val kid = state.kidName.ifBlank { tx.theKid }
    var creating by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxWidth().padding(vertical = OTokens.Spacing.space3).clip(RoundedCornerShape(OTokens.Radius.radiusM))
            .background(c.surface).padding(OTokens.Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
    ) {
        // v0.34 audit round 2: "Você" alone when the parent's name isn't set (it read "Você (a criança)").
        Text(if (state.parentName.isBlank()) tx.you else tx.parentLineTitle(state.parentName), style = OieType.label, color = c.ink)
        Text(tx.parentLineBody(kid), style = OieType.body, color = c.ink)
        // On a phone the second note is one tap away, so the contacts start higher (less-is-more Q3).
        // Fresh review: folded on the tablet too (three paragraphs were a lot to read before the contacts).
        var whyOpen by remember { mutableStateOf(false) }
        if (whyOpen) Text(tx.parentLineNoNotify, style = OieType.caption, color = c.muted)
        else TextLink(tx.why, { whyOpen = true })
        note?.let { Text(it, style = OieType.body, color = c.ink) }
        QuietButton(if (creating) tx.parentLineCreating else tx.parentLineCreate(kid), {
            if (creating) return@QuietButton
            touched(); creating = true
            real?.createParentLine { group ->
                creating = false
                note = when (group) { null -> tx.parentLineNotNow; false -> tx.parentLineFallback(kid); true -> null }
            } ?: run { creating = false; note = tx.parentLineNotNow }
        }, Modifier.padding(top = OTokens.Spacing.space2), icon = R.drawable.gg_ic_plus)
    }
}
