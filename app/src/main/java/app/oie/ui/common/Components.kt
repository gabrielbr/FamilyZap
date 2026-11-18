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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** The one coral action on a screen (README §10: main button). */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: Int? = null) {
    val c = OieTheme.colors
    Row(
        modifier
            .height(OTokens.Size.button)
            .clip(RoundedCornerShape(OTokens.Radius.radiusS))
            .background(if (enabled) c.mint else c.line)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = OTokens.Spacing.space6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2, Alignment.CenterHorizontally),
    ) {
        if (icon != null) OIcon(icon, tint = c.onMint)
        Text(text, style = OieType.label, color = if (enabled) c.onMint else c.muted)
    }
}

/** A quiet button: surface fill, hairline border, ink label (README §10). */
@Composable
fun QuietButton(text: String?, onClick: () -> Unit, modifier: Modifier = Modifier, icon: Int? = null, contentDescription: String? = null) {
    val c = OieTheme.colors
    Row(
        modifier
            .defaultMinSize(minWidth = OTokens.Spacing.space12, minHeight = OTokens.Spacing.space12)
            .clip(RoundedCornerShape(OTokens.Radius.radiusS))
            .background(c.surface)
            .border(BorderStroke(OTokens.Size.hairline, c.line), RoundedCornerShape(OTokens.Radius.radiusS))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (text == null) OTokens.Spacing.space3 else OTokens.Spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2, Alignment.CenterHorizontally),
    ) {
        if (icon != null) OIcon(icon, contentDescription = if (text == null) contentDescription else null)
        if (text != null) Text(text, style = OieType.label, color = c.ink)
    }
}

/** A plain ink text link (Remover, Esqueci o PIN). */
@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.defaultMinSize(minHeight = OTokens.Spacing.space12).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = OTokens.Spacing.space3),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = OieType.label.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), color = OieTheme.colors.ink) }
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
fun Initial(name: String, size: Dp, modifier: Modifier = Modifier) {
    val c = OieTheme.colors
    Box(modifier.size(size).clip(CircleShape).background(c.selected), contentAlignment = Alignment.Center) {
        Text(
            name.firstOrNull()?.uppercase() ?: "?",
            style = if (size >= OTokens.Size.contactTile) OieType.display else OieType.title,
            color = c.onSelected, textAlign = TextAlign.Center,
        )
    }
}

/** The top bar: 56dp, a hairline under it (README §6). */
@Composable
fun Bar(content: @Composable RowScope.() -> Unit) {
    val c = OieTheme.colors
    Row(
        Modifier.fillMaxWidth().height(OTokens.Size.header).padding(horizontal = OTokens.Spacing.space6),
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
        Modifier.fillMaxWidth().background(c.surface).padding(horizontal = OTokens.Spacing.space16, vertical = OTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
    ) {
        OIcon(icon)
        Text(text, style = OieType.body, color = c.ink)
    }
    Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
}
