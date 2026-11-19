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
    /** The number's digits with the country code, from the WhatsApp link (v0.19). Empty if unknown or a group. */
    val phone: String = "",
    /**
     * The name in the parent's WhatsApp when the parent gave the kid a different one (v0.19): [name] is
     * "Vovó", this is "Maria Silva". Empty when they're the same. Only the parent area shows it.
     */
    val waName: String = "",
    /** Paused from the parent's WhatsApp ("/Tito parar", v0.21): nothing reaches the kid, and it leaves the kid's home. */
    val paused: Boolean = false,
)

/** DELETED is WhatsApp's "delete for everyone": [Message.replyTo] names the message it removes. */
enum class Kind { TEXT, VOICE, PHOTO, STICKER, UNSUPPORTED, DELETED }

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
    /** A photo kept in the app's private storage on the tablet (data/Photos.kt): the file name only. */
    val media: String? = null,
)

/** One quiet-hours period (docs/flows/contacts-quiet-hours.md). End before start means overnight. */
data class QuietPeriod(
    val name: String,
    val days: Set<DayOfWeek>,
    val start: LocalTime,
    val end: LocalTime,
    val on: Boolean = true,
)

/**
 * A sticker the parent sent to their own WhatsApp chat (v0.10). It's offered to the kid as new;
 * once the kid saves it, it joins the kid's stickers and goes out as a real WhatsApp sticker.
 */
data class FamilySticker(
    val id: String,
    /** The sealed WebP on the tablet (data/Photos.kt): the file name. */
    val media: String,
    val animated: Boolean,
    val saved: Boolean,
    val at: Long,
)
