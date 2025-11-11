package com.po4yka.trailglass.feature.timeline

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test

class TimelineControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val userId = "test_user"

    @Test
    fun `should initialize with empty state`() = runTest {
        val useCase = MockGetTimelineUseCase()
        val controller = TimelineController(useCase, testScope, userId)

        controller.state.test {
            val state = awaitItem()
            state.selectedDate shouldBe null
            state.items shouldBe emptyList()
            state.isLoading shouldBe false
            state.error shouldBe null
        }
    }

    @Test
    fun `loadDay should load timeline items`() = runTest {
        val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val mockItems = listOf(
            GetTimelineForDayUseCase.TimelineItemUI.DayStartUI(
                id = "day_start",
                timestamp = Clock.System.now()
            )
        )
        val useCase = MockGetTimelineUseCase(items = mockItems)
        val controller = TimelineController(useCase, testScope, userId)

        controller.state.test {
            awaitItem() // Initial state

            controller.loadDay(date)

            val loading = awaitItem()
            loading.isLoading shouldBe true
            loading.selectedDate shouldBe date

            val loaded = awaitItem()
            loaded.isLoading shouldBe false
            loaded.items shouldBe mockItems
            loaded.error shouldBe null
        }
    }

    @Test
    fun `loadDay should handle errors`() = runTest {
        val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val useCase = MockGetTimelineUseCase(shouldThrow = true)
        val controller = TimelineController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadDay(date)

            awaitItem() // Loading
            val error = awaitItem()
            error.isLoading shouldBe false
            error.error shouldBe "Test error"
        }
    }

    @Test
    fun `refresh should reload current day`() = runTest {
        val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val mockItems = listOf(
            GetTimelineForDayUseCase.TimelineItemUI.DayStartUI("start", Clock.System.now())
        )
        val useCase = MockGetTimelineUseCase(items = mockItems)
        val controller = TimelineController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadDay(date)
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
    fun `refresh should do nothing if no date selected`() = runTest {
        val useCase = MockGetTimelineUseCase()
        val controller = TimelineController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.refresh()

            expectNoEvents()
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val useCase = MockGetTimelineUseCase(shouldThrow = true)
        val controller = TimelineController(useCase, testScope, userId)

        controller.state.test {
            awaitItem()

            controller.loadDay(date)
            awaitItem() // Loading
            awaitItem().error shouldBe "Test error"

            controller.clearError()

            val cleared = awaitItem()
            cleared.error shouldBe null
        }
    }
}

class MockGetTimelineUseCase(
    private val items: List<GetTimelineForDayUseCase.TimelineItemUI> = emptyList(),
    private val shouldThrow: Boolean = false
) : GetTimelineForDayUseCase(
    placeVisitRepository = com.po4yka.trailglass.data.sync.MockPlaceVisitRepository(),
    routeSegmentRepository = MockRouteSegmentRepository()
) {
    var callCount = 0

    override suspend fun execute(
        date: LocalDate,
        userId: String
    ): List<GetTimelineForDayUseCase.TimelineItemUI> {
        callCount++
        if (shouldThrow) {
            throw Exception("Test error")
        }
        return items
    }
}

class MockRouteSegmentRepository : com.po4yka.trailglass.data.repository.RouteSegmentRepository {
    private val routes = mutableListOf<com.po4yka.trailglass.domain.model.RouteSegment>()

    override suspend fun insertRoute(route: com.po4yka.trailglass.domain.model.RouteSegment) {
        routes.add(route)
    }

    override suspend fun getRouteById(id: String): com.po4yka.trailglass.domain.model.RouteSegment? {
        return routes.find { it.id == id }
    }

    override suspend fun getRouteSegmentsInRange(
        startTime: kotlinx.datetime.Instant,
        endTime: kotlinx.datetime.Instant
    ): List<com.po4yka.trailglass.domain.model.RouteSegment> {
        return routes.filter { it.startTime >= startTime && it.startTime <= endTime }
    }

    override suspend fun deleteRoute(routeId: String) {
        routes.removeAll { it.id == routeId }
    }

    override suspend fun deleteAllRoutes() {
        routes.clear()
    }
}
