package app.oie.logic

/**
 * The order of people on the kid's home (v0.42, the owner: "allow the kid to reorganize the listing … and mark
 * favorites so they stay on top"; the owner chose the kid's order over the parent's on the home).
 * - Favorites first, then everyone else.
 * - Within each, the kid's own order ([kidOrder], contact ids). Someone the kid never placed (just added by the
 *   parent) comes after the placed ones, in the parent's order, so a new person lands at the end of their group.
 * The parent's order in Contatos is untouched.
 */
object HomeOrder {
    fun order(people: List<Contact>, favorites: Set<String>, kidOrder: List<String>): List<Contact> {
        val rank = kidOrder.withIndex().associate { (i, id) -> id to i }
        val parentRank = people.withIndex().associate { (i, c) -> c.id to i }
        val key = compareBy<Contact>({ it.id !in favorites }, { rank[it.id] ?: (kidOrder.size + parentRank.getValue(it.id)) })
        return people.sortedWith(key)
    }

    /**
     * The kid's order after moving [id] by [delta] places (−1 up, +1 down) within what the home shows now.
     * A favorite never moves below the last favorite, nor someone else above the first non-favorite: the hearts
     * decide the groups, the arrows and dragging decide the order inside them.
     */
    fun move(people: List<Contact>, favorites: Set<String>, kidOrder: List<String>, id: String, delta: Int): List<String> {
        val shown = order(people, favorites, kidOrder).map { it.id }.toMutableList()
        val from = shown.indexOf(id)
        if (from < 0) return shown
        val fav = id in favorites
        val first = if (fav) 0 else shown.count { it in favorites }
        val last = if (fav) shown.count { it in favorites } - 1 else shown.size - 1
        val to = (from + delta).coerceIn(first, last)
        if (to == from) return shown
        shown.removeAt(from); shown.add(to, id)
        return shown
    }

    /** The kid's order when [id] becomes a favorite or stops being one: it goes to the end of its new group. */
    fun toggleFavorite(people: List<Contact>, favorites: Set<String>, kidOrder: List<String>, id: String): Pair<Set<String>, List<String>> {
        val nowFav = id !in favorites
        val favs = if (nowFav) favorites + id else favorites - id
        val shown = order(people, favorites, kidOrder).map { it.id }.filter { it != id }.toMutableList()
        val at = if (nowFav) shown.count { it in favs } else shown.size
        shown.add(at, id)
        return favs to shown
    }

    /**
     * v0.45 (the owner: new messages should "go up, so you can see that you need to give attention to that", as in
     * WhatsApp): whoever has unread messages comes first, the newest first; everyone else keeps [ordered]. Once read,
     * a person goes back to their place. [latestNew]: contact id to the time of their newest unread message.
     */
    fun withNews(ordered: List<Contact>, latestNew: Map<String, Long>): List<Contact> =
        ordered.filter { it.id in latestNew }.sortedByDescending { latestNew.getValue(it.id) } + ordered.filter { it.id !in latestNew }
}
