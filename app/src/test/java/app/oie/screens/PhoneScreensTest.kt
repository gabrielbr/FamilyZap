package app.oie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import app.oie.data.AppState
import app.oie.ui.kid.Conversation
import app.oie.ui.kid.KidHome
import app.oie.ui.parent.ParentArea
import app.oie.ui.setup.Setup
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The rule "works at 360dp phone width" (AGENTS.md), and large text (Android's font size at 1.3×),
 * checked as screenshots (v0.13 audit). Phone portrait, mdpi so the files stay small.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h740dp-port-mdpi")
class PhoneScreensTest {
    @get:Rule val compose = createComposeRule()
    private val now = 1_795_020_000_000L

    @org.junit.Before fun pinTimeZone() { java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC")) }

    private fun ready(): AppState = AppState(clock = { now }).apply {
        parentName = "Ana"; kidName = "Tito"
        link.pretendLinked()
        allowed.addAll(link.contacts().take(5))
        setPin("402817")
        finishSetup()
        kidNoticeSeen = true
    }

    private fun shot(name: String, bigText: Boolean = false, content: @Composable () -> Unit) {
        if (bigText) org.robolectric.RuntimeEnvironment.setFontScale(1.3f)
        compose.setContent {
            OieTheme(dark = false) {
                val c = OieTheme.colors
                androidx.compose.runtime.CompositionLocalProvider(app.oie.ui.text.LocalStrings provides app.oie.ui.text.Pt) {
                    Box(Modifier.fillMaxSize().background(c.page).border(OTokens.Size.frame, c.frameLine)) { content() }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/phone/$name.png")
    }

    @Test fun kidHome() = shot("p01-kid-home") { KidHome(ready()) }
    @Test fun kidHomeBigText() = shot("p02-kid-home-big-text", bigText = true) { KidHome(ready()) }
    @Test fun conversation() = shot("p03-conversation") { Conversation(ready(), "vovo") }
    @Test fun conversationBigText() = shot("p04-conversation-big-text", bigText = true) { Conversation(ready(), "vovo") }
    @Test fun parentContacts() = shot("p05-parent-contacts") { ParentArea(ready().apply { realMode = true }, 0) }
    @Test fun parentQuiet() = shot("p06-parent-quiet") { ParentArea(ready(), 1) }
    @Test fun parentSettings() = shot("p07-parent-settings") { ParentArea(ready(), 3) }
    @Test fun setupRisk() = shot("p08-setup-risk") { Setup(AppState(), 1, onClose = {}) }
    @Test fun setupNames() = shot("p09-setup-names", bigText = true) { Setup(AppState().apply { parentName = "Ana"; kidName = "Tito" }, 3, onClose = {}) }
    @Test fun stickers() = shot("p10-stickers") { Conversation(ready(), "vovo", openStickers = true) }
    @Test fun problemsAfterACrash() {
        app.oie.data.Diagnostics.init(java.nio.file.Files.createTempDirectory("diag").toFile(), "Oiê test")
        app.oie.data.Diagnostics.log("send PHOTO: failed: test"); app.oie.data.Diagnostics.recordCrash(now)
        shot("p11-problems-crash", bigText = true) { ParentArea(ready(), 3) }
        app.oie.data.Diagnostics.clear()
    }
    @Test fun connectionAfterACrash() {
        app.oie.data.Diagnostics.init(java.nio.file.Files.createTempDirectory("diag").toFile(), "Oiê test")
        app.oie.data.Diagnostics.recordCrash(now)
        shot("p12-connection-crash") { ParentArea(ready().apply { realMode = true; link.pretendDropped() }, 2) }
        app.oie.data.Diagnostics.clear()
    }
}
