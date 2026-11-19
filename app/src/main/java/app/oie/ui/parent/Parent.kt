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
        val sectionContent: @Composable () -> Unit = {
            when (section) {
                0 -> Contacts(state) { lastTouch = System.currentTimeMillis() }
                1 -> QuietSection(state) { lastTouch = System.currentTimeMillis() }
                2 -> Connection(state)
                else -> AboutAndHelp(state)
            }
        }
        val sections = listOf(tx.sectionContacts to R.drawable.gg_ic_people, tx.sectionQuiet to R.drawable.gg_ic_clock, tx.sectionConnection to R.drawable.gg_ic_link, tx.sectionHelp to R.drawable.gg_ic_info)
        @Composable
        fun tab(i: Int, label: String, ic: Int, modifier: Modifier) {
            val on = i == section
            Row(
                modifier.height(OTokens.Size.rowMin).clip(RoundedCornerShape(OTokens.Radius.radiusS))
                    .background(if (on) c.selected else c.page)
                    .clickable(role = Role.Tab) { lastTouch = System.currentTimeMillis(); state.route = Route.Parent(i) }
                    .padding(horizontal = OTokens.Spacing.space4),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            ) { OIcon(ic, tint = if (on) c.onSelected else c.ink); Text(label, style = OieType.label, color = if (on) c.onSelected else c.ink, maxLines = 1) }
        }
        val compact = app.oie.ui.common.isCompact
        // A tablet: sections on the left, the section on the right. A phone (v0.13 audit): the sections in a
        // scrolling row on top, the section below, full width.
        val body: @Composable (Modifier) -> Unit = { m ->
            Column(
                m.verticalScroll(rememberScrollState())
                    .padding(horizontal = if (compact) OTokens.Spacing.space4 else OTokens.Spacing.space12, vertical = if (compact) OTokens.Spacing.space6 else OTokens.Spacing.space8),
                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) { sectionContent() }
        }
        if (compact) Column(Modifier.fillMaxSize()) {
            // The open section is always in view (v0.14 audit: it could sit scrolled off the edge).
            val tabs = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = section)
            LaunchedEffect(section) { tabs.animateScrollToItem(section) }
            androidx.compose.foundation.lazy.LazyRow(
                Modifier.fillMaxWidth(), state = tabs,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(OTokens.Spacing.space2),
                horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1),
            ) {
                items(sections.size) { i -> tab(i, sections[i].first, sections[i].second, Modifier) }
            }
            Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
            body(Modifier.fillMaxWidth())
        } else Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(OTokens.Size.contentMax / 2).fillMaxHeight().padding(OTokens.Spacing.space4), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                sections.forEachIndexed { i, (label, ic) -> tab(i, label, ic, Modifier.fillMaxWidth()) }
            }
            Box(Modifier.width(OTokens.Size.hairline).fillMaxHeight().background(c.line))
            body(Modifier.widthIn(max = OTokens.Size.contentMax * 3 / 2))
        }
    }
}
@Composable
private fun Contacts(state: AppState, touched: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    var removed by remember { mutableStateOf<Pair<Int, Contact>?>(null) }
    LaunchedEffect(removed) { if (removed != null) { delay(5_000); removed = null } }
    // v0.19: the name the kid sees, edited in place of the list, like a quiet period.
    var editing by remember { mutableStateOf<String?>(null) }
    val fresh = state.wa.contacts()
    state.allowed.indexOfFirst { it.id == editing }.takeIf { it >= 0 }?.let { i ->
        ContactEditor(state.allowed[i], state.kidName.ifBlank { tx.theKid }, fresh,
            onSave = { touched(); state.allowed[i] = it; editing = null }, onCancel = { editing = null },
            onRemove = { touched(); val ct = state.allowed.removeAt(i); removed = i to ct; editing = null }, touched = touched)
        return
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(tx.whoCanCall(state.kidName), style = OieType.title, color = c.ink)
            Text(tx.peopleAndGroups(state.allowed.count { !it.isGroup }, state.allowed.count { it.isGroup }), style = OieType.caption, color = c.muted)
            if (state.allowed.isNotEmpty() && !state.contactTipUsed) Text(tx.renameHint(state.kidName.ifBlank { tx.theKid }), style = OieType.caption, color = c.muted)
        }
    }
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
            Initial(ct.name, OTokens.Size.avatar)
            // The switch is always labelled, on or off, right under the name, so it fits a phone too (v0.13 audit).
            Column(Modifier.weight(1f).padding(vertical = OTokens.Spacing.space2)) {
                Text(ct.name, style = OieType.label, color = c.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                // v0.19: the number, so the parent knows it's the right person.
                ContactLines(ct, fresh)
                // The line says the current setting, so it's true whichever way the switch is.
                Text(if (ct.quietException) tx.canTalkInQuiet else tx.onlyOutsideQuiet, style = OieType.caption, color = c.muted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            }
            Switch(
                modifier = Modifier.semantics { contentDescription = tx.canTalkInQuietA11y(ct.name) },
                checked = ct.quietException,
                onCheckedChange = { touched(); state.allowed[i] = ct.copy(quietException = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
            // v0.19 audit: Remover moved into the contact's editor (undo instead of confirm, flow step 6), so a phone row fits the number.
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
    // v0.12: the parent's contacts arrive a little after linking; reload them each time Contatos opens.
    val real = LocalRealLink.current
    LaunchedEffect(Unit) { real?.loadContacts() }
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
                Initial(ct.name, OTokens.Size.avatar)
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
    // The title and its button side by side on a tablet; stacked on a phone, so the title doesn't wrap (v0.13 audit).
    val header: @Composable () -> Unit = {
        Text(tx.quietTitle, style = OieType.title, color = c.ink)
        Text(tx.quietParentBody(state.kidName), style = OieType.caption, color = c.muted)
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


/** Ajustes e ajuda (was Ajuda e sobre): the names (v0.9), short answers to the questions the flows raise, the risk said again, version and licences. */
@Composable
private fun AboutAndHelp(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    var open by remember { mutableStateOf<Int?>(null) }
    val help = tx.helpItems(state.kidName.ifBlank { tx.theKid }, state.parentName)
    // v0.14: after Oiê closed by itself, the report leads the section (it's what the parent needs now); otherwise it sits at the end.
    val crashed = remember { app.oie.data.Diagnostics.lastCrashAt() != null }
    if (crashed) Problems(first = true)
    // v0.9: the names from setup can be fixed here. A blank field keeps the last name, so nothing is ever left nameless.
    Text(tx.names, style = OieType.title, color = c.ink, modifier = if (crashed) Modifier.padding(top = OTokens.Spacing.space6) else Modifier)
    var parent by remember { mutableStateOf(state.parentName) }
    var kid by remember { mutableStateOf(state.kidName) }
    app.oie.ui.setup.Field(tx.yourName, parent, { parent = it; if (it.isNotBlank()) state.parentName = it.trim() }, tx.yourNameHelp(state.kidName.ifBlank { tx.theKid }, state.parentName))
    app.oie.ui.setup.Field(tx.kidNameLabel, kid, { kid = it; if (it.isNotBlank()) state.kidName = it.trim() }, tx.kidNameHelp(state.kidName.ifBlank { tx.theKid }))
    Text(tx.help, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    help.forEachIndexed { i, (q, a) ->
        Column(Modifier.fillMaxWidth().clickable(role = Role.Button) { open = if (open == i) null else i }.padding(vertical = OTokens.Spacing.space3)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                Text(q, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
                OIcon(R.drawable.gg_ic_chevron, tint = c.muted, modifier = Modifier.rotate(if (open == i) 90f else 0f))
            }
            if (open == i) Text(a, style = OieType.body, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space2))
        }
    }
    if (!crashed) Problems(first = false)
    Text(tx.about, style = OieType.title, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    Text(tx.aboutVersion(BuildConfigInfo.VERSION), style = OieType.body, color = c.ink)
    app.oie.LocalUpdater.current?.let { app.oie.update.UpdateAboutRows(it) }
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
    Text(if (state.wa.connected) tx.connectedTo(state.parentName) else tx.disconnected, style = OieType.body, color = c.ink)
    if (!state.wa.connected) PrimaryButton(tx.linkConnect, { state.route = Route.Pair })
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
                QuietButton(tx.unlinkReal, { confirming = false; real?.unlink() })
                TextLink(tx.cancel, { confirming = false })
            }
        }
    }
}

/** Problemas (docs/flows/problem-report.md, v0.14): when Oiê last closed by itself, and the report to copy. */
@Composable
private fun Problems(first: Boolean) {
    val c = OieTheme.colors
    val tx = str
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    var version by remember { mutableStateOf(0) } // re-reads after Apagar
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) { if (copied) { delay(2_000); copied = false } }
    val crashAt = remember(version) { app.oie.data.Diagnostics.lastCrashAt() }
    val empty = remember(version) { app.oie.data.Diagnostics.isEmpty() }
    Text(tx.problems, style = OieType.title, color = c.ink, modifier = if (first) Modifier else Modifier.padding(top = OTokens.Spacing.space6))
    when {
        crashAt != null -> {
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
            TextLink(tx.clearReport, { app.oie.data.Diagnostics.clear(); version++ })
        }
        Text(tx.reportPrivacy, style = OieType.caption, color = c.muted)
    }
}
