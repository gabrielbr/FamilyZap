package app.oie.logic

import java.time.LocalDate
import java.time.MonthDay

/** The rules behind the v0.33 family features (docs/flows/family-features.md). Pure, so they're tested. */

/** "Enviada", "Entregue", "Vovó viu": a receipt only moves forward. */
object Receipts {
    fun raise(now: Receipt, heard: Receipt): Receipt = if (heard.ordinal > now.ordinal) heard else now

    /** The link's word for a receipt. */
    fun of(state: String): Receipt? = when (state) {
        "delivered" -> Receipt.DELIVERED
        "read" -> Receipt.READ
        else -> null
    }
}

/** Reactions, as in WhatsApp: one per person, the same one again takes it away. */
object Reactions {
    const val KID = "kid"

    /** The six the kid can pick. They're the family's words, sent as WhatsApp reactions; Oiê's own interface uses no emoji. */
    val CHOICES = listOf("❤️", "😂", "😮", "😢", "👍", "🙏")

    fun set(on: Map<String, String>, who: String, emoji: String): Map<String, String> =
        if (emoji.isBlank()) on - who else on + (who to emoji)

    /** The kid taps [emoji]: what to send (empty = take it away), and the reactions after. */
    fun kidTaps(on: Map<String, String>, emoji: String): Pair<String, Map<String, String>> {
        val send = if (on[KID] == emoji) "" else emoji
        return send to set(on, KID, send)
    }

    /** What shows under a message: each emoji once, with how many when more than one, in the order they came. */
    fun shown(on: Map<String, String>): List<Pair<String, Int>> =
        on.values.groupingBy { it }.eachCount().toList()
}

/** Birthdays: day and month only. */
object Birthdays {
    /** "25/12", "25/12/1950" or "25-12" → "12-25"; null when it isn't a real day. "" clears it. */
    fun parse(input: String): String? {
        val t = input.trim()
        if (t.isEmpty()) return ""
        val parts = t.split('/', '-', '.', ' ').filter { it.isNotBlank() }
        if (parts.size < 2) return null
        val d = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return runCatching { MonthDay.of(m, d) }.getOrNull()?.let { "%02d-%02d".format(m, d) }
    }

    /** "12-25" → "25/12", as the parent typed it. */
    fun display(mmdd: String): String = if (mmdd.length == 5) mmdd.substring(3) + "/" + mmdd.substring(0, 2) else ""

    /** Is it [mmdd] on [date]? 29 February falls on the 28th when the year has no 29th. */
    fun isToday(mmdd: String, date: LocalDate): Boolean {
        if (mmdd.length != 5) return false
        val md = runCatching { MonthDay.of(mmdd.substring(0, 2).toInt(), mmdd.substring(3).toInt()) }.getOrNull() ?: return false
        val day = if (md.monthValue == 2 && md.dayOfMonth == 29 && !date.isLeapYear) MonthDay.of(2, 28) else md
        return MonthDay.from(date) == day
    }

    /** The people (not groups) whose birthday is today, as the kid's home shows them. */
    fun today(contacts: List<Contact>, date: LocalDate): List<Contact> =
        contacts.filter { !it.isGroup && !it.paused && isToday(it.birthday, date) }
}

/**
 * The gentle nudge (off unless the parent turns it on): one person the kid hasn't written to for a
 * week. Never more than one, never a count of days, never someone the kid has never written to.
 */
object Nudge {
    const val AFTER_MS = 7L * 24 * 60 * 60 * 1000

    fun pick(on: Boolean, contacts: List<Contact>, messages: List<Message>, dismissed: Map<String, Long>, now: Long): Contact? {
        if (!on) return null
        return contacts.filter { !it.isGroup && !it.paused && !it.parentLine }
            .mapNotNull { c ->
                val last = messages.filter { it.chatId == c.id && it.fromKid }.maxOfOrNull { it.at } ?: return@mapNotNull null
                val quiet = now - last >= AFTER_MS
                val hidden = dismissed[c.id]?.let { now - it < AFTER_MS } ?: false
                if (quiet && !hidden) c to last else null
            }
            .minByOrNull { it.second }?.first
    }
}

/** "Preciso de você": to the people the parent marked, at most once every 5 minutes. */
object NeedYou {
    const val GAP_MS = 5L * 60 * 1000

    fun to(contacts: List<Contact>): List<Contact> = contacts.filter { it.needYou && !it.paused }

    /** Milliseconds until the kid can send it again (0 = now). */
    fun waitMs(lastAt: Long, now: Long): Long = if (lastAt == 0L) 0 else (lastAt + GAP_MS - now).coerceAtLeast(0)
}

/**
 * The letters on a person's round when there's no photo (v0.34): the first letter; when two people
 * share it ("Vovó", "Vovô"), the first letter plus the first letter where the names differ ("Vó", "Vô").
 */
object Initials {
    fun of(names: List<String>): Map<String, String> {
        val byFirst = names.distinct().groupBy { it.take(1).uppercase() }
        return names.distinct().associateWith { name ->
            val first = name.take(1).uppercase()
            val others = byFirst[first].orEmpty().filter { it != name }
            if (others.isEmpty()) first
            else {
                val i = (1 until name.length).firstOrNull { k -> others.all { o -> k >= o.length || !o[k].equals(name[k], ignoreCase = true) } }
                    ?: (1 until name.length).firstOrNull { k -> others.any { o -> k >= o.length || !o[k].equals(name[k], ignoreCase = true) } }
                if (i == null) first else first + name[i].lowercaseChar()
            }
        }
    }
}
