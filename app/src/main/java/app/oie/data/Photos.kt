package app.oie.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Photos the kid sends, kept in the app's private storage on the tablet (never on a server).
 * Each photo is re-encoded on the way in: it is scaled to at most [MAX_SIDE] px and saved as a
 * fresh JPEG, which also drops the camera's metadata, including any location. The file is then
 * sealed with AES-GCM under its own Android Keystore key, so it can't be read outside Oiê.
 */
object Photos {
    /** Seals photo files. Tests swap in a stand-in, because the Keystore isn't there on the JVM. */
    var sealer: Sealer = KeystoreSealer("oie-photos")

    const val MAX_SIDE = 1600
    private const val QUALITY = 85

    fun dir(context: Context): File = File(context.filesDir, "photos").apply { mkdirs() }

    /** A file the camera app can write into, shared through the FileProvider (res/xml/file_paths.xml). */
    fun newCameraTarget(context: Context): File = File(File(context.cacheDir, "camera").apply { mkdirs() }, "shot.jpg")

    /** Copies [uri] into private storage. Returns the stored file name, or null when it can't be read. */
    fun keep(context: Context, uri: Uri): String? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val scale = MAX_SIDE.toFloat() / maxOf(bmp.width, bmp.height)
        val out = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
        return store(context, out)
    }

    /** Encodes [bitmap] as JPEG, seals it and stores it. Returns the file name. */
    fun store(context: Context, bitmap: Bitmap): String {
        val bytes = java.io.ByteArrayOutputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it); it.toByteArray() }
        val name = UUID.randomUUID().toString() + ".sealed"
        File(dir(context), name).writeBytes(sealer.seal(bytes))
        return name
    }

    /** A small bitmap for the chat bubble or the preview. */
    fun load(context: Context, name: String, maxSide: Int): Bitmap? {
        val f = File(dir(context), name)
        if (!f.exists()) return null
        val bytes = runCatching { sealer.open(f.readBytes()) }.getOrNull() ?: return null
        val b = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, b)
        var sample = 1
        while (maxOf(b.outWidth, b.outHeight) / (sample * 2) >= maxSide) sample *= 2
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    /** Removes a stored photo (when the kid undoes a send). */
    fun forget(context: Context, name: String) { File(dir(context), name).delete() }
}
