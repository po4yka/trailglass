# TrailGlass - Implementation Next Steps

**Last Updated:** 2025-11-17
**Current Status:** Phase 1 - Foundation (Early Stage)

This document outlines the recommended next steps for implementing TrailGlass, based on the completed reverse geocoding infrastructure and the project specifications.

---

## 📊 Current State

### ✅ What We Have

1. **Reverse Geocoding Infrastructure** (Complete)
   - Domain models: `LocationSample`, `PlaceVisit`, `GeocodedLocation`
   - Cross-platform geocoders (Android & iOS)
   - Intelligent caching with spatial proximity matching
   - PlaceVisit clustering algorithm
   - Location service integration layer
   - Comprehensive documentation

2. **Project Structure** (Basic)
   - KMP project initialized
   - Gradle build configuration (basic)
   - Android and iOS app shells
   - Shared module structure

### 🚧 What's Missing

- Database layer (SQLDelight)
- Repository pattern implementation
- Remaining domain entities
- Platform-specific location tracking
- UI implementation
- Network/sync layer
- Test infrastructure

---

## 🎯 Recommended Implementation Phases

## Phase 1A: Database Foundation (Immediate Next - 2-3 weeks)

The reverse geocoding infrastructure is ready, but needs persistence. This phase establishes the database layer.

### Priority 1: SQLDelight Setup & Schema Design

**Why First:** Everything else depends on data persistence. The geocoding cache needs database storage to persist across app restarts.

#### Tasks:

1. **Configure SQLDelight Dependencies**
   - [ ] Add SQLDelight Gradle plugin to `shared/build.gradle.kts`
   - [ ] Configure SQLDelight database name and package
   - [ ] Add platform-specific drivers (Android & iOS)
   - [ ] Set up database migrations infrastructure

   **Files to modify:**
   - `shared/build.gradle.kts`
   - `gradle/libs.versions.toml` (if using version catalog)

2. **Design Core Schema**
   - [ ] Create `location_samples.sq` table
     ```sql
     CREATE TABLE location_samples (
       id TEXT NOT NULL PRIMARY KEY,
       timestamp INTEGER NOT NULL,
       latitude REAL NOT NULL,
       longitude REAL NOT NULL,
       accuracy REAL NOT NULL,
       speed REAL,
       bearing REAL,
       source TEXT NOT NULL,
       trip_id TEXT,
       uploaded_at INTEGER,
       device_id TEXT NOT NULL,
       user_id TEXT NOT NULL,
       created_at INTEGER NOT NULL,
       updated_at INTEGER NOT NULL
     );

     CREATE INDEX location_samples_timestamp ON location_samples(timestamp);
     CREATE INDEX location_samples_user_trip ON location_samples(user_id, trip_id);
     ```

   - [ ] Create `place_visits.sq` table
     ```sql
     CREATE TABLE place_visits (
       id TEXT NOT NULL PRIMARY KEY,
       start_time INTEGER NOT NULL,
       end_time INTEGER NOT NULL,
       center_latitude REAL NOT NULL,
       center_longitude REAL NOT NULL,
       approximate_address TEXT,
       poi_name TEXT,
       city TEXT,
       country_code TEXT,
       user_id TEXT NOT NULL,
       created_at INTEGER NOT NULL,
       updated_at INTEGER NOT NULL
     );

     CREATE INDEX place_visits_time ON place_visits(start_time, end_time);
     CREATE INDEX place_visits_user ON place_visits(user_id);
     ```

   - [ ] Create `geocoding_cache.sq` table
     ```sql
     CREATE TABLE geocoding_cache (
       id TEXT NOT NULL PRIMARY KEY,
       latitude REAL NOT NULL,
       longitude REAL NOT NULL,
       formatted_address TEXT,
       city TEXT,
       state TEXT,
       country_code TEXT,
       country_name TEXT,
       postal_code TEXT,
       poi_name TEXT,
       street TEXT,
       street_number TEXT,
       cached_at INTEGER NOT NULL,
       expires_at INTEGER NOT NULL
     );

     CREATE INDEX geocoding_cache_coords ON geocoding_cache(latitude, longitude);
     CREATE INDEX geocoding_cache_expiry ON geocoding_cache(expires_at);
     ```

   - [ ] Create `place_visit_samples.sq` junction table
     ```sql
     CREATE TABLE place_visit_samples (
       place_visit_id TEXT NOT NULL,
       location_sample_id TEXT NOT NULL,
       PRIMARY KEY (place_visit_id, location_sample_id),
       FOREIGN KEY (place_visit_id) REFERENCES place_visits(id) ON DELETE CASCADE,
       FOREIGN KEY (place_visit_id) REFERENCES location_samples(id) ON DELETE CASCADE
     );
     ```

   **Directory:** `shared/src/commonMain/sqldelight/com/po4yka/trailglass/db/`

3. **Implement Database Wrapper**
   - [ ] Create `Database.kt` interface
     ```kotlin
     expect class DatabaseDriverFactory {
         fun createDriver(): SqlDriver
     }

     class TrailGlassDatabase(driverFactory: DatabaseDriverFactory) {
         private val driver = driverFactory.createDriver()
         val database = Database(driver)

         // Queries
         val locationSampleQueries get() = database.locationSamplesQueries
         val placeVisitQueries get() = database.placeVisitsQueries
         val geocodingCacheQueries get() = database.geocodingCacheQueries
     }
     ```

   - [ ] Implement Android driver factory
     ```kotlin
     // androidMain
     actual class DatabaseDriverFactory(private val context: Context) {
         actual fun createDriver(): SqlDriver {
             return AndroidSqliteDriver(
                 Database.Schema,
                 context,
                 "trailglass.db"
             )
         }
     }
     ```

   - [ ] Implement iOS driver factory
     ```kotlin
     // iosMain
     actual class DatabaseDriverFactory {
         actual fun createDriver(): SqlDriver {
             return NativeSqliteDriver(
                 Database.Schema,
                 "trailglass.db"
             )
         }
     }
     ```

   **Files to create:**
   - `shared/src/commonMain/kotlin/com/po4yka/trailglass/data/db/Database.kt`
   - `shared/src/androidMain/kotlin/com/po4yka/trailglass/data/db/DatabaseDriverFactory.android.kt`
   - `shared/src/iosMain/kotlin/com/po4yka/trailglass/data/db/DatabaseDriverFactory.ios.kt`

### Priority 2: Repository Layer

**Why Second:** Abstracts database operations and provides clean API for use cases.

#### Tasks:

1. **Create Repository Interfaces**
   - [ ] `LocationRepository` interface
     ```kotlin
     interface LocationRepository {
         suspend fun insertSample(sample: LocationSample)
         suspend fun getSamples(userId: String, startTime: Instant, endTime: Instant): List<LocationSample>
         suspend fun getSamplesForTrip(tripId: String): List<LocationSample>
         suspend fun deleteSample(id: String)
     }
     ```

   - [ ] `PlaceVisitRepository` interface
     ```kotlin
     interface PlaceVisitRepository {
         suspend fun insertVisit(visit: PlaceVisit)
         suspend fun getVisits(userId: String, startTime: Instant, endTime: Instant): List<PlaceVisit>
         suspend fun getVisitById(id: String): PlaceVisit?
         suspend fun deleteVisit(id: String)
         suspend fun linkSamples(visitId: String, sampleIds: List<String>)
     }
     ```

   - [ ] `GeocodingCacheRepository` interface
     ```kotlin
     interface GeocodingCacheRepository {
         suspend fun get(latitude: Double, longitude: Double, radiusMeters: Double): GeocodedLocation?
         suspend fun put(location: GeocodedLocation)
         suspend fun clearExpired()
         suspend fun clear()
     }
     ```

   **Directory:** `shared/src/commonMain/kotlin/com/po4yka/trailglass/data/repository/`

2. **Implement SQLDelight Repositories**
   - [ ] `LocationRepositoryImpl`
   - [ ] `PlaceVisitRepositoryImpl`
   - [ ] `GeocodingCacheRepositoryImpl` (with Haversine distance query)

   **Directory:** `shared/src/commonMain/kotlin/com/po4yka/trailglass/data/repository/impl/`

3. **Update Existing Services**
   - [ ] Modify `GeocodingCache` to use `GeocodingCacheRepository`
   - [ ] Update `LocationService` to use `LocationRepository` and `PlaceVisitRepository`
   - [ ] Add database persistence to location recording workflow

### Priority 3: Remaining Domain Entities

**Why Third:** Needed for complete data model before building higher-level features.

#### Tasks:

1. **Create Remaining Core Entities**
   - [ ] `RouteSegment` data class
   - [ ] `Trip` and `TripDay` data classes
   - [ ] `TimelineItem` sealed class
   - [ ] `CountryVisit` and `WorldStats` data classes

   **Directory:** `shared/src/commonMain/kotlin/com/po4yka/trailglass/domain/model/`

2. **Add Corresponding SQLDelight Tables**
   - [ ] `route_segments.sq`
   - [ ] `trips.sq` and `trip_days.sq`
   - [ ] `country_visits.sq`

3. **Create Additional Repositories**
   - [ ] `RouteSegmentRepository`
   - [ ] `TripRepository`
   - [ ] `WorldStatsRepository`

**Estimated Time:** 2-3 weeks
**Dependencies:** None (can start immediately)
**Risk Level:** Low (well-defined, straightforward implementation)

---

## Phase 1B: Location Processing Pipeline (3-4 weeks)

With database in place, implement the complete location processing pipeline.

### Priority 1: Route Segment Builder

**Why First:** Complements PlaceVisit detection to build complete timeline.

#### Tasks:

1. **Implement RouteSegment Detection**
   - [ ] Create `RouteSegmentBuilder` class
     ```kotlin
     class RouteSegmentBuilder(
         private val pathSimplifier: PathSimplifier
     ) {
         fun buildSegments(
             samples: List<LocationSample>,
             visits: List<PlaceVisit>
         ): List<RouteSegment>
     }
     ```

   - [ ] Implement Douglas-Peucker path simplification
     ```kotlin
     class PathSimplifier(
         private val epsilonMeters: Double = 50.0
     ) {
         fun simplify(points: List<LocationSample>): List<LocationSample>
     }
     ```

   - [ ] Add transport type inference based on speed
     ```kotlin
     enum class TransportType {
         WALK, BIKE, CAR, TRAIN, PLANE, UNKNOWN;

         companion object {
             fun inferFromSpeed(speedMps: Double?): TransportType
         }
     }
     ```

   **Directory:** `shared/src/commonMain/kotlin/com/po4yka/trailglass/location/`

### Priority 2: Trip Detection & Aggregation

**Why Second:** Groups visits and routes into meaningful trips.

#### Tasks:

1. **Implement Trip Detection**
   - [ ] Create `TripDetector` class
     ```kotlin
     class TripDetector(
         private val homeLocationDetector: HomeLocationDetector,
         private val tripBoundaryDetector: TripBoundaryDetector
     ) {
         suspend fun detectTrips(
             visits: List<PlaceVisit>,
             segments: List<RouteSegment>
         ): List<Trip>
     }
     ```

   - [ ] Implement home location detection (ML clustering or manual)
   - [ ] Implement trip boundary detection (time gaps, distance from home)
   - [ ] Multi-day trip grouping logic

2. **Create TripDay Aggregator**
   - [ ] Build daily timeline from visits and segments
   - [ ] Group by date with timezone handling
   - [ ] Sort timeline items chronologically

   **Directory:** `shared/src/commonMain/kotlin/com/po4yka/trailglass/location/trip/`

### Priority 3: Scheduled Processing

**Why Third:** Automate the processing pipeline.

#### Tasks:

1. **Create Processing Coordinator**
   - [ ] Implement `LocationProcessor` that orchestrates:
     - Sample smoothing
     - PlaceVisit clustering
     - RouteSegment building
     - Trip detection
     - Geocoding enrichment

2. **Add Scheduling**
   - [ ] Android: WorkManager periodic task
   - [ ] iOS: BGTaskScheduler background processing
   - [ ] Manual trigger API

**Estimated Time:** 3-4 weeks
**Dependencies:** Phase 1A completion
**Risk Level:** Medium (algorithmic complexity, performance tuning needed)

---

## Phase 1C: Platform Location Tracking (3-4 weeks)

Implement actual location tracking on both platforms.

### Priority 1: Android Location Tracking

#### Tasks:

1. **Create Location Tracking Service**
   - [ ] Implement `FusedLocationProviderClient` wrapper
   - [ ] Create tracking mode state machine (Idle/Passive/Active)
   - [ ] Build Foreground Service for Active tracking
     ```kotlin
     class LocationTrackingService : Service() {
         private lateinit var locationClient: FusedLocationProviderClient
         private val locationCallback = object : LocationCallback() {
             override fun onLocationResult(result: LocationResult) {
                 // Convert to LocationSample and record
             }
         }
     }
     ```

2. **Permission Handling**
   - [ ] Request location permissions (coarse, fine, background)
   - [ ] Permission rationale UI
   - [ ] Settings deep link for denied permissions

3. **Background Execution**
   - [ ] Notification channel for foreground service
   - [ ] WorkManager for periodic wake-up
   - [ ] Battery optimization exclusion guidance

   **Directory:** `composeApp/src/androidMain/kotlin/.../location/`

### Priority 2: iOS Location Tracking

#### Tasks:

1. **Create Location Manager Wrapper**
   - [ ] Implement `CLLocationManager` wrapper
   - [ ] Tracking modes: significant changes, visit monitoring, continuous
     ```swift
     class LocationTrackingManager: NSObject, CLLocationManagerDelegate {
         private let locationManager = CLLocationManager()
         private let locationService: LocationService

         func startTracking(mode: TrackingMode)
         func stopTracking()

         func locationManager(_ manager: CLLocationManager,
                            didUpdateLocations locations: [CLLocation])
     }
     ```

2. **Permission Handling**
   - [ ] Request "When In Use" and "Always" authorization
   - [ ] Info.plist permission descriptions
   - [ ] Permission UI flow

3. **Background Execution**
   - [ ] Configure background location capability
   - [ ] BGTaskScheduler for processing
   - [ ] Background task registration

   **Directory:** `iosApp/iosApp/Location/`

**Estimated Time:** 3-4 weeks
**Dependencies:** Phase 1A (database for storing samples)
**Risk Level:** Medium (platform-specific complexities, battery optimization)

---

## Phase 1D: Basic UI Implementation (4-5 weeks)

Create minimal UI to visualize and test the location data.

### Priority 1: Shared Controllers/ViewModels

#### Tasks:

1. **Create Feature Controllers**
   - [ ] `TimelineController` with StateFlow
     ```kotlin
     class TimelineController(
         private val getTimelineUseCase: GetTimelineForDayUseCase,
         private val placeVisitRepository: PlaceVisitRepository
     ) {
         val state: StateFlow<TimelineState>
         suspend fun loadDay(date: LocalDate)
     }
     ```

   - [ ] `StatsController` for year/month stats
   - [ ] `LocationTrackingController` for tracking state

2. **Create Use Cases**
   - [ ] `GetTimelineForDayUseCase`
   - [ ] `GetYearStatsUseCase`
   - [ ] `StartTrackingUseCase`
   - [ ] `StopTrackingUseCase`

   **Directory:** `shared/src/commonMain/kotlin/com/po4yka/trailglass/feature/`

### Priority 2: Android UI

#### Tasks:

1. **Setup Compose Foundation**
   - [ ] Material 3 Expressive theme configuration
   - [ ] Navigation graph
   - [ ] Bottom navigation

2. **Stats Screen**
   - [ ] Month/Year segmented control
   - [ ] Stats cards (countries, days, memories)
   - [ ] Timeline preview

3. **Timeline Screen**
   - [ ] Day list with visits
   - [ ] Visit detail with address
   - [ ] Simple map view (optional)

4. **Settings Screen**
   - [ ] Tracking mode toggle
   - [ ] Permissions status
   - [ ] Manual processing trigger

   **Directory:** `composeApp/src/androidMain/kotlin/.../ui/`

### Priority 3: iOS UI

#### Tasks:

1. **Setup SwiftUI Foundation**
   - [ ] Liquid Glass design tokens
   - [ ] Navigation structure
   - [ ] Tab bar

2. **Stats Screen**
   - [ ] Month/Year picker
   - [ ] Stats cards with glass effect
   - [ ] Timeline preview

3. **Timeline Screen**
   - [ ] Day list with place visits
   - [ ] Visit detail view
   - [ ] Simple map (MapKit)

4. **Settings Screen**
   - [ ] Tracking controls
   - [ ] Permission status
   - [ ] Manual sync trigger

   **Directory:** `iosApp/iosApp/Features/`

**Estimated Time:** 4-5 weeks
**Dependencies:** Phase 1A, 1B (data to display)
**Risk Level:** Low-Medium (standard UI work, design polish needed)

---

## Phase 2: Enhanced Features (Future)

Once Phase 1 is complete, move to:

1. **Photo Integration** (4-5 weeks)
   - Photo library access (Android MediaStore, iOS Photos framework)
   - Photo suggestion algorithm (time + location matching)
   - Photo attachment UI
   - Memorabilia feature

2. **Map Visualization** (3-4 weeks)
   - Google Maps / Mapbox integration (Android)
   - MapKit enhancement (iOS)
   - Route polyline rendering
   - Place markers and clustering

3. **Memory Creation** (3-4 weeks)
   - Memory domain entities (already in spec)
   - Memory CRUD operations
   - Memory detail UI
   - Auto-suggestion from trips

4. **World Meter** (2-3 weeks)
   - Country detection from coordinates
   - World map with visited countries highlighted
   - Country list with flags
   - Travel statistics

---

## Phase 3: Cloud Sync (Future)

After local-first app is solid:

1. **Backend Setup** (4-6 weeks)
   - Choose: Firebase vs Custom (Ktor server)
   - Authentication (email, Apple, Google)
   - Cloud database schema
   - File storage for photos

2. **Sync Engine** (6-8 weeks)
   - Ktor client network layer
   - Change tracking
   - Conflict resolution (Last Write Wins)
   - Photo upload queue
   - Background sync scheduling

---

## 🎯 Critical Path Summary

### Weeks 1-3: Database Foundation
- SQLDelight setup
- Schema design and implementation
- Repository layer
- Geocoding cache persistence

### Weeks 4-7: Location Processing
- RouteSegment builder
- Trip detection
- Processing pipeline
- Scheduled background processing

### Weeks 8-11: Platform Location Tracking
- Android location service
- iOS location manager
- Permission handling
- Background execution

### Weeks 12-16: Basic UI
- Shared controllers and use cases
- Android Compose UI (Stats, Timeline, Settings)
- iOS SwiftUI (Stats, Timeline, Settings)
- Testing and polish

### Week 17+: Enhanced Features
- Photos, Maps, Memories, World Meter
- Cloud sync (Phase 3)

---

## 🚨 Risks & Mitigation

### Technical Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Battery drain from location tracking | High | Multiple tracking modes, aggressive optimization, user controls |
| SQLDelight migration complexity | Medium | Start with simple schema, plan migrations carefully |
| Platform API changes | Medium | Monitor betas, modular architecture |
| Geocoding API rate limits | Medium | Cache aggressively, implement rate limiting |

### Project Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Scope creep | High | Stick to MVP first, defer Phase 2+ features |
| Over-engineering | Medium | Build simplest thing that works, refactor later |
| Lack of testing | High | Write tests alongside features, aim for >80% coverage |

---

## 📚 Resources & References

### Documentation
- [TODO.md](TODO.md) - Detailed task breakdown
- [ROADMAP.md](ROADMAP.md) - Timeline and milestones
- [SPEC.md](SPEC.md) - Product and technical specification
- [shared/src/.../location/README.md](shared/src/commonMain/kotlin/com/po4yka/trailglass/location/README.md) - Geocoding infrastructure docs

### Key Libraries
- [SQLDelight](https://cashapp.github.io/sqldelight/) - Multiplatform database
- [Kotlinx.datetime](https://github.com/Kotlin/kotlinx-datetime) - Date/time handling
- [Kotlinx.coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async programming
- [Ktor](https://ktor.io/) - Network client (Phase 3)

### Platform APIs
- Android: [FusedLocationProviderClient](https://developers.google.com/location-context/fused-location-provider)
- Android: [Geocoder](https://developer.android.com/reference/android/location/Geocoder)
- iOS: [CoreLocation](https://developer.apple.com/documentation/corelocation)
- iOS: [CLGeocoder](https://developer.apple.com/documentation/corelocation/clgeocoder)

---

## 🤝 Contributing

When implementing these steps:

1. **Follow the order**: Dependencies are important
2. **Write tests**: Aim for >80% coverage in shared module
3. **Document as you go**: Update inline docs and this file
4. **Commit regularly**: Reference TODO items in commit messages
5. **Create PRs**: Review before merging to main

---

## ✅ Definition of Done

For each phase:

- [ ] All tasks completed
- [ ] Unit tests written and passing
- [ ] Platform integration tested (Android + iOS)
- [ ] Documentation updated
- [ ] Code reviewed
- [ ] Performance acceptable (no ANRs, battery drain <5%)
- [ ] TODO.md and ROADMAP.md updated

---

**Next Review:** 2025-12-01
**Questions/Discussion:** GitHub Issues
