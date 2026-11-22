@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.semantics.liveRegion
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import androidx.compose.material3.toShape

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
        if (pressed && enabled) PRESSED else 1f, pressSpec(moving), label = "press",
    )
    this.graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(source, androidx.compose.material3.ripple(), enabled = enabled, onClickLabel = label, role = role, onClick = onClick)
}

/** Pressed things scale to 97% (AGENTS.md, Motion). */
private const val PRESSED = 0.97f

/** v0.52 (decision 0018): Material's expressive fast spring; with "Remove animations", no movement. */
@Composable
private fun pressSpec(moving: Boolean): androidx.compose.animation.core.FiniteAnimationSpec<Float> =
    if (moving) androidx.compose.material3.MaterialTheme.motionScheme.fastSpatialSpec() else androidx.compose.animation.core.snap()

/**
 * v0.49 (the owner: "every action of the user has one small interaction animation"; docs/flows/interaction-motion.md):
 * the same 97% press for any touch target, given the target's own interaction source.
 */
fun Modifier.pressScale(source: androidx.compose.foundation.interaction.MutableInteractionSource, enabled: Boolean = true): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val moving = animationsOn()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (pressed && enabled) PRESSED else 1f, pressSpec(moving), label = "press")
    this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** v0.49: a pick among several (tabs, views, colours): the 97% press, with the selected semantics. */
fun Modifier.pressableSelect(selected: Boolean, role: Role, enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val source = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    this.pressScale(source, enabled)
        .selectable(selected, source, androidx.compose.material3.ripple(), enabled = enabled, role = role, onClick = onClick)
}

/** v0.49: an on/off (a switch row, a day, a picked contact): the 97% press, with the toggle semantics. */
fun Modifier.pressableToggle(value: Boolean, role: Role, enabled: Boolean = true, onValueChange: (Boolean) -> Unit): Modifier = composed {
    val source = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    this.pressScale(source, enabled)
        .toggleable(value, source, androidx.compose.material3.ripple(), enabled = enabled, role = role, onValueChange = onValueChange)
}

/** v0.49: a tap that also has a long press (media in a chat: open or play, hold to react), with the 97% press. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.pressableCombined(
    enabled: Boolean = true, role: Role? = Role.Button, label: String? = null, longLabel: String? = null,
    onLongClick: (() -> Unit)? = null, onClick: () -> Unit,
): Modifier = composed {
    val source = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    this.pressScale(source, enabled).combinedClickable(
        interactionSource = source, indication = androidx.compose.material3.ripple(), enabled = enabled,
        onClickLabel = label, role = role, onLongClickLabel = longLabel, onLongClick = onLongClick, onClick = onClick,
    )
}

/** v0.49: a long press with no tap (a message bubble, to react): the 97% press while the finger is down. */
fun Modifier.pressScaleOnHold(key: Any?, onLongPress: () -> Unit): Modifier = composed {
    var held by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val moving = animationsOn()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (held) PRESSED else 1f, pressSpec(moving), label = "hold")
    this.graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(key) { detectTapGestures(onPress = { held = true; tryAwaitRelease(); held = false }, onLongPress = { onLongPress() }) }
}

/** v0.49: a colour that fades to its new value in 150ms when something is picked (at once with "Remove animations"). */
@Composable
fun motionColor(target: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    val moving = animationsOn()
    return androidx.compose.animation.animateColorAsState(
        target, if (moving) androidx.compose.material3.MaterialTheme.motionScheme.fastEffectsSpec() else androidx.compose.animation.core.snap(),
        label = "pick",
    ).value
}

/**
 * v0.52 (decision 0018): Material 3 Expressive buttons, in the medium size (56dp, AGENTS.md targets),
 * with Material's press: the round shape squares a little while held.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ButtonInside(text: String?, icon: Int?, iconOnly: Boolean, tint: Color) {
    if (icon != null) androidx.compose.material3.Icon(painterResource(icon), if (iconOnly || text == null) text else null, Modifier.size(androidx.compose.material3.ButtonDefaults.MediumIconSize), tint = tint)
    if (icon != null && !iconOnly && text != null) androidx.compose.foundation.layout.Spacer(Modifier.size(androidx.compose.material3.ButtonDefaults.MediumIconSpacing))
    if (text != null && (!iconOnly || icon == null)) Text(text, style = androidx.compose.material3.ButtonDefaults.textStyleFor(androidx.compose.material3.ButtonDefaults.MediumContainerHeight))
}

/** The one filled coral action on a screen (README §10: main button): Material's filled button. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: Int? = null, iconOnly: Boolean = false) {
    val size = androidx.compose.material3.ButtonDefaults.MediumContainerHeight
    androidx.compose.material3.Button(
        onClick = onClick, modifier = modifier.heightIn(min = size).then(if (iconOnly && icon != null) Modifier.semantics { contentDescription = text } else Modifier),
        enabled = enabled, shapes = androidx.compose.material3.ButtonDefaults.shapesFor(size),
        contentPadding = androidx.compose.material3.ButtonDefaults.contentPaddingFor(size),
    ) { ButtonInside(text, icon, iconOnly, androidx.compose.material3.LocalContentColor.current) }
}

/** The stop button (README §3): Material's filled button in the error colours, always with a word. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StopButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val size = androidx.compose.material3.ButtonDefaults.MediumContainerHeight
    val s = androidx.compose.material3.MaterialTheme.colorScheme
    androidx.compose.material3.Button(
        onClick = onClick, modifier = modifier.heightIn(min = size), shapes = androidx.compose.material3.ButtonDefaults.shapesFor(size),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = s.error, contentColor = s.onError),
        contentPadding = androidx.compose.material3.ButtonDefaults.contentPaddingFor(size),
    ) { ButtonInside(text, null, false, s.onError) }
}

/**
 * A quiet button (README §10): Material's outlined button; with no word, Material's outlined icon
 * button (56dp). v0.38: a word with a fuller TalkBack name says the fuller one.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuietButton(text: String?, onClick: () -> Unit, modifier: Modifier = Modifier, icon: Int? = null, contentDescription: String? = null) {
    val size = androidx.compose.material3.ButtonDefaults.MediumContainerHeight
    if (text == null && icon != null) {
        // v0.52 audit: on a phone, Material's small icon button (its touch area stays 48dp), so titles beside it fit.
        androidx.compose.material3.OutlinedIconButton(
            onClick = onClick,
            modifier = modifier.size(if (isCompact) androidx.compose.material3.IconButtonDefaults.smallContainerSize() else androidx.compose.material3.IconButtonDefaults.mediumContainerSize()),
            shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
        ) { androidx.compose.material3.Icon(painterResource(icon), contentDescription, Modifier.size(if (isCompact) androidx.compose.material3.IconButtonDefaults.smallIconSize else androidx.compose.material3.IconButtonDefaults.mediumIconSize)) }
        return
    }
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = size)
            .then(if (text != null && contentDescription != null) Modifier.semantics(mergeDescendants = true) { this.contentDescription = contentDescription } else Modifier),
        shapes = androidx.compose.material3.ButtonDefaults.shapesFor(size),
        // v0.52 audit: a quiet button keeps the smaller side padding, so "Gravar áudio" fits on one line in a card.
        contentPadding = androidx.compose.material3.ButtonDefaults.contentPaddingFor(androidx.compose.material3.ButtonDefaults.MinHeight),
    ) { ButtonInside(text, icon, false, androidx.compose.material3.LocalContentColor.current) }
}

/** A text button (Remover, Esqueci o PIN): Material's text button, 48dp tall. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary) {
    androidx.compose.material3.TextButton(
        onClick = onClick, modifier = modifier.heightIn(min = OTokens.Spacing.space12),
        shapes = androidx.compose.material3.ButtonDefaults.shapes(),
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = color),
    ) { Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge) }
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
            .pressableToggle(checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = OTokens.Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
    ) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(label, style = OieType.label, color = c.ink)
            if (help != null) Text(help, style = OieType.caption, color = c.muted)
        }
        androidx.compose.material3.Switch(checked = checked, onCheckedChange = null)
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
                .clip(androidx.compose.material3.BottomSheetDefaults.ExpandedShape)
                .background(androidx.compose.material3.BottomSheetDefaults.ContainerColor).pointerInput(Unit) { detectTapGestures { } }
                // v0.34 audit round 2: a downward drag closes it too (Material's sheet gesture), besides Back and the scrim.
                .pointerInput(onClose) {
                    var pulled = 0f
                    detectVerticalDragGestures(onDragStart = { pulled = 0f }, onDragEnd = { if (pulled > OTokens.Spacing.space16.toPx()) onClose() }) { _, dy -> pulled += dy }
                }
                .padding(start = sideMargin + OTokens.Spacing.space2, end = sideMargin + OTokens.Spacing.space2, bottom = OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            // Material's drag handle, centred at the top.
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
            content()
        }
    }
}

/**
 * The snackbar (README §10): ink ground, page text, at most one action. Announced by TalkBack (a
 * polite live region). Callers place it at the bottom of the screen, above the system bar.
 */
@Composable
fun Snackbar(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    androidx.compose.material3.Snackbar(
        modifier.padding(horizontal = sideMargin, vertical = OTokens.Spacing.space4).widthIn(max = OTokens.Size.contentMax)
            .semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite },
        // v0.52 audit: the action in Material's snackbar action colour (inverse primary), readable on the dark ground.
        action = if (action == null) null else ({ androidx.compose.material3.TextButton(onAction,
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.SnackbarDefaults.actionColor)) { Text(action) } }),
    ) { Text(text) }
}

/** A core line icon (design/core/icons), tinted ink by default. */
@Composable
fun OIcon(res: Int, modifier: Modifier = Modifier, tint: Color = OieTheme.colors.ink, contentDescription: String? = null, size: Dp = ICON) {
    Icon(painterResource(res), contentDescription, modifier.size(size), tint = tint)
}

/** The core icon glyph size: 22dp (AGENTS.md, Icons). */
val ICON: Dp = OTokens.Spacing.space6 - OTokens.Spacing.space1 / 2

/** v0.52 (decision 0018): every face wears the same Material shape, the 9-sided cookie. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun avatarShape(): androidx.compose.ui.graphics.Shape = androidx.compose.material3.MaterialShapes.Cookie9Sided.toShape()

/** A family member's photo, or their initial on the cookie (imagery rules). */
@Composable
fun Initial(name: String, size: Dp, modifier: Modifier = Modifier, letters: String? = null, strong: Boolean = false) {
    val c = OieTheme.colors
    val sc = androidx.compose.material3.MaterialTheme.colorScheme
    // v0.52: on a row with news (the secondary container) the face is the primary container, so it still shows.
    Box(modifier.size(size).clip(avatarShape()).background(if (strong) sc.primaryContainer else c.selected), contentAlignment = Alignment.Center) {
        Text(
            letters?.replaceFirstChar { it.uppercase() } ?: name.firstOrNull()?.uppercase() ?: "?",
            // v0.34 audit: display is the greeting's alone (README §5, one per screen); a big initial is a title.
            style = OieType.title,
            color = if (strong) sc.onPrimaryContainer else c.onSelected, textAlign = TextAlign.Center,
        )
    }
}

/**
 * A contact's face (v0.26): their WhatsApp profile picture when Oiê has it, round, the same size as
 * the letter circle; otherwise the letter circle. Decorative: the name is always next to it.
 */
@Composable
fun Avatar(contact: app.oie.logic.Contact, size: Dp, modifier: Modifier = Modifier, letters: String? = null, strong: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // v0.34 audit: opened and decoded off the main thread, at the size it shows (not the full picture).
    val px = with(androidx.compose.ui.platform.LocalDensity.current) { size.roundToPx() }
    var picture by androidx.compose.runtime.remember(contact.photo) { androidx.compose.runtime.mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    androidx.compose.runtime.LaunchedEffect(contact.photo, px) {
        picture = if (contact.photo.isEmpty()) null else kotlinx.coroutines.withContext(app.oie.data.Background.dispatcher) {
            runCatching { app.oie.data.Photos.load(context, contact.photo, px)?.asImageBitmap() }.getOrNull()
        }
    }
    val shown = picture
    if (shown == null) Initial(contact.name, size, modifier, letters, strong)
    else androidx.compose.foundation.Image(
        shown, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        modifier = modifier.size(size).clip(avatarShape()).background(OieTheme.colors.selected),
    )
}

/** The top bar (decision 0018): Material's top app bar height, on the surface, with no line under it. */
@Composable
fun Bar(content: @Composable RowScope.() -> Unit) {
    val c = OieTheme.colors
    Row(
        // v0.47: 8dp above and below the 52dp buttons, so they never touch the frame or the line under the bar.
        Modifier.fillMaxWidth().heightIn(min = androidx.compose.material3.TopAppBarDefaults.TopAppBarExpandedHeight).padding(horizontal = sideMargin, vertical = OTokens.Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        content = content,
    )
}

/** A one-line banner under the bar (quiet hours, offline, link dropped). */
@Composable
fun Banner(icon: Int, text: String) {
    val c = OieTheme.colors
    val s = androidx.compose.material3.MaterialTheme.colorScheme
    Row(
        Modifier.padding(horizontal = sideMargin, vertical = OTokens.Spacing.space1).fillMaxWidth()
            .clip(androidx.compose.material3.MaterialTheme.shapes.large).background(s.secondaryContainer)
            .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
    ) {
        OIcon(icon, tint = s.onSecondaryContainer)
        Text(text, style = OieType.body, color = s.onSecondaryContainer)
    }
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

/** v0.43: the quiet-hours line: "até 21h" for a period, "até Ana liberar" when the parent turned it on from WhatsApp. */
fun quietText(tx: app.oie.ui.text.Strings, q: app.oie.logic.QuietPeriod?, parent: String, short: Boolean): String = when {
    q?.name == app.oie.logic.QuietHours.MANUAL -> if (short) tx.quietByParentShort(parent) else tx.quietByParent(parent)
    short -> tx.quietUntilShort(q?.end?.hour ?: 0)
    else -> tx.quietUntil(q?.end?.hour ?: 0)
}

