package app.oie.logic

/**
 * Who wrote a message in a group (v0.46, docs/flows/groups.md; the owner: "the messages don't bring who sent them …
 * treat the group as a group"). Worked out when shown, not stored, so someone the parent adds later loses the
 * "Fora dos contatos" mark on old messages too.
 */
object GroupSender {
    /** [name] as the kid sees it; [onList] false marks someone who isn't one of the kid's contacts. */
    data class Who(val name: String, val onList: Boolean)

    fun of(m: Message, allowed: List<Contact>, parentName: String): Who {
        if (m.fromParent) return Who(parentName, true)
        val ids = listOf(m.sender, m.senderAlt).filter { it.isNotBlank() }
        val numbers = ids.filter { it.endsWith("@s.whatsapp.net") }.map { digits(it) }.filter { it.isNotEmpty() }
        val known = allowed.firstOrNull { c ->
            !c.isGroup && (c.id in ids || (c.phone.isNotEmpty() && digits(c.phone) in numbers) || numbers.any { c.id.startsWith("$it@") })
        }
        if (known != null) return Who(known.name, true)
        val shown = m.senderName.trim().ifEmpty { numbers.firstOrNull()?.let { ContactNames.display(it) } ?: "" }
        return Who(shown, false)
    }

    private fun digits(s: String) = s.substringBefore('@').substringBefore(':').filter { it.isDigit() }
}
