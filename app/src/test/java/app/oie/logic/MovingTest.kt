package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovingTest {
    private fun webp(flags: Int): ByteArray {
        val b = ByteArray(30)
        "RIFF".forEachIndexed { i, c -> b[i] = c.code.toByte() }
        "WEBP".forEachIndexed { i, c -> b[8 + i] = c.code.toByte() }
        "VP8X".forEachIndexed { i, c -> b[12 + i] = c.code.toByte() }
        b[20] = flags.toByte()
        return b
    }

    @Test fun anAnimatedStickerIsSpotted() {
        assertTrue(Moving.isAnimatedWebp(webp(0x02)))
        assertTrue(Moving.isAnimatedWebp(webp(0x12)))
        assertFalse(Moving.isAnimatedWebp(webp(0x10)))
        assertFalse(Moving.isAnimatedWebp(ByteArray(4)))
    }

    @Test fun gifs() {
        assertTrue(Moving.isGif("GIF89a....".toByteArray()))
        assertFalse(Moving.isGif("\u0089PNG".toByteArray()))
    }

    @Test fun bigVideosStayInWhatsApp() {
        assertTrue(Moving.download(Kind.VIDEO, 10L * 1024 * 1024))
        assertTrue(Moving.download(Kind.GIF, 0))
        assertFalse(Moving.download(Kind.VIDEO, Moving.MAX_DOWNLOAD_BYTES + 1))
        assertTrue(Moving.download(Kind.PHOTO, Long.MAX_VALUE))
        assertFalse(Moving.download(Kind.TEXT, 0))
    }
}
