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

    /**
     * v0.58: the shape a video or GIF takes in a chat bubble, from its own width and height. Kept between
     * [TALLEST] and [WIDEST]: a chat is a list with no height limit, so a picture's own shape decides its height,
     * and a broken or absurd size (1 by 2000) once asked for a height Android can't measure (issue #88).
     * An unknown size gets 16:9.
     */
    fun bubbleRatio(width: Int, height: Int): Float =
        if (width <= 0 || height <= 0) DEFAULT_RATIO else (width.toFloat() / height).coerceIn(TALLEST, WIDEST)

    const val DEFAULT_RATIO = 16f / 9f
    /** 9:16, a phone video held upright. */
    const val TALLEST = 9f / 16f
    /** 21:9, the widest video. */
    const val WIDEST = 21f / 9f

    /** A GIF file ("GIF87a" or "GIF89a"). */
    fun isGif(b: ByteArray): Boolean = b.size >= 6 && (ascii(b, 0, "GIF87a") || ascii(b, 0, "GIF89a"))

    private fun ascii(b: ByteArray, at: Int, s: String) = s.indices.all { b[at + it] == s[it].code.toByte() }
}
