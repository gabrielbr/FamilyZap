package app.oie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
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
    @Test fun setupContactsSyncing() = shot("p13-setup-contacts-syncing", bigText = true) {
        Setup(ready().apply {
            realMode = true
            real = object : app.oie.data.WhatsAppLink {
                override val connected = true
                override val profileName: String? = null
                override fun contacts() = listOf(app.oie.logic.Contact("g1@g.us", "Família", isGroup = true))
                override fun send(message: app.oie.logic.Message) {}
            }
            allowed.clear(); linkState.status = app.oie.data.LinkState.Status.Connected; linkState.linkedAt = now - 5_000
        }, 5, onClose = {})
    }
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

    // v0.19: a name for the kid, and the number for the parent, at 360dp and 1.3× text.
    private fun renamed() = ready().apply { realMode = true; allowed[0] = allowed[0].copy(waName = "Maria Silva") }
    @Test fun contactsRenamed() = shot("p14-contacts-renamed", bigText = true) { ParentArea(renamed(), 0) }
    @Test fun contactEditor() = shot("p15-contact-editor", bigText = true) {
        val s = renamed()
        Column(Modifier.padding(OTokens.Spacing.space4)) { app.oie.ui.parent.ContactEditor(s.allowed[0], "Tito", s.link.contacts(), {}, {}, onRemove = {}) }
    }
    @Test fun setupContactsPicked() = shot("p16-setup-contacts-picked", bigText = true) { Setup(renamed().apply { realMode = false }, 5, onClose = {}) }

    // v0.21: nicknames in Ajustes e ajuda, and a paused chat, at 1.3× text.
    @Test fun settingsNicknames() = shot("p17-settings-nicknames", bigText = true) { ParentArea(ready().apply { kidNicknames += listOf("Titinho", "Ti") }, 3) }
    @Test fun contactsPaused() = shot("p18-contacts-paused", bigText = true) { ParentArea(ready().apply { realMode = true; allowed[1] = allowed[1].copy(paused = true) }, 0) }

    // v0.22: the emoji field and the preview at 1.3× text, scrolled to them.
    @Test fun settingsSignature() = shot("p19-settings-signature", bigText = true) {
        androidx.compose.foundation.layout.Column(Modifier.padding(OTokens.Spacing.space4)) {
            app.oie.ui.setup.Field("Emoji antes do nome", "😎", {}, "Opcional. Escolha um no teclado.")
        }
    }

    // v0.25: the kid's home as it opens inside the bubble's phone-sized window, at 1.3× text.
    @Test fun settingsBubble() = shot("p21-settings-bubble", bigText = true) {
        Column(Modifier.padding(OTokens.Spacing.space4)) { app.oie.ui.parent.BubbleSetting(ready().apply { bubbleOn = true }) }
    }
    @Test fun kidHomeInTheBubble() = shot("p20-kid-home-bubble", bigText = true) { KidHome(ready()) }

    // v0.27: videos and a GIF in the chat on a phone, at 1.3× text.
    @Test fun conversationVideos() = shot("p22-conversation-videos", bigText = true) {
        Conversation(ready().apply {
            messages += app.oie.logic.Message("v1", "vovo", false, now - 15 * 60 * 1000, app.oie.logic.Kind.VIDEO, "Tito, olha o gato", durationSec = 42)
            messages += app.oie.logic.Message("v2", "vovo", false, now - 12 * 60 * 1000, app.oie.logic.Kind.VIDEO, durationSec = 300, size = 90L * 1024 * 1024)
            messages += app.oie.logic.Message("g1", "vovo", false, now - 10 * 60 * 1000, app.oie.logic.Kind.GIF, durationSec = 3)
        }, "vovo")
    }

    // v0.28: the photo-or-video sheet and a video's preview on a phone, at 1.3× text.
    @Test fun photoOrVideoSheet() = shot("p23-photo-or-video-sheet", bigText = true) { app.oie.ui.kid.PhotoSourceSheet("camera", {}, {}) }
    @Test fun videoTooLong() = shot("p24-video-too-long", bigText = true) {
        app.oie.ui.kid.VideoPreview(app.oie.ui.kid.VideoDraft(android.net.Uri.parse("content://test/clip"), gif = false, problem = app.oie.data.VideoPrep.Why.TOO_LONG), "Vovó", {}, {}, {})
    }

    // v0.29: a chat's bubble window: only that chat, the field focused (the keyboard comes up on a device).
    @Test fun chatBubble() = shot("p25-chat-bubble", bigText = true) { Conversation(ready(), "vovo", inBubble = true, onLeave = {}) }
    @Test fun chatBubbleTyping() = shot("p26-chat-bubble-typing", bigText = true) {
        Conversation(ready().apply { drafts["vovo"] = "Oi vó, tudo bem? Domingo eu vou" }, "vovo", inBubble = true, onLeave = {})
    }

    // v0.33: the family features on a phone at 1.3x text.
    private fun family() = ready().apply {
        nudgeOn = true
        allowed[0] = allowed[0].copy(birthday = "11-18"); allowed[1] = allowed[1].copy(needYou = true)
        messages += app.oie.logic.Message("t1", "vovo2", true, now - 9L * 24 * 60 * 60 * 1000, app.oie.logic.Kind.TEXT, "Oi vô")
        val i = messages.indexOfFirst { it.id == "s3" }
        messages[i] = messages[i].copy(reactions = mapOf("kid" to "❤️", "x" to "❤️"))
        messages += app.oie.logic.Message("k9", "vovo", true, now - 5 * 60 * 1000, app.oie.logic.Kind.TEXT, "Também tô com saudade", receipt = app.oie.logic.Receipt.DELIVERED)
    }
    @Test fun kidHomeFamily() = shot("p27-kid-home-family", bigText = true) { KidHome(family()) }
    @Test fun conversationFamily() = shot("p28-conversation-family", bigText = true) { Conversation(family(), "vovo") }
    @Test fun drawPad() = shot("p29-draw-pad", bigText = true) { app.oie.ui.kid.DrawPad("Vovó", {}, {}) }
    @Test fun reactionSheet() = shot("p30-reaction-sheet", bigText = true) { val s = family(); app.oie.ui.kid.ReactionSheet(s.messages.first { it.id == "s3" }, {}, {}, {}) }
    @Test fun contactEditorFamily() = shot("p31-contact-editor-family", bigText = true) {
        val s = family()
        androidx.compose.foundation.layout.Column(Modifier.padding(OTokens.Spacing.space4)) { app.oie.ui.parent.ContactEditor(s.allowed[1].copy(birthday = "31/02"), "Tito", s.link.contacts(), {}, {}, onRemove = {}) }
    }
    @Test fun listenBeforeSending() = shot("p32-listen-before-sending", bigText = true) {
        Conversation(family(), "vovo", pendingPreview = app.oie.logic.Message("kv", "vovo", true, now, app.oie.logic.Kind.VOICE, durationSec = 8, media = "missing.sealed"))
    }
    @Test fun photosGrid() = shot("p33-photos", bigText = true) { val s = family(); app.oie.ui.kid.PhotosScreen(s, s.allowed[0], {}, {}) }

    @Test fun setupWelcome() = shot("p34-setup-welcome", bigText = true) { app.oie.ui.setup.Setup(AppState(), 0, onClose = {}) }
    @Test fun setupQuiet() = shot("p35-setup-quiet", bigText = true) { app.oie.ui.setup.Setup(ready(), 6, onClose = {}) }
    @Test fun setupDone() = shot("p36-setup-done", bigText = true) { app.oie.ui.setup.Setup(ready(), 7, onClose = {}) }
    @Test fun periodEditor() = shot("p37-period-editor", bigText = true) { val s = ready(); androidx.compose.foundation.layout.Column(Modifier.padding(OTokens.Spacing.space4)) { app.oie.ui.parent.PeriodEditor(s.periods[0], {}, {}, {}) } }
    @Test fun parentPin() = shot("p38-parent-pin", bigText = true) { app.oie.ui.parent.ParentPin(ready()) }
    @Test fun pairQr() = shot("p39-pair", bigText = true) { app.oie.ui.parent.PairScreen(ready().apply { linkState.on = true; linkState.status = app.oie.data.LinkState.Status.Qr("2@sample-code-for-the-screenshot,abc,def,ghi") }) }
    @Test fun videoScreen() = shot("p40-video-screen", bigText = true) { app.oie.ui.kid.VideoScreen(null, gif = false, from = "Vovó", onBack = {}) }
    @Test fun needYouAsk() = shot("p41-need-you-ask", bigText = true) { androidx.compose.foundation.layout.Box(Modifier.padding(OTokens.Spacing.space4)) { app.oie.ui.kid.NeedYouCard("Mãe e Vovô", {}, {}) } }
    @Test fun forgotPin() = shot("p42-forgot-pin", bigText = true) { app.oie.ui.parent.ForgotPin(ready().apply { realMode = true }, 0) }
}
