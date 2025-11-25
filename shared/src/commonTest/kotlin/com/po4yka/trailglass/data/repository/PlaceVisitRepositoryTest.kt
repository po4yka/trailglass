package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.data.repository.impl.LocationRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.PlaceVisitRepositoryImpl
import com.po4yka.trailglass.di.FakeUserSession
import com.po4yka.trailglass.domain.model.CategoryConfidence
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
import com.po4yka.trailglass.domain.model.PlaceVisit
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
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

class PlaceVisitRepositoryTest {
    private lateinit var visitRepository: PlaceVisitRepository
    private lateinit var locationRepository: LocationRepository
    private val database = TestDatabaseHelper.createTestDatabase()
    private val userId = "test_user_123"
    private val deviceId = "test_device_456"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        visitRepository = PlaceVisitRepositoryImpl(database, FakeUserSession(userId))
        locationRepository = LocationRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun `insertVisit should store place visit successfully`() =
        runTest {
            // Given
            val visit = createTestVisit(id = "visit1")

            // When
            visitRepository.insertVisit(visit)

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved shouldNotBe null
            retrieved?.id shouldBe "visit1"
        }

    @Test
    fun `getVisitById should return inserted visit`() =
        runTest {
            // Given
            val visit =
                createTestVisit(
                    id = "visit1",
                    centerLatitude = 37.7749,
                    centerLongitude = -122.4194,
                    poiName = "Golden Gate Park",
                    city = "San Francisco"
                )
            visitRepository.insertVisit(visit)

            // When
            val retrieved = visitRepository.getVisitById("visit1")

            // Then
            retrieved shouldNotBe null
            retrieved?.id shouldBe "visit1"
            retrieved?.centerLatitude shouldBe 37.7749
            retrieved?.centerLongitude shouldBe -122.4194
            retrieved?.poiName shouldBe "Golden Gate Park"
            retrieved?.city shouldBe "San Francisco"
        }

    @Test
    fun `getVisitById should return null for non-existent visit`() =
        runTest {
            // When
            val result = visitRepository.getVisitById("non_existent")

            // Then
            result shouldBe null
        }

    @Test
    fun `getVisits should return visits within time range`() =
        runTest {
            // Given
            val baseTime = Clock.System.now()
            val visit1 =
                createTestVisit(
                    id = "visit1",
                    startTime = baseTime,
                    endTime = baseTime + 30.minutes
                )
            val visit2 =
                createTestVisit(
                    id = "visit2",
                    startTime = baseTime + 1.hours,
                    endTime = baseTime + 2.hours
                )
            val visit3 =
                createTestVisit(
                    id = "visit3",
                    startTime = baseTime + 5.hours,
                    endTime = baseTime + 6.hours
                )

            visitRepository.insertVisit(visit1)
            visitRepository.insertVisit(visit2)
            visitRepository.insertVisit(visit3)

            // When
            val visits =
                visitRepository.getVisits(
                    userId = userId,
                    startTime = baseTime,
                    endTime = baseTime + 3.hours
                )

            // Then
            visits.size shouldBe 2
            visits.map { it.id } shouldContain "visit1"
            visits.map { it.id } shouldContain "visit2"
            visits.map { it.id } shouldNotContain "visit3"
        }

    @Test
    fun `getVisitsByUser should return paginated visits`() =
        runTest {
            // Given
            repeat(10) { index ->
                val visit =
                    createTestVisit(
                        id = "visit_$index",
                        startTime = Clock.System.now() + (index * 10).minutes
                    )
                visitRepository.insertVisit(visit)
            }

            // When - Get first page
            val firstPage = visitRepository.getVisitsByUser(userId, limit = 5, offset = 0)

            // Then
            firstPage.size shouldBe 5

            // When - Get second page
            val secondPage = visitRepository.getVisitsByUser(userId, limit = 5, offset = 5)

            // Then
            secondPage.size shouldBe 5

            // No overlap between pages
            val firstPageIds = firstPage.map { it.id }.toSet()
            val secondPageIds = secondPage.map { it.id }.toSet()
            firstPageIds.intersect(secondPageIds).isEmpty() shouldBe true
        }

    @Test
    fun `updateVisit should modify existing visit`() =
        runTest {
            // Given
            val originalVisit =
                createTestVisit(
                    id = "visit1",
                    poiName = "Original Name",
                    userLabel = null
                )
            visitRepository.insertVisit(originalVisit)

            // When
            val updatedVisit =
                originalVisit.copy(
                    poiName = "Updated Name",
                    userLabel = "My Favorite Place"
                )
            visitRepository.updateVisit(updatedVisit)

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved?.poiName shouldBe "Updated Name"
            retrieved?.userLabel shouldBe "My Favorite Place"
        }

    @Test
    fun `linkSamples should associate location samples with visit`() =
        runTest {
            // Given
            val visit = createTestVisit(id = "visit1")
            visitRepository.insertVisit(visit)

            // Insert location samples
            val sample1 = createTestLocationSample(id = "sample1")
            val sample2 = createTestLocationSample(id = "sample2")
            locationRepository.insertSample(sample1)
            locationRepository.insertSample(sample2)

            // When
            visitRepository.linkSamples("visit1", listOf("sample1", "sample2"))

            // Then
            val linkedSamples = visitRepository.getSamplesForVisit("visit1")
            linkedSamples.size shouldBe 2
            linkedSamples.map { it.id } shouldContain "sample1"
            linkedSamples.map { it.id } shouldContain "sample2"
        }

    @Test
    fun `unlinkSample should remove association between sample and visit`() =
        runTest {
            // Given
            val visit = createTestVisit(id = "visit1")
            visitRepository.insertVisit(visit)

            val sample1 = createTestLocationSample(id = "sample1")
            val sample2 = createTestLocationSample(id = "sample2")
            locationRepository.insertSample(sample1)
            locationRepository.insertSample(sample2)

            visitRepository.linkSamples("visit1", listOf("sample1", "sample2"))

            // When
            visitRepository.unlinkSample("visit1", "sample1")

            // Then
            val linkedSamples = visitRepository.getSamplesForVisit("visit1")
            linkedSamples.size shouldBe 1
            linkedSamples.map { it.id } shouldNotContain "sample1"
            linkedSamples.map { it.id } shouldContain "sample2"
        }

    @Test
    fun `getSamplesForVisit should return empty list for visit with no samples`() =
        runTest {
            // Given
            val visit = createTestVisit(id = "visit1")
            visitRepository.insertVisit(visit)

            // When
            val samples = visitRepository.getSamplesForVisit("visit1")

            // Then
            samples.isEmpty() shouldBe true
        }

    @Test
    fun `deleteVisit should soft delete visit`() =
        runTest {
            // Given
            val visit = createTestVisit(id = "visit1")
            visitRepository.insertVisit(visit)

            // When
            visitRepository.deleteVisit("visit1")

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved shouldBe null
        }

    @Test
    fun `should handle place categorization fields`() =
        runTest {
            // Given
            val visit =
                createTestVisit(
                    id = "visit1",
                    category = PlaceCategory.HOME,
                    categoryConfidence = CategoryConfidence.HIGH,
                    significance = PlaceSignificance.PRIMARY
                )

            // When
            visitRepository.insertVisit(visit)

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved?.category shouldBe PlaceCategory.HOME
            retrieved?.categoryConfidence shouldBe CategoryConfidence.HIGH
            retrieved?.significance shouldBe PlaceSignificance.PRIMARY
        }

    @Test
    fun `should handle user customization fields`() =
        runTest {
            // Given
            val visit =
                createTestVisit(
                    id = "visit1",
                    userLabel = "My Office",
                    userNotes = "Where I spend most of my weekdays",
                    isFavorite = true
                )

            // When
            visitRepository.insertVisit(visit)

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved?.userLabel shouldBe "My Office"
            retrieved?.userNotes shouldBe "Where I spend most of my weekdays"
            retrieved?.isFavorite shouldBe true
        }

    @Test
    fun `should handle frequent place association`() =
        runTest {
            // Given
            val visit =
                createTestVisit(
                    id = "visit1",
                    frequentPlaceId = "frequent_place_123"
                )

            // When
            visitRepository.insertVisit(visit)

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved?.frequentPlaceId shouldBe "frequent_place_123"
        }

    @Test
    fun `should store geocoding information correctly`() =
        runTest {
            // Given
            val visit =
                createTestVisit(
                    id = "visit1",
                    approximateAddress = "123 Main St",
                    poiName = "Coffee Shop",
                    city = "Portland",
                    countryCode = "US"
                )

            // When
            visitRepository.insertVisit(visit)

            // Then
            val retrieved = visitRepository.getVisitById("visit1")
            retrieved?.approximateAddress shouldBe "123 Main St"
            retrieved?.poiName shouldBe "Coffee Shop"
            retrieved?.city shouldBe "Portland"
            retrieved?.countryCode shouldBe "US"
        }

    // Helper functions
    private fun createTestVisit(
        id: String,
        startTime: Instant = Clock.System.now(),
        endTime: Instant = Clock.System.now() + 1.hours,
        centerLatitude: Double = 37.7749,
        centerLongitude: Double = -122.4194,
        approximateAddress: String? = null,
        poiName: String? = null,
        city: String? = null,
        countryCode: String? = null,
        category: PlaceCategory = PlaceCategory.OTHER,
        categoryConfidence: CategoryConfidence = CategoryConfidence.LOW,
        significance: PlaceSignificance = PlaceSignificance.RARE,
        userLabel: String? = null,
        userNotes: String? = null,
        isFavorite: Boolean = false,
        frequentPlaceId: String? = null
    ) = PlaceVisit(
        id = id,
        startTime = startTime,
        endTime = endTime,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        approximateAddress = approximateAddress,
        poiName = poiName,
        city = city,
        countryCode = countryCode,
        locationSampleIds = emptyList(),
        category = category,
        categoryConfidence = categoryConfidence,
        significance = significance,
        userLabel = userLabel,
        userNotes = userNotes,
        isFavorite = isFavorite,
        frequentPlaceId = frequentPlaceId,
        userId = userId,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    )

    private fun createTestLocationSample(
        id: String,
        timestamp: Instant = Clock.System.now()
    ) = LocationSample(
        id = id,
        timestamp = timestamp,
        latitude = 37.7749,
        longitude = -122.4194,
        accuracy = 10.0,
        source = LocationSource.GPS,
        deviceId = deviceId,
        userId = userId
    )
}
