@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package app.oie.ui.kid

import app.oie.ui.common.pressableSelect

import app.oie.ui.common.pressableToggle

import androidx.compose.foundation.selection.selectable

import androidx.compose.ui.semantics.traversalIndex

import androidx.compose.ui.semantics.isTraversalGroup

import androidx.compose.ui.semantics.heading

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import app.oie.ui.common.pressable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.ui.common.Banner
import app.oie.ui.common.Bar
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.setup.Wordmark
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** Prototype screens 01, 02, 04, 21. Flow: docs/flows/kid-sends-message.md. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KidHome(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val quiet = state.quietNow()
    val notice = !state.kidNoticeSeen && state.allowed.isNotEmpty()
    // v0.36: the family's statuses: the row opens a person's statuses; a day later, or switched off, they go.
    var watching by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    // v0.37: the kid is posting a status.
    var posting by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(state.statusesOn, state.statuses.size) {
        state.expireStatuses().forEach { app.oie.data.Photos.forget(context, it) }
    }
    // v0.34 audit: "Preciso de você" answers in a snackbar at the bottom (announced), not a line that scrolls away.
    var said by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    // v0.42: the kid arranging the home (favorites, order).
    var arranging by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(said) { if (said != null) { kotlinx.coroutines.delay(SAID_MS); said = null } }
    Box(Modifier.fillMaxSize()) {
        // v0.34 audit: while the notice is up, TalkBack can't reach the screen behind it.
        Column(Modifier.fillMaxSize().then(if (notice) Modifier.clearAndSetSemantics { } else Modifier)) {
            Bar {
                Wordmark()
                Box(Modifier.weight(1f))
                QuietButton(tx.parentButton, { state.route = Route.ParentPin }, icon = R.drawable.gg_ic_gear)
            }
            // v0.24: only a link that stayed down for 30 s gets the banner; reconnecting after an update says nothing.
            val link = app.oie.ui.common.linkShown(state)
            // v0.34 audit: both can be true at once (the link down in quiet hours), and both are said.
            // v0.34 audit round 2: a dropped internet says so (it comes back by itself); only a link that needs
            // the parent (logged out, banned, never linked) asks for them.
            // Fresh review: no internet isn't a link problem, so it gets the info icon; the link icon is for "ask the parent".
            if (link == app.oie.logic.LinkView.Shown.DOWN) {
                val parentNeeded = state.linkState.needsParent(state.realMode)
                Banner(if (parentNeeded) R.drawable.gg_ic_link else R.drawable.gg_ic_info, if (parentNeeded) tx.linkDropped(state.parentName) else tx.offlineHome)
            }
            if (quiet != null) Banner(R.drawable.gg_ic_clock, app.oie.ui.common.quietText(tx, quiet, state.parentName, short = false))
            // v0.34 audit (P0): the home scrolls, so every person is reachable on a phone and at large text.
            Column(
                Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(start = app.oie.ui.common.sideMargin, end = app.oie.ui.common.sideMargin, top = OTokens.Spacing.space10, bottom = OTokens.Spacing.space16),
                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) {
                Text(tx.hello(state.kidName), style = OieType.display, color = c.ink)
                // v0.21: a chat the parent paused from their WhatsApp leaves the kid's home until resumed.
                if (state.reachable.isEmpty()) {
                    // Empty: the line says who can fix it; the one button asks them (owner, 2026-11-18). The brand's rounds, once.
                    Text(tx.nobodyYet(state.parentName), style = OieType.body, color = c.muted, modifier = Modifier.widthIn(max = OTokens.Size.contentMax))
                    app.oie.ui.common.Rounds(Modifier.padding(vertical = OTokens.Spacing.space8))
                    if (state.addRequested) Text(tx.askedToAdd(state.parentName), style = OieType.body, color = c.ink)
                    else QuietButton(tx.askToAdd, { state.addRequested = true }, Modifier.padding(top = OTokens.Spacing.space4).height(OTokens.Size.button))
                } else {
                    // v0.42: "Arrumar" beside the question, once there are two people to order.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (arranging) tx.arrangeHint else tx.whoToTalk, style = OieType.body, color = c.muted, modifier = Modifier.weight(1f))
                        if (state.reachable.size > 1) app.oie.ui.common.TextLink(if (arranging) tx.arrangeDone else tx.arrange, { arranging = !arranging },
                            Modifier.padding(start = OTokens.Spacing.space4))
                    }
                    if (arranging) Arrange(state) else {
                    // v0.33: today's birthdays first: they're once a year.
                    HomeReminders(state, birthdays = true)
                    NovidadesRow(state, onOpen = { watching = it }, onPost = { posting = true })
                    // v0.38: a list on a phone (like a chat app's), cards with Escrever and Gravar áudio on the tablet;
                    // each says the last message and when. v0.42: in the kid's order, favorites first; places only move in Arrumar.
                    People(state)
                    // v0.33 audit: the gentle nudge after the people, so it never pushes them down (calm, not a nag).
                    HomeReminders(state, birthdays = false)
                    // v0.33: only when the parent marked someone to get it.
                    NeedYou(state) { said = it }
                    }
                }
            }
        }
        said?.let { app.oie.ui.common.Snackbar(it, Modifier.align(Alignment.BottomCenter)) }
        watching?.let { StatusViewer(state, it, onClose = { watching = null }) }
        if (posting) StatusPoster(state, onClose = { posting = false })
        if (notice) KidNotice(state)
    }
}

/** v0.48: how the home shows people. The kid picks it in Arrumar; until then a phone lists, the tablet shows a mosaic. */
internal enum class HomeView { LIST, PHOTOS, MOSAIC }

@Composable
internal fun homeView(state: AppState): HomeView = when (state.homeView) {
    "list" -> HomeView.LIST; "photos" -> HomeView.PHOTOS; "mosaic" -> HomeView.MOSAIC
    else -> if (app.oie.ui.common.isCompact) HomeView.LIST else HomeView.MOSAIC
}

/** v0.48: "Pessoas" or "Grupos" over a section, only when the home has both (a heading for TalkBack). */
@Composable
private fun SectionTitle(text: String) {
    Text(text, style = androidx.compose.material3.MaterialTheme.typography.titleMediumEmphasized, color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = OTokens.Spacing.space6, start = OTokens.Spacing.space1).semantics { heading() })
}

/**
 * v0.38 (flow kid-home-and-attach.md): every person the kid can reach, with the last message in a few words.
 * v0.48 (the owner: "give the kid options to list the contacts as masonry, gallery, list; separate groups from
 * users"): people and groups are two sections, and the kid picks the view in Arrumar:
 *  - Lista: one row each (photo, name, last message);
 *  - Fotos: a grid of big photos with the name, nothing to read;
 *  - Mosaico: cards in staggered columns, each as tall as its last message (with Escrever and Gravar áudio on the tablet).
 */
@Composable
private fun People(state: AppState) {
    val tx = str
    // Two people with the same first letter ("Vovó", "Vovô") get two letters each, so they differ at a glance.
    val letters = app.oie.logic.Initials.of(state.reachable.map { it.name })
    // v0.45: whoever has news comes first in their section (newest first), highlighted; back to their place once read.
    // v0.50: Novas first (everyone with unread messages, people and groups, newest first), then Pessoas and Grupos.
    val sections = state.homeSections
    val shown = listOf(tx.sectionNew to sections.news, tx.sectionPeople to sections.people, tx.sectionGroups to sections.groups).filter { it.second.isNotEmpty() }
    val titled = shown.size > 1
    val view = homeView(state)
    Column(Modifier.fillMaxWidth().then(if (titled) Modifier else Modifier.padding(top = OTokens.Spacing.space4))) {
        shown.forEach { (title, list) ->
            if (titled) SectionTitle(title)
            Box(Modifier.padding(top = OTokens.Spacing.space3)) {
                when (view) {
                    // v0.52 (decision 0018): Material 3 Expressive's segmented list: each section is one group of rows with
                    // big outer corners and small gaps between them, so each row reads as one thing (the v0.51 complaint).
                    HomeView.LIST -> Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(androidx.compose.material3.ListItemDefaults.SegmentedGap)) {
                        list.forEachIndexed { i, ct -> PersonRow(state, ct, letters[ct.name], i, list.size) }
                    }
                    HomeView.PHOTOS -> PhotoGrid(state, list, letters)
                    HomeView.MOSAIC -> Mosaic(state, list, letters)
                }
            }
        }
    }
}

/** v0.48 Fotos: big photos (or letters) with the name under them; as many columns as fit tiles of at least 128dp. */
@Composable
private fun PhotoGrid(state: AppState, list: List<app.oie.logic.Contact>, letters: Map<String, String>) {
    val c = OieTheme.colors
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = OTokens.Spacing.space3
        val minTile = OTokens.Size.contactTile + OTokens.Spacing.space8
        val columns = ((maxWidth + gap) / (minTile + gap)).toInt().coerceAtLeast(1)
        val tileW = (maxWidth - gap * (columns - 1)) / columns
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) {
            list.forEach { ct ->
                val s = said(state, ct)
                val shape = androidx.compose.material3.MaterialTheme.shapes.large
                val sc = androidx.compose.material3.MaterialTheme.colorScheme
                Column(
                    Modifier.width(tileW).clip(shape).background(if (s.unread > 0) sc.secondaryContainer else sc.surfaceContainer)
                        .pressable { state.route = Route.Conversation(ct.id) }
                        .semantics(mergeDescendants = true) { contentDescription = s.label; role = Role.Button }
                        .padding(OTokens.Spacing.space3),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
                ) {
                    app.oie.ui.common.Avatar(ct, OTokens.Size.contactTile, letters = letters[ct.name], strong = s.unread > 0)
                    Text(ct.name, style = OieType.label, color = if (s.unread > 0) sc.onSecondaryContainer else sc.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    if (s.unread > 0) NewPill(s.unread)
                }
            }
        }
    }
}

/**
 * v0.48 Mosaico: cards in staggered columns (2 on a phone, as many 300dp columns as fit on the tablet), each as tall
 * as its last message. Placed left to right, row by row, and read in that order by TalkBack.
 */
@Composable
private fun Mosaic(state: AppState, list: List<app.oie.logic.Contact>, letters: Map<String, String>) {
    val compact = app.oie.ui.common.isCompact
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = if (compact) OTokens.Spacing.space3 else OTokens.Spacing.space4
        val minColumn = if (compact) OTokens.Size.contentMax / 4 else OTokens.Size.contentMax / 2
        val columns = ((maxWidth + gap) / (minColumn + gap)).toInt().coerceAtLeast(1) // a lone card keeps a column's width
        Row(Modifier.fillMaxWidth().semantics { isTraversalGroup = true }, horizontalArrangement = Arrangement.spacedBy(gap)) {
            (0 until columns).forEach { col ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    list.forEachIndexed { i, ct ->
                        if (i % columns == col) PersonCard(state, ct, letters[ct.name],
                            Modifier.fillMaxWidth().semantics { traversalIndex = i.toFloat() }, mosaic = true, actions = !compact)
                    }
                }
            }
        }
    }
}

/** What a row or card says about a person's chat: the last message, and when. */
private class Said(val preview: String, val whenText: String?, val unread: Int, val label: String, val failed: Boolean)

@Composable
private fun said(state: AppState, ct: app.oie.logic.Contact): Said {
    val tx = str
    // Audit (P2): the chat's messages are filtered once per person, for both the preview and the unread count.
    val seen = state.visible(ct.id)
    val p = app.oie.logic.HomeList.preview(seen, state.failed.toSet())
    val unread = seen.count { !it.fromKid && it.at > state.lastSeen.getOrDefault(ct.id, 0L) }
    val preview = if (p == null) tx.noMessagesYet else {
        val base = when (p.what) {
            app.oie.logic.HomeList.What.TEXT -> p.text
            app.oie.logic.HomeList.What.VOICE -> tx.voicePreview(p.seconds)
            app.oie.logic.HomeList.What.PHOTO -> if (p.text.isEmpty()) tx.photo else tx.photo + " · " + p.text
            app.oie.logic.HomeList.What.VIDEO -> if (p.text.isEmpty()) tx.video else tx.video + " · " + p.text
            app.oie.logic.HomeList.What.GIF -> tx.gifLabel
            app.oie.logic.HomeList.What.STICKER -> tx.sticker
            app.oie.logic.HomeList.What.DELETED -> tx.deletedMessage
            app.oie.logic.HomeList.What.OTHER -> tx.somethingInWhatsApp
        }
        // v0.46: in a group, who said it comes first ("Vovó: Domingo tem bolo"), as in WhatsApp.
        val last = seen.lastOrNull()
        val who = if (ct.isGroup && !ct.parentLine && !p.mine && last != null && !last.fromKid) app.oie.logic.GroupSender.of(last, state.allowed, state.parentName).name else ""
        val own = if (p.mine) tx.mine(base) else if (who.isNotEmpty()) "$who: $base" else base
        if (p.failed) tx.notSent + " · " + own else own
    }
    val whenText = p?.let {
        when (val w = app.oie.logic.HomeList.whenOf(it.at, java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(state.clock()), java.time.ZoneId.systemDefault()))) {
            is app.oie.logic.HomeList.When.Time -> tx.clock(w.hour, w.minute).let { t -> if (w.minute == 0 && t.endsWith("h")) t + "00" else t }
            app.oie.logic.HomeList.When.Yesterday -> tx.yesterday
            is app.oie.logic.HomeList.When.Weekday -> tx.weekday(w.day)
            is app.oie.logic.HomeList.When.Date -> tx.date(w.day, w.month)
        }
    }
    val label = listOfNotNull(ct.name, if (ct.id in state.favorites) tx.favorite else null, if (unread > 0) tx.newCount(unread) else null, preview, whenText).joinToString(", ")
    return Said(preview, whenText, unread, label, p?.failed == true)
}

/**
 * "3 novas" (a word, never a bare dot or colour). v0.52 (decision 0018): Material's badge, in primary with on-primary,
 * the one solid thing on a row with news; the row itself is the soft secondary container.
 */
@Composable
private fun NewPill(n: Int) {
    val sc = androidx.compose.material3.MaterialTheme.colorScheme
    androidx.compose.material3.Badge(containerColor = sc.primary, contentColor = sc.onPrimary) {
        Text(str.newCount(n), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, maxLines = 1,
            modifier = Modifier.padding(horizontal = OTokens.Spacing.space1))
    }
}

/**
 * A row (v0.52, decision 0018): Material's segmented list item. Photo, name (and heart), the last message under it,
 * the time and "2 novas" at the end. A row with news is the secondary container, its name and message bold.
 */
@Composable
private fun PersonRow(state: AppState, ct: app.oie.logic.Contact, letters: String?, index: Int, count: Int) {
    val sc = androidx.compose.material3.MaterialTheme.colorScheme
    val s = said(state, ct)
    val news = s.unread > 0
    val fg = if (news) sc.onSecondaryContainer else sc.onSurface
    val soft = if (news) sc.onSecondaryContainer else sc.onSurfaceVariant
    androidx.compose.material3.SegmentedListItem(
        onClick = { state.route = Route.Conversation(ct.id) },
        shapes = androidx.compose.material3.ListItemDefaults.segmentedShapes(index, count),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = s.label; role = Role.Button },
        colors = androidx.compose.material3.ListItemDefaults.segmentedColors(
            containerColor = if (news) sc.secondaryContainer else sc.surfaceContainer, contentColor = fg, supportingContentColor = soft, trailingContentColor = soft,
        ),
        leadingContent = { app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters, strong = news) },
        supportingContent = {
            // A message that didn't go says "Não foi" in the error colour, as in the chat (a word, never the colour alone).
            Text(s.preview, style = if (news) OieType.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else OieType.bodySmall,
                color = if (s.failed) sc.error else soft, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = if (s.whenText == null && !news) null else ({
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                s.whenText?.let { Text(it, style = OieType.caption, color = soft, maxLines = 1) }
                if (news) NewPill(s.unread)
            }
        }),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
            Text(ct.name, style = if (news) OieType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold) else OieType.label, color = fg,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            if (ct.id in state.favorites) app.oie.ui.common.OIcon(R.drawable.gg_ic_heart, tint = soft)
        }
    }
}

/**
 * The tablet's card: the same words as the phone's row, the last message on up to two lines, and two
 * quiet actions: Escrever (the chat with the keyboard up) and Gravar áudio (the chat, already recording).
 * In quiet hours the actions go, as in the chat; the card still opens it to read and listen.
 */
@Composable
private fun PersonCard(state: AppState, ct: app.oie.logic.Contact, letters: String?, modifier: Modifier, mosaic: Boolean = false, actions: Boolean = true) {
    val c = OieTheme.colors
    val tx = str
    val s = said(state, ct)
    val shape = androidx.compose.material3.MaterialTheme.shapes.large
    val sc = androidx.compose.material3.MaterialTheme.colorScheme
    val canSend = state.canSend(ct) && app.oie.ui.common.linkShown(state) != app.oie.logic.LinkView.Shown.DOWN
    // v0.52 (decision 0018): a Material card; one with news is the secondary container, with the "3 novas" badge.
    val news = s.unread > 0
    val fg = if (news) sc.onSecondaryContainer else sc.onSurface
    val soft = if (news) sc.onSecondaryContainer else sc.onSurfaceVariant
    Column(
        modifier.clip(shape).background(if (news) sc.secondaryContainer else sc.surfaceContainer)
            .padding(OTokens.Spacing.space2),
    ) {
        Column(
            Modifier.fillMaxWidth().clip(androidx.compose.material3.MaterialTheme.shapes.small)
                .pressable { state.route = Route.Conversation(ct.id) }
                .semantics(mergeDescendants = true) { contentDescription = s.label; role = Role.Button }
                .padding(OTokens.Spacing.space2),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            // One height with or without the time under the name, so the cards in a row line up.
            Row(Modifier.heightIn(min = OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
                app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters, strong = news)
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                        Text(ct.name, style = OieType.title, color = fg, maxLines = if (mosaic) 2 else 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        if (ct.id in state.favorites) app.oie.ui.common.OIcon(R.drawable.gg_ic_heart, tint = soft)
                    }
                    s.whenText?.let { Text(it, style = OieType.caption, color = soft, maxLines = 1) }
                }
                if (s.unread > 0 && !mosaic) NewPill(s.unread)
            }
            // Two lines held even when the message is short, so the cards in a row line up.
            // v0.48: in the mosaic a card is as tall as its message (up to 6 lines), so the columns stagger.
            Text(s.preview, style = if (news) OieType.label else OieType.body, color = if (s.failed) sc.error else soft,
                minLines = if (mosaic) 1 else 2, maxLines = if (mosaic) 6 else 2, overflow = TextOverflow.Ellipsis)
            // v0.48 audit (M1): in the mosaic's narrow columns the tag sits under the message, whole, not beside the name.
            if (s.unread > 0 && mosaic) NewPill(s.unread)
        }
        if (canSend && actions) Row(
            Modifier.fillMaxWidth().padding(OTokens.Spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            QuietButton(tx.write, { state.route = Route.Conversation(ct.id, app.oie.data.Start.WRITE) }, Modifier.weight(1f), contentDescription = tx.writeA11y(ct.name))
            QuietButton(tx.recordAudio, { state.route = Route.Conversation(ct.id, app.oie.data.Start.RECORD) }, Modifier.weight(1f), icon = R.drawable.gg_ic_mic, contentDescription = tx.recordA11yTo(ct.name))
        }
    }
}

/**
 * v0.42 (flow kid-home-and-attach.md, "Arrumar"): the kid's own order. Each person has a heart (favorites stay on
 * top), up and down arrows, and the whole row drags after a long press. The arrows are the way without dragging.
 */
@Composable
internal fun Arrange(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val letters = app.oie.logic.Initials.of(state.reachable.map { it.name })
    val people = state.homePeople
    var dragging by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var offset by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var rowPx by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(1) }
    val now = people.map { it.id }
    fun moved(id: String, delta: Int) = app.oie.logic.HomeOrder.move(state.reachable, state.favorites.toSet(), state.kidOrder, id, delta)
    val both = people.any { app.oie.logic.HomeOrder.inGroups(it) } && people.any { !app.oie.logic.HomeOrder.inGroups(it) }
    Column(Modifier.fillMaxWidth().widthIn(max = OTokens.Size.contentMax).padding(top = OTokens.Spacing.space4)) {
        // v0.48: how the home shows people; a rare choice, so it lives here and not on the home.
        ViewChoice(state)
        people.forEachIndexed { i, ct ->
            if (both && (i == 0 || app.oie.logic.HomeOrder.inGroups(people[i - 1]) != app.oie.logic.HomeOrder.inGroups(ct)))
                SectionTitle(if (app.oie.logic.HomeOrder.inGroups(ct)) tx.sectionGroups else tx.sectionPeople)
            val fav = ct.id in state.favorites
            // v0.48: a move stays in its section and its side of the hearts (HomeOrder.move); an arrow that can't move is dimmed.
            val canUp = moved(ct.id, -1) != now
            val canDown = moved(ct.id, +1) != now
            val moving = dragging == ct.id
            Row(
                Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin)
                    .onSizeChanged { rowPx = it.height.coerceAtLeast(1) }
                    .zIndex(if (moving) 1f else 0f)
                    .graphicsLayer { translationY = if (moving) offset else 0f }
                    .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                    .background(if (moving) c.surface else c.page)
                    .pointerInput(ct.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragging = ct.id; offset = 0f },
                            onDragEnd = { dragging = null; offset = 0f },
                            onDragCancel = { dragging = null; offset = 0f },
                        ) { change, drag ->
                            change.consume()
                            offset += drag.y
                            // Past half a row, the person swaps with the neighbour (within their group).
                            if (offset > rowPx / 2f) { val before = state.homePeople.indexOfFirst { it.id == ct.id }; state.moveOnHome(ct.id, +1); if (state.homePeople.indexOfFirst { it.id == ct.id } != before) offset -= rowPx }
                            else if (offset < -rowPx / 2f) { val before = state.homePeople.indexOfFirst { it.id == ct.id }; state.moveOnHome(ct.id, -1); if (state.homePeople.indexOfFirst { it.id == ct.id } != before) offset += rowPx }
                        }
                    }
                    .padding(vertical = OTokens.Spacing.space1),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) {
                app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters[ct.name])
                Column(Modifier.weight(1f).padding(start = OTokens.Spacing.space2)) {
                    Text(ct.name, style = OieType.label, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // The word says it, not only the heart's colour.
                    if (fav) Text(tx.favorite, style = OieType.caption, color = c.muted, maxLines = 1)
                }
                SquareIcon(R.drawable.gg_ic_heart, tx.favoriteA11y(ct.name), selected = fav) { state.toggleFavorite(ct.id) }
                SquareIcon(R.drawable.gg_ic_chevron, tx.moveUpA11y(ct.name), rotation = -90f, enabled = canUp) { state.moveOnHome(ct.id, -1) }
                SquareIcon(R.drawable.gg_ic_chevron, tx.moveDownA11y(ct.name), rotation = 90f, enabled = canDown) { state.moveOnHome(ct.id, +1) }
            }
        }
    }
}

/**
 * v0.48: "Mostrar como" with Lista, Fotos and Mosaico, one picked. v0.52 (decision 0018): Material 3 Expressive's
 * connected button group; the picked one is filled and round, with a check (the word and the check say which).
 */
@Composable
private fun ViewChoice(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    val current = homeView(state)
    Text(tx.showAs, style = OieType.label, color = c.ink, modifier = Modifier.padding(bottom = OTokens.Spacing.space2))
    val options = listOf(HomeView.LIST to tx.viewList, HomeView.PHOTOS to tx.viewPhotos, HomeView.MOSAIC to tx.viewMosaic)
    Row(Modifier.fillMaxWidth().padding(bottom = OTokens.Spacing.space4).semantics { isTraversalGroup = true },
        horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { i, (v, label) ->
            val on = v == current
            androidx.compose.material3.ToggleButton(
                checked = on, onCheckedChange = { state.homeView = v.name.lowercase() },
                modifier = Modifier.weight(1f).heightIn(min = androidx.compose.material3.ButtonDefaults.MediumContainerHeight).semantics { role = Role.RadioButton },
                shapes = when (i) {
                    0 -> androidx.compose.material3.ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> androidx.compose.material3.ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> androidx.compose.material3.ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                if (on) { app.oie.ui.common.OIcon(R.drawable.gg_ic_check, tint = androidx.compose.material3.LocalContentColor.current, size = androidx.compose.material3.ToggleButtonDefaults.IconSize)
                    androidx.compose.foundation.layout.Spacer(Modifier.width(androidx.compose.material3.ToggleButtonDefaults.IconSpacing)) }
                Text(label, maxLines = 1)
            }
        }
    }
}

/** A Material icon button (56dp). [selected] makes it an outlined toggle (filled when on); a disabled one is dimmed and says so. */
@Composable
private fun SquareIcon(icon: Int, label: String, tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified, rotation: Float = 0f,
                       enabled: Boolean = true, selected: Boolean? = null, onClick: () -> Unit) {
    // v0.52 audit: Material's small size on a phone (touch area still 48dp), so the names beside them aren't cut.
    val compact = app.oie.ui.common.isCompact
    val size = if (compact) androidx.compose.material3.IconButtonDefaults.smallContainerSize() else androidx.compose.material3.IconButtonDefaults.mediumContainerSize()
    val glyphSize = if (compact) androidx.compose.material3.IconButtonDefaults.smallIconSize else androidx.compose.material3.IconButtonDefaults.mediumIconSize
    val glyph: @Composable () -> Unit = { androidx.compose.material3.Icon(androidx.compose.ui.res.painterResource(icon), label, Modifier.rotate(rotation).size(glyphSize)) }
    if (selected != null) androidx.compose.material3.OutlinedIconToggleButton(checked = selected, onCheckedChange = { onClick() }, modifier = Modifier.size(size), enabled = enabled,
        shapes = androidx.compose.material3.IconButtonDefaults.toggleableShapes()) { glyph() }
    else androidx.compose.material3.OutlinedIconButton(onClick = onClick, modifier = Modifier.size(size), enabled = enabled, shapes = androidx.compose.material3.IconButtonDefaults.shapes()) { glyph() }
}

/** How long the "Preciso de você" answer stays. */
private const val SAID_MS = 6_000L

/** Prototype screen 04: said once, plainly, never a threat (01 Brand/voice.md). */
@Composable
private fun KidNotice(state: AppState) {
    val c = OieTheme.colors
    val tx = str
    // Said once, and only "Entendi" closes it: the scrim takes taps without closing (product law: never dismissed by accident).
    Box(Modifier.fillMaxSize().background(c.scrim).pointerInput(Unit) { detectTapGestures { } }, contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth()
                .clip(androidx.compose.material3.BottomSheetDefaults.ExpandedShape)
                .background(androidx.compose.material3.BottomSheetDefaults.ContainerColor).padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(tx.noticeTitle, style = OieType.title, color = c.ink)
            Text(tx.noticeBody(state.parentName), style = OieType.body, color = c.ink)
            PrimaryButton(tx.understood, { state.kidNoticeSeen = true }, Modifier.fillMaxWidth())
        }
    }
}
