package com.po4yka.trailglass.feature.map

import app.cash.turbine.test
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.domain.service.LocationService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test

class MapControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val userId = "test_user"

    @Test
    fun `should initialize with empty state`() = runTest {
        val controller = createController()

        controller.state.test {
            val state = awaitItem()
            state.mapData shouldBe MapDisplayData()
            state.cameraMove shouldBe null
            state.selectedMarker shouldBe null
            state.selectedRoute shouldBe null
            state.isFollowModeEnabled shouldBe false
            state.isLoading shouldBe false
            state.error shouldBe null
        }
    }

    @Test
    fun `loadMapData should load markers and routes`() = runTest {
        val mapData = createMockMapData()
        val useCase = MockGetMapDataUseCase(mapData)
        val controller = createController(getMapDataUseCase = useCase)

        controller.state.test {
            awaitItem()

            val startTime = Clock.System.now()
            val endTime = Clock.System.now()
            controller.loadMapData(startTime, endTime)

            val loading = awaitItem()
            loading.isLoading shouldBe true

            val loaded = awaitItem()
            loaded.isLoading shouldBe false
            loaded.mapData shouldBe mapData
            loaded.cameraMove.shouldBeInstanceOf<CameraMove.Ease>()
        }
    }

    @Test
    fun `loadMapData should handle errors`() = runTest {
        val useCase = MockGetMapDataUseCase(shouldThrow = true)
        val controller = createController(getMapDataUseCase = useCase)

        controller.state.test {
            awaitItem()

            val startTime = Clock.System.now()
            val endTime = Clock.System.now()
            controller.loadMapData(startTime, endTime)

            awaitItem() // Loading
            val error = awaitItem()
            error.isLoading shouldBe false
            error.error shouldBe "Test error"
        }
    }

    @Test
    fun `selectMarker should update selected marker`() = runTest {
        val controller = createController()
        val marker = createMockMarker("marker1")

        controller.state.test {
            awaitItem()

            controller.selectMarker(marker)

            val selected = awaitItem()
            selected.selectedMarker shouldBe marker
        }
    }

    @Test
    fun `deselectMarker should clear selection`() = runTest {
        val controller = createController()
        val marker = createMockMarker("marker1")

        controller.state.test {
            awaitItem()

            controller.selectMarker(marker)
            awaitItem()

            controller.deselectMarker()

            val deselected = awaitItem()
            deselected.selectedMarker shouldBe null
        }
    }

    @Test
    fun `selectRoute should update selected route`() = runTest {
        val controller = createController()
        val route = createMockRoute("route1")

        controller.state.test {
            awaitItem()

            controller.selectRoute(route)

            val selected = awaitItem()
            selected.selectedRoute shouldBe route
        }
    }

    @Test
    fun `moveCameraTo should apply camera move`() = runTest {
        val controller = createController()
        val coordinate = Coordinate(37.7749, -122.4194)

        controller.state.test {
            awaitItem()

            controller.moveCameraTo(coordinate, zoom = 15f, animated = true)

            val moved = awaitItem()
            moved.cameraMove.shouldBeInstanceOf<CameraMove.Ease>()
            (moved.cameraMove as CameraMove.Ease).position.target shouldBe coordinate
        }
    }

    @Test
    fun `moveCameraTo with animation disabled should use instant move`() = runTest {
        val controller = createController()
        val coordinate = Coordinate(37.7749, -122.4194)

        controller.state.test {
            awaitItem()

            controller.moveCameraTo(coordinate, animated = false)

            val moved = awaitItem()
            moved.cameraMove.shouldBeInstanceOf<CameraMove.Instant>()
        }
    }

    @Test
    fun `fitToData should fit camera to map region`() = runTest {
        val mapData = createMockMapData()
        val useCase = MockGetMapDataUseCase(mapData)
        val controller = createController(getMapDataUseCase = useCase)

        controller.state.test {
            awaitItem()

            // Load map data first
            controller.loadMapData(Clock.System.now(), Clock.System.now())
            awaitItem() // Loading
            awaitItem() // Loaded with camera move

            controller.fitToData(animated = true)

            val fitted = awaitItem()
            fitted.cameraMove.shouldBeInstanceOf<CameraMove.Ease>()
        }
    }

    @Test
    fun `toggleFollowMode should enable follow mode with permission`() = runTest {
        val locationService = MockLocationService(hasPermission = true)
        val controller = createController(locationService = locationService)

        controller.state.test {
            awaitItem()

            controller.toggleFollowMode()

            val enabled = awaitItem()
            enabled.isFollowModeEnabled shouldBe true
            enabled.cameraMove.shouldBeInstanceOf<CameraMove.Ease>()
        }
    }

    @Test
    fun `toggleFollowMode should fail without permission`() = runTest {
        val locationService = MockLocationService(hasPermission = false)
        val controller = createController(locationService = locationService)

        controller.state.test {
            awaitItem()

            controller.toggleFollowMode()

            val error = awaitItem()
            error.isFollowModeEnabled shouldBe false
            error.error shouldBe "Location permission required for follow mode"
        }
    }

    @Test
    fun `toggleFollowMode twice should disable follow mode`() = runTest {
        val locationService = MockLocationService(hasPermission = true)
        val controller = createController(locationService = locationService)

        controller.state.test {
            awaitItem()

            // Enable
            controller.toggleFollowMode()
            awaitItem()

            // Disable
            controller.toggleFollowMode()

            val disabled = awaitItem()
            disabled.isFollowModeEnabled shouldBe false
        }
    }

    @Test
    fun `MapEvent MarkerTapped should select marker`() = runTest {
        val mapData = createMockMapData()
        val useCase = MockGetMapDataUseCase(mapData)
        val controller = createController(getMapDataUseCase = useCase)

        controller.state.test {
            awaitItem()

            // Load map data
            controller.loadMapData(Clock.System.now(), Clock.System.now())
            awaitItem() // Loading
            awaitItem() // Loaded

            // Send marker tapped event
            controller.send(MapEvent.MarkerTapped("marker1"))

            val selected = awaitItem()
            selected.selectedMarker?.id shouldBe "marker1"
        }
    }

    @Test
    fun `MapEvent MapTapped should deselect markers and routes`() = runTest {
        val controller = createController()

        controller.state.test {
            awaitItem()

            controller.selectMarker(createMockMarker("marker1"))
            awaitItem()

            controller.send(MapEvent.MapTapped)

            val deselected = awaitItem()
            deselected.selectedMarker shouldBe null
            deselected.selectedRoute shouldBe null
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val useCase = MockGetMapDataUseCase(shouldThrow = true)
        val controller = createController(getMapDataUseCase = useCase)

        controller.state.test {
            awaitItem()

            controller.loadMapData(Clock.System.now(), Clock.System.now())
            awaitItem() // Loading
            awaitItem().error shouldBe "Test error"

            controller.clearError()

            val cleared = awaitItem()
            cleared.error shouldBe null
        }
    }

    private fun createController(
        getMapDataUseCase: GetMapDataUseCase = MockGetMapDataUseCase(),
        locationService: LocationService = MockLocationService()
    ): MapController {
        return MapController(
            getMapDataUseCase = getMapDataUseCase,
            locationService = locationService,
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )
    }

    private fun createMockMapData(): MapDisplayData {
        return MapDisplayData(
            markers = listOf(createMockMarker("marker1")),
            routes = listOf(createMockRoute("route1")),
            region = MapRegion(
                center = Coordinate(37.7749, -122.4194),
                latitudeDelta = 0.1,
                longitudeDelta = 0.1
            )
        )
    }

    private fun createMockMarker(id: String): MapMarker {
        return MapMarker(
            id = id,
            coordinate = Coordinate(37.7749, -122.4194),
            title = "Test Marker",
            snippet = "Test snippet",
            placeVisitId = "visit_$id"
        )
    }

    private fun createMockRoute(id: String): MapRoute {
        return MapRoute(
            id = id,
            coordinates = listOf(
                Coordinate(37.7749, -122.4194),
                Coordinate(37.7750, -122.4195)
            ),
            transportType = TransportType.WALKING,
            color = 0xFF0000FF.toInt(),
            routeSegmentId = "segment_$id"
        )
    }
}

class MockGetMapDataUseCase(
    private val mapData: MapDisplayData = MapDisplayData(),
    private val shouldThrow: Boolean = false
) : GetMapDataUseCase(
    placeVisitRepository = com.po4yka.trailglass.data.sync.MockPlaceVisitRepository(),
    routeSegmentRepository = com.po4yka.trailglass.feature.timeline.MockRouteSegmentRepository()
) {
    override suspend fun execute(
        userId: String,
        startTime: kotlinx.datetime.Instant,
        endTime: kotlinx.datetime.Instant
    ): MapDisplayData {
        if (shouldThrow) throw Exception("Test error")
        return mapData
    }
}

class MockLocationService(
    private val hasPermission: Boolean = false,
    private val lastKnownLocation: Coordinate? = Coordinate(37.7749, -122.4194)
) : LocationService {
    private val _locationUpdates = MutableSharedFlow<Coordinate>()
    override val locationUpdates: Flow<Coordinate> = _locationUpdates

    override suspend fun hasLocationPermission(): Boolean = hasPermission

    override suspend fun requestLocationPermission(): Boolean = hasPermission

    override suspend fun getLastKnownLocation(): Coordinate? = lastKnownLocation

    override suspend fun getCurrentLocation(): Coordinate? = lastKnownLocation

    override suspend fun startLocationUpdates() {
        // No-op for testing
    }

    override suspend fun stopLocationUpdates() {
        // No-op for testing
    }
}
