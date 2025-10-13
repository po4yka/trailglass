package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.data.repository.PlaceVisitRepositoryImpl
import com.po4yka.trailglass.data.repository.RouteSegmentRepositoryImpl
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TransportType
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

/**
 * Tests for GetMapDataUseCase.
 */
class GetMapDataUseCaseTest {

    private val database = TestDatabaseHelper.createTestDatabase()
    private val visitRepository = PlaceVisitRepositoryImpl(database)
    private val routeRepository = RouteSegmentRepositoryImpl(database)
    private lateinit var useCase: GetMapDataUseCase
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        useCase = GetMapDataUseCase(visitRepository, routeRepository)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun testExecuteWithNoData() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        // When
        val result = useCase.execute(userId, start, end)

        // Then
        assertTrue(result.markers.isEmpty())
        assertTrue(result.routes.isEmpty())
        assertNull(result.region)
    }

    @Test
    fun testExecuteWithVisitsOnly() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val visit1 = createTestVisit(
            id = "visit1",
            city = "Paris",
            latitude = 48.8566,
            longitude = 2.3522
        )
        val visit2 = createTestVisit(
            id = "visit2",
            city = "London",
            latitude = 51.5074,
            longitude = -0.1278
        )

        visitRepository.insertPlaceVisit(visit1)
        visitRepository.insertPlaceVisit(visit2)

        // When
        val result = useCase.execute(userId, start, end)

        // Then
        assertEquals(2, result.markers.size)
        assertTrue(result.routes.isEmpty())

        val parisMarker = result.markers.find { it.placeVisitId == "visit1" }
        assertNotNull(parisMarker)
        assertEquals("Paris", parisMarker.title)
        assertEquals(48.8566, parisMarker.coordinate.latitude)
        assertEquals(2.3522, parisMarker.coordinate.longitude)

        // Check bounding region
        assertNotNull(result.region)
        result.region!!.center.latitude shouldBeGreaterThan 48.0
        result.region!!.center.latitude shouldBeLessThan 52.0
    }

    @Test
    fun testExecuteWithRoutesOnly() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val route1 = createTestRoute(
            id = "route1",
            transportType = TransportType.WALK,
            simplifiedPath = listOf(
                Coordinate(48.8566, 2.3522),
                Coordinate(48.8606, 2.3376)
            )
        )
        val route2 = createTestRoute(
            id = "route2",
            transportType = TransportType.CAR,
            simplifiedPath = listOf(
                Coordinate(51.5074, -0.1278),
                Coordinate(51.5154, -0.1419)
            )
        )

        routeRepository.insertRouteSegment(route1)
        routeRepository.insertRouteSegment(route2)

        // When
        val result = useCase.execute(userId, start, end)

        // Then
        assertTrue(result.markers.isEmpty())
        assertEquals(2, result.routes.size)

        val walkRoute = result.routes.find { it.routeSegmentId == "route1" }
        assertNotNull(walkRoute)
        assertEquals(TransportType.WALK, walkRoute.transportType)
        assertEquals(2, walkRoute.coordinates.size)
        assertNotNull(walkRoute.color)

        val carRoute = result.routes.find { it.routeSegmentId == "route2" }
        assertNotNull(carRoute)
        assertEquals(TransportType.CAR, carRoute.transportType)
    }

    @Test
    fun testExecuteWithBothVisitsAndRoutes() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val visit1 = createTestVisit(id = "visit1", city = "Paris")
        val visit2 = createTestVisit(id = "visit2", city = "London")
        visitRepository.insertPlaceVisit(visit1)
        visitRepository.insertPlaceVisit(visit2)

        val route1 = createTestRoute(id = "route1", transportType = TransportType.TRAIN)
        routeRepository.insertRouteSegment(route1)

        // When
        val result = useCase.execute(userId, start, end)

        // Then
        assertEquals(2, result.markers.size)
        assertEquals(1, result.routes.size)
        assertNotNull(result.region)
    }

    @Test
    fun testBoundingRegionCalculation() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        // Add visits at known locations
        val visit1 = createTestVisit(
            id = "visit1",
            latitude = 48.8566, // Paris
            longitude = 2.3522
        )
        val visit2 = createTestVisit(
            id = "visit2",
            latitude = 51.5074, // London
            longitude = -0.1278
        )

        visitRepository.insertPlaceVisit(visit1)
        visitRepository.insertPlaceVisit(visit2)

        // When
        val result = useCase.execute(userId, start, end)

        // Then
        assertNotNull(result.region)

        // Check that region contains all points with padding
        val region = result.region!!

        // The center should be roughly between Paris and London
        region.center.latitude shouldBeGreaterThan 48.0
        region.center.latitude shouldBeLessThan 52.0

        // The deltas should include 20% padding
        region.latitudeDelta shouldBeGreaterThan (51.5074 - 48.8566)
        region.longitudeDelta shouldBeGreaterThan (2.3522 - (-0.1278))

        // Check computed corners
        assertTrue(region.northEast.latitude >= 51.5074)
        assertTrue(region.southWest.latitude <= 48.8566)
    }

    @Test
    fun testTransportTypeColors() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val transportTypes = listOf(
            TransportType.WALK,
            TransportType.BIKE,
            TransportType.CAR,
            TransportType.TRAIN,
            TransportType.PLANE,
            TransportType.BOAT,
            TransportType.UNKNOWN
        )

        transportTypes.forEachIndexed { index, type ->
            val route = createTestRoute(id = "route$index", transportType = type)
            routeRepository.insertRouteSegment(route)
        }

        // When
        val result = useCase.execute(userId, start, end)

        // Then
        assertEquals(transportTypes.size, result.routes.size)

        // Each transport type should have a unique color
        val colors = result.routes.mapNotNull { it.color }.toSet()
        assertTrue(colors.size > 1) // At least some different colors

        // Walk should be green (#4CAF50)
        val walkRoute = result.routes.find { it.transportType == TransportType.WALK }
        assertEquals(0xFF4CAF50.toInt(), walkRoute?.color)

        // Car should be red (#F44336)
        val carRoute = result.routes.find { it.transportType == TransportType.CAR }
        assertEquals(0xFFF44336.toInt(), carRoute?.color)
    }

    private fun createTestVisit(
        id: String,
        city: String = "Paris",
        country: String = "France",
        latitude: Double = 48.8566,
        longitude: Double = 2.3522
    ) = PlaceVisit(
        id = id,
        tripId = "trip1",
        userId = userId,
        startTime = Instant.parse("2024-01-10T10:00:00Z"),
        endTime = Instant.parse("2024-01-10T12:00:00Z"),
        centerLatitude = latitude,
        centerLongitude = longitude,
        radiusMeters = 100.0,
        city = city,
        country = country,
        approximateAddress = "123 Main St",
        confidence = 0.95,
        arrivalTransportType = TransportType.WALK,
        departureTransportType = TransportType.CAR,
        userNotes = null
    )

    private fun createTestRoute(
        id: String,
        transportType: TransportType = TransportType.WALK,
        simplifiedPath: List<Coordinate> = listOf(
            Coordinate(48.8566, 2.3522),
            Coordinate(48.8606, 2.3376)
        )
    ) = RouteSegment(
        id = id,
        tripId = "trip1",
        userId = userId,
        fromVisitId = "visit1",
        toVisitId = "visit2",
        startTime = Instant.parse("2024-01-10T10:00:00Z"),
        endTime = Instant.parse("2024-01-10T11:00:00Z"),
        transportType = transportType,
        distanceMeters = 1000.0,
        simplifiedPath = simplifiedPath,
        confidence = 0.9
    )
}
