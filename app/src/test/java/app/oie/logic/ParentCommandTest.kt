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

    @Test fun theOwnChatOnlyGivesHelp() {
        assertEquals(Outcome.OWN_CHAT, ParentCommand.apply(Action.PAUSE, listOf("me@s.whatsapp.net"), true, vovo, listOf(vovo)).outcome)
        assertEquals(Outcome.HELP, ParentCommand.apply(Action.HELP, listOf("me@s.whatsapp.net"), true, vovo, listOf(vovo)).outcome)
    }

    @Test fun oldCommandsAreIgnored() {
        assertTrue(ParentCommand.fresh(0, ParentCommand.MAX_AGE_MS))
        assertFalse(ParentCommand.fresh(0, ParentCommand.MAX_AGE_MS + 1))
    }
}
