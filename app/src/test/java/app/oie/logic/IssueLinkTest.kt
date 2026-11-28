package app.oie.logic

import java.net.URLDecoder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IssueLinkTest {
    private fun note(kept: Int, total: Int) = "só $kept de $total linhas"
    private fun body(url: String) = URLDecoder.decode(url.substringAfter("&body="), "UTF-8")

    @Test fun aShortReportGoesWhole() {
        val url = IssueLink.build("Relatório", "Oiê 0.18.0", listOf("a", "b"), ::note)
        assertTrue(url.startsWith("https://github.com/gabrielbr/oie-dev/issues/new?title=Relat"))
        val b = body(url)
        assertTrue(b.contains("Oiê 0.18.0") && b.contains("a\nb"))
        assertFalse(b.contains("linhas"))
    }

    @Test fun aLongReportKeepsTheNewestLinesAndSaysSo() {
        val lines = (1..300).map { "19/11 18:00:00 link: something happened, line $it with some detail to make it long" }
        val url = IssueLink.build("Relatório", "Oiê", lines, ::note)
        assertTrue(url.substringAfter("&body=").length <= IssueLink.MAX_ENCODED_BODY)
        val b = body(url)
        assertTrue(b.contains("line 300"))
        assertFalse(b.contains("line 1 "))
        assertTrue(b.contains("de 300 linhas"))
    }

    @Test fun theStartOfTheLastCrashIsKeptEvenWhenOld() {
        val crash = listOf(
            "28/11 10:46:50 CRASH on main: java.lang.IllegalArgumentException: Can't represent a size of 300000 in Constraints",
            "\tat androidx.compose.ui.unit.Constraints.a(SourceFile:1)",
        ) + (1..200).map { "\tat frame$it(SourceFile:$it)" } + listOf("Caused by: java.lang.Error: inner", "\tat inner.frame(SourceFile:1)")
        val after = (1..100).map { "28/11 10:47:00 link: something happened later, line $it with some detail to make it long" }
        val lines = (1..20).map { "28/11 10:00:00 link: before $it" } + crash + after
        val b = body(IssueLink.build("Relatório", "Oiê", lines, ::note))
        assertTrue(b.contains("Can't represent a size of 300000"))
        assertTrue(b.contains("Caused by: java.lang.Error: inner"))
        assertTrue(b.contains("line 100"))
        assertTrue(b.contains("\n" + IssueLink.GAP + "\n"))
        assertFalse(b.contains("before 1\n"))
        assertFalse(b.contains("frame150("))
    }

    @Test fun noCrashNothingPinned() {
        assertTrue(IssueLink.crashHead(listOf("a", "b")).isEmpty())
    }
}
