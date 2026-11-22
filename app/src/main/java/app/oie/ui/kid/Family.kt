package app.oie.ui.kid

import app.oie.ui.common.pressableSelect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Photos
import app.oie.data.Route
import app.oie.logic.Contact
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.logic.Reactions
import app.oie.ui.common.Bar
import app.oie.ui.common.pressable
import app.oie.ui.common.OIcon
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The v0.33 family features on the kid's side (docs/flows/family-features.md). */

/** v0.34: the shared bottom sheet (Components.BottomSheet), so every sheet closes with Back and darkens in both themes. */
@Composable
private fun Sheet(onClose: () -> Unit, content: @Composable () -> Unit) = app.oie.ui.common.BottomSheet(onClose) { content() }

/**
 * A long press on a message: six reactions, the one the kid already picked shown as picked (tapping it
 * again takes it away), and Ouvir for a text.
 */
@Composable
internal fun ReactionSheet(m: Message, onReact: (String) -> Unit, onRead: (() -> Unit)?, onClose: () -> Unit, lastUsed: String? = null) {
    val c = OieTheme.colors
    val tx = str
    Sheet(onClose) {
        Text(tx.react, style = OieType.title, color = c.ink)
        // v0.33 audit: on a phone the six share the width, so the last one isn't cut off.
        val compact = app.oie.ui.common.isCompact
        Row(
            if (compact) Modifier.fillMaxWidth() else Modifier,
            horizontalArrangement = if (compact) Arrangement.SpaceBetween else Arrangement.spacedBy(OTokens.Spacing.space2),
        ) {
            // v0.34 audit round 2: the kid's last reaction first (a shortcut for the one they use most).
            (listOfNotNull(lastUsed) + Reactions.CHOICES.filter { it != lastUsed }).forEach { e ->
                val picked = m.reactions[Reactions.KID] == e
                Box(
                    Modifier.size(OTokens.Spacing.space12).clip(CircleShape).background(app.oie.ui.common.motionColor(if (picked) c.selected else c.surface))
                        .pressable { onReact(e) }
                        .semantics { contentDescription = tx.reactWith(e); selected = picked },
                    contentAlignment = Alignment.Center,
                ) { Text(e, style = OieType.title) }
            }
        }
        if (onRead != null) QuietButton(tx.readAloud, onRead, Modifier.height(OTokens.Size.button), icon = R.drawable.gg_ic_play)
    }
}

/** The reactions under a message: each emoji once, with a count when more than one person used it. */
@Composable
internal fun ReactionChips(m: Message, alignEnd: Boolean) {
    val c = OieTheme.colors
    val tx = str
    if (m.reactions.isEmpty()) return
    val shown = Reactions.shown(m.reactions)
    val said = shown.joinToString(", ") { (e, n) -> if (n > 1) "$e $n" else e }
    // v0.47 (the owner: the reaction "needs to be attached … in the same way that WhatsApp does"): the chips sit on the
    // bubble's bottom edge, 12dp over it, at its starting corner; the layout gives that space back, so the next
    // message stays where it was.
    val overlap = OTokens.Spacing.space3
    Row(
        Modifier.fillMaxWidth()
            .layout { measurable, constraints ->
                val p = measurable.measure(constraints)
                val up = overlap.roundToPx()
                layout(p.width, (p.height - up).coerceAtLeast(0)) { p.place(0, -up) }
            }
            .padding(horizontal = OTokens.Spacing.space3)
            .semantics(mergeDescendants = true) { contentDescription = tx.reactionsA11y(said) },
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1, if (alignEnd) Alignment.End else Alignment.Start),
    ) {
        shown.forEach { (e, n) ->
            Text(
                if (n > 1) "$e $n" else e, style = OieType.caption, color = c.ink,
                modifier = Modifier.clip(androidx.compose.material3.MaterialTheme.shapes.extraLarge).background(c.surface)
                    .padding(horizontal = OTokens.Spacing.space2, vertical = OTokens.Spacing.space1),
            )
        }
    }
}

/** The photos, videos and GIFs in one chat, newest first (v0.33). Tap one to open it. */
@Composable
internal fun PhotosScreen(state: AppState, contact: Contact, onOpen: (Message) -> Unit, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    androidx.activity.compose.BackHandler(onBack = onBack)
    val media = state.visible(contact.id).filter { it.media != null && it.kind in setOf(Kind.PHOTO, Kind.VIDEO, Kind.GIF) }.reversed()
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.photosWith(contact.name), style = OieType.title, color = c.ink)
        }
        if (media.isEmpty()) {
            // Fresh review: an empty state like the home's, with the brand's rounds, and the way to add one.
            Column(Modifier.padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space8), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
                app.oie.ui.common.Rounds()
                Text(tx.noPhotos(contact.name), style = OieType.body, color = c.ink)
                Text(tx.noPhotosHow, style = OieType.body, color = c.muted)
            }
            return@Column
        }
        LazyVerticalGrid(
            GridCells.Adaptive(OTokens.Size.contactTile + OTokens.Spacing.space8),
            Modifier.fillMaxSize().padding(horizontal = app.oie.ui.common.sideMargin),
            horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { Box(Modifier.height(OTokens.Spacing.space4)) }
            items(media, key = { it.id }) { m -> MediaTile(m) { onOpen(m) } }
        }
    }
}

@Composable
private fun MediaTile(m: Message, onOpen: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val context = LocalContext.current
    val label = when (m.kind) { Kind.VIDEO -> tx.video; Kind.GIF -> tx.gifLabel; else -> tx.photo }
    Box(
        Modifier.aspectRatio(1f).clip(androidx.compose.material3.MaterialTheme.shapes.small).background(c.surface)
            .pressable(label = label, onClick = onOpen).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (m.kind == Kind.PHOTO) {
            var bmp by remember(m.media) { mutableStateOf<android.graphics.Bitmap?>(null) }
            LaunchedEffect(m.media) { bmp = withContext(app.oie.data.Background.dispatcher) { m.media?.let { Photos.load(context, it, Photos.MAX_SIDE / 4) } } }
            bmp?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        } else {
            // v0.34 audit: a video or GIF shows its first picture, with ▶ on it, like in the chat.
            val frame = rememberVideoFrame(m.media)
            frame?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            Box(Modifier.size(OTokens.Spacing.space12).clip(CircleShape).background(c.surface), contentAlignment = Alignment.Center) { OIcon(R.drawable.gg_ic_play) }
            // A GIF says so, as in the chat (videos and GIFs looked the same).
            if (m.kind == Kind.GIF) Text(
                tx.gifLabel, style = OieType.caption, color = c.ink,
                modifier = Modifier.align(Alignment.TopStart).padding(OTokens.Spacing.space2)
                    .clip(androidx.compose.material3.MaterialTheme.shapes.small).background(c.surface).padding(horizontal = OTokens.Spacing.space2),
            )
        }
    }
}

/** One photo, big (v0.33: from a bubble or from Fotos). Back closes it. */
@Composable
internal fun PhotoScreen(media: String, from: String, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val context = LocalContext.current
    androidx.activity.compose.BackHandler(onBack = onBack)
    var bmp by remember(media) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(media) { bmp = withContext(app.oie.data.Background.dispatcher) { Photos.load(context, media, Photos.MAX_SIDE) } }
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            Text(tx.mediaFrom(tx.photo, from), style = OieType.title, color = c.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        Box(Modifier.fillMaxSize().padding(OTokens.Spacing.space6), contentAlignment = Alignment.Center) {
            bmp?.let { Image(it.asImageBitmap(), tx.photo, Modifier.clip(androidx.compose.material3.MaterialTheme.shapes.medium), contentScale = ContentScale.Fit) }
        }
    }
}

/** One line drawn on the pad: its colour, its width in px, and the points it went through. */
private class Stroke2(val color: Color, val width: Float, val points: MutableList<Offset> = mutableListOf())

/**
 * The drawing pad (v0.33): white paper, five colours from the palette, thin or thick, Desfazer and
 * Limpar. Pronto turns it into a picture and hands it to the photo preview, where it's sent like a photo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrawPad(to: String, onDone: (android.graphics.Bitmap) -> Unit, onBack: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val density = LocalDensity.current
    androidx.activity.compose.BackHandler(onBack = onBack)
    val colors = listOf(c.drawInk, c.drawCoral, c.drawSun, c.drawLeaf, c.drawSky)
    var color by remember { mutableStateOf(0) }
    var thick by remember { mutableStateOf(false) }
    val compactPad = app.oie.ui.common.isCompact
    val strokes = remember { mutableStateListOf<Stroke2>() }
    var cleared by remember { mutableStateOf<List<Stroke2>?>(null) }
    // v0.34 audit: Limpar can be undone too (Desfazer right after brings the whole drawing back).
    val editLinks: @Composable () -> Unit = {
        TextLink(tx.undo, {
            val back = cleared
            if (strokes.isEmpty() && back != null) { strokes.addAll(back); cleared = null } else strokes.removeLastOrNull()
        })
        TextLink(tx.drawClear, { if (strokes.isNotEmpty()) { cleared = strokes.toList(); strokes.clear() } })
    }
    var tick by remember { mutableStateOf(0) } // a point added: draw again
    var size by remember { mutableStateOf(IntSize.Zero) }
    val thin = with(density) { OTokens.Spacing.space2.toPx() }
    val wide = with(density) { OTokens.Spacing.space4.toPx() }
    Column(Modifier.fillMaxSize().background(c.page)) {
        Bar {
            QuietButton(null, onBack, icon = R.drawable.gg_ic_back, contentDescription = tx.back)
            // v0.33 audit: on a phone, the name alone, so Pronto has room.
            Text(tx.drawFor(to), style = OieType.title, color = c.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            // Round 3: on a phone the button goes under the pad, full width, so the bar keeps who it's for.
            if (!compactPad) PrimaryButton(tx.drawUse, {
                if (strokes.isEmpty() || size.width == 0) return@PrimaryButton
                onDone(render(strokes, size, c.drawPaper))
            }, enabled = strokes.isNotEmpty(), icon = R.drawable.gg_ic_check)
        }
        Canvas(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = app.oie.ui.common.sideMargin, vertical = OTokens.Spacing.space4)
                .clip(androidx.compose.material3.MaterialTheme.shapes.medium).background(c.drawPaper)
                .onSizeChanged { size = it }
                .semantics { contentDescription = tx.drawA11y }
                .pointerInput(color, thick) {
                    detectTapGestures { p -> strokes += Stroke2(colors[color], if (thick) wide else thin, mutableListOf(p, p)); tick++ }
                }
                .pointerInput(color, thick) {
                    detectDragGestures(
                        onDragStart = { p -> strokes += Stroke2(colors[color], if (thick) wide else thin, mutableListOf(p)); tick++ },
                        onDrag = { change, _ -> strokes.lastOrNull()?.points?.add(change.position); tick++ },
                    )
                },
        ) {
            @Suppress("UNUSED_VARIABLE") val t = tick
            strokes.forEach { s ->
                val path = Path().apply { s.points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } }
                drawPath(path, s.color, style = Stroke(s.width, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        FlowRow(
            Modifier.fillMaxWidth().padding(start = app.oie.ui.common.sideMargin, end = app.oie.ui.common.sideMargin, bottom = OTokens.Spacing.space4),
            horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
        ) {
            colors.forEachIndexed { i, col ->
                // A swatch is never colour alone: a hairline, its name for TalkBack, and an ink ring when picked.
                Box(
                    Modifier.size(OTokens.Spacing.space12).clip(CircleShape)
                        .pressableSelect(color == i, role = Role.RadioButton) { color = i }
                        .semantics { contentDescription = tx.drawColors[i]; selected = color == i },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.size(OTokens.Spacing.space10).clip(CircleShape).background(col)
                            // v0.34 audit: the swatch edge is `muted` (3:1 in both themes), and ink, thicker, when picked.
                            .border(if (color == i) OTokens.Size.hairline * 3 else OTokens.Size.hairline, if (color == i) c.ink else c.muted, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { if (color == i) OIcon(R.drawable.gg_ic_check, tint = if (i == 2) c.drawInk else c.drawPaper) }
                }
            }
            Toggle(tx.drawThin, !thick) { thick = false }
            Toggle(tx.drawThick, thick) { thick = true }
            if (!compactPad) editLinks()
        }
        // Round 4: on a phone, Desfazer and Limpar share their own row (Limpar wrapped alone).
        if (compactPad) Row(Modifier.padding(horizontal = app.oie.ui.common.sideMargin), horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) { editLinks() }
        if (compactPad) PrimaryButton(tx.drawUse, {
            if (strokes.isEmpty() || size.width == 0) return@PrimaryButton
            onDone(render(strokes, size, c.drawPaper))
        }, Modifier.fillMaxWidth().padding(start = app.oie.ui.common.sideMargin, end = app.oie.ui.common.sideMargin, bottom = OTokens.Spacing.space4),
            enabled = strokes.isNotEmpty(), icon = R.drawable.gg_ic_check)
    }
}

/** A picked-or-not choice: selected when on, a hairline when off (README §10: chips). */
@Composable
private fun Toggle(label: String, on: Boolean, onClick: () -> Unit) {
    val c = OieTheme.colors
    Box(
        Modifier.heightIn(min = OTokens.Spacing.space12).clip(androidx.compose.material3.MaterialTheme.shapes.extraLarge)
            .background(app.oie.ui.common.motionColor(if (on) c.selected else c.surface))
            .border(BorderStroke(OTokens.Size.hairline, app.oie.ui.common.motionColor(if (on) c.selected else c.line)), androidx.compose.material3.MaterialTheme.shapes.extraLarge)
            .pressableSelect(on, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = OTokens.Spacing.space4),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = OieType.label, color = if (on) c.onSelected else c.ink) }
}

/** The drawing as a picture, at most 1600 px wide like a photo, on white. */
private fun render(strokes: List<Stroke2>, size: IntSize, paper: Color): android.graphics.Bitmap {
    val scale = minOf(1f, Photos.MAX_SIDE.toFloat() / maxOf(size.width, size.height))
    val bmp = android.graphics.Bitmap.createBitmap((size.width * scale).toInt().coerceAtLeast(1), (size.height * scale).toInt().coerceAtLeast(1), android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    canvas.drawColor(paper.toArgb())
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE; strokeCap = android.graphics.Paint.Cap.ROUND; strokeJoin = android.graphics.Paint.Join.ROUND
    }
    strokes.forEach { s ->
        paint.color = s.color.toArgb(); paint.strokeWidth = s.width * scale
        val path = android.graphics.Path()
        s.points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x * scale, p.y * scale) else path.lineTo(p.x * scale, p.y * scale) }
        canvas.drawPath(path, paint)
    }
    return bmp
}

/**
 * The kid's home lines (v0.33): today's birthdays, then the gentle nudge when the parent turned it on.
 * Each opens that chat; the nudge can be put away for a week.
 */
@Composable
internal fun HomeReminders(state: AppState, birthdays: Boolean) {
    val today = java.time.Instant.ofEpochMilli(state.clock()).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    val tx = str
    // The same letters as on the person's tile (round 3: "V" here, "Vó" there).
    val letters = app.oie.logic.Initials.of(state.reachable.map { it.name })
    if (birthdays) app.oie.logic.Birthdays.today(state.reachable, today).forEach { ct ->
        Reminder(ct, tx.birthdayToday(ct.name), onOpen = { state.route = Route.Conversation(ct.id) }, letters = letters[ct.name])
    }
    if (!birthdays) app.oie.logic.Nudge.pick(state.nudgeOn, state.reachable, state.messages, state.nudgeDismissed, state.clock())?.let { ct ->
        Reminder(ct, tx.nudgeLine(ct.name), onOpen = { state.route = Route.Conversation(ct.id) }, onDismiss = { state.nudgeDismissed[ct.id] = state.clock() }, letters = letters[ct.name])
    }
}

@Composable
private fun Reminder(ct: Contact, text: String, onOpen: () -> Unit, onDismiss: (() -> Unit)? = null, letters: String? = null) {
    val c = OieTheme.colors
    val tx = str
    Row(
        Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth().padding(top = OTokens.Spacing.space4)
            .clip(androidx.compose.material3.MaterialTheme.shapes.medium).background(c.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f).heightIn(min = OTokens.Size.rowMin).pressable(onClick = onOpen)
                .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space2),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            app.oie.ui.common.Avatar(ct, OTokens.Size.avatar, letters = letters)
            Text(text, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
            OIcon(R.drawable.gg_ic_chevron, tint = c.muted)
        }
    }
    // v0.33 audit: "Agora não" under the line, not beside it, so the line has the width on a phone.
    if (onDismiss != null) TextLink(tx.nudgeNotNow, onDismiss)
}

/**
 * "Preciso de você" (v0.33): a quiet button on the kid's home, shown only when the parent marked
 * someone. It asks once before sending; then it says who got it.
 */
@Composable
internal fun NeedYou(state: AppState, onSaid: (String) -> Unit) {
    val tx = str
    val to = app.oie.logic.NeedYou.to(state.reachable)
    if (to.isEmpty()) return
    val names = tx.and(to.map { it.name })
    var asking by remember { mutableStateOf(false) }
    // v0.34 audit: "wait" says how many minutes are left, so the kid knows when to try again.
    fun waitLine() = tx.needYouWait(((app.oie.logic.NeedYou.waitMs(state.needYouAt, state.clock()) + 59_999) / 60_000).toInt().coerceAtLeast(1))
    QuietButton(tx.needYouButton, {
        if (app.oie.logic.NeedYou.waitMs(state.needYouAt, state.clock()) > 0) onSaid(waitLine()) else asking = true
    }, Modifier.padding(top = OTokens.Spacing.space8), icon = R.drawable.gg_ic_people)
    // v0.34 audit: a bottom sheet like every other kid sheet (it was a centred dialog).
    if (asking) Sheet({ asking = false }) {
        NeedYouCard(names, onSend = {
            asking = false
            onSaid(if (state.sendNeedYou(tx.needYouText)) tx.needYouSent(names) else waitLine())
        }, onClose = { asking = false })
    }
}

/** The question before "Preciso de você" goes: who gets it, Mandar or Cancelar. */
@Composable
internal fun NeedYouCard(names: String, onSend: () -> Unit, onClose: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    run {
        Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6)) {
            Text(tx.needYouAsk(names), style = OieType.title, color = c.ink)
            app.oie.ui.setup.ButtonRow {
                PrimaryButton(tx.needYouSend, onSend, icon = R.drawable.gg_ic_send)
                QuietButton(tx.cancel, onClose, Modifier.height(OTokens.Size.button))
            }
        }
    }
}
