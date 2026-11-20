package app.oie.logic

import app.oie.data.AppState
import app.oie.data.Codec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Family statuses (docs/flows/statuses.md, v0.36). */
@RunWith(RobolectricTestRunner::class)
class StatusesTest {
    private val hour = 60L * 60 * 1000
    private val now = 100 * hour
    private val vovo = Contact("vovo@s.whatsapp.net", "Vovó")

    private fun state(on: Boolean = true) = AppState(clock = { now }).apply {
        statusesOn = on; linkedSince = now - 10 * hour
        allowed += vovo; allowed += Contact("g@g.us", "Primos", isGroup = true)
        allowed += Contact("pai@s.whatsapp.net", "Pai", paused = true); allowed += Contact("line@g.us", "Ana", isGroup = true, parentLine = true)
    }
    private fun st(id: String, contact: String = vovo.id, at: Long = now - hour, kind: Kind = Kind.TEXT) = Status(id, contact, contact, at, kind, "Oi")

    @Test fun onlyAllowedPeopleAfterTheLinkWhileOnAndAlive() {
        val s = state()
        assertTrue(s.receiveStatus(st("1")))
        assertFalse(s.receiveStatus(st("1"))) // twice: once
        assertFalse(s.receiveStatus(st("2", "g@g.us")))
        assertFalse(s.receiveStatus(st("3", "pai@s.whatsapp.net")))
        assertFalse(s.receiveStatus(st("4", "line@g.us")))
        assertFalse(s.receiveStatus(st("5", "stranger@s.whatsapp.net")))
        assertFalse(s.receiveStatus(st("6", at = now - 11 * hour))) // before the link (L6)
        assertFalse(s.receiveStatus(st("7", at = now - 25 * hour)))
        assertFalse(state(on = false).receiveStatus(st("8")))
        assertEquals(listOf("1"), s.statuses.map { it.id })
    }

    @Test fun theyGoAfterADayOrWhenSwitchedOff() {
        val s = state()
        s.statuses += st("a", at = now - 23 * hour).copy(media = "a.sealed")
        s.statuses += st("b", at = now - 25 * hour).copy(media = "b.sealed")
        assertEquals(listOf("b.sealed"), s.expireStatuses())
        s.statusesOn = false
        assertEquals(listOf("a.sealed"), s.expireStatuses())
        assertTrue(s.statuses.isEmpty())
    }

    @Test fun theRowPutsUnseenFirstAndTheViewerStartsThere() {
        val tia = Contact("tia@s.whatsapp.net", "Tia")
        val all = listOf(st("1").copy(viewed = true), st("2", tia.id), st("3", at = now - 2 * hour).copy(viewed = true))
        assertEquals(listOf(tia to true, vovo to false), Statuses.row(all, listOf(vovo, tia), now))
        assertEquals(listOf("3", "1"), Statuses.of(all, vovo.id, now).map { it.id }) // oldest first
        assertEquals(0, Statuses.startAt(Statuses.of(all, vovo.id, now)))
        assertEquals(1, Statuses.startAt(listOf(st("x").copy(viewed = true), st("y"))))
    }

    @Test fun viewingReactingAndReplyingGoToThePoster() {
        val s = state()
        s.receiveStatus(st("1"))
        s.viewStatus("1"); s.viewStatus("1")
        assertTrue(s.statuses[0].viewed)
        assertEquals(listOf("1"), s.link.statusViews) // marked once
        s.reactToStatus("1", "❤️")
        assertEquals("❤️", s.statuses[0].reaction)
        s.reactToStatus("1", "❤️")
        assertEquals("", s.statuses[0].reaction)
        assertEquals(listOf("1" to "❤️", "1" to ""), s.link.reacted)
        val m = s.replyToStatus("1", " Que lindo ", "Resposta a uma novidade")!!
        assertEquals(vovo.id, m.chatId); assertEquals("1", m.replyTo); assertEquals("Que lindo", m.text); assertTrue(m.fromKid)
        assertEquals(m, s.link.sent.single())
        assertNull(s.replyToStatus("gone", "oi", "q"))
        assertNull(s.replyToStatus("1", "  ", "q"))
    }

    @Test fun deletedByItsOwnerGoes() {
        val s = state()
        s.receiveStatus(st("1").copy(media = "m.sealed"))
        assertEquals("m.sealed", s.removeStatus("1"))
        assertNull(s.removeStatus("1"))
    }

    @Test fun statusesAreSaved() {
        val s = state(); s.receiveStatus(st("1").copy(viewed = true, reaction = "😂", media = "m")); s.finishSetup()
        s.messages += Message("k1", vovo.id, true, now, Kind.TEXT, "oi", replyTo = "1", statusQuote = "Resposta a uma novidade")
        val back = AppState(); Codec.decode(Codec.encode(s), back)
        assertTrue(back.statusesOn); assertEquals(s.linkedSince, back.linkedSince)
        assertEquals(s.statuses.toList(), back.statuses.toList())
        assertEquals("Resposta a uma novidade", back.messages.first { it.id == "k1" }.statusQuote)
    }
}
