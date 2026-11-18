package app.oie.update

/**
 * The self-update rules from the Gabriel Design System (docs/self-update.md), as plain code so they
 * are unit-tested. Android-facing parts live in UpdateChecker / ApkInstaller.
 */
object UpdateRules {
    /** The public repo that holds only Oiê's releases; the code stays private (owner, 2026-11-18). */
    const val REPO = "gabrielbr/oie-releases"
    const val ASSET_PREFIX = "Oie-"
    const val SUMS = "SHA256SUMS.txt"
    const val MIN_INTERVAL_MS = 15 * 60 * 1000L

    data class Asset(val name: String, val url: String, val size: Long)
    data class Release(val tag: String, val notes: String, val draft: Boolean, val prerelease: Boolean, val assets: List<Asset>)
    data class Offer(val version: String, val notes: String, val apk: Asset, val sums: Asset)

    /** "v0.3.0", "v0.3.0+5" or "0.3.0" → [0, 3, 0]. Null when it isn't a version. */
    fun parse(tag: String): List<Int>? {
        val core = tag.removePrefix("v").substringBefore('+').substringBefore('-')
        val parts = core.split('.').map { it.toIntOrNull() ?: return null }
        return if (parts.isEmpty()) null else parts
    }

    /** True only when [candidate] is strictly newer than [installed]. */
    fun isNewer(candidate: String, installed: String): Boolean {
        val a = parse(candidate) ?: return false
        val b = parse(installed) ?: return false
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }; val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    /** What to offer, or null: drafts, prereleases, older or equal versions, and releases without an APK or sums. */
    fun offer(release: Release, installed: String): Offer? {
        if (release.draft || release.prerelease || !isNewer(release.tag, installed)) return null
        val apk = release.assets.firstOrNull { it.name.startsWith(ASSET_PREFIX) && it.name.endsWith(".apk") } ?: return null
        val sums = release.assets.firstOrNull { it.name == SUMS } ?: return null
        if (!apk.url.startsWith("https://") || !sums.url.startsWith("https://")) return null
        return Offer(release.tag.removePrefix("v").substringBefore('+'), release.notes, apk, sums)
    }

    /** The SHA-256 for [file] in a `sha256sum`-style list ("<hex>  <name>" or "<hex> *<name>"). */
    fun expectedSum(sums: String, file: String): String? = sums.lineSequence()
        .map { it.trim() }.filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val parts = line.split(Regex("\\s+"), limit = 2)
            if (parts.size == 2 && parts[1].removePrefix("*") == file && parts[0].matches(Regex("[0-9a-fA-F]{64}"))) parts[0].lowercase() else null
        }.firstOrNull()

    /** All the download checks that don't need Android: complete, and the checksum matches. */
    fun verify(bytesRead: Long, expectedSize: Long, actualSha256: String, expectedSha256: String?): Problem? = when {
        bytesRead != expectedSize -> Problem.INCOMPLETE
        expectedSha256 == null -> Problem.NO_SUM
        !actualSha256.equals(expectedSha256, ignoreCase = true) -> Problem.CHECKSUM
        else -> null
    }

    enum class Problem { INCOMPLETE, NO_SUM, CHECKSUM, SIGNATURE, NETWORK }

    /** Foreground checks at most every 15 minutes; a manual check always runs. */
    fun shouldCheck(now: Long, lastCheck: Long, manual: Boolean) = manual || now - lastCheck >= MIN_INTERVAL_MS

    /** "12,4 MB" / "12.4 MB" for the Update button. */
    fun size(bytes: Long, decimalComma: Boolean): String {
        val mb = bytes / 1_000_000.0
        val s = if (mb >= 10) "%.0f".format(mb) else "%.1f".format(mb).let { if (decimalComma) it.replace('.', ',') else it.replace(',', '.') }
        return "$s MB"
    }
}
