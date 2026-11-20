package app.oie.ui.kid

import androidx.compose.foundation.background
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
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(state.statusesOn, state.statuses.size) {
        state.expireStatuses().forEach { app.oie.data.Photos.forget(context, it) }
    }
    // v0.34 audit: "Preciso de você" answers in a snackbar at the bottom (announced), not a line that scrolls away.
    var said by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
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
                    Text(tx.whoToTalk, style = OieType.body, color = c.muted)
                    // v0.33: today's birthdays first: they're once a year.
                    HomeReminders(state, birthdays = true)
                    NovidadesRow(state) { watching = it }
                    // v0.34 audit: on a phone, two even columns filling the width; on the tablet, tiles a little wider
                    // than their photo (so long names fit), pulled left by the difference so the first photo starts on
                    // the greeting's edge. Rows sit close on a phone, so the next row peeks above the fold.
                    val compact = app.oie.ui.common.isCompact
                    // Two people with the same first letter ("Vovó", "Vovô") get two letters each, so they differ at a glance.
                    val letters = app.oie.logic.Initials.of(state.reachable.map { it.name })
                    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val tileW = if (compact) (maxWidth - OTokens.Spacing.space4) / 2 else OTokens.Size.contactTile + OTokens.Spacing.space8
                    FlowRow(
                        Modifier.padding(top = OTokens.Spacing.space8).then(if (compact) Modifier else Modifier.offset(x = -OTokens.Spacing.space4)),
                        horizontalArrangement = Arrangement.spacedBy(if (compact) OTokens.Spacing.space4 else OTokens.Spacing.space8),
                        verticalArrangement = Arrangement.spacedBy(if (compact) OTokens.Spacing.space2 else OTokens.Spacing.space6),
                    ) {
                        state.reachable.forEach { ct ->
                            val unread = state.unread(ct.id)
                            val label = ct.name + if (unread > 0) ", " + tx.newCount(unread) else ""
                            Column(
                                Modifier.width(tileW)
                                    .clip(RoundedCornerShape(OTokens.Radius.radiusM))
                                    .pressable { state.route = Route.Conversation(ct.id) }
                                    .semantics(mergeDescendants = true) { contentDescription = label }
                                    .padding(vertical = OTokens.Spacing.space4),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
                            ) {
                                app.oie.ui.common.Avatar(ct, OTokens.Size.contactTile, letters = letters[ct.name])
                                Text(ct.name, style = OieType.label, color = c.ink)
                                if (unread > 0) Text(
                                    tx.newCount(unread), style = OieType.caption, color = c.page,
                                    modifier = Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusL)).background(c.ink)
                                        .padding(horizontal = OTokens.Spacing.space2, vertical = OTokens.Spacing.space1),
                                )
                            }
                        }
                    }
                    }
                    // v0.33 audit: the gentle nudge after the people, so it never pushes them down (calm, not a nag).
                    HomeReminders(state, birthdays = false)
                    // v0.33: only when the parent marked someone to get it.
                    NeedYou(state) { said = it }
                }
            }
        }
        said?.let { app.oie.ui.common.Snackbar(it, Modifier.align(Alignment.BottomCenter)) }
        watching?.let { StatusViewer(state, it, onClose = { watching = null }) }
        if (notice) KidNotice(state)
    }
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
