package app.oie.logic

import app.oie.data.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyStickersTest {
    private fun s(id: String) = FamilySticker(id, "$id.sealed", animated = false, saved = false, at = 0)

    @Test fun newStickersAreCappedAndTheOldestDropOff() {
        val state = AppState()
        val dropped = (1..AppState.NEW_STICKERS_MAX + 2).flatMap { state.offerSticker(s("n$it")) }
        assertEquals(AppState.NEW_STICKERS_MAX, state.stickers.size)
        assertEquals(listOf("n1.sealed", "n2.sealed"), dropped)
    }

    @Test fun theSameStickerTwiceIsOfferedOnce() {
        val state = AppState()
        state.offerSticker(s("a"))
        assertEquals(listOf("a.sealed"), state.offerSticker(s("a")))
        assertEquals(1, state.stickers.size)
    }

    @Test fun savedStickersStayWhenNewOnesPileUp() {
        val state = AppState()
        state.offerSticker(s("keep")); state.saveSticker("keep")
        (1..AppState.NEW_STICKERS_MAX + 5).forEach { state.offerSticker(s("n$it")) }
        assertTrue(state.stickers.single { it.id == "keep" }.saved)
    }
}
