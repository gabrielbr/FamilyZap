package app.oie.logic

import app.oie.ui.text.En
import app.oie.ui.text.Pt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The voice rules (01 Brand/voice.md) checked on every string, in both languages. */
class StringsTest {
    private fun all(s: app.oie.ui.text.Strings): List<String> = listOf(
        s.welcomeBody, s.riskBody, s.riskAccept, s.riskDecline, s.pinEasy, s.photoError, s.noticeBody("Ana"),
        s.linkDropped("Ana"), s.quietUntil(18), s.firstHi("Vovó"), s.tooMany, s.notWhatsApp, s.aboutVersion("0.1.0"),
    ) + s.linkSteps + s.helpItems("Tito", "Ana").flatMap { listOf(it.first, it.second) }

    @Test fun noExclamationNoEmDashNoZap() {
        for (s in listOf(Pt, En)) for (t in all(s)) {
            assertFalse("'!' in: $t", t.contains('!'))
            assertFalse("em dash in: $t", t.contains('—'))
            assertFalse("Zap in: $t", Regex("\\bZap\\b|Wpp").containsMatchIn(t))
        }
    }

    @Test fun riskNoticeIsTheApprovedWording() {
        assertEquals(
            "O Oiê usa o seu WhatsApp de um jeito que o WhatsApp não oferece oficialmente. Ele pode limitar ou bloquear o seu número. Se isso acontecer, você perde o acesso até resolver com o WhatsApp.",
            Pt.riskBody,
        )
        assertTrue(En.riskBody.startsWith("Oiê uses your WhatsApp in a way WhatsApp doesn't officially offer."))
    }

    @Test fun plurals() {
        assertEquals("1 nova", Pt.newCount(1)); assertEquals("3 novas", Pt.newCount(3))
        assertEquals("1 pessoa, 2 grupos", Pt.peopleAndGroups(1, 2)); assertEquals("2 people, 1 group", En.peopleAndGroups(2, 1))
    }

    @Test fun clocks() {
        assertEquals("21h", Pt.clock(21, 0)); assertEquals("7h30", Pt.clock(7, 30))
        assertEquals("9 pm", En.clock(21, 0)); assertEquals("12 am", En.clock(0, 0)); assertEquals("7:30 am", En.clock(7, 30))
    }
}
