package app.oie.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import app.oie.data.AppState
import app.oie.data.Photos
import app.oie.logic.Kind
import app.oie.logic.Message
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
/** The Keystore isn't on the JVM; photo tests store plain bytes. */
object PassThrough : app.oie.data.Sealer {
    override fun seal(plain: ByteArray) = plain
    override fun open(sealed: ByteArray) = sealed
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
class ScreensTest {
    @get:Rule val compose = createComposeRule()

    /** A fixed clock (18 Nov 2026, 16:40 UTC) and time zone, so the times in the screenshots never change. */
    private val now = 1_795_020_000_000L

    @org.junit.Before fun pinTimeZone() { java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC")) }

    private fun ready(): AppState = AppState(clock = { now }).apply {
        parentName = "Ana"; kidName = "Tito"
        link.pretendLinked()
        allowed.addAll(link.contacts().take(5))
        setPin("402817")
        finishSetup()
    }

    private fun shot(name: String, dark: Boolean, english: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent {
            OieTheme(dark = dark) {
                val c = OieTheme.colors
                androidx.compose.runtime.CompositionLocalProvider(app.oie.ui.text.LocalStrings provides if (english) app.oie.ui.text.En else app.oie.ui.text.Pt) {
                    Box(Modifier.fillMaxSize().background(c.page).border(OTokens.Size.frame, c.frameLine)) { content() }
                }
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
    @Test fun conversationWithPhoto() = shot("04b-conversation-photo", false) {
        // A sample "photo": the brand rounds drawn on cream, stored the way Photos.keep stores a real one.
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val bmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            drawColor(0xFFFFF6EE.toInt())
            drawCircle(300f, 330f, 140f, Paint().apply { color = 0xFFE8573C.toInt(); style = Paint.Style.STROKE; strokeWidth = 90f })
            drawCircle(520f, 180f, 60f, Paint().apply { color = 0xFFFCE1D6.toInt() })
        }
        Photos.sealer = PassThrough
        val stored = Photos.store(ctx, bmp)
        val st = ready().apply {
            kidNoticeSeen = true
            messages += Message("ph", "vovo", true, now - 60_000, Kind.PHOTO, media = stored)
        }
        Conversation(st, "vovo")
    }
    @Test fun riskNoticeEnglish() = shot("05-setup-risk-en", false, english = true) { Setup(AppState(), 1, onClose = {}) }
    @Test fun kidHomeEnglish() = shot("02-kid-home-en", false, english = true) { KidHome(ready().apply { kidNoticeSeen = true }) }
    @Test fun parentQuietEditorEnglishDark() = shot("10b-parent-help-en", true, english = true) { ParentArea(ready(), 3) }
    @Test fun parentHelp() = shot("10c-parent-help", false) { ParentArea(ready(), 3) }
    @Test fun riskNotice() = shot("05-setup-risk", false) { Setup(AppState(), 1, onClose = {}) }
    @Test fun names() = shot("06-setup-names", false) { Setup(AppState().apply { parentName = "Ana"; kidName = "Tito" }, 3, onClose = {}) }
    @Test fun createPin() = shot("07-setup-pin", false) { Setup(AppState(), 4, onClose = {}) }
    @Test fun parentPin() = shot("08-parent-pin", true) { ParentPin(ready()) }
    @Test fun parentContacts() = shot("09-parent-contacts", false) { ParentArea(ready(), 0) }
    @Test fun parentQuiet() = shot("10-parent-quiet", true) { ParentArea(ready(), 1) }
}
