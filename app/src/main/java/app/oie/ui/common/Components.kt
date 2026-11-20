package app.oie.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.semantics.liveRegion
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** Narrow screens (a phone, under 600dp): smaller margins, wrapping button rows, stacked parent area (v0.13 audit). */
val isCompact: Boolean
    @Composable get() = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600

/** The side margin of a screen (README §6): 16dp on a phone, 24dp on the tablet; the bar and the content share it (v0.34). */
val sideMargin: Dp
    @Composable get() = if (isCompact) OTokens.Spacing.space4 else OTokens.Spacing.space6

/**
 * Press feedback for anything tappable (AGENTS.md, Motion): scales to 97% while held, in the standard
 * 150ms with the one ease; with "Remove animations" on, nothing moves. Used by the buttons, tiles and rows.
 */
fun Modifier.pressable(enabled: Boolean = true, role: Role? = Role.Button, label: String? = null, onClick: () -> Unit): Modifier = composed {
    val source = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val moving = animationsOn()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (pressed && enabled) PRESSED else 1f,
        androidx.compose.animation.core.tween(if (moving) OTokens.Motion.motionStandard else 0, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "press",
    )
    this.graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(source, androidx.compose.material3.ripple(), enabled = enabled, onClickLabel = label, role = role, onClick = onClick)
}

/** Pressed things scale to 97% (AGENTS.md, Motion). */
private const val PRESSED = 0.97f

/** The one coral action on a screen (README §10: main button). */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: Int? = null, iconOnly: Boolean = false) {
    val c = OieTheme.colors
    Row(
        modifier
            // v0.29: icon only on a narrow screen (the chat's Enviar); the word stays for TalkBack.
            .then(if (iconOnly && icon != null) Modifier.semantics { contentDescription = text } else Modifier)
            .height(OTokens.Size.button)
            .clip(RoundedCornerShape(OTokens.Radius.radiusS))
            .background(if (enabled) c.mint else c.line)
            .pressable(enabled = enabled, onClick = onClick)
            .padding(horizontal = OTokens.Spacing.space6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2, Alignment.CenterHorizontally),
    ) {
        // v0.34 audit: a disabled button's icon is muted like its word, so it never looks half on.
        if (icon != null) OIcon(icon, tint = if (enabled) c.onMint else c.muted)
        if (!iconOnly || icon == null) Text(text, style = OieType.label, color = if (enabled) c.onMint else c.muted)
    }
}

/** The stop button (README §3): the deeper brick fill with its on-stop text, for the one destructive action, always with a word. */
@Composable
fun StopButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = OieTheme.colors
    Box(
        modifier.height(OTokens.Size.button).clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.coral)
            .pressable(onClick = onClick).padding(horizontal = OTokens.Spacing.space6),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = OieType.label, color = c.onCoral) }
}

/** A quiet button: surface fill, hairline border, ink label (README §10). */
@Composable
fun QuietButton(text: String?, onClick: () -> Unit, modifier: Modifier = Modifier, icon: Int? = null, contentDescription: String? = null) {
    val c = OieTheme.colors
    Row(
        modifier
            // v0.34 audit: buttons are 52dp (AGENTS.md, Targets); an icon-only one stays a 52dp square.
            .defaultMinSize(minWidth = OTokens.Size.button, minHeight = OTokens.Size.button)
            .clip(RoundedCornerShape(OTokens.Radius.radiusS))
            .background(c.surface)
            .border(BorderStroke(OTokens.Size.hairline, c.line), RoundedCornerShape(OTokens.Radius.radiusS))
            .pressable(onClick = onClick)
            .padding(horizontal = if (text == null) OTokens.Spacing.space3 else OTokens.Spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2, Alignment.CenterHorizontally),
    ) {
        if (icon != null) OIcon(icon, contentDescription = if (text == null) contentDescription else null)
        if (text != null) Text(text, style = OieType.label, color = c.ink)
    }
}

/**
 * A plain ink text link (Remover, Esqueci o PIN). v0.34 audit: its word starts on the column's edge
 * like the text around it (no inner side padding), wraps on a narrow screen, and keeps a 48dp target.
 */
@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = OieTheme.colors.ink) {
    Box(
        // v0.34 audit round 2: as tall as a button, so a link beside buttons lines up with them.
        modifier.defaultMinSize(minWidth = OTokens.Spacing.space12, minHeight = OTokens.Size.button).pressable(onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) { Text(text, style = OieType.label.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), color = color) }
}

/**
 * A setting that is on or off (v0.34 audit): the whole row is the target, the switch only shows the
 * state, and TalkBack reads the label once with its state.
 */
@Composable
fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit, help: String? = null, modifier: Modifier = Modifier) {
    val c = OieTheme.colors
    Row(
        modifier.fillMaxWidth().defaultMinSize(minHeight = OTokens.Size.rowMin)
            .toggleable(checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = OTokens.Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(label, style = OieType.label, color = c.ink)
            if (help != null) Text(help, style = OieType.caption, color = c.muted)
        }
        androidx.compose.material3.Switch(
            checked = checked, onCheckedChange = null,
            colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
        )
    }
}

/**
 * A bottom sheet (README §10): the scrim (a token, so it darkens in both themes), the surface with the
 * large top radius, one column up to 600dp. Back and a tap on the scrim close it; the scrim isn't a
 * TalkBack stop, since Back closes it.
 */
@Composable
fun BottomSheet(onClose: () -> Unit, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val c = OieTheme.colors
    androidx.activity.compose.BackHandler(onBack = onClose)
    Box(
        Modifier.fillMaxSize().background(c.scrim)
            .pointerInput(onClose) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.BottomCenter,
    ) {
        androidx.compose.foundation.layout.Column(
            Modifier.widthIn(max = OTokens.Size.contentMax).fillMaxWidth()
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).pointerInput(Unit) { detectTapGestures { } }
                // v0.34 audit round 2: a downward drag closes it too (Material's sheet gesture), besides Back and the scrim.
                .pointerInput(onClose) {
                    var pulled = 0f
                    detectVerticalDragGestures(onDragStart = { pulled = 0f }, onDragEnd = { if (pulled > OTokens.Spacing.space16.toPx()) onClose() }) { _, dy -> pulled += dy }
                }
                .padding(horizontal = sideMargin + OTokens.Spacing.space2, vertical = OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            content = content,
        )
    }
}

/**
 * The snackbar (README §10): ink ground, page text, at most one action. Announced by TalkBack (a
 * polite live region). Callers place it at the bottom of the screen, above the system bar.
 */
@Composable
fun Snackbar(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    val c = OieTheme.colors
    Row(
        modifier.padding(horizontal = sideMargin, vertical = OTokens.Spacing.space4).widthIn(max = OTokens.Size.contentMax).fillMaxWidth()
            .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.snackbar)
            .padding(start = OTokens.Spacing.space6, end = if (action == null) OTokens.Spacing.space6 else OTokens.Spacing.space2)
            .defaultMinSize(minHeight = OTokens.Spacing.space12)
            .semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) {
        Text(text, style = OieType.body, color = c.page, modifier = Modifier.weight(1f).padding(vertical = OTokens.Spacing.space3))
        if (action != null) Box(
            Modifier.defaultMinSize(minWidth = OTokens.Spacing.space12, minHeight = OTokens.Spacing.space12).pressable(onClick = onAction)
                .padding(horizontal = OTokens.Spacing.space3),
            contentAlignment = Alignment.Center,
        ) { Text(action, style = OieType.label, color = c.page) }
    }
}

/** A core line icon (design/core/icons), tinted ink by default. */
@Composable
fun OIcon(res: Int, modifier: Modifier = Modifier, tint: Color = OieTheme.colors.ink, contentDescription: String? = null, size: Dp = ICON) {
    Icon(painterResource(res), contentDescription, modifier.size(size), tint = tint)
}

/** The core icon glyph size: 22dp (AGENTS.md, Icons). */
val ICON: Dp = OTokens.Spacing.space6 - OTokens.Spacing.space1 / 2

/** A family member's photo, or their initial on the soft coral round (imagery rules). */
@Composable
fun Initial(name: String, size: Dp, modifier: Modifier = Modifier, letters: String? = null) {
    val c = OieTheme.colors
    Box(modifier.size(size).clip(CircleShape).background(c.selected), contentAlignment = Alignment.Center) {
        Text(
            letters?.replaceFirstChar { it.uppercase() } ?: name.firstOrNull()?.uppercase() ?: "?",
            // v0.34 audit: display is the greeting's alone (README §5, one per screen); a big initial is a title.
            style = OieType.title,
            color = c.onSelected, textAlign = TextAlign.Center,
        )
    }
}

/**
 * A contact's face (v0.26): their WhatsApp profile picture when Oiê has it, round, the same size as
 * the letter circle; otherwise the letter circle. Decorative: the name is always next to it.
 */
@Composable
fun Avatar(contact: app.oie.logic.Contact, size: Dp, modifier: Modifier = Modifier, letters: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // v0.34 audit: opened and decoded off the main thread, at the size it shows (not the full picture).
    val px = with(androidx.compose.ui.platform.LocalDensity.current) { size.roundToPx() }
    var picture by androidx.compose.runtime.remember(contact.photo) { androidx.compose.runtime.mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    androidx.compose.runtime.LaunchedEffect(contact.photo, px) {
        picture = if (contact.photo.isEmpty()) null else kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching { app.oie.data.Photos.load(context, contact.photo, px)?.asImageBitmap() }.getOrNull()
        }
    }
    val shown = picture
    if (shown == null) Initial(contact.name, size, modifier, letters)
    else androidx.compose.foundation.Image(
        shown, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        modifier = modifier.size(size).clip(CircleShape).background(OieTheme.colors.selected),
    )
}

/** The top bar: 56dp, a hairline under it (README §6). */
@Composable
fun Bar(content: @Composable RowScope.() -> Unit) {
    val c = OieTheme.colors
    Row(
        Modifier.fillMaxWidth().height(OTokens.Size.header).padding(horizontal = sideMargin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        content = content,
    )
    Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
}

/** A one-line banner under the bar (quiet hours, offline, link dropped). */
@Composable
fun Banner(icon: Int, text: String) {
    val c = OieTheme.colors
    Row(
        Modifier.fillMaxWidth().background(c.surface).padding(horizontal = sideMargin, vertical = OTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
    ) {
        OIcon(icon)
        Text(text, style = OieType.body, color = c.ink)
    }
    Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
}

/**
 * The link as the screens show it (LinkView, v0.24), re-read once when the 30-second grace ends, so
 * "connecting" turns into "down" by itself if the link doesn't come up.
 */
@Composable
fun linkShown(state: app.oie.data.AppState): app.oie.logic.LinkView.Shown {
    val tick = androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val since = maxOf(state.linkState.downSince, state.startedAt)
    androidx.compose.runtime.LaunchedEffect(since, state.linkState.status) {
        val wait = since + app.oie.logic.LinkView.GRACE_MS - state.clock()
        if (wait > 0) { kotlinx.coroutines.delay(wait); tick.intValue++ }
    }
    @Suppress("UNUSED_VARIABLE") val read = tick.intValue // re-read the clock when the grace ends
    return state.linkShown()
}

/** False when the system's "Remove animations" is on (AGENTS.md: things appear instead of moving). */
@Composable
fun animationsOn(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember {
        runCatching { android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) }.getOrDefault(1f) != 0f
    }
}

/**
 * The touching rounds (README §7): the o's ring and the i's dot from the logo, set so they touch.
 * Coral only, on the page. Decoration: at most one cluster per screen, never behind text, nothing for
 * TalkBack to read. Used on the welcome and on the kid's empty home (v0.34).
 */
@Composable
fun Rounds(modifier: Modifier = Modifier) {
    val coral = OieTheme.colors.mint
    androidx.compose.foundation.Canvas(modifier.size(OTokens.Spacing.space16 + OTokens.Spacing.space8, OTokens.Spacing.space16)) {
        val stroke = OTokens.Spacing.space3.toPx()
        val ring = size.height / 2 - stroke / 2
        val ringCenter = androidx.compose.ui.geometry.Offset(stroke / 2 + ring, size.height / 2)
        drawCircle(coral, ring, ringCenter, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
        // The dot sits on the ring's upper right, touching its outer edge.
        val dot = OTokens.Spacing.space3.toPx()
        val reach = ring + stroke / 2 + dot
        val a = Math.toRadians(-35.0)
        drawCircle(coral, dot, androidx.compose.ui.geometry.Offset(ringCenter.x + reach * kotlin.math.cos(a).toFloat(), ringCenter.y + reach * kotlin.math.sin(a).toFloat()))
    }
}
