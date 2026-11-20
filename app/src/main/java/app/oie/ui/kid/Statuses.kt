package app.oie.ui.kid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Photos
import app.oie.logic.Kind
import app.oie.logic.Reactions
import app.oie.logic.Status
import app.oie.logic.Statuses
import app.oie.ui.common.Bar
import app.oie.ui.common.OIcon
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.pressable
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import kotlinx.coroutines.delay

/** Family statuses on the kid's side (docs/flows/statuses.md, v0.36). */

/**
 * "Novidades" on the kid's home: a round per person with a live status, ringed in coral while something
 * is unseen (with "nova" in words), a hairline once seen. Nothing when there's nothing.
 * v0.37: with posting on, it always shows: "Você" (the kid's own, once posted) and "Postar" come first.
 */
@Composable
internal fun NovidadesRow(state: AppState, onOpen: (String) -> Unit, onPost: () -> Unit = {}) {
    val c = OieTheme.colors
    val tx = str
    if (!state.statusesOn) return
    val row = Statuses.row(state.statuses, state.reachable, state.clock())
    val posting = state.postingOn
    val mine = state.myStatuses()
    if (row.isEmpty() && !posting && mine.isEmpty()) return
    val letters = app.oie.logic.Initials.of(state.reachable.map { it.name })
    Text(tx.novidades, style = OieType.label, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
        if (mine.isNotEmpty()) StatusRound(tx.you, tx.watchStatus(tx.you), tx.you, Ring.SEEN, { onOpen(Statuses.MINE) }) {
            app.oie.ui.common.Avatar(app.oie.logic.Contact(Statuses.MINE, state.kidName), OTokens.Size.avatar)
        }
        if (posting) StatusRound(tx.postStatus, tx.postA11y, tx.postA11y, Ring.NONE, onPost) {
            Box(Modifier.size(OTokens.Size.avatar).clip(CircleShape).background(c.selected), contentAlignment = Alignment.Center) {
                OIcon(R.drawable.gg_ic_plus)
            }
        }
        row.forEach { (ct, unseen) ->
            StatusRound(ct.name, tx.watchStatus(ct.name), if (unseen) tx.statusNewA11y(ct.name) else ct.name, if (unseen) Ring.UNSEEN else Ring.SEEN, { onOpen(ct.id) },
                note = if (unseen) tx.statusNew else null) {
                app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters[ct.name])
            }
        }
    }
}

private enum class Ring { UNSEEN, SEEN, NONE }

/** One round of the row: the ring (coral and thick while unseen, a hairline once seen), the name, and "nova" in words. */
@Composable
private fun StatusRound(name: String, action: String, said: String, ring: Ring, onClick: () -> Unit, note: String? = null, inner: @Composable () -> Unit) {
    val c = OieTheme.colors
    Column(
        Modifier.widthIn(min = OTokens.Size.contactTile).clip(RoundedCornerShape(OTokens.Radius.radiusM))
            .pressable(label = action, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = said }
            .padding(vertical = OTokens.Spacing.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1),
    ) {
        Box(
            Modifier.size(OTokens.Size.avatar + OTokens.Spacing.space3).clip(CircleShape).let {
                when (ring) {
                    Ring.UNSEEN -> it.border(OTokens.Size.hairline * 3, c.mint, CircleShape)
                    Ring.SEEN -> it.border(OTokens.Size.hairline, c.muted, CircleShape)
                    Ring.NONE -> it
                }
            },
            contentAlignment = Alignment.Center,
        ) { inner() }
        Text(name, style = OieType.caption, color = c.ink, maxLines = 1)
        if (note != null) Text(note, style = OieType.caption, color = c.muted)
    }
}

/**
 * One person's statuses, full screen: a segment per status, photos and texts for 5 s, videos to their end;
 * the right half goes on, the left half back; Pausar holds; reactions and Responder below. Each shown
 * status is marked viewed. [paused] and [replying] let the screenshot tests show those states.
 */
@Composable
internal fun StatusViewer(state: AppState, contactId: String, onClose: () -> Unit, startReplying: Boolean = false) {
    val c = OieTheme.colors
    val tx = str
    androidx.activity.compose.BackHandler(onBack = onClose)
    val own = contactId == Statuses.MINE
    // v0.37: the kid's own statuses open here too, as "Você".
    val contact = if (own) app.oie.logic.Contact(Statuses.MINE, tx.you) else state.reachable.firstOrNull { it.id == contactId }
    val context = androidx.compose.ui.platform.LocalContext.current
    val mine = Statuses.of(state.statuses, contactId, state.clock())
    if (contact == null || mine.isEmpty()) { LaunchedEffect(Unit) { onClose() }; return }
    var index by remember(contactId) { mutableIntStateOf(Statuses.startAt(mine)) }
    val current = mine[index.coerceIn(0, mine.lastIndex)]
    var paused by remember { mutableStateOf(false) }
    var replying by remember { mutableStateOf(startReplying) }
    var draft by remember { mutableStateOf("") }
    var said by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(said) { if (said != null) { delay(SAID_MS); said = null } }
    val holding = paused || replying
    // Waiting for the picture or video: it holds there (nothing moves on before the kid has seen it).
    val arriving = current.media == null && current.kind in setOf(Kind.PHOTO, Kind.VIDEO, Kind.GIF) && app.oie.logic.Moving.download(current.kind, current.size)
    val tooBig = current.kind == Kind.VIDEO && !app.oie.logic.Moving.download(current.kind, current.size)
    fun next() { if (index < mine.lastIndex) index++ else onClose() }
    LaunchedEffect(current.id) { if (!own) state.viewStatus(current.id) }
    // The clock for photos, texts and the "too big" line; a video moves on at its own end.
    LaunchedEffect(current.id, holding, arriving) {
        if (holding || arriving || (current.kind == Kind.VIDEO && !tooBig)) return@LaunchedEffect
        delay(Statuses.SHOW_MS); next()
    }
    val linkDown = app.oie.ui.common.linkShown(state) == app.oie.logic.LinkView.Shown.DOWN
    val canSend = state.canSend(contact) && !linkDown
    Box(Modifier.fillMaxSize().background(c.page)) {
        Column(Modifier.fillMaxSize()) {
            // A segment per status: past ones full, the current one marked, the rest a hairline.
            Row(Modifier.fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space2), horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                mine.forEachIndexed { i, _ ->
                    Box(Modifier.weight(1f).height(OTokens.Spacing.space1).clip(RoundedCornerShape(OTokens.Radius.radiusS))
                        .background(if (i <= index) c.ink else c.line))
                }
            }
            Bar {
                QuietButton(null, onClose, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
                if (own) app.oie.ui.common.Avatar(app.oie.logic.Contact(Statuses.MINE, state.kidName), OTokens.Size.avatar)
                else app.oie.ui.common.Avatar(contact, OTokens.Size.avatar, letters = app.oie.logic.Initials.of(state.reachable.map { it.name })[contact.name])
                Column(Modifier.weight(1f)) {
                    Text(contact.name, style = OieType.title, color = c.ink, maxLines = 1)
                    Text(tx.ago(((state.clock() - current.at) / 60_000).toInt()), style = OieType.caption, color = c.muted)
                }
                QuietButton(if (paused) tx.resume else tx.pause, { paused = !paused })
            }
            // The status, with the two halves as next and previous (TalkBack gets them as buttons).
            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space4), contentAlignment = Alignment.Center) {
                StatusContent(current, arriving, tooBig, state.parentName, playing = !holding, onEnd = ::next)
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxSize().pressable(role = Role.Button, label = tx.previous) { if (index > 0) index-- }.semantics { contentDescription = tx.previous })
                    Box(Modifier.weight(1f).fillMaxSize().pressable(role = Role.Button, label = tx.nextOne) { next() }.semantics { contentDescription = tx.nextOne })
                }
            }
            if (current.text.isNotBlank() && current.kind != Kind.TEXT) Text(current.text, style = OieType.body, color = c.ink, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin))
            Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
            Column(Modifier.fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space3), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
                if (own) {
                    // v0.37: the kid's own: who sees it, how the post went, and Apagar para todos (no reactions, no reply).
                    val names = state.allowed.filter { it.id in current.audience }.map { it.name }
                    Text(
                        when { current.posting -> tx.posting; current.failed -> tx.postFailed; else -> tx.whoSees(tx.and(names)) },
                        style = OieType.body, color = if (current.failed) c.ink else c.muted,
                        modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite },
                    )
                    if (linkDown) Text(tx.askReconnect(state.parentName), style = OieType.body, color = c.muted)
                    else app.oie.ui.setup.ButtonRow {
                        if (current.failed) QuietButton(tx.tryAgain, { state.retryStatus(current.id) })
                        // Audit (P2): quiet, not the filled stop: deleting isn't what this screen is for, and a kid taps fills first.
                        QuietButton(tx.deleteForAll, {
                            state.deleteMyStatus(current.id)?.let { Photos.forget(context, it) }
                            said = tx.statusDeleted
                            if (index > 0) index--
                        })
                    }
                } else if (!canSend) {
                    // As in a chat: why the kid can't answer now.
                    val q = state.quietNow()
                    Text(if (linkDown) tx.askReconnect(state.parentName) else tx.quietUntilShort(q?.end?.hour ?: 0), style = OieType.body, color = c.muted)
                } else if (replying) {
                    app.oie.ui.setup.Field(tx.replyTo(contact.name), draft, { draft = it }, "")
                    app.oie.ui.setup.ButtonRow {
                        PrimaryButton(tx.send, {
                            if (state.replyToStatus(current.id, draft, tx.statusReplyQuote) != null) {
                                said = tx.sentTo(contact.name); draft = ""; replying = false
                            }
                        }, enabled = draft.isNotBlank(), icon = R.drawable.gg_ic_send)
                        QuietButton(tx.cancel, { replying = false })
                    }
                } else androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
                    // Six reactions and Responder fit one line on a tablet; on a phone Responder goes below them.
                    val narrow = maxWidth < OTokens.Size.contentMax
                    val reactions: @Composable () -> Unit = { Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
                    Reactions.CHOICES.forEach { e ->
                        val picked = current.reaction == e
                        Box(
                            Modifier.size(OTokens.Spacing.space12).clip(CircleShape).background(if (picked) c.selected else c.page)
                                .pressable(label = tx.reactWith(e)) { state.reactToStatus(current.id, e) }
                                .semantics { contentDescription = tx.reactWith(e); selected = picked },
                            contentAlignment = Alignment.Center,
                        ) { Text(e, style = OieType.title) }
                    }
                    } }
                    if (narrow) Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) { reactions(); QuietButton(tx.reply, { replying = true }) }
                    else Row(verticalAlignment = Alignment.CenterVertically) { reactions(); Box(Modifier.weight(1f)); QuietButton(tx.reply, { replying = true }) }
                }
            }
        }
        said?.let { app.oie.ui.common.Snackbar(it, Modifier.align(Alignment.BottomCenter)) }
    }
}

/** What a status shows: a photo, a video, a text on the `selected` ground, or a line when it can't. */
@Composable
private fun StatusContent(s: Status, arriving: Boolean, tooBig: Boolean, parent: String, playing: Boolean, onEnd: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    when {
        arriving -> Text(tx.arriving, style = OieType.body, color = c.muted)
        tooBig -> Text(tx.statusTooBig(parent), style = OieType.body, color = c.ink, textAlign = TextAlign.Center)
        s.kind == Kind.PHOTO -> rememberPhoto(s.media, Photos.MAX_SIDE)?.let {
            Image(it.asImageBitmap(), tx.photo, Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusM)), contentScale = ContentScale.Fit)
        } ?: Text(tx.arriving, style = OieType.body, color = c.muted)
        s.kind == Kind.VIDEO || s.kind == Kind.GIF -> rememberBytes(s.media)?.let { bytes ->
            VideoSurface(bytes, loop = s.kind == Kind.GIF, muted = s.kind == Kind.GIF, play = playing, modifier = Modifier.fillMaxWidth(), onEnd = onEnd)
        } ?: Text(tx.arriving, style = OieType.body, color = c.muted)
        s.kind == Kind.TEXT -> Box(
            Modifier.fillMaxWidth().heightIn(min = OTokens.Size.contactTile * 2).clip(RoundedCornerShape(OTokens.Radius.radiusM)).background(c.selected)
                .border(BorderStroke(OTokens.Size.hairline, c.line), RoundedCornerShape(OTokens.Radius.radiusM)).padding(OTokens.Spacing.space8),
            contentAlignment = Alignment.Center,
        ) { Text(s.text, style = OieType.title, color = c.onSelected, textAlign = TextAlign.Center) }
        else -> Text(tx.onlyInWhatsApp(parent), style = OieType.body, color = c.muted)
    }
}

/** How long "Mandado para Vovó" stays. */
private const val SAID_MS = 4_000L
