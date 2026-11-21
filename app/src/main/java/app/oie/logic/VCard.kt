package app.oie.logic

/**
 * The contact cards WhatsApp shares (vCard text), read for "/Tito adicionar" (v0.43). WhatsApp puts the person's
 * WhatsApp number in the phone line as `waid=5511987650001`; a number without it isn't on WhatsApp.
 */
object VCard {
    fun parse(text: String): List<ParentCommand.Card> =
        text.split(Regex("(?i)BEGIN:VCARD")).drop(1).mapNotNull { block ->
            val lines = unfold(block.substringBefore("END:VCARD"))
            val name = lines.firstOrNull { it.startsWith("FN", ignoreCase = true) && it.contains(':') }?.substringAfter(':')?.trim()
                ?: lines.firstOrNull { it.startsWith("N:", ignoreCase = true) || it.startsWith("N;", ignoreCase = true) }
                    ?.substringAfter(':')?.split(';')?.filter { it.isNotBlank() }?.reversed()?.joinToString(" ")?.trim()
                ?: ""
            val digits = lines.filter { it.startsWith("TEL", ignoreCase = true) || it.contains(".TEL", ignoreCase = true) }
                .firstNotNullOfOrNull { Regex("(?i)waid=([0-9]+)").find(it)?.groupValues?.get(1) } ?: ""
            if (name.isEmpty() && digits.isEmpty()) null else ParentCommand.Card(unescape(name), digits)
        }

    /** vCard folds long lines: a line starting with a space continues the one before. */
    private fun unfold(s: String): List<String> {
        val out = mutableListOf<String>()
        s.replace("\r\n", "\n").split('\n').forEach { l -> if ((l.startsWith(" ") || l.startsWith("\t")) && out.isNotEmpty()) out[out.size - 1] += l.drop(1) else if (l.isNotBlank()) out += l }
        return out
    }

    private fun unescape(s: String) = s.replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
}
