package app.oie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.oie.R

/** Nunito, the one family (design/brand/oie). Weights 400, 600, 700, 800. */
val Nunito = FontFamily(
    Font(R.font.nunito_400, FontWeight.Normal),
    // v0.52: Material's scale uses weight 500 (medium), which Nunito's files don't have; 600 stands in, so names
    // and titles don't fall back to the thin 400.
    Font(R.font.nunito_600, FontWeight.Medium),
    Font(R.font.nunito_600, FontWeight.SemiBold),
    Font(R.font.nunito_700, FontWeight.Bold),
    Font(R.font.nunito_800, FontWeight.ExtraBold),
)

/**
 * Material 3 Expressive's type scale, every role in Nunito (decision 0018). Sizes, line heights and
 * weights are Material's defaults; only the family is ours.
 */
val OieTypography = Typography(Nunito)

/**
 * The app's six type roles (AGENTS.md, Type), now pointing at Material's scale (decision 0018):
 * display is the greeting, title a screen or section title, label a name or a control, body the
 * reading text, body-small the secondary line, caption the small print (12sp, never less).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object OieType {
    val display = OieTypography.displaySmallEmphasized
    val title = OieTypography.titleLargeEmphasized
    val label = OieTypography.titleMedium
    val body = OieTypography.bodyLarge
    val bodySmall = OieTypography.bodyMedium
    val caption = OieTypography.bodySmall
}

val LocalPalette = staticCompositionLocalOf { palette(OieLightScheme, OTokens.Light) }

object OieTheme {
    /**
     * The app's colour roles, read from Material's scheme (decision 0018). Screens written before
     * Material use these names; new code reads MaterialTheme.colorScheme directly. The frame, the
     * QR code and the drawing pad keep their fixed brand colours.
     */
    val colors: OTokens.Palette @Composable get() = LocalPalette.current
}

/** Maps Material's roles onto the app's older names, so every screen wears the new scheme. */
fun palette(s: ColorScheme, fixed: OTokens.Palette) = OTokens.Palette(
    page = s.surface,
    surface = s.surfaceContainer,
    ink = s.onSurface,
    muted = s.onSurfaceVariant,
    line = s.outlineVariant,
    mint = s.primary,
    onMint = s.onPrimary,
    coral = s.error,
    coralText = s.error,
    coralSoft = s.errorContainer,
    onCoral = s.onError,
    selected = s.secondaryContainer,
    onSelected = s.onSecondaryContainer,
    frameLine = fixed.frameLine,
    snackbar = s.inverseSurface,
    qrGround = fixed.qrGround,
    qrInk = fixed.qrInk,
    drawPaper = fixed.drawPaper,
    drawInk = fixed.drawInk,
    drawCoral = fixed.drawCoral,
    drawSun = fixed.drawSun,
    drawLeaf = fixed.drawLeaf,
    drawSky = fixed.drawSky,
    scrim = fixed.scrim,
)

/**
 * Oiê's theme: Material 3 Expressive (decision 0018) with the scheme generated from the brand coral,
 * Nunito, Material's shapes and its expressive motion. The coral frame round every screen stays.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OieTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val scheme = if (dark) OieDarkScheme else OieLightScheme
    val p = palette(scheme, if (dark) OTokens.Dark else OTokens.Light)
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialExpressiveTheme(colorScheme = scheme, motionScheme = MotionScheme.expressive(), typography = OieTypography) {
            // v0.52 audit (P0): the screens sit on the surface without a Material Surface around them, so Material's
            // icon buttons took the default content colour (black) and vanished on dark. The surface's colour is set here.
            CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides scheme.onSurface, content = content)
        }
    }
}
