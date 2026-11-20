package app.oie.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * v0.23: every photo the kid picked or took was refused ("Não deu para abrir essa foto"), because
 * a size-only decode returns null by design and that null was read as a failure. These run the
 * real decoding on real JPEG files.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class PhotosKeepTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    @Before fun plainSealer() {
        Photos.sealer = object : Sealer {
            override fun seal(plain: ByteArray) = plain
            override fun open(sealed: ByteArray) = sealed
        }
    }

    private fun jpeg(w: Int, h: Int, orientation: Int? = null): Uri {
        val f = File(context.cacheDir, "in-$w-$h-$orientation.jpg")
        f.outputStream().use { Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 90, it) }
        if (orientation != null) android.media.ExifInterface(f.path).apply {
            setAttribute(android.media.ExifInterface.TAG_ORIENTATION, orientation.toString()); saveAttributes()
        }
        return Uri.fromFile(f)
    }

    private fun stored(name: String) = Photos.openBytes(context, name)!!.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

    @Test fun aPhotoIsKept() {
        val name = Photos.keep(context, jpeg(400, 300))
        assertNotNull("a plain JPEG must be accepted", name)
        val b = stored(name!!)
        assertEquals(400, b.width); assertEquals(300, b.height)
    }

    @Test fun aBigPhotoIsScaledDown() {
        val b = stored(Photos.keep(context, jpeg(4000, 3000))!!)
        assertEquals(Photos.MAX_SIDE, maxOf(b.width, b.height))
    }

    @Test fun aCameraPhotoIsTurnedUpright() {
        val b = stored(Photos.keep(context, jpeg(400, 300, android.media.ExifInterface.ORIENTATION_ROTATE_90))!!)
        assertEquals(300, b.width); assertEquals(400, b.height)
    }

    @Test fun somethingThatIsNotAPictureIsRefused() {
        val f = File(context.cacheDir, "not.jpg").apply { writeText("hello") }
        assertNull(Photos.keep(context, Uri.fromFile(f)))
    }

    /** v0.33: "Criar figurinha": the middle of a photo, round, 512 px, clear outside, as WhatsApp takes it. */
    @Test fun aPhotoBecomesARoundSticker() {
        val photo = Photos.keep(context, jpeg(1200, 800))!!
        val sticker = Photos.stickerFrom(context, photo)
        assertNotNull(sticker)
        val bytes = Photos.openBytes(context, sticker!!)!!
        assertEquals("RIFF", String(bytes.copyOfRange(0, 4))) // WebP
        val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertEquals(512, b.width); assertEquals(512, b.height)
        assertEquals(0, android.graphics.Color.alpha(b.getPixel(2, 2))) // a corner is outside the circle
        assertEquals(255, android.graphics.Color.alpha(b.getPixel(256, 256)))
        assertNull(Photos.stickerFrom(context, "missing.sealed"))
    }
}
