package app.oie.logic

/**
 * The kid writes through the parent's WhatsApp, so every message carries the kid's name in front
 * ("Tito: Oi, vó"). The family knows who is writing, and their reply naturally starts with the
 * name, which the message rule recognises (docs/flows/kid-receives-messages.md, "Important").
 */
object Signature {
    /** "Tito: Oi, vó". With no text (a photo or a sticker) the name alone, as the caption. No name, no change. */
    fun sign(kidName: String, text: String): String {
        val name = kidName.trim()
        return when {
            name.isEmpty() -> text
            text.isBlank() -> name
            else -> "$name: $text"
        }
    }
}
