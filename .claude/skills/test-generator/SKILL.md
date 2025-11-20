---
name: test-generator
description: Generates comprehensive tests for Kotlin Multiplatform code with 75%+ coverage. Use when creating tests for repositories, use cases, controllers, or verifying test coverage.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
  - Bash
---

# Test Generator

Automated test generation for Trailglass Kotlin Multiplatform project ensuring 75%+ code coverage.

## Coverage Requirements

### Target Thresholds (Enforced by Kover)
- **Overall Project:** 75%+ (hard requirement)
- **Domain Models:** 90%+
- **Repositories:** 85%+
- **Use Cases:** 80%+
- **Controllers:** 75%+
- **UI Components:** 70%+

### Coverage Commands
```bash
# Generate coverage report
./gradlew koverHtmlReport
# View: shared/build/reports/kover/html/index.html

# Verify threshold
./gradlew koverVerify

# Run all tests
./scripts/run-tests.sh
```

## Test Types

### 1. Repository Tests

**Location:** `shared/src/commonTest/kotlin/data/repository/`

**Pattern:**
```kotlin
class PlaceVisitRepositoryTest {
    private lateinit var database: TrailGlassDatabase
    private lateinit var repository: PlaceVisitRepository
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        database = TestDatabaseHelper.createTestDatabase()
        repository = PlaceVisitRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
        database.close()
    }

    @Test
    fun testInsertAndRetrieve_returnsInsertedVisit() = runTest {
        // Arrange
        val visit = createTestVisit(id = "visit1", city = "Paris")

        // Act
        repository.insertPlaceVisit(visit)
        val result = repository.getPlaceVisitById(visit.id, userId)

        // Assert
        assertNotNull(result)
        assertEquals(visit.id, result.id)
        assertEquals(visit.city, result.city)
    }

    @Test
    fun testGetById_nonexistent_returnsNull() = runTest {
        // Arrange
        // No setup needed

        // Act
        val result = repository.getPlaceVisitById("nonexistent", userId)

        // Assert
        assertNull(result)
    }

    @Test
    fun testGetForTimeRange_filtersCorrectly() = runTest {
        // Arrange
        val visit1 = createTestVisit(
            id = "v1",
            startTime = Instant.parse("2024-01-01T10:00:00Z")
        )
        val visit2 = createTestVisit(
            id = "v2",
            startTime = Instant.parse("2024-01-05T10:00:00Z")
        )
        val visit3 = createTestVisit(
            id = "v3",
            startTime = Instant.parse("2024-01-10T10:00:00Z")
        )
        repository.insertPlaceVisit(visit1)
        repository.insertPlaceVisit(visit2)
        repository.insertPlaceVisit(visit3)

        // Act
        val results = repository.getPlaceVisitsForTimeRange(
            userId = userId,
            startTime = Instant.parse("2024-01-03T00:00:00Z"),
            endTime = Instant.parse("2024-01-08T00:00:00Z")
        ).first()

        // Assert
        assertEquals(1, results.size)
        assertEquals("v2", results[0].id)
    }

    @Test
    fun testSoftDelete_marksAsDeleted() = runTest {
        // Arrange
        val visit = createTestVisit(id = "visit1")
        repository.insertPlaceVisit(visit)

        // Act
        repository.softDeletePlaceVisit(visit.id, userId)

        // Assert
        val result = repository.getPlaceVisitById(visit.id, userId)
        assertNull(result)  // Should not return deleted visits
    }

    @Test
    fun testFlowEmission_reactsToChanges() = runTest {
        // Arrange
        val startTime = Instant.parse("2024-01-01T00:00:00Z")
        val endTime = Instant.parse("2024-12-31T23:59:59Z")

        // Act & Assert
        repository.getPlaceVisitsForTimeRange(userId, startTime, endTime)
            .test {
                // Initial empty state
                val initial = awaitItem()
                assertTrue(initial.isEmpty())

                // Insert visit
                repository.insertPlaceVisit(createTestVisit(id = "v1"))

                // Should emit updated list
                val updated = awaitItem()
                assertEquals(1, updated.size)

                cancelAndIgnoreRemainingEvents()
            }
    }
}
```

**Required Test Cases:**
- Insert and retrieve
- Get by ID (exists and doesn't exist)
- Update operations
- Delete operations (soft delete)
- Time range queries
- Flow emissions
- Edge cases (empty results, large datasets)
- Concurrent operations

### 2. Use Case Tests

**Location:** `shared/src/commonTest/kotlin/feature/*/`

**Pattern:**
```kotlin
class GetMapDataUseCaseTest {
    private lateinit var database: TrailGlassDatabase
    private lateinit var visitRepository: PlaceVisitRepository
    private lateinit var routeRepository: RouteSegmentRepository
    private lateinit var useCase: GetMapDataUseCase
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        database = TestDatabaseHelper.createTestDatabase()
        visitRepository = PlaceVisitRepositoryImpl(database)
        routeRepository = RouteSegmentRepositoryImpl(database)
        useCase = GetMapDataUseCase(visitRepository, routeRepository)
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @Test
    fun testExecute_withVisitsAndRoutes_returnsMapData() = runTest {
        // Arrange
        val visit1 = createTestVisit(
            id = "v1",
            city = "Paris",
            latitude = 48.8566,
            longitude = 2.3522
        )
        val visit2 = createTestVisit(
            id = "v2",
            city = "London",
            latitude = 51.5074,
            longitude = -0.1278
        )
        visitRepository.insertPlaceVisit(visit1)
        visitRepository.insertPlaceVisit(visit2)

        val route = createTestRoute(
            id = "r1",
            fromLat = visit1.centerLatitude,
            fromLon = visit1.centerLongitude,
            toLat = visit2.centerLatitude,
            toLon = visit2.centerLongitude,
            transportType = TransportType.TRAIN
        )
        routeRepository.insertRouteSegment(route)

        // Act
        val result = useCase.execute(
            userId = userId,
            startTime = Instant.DISTANT_PAST,
            endTime = Instant.DISTANT_FUTURE
        )

        // Assert
        when (result) {
            is Result.Success -> {
                val mapData = result.value
                assertEquals(2, mapData.markers.size)
                assertEquals(1, mapData.routes.size)
                assertNotNull(mapData.boundingRegion)
            }
            is Result.Failure -> fail("Expected success, got failure: ${result.error}")
        }
    }

    @Test
    fun testExecute_emptyData_returnsEmptyMapData() = runTest {
        // Act
        val result = useCase.execute(
            userId = userId,
            startTime = Instant.DISTANT_PAST,
            endTime = Instant.DISTANT_FUTURE
        )

        // Assert
        when (result) {
            is Result.Success -> {
                val mapData = result.value
                assertTrue(mapData.markers.isEmpty())
                assertTrue(mapData.routes.isEmpty())
            }
            is Result.Failure -> fail("Should succeed with empty data")
        }
    }

    @Test
    fun testExecute_databaseError_returnsFailure() = runTest {
        // Arrange
        database.close()  // Force error

        // Act
        val result = useCase.execute(userId, Instant.DISTANT_PAST, Instant.DISTANT_FUTURE)

        // Assert
        assertTrue(result is Result.Failure)
    }
}
```

**Required Test Cases:**
- Happy path with valid data
- Empty/no data scenarios
- Error conditions
- Boundary conditions
- Result type verification (Success/Failure)

### 3. Controller Tests

**Location:** `shared/src/commonTest/kotlin/feature/*/`

**Pattern:**
```kotlin
class TimelineControllerTest {
    private lateinit var database: TrailGlassDatabase
    private lateinit var repository: PlaceVisitRepository
    private lateinit var useCase: GetTimelineForDayUseCase
    private lateinit var controller: TimelineController
    private lateinit var testScope: TestScope
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        database = TestDatabaseHelper.createTestDatabase()
        repository = PlaceVisitRepositoryImpl(database)
        useCase = GetTimelineForDayUseCase(repository)
        testScope = TestScope()
        controller = TimelineController(useCase, testScope, userId)
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @Test
    fun testInitialState_isDefault() {
        // Assert
        val state = controller.state.value
        assertNull(state.selectedDate)
        assertTrue(state.items.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun testSelectDate_loadsData() = runTest {
        // Arrange
        val visit = createTestVisit(
            startTime = Instant.parse("2024-01-01T10:00:00Z")
        )
        repository.insertPlaceVisit(visit)
        val date = LocalDate(2024, 1, 1)

        // Act
        controller.selectDate(date)
        testScope.advanceUntilIdle()

        // Assert
        val state = controller.state.value
        assertEquals(date, state.selectedDate)
        assertFalse(state.isLoading)
        assertEquals(1, state.items.size)
        assertNull(state.error)
    }

    @Test
    fun testSelectDate_setsLoadingState() = runTest {
        // Arrange
        val date = LocalDate(2024, 1, 1)

        // Act
        controller.selectDate(date)

        // Assert (check immediately before coroutine completes)
        val state = controller.state.value
        assertEquals(date, state.selectedDate)
        assertTrue(state.isLoading)
    }

    @Test
    fun testSelectDate_error_setsErrorState() = runTest {
        // Arrange
        database.close()  // Force error
        val date = LocalDate(2024, 1, 1)

        // Act
        controller.selectDate(date)
        testScope.advanceUntilIdle()

        // Assert
        val state = controller.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun testStateFlow_emitsUpdates() = runTest {
        // Act & Assert
        controller.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.items.isEmpty())

            controller.selectDate(LocalDate(2024, 1, 1))

            // Loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // Skip to final state
            testScope.advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertFalse(finalState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**Required Test Cases:**
- Initial state verification
- Action triggering state changes
- Loading state handling
- Error state handling
- StateFlow emission verification

## Test Utilities

### TestDatabaseHelper

```kotlin
object TestDatabaseHelper {
    fun createTestDatabase(): TrailGlassDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TrailGlassDatabase.Schema.create(driver)
        return TrailGlassDatabase(driver)
    }

    fun clearDatabase(database: TrailGlassDatabase) {
        database.transaction {
            database.locationSamplesQueries.deleteAll()
            database.placeVisitsQueries.deleteAll()
            database.routeSegmentsQueries.deleteAll()
            database.tripsQueries.deleteAll()
            database.photosQueries.deleteAll()
            database.photoAttachmentsQueries.deleteAll()
        }
    }
}
```

### Test Fixtures

```kotlin
object TestFixtures {
    fun createTestVisit(
        id: String = UUID.randomUUID().toString(),
        userId: String = "test_user",
        tripId: String = "test_trip",
        city: String = "Paris",
        country: String = "France",
        latitude: Double = 48.8566,
        longitude: Double = 2.3522,
        startTime: Instant = Clock.System.now(),
        endTime: Instant = Clock.System.now().plus(2.hours)
    ) = PlaceVisit(
        id = id,
        tripId = tripId,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        centerLatitude = latitude,
        centerLongitude = longitude,
        radiusMeters = 100.0,
        city = city,
        country = country,
        countryCode = "FR",
        approximateAddress = "123 Main St",
        poiName = null,
        confidence = 0.95,
        arrivalTransportType = TransportType.WALK,
        departureTransportType = null,
        userNotes = null
    )

    fun createTestRoute(
        id: String = UUID.randomUUID().toString(),
        userId: String = "test_user",
        tripId: String = "test_trip",
        fromLat: Double = 48.8566,
        fromLon: Double = 2.3522,
        toLat: Double = 51.5074,
        toLon: Double = -0.1278,
        transportType: TransportType = TransportType.TRAIN
    ) = RouteSegment(
        id = id,
        userId = userId,
        tripId = tripId,
        startTime = Clock.System.now(),
        endTime = Clock.System.now().plus(2.hours),
        startLatitude = fromLat,
        startLongitude = fromLon,
        endLatitude = toLat,
        endLongitude = toLon,
        transportType = transportType,
        distanceMeters = 500000.0,
        pathPoints = emptyList()
    )
}
```

## Test Generation Process

### 1. Identify Untested Code

```bash
# Generate coverage report
./gradlew koverHtmlReport

# Open report
# shared/build/reports/kover/html/index.html

# Look for red (uncovered) code
```

### 2. Generate Tests

For each uncovered class:
1. Determine test type (Repository/UseCase/Controller)
2. Create test file in appropriate location
3. Write setup/teardown
4. Generate test cases for each method
5. Include edge cases and error conditions

### 3. Verify Coverage

```bash
# Run new tests
./gradlew :shared:test

# Check coverage improved
./gradlew koverVerify
```

## Best Practices

### Test Naming
```kotlin
// Pattern: test<Method>_<Condition>_<ExpectedResult>
@Test
fun testGetPlaceVisitById_existingId_returnsVisit()

@Test
fun testGetPlaceVisitById_nonexistentId_returnsNull()

@Test
fun testInsertPlaceVisit_duplicateId_throwsException()
```

### AAA Pattern
```kotlin
@Test
fun testExample() = runTest {
    // Arrange: Setup test data
    val data = createTestData()

    // Act: Execute the operation
    val result = operation(data)

    // Assert: Verify outcome
    assertEquals(expected, result)
}
```

### Test Independence
```kotlin
@BeforeTest
fun setup() {
    // Fresh state for each test
    database = TestDatabaseHelper.createTestDatabase()
}

@AfterTest
fun teardown() {
    // Clean up after each test
    database.close()
}
```

### Edge Cases
- Empty inputs
- Null values (where allowed)
- Boundary values (min/max)
- Large datasets
- Concurrent access
- Error conditions

## Verification

After generating tests:
```bash
# Run all tests
./gradlew :shared:test

# Check coverage
./gradlew koverHtmlReport

# Verify threshold met
./gradlew koverVerify

# Should show ≥75% coverage
```

## Related Documentation

- `docs/TESTING.md` - Testing strategy
- `AGENTS.md` - Testing requirements
- `.cursor/rules/testing.mdc` - Testing rules

---

*Use this skill to generate comprehensive tests maintaining 75%+ coverage for Trailglass.*
