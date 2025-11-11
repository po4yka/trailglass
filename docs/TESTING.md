# Testing Guide

Comprehensive testing documentation for TrailGlass. This guide covers unit tests, integration tests, UI tests, and code coverage.

**Coverage Target: 75%+**

## Table of Contents

- [Overview](#overview)
- [Test Structure](#test-structure)
- [Running Tests](#running-tests)
- [Code Coverage](#code-coverage)
- [Test Types](#test-types)
- [Best Practices](#best-practices)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting](#troubleshooting)

## Overview

TrailGlass uses a comprehensive testing strategy across all platforms:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test repository and database interactions
- **UI Tests**: Test user interface on Android and iOS
- **Code Coverage**: Automated verification of 75%+ coverage

### Test Frameworks

- **Kotlin Test**: Multiplatform testing
- **JUnit**: Android unit tests
- **Espresso + Compose Test**: Android UI tests
- **XCTest**: iOS unit and UI tests
- **Kover**: Kotlin code coverage
- **Turbine**: Flow testing
- **Kotest**: Assertions library
- **MockK**: Mocking framework

## Test Structure

```
trailglass/
├── shared/
│   ├── src/
│   │   ├── commonTest/
│   │   │   ├── kotlin/
│   │   │   │   ├── TestDatabaseHelper.kt
│   │   │   │   ├── data/repository/
│   │   │   │   │   ├── PlaceVisitRepositoryTest.kt
│   │   │   │   │   ├── RouteSegmentRepositoryTest.kt
│   │   │   │   │   └── PhotoRepositoryTest.kt
│   │   │   │   └── feature/map/
│   │   │   │       └── GetMapDataUseCaseTest.kt
│   │   ├── androidUnitTest/
│   │   └── androidInstrumentedTest/
├── composeApp/
│   └── src/
│       └── androidInstrumentedTest/
│           └── kotlin/ui/screens/
│               ├── StatsScreenTest.kt
│               ├── TimelineScreenTest.kt
│               └── MapScreenTest.kt
├── iosApp/
│   └── iosAppUITests/
│       ├── StatsScreenUITests.swift
│       ├── TimelineScreenUITests.swift
│       └── MapScreenUITests.swift
└── scripts/
    ├── run-tests.sh
    └── run-ui-tests-android.sh
```

## Running Tests

### All Tests (Quick)

```bash
# Run all unit and integration tests + generate coverage
./scripts/run-tests.sh
```

### Shared Module Tests

```bash
# Common tests (multiplatform)
./gradlew :shared:test

# Android-specific tests
./gradlew :shared:testDebugUnitTest
```

### Android UI Tests

```bash
# Requires connected device or emulator
./scripts/run-ui-tests-android.sh

# Or directly:
./gradlew :composeApp:connectedAndroidTest
```

### iOS Tests

```bash
# From Xcode: Product → Test (⌘U)

# Or from command line:
xcodebuild test \
  -project iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest'
```

### Specific Test Classes

```bash
# Kotlin
./gradlew :shared:test --tests "PlaceVisitRepositoryTest"

# Android
./gradlew :shared:testDebugUnitTest --tests "*.PlaceVisitRepositoryTest"
```

## Code Coverage

### Generate Coverage Reports

```bash
# HTML + XML reports
./gradlew koverHtmlReport koverXmlReport

# Verify 75% target
./gradlew koverVerify
```

### View Coverage Reports

**HTML Report**:
```
shared/build/reports/kover/html/index.html
```

**XML Report** (for CI/CD):
```
shared/build/reports/kover/report.xml
```

### Coverage Configuration

Coverage settings are in `shared/build.gradle.kts`:

```kotlin
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.db.*", // Generated SQLDelight code
                    "*.ComposableSingletons*"
                )
            }
        }
        verify {
            rule {
                minBound(75) // Target: 75%+ coverage
            }
        }
    }
}
```

### Coverage Targets by Module

| Module | Target | Description |
|--------|--------|-------------|
| Domain Models | 90%+ | Simple data classes, high coverage expected |
| Repositories | 85%+ | Integration tests for all CRUD operations |
| Use Cases | 80%+ | Business logic tests |
| Controllers | 75%+ | State management tests |
| UI Components | 70%+ | Compose/SwiftUI tests (harder to test) |
| **Overall** | **75%+** | Project-wide minimum |

## Test Types

### 1. Unit Tests

Test individual components in isolation.

**Example: Domain Model Validation**

```kotlin
@Test
fun testCoordinateValidation() {
    // Valid coordinates
    val valid = Coordinate(48.8566, 2.3522)
    assertEquals(48.8566, valid.latitude)

    // Invalid latitude (should throw)
    assertFailsWith<IllegalArgumentException> {
        Coordinate(91.0, 0.0)  // Latitude must be -90 to 90
    }
}
```

### 2. Integration Tests

Test repository interactions with the database.

**Example: PlaceVisitRepositoryTest**

```kotlin
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
}
```

**Key Features**:
- In-memory SQLite database via `TestDatabaseHelper`
- Tests all CRUD operations
- Tests complex queries (time ranges, location search)
- Tests cascade deletes and foreign keys

### 3. Use Case Tests

Test business logic and data transformation.

**Example: GetMapDataUseCaseTest**

```kotlin
@Test
fun testExecuteWithVisitsAndRoutes() = runTest {
    // Given
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
```

### 4. Android UI Tests

Test Compose screens with Espresso and Compose Test.

**Example: StatsScreenTest**

```kotlin
@Test
fun testStatsScreen_displaysOverviewCards() {
    // Given
    composeTestRule.setContent {
        StatsScreen(controller = controller)
    }

    // Wait for data to load
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithText("Countries").assertExists()
    composeTestRule.onNodeWithText("5").assertExists()
}
```

**Test Coverage**:
- StatsScreen: Overview cards, filters, lists
- TimelineScreen: Date picker, visit/route cards, scrolling
- MapScreen: Markers, info cards, interactions

### 5. iOS UI Tests

Test iOS screens with XCTest.

**Example: StatsScreenUITests**

```swift
func testStatsOverviewCardsExist() throws {
    app.tabBars.buttons["Stats"].tap()

    XCTAssertTrue(app.staticTexts["Countries"].exists)
    XCTAssertTrue(app.staticTexts["Cities"].exists)
    XCTAssertTrue(app.staticTexts["Trips"].exists)
}
```

## Best Practices

### 1. Test Naming

Use descriptive names that explain what is being tested:

```kotlin
// Good
@Test
fun testGetPlaceVisitsForTimeRange_returnsOnlyVisitsInRange()

// Bad
@Test
fun testQuery()
```

### 2. AAA Pattern

Structure tests with Arrange-Act-Assert:

```kotlin
@Test
fun testExample() = runTest {
    // Arrange (Given)
    val visit = createTestVisit(id = "visit1")
    repository.insertPlaceVisit(visit)

    // Act (When)
    val result = repository.getPlaceVisitById(visit.id, userId)

    // Assert (Then)
    assertNotNull(result)
    assertEquals(visit.id, result.id)
}
```

### 3. Test Independence

Each test should be independent and repeatable:

```kotlin
@BeforeTest
fun setup() {
    TestDatabaseHelper.clearDatabase(database)
    repository = PlaceVisitRepositoryImpl(database)
}

@AfterTest
fun teardown() {
    TestDatabaseHelper.clearDatabase(database)
}
```

### 4. Use Test Helpers

Create reusable test utilities:

```kotlin
// TestDatabaseHelper.kt
object TestDatabaseHelper {
    fun createTestDatabase(): TrailGlassDatabase {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TrailGlassDatabase.Schema.create(driver)
        return TrailGlassDatabase(driver)
    }
}
```

### 5. Test Edge Cases

Don't just test the happy path:

```kotlin
@Test
fun testGetPlaceVisitById_returnsNullForNonexistent() = runTest {
    val result = repository.getPlaceVisitById("nonexistent", userId)
    assertNull(result)
}

@Test
fun testGetPlaceVisitsForTimeRange_emptyRange() = runTest {
    val result = repository.getPlaceVisitsForTimeRange(
        userId,
        start = now,
        end = now  // Empty range
    ).first()
    assertTrue(result.isEmpty())
}
```

### 6. Mock External Dependencies

Use MockK for Android-specific dependencies:

```kotlin
val mockContext = mockk<Context>()
every { mockContext.getSystemService(any()) } returns mockLocationManager
```

### 7. Test Asynchronous Code

Use proper coroutine test utilities:

```kotlin
@Test
fun testFlowEmission() = runTest {
    repository.getPlaceVisitsForTimeRange(userId, start, end)
        .test {
            val items = awaitItem()
            assertEquals(2, items.size)
            awaitComplete()
        }
}
```

## CI/CD Integration

### GitHub Actions

Example workflow for running tests:

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Grant execute permission
        run: chmod +x gradlew

      - name: Run tests
        run: ./gradlew test

      - name: Generate coverage
        run: ./gradlew koverXmlReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./shared/build/reports/kover/report.xml

      - name: Verify coverage threshold
        run: ./gradlew koverVerify
```

### Pre-commit Hook

Add to `.git/hooks/pre-commit`:

```bash
#!/bin/bash

echo "Running tests before commit..."
./gradlew :shared:test

if [ $? -ne 0 ]; then
    echo "Tests failed! Commit aborted."
    exit 1
fi

echo "Tests passed!"
```

## Troubleshooting

### Tests Fail to Find Database

**Problem**: Tests can't create in-memory database

**Solution**: Ensure `sqldelight-sqlite` dependency is in `commonTest`:

```kotlin
commonTest.dependencies {
    implementation(libs.sqldelight.sqlite)
}
```

### Android UI Tests Fail

**Problem**: `No connected devices found`

**Solution**: Start an emulator or connect a device:

```bash
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd Pixel_5_API_33 &
```

### iOS Tests Don't Run

**Problem**: Xcode can't find test target

**Solution**:
1. Open iosApp.xcodeproj in Xcode
2. File → Packages → Reset Package Caches
3. Product → Clean Build Folder (⇧⌘K)
4. Build for Testing (⇧⌘U)

### Coverage Too Low

**Problem**: Coverage is below 75%

**Solution**:
1. Identify uncovered code:
   ```bash
   ./gradlew koverHtmlReport
   # Open: shared/build/reports/kover/html/index.html
   ```

2. Add tests for uncovered areas:
   - Red: Not covered
   - Yellow: Partially covered
   - Green: Fully covered

3. Focus on high-value areas first (use cases, repositories)

### Flaky Tests

**Problem**: Tests pass sometimes, fail other times

**Solution**:
- Remove hard-coded waits: Use `waitForIdle()` or `waitForExistence()`
- Ensure test independence: Clear database between tests
- Check for race conditions: Use proper coroutine test utilities
- Avoid shared mutable state

### Out of Memory

**Problem**: Tests fail with OOM errors

**Solution**: Increase JVM memory in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

## Test Data Fixtures

### Example Test Fixtures

```kotlin
// TestFixtures.kt
object TestFixtures {
    fun createTestVisit(
        id: String = "visit1",
        city: String = "Paris",
        latitude: Double = 48.8566,
        longitude: Double = 2.3522
    ) = PlaceVisit(
        id = id,
        tripId = "trip1",
        userId = "test_user",
        startTime = Instant.parse("2024-01-01T10:00:00Z"),
        endTime = Instant.parse("2024-01-01T12:00:00Z"),
        centerLatitude = latitude,
        centerLongitude = longitude,
        radiusMeters = 100.0,
        city = city,
        country = "France",
        approximateAddress = "123 Main St",
        confidence = 0.95,
        arrivalTransportType = TransportType.WALK,
        departureTransportType = TransportType.CAR,
        userNotes = null
    )
}
```

## Performance Testing

### Measure Test Execution Time

```bash
# With timing
./gradlew test --profile

# Report: build/reports/profile/profile-*.html
```

### iOS Performance Tests

```swift
func testMapLoadingPerformance() throws {
    measure(metrics: [XCTApplicationLaunchMetric()]) {
        let app = XCUIApplication()
        app.launch()
        app.tabBars.buttons["Map"].tap()
    }
}
```

## Resources

- [Kotlin Test Documentation](https://kotlinlang.org/api/latest/kotlin.test/)
- [Compose Testing Cheat Sheet](https://developer.android.com/jetpack/compose/testing-cheatsheet)
- [XCTest Documentation](https://developer.apple.com/documentation/xctest)
- [Kover Plugin](https://github.com/Kotlin/kotlinx-kover)
- [Turbine Flow Testing](https://github.com/cashapp/turbine)
- [Kotest Assertions](https://kotest.io/docs/assertions/assertions.html)

## Test Metrics

Track these metrics over time:

- **Test Count**: Total number of tests
- **Coverage**: Percentage of code covered
- **Execution Time**: How long tests take
- **Failure Rate**: Percentage of failed test runs
- **Flakiness**: Tests that fail intermittently

### Current Status

```
Total Tests: 30+
Coverage Target: 75%+
Platforms: Android, iOS, JVM
Frameworks: Kotlin Test, JUnit, Espresso, XCTest
```

---

**Remember**: Tests are documentation. Write tests that explain how your code should work, and maintain them as carefully as production code.
