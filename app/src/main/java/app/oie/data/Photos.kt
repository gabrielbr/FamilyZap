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

    /** The file the camera app records a video into (v0.28), shared the same way; replaced each time. */
    fun newVideoTarget(context: Context): File = File(File(context.cacheDir, "camera").apply { mkdirs() }, "clip.mp4").apply { delete() }

    /** Copies [uri] into private storage. Returns the stored file name, or null when it can't be read. */
    fun keep(context: Context, uri: Uri): String? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val first = context.contentResolver.openInputStream(uri) ?: run { Diagnostics.log("photo: can't open the picked file"); return null }
        // With inJustDecodeBounds, decodeStream returns null by design and puts the size in [bounds].
        // v0.23: that null was read as "can't open", so every photo was refused since v0.1 (the owner's report).
        first.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) { Diagnostics.log("photo: not a picture Android can read (${bounds.outMimeType ?: "unknown type"})"); return null }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: run { Diagnostics.log("photo: couldn't decode ${bounds.outMimeType}"); return null }
        val scale = MAX_SIDE.toFloat() / maxOf(bmp.width, bmp.height)
        val sized = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
        // v0.23: the camera stores the turn in the file's metadata; apply it, since the metadata is dropped on re-encoding.
        store(context, upright(sized, orientation(context, uri)))
    }.getOrElse { Diagnostics.log("photo: failed: ${it.javaClass.simpleName}"); null }

    /** The EXIF orientation of [uri], or "normal" when there's none. */
    private fun orientation(context: Context, uri: Uri): Int = runCatching {
        context.contentResolver.openInputStream(uri)?.use {
            android.media.ExifInterface(it).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)
        }
    }.getOrNull() ?: android.media.ExifInterface.ORIENTATION_NORMAL

    /** [bitmap] turned the way the camera held it (the common cases: quarter turns and mirror). */
    internal fun upright(bitmap: Bitmap, orientation: Int): Bitmap {
        val m = android.graphics.Matrix()
        when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    /** Encodes [bitmap] as JPEG, seals it and stores it. Returns the file name. */
    fun store(context: Context, bitmap: Bitmap): String {
        val bytes = java.io.ByteArrayOutputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it); it.toByteArray() }
        val name = UUID.randomUUID().toString() + ".sealed"
        File(dir(context), name).writeBytes(sealer.seal(bytes))
        return name
    }

    /** Seals and stores bytes received through the WhatsApp link (a photo, a sticker, a voice note) as they are. */
    fun keepBytes(context: Context, bytes: ByteArray, prefix: String = ""): String {
        val name = prefix + UUID.randomUUID().toString() + ".sealed"
        File(dir(context), name).writeBytes(sealer.seal(bytes))
        return name
    }

    /** The plain bytes of a stored file, to send it through the WhatsApp link. */
    fun openBytes(context: Context, name: String): ByteArray? =
        File(dir(context), name).takeIf { it.exists() }?.let { runCatching { sealer.open(it.readBytes()) }.getOrNull() }

    /** One of Oiê's stickers as a JPEG on white, to send it as a picture (WhatsApp stickers need another format). */
    fun stickerJpeg(context: Context, res: Int): ByteArray {
        val d = androidx.core.content.ContextCompat.getDrawable(context, res) ?: return ByteArray(0)
        val side = 512
        val bmp = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.WHITE)
        d.setBounds(0, 0, side, side); d.draw(canvas)
        return java.io.ByteArrayOutputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY, it); it.toByteArray() }
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

    /** WhatsApp's sticker side, in px. */
    const val STICKER_SIDE = 512

    /**
     * A round sticker from the middle of a stored photo (v0.33): a 512 px WebP with a clear outside,
     * the form WhatsApp takes. Returns the stored file name, or null when the photo can't be read.
     */
    fun stickerFrom(context: Context, photo: String): String? = runCatching {
        val src = load(context, photo, MAX_SIDE / 2) ?: return null
        val side = minOf(src.width, src.height)
        val square = Bitmap.createBitmap(src, (src.width - side) / 2, (src.height - side) / 2, side, side)
        val out = Bitmap.createBitmap(STICKER_SIDE, STICKER_SIDE, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        val r = STICKER_SIDE / 2f
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(square, null, android.graphics.Rect(0, 0, STICKER_SIDE, STICKER_SIDE), paint)
        @Suppress("DEPRECATION")
        val format = if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
        val bytes = java.io.ByteArrayOutputStream().use { out.compress(format, 80, it); it.toByteArray() }
        keepBytes(context, bytes)
    }.getOrElse { Diagnostics.log("sticker: failed: ${it.javaClass.simpleName}"); null }

    /** Removes a stored photo (when the kid undoes a send). */
    fun forget(context: Context, name: String) { File(dir(context), name).delete() }

    /** Contacts' profile pictures are stored with this prefix (v0.26). */
    const val AVATAR = "avatar-"

    /** Deletes stored profile pictures no contact uses any more (a removed contact, a replaced picture). */
    fun forgetAvatarsExcept(context: Context, keep: Set<String>) {
        dir(context).listFiles()?.filter { it.name.startsWith(AVATAR) && it.name !in keep }?.forEach { it.delete() }
    }
}
