package app.oie.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.oie.logic.Contact
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.logic.RuleTest

/**
 * The rule test's state (docs/flows/rule-test.md). It lives in the parent area only. Saved on the tablet
 * with the rest of the state: the chats picked, when the kid "spoke", and the entries (text only
 * for messages that would reach the kid).
 */
class LinkTest {
    sealed interface Status {
        data object Off : Status
        data object Connecting : Status
        data class Qr(val code: String) : Status
        data object Connected : Status
        data object Disconnected : Status
        /** WhatsApp blocked the number for a while (rule L9). */
        data class Banned(val hours: Int) : Status
        data object LoggedOut : Status
        data object Unavailable : Status
    }

    /** The parent turned the test on; the link connects whenever the app is open. */
    var on by mutableStateOf(false)
    var status: Status by mutableStateOf(Status.Off)
    /** The chats the parent picked for the test. Messages from any other chat are counted, never kept. */
    val chats = mutableStateListOf<Contact>()
    /** The parent's WhatsApp contacts and groups, to pick from. Not saved. */
    val available = mutableStateListOf<Contact>()
    /** Chat id → when the parent pretended the kid sent a message there. */
    val kidSpoke = mutableStateMapOf<String, Long>()
    /** Newest first. */
    val entries = mutableStateListOf<RuleTest.Entry>()
    var ignored by mutableStateOf(0)

    /** One message from the link, already parsed. Matches the picked chats by either address. */
    /** Returns the new entry, or null when the chat isn't in the test. Entries that fall off the end go to [dropped]. */
    fun onIncoming(id: String, chat: String, chatAlt: String?, fromMe: Boolean, at: Long, kind: Kind, text: String, replyTo: String?, kidName: String, durationSec: Int = 0, dropped: (RuleTest.Entry) -> Unit = {}): RuleTest.Entry? {
        val picked = chats.firstOrNull { it.id == chat || it.id == chatAlt }
        if (picked == null) { ignored++; return null }
        val m = Message(id, picked.id, false, at, kind, text, replyTo, durationSec)
        val e = RuleTest.judge(m, picked, fromMe, kidSpoke[picked.id], kidName)
        entries.add(0, e)
        while (entries.size > RuleTest.MAX) dropped(entries.removeAt(entries.lastIndex))
        return e
    }

    /** Attaches a downloaded photo or sticker to its entry. */
    fun attach(id: String, media: String) {
        val i = entries.indexOfFirst { it.id == id }
        if (i >= 0) entries[i] = entries[i].copy(media = media)
    }

    /** Everything about the test goes: the chats, the entries, the count. */
    fun clear() {
        on = false; status = Status.Off
        chats.clear(); available.clear(); kidSpoke.clear(); entries.clear(); ignored = 0
    }
}
