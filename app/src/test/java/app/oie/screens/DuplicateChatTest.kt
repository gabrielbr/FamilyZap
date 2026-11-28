package app.oie.screens

import androidx.compose.ui.test.junit4.createComposeRule
import app.oie.data.AppState
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.ui.theme.OieTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** v0.59, issue #90: a chat holding the same message twice opens instead of closing Oiê. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
class DuplicateChatTest {
    @get:Rule val compose = createComposeRule()

    @Test fun aChatWithAMessageSavedTwiceOpens() {
        val now = 1_795_020_000_000L
        val s = AppState(clock = { now }).apply {
            parentName = "Ana"; kidName = "Tito"; kidNoticeSeen = true
            link.pretendLinked(); allowed.addAll(link.contacts().take(5)); setPin("402817"); finishSetup()
            val m = Message("AC8B09F24F28E339D43C8B00802E6228", "primos", false, now - 60_000, Kind.TEXT, "Tito, vem domingo", sender = "5511987650001@s.whatsapp.net")
            messages += m; messages += m
        }
        compose.setContent { OieTheme { app.oie.ui.kid.Conversation(s, "primos") } }
        compose.waitForIdle()
    }
}
