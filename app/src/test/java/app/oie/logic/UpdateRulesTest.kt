package app.oie.logic

import app.oie.update.UpdateRules
import app.oie.update.UpdateRules.Asset
import app.oie.update.UpdateRules.Problem
import app.oie.update.UpdateRules.Release
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRulesTest {
    private val apk = Asset("Oie-0.3.0.apk", "https://github.com/x/Oie-0.3.0.apk", 12_400_000)
    private val sums = Asset("SHA256SUMS.txt", "https://github.com/x/SHA256SUMS.txt", 90)
    private fun rel(tag: String, vararg a: Asset, draft: Boolean = false, pre: Boolean = false) = Release(tag, "Notas", draft, pre, a.toList())

    @Test fun onlyStrictlyNewer() {
        assertTrue(UpdateRules.isNewer("v0.3.0", "0.2.0"))
        assertTrue(UpdateRules.isNewer("v0.10.0", "0.9.9"))
        assertTrue(UpdateRules.isNewer("v1.0", "0.9.9"))
        assertFalse(UpdateRules.isNewer("v0.2.0", "0.2.0"))
        assertFalse(UpdateRules.isNewer("v0.1.9", "0.2.0"))
        assertFalse(UpdateRules.isNewer("nightly", "0.2.0"))
    }

    @Test fun offerNeedsApkAndSumsAndARealRelease() {
        assertEquals("0.3.0", UpdateRules.offer(rel("v0.3.0", apk, sums), "0.2.0")?.version)
        assertNull(UpdateRules.offer(rel("v0.3.0", apk), "0.2.0")) // no sums, no install
        assertNull(UpdateRules.offer(rel("v0.3.0", sums), "0.2.0")) // no apk: "no update", not an error
        assertNull(UpdateRules.offer(rel("v0.3.0", apk, sums, draft = true), "0.2.0"))
        assertNull(UpdateRules.offer(rel("v0.3.0", apk, sums, pre = true), "0.2.0"))
        assertNull(UpdateRules.offer(rel("v0.3.0", apk.copy(url = "http://x/a.apk"), sums), "0.2.0")) // HTTPS only
    }

    @Test fun sumsFile() {
        val list = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855  Oie-0.3.0.apk\n" +
            "aaaa  broken-line\n"
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", UpdateRules.expectedSum(list, "Oie-0.3.0.apk"))
        assertNull(UpdateRules.expectedSum(list, "Other.apk"))
    }

    @Test fun downloadChecks() {
        val h = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertEquals(Problem.INCOMPLETE, UpdateRules.verify(10, 11, h, h))
        assertEquals(Problem.NO_SUM, UpdateRules.verify(11, 11, h, null))
        assertEquals(Problem.CHECKSUM, UpdateRules.verify(11, 11, h, "0".repeat(64)))
        assertNull(UpdateRules.verify(11, 11, h, h.uppercase()))
    }

    @Test fun throttle() {
        assertFalse(UpdateRules.shouldCheck(now = 10 * 60_000, lastCheck = 0, manual = false))
        assertTrue(UpdateRules.shouldCheck(now = 15 * 60_000, lastCheck = 0, manual = false))
        assertTrue(UpdateRules.shouldCheck(now = 1, lastCheck = 0, manual = true))
    }

    @Test fun sizes() {
        assertEquals("9,5 MB", UpdateRules.size(9_480_000, decimalComma = true))
        assertEquals("9.5 MB", UpdateRules.size(9_480_000, decimalComma = false))
        assertEquals("12 MB", UpdateRules.size(12_400_000, decimalComma = true))
    }

    @Test fun picksTheApkForThisTabletsArchitecture() {
        val uni = UpdateRules.Asset("Oie-0.6.0.apk", "https://x/u.apk", 40)
        val arm64 = UpdateRules.Asset("Oie_arm64-v8a_0.6.0.apk", "https://x/a64.apk", 20)
        val arm32 = UpdateRules.Asset("Oie_armeabi-v7a_0.6.0.apk", "https://x/a32.apk", 18)
        val sums = UpdateRules.Asset("SHA256SUMS.txt", "https://x/s", 1)
        val r = UpdateRules.Release("v0.6.0", "", false, false, listOf(arm32, arm64, uni, sums))
        assertEquals(arm64, UpdateRules.offer(r, "0.5.0", listOf("arm64-v8a", "armeabi-v7a", "armeabi"))?.apk)
        assertEquals(arm32, UpdateRules.offer(r, "0.5.0", listOf("armeabi-v7a", "armeabi"))?.apk)
        assertEquals(uni, UpdateRules.offer(r, "0.5.0", listOf("x86_64"))?.apk)
        assertEquals(uni, UpdateRules.offer(r, "0.5.0")?.apk)
    }
}
