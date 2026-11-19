package app.oie.logic

import org.json.JSONArray

/**
 * Reads the parent's contact list from the WhatsApp link. Anything that isn't a list ("null",
 * empty, broken) is an empty list, never a crash: in v0.15 a "null" right after linking closed the
 * app mid-link, and WhatsApp dropped the tablet (v0.16).
 */
object ContactsJson {
    fun parse(json: String?): List<Contact> {
        val a = runCatching { JSONArray(json ?: return emptyList()) }.getOrNull() ?: return emptyList()
        return (0 until a.length()).mapNotNull { i ->
            val o = a.optJSONObject(i) ?: return@mapNotNull null
            val id = o.optString("id"); val name = o.optString("name")
            if (id.isBlank() || name.isBlank()) null else Contact(id, name, o.optBoolean("isGroup"), phone = o.optString("phone").filter { it.isDigit() })
        }
    }
}
