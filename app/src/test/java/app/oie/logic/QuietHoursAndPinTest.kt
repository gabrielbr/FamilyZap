package app.oie.logic

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursAndPinTest {
    private val school = QuietPeriod("Escola", DayOfWeek.entries.take(5).toSet(), LocalTime.of(7, 0), LocalTime.of(12, 0))
    private val bed = QuietPeriod("Dormir", DayOfWeek.entries.toSet(), LocalTime.of(21, 0), LocalTime.of(7, 0))
    private val monday9 = LocalDateTime.of(2026, 11, 16, 9, 0)

    @Test fun schoolOnWeekdayMornings() {
        assertEquals("Escola", QuietHours.active(listOf(school), monday9)?.name)
        assertNull(QuietHours.active(listOf(school), LocalDateTime.of(2026, 11, 21, 9, 0))) // Saturday
        assertNull(QuietHours.active(listOf(school), monday9.withHour(12))) // ends at 12h
    }

    @Test fun bedtimeRunsOvernight() {
        assertEquals("Dormir", QuietHours.active(listOf(bed), monday9.withHour(23))?.name)
        assertEquals("Dormir", QuietHours.active(listOf(bed), monday9.withHour(6))?.name)
        assertNull(QuietHours.active(listOf(bed), monday9.withHour(7)))
    }

    @Test fun pausedPeriodDoesNothing() {
        assertNull(QuietHours.active(listOf(school.copy(on = false)), monday9))
    }

    @Test fun exceptionCanAlwaysSend() {
        val vovo = Contact("v", "Vovó", quietException = true)
        val mae = Contact("m", "Mãe")
        assertTrue(QuietHours.canSend(listOf(school), vovo, monday9))
        assertFalse(QuietHours.canSend(listOf(school), mae, monday9))
    }

    @Test fun presetsMatchTheOwnersChoiceAndStartOff() {
        assertEquals(listOf("Escola", "Dormir"), QuietHours.presets.map { it.name })
        assertTrue(QuietHours.presets.none { it.on })
    }

    @Test fun easyPinsRefused() {
        assertEquals(Pin.Problem.EASY, Pin.problem("000000"))
        assertEquals(Pin.Problem.EASY, Pin.problem("123456"))
        assertEquals(Pin.Problem.EASY, Pin.problem("987654"))
        assertEquals(Pin.Problem.LENGTH, Pin.problem("1234"))
        assertEquals(Pin.Problem.NOT_DIGITS, Pin.problem("12a456"))
        assertNull(Pin.problem("402817"))
    }

    @Test fun fiveWrongTriesLockForAMinute() {
        val g = Pin.Guard()
        repeat(5) { assertFalse(g.attempt(false, 0)) }
        assertEquals(60_000L, g.lockedFor(0))
        assertFalse(g.attempt(true, 30_000)) // still locked, even with the right PIN
        assertTrue(g.attempt(true, 60_000))
    }
}
