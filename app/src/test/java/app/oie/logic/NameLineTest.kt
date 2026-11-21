package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NameLineTest {
    private val now = 10_000_000L

    @Test fun captionedKindsCarryTheNameThemselves() {
        for (k in listOf(Kind.TEXT, Kind.PHOTO, Kind.VIDEO, Kind.GIF)) {
            assertTrue(k.name, NameLine.carriesName(k, familySticker = false))
            assertFalse(k.name, NameLine.needed(k, false, "Tito", null, now))
        }
    }

    @Test fun oiesOwnStickerGoesAsAPictureWithTheNameAsCaption() {
        assertTrue(NameLine.carriesName(Kind.STICKER, familySticker = false))
        assertFalse(NameLine.needed(Kind.STICKER, false, "Tito", null, now))
    }

    @Test fun aVoiceNoteGetsTheNameLineFirst() = assertTrue(NameLine.needed(Kind.VOICE, false, "Tito", null, now))

    @Test fun aFamilyStickerGetsTheNameLineFirst() = assertTrue(NameLine.needed(Kind.STICKER, true, "Tito", null, now))

    @Test fun onlyOncePerBurstNotAgainWithinTwoMinutes() {
        assertFalse(NameLine.needed(Kind.VOICE, false, "Tito", now - 30_000, now))
        assertFalse(NameLine.needed(Kind.STICKER, true, "Tito", now - NameLine.BURST_MS + 1, now))
    }

    @Test fun againAfterTwoMinutes() = assertTrue(NameLine.needed(Kind.VOICE, false, "Tito", now - NameLine.BURST_MS, now))

    @Test fun aClockThatWentBackwardsSendsTheName() = assertTrue(NameLine.needed(Kind.VOICE, false, "Tito", now + 60_000, now))

    @Test fun noNameNoLine() {
        assertFalse(NameLine.needed(Kind.VOICE, false, "", null, now))
        assertFalse(NameLine.needed(Kind.VOICE, false, " * ", null, now))
    }

    @Test fun reactionsAndOthersNeverGetALine() {
        for (k in listOf(Kind.REACTION, Kind.DELETED, Kind.UNSUPPORTED)) assertFalse(k.name, NameLine.needed(k, false, "Tito", null, now))
    }
}
