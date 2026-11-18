package app.oie.logic

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class InboxTest {
    private val vovo = Contact("vovo", "Vovó")
    private val mae = Contact("mae", "Mãe", quietException = true)
    private val allowed = listOf(vovo, mae)
    private val school = QuietPeriod("Escola", DayOfWeek.entries.toSet(), LocalTime.of(7, 0), LocalTime.of(12, 0))
    private val nine = LocalDateTime.of(2026, 11, 18, 9, 0)
    private val three = LocalDateTime.of(2026, 11, 18, 15, 0)
    private fun msg(chat: String, text: String, kind: Kind = Kind.TEXT, replyTo: String? = null, fromKid: Boolean = false) =
        Message("m${text.hashCode()}", chat, fromKid, 1000, kind, text, replyTo)
    private fun decide(m: Message, history: List<Message> = emptyList(), now: LocalDateTime = three, periods: List<QuietPeriod> = listOf(school)) =
        Inbox.decide(m, allowed, history, "Tito", periods, now)

    @Test fun strangersNeverReachTheTablet() {
        assertEquals(Inbox.Decision.DROP, decide(msg("desconhecido", "Tito, oi")))
    }

    @Test fun parentsPrivateMessagesAreNotStored() {
        assertEquals(Inbox.Decision.DROP, decide(msg("vovo", "Filha, me liga")))
    }

    @Test fun forTheKidNotifies() {
        assertEquals(Inbox.Decision.NOTIFY, decide(msg("vovo", "Tito, domingo tem bolo")))
    }

    @Test fun quietHoursHoldUnlessException() {
        assertEquals(Inbox.Decision.QUIET, decide(msg("vovo", "Tito, oi"), now = nine))
        assertEquals(Inbox.Decision.NOTIFY, decide(msg("mae", "Tito, oi"), now = nine))
    }

    @Test fun deleteForEveryone() {
        val shown = msg("vovo", "Tito, oi")
        assertEquals(Inbox.Decision.DELETE, decide(Message("d", "vovo", false, 2000, Kind.DELETED, replyTo = shown.id), listOf(shown)))
        assertEquals(Inbox.Decision.DROP, decide(Message("d", "vovo", false, 2000, Kind.DELETED, replyTo = "never-seen")))
    }

    @Test fun kidsOwnMessagesDontNotify() {
        assertEquals(Inbox.Decision.QUIET, decide(msg("vovo", "Oi vó", fromKid = true)))
    }
}

class ReceiveTest {
    private val nine = java.time.LocalDateTime.of(2026, 11, 18, 9, 0)
    private val three = java.time.LocalDateTime.of(2026, 11, 18, 15, 0)
    private fun state() = app.oie.data.AppState().apply {
        kidName = "Tito"; allowed += Contact("vovo", "Vovó")
        periods.clear(); periods += QuietPeriod("Escola", java.time.DayOfWeek.entries.toSet(), java.time.LocalTime.of(7, 0), java.time.LocalTime.of(12, 0))
    }

    @Test fun privateMessagesAreNeverStored() {
        val s = state()
        s.receive(Message("p", "vovo", false, 1, Kind.TEXT, "Filha, me liga"), three)
        s.receive(Message("x", "estranho", false, 1, Kind.TEXT, "Tito, oi"), three)
        assertEquals(0, s.messages.size)
    }

    @Test fun quietMessagesBecomeOneSummaryAfterTheyEnd() {
        val s = state()
        s.receive(Message("a", "vovo", false, 1, Kind.TEXT, "Tito, oi"), nine)
        s.receive(Message("b", "vovo", false, 2, Kind.TEXT, "Tito, saudade"), nine)
        assertEquals(2, s.messages.size) // the kid can still read them
        assertEquals(0, s.takeQuietSummary(nine)) // still quiet: nothing to announce yet
        assertEquals(2, s.takeQuietSummary(three))
        assertEquals(0, s.takeQuietSummary(three)) // announced once
    }

    @Test fun deleteForEveryoneBlanksTheMessage() {
        val s = state()
        s.receive(Message("a", "vovo", false, 1, Kind.PHOTO, "Tito, olha", media = "f.sealed"), three)
        s.receive(Message("d", "vovo", false, 2, Kind.DELETED, replyTo = "a"), three)
        assertEquals(Kind.DELETED, s.messages.single().kind)
        assertEquals(null, s.messages.single().media)
    }
}
