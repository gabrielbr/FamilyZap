package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KidNamesTest {
    @Test fun nicknamesAreSplitTidiedAndUnique() {
        assertEquals(listOf("Tito", "Chico", "Fran"), KidNames.parse(" Tito, Chico ;Fran, tito,, "))
        assertEquals(KidNames.MAX, KidNames.parse((1..20).joinToString(",") { "n$it" }).size)
    }

    @Test fun allPutsTheMainNameFirst() {
        assertEquals(listOf("Francisco", "Tito", "Chico"), KidNames.all("Francisco", listOf("Tito", "francisco", "Chico")))
    }

    @Test fun anyNameAfterAShortGreeting() {
        val n = listOf("Francisco", "Tito", "Chico")
        for (t in listOf("Tito, vem jantar", "Oi Tito", "Oi, Chico!", "Olá Francisco", "Bom dia, Tito", "boa noite tito", "E aí, Chico", "Oiê Tito", "Hi Tito", "oii tito")) {
            assertTrue(t, MessageRule.addressed(t, n))
        }
        for (t in listOf("Oi, o Tito está aí?", "Fala pro Tito", "Titozão", "Oi", "Oi oi Tito", "Bom Tito")) {
            assertFalse(t, MessageRule.addressed(t, n))
        }
    }
}
