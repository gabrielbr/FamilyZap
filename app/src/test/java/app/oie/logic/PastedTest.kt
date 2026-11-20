package app.oie.logic

import app.oie.logic.Pasted.Goes
import org.junit.Assert.assertEquals
import org.junit.Test

class PastedTest {
    @Test fun keyboardAndPastedMediaGoToTheRightPreview() {
        assertEquals(Goes.GIF, Pasted.route("image/gif"))
        assertEquals(Goes.PHOTO, Pasted.route("image/png"))
        assertEquals(Goes.PHOTO, Pasted.route("image/webp")) // a Gboard sticker
        assertEquals(Goes.PHOTO, Pasted.route("IMAGE/JPEG"))
        assertEquals(Goes.VIDEO, Pasted.route("video/mp4"))
        assertEquals(Goes.NOTHING, Pasted.route("text/plain"))
        assertEquals(Goes.NOTHING, Pasted.route(null))
    }
}
