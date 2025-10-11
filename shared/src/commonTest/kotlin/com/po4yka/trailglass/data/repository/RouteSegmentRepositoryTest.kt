package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TransportType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

/**
 * Integration tests for RouteSegmentRepository.
 */
class RouteSegmentRepositoryTest {

    private lateinit var repository: RouteSegmentRepository
    private val database = TestDatabaseHelper.createTestDatabase()
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        repository = RouteSegmentRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun testInsertAndGetRouteSegment() = runTest {
        // Given
        val route = createTestRoute(id = "route1")

        // When
        repository.insertRouteSegment(route)
        val result = repository.getRouteSegmentById(route.id, userId)

        // Then
        assertNotNull(result)
        assertEquals(route.id, result.id)
        assertEquals(route.transportType, result.transportType)
        assertEquals(route.distanceMeters, result.distanceMeters)
        assertEquals(route.simplifiedPath.size, result.simplifiedPath.size)
    }

    @Test
    fun testGetRouteSegmentsForTimeRange() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val route1 = createTestRoute(
            id = "route1",
            startTime = Instant.parse("2024-01-05T10:00:00Z"),
            endTime = Instant.parse("2024-01-05T11:00:00Z")
        )
        val route2 = createTestRoute(
            id = "route2",
            startTime = Instant.parse("2024-01-10T14:00:00Z"),
            endTime = Instant.parse("2024-01-10T15:00:00Z")
        )
        val route3 = createTestRoute(
            id = "route3",
            startTime = Instant.parse("2024-02-15T10:00:00Z"),
            endTime = Instant.parse("2024-02-15T11:00:00Z")
        )

        repository.insertRouteSegment(route1)
        repository.insertRouteSegment(route2)
        repository.insertRouteSegment(route3)

        // When
        val results = repository.getRouteSegmentsForTimeRange(userId, start, end).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "route1" })
        assertTrue(results.any { it.id == "route2" })
        assertFalse(results.any { it.id == "route3" })
    }

    @Test
    fun testGetRouteSegmentsByTransportType() = runTest {
        // Given
        val route1 = createTestRoute(id = "route1", transportType = TransportType.WALK)
        val route2 = createTestRoute(id = "route2", transportType = TransportType.CAR)
        val route3 = createTestRoute(id = "route3", transportType = TransportType.WALK)

        repository.insertRouteSegment(route1)
        repository.insertRouteSegment(route2)
        repository.insertRouteSegment(route3)

        // When
        val results = repository.getRouteSegmentsByTransportType(
            userId,
            TransportType.WALK
        ).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.transportType == TransportType.WALK })
    }

    @Test
    fun testUpdateRouteSegment() = runTest {
        // Given
        val original = createTestRoute(
            id = "route1",
            transportType = TransportType.UNKNOWN,
            distanceMeters = 1000.0
        )
        repository.insertRouteSegment(original)

        // When
        val updated = original.copy(
            transportType = TransportType.CAR,
            distanceMeters = 1500.0
        )
        repository.updateRouteSegment(updated)
        val result = repository.getRouteSegmentById(original.id, userId)

        // Then
        assertNotNull(result)
        assertEquals(TransportType.CAR, result.transportType)
        assertEquals(1500.0, result.distanceMeters)
    }

    @Test
    fun testDeleteRouteSegment() = runTest {
        // Given
        val route = createTestRoute(id = "route1")
        repository.insertRouteSegment(route)

        // When
        repository.deleteRouteSegment(route.id, userId)
        val result = repository.getRouteSegmentById(route.id, userId)

        // Then
        assertNull(result)
    }

    @Test
    fun testGetRouteSegmentsForTrip() = runTest {
        // Given
        val tripId = "trip1"
        val route1 = createTestRoute(id = "route1", tripId = tripId)
        val route2 = createTestRoute(id = "route2", tripId = tripId)
        val route3 = createTestRoute(id = "route3", tripId = "trip2")

        repository.insertRouteSegment(route1)
        repository.insertRouteSegment(route2)
        repository.insertRouteSegment(route3)

        // When
        val results = repository.getRouteSegmentsForTrip(tripId, userId).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.tripId == tripId })
    }

    @Test
    fun testGetTotalDistanceByTransportType() = runTest {
        // Given
        val route1 = createTestRoute(
            id = "route1",
            transportType = TransportType.WALK,
            distanceMeters = 1000.0
        )
        val route2 = createTestRoute(
            id = "route2",
            transportType = TransportType.WALK,
            distanceMeters = 500.0
        )
        val route3 = createTestRoute(
            id = "route3",
            transportType = TransportType.CAR,
            distanceMeters = 5000.0
        )

        repository.insertRouteSegment(route1)
        repository.insertRouteSegment(route2)
        repository.insertRouteSegment(route3)

        // When
        val walkDistance = repository.getTotalDistanceByTransportType(
            userId,
            TransportType.WALK
        ).first()
        val carDistance = repository.getTotalDistanceByTransportType(
            userId,
            TransportType.CAR
        ).first()

        // Then
        assertEquals(1500.0, walkDistance)
        assertEquals(5000.0, carDistance)
    }

    @Test
    fun testSimplifiedPathSerialization() = runTest {
        // Given
        val path = listOf(
            Coordinate(48.8566, 2.3522),
            Coordinate(48.8606, 2.3376),
            Coordinate(48.8656, 2.3212)
        )
        val route = createTestRoute(id = "route1", simplifiedPath = path)

        // When
        repository.insertRouteSegment(route)
        val result = repository.getRouteSegmentById(route.id, userId)

        // Then
        assertNotNull(result)
        assertEquals(path.size, result.simplifiedPath.size)
        path.forEachIndexed { index, coord ->
            assertEquals(coord.latitude, result.simplifiedPath[index].latitude, 0.0001)
            assertEquals(coord.longitude, result.simplifiedPath[index].longitude, 0.0001)
        }
    }

    private fun createTestRoute(
        id: String,
        tripId: String = "trip1",
        transportType: TransportType = TransportType.WALK,
        startTime: Instant = Instant.parse("2024-01-01T10:00:00Z"),
        endTime: Instant = Instant.parse("2024-01-01T11:00:00Z"),
        distanceMeters: Double = 1000.0,
        simplifiedPath: List<Coordinate> = listOf(
            Coordinate(48.8566, 2.3522),
            Coordinate(48.8606, 2.3376)
        )
    ) = RouteSegment(
        id = id,
        tripId = tripId,
        userId = userId,
        fromVisitId = "visit1",
        toVisitId = "visit2",
        startTime = startTime,
        endTime = endTime,
        transportType = transportType,
        distanceMeters = distanceMeters,
        simplifiedPath = simplifiedPath,
        confidence = 0.9
    )
}
