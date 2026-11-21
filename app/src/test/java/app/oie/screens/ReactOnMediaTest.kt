package app.oie.screens

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.semantics.getOrNull
import androidx.test.core.app.ApplicationProvider
import app.oie.data.AppState
import app.oie.data.Photos
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.ui.kid.Conversation
import app.oie.ui.theme.OieTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * v0.46 (the owner: "allow the kids to react to messages received or any type of media … tap, hold and select
 * some emojis"): a long press on a photo, a video or a voice message opens Reagir, as on text, and a tap still
 * opens or plays it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
class ReactOnMediaTest {
    @get:Rule val compose = createComposeRule()
    private val now = 1_795_020_000_000L

    private fun chatWith(m: Message): AppState = AppState(clock = { now }).apply {
        parentName = "Ana"; kidName = "Tito"; link.pretendLinked(); allowed.addAll(link.contacts().take(5)); setPin("402817"); finishSetup()
        kidNoticeSeen = true; reactTipUsed = true
        // Tito wrote first, so Vovó's answer is in the 24-hour window and shows.
        messages.clear(); messages += Message("k", "vovo", true, now - 120_000, Kind.TEXT, "Oi vó"); messages += m
    }

    private fun longPressOpensReagir(state: AppState, target: String, byLabel: Boolean = false) {
        compose.setContent { OieTheme(dark = false) { Conversation(state, "vovo") } }
        // The media's own tap target: a node with a click action, named by its click label or its description.
        val media = androidx.compose.ui.test.SemanticsMatcher("tap target \"$target\"") { n ->
            val click = n.config.getOrNull(androidx.compose.ui.semantics.SemanticsActions.OnClick) ?: return@SemanticsMatcher false
            if (byLabel) click.label == target else n.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription).orEmpty().any { it.startsWith(target) }
        }
        compose.onAllNodes(media)[0].performTouchInput { longClick() }
        compose.waitForIdle()
        compose.onNodeWithText("Reagir").assertExists()
    }

    @Test fun onAPhoto() {
        Photos.sealer = PassThrough
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        val stored = Photos.store(ctx, Bitmap.createBitmap(200, 150, Bitmap.Config.ARGB_8888))
        longPressOpensReagir(chatWith(Message("ph", "vovo", false, now - 60_000, Kind.PHOTO, media = stored)), "Foto", byLabel = true)
    }

    @Test fun onAVoiceMessage() =
        longPressOpensReagir(chatWith(Message("v", "vovo", false, now - 60_000, Kind.VOICE, durationSec = 9, media = "v.ogg")), "Ouvir")
}
