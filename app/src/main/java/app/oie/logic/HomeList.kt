package app.oie.logic

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * v0.38: what each person's row (phone) or card (tablet) on the kid's home says about the chat:
 * the last message the kid can see, in a few words, and when it came (flow kid-home-and-attach.md).
 * Pure, so it is tested without the UI; the words come from Strings.
 */
object HomeList {

    /** What the last message was, for its few words. */
    enum class What { TEXT, VOICE, PHOTO, VIDEO, GIF, STICKER, DELETED, OTHER }

    data class Preview(
        val what: What,
        /** The text, or a photo's caption; empty for the rest. One line, trimmed. */
        val text: String,
        /** The kid sent it ("Você: …"). */
        val mine: Boolean,
        val at: Long,
        val seconds: Int = 0,
        /** The kid's message didn't go (rule L4): the row says so, the same as the chat. */
        val failed: Boolean = false,
    )

    /** When the last message came, said the way a chat list says it. */
    sealed interface When {
        data class Time(val hour: Int, val minute: Int) : When
        data object Yesterday : When
        /** 1 = Monday … 7 = Sunday (java.time). */
        data class Weekday(val day: Int) : When
        data class Date(val day: Int, val month: Int) : When
    }

    /**
     * The last message the kid sees in a chat (already through MessageRule), or null for a chat with none.
     * Reactions aren't messages in the list (they sit on a message), so they never become the preview.
     */
    fun preview(visible: List<Message>, failed: Set<String> = emptySet()): Preview? {
        val last = visible.lastOrNull { it.kind != Kind.REACTION } ?: return null
        val what = when (last.kind) {
            Kind.TEXT -> What.TEXT
            Kind.VOICE -> What.VOICE
            Kind.PHOTO -> What.PHOTO
            Kind.VIDEO -> What.VIDEO
            Kind.GIF -> What.GIF
            Kind.STICKER -> What.STICKER
            Kind.DELETED -> What.DELETED
            Kind.UNSUPPORTED, Kind.REACTION -> What.OTHER
        }
        val text = if (what == What.TEXT || what == What.PHOTO || what == What.VIDEO) oneLine(last.text) else ""
        return Preview(what, text, last.fromKid, last.at, last.durationSec, failed = last.fromKid && last.id in failed)
    }

    /** New lines and runs of spaces become one space, so a long message is one line that ellipsizes. */
    fun oneLine(s: String): String = s.trim().replace(Regex("\\s+"), " ")

    /** Today: the time. Yesterday: "Ontem". The last week: the weekday. Older: the date. */
    fun whenOf(at: Long, now: LocalDateTime, zone: ZoneId = ZoneId.systemDefault()): When {
        val t = LocalDateTime.ofInstant(Instant.ofEpochMilli(at), zone)
        val day: LocalDate = t.toLocalDate()
        val today = now.toLocalDate()
        return when {
            !day.isBefore(today) -> When.Time(t.hour, t.minute)
            day == today.minusDays(1) -> When.Yesterday
            day.isAfter(today.minusDays(WEEK)) -> When.Weekday(t.dayOfWeek.value)
            else -> When.Date(t.dayOfMonth, t.monthValue)
        }
    }

    private const val WEEK = 7L
}
