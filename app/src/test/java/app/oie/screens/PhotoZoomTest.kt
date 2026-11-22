package app.oie.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.test.core.app.ApplicationProvider
import app.oie.data.Photos
import app.oie.ui.kid.PhotoScreen
import app.oie.ui.theme.OieTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * v0.53 (the owner: "the kid cannot pinch to zoom the images"): a photo shown big zooms with a pinch, a double-tap,
 * or TalkBack's actions, and says "Ampliada" while zoomed.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
class PhotoZoomTest {
    @get:Rule val compose = createComposeRule()

    private fun open(dark: Boolean = false): SemanticsNodeInteraction {
        app.oie.data.Background.dispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val bmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(0xFFFFF6EE.toInt()); drawCircle(300f, 330f, 140f, Paint().apply { color = 0xFFE8573C.toInt() }) }
        Photos.sealer = PassThrough
        val stored = Photos.store(ctx, bmp)
        compose.setContent {
            OieTheme(dark = dark) {
                val c = OieTheme.colors
                androidx.compose.foundation.layout.Box(
                    androidx.compose.ui.Modifier.fillMaxSize().background(c.page).border(app.oie.ui.theme.OTokens.Size.frame, c.frameLine).padding(app.oie.ui.theme.OTokens.Size.frame),
                ) { PhotoScreen(stored, "Vovó") {} }
            }
        }
        compose.waitForIdle()
        return compose.onNodeWithContentDescription("Foto")
    }

    private fun SemanticsNodeInteraction.state(): String? = fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription)

    @Test fun doubleTapZoomsInAndBack() {
        val photo = open()
        assertNull(photo.state())
        photo.performTouchInput { doubleClick(center) }
        compose.waitForIdle()
        assertEquals("Ampliada", photo.state())
        photo.performTouchInput { doubleClick(center) }
        compose.waitForIdle()
        assertNull(photo.state())
    }

    @Test fun aPinchZoomsIn() {
        val photo = open()
        photo.performTouchInput {
            pinch(center - Offset(20f, 0f), center - Offset(200f, 0f), center + Offset(20f, 0f), center + Offset(200f, 0f))
        }
        compose.waitForIdle()
        assertEquals("Ampliada", photo.state())
    }

    @Test fun talkBackCanZoomInAndSeeItWhole() {
        val photo = open()
        val actions = photo.fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Ampliar", "Ver inteira"), actions.map { it.label })
        compose.runOnIdle { actions[0].action() }
        compose.waitForIdle()
        assertEquals("Ampliada", photo.state())
        val again = photo.fetchSemanticsNode().config[SemanticsActions.CustomActions]
        compose.runOnIdle { again[1].action() }
        compose.waitForIdle()
        assertNull(photo.state())
    }

    /** What a zoomed photo looks like (looked at in the v0.53 audit), light and dark. */
    @Test fun zoomedLight() = zoomedShot(false)
    @Test fun zoomedDark() = zoomedShot(true)
    private fun zoomedShot(dark: Boolean) {
        val photo = open(dark)
        photo.performTouchInput { doubleClick(center + Offset(-120f, 40f)) }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/84-photo-zoomed-${if (dark) "dark" else "light"}.png")
    }
}
