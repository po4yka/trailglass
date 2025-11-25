package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.data.repository.impl.LocationRepositoryImpl
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class LocationRepositoryTest {
    private lateinit var repository: LocationRepository
    private val database = TestDatabaseHelper.createTestDatabase()
    private val userId = "test_user_123"
    private val deviceId = "test_device_456"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        repository = LocationRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun `insertSample should store location sample successfully`() =
        runTest {
            // Given
            val sample =
                createTestSample(
                    id = "sample1",
                    latitude = 37.7749,
                    longitude = -122.4194
                )

            // When
            val result = repository.insertSample(sample)

            // Then
            result.isSuccess shouldBe true
        }

    @Test
    fun `getSampleById should return inserted sample`() =
        runTest {
            // Given
            val sample = createTestSample(id = "sample1")
            repository.insertSample(sample)

            // When
            val result = repository.getSampleById("sample1")

            // Then
            result.isSuccess shouldBe true
            val retrieved = result.getOrNull()
            retrieved shouldNotBe null
            retrieved?.id shouldBe "sample1"
            retrieved?.latitude shouldBe sample.latitude
            retrieved?.longitude shouldBe sample.longitude
            retrieved?.accuracy shouldBe sample.accuracy
            retrieved?.userId shouldBe userId
        }

    @Test
    fun `getSampleById should return null for non-existent sample`() =
        runTest {
            // When
            val result = repository.getSampleById("non_existent")

            // Then
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe null
        }

    @Test
    fun `getSamples should return samples within time range`() =
        runTest {
            // Given
            val baseTime = Clock.System.now()
            val sample1 = createTestSample(id = "sample1", timestamp = baseTime)
            val sample2 = createTestSample(id = "sample2", timestamp = baseTime + 30.minutes)
            val sample3 = createTestSample(id = "sample3", timestamp = baseTime + 2.hours)

            repository.insertSample(sample1)
            repository.insertSample(sample2)
            repository.insertSample(sample3)

            // When
            val result =
                repository.getSamples(
                    userId = userId,
                    startTime = baseTime,
                    endTime = baseTime + 1.hours
                )

            // Then
            result.isSuccess shouldBe true
            val samples = result.getOrNull()!!
            samples.size shouldBe 2
            samples.any { it.id == "sample1" } shouldBe true
            samples.any { it.id == "sample2" } shouldBe true
            samples.any { it.id == "sample3" } shouldBe false
        }

    @Test
    fun `getSamplesForTrip should return only samples for specific trip`() =
        runTest {
            // Given
            val trip1Sample = createTestSample(id = "trip1_sample", tripId = "trip_123")
            val trip2Sample = createTestSample(id = "trip2_sample", tripId = "trip_456")
            val noTripSample = createTestSample(id = "no_trip_sample", tripId = null)

            repository.insertSample(trip1Sample)
            repository.insertSample(trip2Sample)
            repository.insertSample(noTripSample)

            // When
            val result = repository.getSamplesForTrip("trip_123")

            // Then
            result.isSuccess shouldBe true
            val samples = result.getOrNull()!!
            samples.size shouldBe 1
            samples.first().id shouldBe "trip1_sample"
            samples.first().tripId shouldBe "trip_123"
        }

    @Test
    fun `getUnprocessedSamples should return samples without trip ID`() =
        runTest {
            // Given
            val unprocessed1 = createTestSample(id = "unprocessed1", tripId = null)
            val unprocessed2 = createTestSample(id = "unprocessed2", tripId = null)
            val processed = createTestSample(id = "processed", tripId = "trip_123")

            repository.insertSample(unprocessed1)
            repository.insertSample(unprocessed2)
            repository.insertSample(processed)

            // When
            val result = repository.getUnprocessedSamples(userId, limit = 10)

            // Then
            result.isSuccess shouldBe true
            val samples = result.getOrNull()!!
            samples.size shouldBe 2
            samples.all { it.tripId == null } shouldBe true
        }

    @Test
    fun `getUnprocessedSamples should respect limit parameter`() =
        runTest {
            // Given
            repeat(5) { index ->
                val sample = createTestSample(id = "unprocessed_$index", tripId = null)
                repository.insertSample(sample)
            }

            // When
            val result = repository.getUnprocessedSamples(userId, limit = 3)

            // Then
            result.isSuccess shouldBe true
            result.getOrNull()!!.size shouldBe 3
        }

    @Test
    fun `updateTripId should assign trip to sample`() =
        runTest {
            // Given
            val sample = createTestSample(id = "sample1", tripId = null)
            repository.insertSample(sample)

            // When
            val updateResult = repository.updateTripId("sample1", "trip_789")

            // Then
            updateResult.isSuccess shouldBe true

            val retrievedResult = repository.getSampleById("sample1")
            retrievedResult.getOrNull()?.tripId shouldBe "trip_789"
        }

    @Test
    fun `updateTripId should clear trip from sample`() =
        runTest {
            // Given
            val sample = createTestSample(id = "sample1", tripId = "trip_123")
            repository.insertSample(sample)

            // When
            val updateResult = repository.updateTripId("sample1", null)

            // Then
            updateResult.isSuccess shouldBe true

            val retrievedResult = repository.getSampleById("sample1")
            retrievedResult.getOrNull()?.tripId shouldBe null
        }

    @Test
    fun `deleteSample should soft delete sample`() =
        runTest {
            // Given
            val sample = createTestSample(id = "sample1")
            repository.insertSample(sample)

            // When
            val deleteResult = repository.deleteSample("sample1")

            // Then
            deleteResult.isSuccess shouldBe true

            // Sample should no longer be retrievable
            val retrievedResult = repository.getSampleById("sample1")
            retrievedResult.getOrNull() shouldBe null
        }

    @Test
    fun `deleteOldSamples should remove samples before given time`() =
        runTest {
            // Given
            val cutoffTime = Clock.System.now()
            val oldSample = createTestSample(id = "old", timestamp = cutoffTime - 2.hours)
            val recentSample = createTestSample(id = "recent", timestamp = cutoffTime + 1.hours)

            repository.insertSample(oldSample)
            repository.insertSample(recentSample)

            // When
            val deleteResult = repository.deleteOldSamples(userId, beforeTime = cutoffTime)

            // Then
            deleteResult.isSuccess shouldBe true

            repository.getSampleById("old").getOrNull() shouldBe null
            repository.getSampleById("recent").getOrNull() shouldNotBe null
        }

    @Test
    fun `should handle different location sources`() =
        runTest {
            // Given
            val gpsSample = createTestSample(id = "gps", source = LocationSource.GPS)
            val networkSample = createTestSample(id = "network", source = LocationSource.NETWORK)
            val visitSample = createTestSample(id = "visit", source = LocationSource.VISIT)

            // When
            repository.insertSample(gpsSample)
            repository.insertSample(networkSample)
            repository.insertSample(visitSample)

            // Then
            repository.getSampleById("gps").getOrNull()?.source shouldBe LocationSource.GPS
            repository.getSampleById("network").getOrNull()?.source shouldBe LocationSource.NETWORK
            repository.getSampleById("visit").getOrNull()?.source shouldBe LocationSource.VISIT
        }

    @Test
    fun `should store optional fields correctly`() =
        runTest {
            // Given
            val sampleWithOptionals =
                createTestSample(
                    id = "with_optionals",
                    speed = 25.5,
                    bearing = 180.0,
                    altitude = 100.5
                )
            val sampleWithoutOptionals =
                createTestSample(
                    id = "without_optionals",
                    speed = null,
                    bearing = null,
                    altitude = null
                )

            // When
            repository.insertSample(sampleWithOptionals)
            repository.insertSample(sampleWithoutOptionals)

            // Then
            val retrieved1 = repository.getSampleById("with_optionals").getOrNull()
            retrieved1?.speed shouldBe 25.5
            retrieved1?.bearing shouldBe 180.0
            retrieved1?.altitude shouldBe 100.5

            val retrieved2 = repository.getSampleById("without_optionals").getOrNull()
            retrieved2?.speed shouldBe null
            retrieved2?.bearing shouldBe null
            retrieved2?.altitude shouldBe null
        }

    // Helper function to create test samples
    private fun createTestSample(
        id: String,
        timestamp: Instant = Clock.System.now(),
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        altitude: Double? = null,
        accuracy: Double = 10.0,
        speed: Double? = null,
        bearing: Double? = null,
        source: LocationSource = LocationSource.GPS,
        tripId: String? = null
    ) = LocationSample(
        id = id,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        source = source,
        tripId = tripId,
        uploadedAt = null,
        deviceId = deviceId,
        userId = userId
    )
}
