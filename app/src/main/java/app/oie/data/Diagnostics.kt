package app.oie.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The problem report (docs/flows/problem-report.md, v0.14). What the link did, its warnings and
 * errors, and any crash, kept on the tablet only, in the app's private no-backup storage. The
 * parent copies it from Ajustes e ajuda; Oiê never sends it. No message text or names are written,
 * and any run of 6+ digits (a phone number) is masked to its last 2.
 */
object Diagnostics {
    const val MAX_LINES = 300
    private var file: File? = null
    private var crashFile: File? = null
    private val lock = Any()

    /** Starts the report and catches crashes. Called once, when the app process starts. */
    fun init(dir: File, header: String) {
        file = File(dir, "problem-report.log"); crashFile = File(dir, "last-crash.txt")
        this.header = header
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runCatching {
                log("CRASH on ${t.name}: ${e.stackTraceToString().take(4000)}")
                recordCrash(System.currentTimeMillis())
            }
            previous?.uncaughtException(t, e)
        }
    }

    private var header: String = ""

    /** "5511999990000@s.whatsapp.net" → "••••••00@s.whatsapp.net". */
    fun mask(s: String): String = s.replace(Regex("\\d{6,}")) { m -> "••••••" + m.value.takeLast(2) }

    fun log(line: String) {
        val f = file ?: return
        val stamp = SimpleDateFormat("dd/MM HH:mm:ss", Locale.ROOT).format(Date())
        synchronized(lock) {
            runCatching {
                val lines = (if (f.exists()) f.readLines() else emptyList()) + mask("$stamp $line").lines()
                f.writeText(lines.takeLast(MAX_LINES).joinToString("\n"))
            }
        }
    }

    fun recordCrash(at: Long) { crashFile?.writeText(at.toString()) }

    /** When Oiê last closed by itself, or null. */
    fun lastCrashAt(): Long? = crashFile?.takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull()

    fun isEmpty(): Boolean = synchronized(lock) { file?.let { !it.exists() || it.length() == 0L } ?: true }

    /** The text the parent copies. */
    fun report(): String = synchronized(lock) {
        header + "\n\n" + (file?.takeIf { it.exists() }?.readText() ?: "")
    }

    fun clear() = synchronized(lock) { file?.delete(); crashFile?.delete(); Unit }
}
