package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupSenderTest {
    private val vovo = Contact("5511987650001@s.whatsapp.net", "Vovó", phone = "5511987650001")
    private val primos = Contact("120363000000@g.us", "Primos", isGroup = true)
    private val allowed = listOf(vovo, primos)
    private fun msg(sender: String, alt: String = "", name: String = "", parent: Boolean = false) =
        Message("m", primos.id, false, 0, Kind.TEXT, "oi", sender = sender, senderAlt = alt, senderName = name, fromParent = parent)

    @Test fun someoneOnTheListGoesByTheNameTheKidKnows() {
        assertEquals(GroupSender.Who("Vovó", true), GroupSender.of(msg("5511987650001@s.whatsapp.net", name = "Rosa Silva"), allowed, "Ana"))
        // WhatsApp often names the sender by a LID; the phone address comes along as the other address.
        assertEquals(GroupSender.Who("Vovó", true), GroupSender.of(msg("99887766@lid", "5511987650001@s.whatsapp.net", "Rosa"), allowed, "Ana"))
    }

    @Test fun someoneElseGoesByTheirWhatsAppNameAndIsMarked() {
        assertEquals(GroupSender.Who("Lucas", false), GroupSender.of(msg("123@lid", "5511912345678@s.whatsapp.net", "Lucas"), allowed, "Ana"))
        // No profile name: the number, grouped for reading.
        assertEquals(GroupSender.Who("+55 11 91234-5678", false), GroupSender.of(msg("123@lid", "5511912345678@s.whatsapp.net"), allowed, "Ana"))
    }

    @Test fun theParentGoesByTheirName() = assertEquals(GroupSender.Who("Ana", true), GroupSender.of(msg("", parent = true), allowed, "Ana"))

    @Test fun aGroupIsNeverTakenForAPerson() =
        assertEquals(false, GroupSender.of(msg("120363000000@g.us", name = "x"), allowed, "Ana").onList)
}
