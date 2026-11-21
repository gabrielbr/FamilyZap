package app.oie.logic

import app.oie.logic.ParentCommand.Action
import app.oie.logic.ParentCommand.Outcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentCommandTest {
    private val names = listOf("Francisco", "Tito", "Chico")
    private val vovo = Contact("5511987650001@s.whatsapp.net", "Vovó")
    private val ids = listOf(vovo.id)

    @Test fun anyNameAfterTheSlashIsACommand() {
        assertEquals(Action.HELP, ParentCommand.parse("/Tito", names)!!.action)
        assertEquals(Action.HELP, ParentCommand.parse(" /chico ajuda ", names)!!.action)
        assertEquals(Action.PAUSE, ParentCommand.parse("/Francisco parar", names)!!.action)
        assertEquals(Action.PAUSE, ParentCommand.parse("/TITO stop", names)!!.action)
        assertEquals(Action.RESUME, ParentCommand.parse("/Tito voltar", names)!!.action)
        assertEquals(Action.ADD, ParentCommand.parse("/Tito adicionar", names)!!.action)
        assertEquals(Action.REMOVE, ParentCommand.parse("/Tito remover", names)!!.action)
        assertEquals("Chico", ParentCommand.parse("/chico", names)!!.name)
    }

    @Test fun anUnknownWordStillAnswers() {
        val p = ParentCommand.parse("/Tito dormir", names)!!
        assertEquals(Action.UNKNOWN, p.action); assertEquals("dormir", p.word)
    }

    @Test fun notACommand() {
        assertNull(ParentCommand.parse("Tito parar", names))
        assertNull(ParentCommand.parse("/Titozão parar", names))
        assertNull(ParentCommand.parse("/Ana parar", names))
        assertNull(ParentCommand.parse("a /Tito parar", names))
    }

    @Test fun pauseAndResumeKeepTheContact() {
        val renamed = vovo.copy(waName = "Maria Silva", quietException = true)
        val p = ParentCommand.apply(Action.PAUSE, ids, false, vovo, listOf(renamed))
        assertEquals(Outcome.PAUSED, p.outcome); assertEquals(listOf(renamed.copy(paused = true)), p.allowed)
        val r = ParentCommand.apply(Action.RESUME, ids, false, vovo, p.allowed)
        assertEquals(Outcome.RESUMED, r.outcome); assertEquals(listOf(renamed), r.allowed)
    }

    @Test fun addAddsOrResumesOrSaysAlready() {
        val a = ParentCommand.apply(Action.ADD, ids, false, vovo, emptyList())
        assertEquals(Outcome.ADDED, a.outcome); assertEquals(listOf(vovo), a.allowed)
        assertEquals(Outcome.ALREADY_IN, ParentCommand.apply(Action.ADD, ids, false, vovo, listOf(vovo)).outcome)
        val back = ParentCommand.apply(Action.ADD, ids, false, vovo, listOf(vovo.copy(paused = true)))
        assertEquals(Outcome.RESUMED, back.outcome); assertFalse(back.allowed[0].paused)
    }

    @Test fun removeAndNotInList() {
        assertEquals(Outcome.REMOVED, ParentCommand.apply(Action.REMOVE, ids, false, vovo, listOf(vovo)).outcome)
        for (a in listOf(Action.PAUSE, Action.RESUME, Action.REMOVE)) {
            assertEquals(Outcome.NOT_IN_LIST, ParentCommand.apply(a, ids, false, vovo, emptyList()).outcome)
        }
    }

    @Test fun matchesTheChatByEitherAddress() {
        val byLid = ParentCommand.apply(Action.PAUSE, listOf("123@lid", vovo.id), false, vovo, listOf(vovo))
        assertEquals(Outcome.PAUSED, byLid.outcome)
    }

    // v0.43: in the own chat a change needs to say who (a number from "/Tito contatos" or a name); it never acts on the own chat.
    @Test fun theOwnChatAsksWhoAndNeverChangesItself() {
        val r = ParentCommand.apply(Action.PAUSE, listOf("me@s.whatsapp.net"), true, vovo, listOf(vovo))
        assertEquals(Outcome.NEED_WHO, r.outcome); assertEquals(listOf(vovo), r.allowed)
        assertEquals(Outcome.HELP, ParentCommand.apply(Action.HELP, listOf("me@s.whatsapp.net"), true, vovo, listOf(vovo)).outcome)
    }

    @Test fun oldCommandsAreIgnored() {
        assertTrue(ParentCommand.fresh(0, ParentCommand.MAX_AGE_MS))
        assertFalse(ParentCommand.fresh(0, ParentCommand.MAX_AGE_MS + 1))
    }

    // v0.43: commands from the parent's own chat.
    private val tia = Contact("5521998760004@s.whatsapp.net", "Tia Bia", phone = "5521998760004")
    private val primos = Contact("1203@g.us", "Primos", isGroup = true)
    private val line = Contact("me@s.whatsapp.net", "Ana", parentLine = true)
    private val list = listOf(line, vovo, tia, primos)

    @Test fun theNewWordsParse() {
        assertEquals(Action.LIST, ParentCommand.parse("/Tito contatos", names)!!.action)
        assertEquals(Action.LIST, ParentCommand.parse("/Tito list", names)!!.action)
        ParentCommand.parse("/Tito remover 2", names)!!.let { assertEquals(Action.REMOVE, it.action); assertEquals("2", it.arg) }
        ParentCommand.parse("/Tito só dele Tia Bia", names)!!.let { assertEquals(Action.KIDS_OWN, it.action); assertEquals("Tia Bia", it.arg) }
        ParentCommand.parse("/Tito so 3", names)!!.let { assertEquals(Action.KIDS_OWN, it.action); assertEquals("3", it.arg) }
        assertEquals(Action.NOT_KIDS_OWN, ParentCommand.parse("/Tito normal 3", names)!!.action)
        assertEquals(Action.QUIET_ON, ParentCommand.parse("/Tito silêncio", names)!!.action)
        assertEquals(Action.QUIET_OFF, ParentCommand.parse("/Tito liberar", names)!!.action)
    }

    @Test fun numbersCountTheKidsContactsNotTheParentLine() {
        assertEquals(listOf(vovo, tia, primos), ParentCommand.numbered(list))
        assertEquals(tia, ParentCommand.target("2", list))
        assertEquals(tia, ParentCommand.target("tia bia", list))
        assertEquals(primos, ParentCommand.target("prim", list))
        assertNull(ParentCommand.target("9", list))
        assertNull(ParentCommand.target("Ana", list)) // the line to the parent isn't a target
    }

    @Test fun inTheOwnChatTheTargetComesFromTheArgument() {
        val r = ParentCommand.apply(Action.PAUSE, listOf("me@s.whatsapp.net"), ownChat = true, candidate = vovo, allowed = list, arg = "1")
        assertEquals(Outcome.PAUSED, r.outcome); assertTrue(r.allowed.first { it.id == vovo.id }.paused)
        assertEquals(Outcome.NEED_WHO, ParentCommand.apply(Action.REMOVE, emptyList(), true, vovo, list, "").outcome)
        assertEquals(Outcome.NO_SUCH, ParentCommand.apply(Action.REMOVE, emptyList(), true, vovo, list, "7").outcome)
        val gone = ParentCommand.apply(Action.REMOVE, emptyList(), true, vovo, list, "Primos")
        assertEquals(Outcome.REMOVED, gone.outcome); assertFalse(gone.allowed.contains(primos))
    }

    @Test fun kidsOwnOnAndOff() {
        val on = ParentCommand.apply(Action.KIDS_OWN, emptyList(), true, vovo, list, "3")
        assertEquals(Outcome.KIDS_OWN_ON, on.outcome); assertTrue(on.allowed.first { it.id == primos.id }.showWholeGroup)
        val off = ParentCommand.apply(Action.NOT_KIDS_OWN, emptyList(), true, vovo, on.allowed, "3")
        assertEquals(Outcome.KIDS_OWN_OFF, off.outcome); assertFalse(off.allowed.first { it.id == primos.id }.showWholeGroup)
    }

    @Test fun listAndQuietWorkAnywhereAndAddInTheOwnChatAsksForTheCard() {
        assertEquals(Outcome.LIST, ParentCommand.apply(Action.LIST, ids, false, vovo, list).outcome)
        assertEquals(Outcome.QUIET_ON, ParentCommand.apply(Action.QUIET_ON, emptyList(), true, vovo, list).outcome)
        assertEquals(Outcome.QUIET_OFF, ParentCommand.apply(Action.QUIET_OFF, emptyList(), true, vovo, list).outcome)
        assertEquals(Outcome.SEND_CARD, ParentCommand.apply(Action.ADD, emptyList(), true, vovo, list).outcome)
    }

    @Test fun aSharedCardAddsThePerson() {
        val r = ParentCommand.addCard(ParentCommand.Card("Lucas", "5511912345678"), list)
        assertEquals(Outcome.ADDED, r.outcome); assertEquals("5511912345678@s.whatsapp.net", r.contact!!.id)
        assertEquals(Outcome.ALREADY_IN, ParentCommand.addCard(ParentCommand.Card("Bia", "5521998760004"), list).outcome)
        assertEquals(Outcome.NO_WHATSAPP, ParentCommand.addCard(ParentCommand.Card("Fixo", ""), list).outcome)
        val paused = list.map { if (it.id == tia.id) it.copy(paused = true) else it }
        assertEquals(Outcome.RESUMED, ParentCommand.addCard(ParentCommand.Card("Bia", "5521998760004"), paused).outcome)
    }
}
