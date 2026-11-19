package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactNamesTest {
    private val maria = Contact("5511987650001@s.whatsapp.net", "Maria Silva")

    @Test fun renameKeepsTheWhatsAppNameForTheParent() {
        val v = ContactNames.rename(maria, "  Vovó  ")
        assertEquals("Vovó", v.name)
        assertEquals("Maria Silva", v.waName)
        assertEquals("Maria Silva", ContactNames.whatsAppName(v))
    }

    @Test fun renamingAgainKeepsTheOriginalWhatsAppName() {
        val v = ContactNames.rename(ContactNames.rename(maria, "Vovó"), "Vó Maria")
        assertEquals("Vó Maria", v.name); assertEquals("Maria Silva", v.waName)
    }

    @Test fun blankOrTheSameNameGoesBackToWhatsApp() {
        val v = ContactNames.rename(maria, "Vovó")
        assertEquals(maria, ContactNames.rename(v, "   "))
        assertEquals(maria, ContactNames.rename(v, "Maria Silva"))
    }

    @Test fun nameIsTidiedAndCapped() {
        assertEquals("Vovó Maria", ContactNames.rename(maria, "Vovó   Maria").name)
        assertEquals(ContactNames.MAX, ContactNames.rename(maria, "a".repeat(80)).name.length)
    }

    @Test fun numberFromTheLinkOrFromAPhoneId() {
        assertEquals("5511987650001", ContactNames.phoneOf(maria))
        assertEquals("447700900123", ContactNames.phoneOf(Contact("12345@lid", "Ann", phone = "447700900123")))
        assertEquals("", ContactNames.phoneOf(Contact("12345@lid", "Ann")))
        assertEquals("", ContactNames.phoneOf(Contact("1203@g.us", "Primos", isGroup = true)))
    }

    @Test fun missingNumberIsFilledFromTheFreshListAndNothingElseChanges() {
        val kept = Contact("12345@lid", "Vovô", quietException = true, waName = "João")
        val filled = ContactNames.withNumber(kept, listOf(Contact("12345@lid", "João Silva", phone = "5511912340000")))
        assertEquals(kept.copy(phone = "5511912340000"), filled)
        assertEquals(kept, ContactNames.withNumber(kept, emptyList()))
    }

    @Test fun displayGroupsABrazilianNumber() {
        assertEquals("+55\u00A011\u00A098765-0001", ContactNames.display("5511987650001"))
        assertEquals("+55\u00A011\u00A03456-7890", ContactNames.display("551134567890"))
        assertEquals("+447700900123", ContactNames.display("447700900123"))
        assertEquals("", ContactNames.display(""))
    }

    @Test fun searchFindsNameWhatsAppNameOrDigits() {
        val v = ContactNames.rename(maria, "Vovó")
        assertTrue(ContactNames.matches(v, "vov"))
        assertTrue(ContactNames.matches(v, "silva"))
        assertTrue(ContactNames.matches(v, "98765"))
        assertTrue(ContactNames.matches(v, "98765-0001"))
        assertFalse(ContactNames.matches(v, "123"))
        assertFalse(ContactNames.matches(v, "Tia 98765"))
    }
}
