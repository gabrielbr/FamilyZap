package app.oie.logic

/**
 * Moving pictures (docs/flows/kid-receives-messages.md, v0.27): what to download, and whether a
 * sticker or picture file is animated.
 */
object Moving {
    /** A video over this isn't downloaded to the tablet; the chat says it opens in WhatsApp. */
    const val MAX_DOWNLOAD_BYTES: Long = 64L * 1024 * 1024

    /** The kinds whose file Oiê keeps on the tablet. */
    fun hasFile(kind: Kind) = kind == Kind.PHOTO || kind == Kind.STICKER || kind == Kind.VOICE || kind == Kind.VIDEO || kind == Kind.GIF

    /** Download it? Videos and GIFs only up to [MAX_DOWNLOAD_BYTES]; an unknown size (0) is tried. */
    fun download(kind: Kind, size: Long): Boolean = when (kind) {
        Kind.VIDEO, Kind.GIF -> size <= MAX_DOWNLOAD_BYTES
        else -> hasFile(kind)
    }

    /** An animated WebP: RIFF…WEBP with a VP8X header whose animation flag is set. */
    fun isAnimatedWebp(b: ByteArray): Boolean =
        b.size >= 21 && ascii(b, 0, "RIFF") && ascii(b, 8, "WEBP") && ascii(b, 12, "VP8X") && (b[20].toInt() and 0x02) != 0

    /** WhatsApp's own animated sticker (v0.31): a Lottie animation in a zip ("application/was"). */
    fun isLottie(b: ByteArray): Boolean = b.size >= 4 && b[0] == 'P'.code.toByte() && b[1] == 'K'.code.toByte() && b[2] == 3.toByte() && b[3] == 4.toByte()

    /** What a sticker file is, for the problem report (no picture, just the kind). */
    fun kindOf(b: ByteArray): String = when {
        isLottie(b) -> "lottie"
        isAnimatedWebp(b) -> "animated webp"
        isGif(b) -> "gif"
        else -> "still"
    }

    /** A GIF file ("GIF87a" or "GIF89a"). */
    fun isGif(b: ByteArray): Boolean = b.size >= 6 && (ascii(b, 0, "GIF87a") || ascii(b, 0, "GIF89a"))

    private fun ascii(b: ByteArray, at: Int, s: String) = s.indices.all { b[at + it] == s[it].code.toByte() }
}
