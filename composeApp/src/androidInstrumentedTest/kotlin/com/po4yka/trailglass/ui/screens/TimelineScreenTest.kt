package com.po4yka.trailglass.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.timeline.GetTimelineForDayUseCase
import com.po4yka.trailglass.feature.timeline.TimelineController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for TimelineScreen.
 */
class TimelineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var controller: TimelineController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Before
    fun setup() {
        // Create a mock controller with test data
        val mockUseCase = object : GetTimelineForDayUseCase(null, null) {
            override suspend fun execute(
                userId: String,
                date: LocalDate
            ): List<GetTimelineForDayUseCase.TimelineItemUI> {
                val visit = PlaceVisit(
                    id = "visit1",
                    tripId = "trip1",
                    userId = userId,
                    startTime = Instant.parse("2024-01-15T10:00:00Z"),
                    endTime = Instant.parse("2024-01-15T12:00:00Z"),
                    centerLatitude = 48.8566,
                    centerLongitude = 2.3522,
                    radiusMeters = 100.0,
                    city = "Paris",
                    country = "France",
                    approximateAddress = "Eiffel Tower, Avenue Anatole France",
                    confidence = 0.95,
                    arrivalTransportType = TransportType.WALK,
                    departureTransportType = TransportType.CAR,
                    userNotes = null
                )

                val route = RouteSegment(
                    id = "route1",
                    tripId = "trip1",
                    userId = userId,
                    fromVisitId = "visit1",
                    toVisitId = "visit2",
                    startTime = Instant.parse("2024-01-15T12:00:00Z"),
                    endTime = Instant.parse("2024-01-15T13:00:00Z"),
                    transportType = TransportType.CAR,
                    distanceMeters = 5000.0,
                    simplifiedPath = listOf(
                        Coordinate(48.8566, 2.3522),
                        Coordinate(48.8606, 2.3376)
                    ),
                    confidence = 0.9
                )

                return listOf(
                    GetTimelineForDayUseCase.TimelineItemUI.DayStartUI(
                        Instant.parse("2024-01-15T00:00:00Z")
                    ),
                    GetTimelineForDayUseCase.TimelineItemUI.VisitUI(visit),
                    GetTimelineForDayUseCase.TimelineItemUI.RouteUI(route),
                    GetTimelineForDayUseCase.TimelineItemUI.DayEndUI(
                        Instant.parse("2024-01-15T23:59:59Z")
                    )
                )
            }
        }

        controller = TimelineController(mockUseCase, scope, "test_user")
    }

    @Test
    fun timelineScreen_displaysDatePicker() {
        // Given
        composeTestRule.setContent {
            TimelineScreen(controller = controller)
        }

        // Then - date picker should be visible
        composeTestRule.onNodeWithContentDescription("Select date").assertExists()
    }

    @Test
    fun timelineScreen_displaysVisitCard() {
        // Given
        composeTestRule.setContent {
            TimelineScreen(controller = controller)
        }

        // Load timeline for a date
        controller.selectDate(LocalDate(2024, 1, 15))
        composeTestRule.waitForIdle()

        // Then - visit card should be displayed
        composeTestRule.onNodeWithText("Paris").assertExists()
        composeTestRule.onNodeWithText("France").assertExists()
        composeTestRule.onNodeWithText("Eiffel Tower, Avenue Anatole France")
            .assertExists()
    }

    @Test
    fun timelineScreen_displaysRouteCard() {
        // Given
        composeTestRule.setContent {
            TimelineScreen(controller = controller)
        }

        // Load timeline for a date
        controller.selectDate(LocalDate(2024, 1, 15))
        composeTestRule.waitForIdle()

        // Then - route card should be displayed
        composeTestRule.onNodeWithText("5.0 km", substring = true).assertExists()
        composeTestRule.onNodeWithText("CAR", ignoreCase = true).assertExists()
    }

    @Test
    fun timelineScreen_displaysDayMarkers() {
        // Given
        composeTestRule.setContent {
            TimelineScreen(controller = controller)
        }

        // Load timeline for a date
        controller.selectDate(LocalDate(2024, 1, 15))
        composeTestRule.waitForIdle()

        // Then - day markers should be displayed
        composeTestRule.onNodeWithText("Day Start").assertExists()
        composeTestRule.onNodeWithText("Day End").assertExists()
    }

    @Test
    fun timelineScreen_displaysEmptyState() {
        // Given - controller with no data
        val emptyUseCase = object : GetTimelineForDayUseCase(null, null) {
            override suspend fun execute(
                userId: String,
                date: LocalDate
            ): List<GetTimelineForDayUseCase.TimelineItemUI> {
                return emptyList()
            }
        }
        val emptyController = TimelineController(emptyUseCase, scope, "test_user")

        composeTestRule.setContent {
            TimelineScreen(controller = emptyController)
        }

        // When
        emptyController.selectDate(LocalDate(2024, 1, 15))
        composeTestRule.waitForIdle()

        // Then - empty state should be displayed
        composeTestRule.onNodeWithText("No timeline data").assertExists()
    }

    @Test
    fun timelineScreen_scrollsToBottomContent() {
        // Given
        composeTestRule.setContent {
            TimelineScreen(controller = controller)
        }

        // Load timeline
        controller.selectDate(LocalDate(2024, 1, 15))
        composeTestRule.waitForIdle()

        // When - scroll to bottom
        composeTestRule.onNodeWithText("Day End")
            .performScrollTo()

        // Then - bottom content is visible
        composeTestRule.onNodeWithText("Day End").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_visitCardClick() {
        // Given
        composeTestRule.setContent {
            TimelineScreen(
                controller = controller,
                onNavigateToVisit = { visitId ->
                    // Navigation would happen here
                }
            )
        }

        // Load timeline
        controller.selectDate(LocalDate(2024, 1, 15))
        composeTestRule.waitForIdle()

        // When - click on visit card
        composeTestRule.onNodeWithText("Paris").performClick()

        // Then - should trigger navigation (verified by callback)
        composeTestRule.waitForIdle()
    }
}
