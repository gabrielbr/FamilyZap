package app.oie.ui.kid

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
            if (quiet != null) Banner(R.drawable.gg_ic_clock, tx.quietUntil(quiet.end.hour))
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

/**
 * v0.38 (flow kid-home-and-attach.md): every person the kid can reach, with the last message in a few words.
 * A phone lists them in rows; the tablet shows cards in even columns, each with Escrever and Gravar áudio.
 */
@Composable
private fun People(state: AppState) {
    // Two people with the same first letter ("Vovó", "Vovô") get two letters each, so they differ at a glance.
    val letters = app.oie.logic.Initials.of(state.reachable.map { it.name })
    val people = state.homePeople
    if (app.oie.ui.common.isCompact) {
        Column(Modifier.fillMaxWidth().padding(top = OTokens.Spacing.space4)) {
            people.forEach { ct -> PersonRow(state, ct, letters[ct.name]) }
        }
        return
    }
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth().padding(top = OTokens.Spacing.space6)) {
        // Cards at least half the reading column wide (300dp), as many as fit: 3 on a landscape tablet, 2 standing.
        val gap = OTokens.Spacing.space4
        val columns = ((maxWidth + gap) / (OTokens.Size.contentMax / 2 + gap)).toInt().coerceAtLeast(1)
        val cardW = (maxWidth - gap * (columns - 1)) / columns
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) {
            people.forEach { ct -> PersonCard(state, ct, letters[ct.name], Modifier.width(cardW)) }
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
        val own = if (p.mine) tx.mine(base) else base
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

/** "3 novas": ink ground, page text (a word, never a bare dot or colour). */
@Composable
private fun NewPill(n: Int) {
    val c = OieTheme.colors
    Text(
        str.newCount(n), style = OieType.caption, color = c.page, maxLines = 1,
        modifier = Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusL)).background(c.ink)
            .padding(horizontal = OTokens.Spacing.space2, vertical = OTokens.Spacing.space1),
    )
}

/** A phone's row: photo, name and when on top, the last message under it, "2 novas" at the end. One tap opens the chat. */
@Composable
private fun PersonRow(state: AppState, ct: app.oie.logic.Contact, letters: String?) {
    val c = OieTheme.colors
    val s = said(state, ct)
    Row(
        Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin + OTokens.Spacing.space4)
            .clip(RoundedCornerShape(OTokens.Radius.radiusM))
            .pressable { state.route = Route.Conversation(ct.id) }
            .semantics(mergeDescendants = true) { contentDescription = s.label; role = Role.Button }
            .padding(vertical = OTokens.Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) {
        app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                Text(ct.name, style = OieType.label, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (ct.id in state.favorites) app.oie.ui.common.OIcon(R.drawable.gg_ic_heart, tint = c.muted)
                Box(Modifier.weight(1f))
                s.whenText?.let { Text(it, style = OieType.caption, color = if (s.unread > 0) c.ink else c.muted, maxLines = 1) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                // A message that didn't go says "Não foi" in the error colour, as in the chat (a word, never the colour alone).
                Text(s.preview, style = if (s.unread > 0) OieType.label else OieType.bodySmall, color = if (s.failed) c.coralText else if (s.unread > 0) c.ink else c.muted,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (s.unread > 0) NewPill(s.unread)
            }
        }
    }
}

/**
 * The tablet's card: the same words as the phone's row, the last message on up to two lines, and two
 * quiet actions: Escrever (the chat with the keyboard up) and Gravar áudio (the chat, already recording).
 * In quiet hours the actions go, as in the chat; the card still opens it to read and listen.
 */
@Composable
private fun PersonCard(state: AppState, ct: app.oie.logic.Contact, letters: String?, modifier: Modifier) {
    val c = OieTheme.colors
    val tx = str
    val s = said(state, ct)
    val shape = RoundedCornerShape(OTokens.Radius.radiusM)
    val canSend = state.canSend(ct) && app.oie.ui.common.linkShown(state) != app.oie.logic.LinkView.Shown.DOWN
    Column(
        modifier.clip(shape).background(c.surface).border(OTokens.Size.hairline, c.line, shape)
            .padding(OTokens.Spacing.space2),
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(OTokens.Radius.radiusS))
                .pressable { state.route = Route.Conversation(ct.id) }
                .semantics(mergeDescendants = true) { contentDescription = s.label; role = Role.Button }
                .padding(OTokens.Spacing.space2),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            // One height with or without the time under the name, so the cards in a row line up.
            Row(Modifier.heightIn(min = OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
                app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters)
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                        Text(ct.name, style = OieType.title, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        if (ct.id in state.favorites) app.oie.ui.common.OIcon(R.drawable.gg_ic_heart, tint = c.muted)
                    }
                    s.whenText?.let { Text(it, style = OieType.caption, color = c.muted, maxLines = 1) }
                }
                if (s.unread > 0) NewPill(s.unread)
            }
            // Two lines held even when the message is short, so the cards in a row line up.
            Text(s.preview, style = if (s.unread > 0) OieType.label else OieType.body, color = if (s.failed) c.coralText else if (s.unread > 0) c.ink else c.muted,
                minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (canSend) Row(
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
    Column(Modifier.fillMaxWidth().widthIn(max = OTokens.Size.contentMax).padding(top = OTokens.Spacing.space4)) {
        people.forEachIndexed { i, ct ->
            val fav = ct.id in state.favorites
            val favCount = people.count { it.id in state.favorites }
            val canUp = i > 0 && (fav || i > favCount)
            val canDown = i < people.size - 1 && (!fav || i < favCount - 1)
            val moving = dragging == ct.id
            Row(
                Modifier.fillMaxWidth().heightIn(min = OTokens.Size.rowMin)
                    .onSizeChanged { rowPx = it.height.coerceAtLeast(1) }
                    .zIndex(if (moving) 1f else 0f)
                    .graphicsLayer { translationY = if (moving) offset else 0f }
                    .clip(RoundedCornerShape(OTokens.Radius.radiusM))
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
                SquareIcon(R.drawable.gg_ic_heart, tx.favoriteA11y(ct.name), tint = if (fav) c.ink else c.muted, selected = fav) { state.toggleFavorite(ct.id) }
                SquareIcon(R.drawable.gg_ic_chevron, tx.moveUpA11y(ct.name), rotation = -90f, enabled = canUp) { state.moveOnHome(ct.id, -1) }
                SquareIcon(R.drawable.gg_ic_chevron, tx.moveDownA11y(ct.name), rotation = 90f, enabled = canDown) { state.moveOnHome(ct.id, +1) }
            }
        }
    }
}

/** A 52dp square button with one icon (quiet style). [selected] makes it a toggle for TalkBack; a disabled one is dimmed and says so. */
@Composable
private fun SquareIcon(icon: Int, label: String, tint: androidx.compose.ui.graphics.Color = OieTheme.colors.ink, rotation: Float = 0f,
                       enabled: Boolean = true, selected: Boolean? = null, onClick: () -> Unit) {
    val c = OieTheme.colors
    val shape = RoundedCornerShape(OTokens.Radius.radiusS)
    Box(
        Modifier.size(OTokens.Size.button).clip(shape)
            .background(if (selected == true) c.selected else c.surface)
            .border(OTokens.Size.hairline, c.line, shape)
            .then(if (selected != null) Modifier.toggleable(value = selected, enabled = enabled, role = Role.Switch) { onClick() }
                  else Modifier.pressable(enabled = enabled, onClick = onClick))
            .semantics { contentDescription = label; if (!enabled) disabled() }
            .graphicsLayer { alpha = if (enabled) 1f else 0.38f },
        contentAlignment = Alignment.Center,
    ) { app.oie.ui.common.OIcon(icon, Modifier.rotate(rotation), tint = tint) }
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
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(tx.noticeTitle, style = OieType.title, color = c.ink)
            Text(tx.noticeBody(state.parentName), style = OieType.body, color = c.ink)
            PrimaryButton(tx.understood, { state.kidNoticeSeen = true }, Modifier.fillMaxWidth())
        }
    }
}
