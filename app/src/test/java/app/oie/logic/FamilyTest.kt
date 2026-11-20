package app.oie.logic

import app.oie.data.AppState
import app.oie.data.Codec
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The v0.33 family features (docs/flows/family-features.md). */
@RunWith(RobolectricTestRunner::class)
class FamilyTest {
    private val day = 24L * 60 * 60 * 1000

    @Test fun receiptsOnlyMoveForward() {
        assertEquals(Receipt.DELIVERED, Receipts.raise(Receipt.SENT, Receipt.DELIVERED))
        assertEquals(Receipt.READ, Receipts.raise(Receipt.DELIVERED, Receipt.READ))
        assertEquals(Receipt.READ, Receipts.raise(Receipt.READ, Receipt.DELIVERED)) // a late "delivered" can't undo "seen"
        assertEquals(Receipt.READ, Receipts.raise(Receipt.READ, Receipt.SENT))
        assertEquals(Receipt.DELIVERED, Receipts.of("delivered"))
        assertEquals(Receipt.READ, Receipts.of("read"))
        assertNull(Receipts.of("retry"))
    }

    @Test fun heardMarksOnlyTheKidsMessages() {
        val s = AppState()
        s.messages += Message("k1", "vovo", true, 1, Kind.TEXT, "oi")
        s.messages += Message("v1", "vovo", false, 2, Kind.TEXT, "oi")
        s.heard(setOf("k1", "v1"), Receipt.READ)
        assertEquals(Receipt.READ, s.messages[0].receipt)
        assertEquals(Receipt.NONE, s.messages[1].receipt)
        s.heard(setOf("k1"), Receipt.DELIVERED)
        assertEquals(Receipt.READ, s.messages[0].receipt)
    }

    @Test fun reactionsAreOnePerPersonAndTheSameAgainTakesItAway() {
        val (send, after) = Reactions.kidTaps(emptyMap(), "❤️")
        assertEquals("❤️", send); assertEquals(mapOf("kid" to "❤️"), after)
        val (send2, after2) = Reactions.kidTaps(after, "❤️")
        assertEquals("", send2); assertTrue(after2.isEmpty())
        val (send3, after3) = Reactions.kidTaps(after, "😂")
        assertEquals("😂", send3); assertEquals(mapOf("kid" to "😂"), after3)
        val family = Reactions.set(Reactions.set(after, "a@s", "❤️"), "b@s", "👍")
        assertEquals(listOf("❤️" to 2, "👍" to 1), Reactions.shown(family))
        assertEquals(mapOf("kid" to "❤️", "b@s" to "👍"), Reactions.set(family, "a@s", ""))
        assertEquals(6, Reactions.CHOICES.size)
    }

    @Test fun theKidReactsOnTheTabletAndThroughTheLink() {
        val s = AppState()
        s.messages += Message("v1", "vovo", false, 2, Kind.TEXT, "oi")
        s.react("v1", "❤️")
        assertEquals("❤️", s.messages[0].reactions["kid"])
        assertEquals(listOf("v1" to "❤️"), s.link.reacted)
        s.react("v1", "❤️")
        assertTrue(s.messages[0].reactions.isEmpty())
        assertEquals("v1" to "", s.link.reacted.last())
        s.react("nope", "❤️") // a message the kid doesn't have: nothing
        assertEquals(2, s.link.reacted.size)
    }

    @Test fun familyReactionsLandOnlyOnAMessageInThatChat() {
        val s = AppState()
        s.messages += Message("v1", "vovo", false, 2, Kind.TEXT, "oi")
        s.reacted("v1", "mae", "x@s", "❤️") // wrong chat
        assertTrue(s.messages[0].reactions.isEmpty())
        s.reacted("v1", "vovo", "vovo", "😂")
        assertEquals(mapOf("vovo" to "😂"), s.messages[0].reactions)
    }

    @Test fun birthdaysTakeDayAndMonthOnly() {
        assertEquals("12-25", Birthdays.parse("25/12"))
        assertEquals("12-25", Birthdays.parse(" 25/12/1950 "))
        assertEquals("03-07", Birthdays.parse("7-3"))
        assertEquals("02-29", Birthdays.parse("29/02"))
        assertEquals("", Birthdays.parse(""))
        assertNull(Birthdays.parse("31/02"))
        assertNull(Birthdays.parse("25"))
        assertNull(Birthdays.parse("ab/cd"))
        assertEquals("25/12", Birthdays.display("12-25"))
        assertEquals("", Birthdays.display(""))
    }

    @Test fun birthdaysShowOnTheDayAndLeapDaysOnThe28th() {
        val vovo = Contact("vovo", "Vovó", birthday = "12-25")
        val leap = Contact("leap", "Tia", birthday = "02-29")
        val group = Contact("g", "Primos", isGroup = true, birthday = "12-25")
        val paused = Contact("p", "Vô", paused = true, birthday = "12-25")
        assertEquals(listOf(vovo), Birthdays.today(listOf(vovo, group, paused), LocalDate.of(2026, 12, 25)))
        assertTrue(Birthdays.today(listOf(vovo), LocalDate.of(2026, 12, 24)).isEmpty())
        assertEquals(listOf(leap), Birthdays.today(listOf(leap), LocalDate.of(2027, 2, 28)))
        assertTrue(Birthdays.today(listOf(leap), LocalDate.of(2028, 2, 28)).isEmpty())
        assertEquals(listOf(leap), Birthdays.today(listOf(leap), LocalDate.of(2028, 2, 29)))
    }

    @Test fun theNudgeIsOffByDefaultAndGentle() {
        val now = 100 * day
        val vovo = Contact("vovo", "Vovó"); val mae = Contact("mae", "Mãe"); val novo = Contact("novo", "Tio")
        val group = Contact("g", "Primos", isGroup = true); val line = Contact("line", "Ana", isGroup = true, parentLine = true)
        val msgs = listOf(
            Message("1", "vovo", true, now - 9 * day, Kind.TEXT), Message("2", "mae", true, now - 8 * day, Kind.TEXT),
            Message("3", "g", true, now - 30 * day, Kind.TEXT), Message("4", "line", true, now - 30 * day, Kind.TEXT),
            Message("5", "mae", false, now - 1 * day, Kind.TEXT), // a message to the kid doesn't count: it's the kid writing that matters
        )
        val all = listOf(vovo, mae, novo, group, line)
        assertNull(Nudge.pick(false, all, msgs, emptyMap(), now))
        assertEquals(vovo, Nudge.pick(true, all, msgs, emptyMap(), now)) // the longest quiet first, one at a time
        assertEquals(mae, Nudge.pick(true, all, msgs, mapOf("vovo" to now - day), now)) // "Agora não" for a week
        assertEquals(vovo, Nudge.pick(true, all, msgs, mapOf("vovo" to now - 8 * day), now))
        assertNull(Nudge.pick(true, all, msgs + Message("6", "vovo", true, now - day, Kind.TEXT) + Message("7", "mae", true, now - day, Kind.TEXT), emptyMap(), now))
        assertNull(Nudge.pick(true, listOf(vovo.copy(paused = true)), msgs, emptyMap(), now))
    }

    @Test fun needYouGoesToMarkedPeopleOnceEveryFiveMinutes() {
        val s = AppState(clock = { 1_000_000L })
        s.allowed += Contact("vovo", "Vovó"); s.allowed += Contact("mae", "Mãe", needYou = true); s.allowed += Contact("pai", "Pai", needYou = true, paused = true)
        assertTrue(s.sendNeedYou("Preciso de você.", now = 1_000_000L))
        assertEquals(listOf("mae"), s.link.sent.map { it.chatId })
        assertEquals("Preciso de você.", s.link.sent[0].text)
        assertTrue(s.messages.single().fromKid)
        assertFalse(s.sendNeedYou("Preciso de você.", now = 1_000_000L + NeedYou.GAP_MS - 1))
        assertEquals(1, s.link.sent.size)
        assertTrue(s.sendNeedYou("Preciso de você.", now = 1_000_000L + NeedYou.GAP_MS))
        assertEquals(2, s.link.sent.size)
        val none = AppState(); none.allowed += Contact("vovo", "Vovó")
        assertFalse(none.sendNeedYou("x"))
        assertEquals(0L, NeedYou.waitMs(0, 5))
    }

    @Test fun theNewThingsAreSaved() {
        val s = AppState()
        s.allowed += Contact("vovo", "Vovó", birthday = "12-25", needYou = true)
        s.messages += Message("k1", "vovo", true, 1, Kind.TEXT, "oi", receipt = Receipt.READ, reactions = mapOf("vovo" to "❤️"), sender = "x@s")
        s.nudgeOn = true; s.nudgeDismissed["vovo"] = 77L; s.needYouAt = 88L
        s.finishSetup()
        val back = AppState(); Codec.decode(Codec.encode(s), back)
        assertEquals(s.allowed.toList(), back.allowed.toList())
        assertEquals(s.messages.toList(), back.messages.toList())
        assertTrue(back.nudgeOn); assertEquals(77L, back.nudgeDismissed["vovo"]); assertEquals(88L, back.needYouAt)
    }

    @Test fun anOlderSaveReadsWithTheNewThingsEmpty() {
        val s = AppState(); s.allowed += Contact("vovo", "Vovó"); s.messages += Message("k1", "vovo", true, 1, Kind.TEXT, "oi"); s.finishSetup()
        val old = org.json.JSONObject(Codec.encode(s)).apply {
            remove("nudgeOn"); remove("nudgeDismissed"); remove("needYouAt")
            getJSONArray("allowed").getJSONObject(0).apply { remove("birthday"); remove("needYou") }
            getJSONArray("messages").getJSONObject(0).apply { remove("receipt"); remove("reactions"); remove("sender") }
        }
        val back = AppState(); Codec.decode(old.toString(), back)
        assertEquals("", back.allowed[0].birthday); assertFalse(back.allowed[0].needYou)
        assertEquals(Receipt.NONE, back.messages[0].receipt); assertTrue(back.messages[0].reactions.isEmpty())
        assertFalse(back.nudgeOn)
    }
}
