# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Trailglass is a privacy-respectful travel logging app for iOS and Android built with Kotlin Multiplatform. It tracks location in the background, builds a timeline of trips, and allows attaching photos and journal entries.

## Build Commands

### Android

```bash
# Build and install debug APK
./gradlew :composeApp:installDebug

# Clean build
./gradlew clean build

# Build without tests
./gradlew :composeApp:assembleDebug

# Rebuild shared framework and Android app (after KSP changes)
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
./gradlew :composeApp:assembleDebug
```

### iOS

```bash
# Build shared framework for Xcode
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# Then build in Xcode or via command line:
cd iosApp
xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15' build
```

### Testing

```bash
# Run all tests with coverage
./scripts/run-tests.sh

# Run shared module tests only
./gradlew :shared:test

# Run Android unit tests
./gradlew :composeApp:testDebugUnitTest

# Run Android UI tests (requires emulator/device)
./scripts/run-ui-tests-android.sh

# Generate coverage report (75%+ required)
./gradlew koverHtmlReport
# View: shared/build/reports/kover/html/index.html

# Verify coverage threshold
./gradlew koverVerify
```

### Database

```bash
# Regenerate SQLDelight code after schema changes
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface

# After SQLDelight changes, rebuild:
./gradlew clean :shared:build
```

### Linting & Code Quality

```bash
# Run all linters (Kotlin, Android, Swift)
./scripts/lint-all.sh

# Auto-format all code
./scripts/format-all.sh

# Individual linters
./gradlew ktlintCheck          # Check Kotlin formatting
./gradlew ktlintFormat         # Auto-format Kotlin
./gradlew detekt               # Static analysis
./gradlew :composeApp:lint     # Android Lint

# iOS (requires SwiftLint: brew install swiftlint)
cd iosApp && swiftlint         # Lint Swift code
cd iosApp && swiftlint --fix   # Auto-fix Swift issues
```

See `docs/LINTING.md` for detailed documentation.

### CI/CD

GitHub Actions workflows run automatically on pull requests and pushes:

**Workflows:**
- **lint.yml** - Code quality checks (ktlint, detekt, Android Lint, SwiftLint)
- **ci.yml** - Build and test (shared tests, Android tests, APK build, iOS framework build)

**Viewing Results:**
- Check the "Actions" tab on GitHub
- PR checks show workflow status
- Failed workflows upload reports as artifacts

**Local Testing:**
```bash
# Run the same checks locally before pushing
./scripts/lint-all.sh       # All linters
./scripts/run-tests.sh      # All tests
./gradlew :composeApp:assembleDebug  # Android build
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64  # iOS framework
```

**CI Requirements:**
- All lint checks must pass
- Test coverage must be 75%+
- All tests must pass
- Builds must succeed for Android and iOS

## Architecture

### Module Structure

- **shared/**: Kotlin Multiplatform module with business logic, data layer, and domain models
  - `commonMain/`: Platform-agnostic code (repositories, use cases, domain models)
  - `androidMain/`: Android-specific implementations (location tracking, MediaStore, Geocoder)
  - `iosMain/`: iOS-specific implementations (CLLocationManager, Photos framework, CLGeocoder)
  - `commonTest/`: Shared tests
- **composeApp/**: Android app with Jetpack Compose UI
- **iosApp/**: iOS app with SwiftUI

### Layer Separation

```
Platform UI (Compose/SwiftUI)
    ↓
Controllers (StateFlow-based, in shared)
    ↓
Use Cases (Business logic, in shared)
    ↓
Repositories (Data access, in shared)
    ↓
Database (SQLDelight, in shared)
    ↓
Platform APIs (expect/actual pattern)
```

### Key Technologies

- **Kotlin 2.2.21** with Kotlin Multiplatform
- **SQLDelight 2.2.1**: Type-safe database with cross-platform support
- **kotlin-inject 0.7.2**: Compile-time dependency injection via KSP
- **Decompose 3.2.0**: Navigation with lifecycle and state preservation
- **Ktor 3.3.2**: HTTP client for sync
- **kotlin-logging 7.0.0**: Multiplatform structured logging
- **Compass 2.4.1**: Location toolkit for geocoding and geolocation (KMP)
- **Moko-Resources 0.25.1**: Multiplatform resource management (KMP)
- **AndroidX Input Motion Prediction 1.0.0**: Low-latency touch/stylus input for drawing (Android only)
- **Kover**: Code coverage (75%+ enforced)

### Database Schema

Six main tables:
- `location_samples`: Raw GPS points
- `place_visits`: Detected stationary periods
- `route_segments`: Movement between places
- `trips`: Multi-day journeys
- `photos`: Photo metadata
- `photo_attachments`: Links photos to visits

All queries are in `.sq` files under `shared/src/commonMain/sqldelight/`.

### Dependency Injection

Uses kotlin-inject (KSP-based compile-time DI):
- Component: `AppComponent` (in `shared/src/commonMain/kotlin/com/po4yka/trailglass/di/`)
- Modules: `DataModule`, `LocationModule`, `PlatformModule`, etc.
- Scope: `@AppScope` for application-level singletons
- Creation: `createAndroidAppComponent(context)` or `createIOSAppComponent()`

After modifying DI code, rebuild KSP:
```bash
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
```

### State Management

Controllers use `StateFlow` for reactive state:
```kotlin
data class State(val data: T?, val isLoading: Boolean, val error: String?)
private val _state = MutableStateFlow(State(...))
val state: StateFlow<State> = _state.asStateFlow()
```

UI observes state via `collectAsState()` (Compose) or Combine/SwiftUI patterns (iOS).

## Development Workflow

### Making Changes

1. **Shared code changes**: Modify in `shared/src/commonMain/kotlin/`
2. **Platform-specific**: Use expect/actual pattern in `androidMain/` or `iosMain/`
3. **Database schema**: Edit `.sq` files, then regenerate
4. **Tests**: Add tests to maintain 75%+ coverage

### After Schema Changes

```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
./gradlew clean build
```

### After DI Changes

```bash
./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid
./gradlew build
```

### Common Errors

- **"Cannot resolve symbol generated by SQLDelight"**: Run `./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface`
- **"Framework not found Shared" (iOS)**: Run `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
- **"Unsatisfied dependencies" (kotlin-inject)**: Rebuild KSP: `./gradlew clean kspCommonMainKotlinMetadata kspDebugKotlinAndroid`
- **Coverage verification failed**: Run `./gradlew koverHtmlReport` to see uncovered code

## Documentation Policy

**IMPORTANT: Do NOT create report files or documentation files in markdown format unless explicitly requested by the user.**

This includes but is not limited to:

- Summary reports (SUMMARY.md, IMPLEMENTATION_SUMMARY.md, etc.)
- Migration guides (MIGRATION.md, etc.)
- Proposal documents (PROPOSAL.md, etc.)
- Phase reports (PHASE_*.md, etc.)
- Guide documents (GUIDE.md, COLOR_GUIDE.md, etc.)
- Any other .md files except README.md when necessary

### What TO Do Instead

- Focus on implementing code changes
- Provide brief summaries in chat responses
- Update existing README.md only when explicitly requested
- Answer user questions directly in conversation

### Exceptions

You may create markdown files ONLY when:

- The user explicitly asks for a specific markdown file by name
- The user requests written documentation
- It's a critical project file like README.md and the user approves

## Important Locations

### Configuration
- Version catalog: `gradle/libs.versions.toml`
- Android manifest: `composeApp/src/main/AndroidManifest.xml`
- Local properties (Git-ignored): `local.properties`
- Editor config: `.editorconfig`
- Detekt config: `config/detekt/detekt.yml`
- Android Lint config: `config/android-lint.xml`
- SwiftLint config: `iosApp/.swiftlint.yml`

### Documentation
- Architecture: `docs/ARCHITECTURE.md`
- Linting & Code Quality: `docs/LINTING.md`
- Development: `docs/DEVELOPMENT.md`
- Testing: `docs/TESTING.md`
- Error Handling: `docs/ERROR_HANDLING.md`
- Location Tracking: `docs/LOCATION_TRACKING.md`
- DI: `docs/dependency-injection.md`
- Motion Prediction: `docs/MOTION_PREDICTION.md`
- Compass & Moko-Resources: `docs/COMPASS_MOKO_INTEGRATION.md`
- Kotlin 2.2.21 Migration: `docs/KOTLIN_2_2_21_MIGRATION.md`

### Code
- Domain models: `shared/src/commonMain/kotlin/com/po4yka/trailglass/domain/model/`
- Repositories: `shared/src/commonMain/kotlin/com/po4yka/trailglass/data/repository/`
- Controllers: `shared/src/commonMain/kotlin/com/po4yka/trailglass/feature/*/`
- SQLDelight schema: `shared/src/commonMain/sqldelight/com/po4yka/trailglass/db/`
- DI components: `shared/src/commonMain/kotlin/com/po4yka/trailglass/di/`
- Android UI utilities: `composeApp/src/main/kotlin/com/po4yka/trailglass/ui/util/`
- Android UI components: `composeApp/src/main/kotlin/com/po4yka/trailglass/ui/components/`

## Code Style

- Use kotlin-logging for all logging: `private val logger = logger()`
- Follow sealed class hierarchy for errors (see `docs/ERROR_HANDLING.md`)
- Prefer `Flow` for reactive streams
- Use `Result<T>` for error handling in use cases
- All new code must have tests (75%+ coverage required)
- Immutable data classes for domain models
- Repository interfaces in `domain/repository/`, implementations in `data/repository/impl/`

## Platform Differences

Key platform-specific implementations:
- **Location tracking**: FusedLocationProviderClient (Android) vs CLLocationManager (iOS)
- **Photos**: MediaStore (Android) vs Photos framework (iOS)
- **Maps**: Google Maps (Android) vs MapKit (iOS)
- **Geocoding**: Android Geocoder vs CLGeocoder
- **Preferences**: DataStore (Android) vs UserDefaults (iOS)

See `docs/PLATFORM_DIFFERENCES.md` for details.
