package app.oie.logic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DayLineTest {
    private val zone: ZoneId = ZoneOffset.ofHours(-3)
    private fun at(y: Int, mo: Int, d: Int, h: Int, mi: Int = 0) = LocalDateTime.of(y, mo, d, h, mi).atZone(zone).toInstant().toEpochMilli()
    private val today = LocalDate.of(2026, 11, 28) // a Saturday

    @Test fun theFirstMessageAlwaysStartsADay() {
        assertEquals(setOf(0), DayLine.starts(listOf(at(2026, 11, 28, 9)), zone))
    }

    @Test fun aLineWhereTheDayChangesOnly() {
        val times = listOf(at(2026, 11, 26, 9), at(2026, 11, 26, 23, 59), at(2026, 11, 27, 0, 0), at(2026, 11, 27, 18), at(2026, 11, 28, 7))
        assertEquals(setOf(0, 2, 4), DayLine.starts(times, zone))
    }

    @Test fun noMessagesNoLines() {
        assertEquals(emptySet<Int>(), DayLine.starts(emptyList(), zone))
    }

    @Test fun theDayFollowsTheTabletsTimeZone() {
        // 01:30 UTC on the 28th is still the 27th at UTC-3.
        val t = LocalDateTime.of(2026, 11, 28, 1, 30).atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(DayLine.Day.Yesterday, DayLine.label(t, today, zone))
    }

    @Test fun todayYesterdayWeekdayDate() {
        assertEquals(DayLine.Day.Today, DayLine.label(at(2026, 11, 28, 0, 0), today, zone))
        assertEquals(DayLine.Day.Yesterday, DayLine.label(at(2026, 11, 27, 23, 59), today, zone))
        assertEquals(DayLine.Day.Weekday(3), DayLine.label(at(2026, 11, 25, 12), today, zone)) // Wednesday
        assertEquals(DayLine.Day.Weekday(7), DayLine.label(at(2026, 11, 22, 12), today, zone)) // 6 days ago: Sunday
        assertEquals(DayLine.Day.Date(21, 11, null), DayLine.label(at(2026, 11, 21, 12), today, zone)) // a week ago: the date
    }

    @Test fun anotherYearSaysTheYear() {
        assertEquals(DayLine.Day.Date(18, 11, 2025), DayLine.label(at(2025, 11, 18, 12), today, zone))
    }

    @Test fun aClockAheadOfTheTabletIsStillToday() {
        assertEquals(DayLine.Day.Today, DayLine.label(at(2026, 11, 29, 8), today, zone))
    }
}
