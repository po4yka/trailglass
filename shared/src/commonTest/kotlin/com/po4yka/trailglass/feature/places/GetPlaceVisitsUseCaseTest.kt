package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.impl.PlaceVisitRepositoryImpl
import com.po4yka.trailglass.di.FakeUserSession
import com.po4yka.trailglass.domain.model.PlaceVisit
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class GetPlaceVisitsUseCaseTest {
    private lateinit var useCase: GetPlaceVisitsUseCase
    private lateinit var repository: PlaceVisitRepository
    private val database = TestDatabaseHelper.createTestDatabase()
    private val userId = "test_user_123"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        repository = PlaceVisitRepositoryImpl(database, FakeUserSession(userId))
        useCase = GetPlaceVisitsUseCase(repository)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun `execute with time range should return visits within range sorted by start time descending`() =
        runTest {
            // Given
            val baseTime = Clock.System.now().truncateToMillis()
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
                    startTime = baseTime + 2.hours,
                    endTime = baseTime + 3.hours
                )

            repository.insertVisit(visit1)
            repository.insertVisit(visit2)
            repository.insertVisit(visit3)

            // When
            val result =
                useCase.execute(
                    userId = userId,
                    startTime = baseTime,
                    endTime = baseTime + 2.5.hours
                )

            // Then
            result.isSuccess shouldBe true
            val visits = result.getOrThrow()
            visits.size shouldBe 2

            // Should be sorted by start time descending
            visits[0].id shouldBe "visit2" // Most recent first
            visits[1].id shouldBe "visit1"
        }

    @Test
    fun `execute with time range should return empty list when no visits in range`() =
        runTest {
            // Given
            val baseTime = Clock.System.now().truncateToMillis()
            val visit =
                createTestVisit(
                    id = "visit1",
                    startTime = baseTime + 5.hours,
                    endTime = baseTime + 6.hours
                )
            repository.insertVisit(visit)

            // When
            val result =
                useCase.execute(
                    userId = userId,
                    startTime = baseTime,
                    endTime = baseTime + 1.hours
                )

            // Then
            result.isSuccess shouldBe true
            result.getOrThrow().isEmpty() shouldBe true
        }

    @Test
    fun `execute with pagination should return limited visits sorted descending`() =
        runTest {
            // Given
            val baseTime = Clock.System.now().truncateToMillis()
            repeat(10) { index ->
                val visit =
                    createTestVisit(
                        id = "visit_$index",
                        startTime = baseTime + (index * 10).minutes
                    )
                repository.insertVisit(visit)
            }

            // When
            val result = useCase.execute(userId = userId, limit = 5, offset = 0)

            // Then
            result.isSuccess shouldBe true
            val visits = result.getOrThrow()
            visits.size shouldBe 5

            // Should be sorted by start time descending
            visits[0].startTime shouldBe baseTime + 90.minutes
            visits[4].startTime shouldBe baseTime + 50.minutes
        }

    @Test
    fun `execute with pagination should support offset`() =
        runTest {
            // Given
            val baseTime = Clock.System.now().truncateToMillis()
            repeat(10) { index ->
                val visit =
                    createTestVisit(
                        id = "visit_$index",
                        startTime = baseTime + (index * 10).minutes
                    )
                repository.insertVisit(visit)
            }

            // When - Get second page
            val result = useCase.execute(userId = userId, limit = 5, offset = 5)

            // Then
            result.isSuccess shouldBe true
            val visits = result.getOrThrow()
            visits.size shouldBe 5

            // Should be different from first page
            visits[0].startTime shouldBe baseTime + 40.minutes
            visits[4].startTime shouldBe baseTime
        }

    @Test
    fun `execute should use default pagination values`() =
        runTest {
            // Given
            val baseTime = Clock.System.now().truncateToMillis()
            repeat(60) { index ->
                val visit =
                    createTestVisit(
                        id = "visit_$index",
                        startTime = baseTime + (index * 10).minutes
                    )
                repository.insertVisit(visit)
            }

            // When - Call without explicit limit/offset (should use defaults: limit=50, offset=0)
            val result = useCase.execute(userId = userId)

            // Then
            result.isSuccess shouldBe true
            val visits = result.getOrThrow()
            visits.size shouldBe 50 // Default limit
        }

    @Test
    fun `execute should return empty list when user has no visits`() =
        runTest {
            // When
            val result =
                useCase.execute(
                    userId = "user_with_no_visits",
                    startTime = Clock.System.now(),
                    endTime = Clock.System.now() + 1.hours
                )

            // Then
            result.isSuccess shouldBe true
            result.getOrThrow().isEmpty() shouldBe true
        }

    @Test
    fun `execute should handle single visit correctly`() =
        runTest {
            // Given
            val baseTime = Clock.System.now().truncateToMillis()
            val visit =
                createTestVisit(
                    id = "single_visit",
                    startTime = baseTime,
                    endTime = baseTime + 1.hours
                )
            repository.insertVisit(visit)

            // When
            val result =
                useCase.execute(
                    userId = userId,
                    startTime = baseTime - 1.hours,
                    endTime = baseTime + 2.hours
                )

            // Then
            result.isSuccess shouldBe true
            val visits = result.getOrThrow()
            visits.size shouldBe 1
            visits[0].id shouldBe "single_visit"
        }

    // Helper function
    private fun createTestVisit(
        id: String,
        startTime: Instant,
        endTime: Instant = startTime + 1.hours
    ) = PlaceVisit(
        id = id,
        startTime = startTime.truncateToMillis(),
        endTime = endTime.truncateToMillis(),
        centerLatitude = 37.7749,
        centerLongitude = -122.4194,
        userId = userId,
        createdAt = Clock.System.now().truncateToMillis(),
        updatedAt = Clock.System.now().truncateToMillis()
    )

    // Truncate Instant to millisecond precision to match database storage
    private fun Instant.truncateToMillis(): Instant = Instant.fromEpochMilliseconds(this.toEpochMilliseconds())
}
