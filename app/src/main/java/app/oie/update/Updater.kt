package app.oie.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Self-update (Gabriel Design System docs/self-update.md): a check on every return to the foreground,
 * at most every 15 minutes; the sheet; a download with progress and Cancel; install only when the file
 * is complete, matches SHA256SUMS.txt, and is the same package signed with the same key; then Android's
 * own confirmation. The check is a plain request to GitHub's public API and sends nothing about anyone.
 */
class Updater(private val context: Context, private val scope: CoroutineScope) {
    sealed interface State {
        data object Idle : State
        data object Checking : State
        data class UpToDate(val version: String) : State
        data class CouldNotCheck(val reason: String) : State
        data class Available(val offer: UpdateRules.Offer) : State
        data class Downloading(val offer: UpdateRules.Offer, val percent: Int) : State
        data class Failed(val offer: UpdateRules.Offer, val problem: UpdateRules.Problem) : State
        data object NeedsInstallPermission : State
    }

    var state: State by mutableStateOf(State.Idle)
        private set
    /** "Later" hides the sheet for that version until the next cold start. */
    var dismissed: String? by mutableStateOf(null)

    private val prefs = context.getSharedPreferences("update", Context.MODE_PRIVATE)
    private var job: Job? = null

    val installed: PackageInfo get() = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
    val versionName: String get() = installed.versionName ?: "0"
    @Suppress("DEPRECATION") val versionCode: Long get() = if (Build.VERSION.SDK_INT >= 28) installed.longVersionCode else installed.versionCode.toLong()
    /** A debug copy is signed with a different key, so it can't update itself. */
    val isReleaseBuild: Boolean get() = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0

    fun onForeground() = check(manual = false)

    fun check(manual: Boolean) {
        if (!isReleaseBuild || job?.isActive == true) return
        val now = System.currentTimeMillis()
        if (!UpdateRules.shouldCheck(now, prefs.getLong("last", 0), manual)) return
        if (manual) state = State.Checking
        job = scope.launch {
            val result = withContext(Dispatchers.IO) { fetch(manual) }
            prefs.edit().putLong("last", now).apply()
            state = when {
                result.isFailure -> if (manual) State.CouldNotCheck(result.exceptionOrNull()?.message ?: "erro") else State.Idle
                else -> result.getOrNull()?.let { UpdateRules.offer(it, versionName) }?.let { State.Available(it) }
                    ?: if (manual) State.UpToDate(versionName) else State.Idle
            }
        }
    }

    /** GET releases/latest with If-None-Match; a 304 re-reads the cached release. */
    private fun fetch(manual: Boolean): Result<UpdateRules.Release?> = runCatching {
        val c = URL("https://api.github.com/repos/${UpdateRules.REPO}/releases/latest").openConnection() as HttpURLConnection
        c.setRequestProperty("Accept", "application/vnd.github+json")
        c.setRequestProperty("User-Agent", "Oie-app")
        if (!manual) prefs.getString("etag", null)?.let { c.setRequestProperty("If-None-Match", it) }
        c.connectTimeout = 10_000; c.readTimeout = 15_000
        val body = when (c.responseCode) {
            200 -> c.inputStream.bufferedReader().readText().also { b -> prefs.edit().putString("etag", c.getHeaderField("ETag")).putString("cache", b).apply() }
            304 -> prefs.getString("cache", null) ?: return@runCatching null
            404 -> return@runCatching null // no release yet: nothing to offer
            403, 429 -> error("GitHub: muitas consultas")
            else -> error("GitHub ${c.responseCode}")
        }
        parse(body)
    }

    private fun parse(json: String): UpdateRules.Release {
        val o = JSONObject(json)
        val a = o.optJSONArray("assets")
        val assets = (0 until (a?.length() ?: 0)).map { i ->
            a!!.getJSONObject(i).let { UpdateRules.Asset(it.getString("name"), it.getString("browser_download_url"), it.getLong("size")) }
        }
        return UpdateRules.Release(o.getString("tag_name"), o.optString("body"), o.optBoolean("draft"), o.optBoolean("prerelease"), assets)
    }

    fun later(offer: UpdateRules.Offer) { dismissed = offer.version; state = State.Idle }

    /** Update: permission first, then download, verify and install. One download at a time. */
    fun update(offer: UpdateRules.Offer) {
        if (state is State.Downloading) return
        if (!context.packageManager.canRequestPackageInstalls()) {
            state = State.NeedsInstallPermission
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        val file = File(context.cacheDir, "update.apk")
        state = State.Downloading(offer, 0)
        job = scope.launch {
            val problem = withContext(Dispatchers.IO) { download(offer, file) }
            if (problem != null) { file.delete(); state = State.Failed(offer, problem); return@launch }
            install(file)
            state = State.Idle
        }
    }

    fun cancel() { job?.cancel(); File(context.cacheDir, "update.apk").delete(); state = State.Idle }

    private suspend fun download(offer: UpdateRules.Offer, file: File): UpdateRules.Problem? = try {
        val sums = (URL(offer.sums.url).openConnection() as HttpURLConnection).run {
            instanceFollowRedirects = true; inputStream.bufferedReader().readText()
        }
        val c = URL(offer.apk.url).openConnection() as HttpURLConnection
        c.instanceFollowRedirects = true
        val digest = MessageDigest.getInstance("SHA-256")
        var read = 0L
        c.inputStream.use { input ->
            file.outputStream().use { out ->
                val buf = ByteArray(64 * 1024)
                while (currentCoroutineContext().isActive) {
                    val n = input.read(buf); if (n < 0) break
                    out.write(buf, 0, n); digest.update(buf, 0, n); read += n
                    val pct = (read * 100 / offer.apk.size.coerceAtLeast(1)).toInt().coerceIn(0, 100)
                    withContext(Dispatchers.Main) { state = State.Downloading(offer, pct) }
                }
            }
        }
        if (c.url.protocol != "https") UpdateRules.Problem.NETWORK // redirected off HTTPS
        else UpdateRules.verify(read, offer.apk.size, digest.digest().joinToString("") { "%02x".format(it) }, UpdateRules.expectedSum(sums, offer.apk.name))
            ?: if (!sameSigner(file)) UpdateRules.Problem.SIGNATURE else null
    } catch (e: Exception) {
        UpdateRules.Problem.NETWORK
    }

    /** Same package, signed with the installed app's key. */
    private fun sameSigner(file: File): Boolean {
        val pm = context.packageManager
        val apk = pm.getPackageArchiveInfo(file.path, PackageManager.GET_SIGNING_CERTIFICATES) ?: return false
        if (apk.packageName != context.packageName) return false
        val mine = installed.signingInfo?.apkContentsSigners?.map { it.toByteArray().contentHashCode() }?.toSet() ?: return false
        val theirs = apk.signingInfo?.apkContentsSigners?.map { it.toByteArray().contentHashCode() }?.toSet() ?: return false
        return mine == theirs
    }

    /** Hands the verified file to Android's PackageInstaller; Android asks the person to confirm. */
    private fun install(file: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply { setAppPackageName(context.packageName) }
        val id = installer.createSession(params)
        installer.openSession(id).use { session ->
            session.openWrite("oie", 0, file.length()).use { out -> file.inputStream().use { it.copyTo(out) }; session.fsync(out) }
            val intent = Intent(context, app.oie.MainActivity::class.java).setAction(ACTION_INSTALL_STATUS)
            val pi = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            session.commit(pi.intentSender)
        }
        file.delete()
    }

    companion object {
        const val ACTION_INSTALL_STATUS = "app.oie.INSTALL_STATUS"

        /** Called by MainActivity with the installer's answer: shows Android's confirmation when it asks for one. */
        fun onInstallStatus(context: Context, intent: Intent) {
            if (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -99) == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                @Suppress("DEPRECATION")
                (intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT))?.let { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        }
    }
}
