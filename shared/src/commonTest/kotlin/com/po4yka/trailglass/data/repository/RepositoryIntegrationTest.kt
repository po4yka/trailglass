package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.data.repository.impl.TripRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.PlaceVisitRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.LocationRepositoryImpl
import com.po4yka.trailglass.domain.model.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration tests for repository implementations.
 * Tests actual database operations, cross-repository interactions, and data persistence.
 */
class RepositoryIntegrationTest {

    @Test
    fun `TripRepository should persist and retrieve trips`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        val trip = createTestTrip("trip1", "Test Trip")

        // Insert trip
        repository.upsertTrip(trip)

        // Retrieve trip
        val retrieved = repository.getTripById("trip1")
        retrieved shouldNotBe null
        retrieved?.id shouldBe "trip1"
        retrieved?.name shouldBe "Test Trip"
        retrieved?.userId shouldBe "test_user"
    }

    @Test
    fun `TripRepository should update existing trips`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        val trip = createTestTrip("trip1", "Original Name")
        repository.upsertTrip(trip)

        // Update trip
        val updated = trip.copy(name = "Updated Name")
        repository.upsertTrip(updated)

        // Verify update
        val retrieved = repository.getTripById("trip1")
        retrieved?.name shouldBe "Updated Name"
    }

    @Test
    fun `TripRepository should retrieve trips by user`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        // Insert trips for different users
        repository.upsertTrip(createTestTrip("trip1", "Trip 1", "user1"))
        repository.upsertTrip(createTestTrip("trip2", "Trip 2", "user1"))
        repository.upsertTrip(createTestTrip("trip3", "Trip 3", "user2"))

        // Get trips for user1
        val user1Trips = repository.getTripsForUser("user1")
        user1Trips.size shouldBe 2
        user1Trips.all { it.userId == "user1" } shouldBe true
    }

    @Test
    fun `TripRepository should filter ongoing trips`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        // Insert ongoing and completed trips
        val ongoingTrip = createTestTrip("trip1", "Ongoing", isOngoing = true)
        val completedTrip = createTestTrip("trip2", "Completed", isOngoing = false)

        repository.upsertTrip(ongoingTrip)
        repository.upsertTrip(completedTrip)

        // Get ongoing trips
        val ongoing = repository.getOngoingTrips("test_user")
        ongoing.size shouldBe 1
        ongoing[0].isOngoing shouldBe true
    }

    @Test
    fun `TripRepository should retrieve trips in time range`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        val now = Clock.System.now()
        val past = now.minus(kotlinx.datetime.DateTimeUnit.DAY, 10, kotlinx.datetime.TimeZone.UTC)
        val future = now.plus(kotlinx.datetime.DateTimeUnit.DAY, 10, kotlinx.datetime.TimeZone.UTC)

        repository.upsertTrip(createTestTrip("trip1", "Recent", startTime = now))
        repository.upsertTrip(createTestTrip("trip2", "Old", startTime = past))
        repository.upsertTrip(createTestTrip("trip3", "Future", startTime = future))

        // Get trips in range
        val rangeStart = now.minus(kotlinx.datetime.DateTimeUnit.DAY, 5, kotlinx.datetime.TimeZone.UTC)
        val rangeEnd = now.plus(kotlinx.datetime.DateTimeUnit.DAY, 5, kotlinx.datetime.TimeZone.UTC)
        val tripsInRange = repository.getTripsInRange("test_user", rangeStart, rangeEnd)

        tripsInRange.size shouldBe 1
        tripsInRange[0].id shouldBe "trip1"
    }

    @Test
    fun `TripRepository should complete ongoing trips`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        val trip = createTestTrip("trip1", "Test", isOngoing = true)
        repository.upsertTrip(trip)

        val endTime = Clock.System.now()
        repository.completeTrip("trip1", endTime)

        val completed = repository.getTripById("trip1")
        completed?.isOngoing shouldBe false
        completed?.endTime shouldBe endTime
    }

    @Test
    fun `TripRepository should delete trips`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        val trip = createTestTrip("trip1", "Test")
        repository.upsertTrip(trip)

        repository.deleteTrip("trip1")

        val deleted = repository.getTripById("trip1")
        deleted shouldBe null
    }

    @Test
    fun `TripRepository should count total trips`() = runTest {
        val database = createTestDatabase()
        val repository = TripRepositoryImpl(database)

        repository.upsertTrip(createTestTrip("trip1", "Trip 1"))
        repository.upsertTrip(createTestTrip("trip2", "Trip 2"))
        repository.upsertTrip(createTestTrip("trip3", "Trip 3"))

        val count = repository.getTripCount("test_user")
        count shouldBe 3
    }

    @Test
    fun `PlaceVisitRepository should persist and retrieve visits`() = runTest {
        val database = createTestDatabase()
        val repository = PlaceVisitRepositoryImpl(database)

        val visit = createTestPlaceVisit("visit1")
        repository.insertVisit(visit)

        val retrieved = repository.getVisitById("visit1")
        retrieved shouldNotBe null
        retrieved?.id shouldBe "visit1"
        retrieved?.userId shouldBe "test_user"
    }

    @Test
    fun `PlaceVisitRepository should retrieve visits in time range`() = runTest {
        val database = createTestDatabase()
        val repository = PlaceVisitRepositoryImpl(database)

        val now = Clock.System.now()
        val past = now.minus(kotlinx.datetime.DateTimeUnit.DAY, 10, kotlinx.datetime.TimeZone.UTC)
        val future = now.plus(kotlinx.datetime.DateTimeUnit.DAY, 10, kotlinx.datetime.TimeZone.UTC)

        repository.insertVisit(createTestPlaceVisit("visit1", startTime = now))
        repository.insertVisit(createTestPlaceVisit("visit2", startTime = past))
        repository.insertVisit(createTestPlaceVisit("visit3", startTime = future))

        val rangeStart = now.minus(kotlinx.datetime.DateTimeUnit.DAY, 5, kotlinx.datetime.TimeZone.UTC)
        val rangeEnd = now.plus(kotlinx.datetime.DateTimeUnit.DAY, 5, kotlinx.datetime.TimeZone.UTC)
        val visitsInRange = repository.getVisitsInRange("test_user", rangeStart, rangeEnd)

        visitsInRange.size shouldBe 1
        visitsInRange[0].id shouldBe "visit1"
    }

    @Test
    fun `LocationRepository should handle Result types correctly`() = runTest {
        val database = createTestDatabase()
        val repository = LocationRepositoryImpl(database)

        val sample = createTestLocationSample("sample1")
        val insertResult = repository.insertSample(sample)

        assertTrue(insertResult.isSuccess)

        val getResult = repository.getSampleById("sample1")
        assertTrue(getResult.isSuccess)
        getResult.getOrNull()?.id shouldBe "sample1"
    }

    @Test
    fun `LocationRepository should retrieve samples for trip`() = runTest {
        val database = createTestDatabase()
        val repository = LocationRepositoryImpl(database)

        // Insert samples with different trip IDs
        repository.insertSample(createTestLocationSample("sample1", tripId = "trip1"))
        repository.insertSample(createTestLocationSample("sample2", tripId = "trip1"))
        repository.insertSample(createTestLocationSample("sample3", tripId = "trip2"))

        val trip1Samples = repository.getSamplesForTrip("trip1").getOrNull()
        trip1Samples?.size shouldBe 2
        trip1Samples?.all { it.tripId == "trip1" } shouldBe true
    }

    @Test
    fun `LocationRepository should retrieve unprocessed samples`() = runTest {
        val database = createTestDatabase()
        val repository = LocationRepositoryImpl(database)

        // Insert processed and unprocessed samples
        repository.insertSample(createTestLocationSample("sample1", tripId = "trip1"))
        repository.insertSample(createTestLocationSample("sample2", tripId = null))
        repository.insertSample(createTestLocationSample("sample3", tripId = null))

        val unprocessed = repository.getUnprocessedSamples().getOrNull()
        unprocessed?.size shouldBe 2
        unprocessed?.all { it.tripId == null } shouldBe true
    }

    @Test
    fun `LocationRepository should update trip ID`() = runTest {
        val database = createTestDatabase()
        val repository = LocationRepositoryImpl(database)

        repository.insertSample(createTestLocationSample("sample1", tripId = null))

        repository.updateTripId("sample1", "trip1")

        val updated = repository.getSampleById("sample1").getOrNull()
        updated?.tripId shouldBe "trip1"
    }

    @Test
    fun `LocationRepository should delete old samples`() = runTest {
        val database = createTestDatabase()
        val repository = LocationRepositoryImpl(database)

        val now = Clock.System.now()
        val old = now.minus(kotlinx.datetime.DateTimeUnit.DAY, 100, kotlinx.datetime.TimeZone.UTC)

        repository.insertSample(createTestLocationSample("sample1", timestamp = now))
        repository.insertSample(createTestLocationSample("sample2", timestamp = old))

        val cutoffTime = now.minus(kotlinx.datetime.DateTimeUnit.DAY, 90, kotlinx.datetime.TimeZone.UTC)
        repository.deleteOldSamples(cutoffTime)

        val remaining = repository.getSamples("test_user").getOrNull()
        remaining?.size shouldBe 1
        remaining?.get(0)?.id shouldBe "sample1"
    }

    @Test
    fun `cross-repository trip and visit integration`() = runTest {
        val database = createTestDatabase()
        val tripRepository = TripRepositoryImpl(database)
        val visitRepository = PlaceVisitRepositoryImpl(database)

        // Create a trip
        val trip = createTestTrip("trip1", "Paris Trip")
        tripRepository.upsertTrip(trip)

        // Create visits for the trip
        val visit1 = createTestPlaceVisit("visit1", tripId = "trip1")
        val visit2 = createTestPlaceVisit("visit2", tripId = "trip1")
        visitRepository.insertVisit(visit1)
        visitRepository.insertVisit(visit2)

        // Verify trip exists
        val retrievedTrip = tripRepository.getTripById("trip1")
        retrievedTrip shouldNotBe null

        // Verify visits are linked to trip
        val visits = visitRepository.getVisitsForUser("test_user")
        visits.size shouldBe 2
        visits.all { it.tripId == "trip1" } shouldBe true
    }

    @Test
    fun `cross-repository trip deletion should handle orphaned visits`() = runTest {
        val database = createTestDatabase()
        val tripRepository = TripRepositoryImpl(database)
        val visitRepository = PlaceVisitRepositoryImpl(database)

        val trip = createTestTrip("trip1", "Test Trip")
        tripRepository.upsertTrip(trip)

        val visit = createTestPlaceVisit("visit1", tripId = "trip1")
        visitRepository.insertVisit(visit)

        // Delete trip
        tripRepository.deleteTrip("trip1")

        // Visit should still exist (orphaned)
        val orphanedVisit = visitRepository.getVisitById("visit1")
        orphanedVisit shouldNotBe null
        orphanedVisit?.tripId shouldBe "trip1" // Still references deleted trip
    }

    // Helper functions

    private fun createTestDatabase(): com.po4yka.trailglass.db.TrailGlassDatabase {
        // Create an in-memory database for testing
        val driver = app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver(
            app.cash.sqldelight.db.SqlDriver.Companion.IN_MEMORY
        )
        com.po4yka.trailglass.db.TrailGlassDatabase.Schema.create(driver)
        return com.po4yka.trailglass.db.TrailGlassDatabase(driver)
    }

    private fun createTestTrip(
        id: String,
        name: String,
        userId: String = "test_user",
        startTime: kotlinx.datetime.Instant = Clock.System.now(),
        isOngoing: Boolean = false
    ): Trip {
        return Trip(
            id = id,
            name = name,
            startTime = startTime,
            endTime = if (isOngoing) null else Clock.System.now(),
            primaryCountry = "US",
            isOngoing = isOngoing,
            userId = userId
        )
    }

    private fun createTestPlaceVisit(
        id: String,
        userId: String = "test_user",
        tripId: String? = null,
        startTime: kotlinx.datetime.Instant = Clock.System.now()
    ): PlaceVisit {
        return PlaceVisit(
            id = id,
            userId = userId,
            tripId = tripId,
            startTime = startTime,
            endTime = null,
            centerLatitude = 37.7749,
            centerLongitude = -122.4194,
            city = "San Francisco",
            countryCode = "US",
            poiName = "Test Location",
            approximateAddress = "123 Test St"
        )
    }

    private fun createTestLocationSample(
        id: String,
        userId: String = "test_user",
        tripId: String? = null,
        timestamp: kotlinx.datetime.Instant = Clock.System.now()
    ): LocationSample {
        return LocationSample(
            id = id,
            userId = userId,
            tripId = tripId,
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 0.0,
            accuracy = 10.0,
            timestamp = timestamp,
            speed = null,
            bearing = null
        )
    }
}
