package app.oie.logic

/**
 * The name the kid sees, the name in the parent's WhatsApp, and the number the parent checks
 * (docs/flows/contacts-quiet-hours.md, v0.19). Everything stays on the tablet; WhatsApp isn't changed.
 */
object ContactNames {
    /** The contact's name in the parent's WhatsApp. */
    fun whatsAppName(c: Contact): String = c.waName.ifBlank { c.name }

    /** Gives the kid's name. Blank goes back to the WhatsApp name; never a nameless contact. */
    fun rename(c: Contact, typed: String): Contact {
        val wa = whatsAppName(c)
        val n = typed.trim().replace(Regex("\\s+"), " ").take(MAX)
        return if (n.isEmpty() || n == wa) c.copy(name = wa, waName = "") else c.copy(name = n, waName = wa)
    }

    /** The number's digits: the link's, or the id's when the id is a phone number. Empty for groups and private ids. */
    fun phoneOf(c: Contact): String = when {
        c.isGroup -> ""
        c.phone.isNotBlank() -> c.phone
        c.id.endsWith("@s.whatsapp.net") -> c.id.substringBefore('@').substringBefore(':').filter { it.isDigit() }
        else -> ""
    }

    /** Keeps the parent's names and settings, and takes the number from the fresh WhatsApp list when it was missing. */
    fun withNumber(c: Contact, fresh: List<Contact>): Contact =
        if (phoneOf(c).isNotEmpty()) c else fresh.firstOrNull { it.id == c.id }?.phone?.takeIf { it.isNotBlank() }?.let { c.copy(phone = it) } ?: c

    /** A search finds the name, the WhatsApp name, or 4+ digits of the number. */
    fun matches(c: Contact, query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        if (c.name.contains(q, ignoreCase = true) || c.waName.contains(q, ignoreCase = true)) return true
        val digits = q.filter { it.isDigit() }
        return digits.length >= 4 && digits.length == q.count { !it.isWhitespace() && it !in "+-()." } && phoneOf(c).contains(digits)
    }

    /**
     * Grouped for reading. Brazil: "+55 11 98765-0001" (or 8 digits "+55 11 3456-7890"). Elsewhere the
     * country code isn't known for sure, so the digits stay together after the plus: "+447700900123".
     */
    fun display(digits: String): String {
        val d = digits.filter { it.isDigit() }
        if (d.isEmpty()) return ""
        if (d.startsWith("55") && (d.length == 13 || d.length == 12)) {
            val area = d.substring(2, 4); val rest = d.substring(4)
            // No-break spaces: a number is never split across lines (v0.19 audit).
            return "+55\u00A0$area\u00A0${rest.dropLast(4)}-${rest.takeLast(4)}"
        }
        return "+$d"
    }

    /** Longest name kept (a name is a line in the kid's home). */
    const val MAX = 40
}
