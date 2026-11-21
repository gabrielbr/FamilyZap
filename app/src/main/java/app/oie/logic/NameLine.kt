package app.oie.logic

/**
 * The kid's name on every message (v0.39, the owner: "every type of message should have the kid prefix").
 * Text carries it in front, and photos, videos, GIFs and Oiê's own stickers carry it as their caption
 * ([Signature]). WhatsApp gives a voice note or a real sticker no caption, so before one of those Oiê sends
 * the name alone as a short line ("😎*Tito*"), once per burst: not if the kid's name already went to that
 * chat in the last [BURST_MS] (the owner's choice, 2026-11-21, so five audios in a row send one name line,
 * not five; fewer messages also keep the pace human, rule L3). Reactions stay bare (the owner's choice).
 */
object NameLine {
    const val BURST_MS = 2 * 60_000L

    /** Does this message carry the kid's name itself (in front, or as its caption)? */
    fun carriesName(kind: Kind, familySticker: Boolean): Boolean = when (kind) {
        Kind.TEXT, Kind.PHOTO, Kind.VIDEO, Kind.GIF -> true
        Kind.STICKER -> !familySticker // Oiê's own stickers go as pictures with the name as caption
        else -> false // a voice note or a family sticker (a real WhatsApp sticker): no caption
    }

    /**
     * Should the name line go out before this message? Only when there's a name, the message can't carry
     * it, and the name didn't reach this chat in the last [BURST_MS]. [lastNamedAt] is when it last did
     * (null: not since the app started).
     */
    fun needed(kind: Kind, familySticker: Boolean, kidName: String, lastNamedAt: Long?, now: Long): Boolean {
        if (kidName.replace("*", "").isBlank()) return false
        if (kind != Kind.VOICE && kind != Kind.STICKER) return false
        if (carriesName(kind, familySticker)) return false
        return lastNamedAt == null || now - lastNamedAt >= BURST_MS || now < lastNamedAt
    }
}
