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
    /** v0.37, the kid's own: who it went to (so "Apagar para todos" reaches the same people), and how the post went. */
    val audience: List<String> = emptyList(),
    val posting: Boolean = false,
    val failed: Boolean = false,
) {
    val mine: Boolean get() = contactId == Statuses.MINE
}

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

    /** The kid's own statuses (v0.37) are kept under this id. */
    const val MINE = "me"

    /** WhatsApp's limits for a status: a video up to a minute, a text up to 700 characters. */
    const val VIDEO_MAX_SEC = 60
    const val TEXT_MAX = 700

    /**
     * Who sees the kid's status (v0.37): the people the kid can reach. Never a group, a paused person or the
     * line to the parent (the parent sees it in Meu status anyway). Empty means nothing can be posted.
     */
    fun audience(allowed: List<Contact>): List<Contact> = allowed.filter { !it.isGroup && !it.paused && !it.parentLine }

    /** Where the viewer starts: the first unseen status, else the first. */
    fun startAt(mine: List<Status>): Int = mine.indexOfFirst { !it.viewed }.takeIf { it >= 0 } ?: 0
}
