package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.TransportType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

/**
 * Integration tests for PlaceVisitRepository.
 */
class PlaceVisitRepositoryTest {

    private lateinit var repository: PlaceVisitRepository
    private val database = TestDatabaseHelper.createTestDatabase()
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        repository = PlaceVisitRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun testInsertAndGetPlaceVisit() = runTest {
        // Given
        val visit = createTestVisit(id = "visit1")

        // When
        repository.insertPlaceVisit(visit)
        val result = repository.getPlaceVisitById(visit.id, userId)

        // Then
        assertNotNull(result)
        assertEquals(visit.id, result.id)
        assertEquals(visit.city, result.city)
        assertEquals(visit.country, result.country)
        assertEquals(visit.centerLatitude, result.centerLatitude)
        assertEquals(visit.centerLongitude, result.centerLongitude)
    }

    @Test
    fun testGetPlaceVisitsForTimeRange() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val visit1 = createTestVisit(
            id = "visit1",
            startTime = Instant.parse("2024-01-05T10:00:00Z"),
            endTime = Instant.parse("2024-01-05T12:00:00Z")
        )
        val visit2 = createTestVisit(
            id = "visit2",
            startTime = Instant.parse("2024-01-10T14:00:00Z"),
            endTime = Instant.parse("2024-01-10T16:00:00Z")
        )
        val visit3 = createTestVisit(
            id = "visit3",
            startTime = Instant.parse("2024-02-15T10:00:00Z"),
            endTime = Instant.parse("2024-02-15T12:00:00Z")
        )

        repository.insertPlaceVisit(visit1)
        repository.insertPlaceVisit(visit2)
        repository.insertPlaceVisit(visit3)

        // When
        val results = repository.getPlaceVisitsForTimeRange(userId, start, end).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "visit1" })
        assertTrue(results.any { it.id == "visit2" })
        assertFalse(results.any { it.id == "visit3" })
    }

    @Test
    fun testUpdatePlaceVisit() = runTest {
        // Given
        val original = createTestVisit(id = "visit1", city = "Paris")
        repository.insertPlaceVisit(original)

        // When
        val updated = original.copy(city = "Lyon", country = "France")
        repository.updatePlaceVisit(updated)
        val result = repository.getPlaceVisitById(original.id, userId)

        // Then
        assertNotNull(result)
        assertEquals("Lyon", result.city)
        assertEquals("France", result.country)
    }

    @Test
    fun testDeletePlaceVisit() = runTest {
        // Given
        val visit = createTestVisit(id = "visit1")
        repository.insertPlaceVisit(visit)

        // When
        repository.deletePlaceVisit(visit.id, userId)
        val result = repository.getPlaceVisitById(visit.id, userId)

        // Then
        assertNull(result)
    }

    @Test
    fun testGetPlaceVisitsForTrip() = runTest {
        // Given
        val tripId = "trip1"
        val visit1 = createTestVisit(id = "visit1", tripId = tripId)
        val visit2 = createTestVisit(id = "visit2", tripId = tripId)
        val visit3 = createTestVisit(id = "visit3", tripId = "trip2")

        repository.insertPlaceVisit(visit1)
        repository.insertPlaceVisit(visit2)
        repository.insertPlaceVisit(visit3)

        // When
        val results = repository.getPlaceVisitsForTrip(tripId, userId).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.tripId == tripId })
    }

    @Test
    fun testGetPlaceVisitsNearLocation() = runTest {
        // Given
        val centerLat = 48.8566
        val centerLon = 2.3522
        val radiusMeters = 1000.0

        // Paris center
        val visit1 = createTestVisit(
            id = "visit1",
            centerLatitude = 48.8566,
            centerLongitude = 2.3522
        )
        // Nearby
        val visit2 = createTestVisit(
            id = "visit2",
            centerLatitude = 48.8606,
            centerLongitude = 2.3376
        )
        // Far away (London)
        val visit3 = createTestVisit(
            id = "visit3",
            centerLatitude = 51.5074,
            centerLongitude = -0.1278
        )

        repository.insertPlaceVisit(visit1)
        repository.insertPlaceVisit(visit2)
        repository.insertPlaceVisit(visit3)

        // When
        val results = repository.getPlaceVisitsNearLocation(
            userId,
            centerLat,
            centerLon,
            radiusMeters
        ).first()

        // Then
        assertTrue(results.size >= 1) // At least the exact match
        assertTrue(results.any { it.id == "visit1" })
        assertFalse(results.any { it.id == "visit3" }) // London should not be included
    }

    @Test
    fun testGetAllCountries() = runTest {
        // Given
        val visit1 = createTestVisit(id = "visit1", country = "France")
        val visit2 = createTestVisit(id = "visit2", country = "Spain")
        val visit3 = createTestVisit(id = "visit3", country = "France")

        repository.insertPlaceVisit(visit1)
        repository.insertPlaceVisit(visit2)
        repository.insertPlaceVisit(visit3)

        // When
        val results = repository.getAllCountries(userId).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.contains("France"))
        assertTrue(results.contains("Spain"))
    }

    @Test
    fun testGetAllCities() = runTest {
        // Given
        val visit1 = createTestVisit(id = "visit1", city = "Paris", country = "France")
        val visit2 = createTestVisit(id = "visit2", city = "Lyon", country = "France")
        val visit3 = createTestVisit(id = "visit3", city = "Paris", country = "France")

        repository.insertPlaceVisit(visit1)
        repository.insertPlaceVisit(visit2)
        repository.insertPlaceVisit(visit3)

        // When
        val results = repository.getAllCities(userId).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.contains("Paris"))
        assertTrue(results.contains("Lyon"))
    }

    private fun createTestVisit(
        id: String,
        tripId: String = "trip1",
        city: String = "Paris",
        country: String = "France",
        startTime: Instant = Instant.parse("2024-01-01T10:00:00Z"),
        endTime: Instant = Instant.parse("2024-01-01T12:00:00Z"),
        centerLatitude: Double = 48.8566,
        centerLongitude: Double = 2.3522
    ) = PlaceVisit(
        id = id,
        tripId = tripId,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        radiusMeters = 100.0,
        city = city,
        country = country,
        approximateAddress = "123 Main St",
        confidence = 0.95,
        arrivalTransportType = TransportType.WALK,
        departureTransportType = TransportType.CAR,
        userNotes = null
    )
}
