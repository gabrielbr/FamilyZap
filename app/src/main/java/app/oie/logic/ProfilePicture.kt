package app.oie.logic

import org.json.JSONObject

/** Contacts' WhatsApp profile pictures (docs/flows/contacts-quiet-hours.md, v0.26). */
object ProfilePicture {
    /** Each contact's picture is checked at most this often. */
    const val EVERY_MS: Long = 24L * 60 * 60 * 1000

    enum class State { SAME, NONE, NEW }

    data class Answer(val state: State, val id: String, val jpeg: ByteArray?)

    /** Due for a check: never checked, or not in the last day. */
    fun due(c: Contact, now: Long): Boolean = c.photoAt == 0L || now - c.photoAt >= EVERY_MS

    /** Reads the link's answer; anything unreadable is null (try again next time). */
    fun parse(json: String?): Answer? = runCatching {
        val o = JSONObject(json ?: return null)
        val state = when (o.getString("state")) { "same" -> State.SAME; "none" -> State.NONE; "new" -> State.NEW; else -> return null }
        val jpeg = o.optString("jpeg").takeIf { it.isNotEmpty() }?.let { java.util.Base64.getDecoder().decode(it) }
        if (state == State.NEW && jpeg == null) return null
        Answer(state, o.optString("id"), jpeg)
    }.getOrNull()

    /**
     * The contact after a check at [now]. [stored] is the new picture's file name when the answer
     * was NEW. NONE clears the picture; SAME keeps it. Nothing else about the contact changes.
     */
    fun apply(c: Contact, a: Answer, stored: String?, now: Long): Contact = when (a.state) {
        State.SAME -> c.copy(photoAt = now)
        State.NONE -> c.copy(photo = "", photoId = "", photoAt = now)
        State.NEW -> c.copy(photo = stored ?: c.photo, photoId = if (stored != null) a.id else c.photoId, photoAt = now)
    }
}
