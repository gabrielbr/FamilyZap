package app.oie.logic

import java.time.DayOfWeek
import java.time.LocalTime

/** A person or group the parent allowed (docs/flows/contacts-quiet-hours.md). */
data class Contact(
    val id: String,
    val name: String,
    val isGroup: Boolean = false,
    /** Can talk during quiet hours (the owner's per-contact exception, 2026-11-18). */
    val quietException: Boolean = false,
    /** For a group made for the kid: show everything from it, not only what the message rule passes. */
    val showWholeGroup: Boolean = false,
)

enum class Kind { TEXT, VOICE, PHOTO, STICKER, UNSUPPORTED }

/** One message in a chat with an allowed contact, as the WhatsApp link reports it. */
data class Message(
    val id: String,
    val chatId: String,
    val fromKid: Boolean,
    /** Epoch millis. */
    val at: Long,
    val kind: Kind,
    val text: String = "",
    /** The id of the message this one replies to (quotes), if any. */
    val replyTo: String? = null,
    val durationSec: Int = 0,
)

/** One quiet-hours period (docs/flows/contacts-quiet-hours.md). End before start means overnight. */
data class QuietPeriod(
    val name: String,
    val days: Set<DayOfWeek>,
    val start: LocalTime,
    val end: LocalTime,
    val on: Boolean = true,
)
