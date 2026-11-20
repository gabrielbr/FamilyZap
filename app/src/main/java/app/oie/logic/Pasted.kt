package app.oie.logic

/**
 * What a GIF, sticker or picture from the keyboard, or a pasted file, becomes (v0.31,
 * docs/flows/kid-sends-message.md). Nothing is sent without Enviar.
 */
object Pasted {
    enum class Goes { PHOTO, GIF, VIDEO, NOTHING }

    fun route(mimeType: String?): Goes {
        val t = mimeType.orEmpty().lowercase()
        return when {
            t == "image/gif" -> Goes.GIF
            t.startsWith("image/") -> Goes.PHOTO
            t.startsWith("video/") -> Goes.VIDEO
            else -> Goes.NOTHING
        }
    }

    /** The file name extension used for the copy kept while it's prepared. */
    fun extension(goes: Goes): String = when (goes) { Goes.GIF -> "gif"; Goes.VIDEO -> "mp4"; else -> "img" }
}
