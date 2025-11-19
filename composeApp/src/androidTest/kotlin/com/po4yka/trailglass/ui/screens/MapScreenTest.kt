package com.po4yka.trailglass.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.MapDisplayData
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.domain.model.MapRegion
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.map.GetMapDataUseCase
import com.po4yka.trailglass.feature.map.MapController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for MapScreen.
 */
class MapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var controller: MapController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Before
    fun setup() {
        // Create a mock controller with test data
        val mockUseCase = object : GetMapDataUseCase(null, null) {
            override suspend fun execute(
                userId: String,
                startTime: Instant,
                endTime: Instant
            ): MapDisplayData {
                val marker1 = MapMarker(
                    id = "marker1",
                    coordinate = Coordinate(48.8566, 2.3522),
                    title = "Paris",
                    snippet = "Eiffel Tower, Avenue Anatole France",
                    placeVisitId = "visit1"
                )

                val marker2 = MapMarker(
                    id = "marker2",
                    coordinate = Coordinate(51.5074, -0.1278),
                    title = "London",
                    snippet = "Big Ben, Westminster",
                    placeVisitId = "visit2"
                )

                val route1 = MapRoute(
                    id = "route1",
                    coordinates = listOf(
                        Coordinate(48.8566, 2.3522),
                        Coordinate(51.5074, -0.1278)
                    ),
                    transportType = TransportType.TRAIN,
                    color = 0xFF9C27B0.toInt(), // Purple
                    routeSegmentId = "route1"
                )

                val region = MapRegion(
                    center = Coordinate(50.0, 1.0),
                    latitudeDelta = 5.0,
                    longitudeDelta = 3.0
                )

                return MapDisplayData(
                    markers = listOf(marker1, marker2),
                    routes = listOf(route1),
                    region = region
                )
            }
        }

        controller = MapController(mockUseCase, scope, "test_user")
    }

    @Test
    fun mapScreen_displaysMap() {
        // Given
        composeTestRule.setContent {
            MapScreen(controller = controller)
        }

        // Wait for map to load
        composeTestRule.waitForIdle()

        // Then - map view should be present
        // Note: Testing actual map rendering requires Espresso or platform-specific testing
        // Here we verify the screen is composed without errors
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun mapScreen_displaysMarkerInfoCard() {
        // Given
        composeTestRule.setContent {
            MapScreen(controller = controller)
        }

        // Wait for data to load
        composeTestRule.waitForIdle()

        // When - select a marker
        val marker = MapMarker(
            id = "marker1",
            coordinate = Coordinate(48.8566, 2.3522),
            title = "Paris",
            snippet = "Eiffel Tower, Avenue Anatole France",
            placeVisitId = "visit1"
        )
        controller.selectMarker(marker)
        composeTestRule.waitForIdle()

        // Then - marker info card should be displayed
        composeTestRule.onNodeWithText("Paris").assertExists()
        composeTestRule.onNodeWithText("Eiffel Tower, Avenue Anatole France")
            .assertExists()
        composeTestRule.onNodeWithText("Details").assertExists()
        composeTestRule.onNodeWithText("Photos").assertExists()
    }

    @Test
    fun mapScreen_closesMarkerInfoCard() {
        // Given
        composeTestRule.setContent {
            MapScreen(controller = controller)
        }

        // Select a marker
        val marker = MapMarker(
            id = "marker1",
            coordinate = Coordinate(48.8566, 2.3522),
            title = "Paris",
            snippet = "Eiffel Tower",
            placeVisitId = "visit1"
        )
        controller.selectMarker(marker)
        composeTestRule.waitForIdle()

        // When - click close button
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        composeTestRule.waitForIdle()

        // Then - marker info card should be hidden
        composeTestRule.onNodeWithText("Paris").assertDoesNotExist()
    }

    @Test
    fun mapScreen_detailsButtonExists() {
        // Given
        composeTestRule.setContent {
            MapScreen(controller = controller)
        }

        // Select a marker
        val marker = MapMarker(
            id = "marker1",
            coordinate = Coordinate(48.8566, 2.3522),
            title = "Paris",
            snippet = "Eiffel Tower",
            placeVisitId = "visit1"
        )
        controller.selectMarker(marker)
        composeTestRule.waitForIdle()

        // Then - Details button should be clickable
        composeTestRule.onNodeWithText("Details")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun mapScreen_photosButtonExists() {
        // Given
        composeTestRule.setContent {
            MapScreen(controller = controller)
        }

        // Select a marker
        val marker = MapMarker(
            id = "marker1",
            coordinate = Coordinate(48.8566, 2.3522),
            title = "Paris",
            snippet = "Eiffel Tower",
            placeVisitId = "visit1"
        )
        controller.selectMarker(marker)
        composeTestRule.waitForIdle()

        // Then - Photos button should be clickable
        composeTestRule.onNodeWithText("Photos")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun mapScreen_displaysEmptyState() {
        // Given - controller with no data
        val emptyUseCase = object : GetMapDataUseCase(null, null) {
            override suspend fun execute(
                userId: String,
                startTime: Instant,
                endTime: Instant
            ): MapDisplayData {
                return MapDisplayData(
                    markers = emptyList(),
                    routes = emptyList(),
                    region = null
                )
            }
        }
        val emptyController = MapController(emptyUseCase, scope, "test_user")

        composeTestRule.setContent {
            MapScreen(controller = emptyController)
        }

        // Wait for map to load
        composeTestRule.waitForIdle()

        // Then - map should still render (just empty)
        composeTestRule.onRoot().assertExists()
    }
}
