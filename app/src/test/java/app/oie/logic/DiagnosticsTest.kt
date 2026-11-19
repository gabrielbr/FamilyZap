package app.oie.logic

import app.oie.data.Diagnostics
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsTest {
    @Test fun phoneNumbersAreMasked() {
        assertEquals("send TEXT to ••••••00@s.whatsapp.net: ok", Diagnostics.mask("send TEXT to 5511999990000@s.whatsapp.net: ok"))
        assertEquals("reason 401", Diagnostics.mask("reason 401"))
    }

    @Test fun theReportKeepsTheNewestLinesOnly() {
        val dir = Files.createTempDirectory("diag").toFile()
        Diagnostics.init(dir, "Oiê test")
        Diagnostics.clear()
        repeat(Diagnostics.MAX_LINES + 20) { Diagnostics.log("line $it") }
        val r = Diagnostics.report()
        assertTrue(r.startsWith("Oiê test"))
        assertFalse(r.contains("line 19\n"))
        assertTrue(r.contains("line ${Diagnostics.MAX_LINES + 19}"))
        Diagnostics.clear()
        assertTrue(Diagnostics.isEmpty())
    }
}
