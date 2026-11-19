package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RealInboxTest {
    private val vovo = Contact("5511900000001@s.whatsapp.net", "Vovó")
    private val allowed = listOf(vovo)
    private fun route(chat: String, alt: String? = null, fromMe: Boolean = false, id: String = "m1", ours: Set<String> = emptySet()) =
        RealInbox.toMessage(id, chat, alt, fromMe, ours, allowed, 1L, Kind.TEXT, "Tito, oi", null, 0)

    @Test fun aChatThatIsNotAllowedIsNeverStored() = assertNull(route("5511999999999@s.whatsapp.net"))

    @Test fun theParentsOwnMessagesStayPrivate() = assertNull(route(vovo.id, fromMe = true))

    @Test fun theTabletsOwnSendComingBackIsNotDoubled() = assertNull(route(vovo.id, fromMe = true, id = "k1", ours = setOf("k1")))

    @Test fun anAllowedChatMatchesByEitherAddress() {
        assertEquals(vovo.id, route(vovo.id)?.chatId)
        assertEquals(vovo.id, route("123456@lid", alt = vovo.id)?.chatId)
    }
}
