package app.oie.logic

import app.oie.logic.ProfilePicture.State
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProfilePictureTest {
    private val vovo = Contact("5511987650001@s.whatsapp.net", "Vovó", waName = "Maria Silva", quietException = true)

    @Test fun checkedOnceADay() {
        assertTrue(ProfilePicture.due(vovo, 1_000))
        val checked = vovo.copy(photoAt = 1_000)
        assertFalse(ProfilePicture.due(checked, 1_000 + ProfilePicture.EVERY_MS - 1))
        assertTrue(ProfilePicture.due(checked, 1_000 + ProfilePicture.EVERY_MS))
    }

    @Test fun readsTheLinksAnswers() {
        assertEquals(State.SAME, ProfilePicture.parse("""{"state":"same","id":"7"}""")!!.state)
        assertEquals(State.NONE, ProfilePicture.parse("""{"state":"none"}""")!!.state)
        val n = ProfilePicture.parse("""{"state":"new","id":"8","jpeg":"/9g="}""")!!
        assertEquals("8", n.id); assertArrayEquals(byteArrayOf(0xff.toByte(), 0xd8.toByte()), n.jpeg)
        assertNull(ProfilePicture.parse("""{"state":"new","id":"8"}"""))
        assertNull(ProfilePicture.parse("oops")); assertNull(ProfilePicture.parse(null))
    }

    @Test fun applyingKeepsEverythingElse() {
        val withPic = ProfilePicture.apply(vovo, ProfilePicture.Answer(State.NEW, "8", byteArrayOf(1)), "avatar-a.sealed", 5)
        assertEquals(vovo.copy(photo = "avatar-a.sealed", photoId = "8", photoAt = 5), withPic)
        assertEquals(withPic.copy(photoAt = 9), ProfilePicture.apply(withPic, ProfilePicture.Answer(State.SAME, "8", null), null, 9))
        assertEquals(vovo.copy(photoAt = 9), ProfilePicture.apply(withPic, ProfilePicture.Answer(State.NONE, "", null), null, 9))
    }
}
