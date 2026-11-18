package app.oie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.oie.R

/** Nunito, the one family (design/brand/oie). Weights 400, 600, 700, 800. */
val Nunito = FontFamily(
    Font(R.font.nunito_400, FontWeight.Normal),
    Font(R.font.nunito_600, FontWeight.SemiBold),
    Font(R.font.nunito_700, FontWeight.Bold),
    Font(R.font.nunito_800, FontWeight.ExtraBold),
)

/** The type roles (02 Design System/README.md §5). Sizes from OTokens.Type. */
object OieType {
    private fun style(size: Int, line: Int, weight: FontWeight) =
        TextStyle(fontFamily = Nunito, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight)

    // The generator gives a bold flag; the design system README §5 sets the exact weights.
    val display = style(OTokens.Type.displaySize, OTokens.Type.displayLine, FontWeight.ExtraBold)
    val title = style(OTokens.Type.titleSize, OTokens.Type.titleLine, FontWeight.Bold)
    val label = style(OTokens.Type.labelSize, OTokens.Type.labelLine, FontWeight.Bold)
    val body = style(OTokens.Type.bodySize, OTokens.Type.bodyLine, FontWeight.Normal)
    val bodySmall = style(OTokens.Type.bodySmallSize, OTokens.Type.bodySmallLine, FontWeight.Normal)
    val caption = style(OTokens.Type.captionSize, OTokens.Type.captionLine, FontWeight.SemiBold)
}

val LocalPalette = staticCompositionLocalOf { OTokens.Light }

object OieTheme {
    val colors: OTokens.Palette @Composable get() = LocalPalette.current
}

@Composable
fun OieTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val p = if (dark) OTokens.Dark else OTokens.Light
    // Material components read these roles; screens read OieTheme.colors directly.
    val scheme = if (dark) darkColorScheme(
        primary = p.mint, onPrimary = p.onMint, background = p.page, onBackground = p.ink,
        surface = p.surface, onSurface = p.ink, onSurfaceVariant = p.muted, outline = p.line,
        error = p.coralText, secondaryContainer = p.selected, onSecondaryContainer = p.onSelected,
    ) else lightColorScheme(
        primary = p.mint, onPrimary = p.onMint, background = p.page, onBackground = p.ink,
        surface = p.surface, onSurface = p.ink, onSurfaceVariant = p.muted, outline = p.line,
        error = p.coralText, secondaryContainer = p.selected, onSecondaryContainer = p.onSelected,
    )
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
