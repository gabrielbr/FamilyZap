package app.oie.logic

import java.time.LocalDateTime
import java.time.LocalTime

/** Quiet hours (docs/flows/contacts-quiet-hours.md): during a period the kid reads and listens, but can't send. */
object QuietHours {
    /** The owner's presets (2026-11-18): school on weekdays, bed every day. Off until the parent turns them on. */
    val presets = listOf(
        QuietPeriod("Escola", java.time.DayOfWeek.entries.take(5).toSet(), LocalTime.of(7, 0), LocalTime.of(12, 0), on = false),
        QuietPeriod("Dormir", java.time.DayOfWeek.entries.toSet(), LocalTime.of(21, 0), LocalTime.of(7, 0), on = false),
    )

    /** v0.43: "/Tito silêncio" from the parent's WhatsApp: quiet now, all day, until "/Tito liberar". */
    const val MANUAL = "manual"
    val manual = QuietPeriod(MANUAL, java.time.DayOfWeek.entries.toSet(), LocalTime.MIDNIGHT, LocalTime.MAX, on = true)

    /** The period in force at [now], if any. An overnight period belongs to the day it starts. */
    fun active(periods: List<QuietPeriod>, now: LocalDateTime): QuietPeriod? = periods.firstOrNull { p ->
        if (!p.on) return@firstOrNull false
        val t = now.toLocalTime()
        if (p.start <= p.end) {
            now.dayOfWeek in p.days && t >= p.start && t < p.end
        } else {
            (now.dayOfWeek in p.days && t >= p.start) || (now.dayOfWeek.minus(1) in p.days && t < p.end)
        }
    }

    /** When the kid may send to [contact] at [now]. */
    fun canSend(periods: List<QuietPeriod>, contact: Contact, now: LocalDateTime): Boolean =
        contact.quietException || active(periods, now) == null
}
