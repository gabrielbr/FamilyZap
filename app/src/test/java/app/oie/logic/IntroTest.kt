package app.oie.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
}
