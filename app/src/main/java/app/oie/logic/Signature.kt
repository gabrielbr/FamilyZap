package app.oie.logic

/**
 * The kid writes through the parent's WhatsApp, so every message carries the kid's name in front.
 * The family knows who is writing, and their reply naturally starts with the name, which the
 * message rule recognises (docs/flows/kid-receives-messages.md, "Important").
 *
 * v0.22 (the owner): the name in WhatsApp's bold, after an emoji the parent may choose:
 * "😎*Tito*: Oi, vó", which WhatsApp shows as 😎**Tito**: Oi, vó.
 */
object Signature {
    /** "*Tito*: Oi, vó", or "😎*Tito*: Oi, vó". With no text (a photo or a sticker) the name alone, as the caption. No name, no change. */
    fun sign(kidName: String, text: String, emoji: String = ""): String {
        // A star in the name would break WhatsApp's bold.
        val name = kidName.replace("*", "").trim()
        if (name.isEmpty()) return text
        val tag = emojiOf(emoji) + "*" + name + "*"
        return if (text.isBlank()) tag else "$tag: $text"
    }

    /**
     * The first emoji of what the parent typed, or "" (v0.22): one grapheme (so 👍🏽 and 👨‍👩‍👧 stay
     * whole), and not a letter, a digit, a star or punctuation.
     */
    fun emojiOf(typed: String): String {
        val t = typed.trim()
        if (t.isEmpty()) return ""
        val first = firstCluster(t)
        val cp = first.codePointAt(0)
        val emoji = Character.getType(cp).toByte() == Character.OTHER_SYMBOL || cp >= 0x1F000 || first.contains('\uFE0F')
        return if (emoji && first.none { it.isLetterOrDigit() || it == '*' }) first else ""
    }

    /**
     * One emoji with what joins it (skin tone, variation selector, keycap, a ZWJ family, a flag's
     * pair, tags). Written out rather than BreakIterator, so the tablet and the tests agree.
     */
    private fun firstCluster(t: String): String {
        var i = Character.charCount(t.codePointAt(0))
        val firstCp = t.codePointAt(0)
        val flag = firstCp in 0x1F1E6..0x1F1FF
        var pairs = if (flag) 1 else 0
        while (i < t.length) {
            val cp = t.codePointAt(i)
            val joins = cp == 0xFE0F || cp == 0xFE0E || cp == 0x20E3 || cp in 0x1F3FB..0x1F3FF || cp in 0xE0020..0xE007F ||
                (flag && pairs == 1 && cp in 0x1F1E6..0x1F1FF)
            when {
                cp == 0x200D && i + 1 < t.length -> { i += 1; i += Character.charCount(t.codePointAt(i)) }
                joins -> { if (flag && cp in 0x1F1E6..0x1F1FF) pairs++; i += Character.charCount(cp) }
                else -> break
            }
        }
        return t.substring(0, i)
    }
}
