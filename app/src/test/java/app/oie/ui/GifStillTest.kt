package app.oie.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * v0.58, issue #88: a GIF whose first frame has an absurd shape (4 by 8000) in a chat. Before the fix its
 * height followed the frame's ("Can't represent a width of 600 and height of 1200000 in Constraints")
 * and the chat closed. Now it's kept at 9:16 at most.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
class GifStillTest {
    @get:Rule val compose = createComposeRule()

    @Test fun anAbsurdlyTallFrameInAChatKeepsAHeightTheChatCanMeasure() {
        val tall = Bitmap.createBitmap(4, 8000, Bitmap.Config.ARGB_8888)
        compose.setContent {
            LazyColumn(Modifier.size(600.dp, 700.dp)) {
                item { Box(Modifier.testTag("gif")) { app.oie.ui.kid.GifStill(tall) } }
            }
        }
        compose.waitForIdle()
        val b = compose.onNodeWithTag("gif").getBoundsInRoot()
        val h = b.bottom - b.top
        val w = b.right - b.left
        assertTrue("height $h for width $w", h.value <= w.value * 16f / 9f + 1f)
    }
}
