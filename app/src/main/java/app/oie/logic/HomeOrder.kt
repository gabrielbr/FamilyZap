package app.oie.logic

/**
 * The order of people on the kid's home (v0.42, the owner: "allow the kid to reorganize the listing … and mark
 * favorites so they stay on top"; the owner chose the kid's order over the parent's on the home).
 * - v0.48 (the owner: "separate groups from users"): people first, then groups, as two sections. The line to the
 *   parent counts as a person (it's the parent, even though WhatsApp makes it a group).
 * - Within each section, favorites first, then everyone else.
 * - Within each, the kid's own order ([kidOrder], contact ids). Someone the kid never placed (just added by the
 *   parent) comes after the placed ones, in the parent's order, so a new person lands at the end of their group.
 * The parent's order in Contatos is untouched.
 */
object HomeOrder {
    /** v0.48: which section a contact sits in on the home. */
    fun inGroups(c: Contact): Boolean = c.isGroup && !c.parentLine

    fun order(people: List<Contact>, favorites: Set<String>, kidOrder: List<String>): List<Contact> {
        val rank = kidOrder.withIndex().associate { (i, id) -> id to i }
        val parentRank = people.withIndex().associate { (i, c) -> c.id to i }
        val key = compareBy<Contact>({ inGroups(it) }, { it.id !in favorites }, { rank[it.id] ?: (kidOrder.size + parentRank.getValue(it.id)) })
        return people.sortedWith(key)
    }

    /**
     * The kid's order after moving [id] by [delta] places (−1 up, +1 down) within what the home shows now.
     * A move stays inside its section (people or groups), and a favorite never moves below the section's last
     * favorite, nor someone else above its first non-favorite: the hearts and the sections decide the groups,
     * the arrows and dragging decide the order inside them.
     */
    fun move(people: List<Contact>, favorites: Set<String>, kidOrder: List<String>, id: String, delta: Int): List<String> {
        val shownContacts = order(people, favorites, kidOrder)
        val shown = shownContacts.map { it.id }.toMutableList()
        val from = shown.indexOf(id)
        if (from < 0) return shown
        val me = shownContacts[from]
        val fav = id in favorites
        // The block this person may move within: same section, same side of the hearts.
        val block = shownContacts.indices.filter { inGroups(shownContacts[it]) == inGroups(me) && (shownContacts[it].id in favorites) == fav }
        val to = (from + delta).coerceIn(block.first(), block.last())
        if (to == from) return shown
        shown.removeAt(from); shown.add(to, id)
        return shown
    }

    /** The kid's order when [id] becomes a favorite or stops being one: it goes to the end of its new group, in its section. */
    fun toggleFavorite(people: List<Contact>, favorites: Set<String>, kidOrder: List<String>, id: String): Pair<Set<String>, List<String>> {
        val nowFav = id !in favorites
        val favs = if (nowFav) favorites + id else favorites - id
        val shownContacts = order(people, favorites, kidOrder).filter { it.id != id }
        val me = people.firstOrNull { it.id == id } ?: return favs to shownContacts.map { it.id }
        val shown = shownContacts.map { it.id }.toMutableList()
        // After the last one in the same section that's on the same side of the hearts as the person will be.
        val lastSame = shownContacts.indexOfLast { inGroups(it) == inGroups(me) && (it.id in favs) == nowFav }
        val at = if (lastSame >= 0) lastSame + 1 else {
            // None yet on that side: favorites start the section, others end it.
            val sectionStart = shownContacts.indexOfFirst { inGroups(it) == inGroups(me) }.let { if (it < 0) shown.size else it }
            val sectionEnd = shownContacts.indexOfLast { inGroups(it) == inGroups(me) }.let { if (it < 0) shown.size else it + 1 }
            if (nowFav) sectionStart else sectionEnd
        }
        shown.add(at, id)
        return favs to shown
    }

    /**
     * v0.45 (the owner: new messages should "go up, so you can see that you need to give attention to that", as in
     * WhatsApp): whoever has unread messages comes first in their section, the newest first; everyone else keeps
     * [ordered]. Once read, a person goes back to their place. [latestNew]: contact id to the time of their newest
     * unread message.
     */
    fun withNews(ordered: List<Contact>, latestNew: Map<String, Long>): List<Contact> {
        fun section(list: List<Contact>) = list.filter { it.id in latestNew }.sortedByDescending { latestNew.getValue(it.id) } + list.filter { it.id !in latestNew }
        return section(ordered.filter { !inGroups(it) }) + section(ordered.filter { inGroups(it) })
    }
}
