package app.oie.logic

import java.net.URLDecoder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IssueLinkTest {
    private fun note(kept: Int, total: Int) = "últimas $kept de $total linhas"
    private fun body(url: String) = URLDecoder.decode(url.substringAfter("&body="), "UTF-8")

    @Test fun aShortReportGoesWhole() {
        val url = IssueLink.build("Relatório", "Oiê 0.18.0", listOf("a", "b"), ::note)
        assertTrue(url.startsWith("https://github.com/gabrielbr/oie-dev/issues/new?title=Relat"))
        val b = body(url)
        assertTrue(b.contains("Oiê 0.18.0") && b.contains("a\nb"))
        assertFalse(b.contains("últimas"))
    }

    @Test fun aLongReportKeepsTheNewestLinesAndSaysSo() {
        val lines = (1..300).map { "19/11 18:00:00 link: something happened, line $it with some detail to make it long" }
        val url = IssueLink.build("Relatório", "Oiê", lines, ::note)
        assertTrue(url.substringAfter("&body=").length <= IssueLink.MAX_ENCODED_BODY)
        val b = body(url)
        assertTrue(b.contains("line 300"))
        assertFalse(b.contains("line 1 "))
        assertTrue(b.contains("últimas") && b.contains("de 300 linhas"))
    }
}
