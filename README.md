# Trailglass

> Your journeys, captured as they unfold.

Trailglass is a privacy-respectful travel logging app for iOS and Android.  
It quietly logs your location in the background, builds a living timeline of your trips, and lets you attach photos and journal entries to any moment along the way.

The code is open for **non-commercial use** under the **Non-Commercial Open Software License (NC-OSL)** by Nikita Pochaev (po4yka).

---

## Features

- **Background travel logging**
  - Lightweight background location tracking.
  - Automatic grouping of raw points into trips and daily segments.
  - Offline-friendly storage with sync conflict handling.

- **Timeline & map**
  - Chronological timeline of your days and trips.
  - Map view with reconstructed paths and key stops.
  - Detail screens for each trip/day with stats.

- **Photos & journal entries**
  - Attach photos to locations and time segments.
  - Write short notes or full journal entries for each day or trip.
  - Rich metadata: time, place, weather (planned) and other context.

- **Cross-device sync**
  - Sync between iOS and Android via a shared cloud backend.
  - Conflict-aware merge strategy designed for intermittent connectivity.
  - Prepared for end-to-end encryption for sensitive data (planned).

- **Native experience on both platforms**
  - Android: Material 3 Expressive, modern navigation, system dark mode, dynamic color.
  - iOS: Liquid Glass-inspired visuals, blur, depth, and native gesture patterns.

---

## Quick Start

```bash
# Clone repository
git clone https://github.com/po4yka/trailglass.git
cd trailglass

# Run Android app
./gradlew :composeApp:installDebug

# Run tests
./scripts/run-tests.sh

# Generate coverage report
./gradlew koverHtmlReport
```

See **[Development Guide](docs/DEVELOPMENT.md)** for detailed setup instructions.

---

## Documentation

### 📚 Core Documentation
- **[Architecture](docs/ARCHITECTURE.md)**: System architecture, layers, state management, DI
- **[Development](docs/DEVELOPMENT.md)**: Setup, running the app, debugging, troubleshooting
- **[Testing](docs/TESTING.md)**: Testing strategy, coverage requirements (75%+)
- **[Error Handling](docs/ERROR_HANDLING.md)**: Error types, retry mechanisms, analytics, offline mode

### 🔧 Feature Documentation
- **[Location Tracking](docs/LOCATION_TRACKING.md)**: Platform location tracking implementation
- **[UI Implementation](docs/UI_IMPLEMENTATION.md)**: Material 3 UI, screens, components
- **[Photo Integration](docs/PHOTO_INTEGRATION.md)**: MediaStore, Photos framework, smart matching
- **[Map Visualization](docs/MAP_VISUALIZATION.md)**: Google Maps, MapKit, markers, routes

---

## Implementation Status

### ✅ Completed (Phase 1 - Foundation)

**Phase 1A: Database Foundation** (SQLDelight)
- Complete database schema with 6 tables (Trips, PlaceVisits, RouteSegments, LocationSamples, Photos, PhotoAttachments)
- 100+ type-safe SQL queries for CRUD operations
- Foreign key constraints and cascade deletes
- Platform-specific drivers (Android, iOS, JVM)

**Phase 1B: Location Processing Pipeline**
- Location clustering (DBSCAN-based) for visit detection
- Route segment generation with transport type classification
- Path simplification (Douglas-Peucker algorithm)
- Trip assembly from visits and routes
- Reverse geocoding integration with spatial caching

**Phase 1C: Platform Location Tracking**
- Android: FusedLocationProviderClient, Foreground Service, WorkManager
- iOS: CLLocationManager, visit monitoring, background tasks
- Three tracking modes: IDLE, PASSIVE (5min/500m), ACTIVE (30sec/10m)
- Battery-optimized configurations
- Comprehensive permission handling

**Phase 1D: Basic UI Implementation**
- Material 3 Expressive theme (Android) with dynamic color
- StateFlow-based controllers for Stats, Timeline, Map, Photo features
- Screens: StatsScreen, TimelineScreen, SettingsScreen
- Bottom navigation with icon-based tabs
- Loading/error/empty states

**Photo Integration**
- MediaStore (Android) and Photos framework (iOS) integration
- Smart photo-visit matching algorithm (time + location scoring)
- Photo attachment with captions
- Automatic metadata extraction (timestamp, GPS, dimensions)
- Coil for async image loading

**Map Visualization**
- Google Maps Compose (Android) and MapKit (iOS)
- Marker rendering for place visits
- Route polylines with transport type color coding
- Bounding region calculation with auto-fit
- Camera controls and zoom management

**Testing Infrastructure**
- 98+ tests across unit, integration, and UI layers
- Kover code coverage with 75%+ enforcement
- Integration tests for all repositories
- Android UI tests (Compose Test + Espresso)
- iOS UI tests (XCTest)
- Test scripts and documentation

**Error Handling Infrastructure**
- Sealed error class hierarchy (NetworkError, DatabaseError, LocationError, PhotoError, ValidationError)
- Result<T> wrapper for type-safe error handling
- User-friendly error messages separate from technical details
- Platform-specific network connectivity monitoring (Android ConnectivityManager, iOS Network framework)
- Retry mechanisms with exponential backoff (1s → 2s → 4s → 8s, max 30s)
- Multiple retry policies (DEFAULT, AGGRESSIVE, CONSERVATIVE, NETWORK)
- Error analytics with severity levels (INFO, WARNING, ERROR, FATAL)
- Context-aware error mapping and logging
- Offline mode support with automatic retry
- Comprehensive error handling documentation

### 📋 Planned (Phase 2 - Enhanced Features)

- Background sync with cloud backend
- End-to-end encryption for sensitive data
- Weather and context data integration
- Advanced statistics and visualizations
- Photo recognition and tagging
- Trip sharing and export
- Offline maps support
- Dark mode refinements

See feature documentation for detailed implementations.

---

## Tech Stack

### Core / Shared (Kotlin Multiplatform)
- **Kotlin 2.2.20**: Shared business logic and domain models
- **Coroutines & Flow**: Asynchronous and reactive programming
- **SQLDelight 2.0.2**: Type-safe cross-platform database
- **Kotlinx.datetime**: Cross-platform date/time handling
- **kotlin-logging**: Structured multiplatform logging

### Android
- **Kotlin**: Modern Android development
- **Jetpack Compose**: Declarative UI with Material 3 Expressive theme
- **Decompose 3.2.0**: Lifecycle-aware navigation with state preservation and deep linking
- **Lifecycle & ViewModel**: Android Architecture Components
- **WorkManager**: Background location processing
- **Coil**: Async image loading
- **FusedLocationProvider**: Location tracking
- **Google Maps Compose**: Map visualization

### iOS
- **Swift & SwiftUI**: Native iOS UI
- **MapKit**: Map visualization
- **CoreLocation**: Location tracking (CLLocationManager)
- **Photos Framework**: Photo library access
- **Shared Framework**: Kotlin Multiplatform integration

### Testing
- **Kotlin Test**: Multiplatform unit tests
- **JUnit**: Android unit tests
- **Espresso & Compose Test**: Android UI tests
- **XCTest**: iOS unit and UI tests
- **Kover**: Code coverage (75%+ enforced)
- **Turbine**: Flow testing
- **Kotest**: Assertions
- **MockK**: Mocking

### Development Tools
- **Gradle 8.11**: Build system
- **Android Studio**: Primary IDE
- **Xcode 15+**: iOS development
- **Git**: Version control

### Future / Planned
- **Cloud Backend**: Ktor server for sync
- **Encryption**: End-to-end encryption for sensitive data
- **CI/CD**: GitHub Actions for automated testing and deployment
