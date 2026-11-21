package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeOrderTest {
    private val vo = Contact("vo", "Vovó"); private val tia = Contact("tia", "Tia Bia")
    private val lu = Contact("lu", "Lucas"); private val pai = Contact("pai", "Papai")
    private val parent = listOf(vo, tia, lu, pai)
    private fun ids(l: List<Contact>) = l.map { it.id }

    @Test fun withNothingSetItsTheParentsOrder() = assertEquals(listOf("vo", "tia", "lu", "pai"), ids(HomeOrder.order(parent, emptySet(), emptyList())))

    @Test fun favoritesFirstThenTheKidsOrder() =
        assertEquals(listOf("lu", "pai", "tia", "vo"), ids(HomeOrder.order(parent, setOf("lu"), listOf("pai", "lu", "tia", "vo"))))

    @Test fun someoneNewGoesAfterThePlacedOnesInTheParentsOrder() =
        assertEquals(listOf("lu", "vo", "tia", "pai"), ids(HomeOrder.order(parent, emptySet(), listOf("lu", "vo"))))

    @Test fun movingUpAndDown() {
        val order = listOf("vo", "tia", "lu", "pai")
        assertEquals(listOf("vo", "lu", "tia", "pai"), HomeOrder.move(parent, emptySet(), order, "lu", -1))
        assertEquals(listOf("vo", "tia", "pai", "lu"), HomeOrder.move(parent, emptySet(), order, "lu", +1))
        assertEquals(order, HomeOrder.move(parent, emptySet(), order, "vo", -1)) // already at the top
        assertEquals(listOf("tia", "lu", "vo", "pai"), HomeOrder.move(parent, emptySet(), order, "vo", +2)) // a drag of two places
    }

    @Test fun aFavoriteStaysAmongTheFavorites() {
        val order = listOf("lu", "vo", "tia", "pai")
        val favs = setOf("lu", "vo")
        assertEquals(listOf("vo", "lu", "tia", "pai"), HomeOrder.move(parent, favs, order, "lu", +1))
        assertEquals(order, HomeOrder.move(parent, favs, order, "vo", +1)) // can't drop below the favorites
        assertEquals(order, HomeOrder.move(parent, favs, order, "tia", -1)) // can't climb into them
    }

    @Test fun aNewFavoriteGoesToTheEndOfTheFavorites() {
        val (favs, order) = HomeOrder.toggleFavorite(parent, setOf("vo"), listOf("vo", "tia", "lu", "pai"), "pai")
        assertEquals(setOf("vo", "pai"), favs)
        assertEquals(listOf("vo", "pai", "tia", "lu"), ids(HomeOrder.order(parent, favs, order)))
    }

    @Test fun anUnfavoriteGoesToTheEnd() {
        val (favs, order) = HomeOrder.toggleFavorite(parent, setOf("vo", "pai"), listOf("vo", "pai", "tia", "lu"), "vo")
        assertEquals(setOf("pai"), favs)
        assertEquals(listOf("pai", "tia", "lu", "vo"), ids(HomeOrder.order(parent, favs, order)))
    }

    @Test fun newsComeFirstNewestFirstAndTheRestKeepTheirPlace() {
        val ordered = HomeOrder.order(parent, setOf("vo"), listOf("vo", "tia", "lu", "pai"))
        assertEquals(listOf("pai", "lu", "vo", "tia"), ids(HomeOrder.withNews(ordered, mapOf("lu" to 100L, "pai" to 200L))))
        assertEquals(ids(ordered), ids(HomeOrder.withNews(ordered, emptyMap()))) // all read: the kid's order again
    }

    // v0.48: people and groups are two sections.
    private val primos = Contact("primos", "Primos", isGroup = true)
    private val escola = Contact("escola", "Escola", isGroup = true)
    private val linha = Contact("linha", "Mãe", isGroup = true, parentLine = true)
    private val mixed = listOf(primos, vo, escola, linha, lu)

    @Test fun peopleComeBeforeGroupsAndTheParentsLineIsAPerson() =
        assertEquals(listOf("vo", "linha", "lu", "primos", "escola"), ids(HomeOrder.order(mixed, emptySet(), emptyList())))

    @Test fun aGroupFavoriteLeadsTheGroupsNotTheHome() =
        assertEquals(listOf("vo", "linha", "lu", "escola", "primos"), ids(HomeOrder.order(mixed, setOf("escola"), emptyList())))

    @Test fun aMoveNeverCrossesFromPeopleToGroups() {
        val order = ids(HomeOrder.order(mixed, emptySet(), emptyList()))
        assertEquals(order, HomeOrder.move(mixed, emptySet(), emptyList(), "lu", +1)) // last person: stays
        assertEquals(order, HomeOrder.move(mixed, emptySet(), emptyList(), "primos", -1)) // first group: stays
        assertEquals(listOf("vo", "linha", "lu", "escola", "primos"), HomeOrder.move(mixed, emptySet(), emptyList(), "primos", +1))
    }

    @Test fun aNewGroupFavoriteGoesToTheTopOfTheGroups() {
        val (favs, order) = HomeOrder.toggleFavorite(mixed, emptySet(), emptyList(), "escola")
        assertEquals(listOf("vo", "linha", "lu", "escola", "primos"), ids(HomeOrder.order(mixed, favs, order)))
    }

    @Test fun newsComeFirstWithinEachSection() {
        val ordered = HomeOrder.order(mixed, emptySet(), emptyList())
        assertEquals(listOf("lu", "vo", "linha", "escola", "primos"), ids(HomeOrder.withNews(ordered, mapOf("lu" to 1L, "escola" to 2L))))
    }
}
