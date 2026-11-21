package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class VCardTest {
    @Test fun whatsAppsCard() {
        val v = "BEGIN:VCARD\nVERSION:3.0\nN:Silva;Lucas;;;\nFN:Lucas Silva\nitem1.TEL;waid=5511912345678:+55 11 91234-5678\nitem1.X-ABLabel:Celular\nEND:VCARD"
        assertEquals(listOf(ParentCommand.Card("Lucas Silva", "5511912345678")), VCard.parse(v))
    }

    @Test fun severalCardsAndOneWithoutWhatsApp() {
        val v = "BEGIN:VCARD\r\nFN:Vovó\r\nTEL;type=CELL;waid=5511987650001:+55 11 98765-0001\r\nEND:VCARD\r\n" +
            "BEGIN:VCARD\r\nFN:Casa\r\nTEL:+55 11 3456-7890\r\nEND:VCARD"
        assertEquals(listOf(ParentCommand.Card("Vovó", "5511987650001"), ParentCommand.Card("Casa", "")), VCard.parse(v))
    }

    @Test fun nameFromNWhenThereIsNoFnAndFoldedLines() {
        val v = "BEGIN:VCARD\nN:Souza;Bia\nTEL;waid=55219987\n 60004:+55\nEND:VCARD"
        assertEquals(listOf(ParentCommand.Card("Bia Souza", "5521998760004")), VCard.parse(v))
    }
}
