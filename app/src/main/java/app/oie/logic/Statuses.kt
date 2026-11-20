package app.oie.logic

/**
 * A family member's status (docs/flows/statuses.md, v0.36). [poster] is their WhatsApp id as the status
 * came (reactions and replies point at it); [contactId] is the allowed contact it belongs to.
 */
data class Status(
    val id: String,
    val contactId: String,
    val poster: String,
    val at: Long,
    val kind: Kind,
    val text: String = "",
    val media: String? = null,
    val size: Long = 0,
    val durationSec: Int = 0,
    val viewed: Boolean = false,
    /** The kid's reaction to it, if any. */
    val reaction: String = "",
)

/** The rules for statuses: who, how long, and in what order. Pure, so they're tested. */
object Statuses {
    /** A status lives a day, as in WhatsApp. */
    const val LIFE_MS = 24L * 60 * 60 * 1000

    /** How long a photo or a text stays before the next one. */
    const val SHOW_MS = 5_000L

    /**
     * Whether a status is kept: statuses on, from a person the kid can reach (not a group, not paused,
     * not the line to the parent), posted after Oiê was linked (rule L6), and still alive.
     */
    fun keep(on: Boolean, from: Contact?, at: Long, linkedSince: Long, now: Long): Boolean =
        on && from != null && !from.isGroup && !from.paused && !from.parentLine &&
            at >= linkedSince && now - at < LIFE_MS

    fun alive(s: Status, now: Long): Boolean = now - s.at < LIFE_MS

    /** One person's statuses, oldest first. */
    fun of(all: List<Status>, contactId: String, now: Long): List<Status> =
        all.filter { it.contactId == contactId && alive(it, now) }.sortedBy { it.at }

    /** The home row: people with live statuses, the ones with something unseen first (each keeps the parent's order). */
    fun row(all: List<Status>, contacts: List<Contact>, now: Long): List<Pair<Contact, Boolean>> =
        contacts.mapNotNull { c ->
            val mine = of(all, c.id, now)
            if (mine.isEmpty()) null else c to mine.any { !it.viewed }
        }.sortedBy { if (it.second) 0 else 1 }

    /** Where the viewer starts: the first unseen status, else the first. */
    fun startAt(mine: List<Status>): Int = mine.indexOfFirst { !it.viewed }.takeIf { it >= 0 } ?: 0
}
