package app.oie.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.oie.logic.Contact

/** The WhatsApp link as the screens see it: wanted or not, its status, and the parent's contacts. */
class LinkState {
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

    /** The parent linked WhatsApp: the link connects whenever it can, in the background too. Saved. */
    var on by mutableStateOf(false)
    var status: Status by mutableStateOf(Status.Off)
    /** The parent's WhatsApp contacts and groups, to choose from. Not saved: reloaded from the link. */
    val available = mutableStateListOf<Contact>()

    fun clear() { on = false; status = Status.Off; available.clear() }
}
