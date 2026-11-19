package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContactsJsonTest {
    @Test fun nullOrBrokenIsAnEmptyListNotACrash() {
        assertTrue(ContactsJson.parse("null").isEmpty())
        assertTrue(ContactsJson.parse(null).isEmpty())
        assertTrue(ContactsJson.parse("").isEmpty())
        assertTrue(ContactsJson.parse("{oops").isEmpty())
    }

    @Test fun aListReadsAndSkipsEntriesWithoutANameOrId() {
        val list = ContactsJson.parse("""[{"id":"a@s.whatsapp.net","name":"Vovó","isGroup":false,"phone":"5511987650001"},{"id":"g@g.us","name":"Família","isGroup":true},{"id":"x"},null]""")
        assertEquals(listOf(Contact("a@s.whatsapp.net", "Vovó", phone = "5511987650001"), Contact("g@g.us", "Família", isGroup = true)), list)
    }
}
