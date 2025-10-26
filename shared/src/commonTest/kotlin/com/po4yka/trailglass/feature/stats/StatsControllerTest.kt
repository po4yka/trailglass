package com.po4yka.trailglass.feature.stats

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StatsControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val userId = "test_user"

    @Test
    fun `should initialize with empty state`() = runTest {
        val useCase = MockGetStatsUseCase()
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            val state = awaitItem()
            state.period shouldBe null
            state.stats shouldBe null
            state.isLoading shouldBe false
            state.error shouldBe null
        }
    }

    @Test
    fun `loadPeriod should load stats`() = runTest {
        val period = GetStatsUseCase.Period.Year(2024)
        val mockStats = createMockStats(period)
        val useCase = MockGetStatsUseCase(stats = mockStats)
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadPeriod(period)

            val loading = awaitItem()
            loading.isLoading shouldBe true
            loading.period shouldBe period

            val loaded = awaitItem()
            loaded.isLoading shouldBe false
            loaded.stats shouldBe mockStats
            loaded.error shouldBe null
        }
    }

    @Test
    fun `loadPeriod should handle errors`() = runTest {
        val period = GetStatsUseCase.Period.Year(2024)
        val useCase = MockGetStatsUseCase(shouldThrow = true)
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadPeriod(period)

            awaitItem() // Loading
            val error = awaitItem()
            error.isLoading shouldBe false
            error.error shouldBe "Test error"
        }
    }

    @Test
    fun `loadPeriod should load stats for different period types`() = runTest {
        val monthPeriod = GetStatsUseCase.Period.Month(2024, 5)
        val mockStats = createMockStats(monthPeriod)
        val useCase = MockGetStatsUseCase(stats = mockStats)
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadPeriod(monthPeriod)

            awaitItem() // Loading
            val loaded = awaitItem()
            loaded.period shouldBe monthPeriod
            loaded.stats?.period shouldBe monthPeriod
        }
    }

    @Test
    fun `refresh should reload current period`() = runTest {
        val period = GetStatsUseCase.Period.Year(2024)
        val mockStats = createMockStats(period)
        val useCase = MockGetStatsUseCase(stats = mockStats)
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadPeriod(period)
            awaitItem() // Loading
            awaitItem() // Loaded

            useCase.callCount = 0 // Reset counter
            controller.refresh()

            awaitItem() // Loading
            awaitItem() // Loaded
            useCase.callCount shouldBe 1
        }
    }

    @Test
    fun `refresh should do nothing if no period selected`() = runTest {
        val useCase = MockGetStatsUseCase()
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.refresh()

            expectNoEvents()
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val period = GetStatsUseCase.Period.Year(2024)
        val useCase = MockGetStatsUseCase(shouldThrow = true)
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadPeriod(period)
            awaitItem() // Loading
            awaitItem().error shouldBe "Test error"

            controller.clearError()

            val cleared = awaitItem()
            cleared.error shouldBe null
        }
    }

    @Test
    fun `should handle stats with top countries and cities`() = runTest {
        val period = GetStatsUseCase.Period.Year(2024)
        val stats = GetStatsUseCase.Stats(
            period = period,
            countriesVisited = setOf("US", "UK", "FR"),
            citiesVisited = setOf("New York", "London", "Paris"),
            totalTrips = 5,
            totalDays = 30,
            totalVisits = 25,
            topCountries = listOf("US" to 10, "UK" to 7, "FR" to 5),
            topCities = listOf("New York" to 8, "London" to 6, "Paris" to 4)
        )
        val useCase = MockGetStatsUseCase(stats = stats)
        val controller = StatsController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadPeriod(period)

            awaitItem() // Loading
            val loaded = awaitItem()
            loaded.stats?.topCountries shouldBe listOf("US" to 10, "UK" to 7, "FR" to 5)
            loaded.stats?.topCities shouldBe listOf("New York" to 8, "London" to 6, "Paris" to 4)
            loaded.stats?.totalTrips shouldBe 5
        }
    }

    private fun createMockStats(period: GetStatsUseCase.Period): GetStatsUseCase.Stats {
        return GetStatsUseCase.Stats(
            period = period,
            countriesVisited = setOf("US", "UK"),
            citiesVisited = setOf("New York", "London"),
            totalTrips = 3,
            totalDays = 15,
            totalVisits = 10,
            topCountries = listOf("US" to 5, "UK" to 5),
            topCities = listOf("New York" to 4, "London" to 6)
        )
    }
}

class MockGetStatsUseCase(
    private val stats: GetStatsUseCase.Stats? = null,
    private val shouldThrow: Boolean = false
) : GetStatsUseCase(
    tripRepository = com.po4yka.trailglass.data.sync.MockTripRepository(),
    placeVisitRepository = com.po4yka.trailglass.data.sync.MockPlaceVisitRepository()
) {
    var callCount = 0

    override suspend fun execute(period: Period, userId: String): Stats {
        callCount++
        if (shouldThrow) {
            throw Exception("Test error")
        }
        return stats ?: Stats(
            period = period,
            countriesVisited = emptySet(),
            citiesVisited = emptySet(),
            totalTrips = 0,
            totalDays = 0,
            totalVisits = 0,
            topCountries = emptyList(),
            topCities = emptyList()
        )
    }
}
