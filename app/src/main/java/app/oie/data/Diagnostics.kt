package app.oie.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The problem report (docs/flows/problem-report.md, v0.14). What the link did, its warnings and
 * errors, and any crash, kept on the tablet only, in the app's private no-backup storage. The
 * parent copies it from Ajuda; Oiê never sends it. No message text or names are written,
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

    /**
     * v0.15: what ended Oiê last time, when it wasn't an ordinary crash:
     *  - a Go fatal error from the WhatsApp library, which Go wrote to [goCrash];
     *  - Android's own record of why the app's process ended (low memory, killed, a native crash),
     *    for each exit newer than the last one already reported.
     * Called once when the app starts.
     */
    fun collectPreviousExits(context: android.content.Context, goCrash: File) {
        runCatching {
            if (goCrash.exists() && goCrash.length() > 0) {
                log("GO CRASH (WhatsApp library): " + goCrash.readText().take(6000))
                recordCrash(goCrash.lastModified())
            }
            goCrash.delete()
        }
        if (android.os.Build.VERSION.SDK_INT < 30) return
        runCatching {
            val seenFile = File(file?.parentFile, "last-exit-seen.txt")
            val seen = seenFile.takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull() ?: 0L
            val am = context.getSystemService(android.app.ActivityManager::class.java)
            val exits = am.getHistoricalProcessExitReasons(context.packageName, 0, 10).filter { it.timestamp > seen }.sortedBy { it.timestamp }
            for (e in exits) {
                log("exit: ${exitReason(e.reason)} · ${e.description ?: ""} · importance ${e.importance} · ${e.pss / 1024} MB")
                if (e.reason in CRASH_REASONS) recordCrash(e.timestamp)
            }
            exits.maxOfOrNull { it.timestamp }?.let { seenFile.writeText(it.toString()) }
        }
    }

    /** Exits that mean Oiê closed by itself, rather than the person or the system closing it normally. */
    private val CRASH_REASONS = setOf(4, 5, 6, 3) // CRASH, CRASH_NATIVE, ANR, LOW_MEMORY

    fun exitReason(code: Int): String = when (code) {
        1 -> "EXIT_SELF"; 2 -> "SIGNALED"; 3 -> "LOW_MEMORY"; 4 -> "CRASH"; 5 -> "CRASH_NATIVE"; 6 -> "ANR"
        7 -> "INITIALIZATION_FAILURE"; 8 -> "PERMISSION_CHANGE"; 9 -> "EXCESSIVE_RESOURCE_USAGE"; 10 -> "USER_REQUESTED"
        11 -> "USER_STOPPED"; 12 -> "DEPENDENCY_DIED"; 13 -> "OTHER"; 14 -> "FREEZER"; 15 -> "PACKAGE_STATE_CHANGE"; 16 -> "PACKAGE_UPDATED"
        else -> "UNKNOWN($code)"
    }

    /** When Oiê last closed by itself, or null. */
    fun lastCrashAt(): Long? = crashFile?.takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull()

    fun isEmpty(): Boolean = synchronized(lock) { file?.let { !it.exists() || it.length() == 0L } ?: true }

    /** The first line of the report: version, Android and tablet. */
    fun header(): String = header

    /** The report's lines, oldest first. */
    fun lines(): List<String> = synchronized(lock) { file?.takeIf { it.exists() }?.readLines()?.filter { it.isNotBlank() } ?: emptyList() }

    /** The text the parent copies. */
    fun report(): String = synchronized(lock) {
        header + "\n\n" + (file?.takeIf { it.exists() }?.readText() ?: "")
    }

    fun clear() = synchronized(lock) { file?.delete(); crashFile?.delete(); Unit }
}
