# Trailglass - Gemini CLI Context

You are an expert Kotlin Multiplatform developer working on Trailglass, a privacy-focused cross-platform travel logging app for iOS and Android.

## Project Overview

Trailglass is a privacy-respectful travel logging application that:
- Tracks location in the background using platform-native APIs
- Detects place visits and routes using clustering algorithms (DBSCAN)
- Allows users to attach photos and journal entries to locations
- Provides timeline and map visualizations of trips
- Supports cross-device sync (planned) with end-to-end encryption

**License**: Non-Commercial Open Software License (NC-OSL) by Nikita Pochaev

## Technology Stack

### Core
- **Language**: Kotlin 2.2.20
- **Architecture**: Kotlin Multiplatform (KMP) with Clean Architecture
- **Database**: SQLDelight 2.1.0 (type-safe, cross-platform)
- **DI**: kotlin-inject 0.7.2 (compile-time via KSP)
- **Networking**: Ktor 3.3.2
- **Logging**: kotlin-logging 7.0.0
- **Serialization**: kotlinx-serialization 1.7.3

### Android
- **UI**: Jetpack Compose with Material 3 Expressive
- **Navigation**: Decompose 3.2.0
- **Location**: FusedLocationProviderClient
- **Images**: Coil 2.7.0
- **Maps**: Google Maps Compose 6.2.0
- **Background**: WorkManager 2.10.0, Foreground Service

### iOS
- **UI**: SwiftUI
- **Location**: CLLocationManager
- **Images**: Photos Framework
- **Maps**: MapKit

### Testing
- **Framework**: Kotlin Test, JUnit, XCTest
- **Coverage**: Kover (75%+ enforced)
- **Assertions**: Kotest 5.9.1
- **Flow Testing**: Turbine 1.1.0
- **Mocking**: MockK 1.13.12

## Project Structure

```
shared/                    # Kotlin Multiplatform module
├── commonMain/           # Platform-agnostic code (business logic)
│   ├── kotlin/
│   │   ├── domain/      # Domain models, repository interfaces
│   │   ├── data/        # Repository implementations
│   │   ├── feature/     # Controllers (StateFlow) and use cases
│   │   ├── location/    # Location processing (clustering, geocoding)
│   │   ├── di/          # Dependency injection (kotlin-inject)
│   │   └── logging/     # Structured logging
│   └── sqldelight/      # Database schema (.sq files)
├── androidMain/          # Android platform implementations
│   └── kotlin/
│       ├── location/    # FusedLocationProvider, Foreground Service
│       ├── photo/       # MediaStore integration
│       └── di/          # Android DI module
├── iosMain/              # iOS platform implementations
│   └── kotlin/
│       ├── location/    # CLLocationManager wrapper
│       ├── photo/       # Photos framework wrapper
│       └── di/          # iOS DI module
└── commonTest/           # Shared tests
composeApp/               # Android app (Jetpack Compose UI)
iosApp/                   # iOS app (SwiftUI)
docs/                     # Project documentation
scripts/                  # Build and test scripts
```

## Architecture Principles

### Layer Separation (Clean Architecture)
Follow strict unidirectional flow:
```
UI (Platform) → Controllers (Shared) → Use Cases (Shared) →
Repositories (Shared) → Database (Shared) → Platform APIs (expect/actual)
```

**Never skip layers or reverse dependencies.**

### Key Patterns
- **State Management**: StateFlow for reactive UI state (immutable updates with `.copy()`)
- **Platform Code**: expect/actual pattern for platform-specific implementations
- **Error Handling**: Sealed class hierarchy with `Result<T>` wrapper
- **Dependency Injection**: Constructor injection with `@Inject` annotation
- **Repository Pattern**: Interfaces in `domain/`, implementations in `data/`

## Coding Standards

### Kotlin Style
- **Classes**: PascalCase (e.g., `PlaceVisit`, `LocationRepository`)
- **Functions**: camelCase (e.g., `getUserId`, `processLocations`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRIES`)
- **Composables**: PascalCase (e.g., `TimelineScreen`, `VisitCard`)
- **Immutability**: Always prefer `val` over `var`, use data classes with `copy()`

### State Management Pattern
```kotlin
class MyController @Inject constructor(
    private val useCase: MyUseCase,
    @AppScope private val coroutineScope: CoroutineScope
) {
    data class State(
        val data: MyData? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun action() {
        _state.update { it.copy(isLoading = true) }
        coroutineScope.launch {
            // Business logic
        }
    }
}
```

### Platform-Specific Code (expect/actual)
```kotlin
// commonMain
expect class PlatformService {
    suspend fun doWork(): Result<Data>
}

// androidMain
actual class PlatformService(private val context: Context) {
    actual suspend fun doWork(): Result<Data> { /* Android impl */ }
}

// iosMain
actual class PlatformService {
    actual suspend fun doWork(): Result<Data> { /* iOS impl */ }
}
```

### Logging
Always use kotlin-logging with lazy evaluation:
```kotlin
private val logger = logger()  // Automatically uses class name

logger.info { "Important event" }
logger.debug { "Details: $data" }
logger.error(exception) { "Operation failed" }
```

## Database (SQLDelight)

### Schema Files
Located in `shared/src/commonMain/sqldelight/com/po4yka/trailglass/db/`

Tables:
- `location_samples` - Raw GPS points
- `place_visits` - Detected stationary periods with geocoding
- `route_segments` - Movement between places
- `trips` - Multi-day journeys
- `photos` - Photo metadata
- `photo_attachments` - Links photos to visits
- `geocoding_cache` - Persistent reverse geocoding cache

### After Schema Changes
```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
./gradlew clean build
```

### Query Best Practices
- Use named queries with clear purpose
- Always parameterize (avoid string concatenation)
- Create indices for WHERE/ORDER BY columns
- Use soft deletes (deleted_at column)
- Wrap multiple operations in transactions

## Build & Test Commands

### Build
```bash
# Android
./gradlew :composeApp:installDebug
./gradlew :composeApp:assembleDebug

# iOS framework
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# Full clean build
./gradlew clean build
```

### Testing
```bash
# All tests with coverage
./scripts/run-tests.sh

# Shared module tests
./gradlew :shared:test

# Android UI tests (requires emulator)
./gradlew :composeApp:connectedAndroidTest

# Coverage report (75%+ required)
./gradlew koverHtmlReport
# View: shared/build/reports/kover/html/index.html

# Verify coverage threshold
./gradlew koverVerify
```

### Code Generation
```bash
# After SQLDelight changes
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface

# After kotlin-inject DI changes
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
```

## Testing Requirements

### Coverage Targets (Enforced by Kover)
- Overall: 75%+ (enforced)
- Domain models: 90%+
- Repositories: 85%+
- Use cases: 80%+
- Controllers: 75%+

### Test Structure (AAA Pattern)
```kotlin
@Test
fun testMethodName_condition_expectedResult() = runTest {
    // Arrange (Given)
    val testData = createTestData()
    repository.insert(testData)

    // Act (When)
    val result = repository.query()

    // Assert (Then)
    assertNotNull(result)
    assertEquals(expected, result)
}
```

### Test Requirements
- Use descriptive test names that explain what/when/why
- Test both happy path and edge cases
- Ensure test independence with @BeforeTest/@AfterTest
- Use TestDatabaseHelper for in-memory database
- Run tests before every commit

## Common Tasks

### Adding New Feature
1. Create domain model in `domain/model/`
2. Create repository interface in `domain/repository/`
3. Implement repository in `data/repository/impl/`
4. Create use case in `feature/*/`
5. Create controller with StateFlow in `feature/*/`
6. Add DI bindings in appropriate module
7. Create UI screen (Compose/SwiftUI)
8. Write tests (75%+ coverage)
9. Update documentation

### Modifying Database Schema
1. Edit `.sq` file in `sqldelight/`
2. Run: `./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface`
3. Update repository implementation
4. Update tests
5. Create migration if needed
6. Run: `./gradlew clean build`

### Adding Platform-Specific Code
1. Create `expect` declaration in `commonMain/`
2. Create `actual` implementation in `androidMain/`
3. Create `actual` implementation in `iosMain/`
4. Test on both platforms
5. Document platform differences

## Error Patterns to Avoid

### ❌ Bad Practices
- Skipping architecture layers
- Exposing MutableStateFlow publicly
- Using GlobalScope or unstructured coroutines
- Hardcoding values (use constants/config)
- Ignoring test coverage
- Mutating state directly (use `.copy()`)
- Raw SQL strings (use SQLDelight)
- Manual DI (use kotlin-inject)

### ✅ Good Practices
- Follow layer separation strictly
- Immutable state with StateFlow
- Structured concurrency with scopes
- Comprehensive error handling
- 75%+ test coverage
- Logging with lazy evaluation
- Type-safe database queries
- Compile-time dependency injection

## Troubleshooting

### "Cannot resolve symbol generated by SQLDelight"
```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
```

### "Framework not found Shared" (iOS)
```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### "Unsatisfied dependencies" (kotlin-inject)
```bash
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
```

### Coverage verification failed
```bash
./gradlew koverHtmlReport
# Review uncovered code in: shared/build/reports/kover/html/index.html
```

## Documentation

Comprehensive docs in `docs/`:
- `ARCHITECTURE.md` - System architecture and patterns
- `DEVELOPMENT.md` - Setup, building, debugging
- `TESTING.md` - Testing strategy and coverage
- `ERROR_HANDLING.md` - Error handling patterns
- `LOCATION_TRACKING.md` - Platform location tracking
- `UI_IMPLEMENTATION.md` - UI architecture
- `dependency-injection.md` - DI setup with kotlin-inject

## Security Considerations

- Never commit API keys (use `local.properties`)
- Don't log coordinates at INFO level (DEBUG/TRACE only)
- Use encrypted preferences for tokens
- Validate all user input
- Review platform permissions carefully

## Verification Checklist

Before committing code:
- [ ] Tests pass: `./gradlew :shared:test`
- [ ] Coverage ≥75%: `./gradlew koverVerify`
- [ ] Android builds: `./gradlew :composeApp:assembleDebug`
- [ ] iOS framework: `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
- [ ] No warnings or errors
- [ ] Architecture layers respected
- [ ] New code has tests
- [ ] Documentation updated

## Additional Context

Reference `CLAUDE.md` for detailed guidance on:
- Specific build commands and workflows
- Detailed architecture explanations
- Platform-specific implementation details
- Common error resolutions

For detailed coding rules, see `.cursor/rules/`:
- `kotlin-multiplatform.mdc` - KMP development patterns
- `sqldelight.mdc` - Database guidelines
- `android-compose.mdc` - Android UI patterns
- `testing.mdc` - Testing best practices
- `project-conventions.mdc` - General conventions
