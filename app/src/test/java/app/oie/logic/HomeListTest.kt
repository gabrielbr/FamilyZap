package app.oie.logic

import app.oie.logic.HomeList.What
import app.oie.logic.HomeList.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class HomeListTest {
    private val zone = ZoneOffset.UTC
    private val now = LocalDateTime.of(2026, 11, 20, 15, 0) // a Friday
    private fun at(t: LocalDateTime) = t.toInstant(zone).toEpochMilli()
    private fun msg(id: String, kind: Kind, text: String = "", mine: Boolean = false, t: Long = 0, secs: Int = 0) =
        Message(id, "c", mine, t, kind, text, durationSec = secs)

    @Test fun aChatWithNothingHasNoPreview() = assertNull(HomeList.preview(emptyList()))

    @Test fun theLastMessageIsTheOne() {
        val p = HomeList.preview(listOf(msg("1", Kind.TEXT, "Oi", t = 1), msg("2", Kind.TEXT, "Saudade, vó", mine = true, t = 2)))!!
        assertEquals(What.TEXT, p.what); assertEquals("Saudade, vó", p.text); assertTrue(p.mine); assertEquals(2L, p.at)
    }

    @Test fun reactionsNeverBecomeThePreview() {
        val p = HomeList.preview(listOf(msg("1", Kind.TEXT, "Oi", t = 1), msg("2", Kind.REACTION, "❤", t = 2)))!!
        assertEquals("Oi", p.text)
    }

    @Test fun mediaSaysWhatItIs() {
        assertEquals(What.VOICE, HomeList.preview(listOf(msg("1", Kind.VOICE, secs = 12)))!!.what)
        assertEquals(12, HomeList.preview(listOf(msg("1", Kind.VOICE, secs = 12)))!!.seconds)
        assertEquals(What.STICKER, HomeList.preview(listOf(msg("1", Kind.STICKER, "Te amo")))!!.what)
        assertEquals("", HomeList.preview(listOf(msg("1", Kind.STICKER, "Te amo")))!!.text) // the word isn't a caption
        assertEquals(What.OTHER, HomeList.preview(listOf(msg("1", Kind.UNSUPPORTED)))!!.what)
        assertEquals(What.DELETED, HomeList.preview(listOf(msg("1", Kind.DELETED)))!!.what)
    }

    @Test fun aPhotoKeepsItsCaption() = assertEquals("Olha o bolo", HomeList.preview(listOf(msg("1", Kind.PHOTO, " Olha o bolo ")))!!.text)

    @Test fun longTextBecomesOneLine() = assertEquals("Oi vó tudo bem?", HomeList.oneLine("Oi vó\n\n tudo   bem?"))

    @Test fun aFailedSendIsSaid() {
        assertTrue(HomeList.preview(listOf(msg("1", Kind.TEXT, "Oi", mine = true)), failed = setOf("1"))!!.failed)
        // Only the kid's own messages can fail.
        assertFalse(HomeList.preview(listOf(msg("1", Kind.TEXT, "Oi")), failed = setOf("1"))!!.failed)
    }

    @Test fun todayShowsTheTime() = assertEquals(When.Time(9, 5), HomeList.whenOf(at(now.withHour(9).withMinute(5)), now, zone))
    @Test fun yesterdayIsSaid() = assertEquals(When.Yesterday, HomeList.whenOf(at(now.minusDays(1).withHour(23)), now, zone))
    @Test fun thisWeekShowsTheDay() = assertEquals(When.Weekday(1), HomeList.whenOf(at(now.minusDays(4)), now, zone)) // Monday
    @Test fun olderShowsTheDate() = assertEquals(When.Date(13, 11), HomeList.whenOf(at(now.minusDays(7)), now, zone))
    @Test fun aClockAheadStillReadsAsToday() = assertEquals(When.Time(15, 30), HomeList.whenOf(at(now.plusMinutes(30)), now, zone))
}
