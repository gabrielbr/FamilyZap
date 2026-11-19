package app.oie.logic

import app.oie.data.AppState
import app.oie.data.Codec
import app.oie.data.Route
import app.oie.data.Sealer
import app.oie.data.Store
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Saving on the tablet: what goes in comes back, the parent area is never restored open, a bad file starts fresh. */
@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class PersistenceTest {
    /** A stand-in for the Keystore (not available on the JVM): XOR is enough to prove the file isn't plain text. */
    private val xor = object : Sealer {
        override fun seal(plain: ByteArray) = plain.map { (it.toInt() xor 0x5A).toByte() }.toByteArray()
        override fun open(sealed: ByteArray) = seal(sealed)
    }

    private fun filled() = AppState().apply {
        parentName = "Ana"; kidName = "Tito"; setPin("402817"); kidNoticeSeen = true; lastPhotoSource = "gallery"
        link.pretendLinked()
        allowed.addAll(link.contacts().take(2)); allowed[0] = allowed[0].copy(quietException = true, phone = "5511987650001", waName = "Maria Silva", paused = true)
        periods[0] = periods[0].copy(on = true)
        messages += Message("p1", allowed[0].id, true, 1000L, Kind.PHOTO, media = "a.jpg")
        drafts[allowed[1].id] = "Oi m"
        lastSeen[allowed[0].id] = 999L
        quietCount = 2
        addRequested = true; contactTipUsed = true; kidNicknames += listOf("Tito", "Chico"); kidEmoji = "😎"
        route = Route.Parent(0)
    }

    @Test fun roundTrip() {
        val a = filled()
        val b = AppState().also { Codec.decode(Codec.encode(a), it) }
        assertEquals("Ana", b.parentName); assertEquals("Tito", b.kidName)
        assertTrue(b.checkPin("402817", 0)); assertTrue(b.kidNoticeSeen); assertTrue(b.link.connected)
        assertEquals(a.allowed.toList(), b.allowed.toList())
        assertEquals(a.periods.toList(), b.periods.toList())
        assertEquals(a.messages.toList(), b.messages.toList())
        assertEquals("Oi m", b.drafts[a.allowed[1].id]); assertEquals(999L, b.lastSeen[a.allowed[0].id])
        assertEquals("gallery", b.lastPhotoSource)
        assertEquals(2, b.quietCount); assertTrue(b.addRequested); assertTrue(b.contactTipUsed); assertEquals(listOf("Tito", "Chico"), b.kidNicknames.toList()); assertEquals("😎", b.kidEmoji)
        assertEquals(Route.KidHome, b.route) // never back into the parent area without the PIN
    }

    @Test fun setupResumesAtTheSameStep() {
        val a = AppState().apply { route = Route.Setup(4); parentName = "Ana" }
        val b = AppState().also { Codec.decode(Codec.encode(a), it) }
        assertEquals(Route.Setup(4), b.route)
    }

    @Test fun storeSealsAndReadsBack() {
        val dir = Files.createTempDirectory("oie").toFile()
        val store = Store(File(dir, "state.sealed"), xor)
        val json = Codec.encode(filled())
        store.write(json)
        assertFalse(String(File(dir, "state.sealed").readBytes()).contains("Tito")) // not plain text on disk
        assertEquals(json, store.read())
    }

    @Test fun photosAreSealedOnDisk() {
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        app.oie.data.Photos.sealer = xor
        val bmp = android.graphics.Bitmap.createBitmap(64, 48, android.graphics.Bitmap.Config.ARGB_8888)
        val name = app.oie.data.Photos.store(ctx, bmp)
        val raw = File(app.oie.data.Photos.dir(ctx), name).readBytes()
        // A JPEG starts FF D8; the sealed file must not.
        assertFalse(raw[0] == 0xFF.toByte() && raw[1] == 0xD8.toByte())
        val back = app.oie.data.Photos.load(ctx, name, 64)
        assertEquals(64, back?.width)
    }

    @Test fun badFileStartsFresh() {
        val dir = Files.createTempDirectory("oie").toFile()
        File(dir, "state.sealed").writeBytes(byteArrayOf(1, 2, 3))
        val broken = object : Sealer {
            override fun seal(plain: ByteArray) = plain
            override fun open(sealed: ByteArray): ByteArray = throw javax.crypto.AEADBadTagException()
        }
        assertNull(Store(File(dir, "state.sealed"), broken).read())
    }
}
