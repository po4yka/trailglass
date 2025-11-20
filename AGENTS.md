# Trailglass Agent Instructions

> Instructions for AI coding agents working on Trailglass, a privacy-focused cross-platform travel logging app.

## Project Overview

**Trailglass** is a Kotlin Multiplatform application for iOS and Android that enables privacy-respectful travel logging with background location tracking, automatic trip detection, and photo/journal integration.

**Key Features:**
- Background location tracking using platform-native APIs
- Automatic place visit and route detection (DBSCAN clustering)
- Photo attachment with smart time/location matching
- Timeline and map visualizations
- Cross-device sync with end-to-end encryption (planned)

**License:** Non-Commercial Open Software License (NC-OSL)

## Technology Stack

### Core (Kotlin Multiplatform)
- Kotlin 2.2.20
- SQLDelight 2.1.0 (type-safe cross-platform database)
- kotlin-inject 0.7.2 (compile-time DI via KSP)
- Ktor 3.3.2 (HTTP client)
- kotlin-logging 7.0.0 (structured logging)
- kotlinx-serialization 1.7.3
- kotlinx-coroutines 1.10.2

### Android
- Jetpack Compose with Material 3 Expressive
- Decompose 3.2.0 (navigation)
- FusedLocationProviderClient (location)
- Coil 2.7.0 (image loading)
- Google Maps Compose 6.2.0
- WorkManager 2.10.0 (background tasks)

### iOS
- SwiftUI
- CLLocationManager (location)
- Photos Framework
- MapKit

### Testing & Quality
- Kover (75%+ coverage enforced)
- Kotlin Test, JUnit, XCTest
- Kotest 5.9.1 (assertions)
- Turbine 1.1.0 (Flow testing)
- MockK 1.13.12

## Architecture

### Clean Architecture Layers

Follow strict unidirectional dependency flow:

```
Platform UI (Compose/SwiftUI)
    ↓
Controllers (StateFlow-based, Shared)
    ↓
Use Cases (Business logic, Shared)
    ↓
Repositories (Data access, Shared)
    ↓
Database (SQLDelight, Shared)
    ↓
Platform APIs (expect/actual)
```

**Critical Rule:** Never skip layers or reverse dependencies.

### Module Structure

```
shared/
├── commonMain/          # Platform-agnostic business logic
│   ├── kotlin/
│   │   ├── domain/     # Models, repository interfaces
│   │   ├── data/       # Repository implementations
│   │   ├── feature/    # Controllers (StateFlow), use cases
│   │   ├── location/   # Location processing, geocoding
│   │   ├── di/         # Dependency injection (kotlin-inject)
│   │   └── logging/    # Structured logging
│   └── sqldelight/     # Database schema (.sq files)
├── androidMain/         # Android platform code
├── iosMain/             # iOS platform code
└── commonTest/          # Shared tests

composeApp/              # Android app (Jetpack Compose)
iosApp/                  # iOS app (SwiftUI)
```

### Key Architectural Patterns

**State Management:**
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

    fun performAction() {
        _state.update { it.copy(isLoading = true) }
        coroutineScope.launch {
            try {
                val result = useCase.execute()
                _state.update { it.copy(data = result, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

**Platform-Specific Code (expect/actual):**
```kotlin
// commonMain
expect class PlatformService {
    suspend fun performAction(): Result<Data>
}

// androidMain
actual class PlatformService(private val context: Context) {
    actual suspend fun performAction(): Result<Data> {
        // Android implementation
    }
}

// iosMain
actual class PlatformService {
    actual suspend fun performAction(): Result<Data> {
        // iOS implementation
    }
}
```

## Coding Standards

### Naming Conventions
- **Classes/Interfaces:** PascalCase (`PlaceVisit`, `LocationRepository`)
- **Functions/Properties:** camelCase (`getUserId`, `processLocations`, `isLoading`)
- **Constants:** UPPER_SNAKE_CASE (`MAX_RETRIES`, `DEFAULT_TIMEOUT`)
- **Composables:** PascalCase (`TimelineScreen`, `VisitCard`)
- **Test Files:** `*Test.kt` (`PlaceVisitRepositoryTest.kt`)

### Code Style
- **Immutability:** Always prefer `val` over `var`
- **Data Classes:** Use for domain models with `copy()` for updates
- **Null Safety:** Avoid nullable types when possible; use default values
- **Extension Functions:** Use to enhance readability
- **Sealed Classes:** Use for finite state representations

### State Updates
Always update state immutably:
```kotlin
// ✅ Correct
_state.update { it.copy(isLoading = true) }

// ❌ Wrong
_state.value.isLoading = true
```

### Logging
Use kotlin-logging with lazy evaluation:
```kotlin
private val logger = logger()  // Automatically uses class name

logger.info { "Important event occurred" }
logger.debug { "Processing data: $data" }
logger.error(exception) { "Operation failed" }
```

**Log Levels:**
- TRACE: Fine-grained diagnostic
- DEBUG: Detailed debugging (coordinates OK here)
- INFO: Important business events
- WARN: Potential issues
- ERROR: Failures and exceptions

### Error Handling
Use sealed class hierarchy with `Result<T>`:
```kotlin
sealed class AppError {
    abstract val message: String
    abstract val technicalDetails: String?
}

sealed class NetworkError : AppError() {
    data class NoConnection(
        override val message: String = "No internet connection",
        override val technicalDetails: String? = null
    ) : NetworkError()
}

// In use cases
suspend fun execute(): Result<Data> {
    return try {
        val data = repository.getData()
        Result.success(data)
    } catch (e: Exception) {
        Result.failure(DatabaseError.QueryFailed(e.message))
    }
}
```

### Dependency Injection
Use kotlin-inject with constructor injection:
```kotlin
@Inject
class MyRepository(
    private val database: TrailGlassDatabase
) {
    // Implementation
}

// In module (if interface exists)
interface DataModule {
    @AppScope
    @Provides
    fun provideMyRepository(impl: MyRepositoryImpl): MyRepository = impl
}
```

After modifying DI code:
```bash
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
```

## Database (SQLDelight)

### Schema
All `.sq` files located in: `shared/src/commonMain/sqldelight/com/po4yka/trailglass/db/`

**Tables:**
- `location_samples` - Raw GPS points with indices
- `place_visits` - Detected stationary periods with geocoding
- `route_segments` - Movement between places with transport type
- `trips` - Multi-day journeys
- `photos` - Photo metadata
- `photo_attachments` - Many-to-many photo-visit links
- `geocoding_cache` - Persistent reverse geocoding cache (30-day TTL)

### Query Best Practices
```sql
-- ✅ Good: Named query with clear purpose
getById:
SELECT * FROM place_visits
WHERE id = ? AND user_id = ?;

-- ✅ Good: Time range with soft delete filter
getForTimeRange:
SELECT * FROM place_visits
WHERE user_id = ?
  AND start_time >= ?
  AND end_time <= ?
  AND deleted_at IS NULL
ORDER BY start_time ASC;

-- Always use parameters, never string concatenation
-- Always create indices for WHERE/ORDER BY columns
-- Always use transactions for multiple operations
-- Always use soft deletes (deleted_at column)
```

### After Schema Changes
```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
./gradlew clean build
```

## Testing

### Coverage Requirements (Enforced by Kover)
- **Overall:** 75%+ (hard requirement)
- **Domain models:** 90%+
- **Repositories:** 85%+
- **Use cases:** 80%+
- **Controllers:** 75%+

### Test Structure (AAA Pattern)
```kotlin
@Test
fun testMethodName_condition_expectedOutcome() = runTest {
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
- Use descriptive names explaining what/when/why
- Test happy path AND edge cases
- Ensure test independence with @BeforeTest/@AfterTest
- Use `TestDatabaseHelper` for in-memory database
- Run tests before every commit

### Running Tests
```bash
./scripts/run-tests.sh              # All tests + coverage
./gradlew :shared:test              # Shared module only
./gradlew koverHtmlReport           # Generate HTML coverage report
./gradlew koverVerify               # Verify 75% threshold
```

## Build Commands

### Android
```bash
./gradlew :composeApp:installDebug        # Build and install
./gradlew :composeApp:assembleDebug       # Build APK only
./gradlew clean build                     # Full clean build
```

### iOS
```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode  # Build framework
cd iosApp && xcodebuild -scheme iosApp build          # Build iOS app
```

### Code Generation
```bash
# After SQLDelight schema changes
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface

# After kotlin-inject DI changes
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
```

## Common Development Tasks

### Adding a New Feature
1. Create domain model in `domain/model/`
2. Create repository interface in `domain/repository/`
3. Implement repository in `data/repository/impl/`
4. Create use case in `feature/*/`
5. Create controller with StateFlow in `feature/*/`
6. Add DI bindings in appropriate module
7. Create UI screen (Compose/SwiftUI)
8. Write tests achieving 75%+ coverage
9. Update documentation as needed

### Modifying Database Schema
1. Edit `.sq` file in `sqldelight/`
2. Run: `./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface`
3. Update repository implementation
4. Update affected tests
5. Create migration file if needed
6. Run: `./gradlew clean build`

### Adding Platform-Specific Functionality
1. Create `expect` declaration in `commonMain/`
2. Create `actual` implementation in `androidMain/`
3. Create `actual` implementation in `iosMain/`
4. Test on both platforms
5. Document platform differences in comments

## Rules & Guidelines

### ✅ DO
- Follow architecture layer separation strictly
- Use immutable state with StateFlow
- Write tests for all new code (75%+ coverage)
- Use structured coroutines with proper scopes
- Log with lazy evaluation: `logger.debug { "..." }`
- Use SQLDelight for all database queries
- Use kotlin-inject for dependency injection
- Document public APIs with KDoc
- Handle errors with sealed classes and Result<T>

### ❌ DON'T
- Skip architecture layers
- Expose MutableStateFlow publicly
- Use GlobalScope or unstructured coroutines
- Hardcode values (use constants/configuration)
- Ignore test coverage requirements
- Mutate state directly (always use `.copy()`)
- Write raw SQL strings
- Use manual dependency injection
- Log sensitive data at INFO level
- Commit API keys or secrets

## Troubleshooting

### "Cannot resolve symbol generated by SQLDelight"
```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
# Then: File → Invalidate Caches → Restart (in IDE)
```

### "Framework not found Shared" (iOS)
```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
# Then: Product → Clean Build Folder in Xcode
```

### "Unsatisfied dependencies" (kotlin-inject)
```bash
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
```

### Coverage verification failed
```bash
./gradlew koverHtmlReport
# Open: shared/build/reports/kover/html/index.html
# Review uncovered code and add tests
```

## Security Best Practices

- Never commit API keys (use `local.properties`)
- Don't log coordinates at INFO level (DEBUG/TRACE only)
- Don't log user IDs in production
- Use encrypted preferences for tokens
- Validate all user input
- Review platform permissions carefully

## Documentation

Comprehensive documentation in `docs/`:
- `ARCHITECTURE.md` - System architecture
- `DEVELOPMENT.md` - Setup and development guide
- `TESTING.md` - Testing strategy and requirements
- `ERROR_HANDLING.md` - Error handling patterns
- `LOCATION_TRACKING.md` - Platform location implementations
- `UI_IMPLEMENTATION.md` - UI architecture
- `dependency-injection.md` - DI setup guide

Also see:
- `CLAUDE.md` - Instructions for Claude Code
- `GEMINI.md` - Instructions for Gemini CLI
- `.cursor/rules/` - Cursor IDE rules

## Pre-Commit Checklist

Before committing code, verify:
- [ ] Tests pass: `./gradlew :shared:test`
- [ ] Coverage ≥75%: `./gradlew koverVerify`
- [ ] Android builds: `./gradlew :composeApp:assembleDebug`
- [ ] iOS framework builds: `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
- [ ] No compilation errors or warnings
- [ ] Architecture layers are respected
- [ ] New code has corresponding tests
- [ ] Documentation is updated if needed
- [ ] No hardcoded values or secrets

## Quick Reference

**Project Type:** Kotlin Multiplatform (Android + iOS)
**Primary Language:** Kotlin 2.2.20
**Architecture:** Clean Architecture with KMP
**Database:** SQLDelight 2.1.0
**DI:** kotlin-inject 0.7.2 (KSP)
**Min Coverage:** 75% (enforced)
**State Management:** StateFlow (immutable)
**Platform Pattern:** expect/actual

---

*This file provides instructions for AI coding agents. For human developers, start with `README.md` and `docs/DEVELOPMENT.md`.*
