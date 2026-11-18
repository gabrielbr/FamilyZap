package app.oie.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.ui.kid.Conversation
import app.oie.ui.kid.KidHome
import app.oie.ui.parent.ParentArea
import app.oie.ui.parent.ParentPin
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
 * Screenshot tests of the real Compose screens on a 1280×800 tablet, light and dark.
 * Record: ./gradlew recordRoborazziDebug  ·  check against the goldens: ./gradlew verifyRoborazziDebug
 * Goldens live in app/src/test/screenshots/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
class ScreensTest {
    @get:Rule val compose = createComposeRule()

    private fun ready(): AppState = AppState().apply {
        parentName = "Ana"; kidName = "Tito"
        link.pretendLinked()
        allowed.addAll(link.contacts().take(5))
        setPin("402817")
        finishSetup()
    }

    private fun shot(name: String, dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent {
            OieTheme(dark = dark) {
                val c = OieTheme.colors
                Box(Modifier.fillMaxSize().background(c.page).border(OTokens.Size.frame, c.frameLine)) { content() }
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name-${if (dark) "dark" else "light"}.png")
    }

    @Test fun kidNotice() = shot("01-kid-notice", false) { KidHome(ready()) }
    @Test fun kidHomeLight() = shot("02-kid-home", false) { KidHome(ready().apply { kidNoticeSeen = true }) }
    @Test fun kidHomeDark() = shot("02-kid-home", true) { KidHome(ready().apply { kidNoticeSeen = true }) }
    @Test fun kidHomeUnlinked() = shot("03-kid-home-unlinked", false) { KidHome(ready().apply { kidNoticeSeen = true; link.pretendDropped() }) }
    @Test fun conversationLight() = shot("04-conversation", false) { Conversation(ready().apply { kidNoticeSeen = true }, "vovo") }
    @Test fun conversationDark() = shot("04-conversation", true) { Conversation(ready().apply { kidNoticeSeen = true }, "vovo") }
    @Test fun riskNotice() = shot("05-setup-risk", false) { Setup(AppState(), 1, onClose = {}) }
    @Test fun names() = shot("06-setup-names", false) { Setup(AppState().apply { parentName = "Ana"; kidName = "Tito" }, 3, onClose = {}) }
    @Test fun createPin() = shot("07-setup-pin", false) { Setup(AppState(), 4, onClose = {}) }
    @Test fun parentPin() = shot("08-parent-pin", true) { ParentPin(ready()) }
    @Test fun parentContacts() = shot("09-parent-contacts", false) { ParentArea(ready(), 0) }
    @Test fun parentQuiet() = shot("10-parent-quiet", true) { ParentArea(ready(), 1) }
}
