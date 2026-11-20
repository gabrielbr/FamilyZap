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
    /** WhatsApp said the contact list is complete (v0.17). */
    var contactsSynced by mutableStateOf(false)
    /** When the link connected in this run of the app (0 = not yet), to tell "still coming" from "slow". */
    var linkedAt by mutableStateOf(0L)
    /** When the link last stopped being up: an attempt started, or it dropped (v0.24, for LinkView's grace). */
    var downSince by mutableStateOf(0L)
    /**
     * A link that's down needs the parent only when it can't come back by itself (v0.34 audit): not
     * linked (or the parent stopped it), logged out, banned, unavailable, or waiting for a QR. A dropped
     * internet is retried. Exhaustive, so a new status has to be placed here.
     */
    fun needsParent(real: Boolean): Boolean = !real || !on || when (status) {
        Status.LoggedOut, is Status.Banned, is Status.Qr, Status.Unavailable -> true
        Status.Off, Status.Connecting, Status.Connected, Status.Disconnected -> false
    }

    /** The last pairing attempt failed (v0.34): the link screen says so above the button. */
    var pairFailed by mutableStateOf(false)

    fun clear() { on = false; status = Status.Off; available.clear(); contactsSynced = false; linkedAt = 0L; downSince = 0L; pairFailed = false }
}
