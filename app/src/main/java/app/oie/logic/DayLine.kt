package app.oie.logic

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * v0.58: the line for each day in a conversation (flow kid-receives-messages.md, "a line for each day").
 * Pure, so it is tested without the UI; the words come from Strings.
 */
object DayLine {

    /** What a day line says. */
    sealed interface Day {
        data object Today : Day
        data object Yesterday : Day
        /** 1 = Monday … 7 = Sunday (java.time). */
        data class Weekday(val day: Int) : Day
        /** [year] is null in the current year. */
        data class Date(val day: Int, val month: Int, val year: Int?) : Day
    }

    fun dayOf(at: Long, zone: ZoneId): LocalDate = Instant.ofEpochMilli(at).atZone(zone).toLocalDate()

    /** The indexes in [times] (oldest first) where a new day starts: always the first, then every change of day. */
    fun starts(times: List<Long>, zone: ZoneId = ZoneId.systemDefault()): Set<Int> {
        val out = mutableSetOf<Int>()
        var last: LocalDate? = null
        times.forEachIndexed { i, at ->
            val d = dayOf(at, zone)
            if (d != last) out += i
            last = d
        }
        return out
    }

    /** Today, yesterday, a weekday within the last week, else the date (with its year when it isn't this year). */
    fun label(at: Long, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Day {
        val d = dayOf(at, zone)
        return when {
            !d.isBefore(today) -> Day.Today
            d == today.minusDays(1) -> Day.Yesterday
            d.isAfter(today.minusDays(WEEK)) -> Day.Weekday(d.dayOfWeek.value)
            else -> Day.Date(d.dayOfMonth, d.monthValue, if (d.year == today.year) null else d.year)
        }
    }

    private const val WEEK = 7L
}
