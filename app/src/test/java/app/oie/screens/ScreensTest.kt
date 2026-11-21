package app.oie.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
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

    @org.junit.Before fun pinTimeZone() { java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC")); app.oie.data.Background.dispatcher = kotlinx.coroutines.Dispatchers.Unconfined }

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
    @Config(qualifiers = "w800dp-h1280dp-port-mdpi")
    @Test fun kidHomePortrait() = shot("11-kid-home-portrait", false) { KidHome(ready().apply { kidNoticeSeen = true }) }
    @Config(qualifiers = "w800dp-h1280dp-port-mdpi")
    @Test fun conversationPortrait() = shot("12-conversation-portrait", false) { Conversation(ready().apply { kidNoticeSeen = true }, "vovo") }
    @Config(qualifiers = "w800dp-h1280dp-port-mdpi")
    @Test fun parentPortrait() = shot("13-parent-portrait", false) { ParentArea(ready(), 0) }
    @Test fun kidHomeEmpty() = shot("14-kid-home-empty", false) { KidHome(AppState(clock = { now }).apply { parentName = "Ana"; kidName = "Tito"; link.pretendLinked(); kidNoticeSeen = true; route = app.oie.data.Route.KidHome }) }
    @Test fun parentSeesRequest() = shot("15-parent-request", false) { ParentArea(ready().apply { addRequested = true }, 0) }
    @Test fun riskNotice() = shot("05-setup-risk", false) { Setup(AppState(), 1, onClose = {}) }
    @Test fun names() = shot("06-setup-names", false) { Setup(AppState().apply { parentName = "Ana"; kidName = "Tito" }, 3, onClose = {}) }
    @Test fun createPin() = shot("07-setup-pin", false) { Setup(AppState(), 4, onClose = {}) }
    @Test fun parentPin() = shot("08-parent-pin", true) { ParentPin(ready()) }
    @Test fun parentContacts() = shot("09-parent-contacts", false) { ParentArea(ready(), 0) }
    @Test fun parentQuiet() = shot("10-parent-quiet", true) { ParentArea(ready(), 1) }
    // v0.8: Conexão once the real WhatsApp is linked (offer to use it with the kid), and setup's real link step.
    @Test fun setupLinkReal() = shot("20-setup-link", false) { Setup(AppState(), 2, onClose = {}) }
    @Test fun conversationNotSent() = shot("21-conversation-not-sent", false) {
        Conversation(ready().apply { kidNoticeSeen = true; messages += app.oie.logic.Message("k9", "vovo", true, now - 60_000, app.oie.logic.Kind.TEXT, "Vó, posso ir aí domingo?"); failed += "k9" }, "vovo")
    }
    // v0.10: the sticker sheet with a saved family sticker and a new one to keep (no images on the JVM: the slots show empty).
    @Test fun stickerSheetFamily() = shot("22-stickers-family", false) {
        Conversation(ready().apply {
            kidNoticeSeen = true
            offerSticker(app.oie.logic.FamilySticker("f1", "missing-1", false, false, now)); saveSticker("f1")
            offerSticker(app.oie.logic.FamilySticker("f2", "missing-2", false, false, now))
        }, "vovo", openStickers = true)
    }
    // v0.14: Ajustes e ajuda after Oiê closed by itself: the report leads the section.
    @Test fun problemsAfterACrash() {
        app.oie.data.Diagnostics.init(java.nio.file.Files.createTempDirectory("diag").toFile(), "Oiê test")
        app.oie.data.Diagnostics.log("send PHOTO: failed: test"); app.oie.data.Diagnostics.recordCrash(now)
        shot("19-problems-crash", false) { ParentArea(ready(), 3) }
        app.oie.data.Diagnostics.clear()
    }
    // v0.17: the contacts arriving after linking, in setup and in Contatos; and when they're slow.
    private fun linking(sinceMs: Long, people: Int = 0, synced: Boolean = false) = ready().apply {
        realMode = true
        val list = (1..people).map { app.oie.logic.Contact("p$it@s.whatsapp.net", "Pessoa $it") } +
            listOf(app.oie.logic.Contact("g1@g.us", "Família", isGroup = true), app.oie.logic.Contact("g2@g.us", "Escola do Tito", isGroup = true))
        real = object : app.oie.data.WhatsAppLink {
            override val connected = true
            override val profileName: String? = null
            override fun contacts() = list
            override fun send(message: app.oie.logic.Message) {}
        }
        allowed.clear()
        linkState.status = app.oie.data.LinkState.Status.Connected
        linkState.linkedAt = now - sinceMs; linkState.contactsSynced = synced
    }
    @Test fun setupContactsSyncing() = shot("20b-setup-contacts-syncing", false) { Setup(linking(5_000, people = 3), 5, onClose = {}) }
    @Test fun setupContactsSlow() = shot("20c-setup-contacts-slow", false) { Setup(linking(90_000), 5, onClose = {}) }
    @Test fun contactsSyncingDark() = shot("20d-contacts-syncing", true) { ParentArea(linking(5_000), 0) }
    // v0.12: Conexão unlinked, the pairing QR, and Contatos with "Adicionar pelo número".
    @Test fun connectionUnlinked() = shot("16-connection-unlinked", false) { ParentArea(ready().apply { realMode = true; link.pretendDropped() }, 2) }
    @Test fun pairQr() = shot("17-pair-qr", true) {
        app.oie.ui.parent.PairScreen(ready().apply { linkState.on = true; linkState.status = app.oie.data.LinkState.Status.Qr("2@sample-code-for-the-screenshot,abc,def,ghi") })
    }
    @Test fun contactsAddByNumber() = shot("18-contacts-add-by-number", false) { ParentArea(ready().apply { realMode = true }, 0) }

    // v0.19: a name for the kid, and the number for the parent.
    private fun renamed() = ready().apply { allowed[0] = allowed[0].copy(waName = "Maria Silva") }
    @Test fun contactsRenamed() = shot("23-contacts-renamed", false) { ParentArea(renamed(), 0) }
    @Test fun contactsRenamedDark() = shot("23-contacts-renamed", true) { ParentArea(renamed(), 0) }
    @Test fun contactEditor() = shot("24-contact-editor", false) {
        val s = renamed()
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.ContactEditor(s.allowed[0], "Tito", s.link.contacts(), {}, {}, onRemove = {}) }
    }
    @Test fun contactEditorDark() = shot("24-contact-editor", true) {
        val s = renamed()
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.ContactEditor(s.allowed[0], "Tito", s.link.contacts(), {}, {}, onRemove = {}) }
    }
    @Test fun setupContactsPicked() = shot("25-setup-contacts-picked", false) { Setup(renamed(), 5, onClose = {}) }

    // v0.21: nicknames, and a chat paused from the parent's WhatsApp.
    private fun paused() = ready().apply { allowed[1] = allowed[1].copy(paused = true); kidNicknames += listOf("Titinho", "Ti") }
    @Test fun contactsPaused() = shot("26-contacts-paused", false) { ParentArea(paused(), 0) }
    @Test fun contactsPausedDark() = shot("26-contacts-paused", true) { ParentArea(paused(), 0) }
    @Test fun kidHomeWithAPausedChat() = shot("27-kid-home-paused", false) { KidHome(paused().apply { kidNoticeSeen = true }) }

    // v0.22: the emoji before the kid's bold name, and its preview.
    @Test fun settingsSignatureDark() = shot("28-settings-signature", true) { ParentArea(ready().apply { kidEmoji = "😎" }, 3) }

    // v0.24: right after an update the link is on its way: no "desconectado", no Connect button.
    private fun reconnecting() = ready().apply {
        kidNoticeSeen = true; realMode = true; link.pretendDropped()
        linkState.on = true; linkState.status = app.oie.data.LinkState.Status.Connecting
    }
    @Test fun kidHomeReconnecting() = shot("29-kid-home-reconnecting", false) { KidHome(reconnecting()) }
    @Test fun connectionReconnecting() = shot("30-connection-reconnecting", true) { ParentArea(reconnecting(), 2) }

    // v0.25: Bolha na tela, turned on (on the test device Android hasn't allowed bubbles yet).
    @Test fun settingsBubble() = shot("31-settings-bubble", false) {
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.BubbleSetting(ready().apply { bubbleOn = true }) }
    }
    @Test fun settingsBubbleOffDark() = shot("31-settings-bubble", true) {
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.BubbleSetting(ready()) }
    }

    // v0.42: the kid arranges the home: Vovó a favorite, the others in order, arrows and hearts.
    private fun arranged() = ready().apply { kidNoticeSeen = true; toggleFavorite(allowed[2].id) }
    @Test fun kidHomeArrange() = shot("72-kid-home-arrange", false) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.kid.Arrange(arranged()) } }
    @Test fun kidHomeArrangeDark() = shot("72-kid-home-arrange", true) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.kid.Arrange(arranged()) } }
    @Test fun kidHomeFavorites() = shot("73-kid-home-favorites", false) { KidHome(arranged()) }
    // v0.42: a conversation that is the kid's own says so in Contatos.
    @Test fun contactsKidsOwn() = shot("74-contacts-kids-own", false) { ParentArea(ready().apply { allowed[4] = allowed[4].copy(showWholeGroup = true) }, 0) }

    // v0.41: the unread mark on the parent's own line, off by default (low-risk pass).
    @Test fun settingsUnreadMark() = shot("71-settings-unread-mark", false) {
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.UnreadMarkSetting(ready().apply { allowed.add(0, app.oie.logic.Contact("1203630@g.us", "Ana", isGroup = true, parentLine = true)) }) }
    }
    @Test fun settingsUnreadMarkOnDark() = shot("71-settings-unread-mark", true) {
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.UnreadMarkSetting(ready().apply { markUnreadOn = true; allowed.add(0, app.oie.logic.Contact("1203630@g.us", "Ana", isGroup = true, parentLine = true)) }) }
    }

    // v0.26: WhatsApp profile pictures where Oiê has them; the letter circle for the rest.
    private fun withPictures() = ready().apply {
        kidNoticeSeen = true
        app.oie.data.Photos.sealer = PassThrough
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        fun picture(top: Int, bottom: Int): String {
            val b = android.graphics.Bitmap.createBitmap(160, 160, android.graphics.Bitmap.Config.ARGB_8888)
            android.graphics.Canvas(b).apply {
                drawColor(top)
                drawCircle(80f, 66f, 30f, android.graphics.Paint().apply { color = bottom; isAntiAlias = true })
                drawCircle(80f, 170f, 62f, android.graphics.Paint().apply { color = bottom; isAntiAlias = true })
            }
            return app.oie.data.Photos.store(context, b)
        }
        allowed[0] = allowed[0].copy(photo = picture(0xFF8FB8C9.toInt(), 0xFF3E5A66.toInt()))
        allowed[2] = allowed[2].copy(photo = picture(0xFFE3C7A1.toInt(), 0xFF6B4E2E.toInt()))
    }
    @Test fun kidHomePictures() = shot("32-kid-home-pictures", false) { KidHome(withPictures()) }
    @Test fun kidHomePicturesDark() = shot("32-kid-home-pictures", true) { KidHome(withPictures()) }
    @Test fun conversationPicture() = shot("33-conversation-picture", false) { Conversation(withPictures(), "vovo") }
    @Test fun contactsPictures() = shot("34-contacts-pictures", false) { ParentArea(withPictures(), 0) }

    // v0.27: a video (not downloaded yet in the test), a video too big to keep, and a GIF.
    private fun withVideos() = ready().apply {
        kidNoticeSeen = true
        messages += app.oie.logic.Message("v1", "vovo", false, now - 15 * 60 * 1000, app.oie.logic.Kind.VIDEO, "Tito, olha o gato", durationSec = 42)
        messages += app.oie.logic.Message("v2", "vovo", false, now - 12 * 60 * 1000, app.oie.logic.Kind.VIDEO, durationSec = 300, size = 90L * 1024 * 1024)
        messages += app.oie.logic.Message("g1", "vovo", false, now - 10 * 60 * 1000, app.oie.logic.Kind.GIF, durationSec = 3)
    }
    @Test fun conversationVideos() = shot("35-conversation-videos", false) { Conversation(withVideos(), "vovo") }
    @Test fun conversationVideosDark() = shot("35-conversation-videos", true) { Conversation(withVideos(), "vovo") }
    @Test fun videoScreen() = shot("36-video-screen", false) { app.oie.ui.kid.VideoScreen(null, gif = false, from = "Vovó", onBack = {}) }

    // v0.28: sending videos and GIFs.
    private val anyUri = android.net.Uri.parse("content://test/clip")
    @Test fun attachSheet() = shot("37-attach-sheet", false) { app.oie.ui.kid.AttachSheet(ready(), "camera", {}, {}, {}, {}, {}) }
    @Test fun attachSheetDark() = shot("37-attach-sheet", true) { app.oie.ui.kid.AttachSheet(ready(), "camera", {}, {}, {}, {}, {}) }
    @Test fun videoPreparing() = shot("38-video-preparing", false) { app.oie.ui.kid.VideoPreview(app.oie.ui.kid.VideoDraft(anyUri, gif = false, progress = 0.4f), "Vovó", {}, {}, {}) }
    @Test fun videoTooLong() = shot("38-video-too-long", true) {
        app.oie.ui.kid.VideoPreview(app.oie.ui.kid.VideoDraft(anyUri, gif = false, problem = app.oie.data.VideoPrep.Why.TOO_LONG), "Vovó", {}, {}, {})
    }
    @Test fun videoReady() = shot("38-video-ready", false) {
        app.oie.ui.kid.VideoPreview(app.oie.ui.kid.VideoDraft(anyUri, gif = false, progress = 1f, ready = app.oie.data.VideoPrep.Result.Ready("missing.sealed", 42, gif = false)), "Vovó", {}, {}, {})
    }

    // v0.30: the line to the parent who owns the WhatsApp: the card before, the contact after.
    @Test fun contactsParentLineCard() = shot("39-contacts-parent-line", false) { ParentArea(ready().apply { realMode = true }, 0) }
    @Test fun contactsParentLineCardDark() = shot("39-contacts-parent-line", true) { ParentArea(ready().apply { realMode = true }, 0) }
    @Test fun contactsWithParentLine() = shot("40-contacts-with-line", false) {
        ParentArea(ready().apply { realMode = true; allowed.add(0, app.oie.logic.Contact("1203630@g.us", "Ana", isGroup = true, parentLine = true)) }, 0)
    }

    // v0.33: the family features (docs/flows/family-features.md). The tests' day is 18 November.
    private fun family() = ready().apply {
        kidNoticeSeen = true; nudgeOn = true
        allowed[0] = allowed[0].copy(birthday = "11-18"); allowed[1] = allowed[1].copy(needYou = true)
        messages += Message("t1", "vovo2", true, now - 9L * 24 * 60 * 60 * 1000, Kind.TEXT, "Oi vô")
        val i = messages.indexOfFirst { it.id == "s3" }
        messages[i] = messages[i].copy(reactions = mapOf("kid" to "❤️"))
        messages += Message("k9", "vovo", true, now - 5 * 60 * 1000, Kind.TEXT, "Também tô com saudade", receipt = app.oie.logic.Receipt.READ, reactions = mapOf("vovo" to "😂"))
    }
    @Test fun kidHomeFamily() = shot("41-kid-home-family", false) { KidHome(family()) }
    @Test fun kidHomeFamilyDark() = shot("41-kid-home-family", true) { KidHome(family()) }
    @Test fun conversationFamily() = shot("42-conversation-family", false) { Conversation(family(), "vovo") }
    @Test fun conversationFamilyDark() = shot("42-conversation-family", true) { Conversation(family(), "vovo") }
    @Test fun reactionSheet() = shot("43-reaction-sheet", false) {
        val s = family(); app.oie.ui.kid.ReactionSheet(s.messages.first { it.id == "s3" }, {}, {}, {})
    }
    @Test fun reactionSheetDark() = shot("43-reaction-sheet", true) {
        val s = family(); app.oie.ui.kid.ReactionSheet(s.messages.first { it.id == "s3" }, {}, {}, {})
    }
    @Test fun photosEmpty() = shot("44-photos-empty", false) { val s = family(); app.oie.ui.kid.PhotosScreen(s, s.allowed[1], {}, {}) }
    @Test fun photosGrid() = shot("44-photos-grid", true) {
        val s = withVideos(); s.messages.replaceAll { if (it.kind != Kind.TEXT && it.kind != Kind.VOICE) it.copy(media = "missing.sealed") else it }
        app.oie.ui.kid.PhotosScreen(s, s.allowed[0], {}, {})
    }
    @Test fun drawPad() = shot("45-draw-pad", false) { app.oie.ui.kid.DrawPad("Vovó", {}, {}) }
    @Test fun drawPadDark() = shot("45-draw-pad", true) { app.oie.ui.kid.DrawPad("Vovó", {}, {}) }
    @Test fun contactEditorFamily() = shot("46-contact-editor-family", false) {
        val s = family()
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.ContactEditor(s.allowed[1].copy(birthday = "03-07"), "Tito", s.link.contacts(), {}, {}, onRemove = {}) }
    }
    @Test fun contactEditorFamilyDark() = shot("46-contact-editor-family", true) {
        val s = family()
        Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.ContactEditor(s.allowed[1].copy(birthday = "03-07"), "Tito", s.link.contacts(), {}, {}, onRemove = {}) }
    }
    @Test fun settingsNudge() = shot("47-settings-nudge", false) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.NudgeSetting(family()) } }
    @Test fun settingsNudgeDark() = shot("47-settings-nudge", true) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.NudgeSetting(family().apply { nudgeOn = false }) } }
    @Test fun needYouAsk() = shot("48-need-you-ask", false) { Box(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.kid.NeedYouCard("Mãe", {}, {}) } }
    @Test fun needYouAskDark() = shot("48-need-you-ask", true) { Box(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.kid.NeedYouCard("Mãe e Vovô", {}, {}) } }
    @Test fun listenBeforeSending() = shot("49-listen-before-sending", false) {
        Conversation(family(), "vovo", pendingPreview = Message("kv", "vovo", true, now, Kind.VOICE, durationSec = 8, media = "missing.sealed"))
    }
    @Test fun kidHomeFamilyEnglish() = shot("41-kid-home-family-en", false, english = true) { KidHome(family()) }

    // v0.34: the screens that had no screenshot yet, so the design pass can see them.
    @Test fun setupWelcome() = shot("50-setup-welcome", false) { Setup(AppState(), 0, onClose = {}) }
    @Test fun setupWelcomeDark() = shot("50-setup-welcome", true) { Setup(AppState(), 0, onClose = {}) }
    @Test fun setupQuiet() = shot("51-setup-quiet", false) { Setup(ready(), 6, onClose = {}) }
    @Test fun setupDone() = shot("52-setup-done", false) { Setup(ready(), 7, onClose = {}) }
    @Test fun setupDoneDark() = shot("52-setup-done", true) { Setup(ready(), 7, onClose = {}) }
    @Test fun periodEditor() = shot("53-period-editor", false) {
        val s = ready(); Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.PeriodEditor(s.periods[0], {}, {}, {}) }
    }
    @Test fun periodEditorNew() = shot("53-period-editor-new", true) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.PeriodEditor(null, {}, null, {}) } }
    @Test fun connectionConnected() = shot("54-connection-connected", false) { ParentArea(ready().apply { realMode = true; linkState.on = true; linkState.status = app.oie.data.LinkState.Status.Connected }, 2) }
    // v0.34: Esqueci o PIN, the proof first (link WhatsApp again), then the new PIN.
    @Test fun forgotPin() = shot("55-forgot-pin", false) { app.oie.ui.parent.ForgotPin(ready().apply { realMode = true }, 0) }
    @Test fun forgotPinDark() = shot("55-forgot-pin", true) { app.oie.ui.parent.ForgotPin(ready().apply { realMode = true }, 0) }
    @Test fun forgotPinNew() = shot("55-forgot-pin-new", false) { app.oie.ui.parent.ForgotPin(ready(), 1) }
    // v0.34 round 2: every state of setup step 2, the forgot-PIN QR, setup in dark, and the home offline.
    private fun linkIn(status: app.oie.data.LinkState.Status, failed: Boolean = false) = AppState(clock = { now }).apply {
        linkState.on = true; linkState.status = status; linkState.pairFailed = failed
    }
    private val qr = app.oie.data.LinkState.Status.Qr("2@sample-code-for-the-screenshot,abc,def,ghi")
    @Test fun linkQr() = shot("56-link-qr", false) { Setup(linkIn(qr), 2, onClose = {}) }
    @Test fun linkWaiting() = shot("56-link-waiting", false) { Setup(linkIn(app.oie.data.LinkState.Status.Connecting), 2, onClose = {}) }
    @Test fun linkFailed() = shot("56-link-failed", false) { Setup(linkIn(app.oie.data.LinkState.Status.Off, failed = true), 2, onClose = {}) }
    @Test fun linkOffline() = shot("56-link-offline", true) { Setup(linkIn(app.oie.data.LinkState.Status.Disconnected), 2, onClose = {}) }
    @Test fun linkAlready() = shot("56-link-already", false) { Setup(ready().apply { realMode = true; linkState.on = true; linkState.status = app.oie.data.LinkState.Status.Connected }, 2, onClose = {}) }
    @Test fun forgotPinQr() = shot("55-forgot-pin-qr", false) { app.oie.ui.parent.ForgotPin(ready().apply { realMode = true; pinResetFrom = "x"; linkState.on = true; linkState.status = qr }, 0) }
    @Test fun namesDark() = shot("06-setup-names", true) { Setup(AppState().apply { parentName = "Ana"; kidName = "Tito" }, 3, onClose = {}) }
    @Test fun pinDark() = shot("07-setup-pin", true) { Setup(AppState(), 4, onClose = {}) }
    @Test fun contactsDark() = shot("25-setup-contacts-picked", true) { Setup(renamed(), 5, onClose = {}) }
    @Test fun quietDark() = shot("51-setup-quiet", true) { Setup(ready(), 6, onClose = {}) }
    @Test fun kidHomeOffline() {
        // Oiê opened two minutes ago, so the 30-second grace is over (LinkView).
        var t = now - 120_000
        val s = AppState(clock = { t }).apply {
            parentName = "Ana"; kidName = "Tito"; link.pretendLinked(); allowed.addAll(link.contacts().take(5)); setPin("402817"); finishSetup()
            kidNoticeSeen = true; realMode = true; link.pretendDropped(); linkState.on = true
            linkState.status = app.oie.data.LinkState.Status.Disconnected; linkState.downSince = now - 120_000
        }
        t = now
        shot("57-kid-home-offline", false) { KidHome(s) }
    }
    // v0.34 round 3: recording, its last 10 s, the slip line, and a long chat that opens on "Novas".
    @Test fun recording() = shot("58-recording", false) { Conversation(family(), "vovo", recordingPreviewSec = 5) }
    @Test fun recordingEnds() = shot("58-recording-ends", true) { Conversation(family(), "vovo", recordingPreviewSec = 115) }
    @Test fun recordingSlip() = shot("58-recording-slip", false) { Conversation(family(), "vovo", slipPreview = true) }
    @Test fun longChatOpensOnNew() = shot("59-long-chat-new", false) {
        val s = ready().apply {
            kidNoticeSeen = true
            messages.removeAll { it.chatId == "mae" }
            repeat(30) { k -> messages += Message("m$k", "mae", k % 3 == 0, now - (60 - k) * 60_000L, Kind.TEXT, "Mensagem ${k + 1}") }
            lastSeen["mae"] = now - (60 - 24) * 60_000L - 1
        }
        Conversation(s, "mae")
    }

    // v0.36: family statuses (docs/flows/statuses.md). The clock is held so the 5 s timer doesn't move on.
    private fun statuses() = ready().apply {
        kidNoticeSeen = true; statusesOn = true; linkedSince = now - 20L * 60 * 60 * 1000
        statuses += app.oie.logic.Status("st1", "vovo", "vovo", now - 2L * 60 * 60 * 1000, Kind.TEXT, "Bolo de laranja saindo do forno")
        statuses += app.oie.logic.Status("st2", "vovo", "vovo", now - 40L * 60 * 1000, Kind.TEXT, "Quem vem no domingo?")
        statuses += app.oie.logic.Status("st3", "bia", "bia", now - 5L * 60 * 60 * 1000, Kind.TEXT, "Praia hoje", viewed = true)
    }
    private fun held(name: String, dark: Boolean, content: @Composable () -> Unit) { compose.mainClock.autoAdvance = false; shot(name, dark, content = content) }
    @Test fun kidHomeStatuses() = shot("60-kid-home-statuses", false) { KidHome(statuses()) }
    @Test fun kidHomeStatusesDark() = shot("60-kid-home-statuses", true) { KidHome(statuses()) }
    @Test fun statusViewer() = held("61-status-viewer", false) { app.oie.ui.kid.StatusViewer(statuses(), "vovo", {}) }
    @Test fun statusViewerDark() = held("61-status-viewer", true) { app.oie.ui.kid.StatusViewer(statuses().apply { statuses[0] = statuses[0].copy(reaction = "❤️") }, "vovo", {}) }
    @Test fun statusReply() = held("62-status-reply", false) { app.oie.ui.kid.StatusViewer(statuses(), "vovo", {}, startReplying = true) }
    @Test fun statusReplyDark() = held("62-status-reply", true) { app.oie.ui.kid.StatusViewer(statuses(), "vovo", {}, startReplying = true) }
    @Test fun settingsStatuses() = shot("63-settings-statuses", false) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.StatusSetting(statuses()) } }
    @Test fun settingsStatusesDark() = shot("63-settings-statuses", true) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.StatusSetting(statuses().apply { statusesOn = false }) } }
    @Test fun conversationStatusReply() = shot("64-conversation-status-reply", false) {
        Conversation(statuses().apply { messages += Message("kr", "vovo", true, now - 60_000, Kind.TEXT, "Quero um pedaço", replyTo = "st1", statusQuote = "Resposta a uma novidade") }, "vovo")
    }

    // v0.37: the kid posts a status.
    private fun posting(failed: Boolean = false) = statuses().apply {
        postingOn = true
        statuses += app.oie.logic.Status("me1", app.oie.logic.Statuses.MINE, "", now - 30L * 60 * 1000, Kind.TEXT, "Fiz um desenho do Bento",
            viewed = true, audience = listOf("vovo", "mae", "vovo2", "bia"), failed = failed)
    }
    @Test fun kidHomePosting() = shot("65-kid-home-posting", false) { KidHome(posting()) }
    @Test fun kidHomePostingDark() = shot("65-kid-home-posting", true) { KidHome(posting()) }
    @Test fun postSheet() = shot("66-post-sheet", false) { val s = posting(); app.oie.ui.kid.PostSheet(s, false, {}, {}) }
    @Test fun postSheetDark() = shot("66-post-sheet", true) { val s = posting(); app.oie.ui.kid.PostSheet(s, false, {}, {}) }
    @Test fun postSheetNobody() = shot("67-post-sheet-nobody", false) {
        val s = posting().apply { for (k in allowed.indices) if (!allowed[k].isGroup) allowed[k] = allowed[k].copy(paused = true) }
        app.oie.ui.kid.PostSheet(s, false, {}, {})
    }
    @Test fun statusWriter() = shot("68-status-writer", false) { app.oie.ui.kid.StatusWriter(posting(), {}, {}, start = "Fiz um desenho do Bento") }
    @Test fun statusWriterDark() = shot("68-status-writer", true) { app.oie.ui.kid.StatusWriter(posting(), {}, {}) }
    @Test fun ownStatus() = held("69-own-status", false) { app.oie.ui.kid.StatusViewer(posting(), app.oie.logic.Statuses.MINE, {}) }
    @Test fun ownStatusDark() = held("69-own-status", true) { app.oie.ui.kid.StatusViewer(posting(), app.oie.logic.Statuses.MINE, {}) }
    @Test fun ownStatusFailed() = held("69-own-status-failed", false) { app.oie.ui.kid.StatusViewer(posting(failed = true), app.oie.logic.Statuses.MINE, {}) }
    @Test fun settingsPosting() = shot("70-settings-posting", false) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.StatusSetting(posting()) } }
    @Test fun settingsPostingDark() = shot("70-settings-posting", true) { Column(Modifier.padding(OTokens.Spacing.space12)) { app.oie.ui.parent.StatusSetting(posting().apply { postingOn = false }) } }
    /** v0.38: every kind of preview on the home: unread text, a photo with its words, a sticker, a voice message that didn't go, a long old text. */
    private fun previews() = ready().apply {
        kidNoticeSeen = true
        val day = 24L * 60 * 60 * 1000
        messages.removeAll { true }
        messages += app.oie.logic.Message("v1", "vovo", false, now - 5 * 60_000, app.oie.logic.Kind.TEXT, "Tito, olha o bolo que eu fiz hoje para o domingo")
        messages += app.oie.logic.Message("m1", "mae", true, now - day, app.oie.logic.Kind.PHOTO, "Olha meu desenho")
        messages += app.oie.logic.Message("z1", "vovo2", true, now - 3 * day, app.oie.logic.Kind.STICKER, "Te amo")
        messages += app.oie.logic.Message("b1", "bia", true, now - 60 * 60_000, app.oie.logic.Kind.VOICE, durationSec = 72)
        failed += "b1"
        messages += app.oie.logic.Message("p1", "primos", true, now - 10 * day, app.oie.logic.Kind.TEXT, "Quem vai\nno sábado?")
    }
    @Test fun homePreviews() = shot("66-kid-home-previews", false) { KidHome(previews()) }
    @Test fun homePreviewsDark() = shot("66-kid-home-previews", true) { KidHome(previews()) }
    @Test fun homePreviewsEnglish() = shot("66-kid-home-previews-en", false, english = true) { KidHome(previews()) }
    @Test fun recordingByTap() = shot("67-recording-by-tap", false) { Conversation(family(), "vovo", recordingPreviewSec = 3, tapPreview = true) }
}
