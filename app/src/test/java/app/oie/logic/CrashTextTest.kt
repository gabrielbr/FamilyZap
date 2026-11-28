package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashTextTest {
    private fun deep(n: Int, e: () -> Throwable): Throwable = if (n == 0) e() else runCatching { throw deep(n - 1, e) }.exceptionOrNull()!!

    @Test fun keepsTheErrorTheFirstFramesAndEachCause() {
        val root = IllegalArgumentException("Can't represent a size of 300000 in Constraints")
        val top = RuntimeException("measure failed", root)
        val s = CrashText.of(top)
        assertTrue(s.startsWith("java.lang.RuntimeException: measure failed"))
        assertTrue(s.contains("Caused by: java.lang.IllegalArgumentException: Can't represent a size of 300000 in Constraints"))
        assertTrue(s.lines().count { it.startsWith("\tat ") } <= CrashText.FRAMES + CrashText.CAUSE_FRAMES)
    }

    @Test fun aDeepTraceIsShortened() {
        val e = deep(0) { IllegalStateException("x") }
        val stack = Array(500) { StackTraceElement("C$it", "m", "F.kt", it) }
        e.stackTrace = stack
        val s = CrashText.of(e)
        assertTrue(s.contains("... ${500 - CrashText.FRAMES} more"))
        assertFalse(s.contains("C499"))
    }
}
