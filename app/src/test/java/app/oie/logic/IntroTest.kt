package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class IntroTest {
    private val vovo = Contact("vovo", "Vovó")
    private val primos = Contact("g", "Primos", isGroup = true)

    @Test fun onceInEveryChat() {
        assertTrue(Intro.needed(true, vovo, emptySet()))
        assertTrue(Intro.needed(true, primos, setOf("vovo")))
        assertFalse(Intro.needed(true, vovo, setOf("vovo")))
    }

    @Test fun neverOnTheParentsLineNorWhenTurnedOff() {
        assertFalse(Intro.needed(true, Contact("me", "Ana", parentLine = true), emptySet()))
        assertFalse(Intro.needed(false, vovo, emptySet()))
    }

    @Test fun theApprovedWordsStartWithTheMarkAndCarryTheLink() {
        val p = app.oie.ui.text.Pt.introPerson("Tito", "Ana")
        assertTrue(p.startsWith(Intro.MARK) && p.endsWith(Intro.LINK_PT))
        assertTrue(app.oie.ui.text.Pt.introGroup("Tito", "Ana").contains("Tito vê tudo o que o grupo fala"))
        assertTrue(app.oie.ui.text.En.introPerson("Tito", "Ana").endsWith(Intro.LINK_EN))
    }

    // v0.54 (the owner: "it must happen for every new WhatsApp connection").
    @Test fun anotherWhatsAppStartsTheIntrosOver() {
        assertTrue(Intro.accountChanged("5511900000001@s.whatsapp.net", "5511900000002@s.whatsapp.net"))
        // The first intro on a tablet that never sent one belongs to this WhatsApp.
        assertTrue(Intro.accountChanged("", "5511900000002@s.whatsapp.net"))
    }

    @Test fun theSameWhatsAppKeepsItsIntros() {
        assertFalse(Intro.accountChanged("5511900000001@s.whatsapp.net", "5511900000001@s.whatsapp.net"))
    }

    @Test fun anUnknownWhatsAppChangesNothing() {
        assertFalse(Intro.accountChanged("5511900000001@s.whatsapp.net", ""))
    }

    @Test fun forgettingIntrosEmptiesTheListAndItsOwner() {
        val s = app.oie.data.AppState()
        s.introduced += "vovo"; s.introducedBy = "5511900000001@s.whatsapp.net"
        s.forgetIntros()
        assertTrue(s.introduced.isEmpty()); assertEquals("", s.introducedBy)
        // After it, every chat needs the intro again.
        val vovo = Contact("vovo", "Vovó")
        assertTrue(Intro.needed(true, vovo, s.introduced.toSet()))
    }
}
