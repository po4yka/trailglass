# TrailGlass - Implementation TODO

This document tracks all implementation tasks for the TrailGlass travel logging application. Tasks are organized by development phase and component area.

---

## Phase 1: Foundation & Shared Core (Without Sync)

### Project Setup
- [x] Initialize Kotlin Multiplatform project structure
- [ ] Configure Gradle build files for KMP, Android, and iOS
- [ ] Set up dependency management (versions catalog)
- [ ] Configure SQLDelight for multiplatform database
- [ ] Set up test infrastructure (JUnit, XCTest)
- [ ] Configure CI/CD pipeline (GitHub Actions / GitLab CI)
- [ ] Set up code quality tools (ktlint, detekt, SwiftLint)

### Shared Module - Domain Layer
- [ ] Define core domain entities
  - [ ] `LocationSample` data class
  - [ ] `PlaceVisit` data class
  - [ ] `RouteSegment` data class
  - [ ] `Trip` and `TripDay` data classes
  - [ ] `TimelineItem` sealed class
  - [ ] `Memory` and `MemoryDay` data classes
  - [ ] `JournalEntry` data class
  - [ ] `PhotoAttachment` data class
  - [ ] `Memorabilia` data class
  - [ ] `CountryVisit` and `WorldStats` data classes

### Shared Module - Data Layer (Local)
- [ ] Design SQLDelight schema
  - [ ] `location_samples` table
  - [ ] `place_visits` table
  - [ ] `route_segments` table
  - [ ] `trips` and `trip_days` tables
  - [ ] `memories` and `memory_days` tables
  - [ ] `journal_entries` table
  - [ ] `photo_attachments` table
  - [ ] `memorabilia` table
  - [ ] `country_visits` table
- [ ] Implement database migrations
- [ ] Create repository interfaces
- [ ] Implement SQLDelight repositories
  - [ ] `LocationRepository`
  - [ ] `TripRepository`
  - [ ] `MemoryRepository`
  - [ ] `JournalRepository`
  - [ ] `PhotoRepository`
  - [ ] `WorldStatsRepository`
- [ ] Add database indices for performance
- [ ] Implement soft delete mechanism

### Shared Module - Location Processing
- [ ] Create `LocationRecorder` interface
- [ ] Implement location smoothing algorithm
- [ ] Build PlaceVisit clustering algorithm
  - [ ] DBSCAN or similar spatial clustering
  - [ ] Time-based grouping
  - [ ] Duration threshold filtering
- [ ] Implement RouteSegment builder
  - [ ] Path simplification (Douglas-Peucker)
  - [ ] Transport type inference
- [ ] Create TripDay aggregator
- [ ] Implement automatic Trip detection
  - [ ] Home location detection
  - [ ] Trip boundary detection
  - [ ] Multi-day trip grouping

### Shared Module - Use Cases
- [ ] Timeline use cases
  - [ ] `GetYearStatsUseCase`
  - [ ] `GetMonthStatsUseCase`
  - [ ] `GetTimelineForDayUseCase`
  - [ ] `GetTripDetailsUseCase`
- [ ] Memory use cases
  - [ ] `CreateMemoryUseCase`
  - [ ] `GetMemoryListUseCase`
  - [ ] `GetMemoryDetailsUseCase`
  - [ ] `UpdateMemoryUseCase`
  - [ ] `DeleteMemoryUseCase`
  - [ ] `SuggestMemoryFromTripUseCase`
- [ ] Journal use cases
  - [ ] `CreateJournalEntryUseCase`
  - [ ] `UpdateJournalEntryUseCase`
  - [ ] `GetEntriesForDayUseCase`
- [ ] World Meter use cases
  - [ ] `GetVisitedCountriesUseCase`
  - [ ] `GetWorldStatsUseCase`
  - [ ] `UpdateCountryVisitUseCase`
  - [ ] `CalculateTravelProgressUseCase`
- [ ] Photo use cases
  - [ ] `SuggestPhotosForDayUseCase`
  - [ ] `AttachPhotoUseCase`
  - [ ] `GetPhotosForMemoryUseCase`

### Shared Module - Controllers/Facades
- [ ] `TimelineController` with StateFlow
- [ ] `MemoryController` with StateFlow
- [ ] `WorldMeterController` with StateFlow
- [ ] `JournalController` with StateFlow

### Shared Module - expect/actual
- [ ] `expect fun currentInstant(): Instant`
- [ ] `expect object Log` for logging
- [ ] `expect class FileManager` for temp file access
- [ ] Platform-specific database drivers setup

### Android App - Setup
- [ ] Configure Material 3 Expressive theme
  - [ ] Define color palette (dynamic + custom)
  - [ ] Configure typography scale
  - [ ] Define shape tokens
- [ ] Set up Jetpack Compose navigation
- [ ] Configure Hilt dependency injection
- [ ] Implement splash screen
- [ ] Create base Activity and theme setup

### Android App - Permissions & Onboarding
- [ ] Design permission request screens
- [ ] Implement location permission flow
  - [ ] Coarse location
  - [ ] Fine location
  - [ ] Background location
- [ ] Implement photo library permission flow
- [ ] Create onboarding carousel
- [ ] Add permission rationale explanations

### Android App - Location Tracking
- [ ] Implement `FusedLocationProviderClient` wrapper
- [ ] Create tracking mode state machine
  - [ ] Idle mode implementation
  - [ ] Passive mode implementation
  - [ ] Active mode implementation
- [ ] Build Foreground Service for Active tracking
  - [ ] Notification channel setup
  - [ ] Ongoing notification design
  - [ ] Service lifecycle management
- [ ] Implement WorkManager for periodic sync
- [ ] Add Activity Recognition (optional)
- [ ] Connect to shared `LocationRecorder`

### Android App - UI Components
- [ ] Create custom composables
  - [ ] `StatCard` component
  - [ ] `MemoryCard` component
  - [ ] `CountryChip` component
  - [ ] `TimelineItem` component
  - [ ] `DayHeader` component
  - [ ] `MapView` wrapper
- [ ] Build bottom navigation bar
- [ ] Implement pull-to-refresh patterns

### Android App - Stats Screen
- [ ] Implement Stats ViewModel
- [ ] Build month/year segmented control
- [ ] Create metric cards UI
  - [ ] Countries counter
  - [ ] Memories counter
  - [ ] Days traveling with progress bar
- [ ] Build timeline preview section
- [ ] Implement top countries list
- [ ] Add tap interactions and navigation

### Android App - Memories Screen
- [ ] Implement Memories ViewModel
- [ ] Build Memories list screen
- [ ] Create empty state with CTA
- [ ] Design FAB for creating Memory
- [ ] Build Memory detail screen
  - [ ] Header with name, dates, duration
  - [ ] Map view for route
  - [ ] Memorabilia section
  - [ ] Photo gallery
  - [ ] Day-by-day timeline
- [ ] Implement Memory creation flow
- [ ] Add Memory editing capabilities

### Android App - World Meter Screen
- [ ] Implement WorldMeter ViewModel
- [ ] Build world map view
  - [ ] Integrate map library (Google Maps / Mapbox)
  - [ ] Highlight visited countries
- [ ] Create visit counter display
- [ ] Show percentage of world visited
- [ ] Build country list (grouped by continent)
- [ ] Implement "Manage Countries" screen
  - [ ] Continent filter chips
  - [ ] Country toggle list with flags

### Android App - Additional Screens
- [ ] Build full Timeline screen
- [ ] Create Settings screen
  - [ ] Tracking mode selector
  - [ ] Precision settings
  - [ ] Privacy controls
  - [ ] Account management
- [ ] Implement data export screen
- [ ] Add about/help screens

### iOS App - Setup
- [ ] Configure Xcode project with SwiftUI
- [ ] Set up Liquid Glass design system
  - [ ] Define color palette
  - [ ] Configure materials and blur styles
  - [ ] Typography setup
- [ ] Integrate KMP framework
- [ ] Set up SwiftUI navigation (NavigationStack)
- [ ] Create base app structure

### iOS App - Permissions & Onboarding
- [ ] Design permission request views
- [ ] Implement location permission flow
  - [ ] When In Use authorization
  - [ ] Always authorization
- [ ] Implement photo library permission flow
- [ ] Create onboarding flow
- [ ] Add Info.plist permission descriptions

### iOS App - Location Tracking
- [ ] Implement `CLLocationManager` wrapper
- [ ] Create tracking mode state machine
  - [ ] Idle: significant location changes
  - [ ] Passive: visit monitoring
  - [ ] Active: continuous updates
- [ ] Configure background location updates
- [ ] Implement `BGTaskScheduler` for processing
- [ ] Set up background modes in capabilities
- [ ] Connect to shared `LocationRecorder`

### iOS App - UI Components
- [ ] Create SwiftUI reusable views
  - [ ] `StatCardView`
  - [ ] `MemoryCardView`
  - [ ] `CountryChipView`
  - [ ] `TimelineItemView`
  - [ ] `DayHeaderView`
  - [ ] `MapView` wrapper
- [ ] Build tab bar navigation
- [ ] Implement pull-to-refresh

### iOS App - Stats Screen
- [ ] Create StatsViewModel (ObservableObject)
- [ ] Build month/year picker
- [ ] Create metric cards UI
  - [ ] Countries counter
  - [ ] Memories counter
  - [ ] Days traveling with progress
- [ ] Build timeline preview
- [ ] Create top countries list
- [ ] Wire up navigation

### iOS App - Memories Screen
- [ ] Create MemoriesViewModel
- [ ] Build Memories list view
- [ ] Design empty state
- [ ] Create floating action button / toolbar button
- [ ] Build Memory detail view
  - [ ] Header with glass effect
  - [ ] Map view
  - [ ] Memorabilia section
  - [ ] Photo gallery with glass cards
  - [ ] Day timeline
- [ ] Implement Memory creation sheet
- [ ] Add Memory editing

### iOS App - World Meter Screen
- [ ] Create WorldMeterViewModel
- [ ] Build map view
  - [ ] Integrate MapKit
  - [ ] Overlay visited countries
- [ ] Create visit counter
- [ ] Show world percentage
- [ ] Build country list (by continent)
- [ ] Implement "Manage Countries" view
  - [ ] Continent filters
  - [ ] Country list with toggles

### iOS App - Additional Screens
- [ ] Build full Timeline screen
- [ ] Create Settings screen
  - [ ] Tracking controls
  - [ ] Privacy options
  - [ ] Account section
- [ ] Implement export functionality
- [ ] Add about/help screens

---

## Phase 2: Shared Database & Photo Integration

### SQLDelight Migration
- [ ] Migrate any remaining platform-specific storage to SQLDelight
- [ ] Add missing indices and optimize queries
- [ ] Implement database versioning and migrations

### Photo Library Integration
- [ ] Android: Implement MediaStore/Photo Picker integration
  - [ ] Query photos by date range
  - [ ] Query photos by location
  - [ ] Handle permissions properly
- [ ] iOS: Implement Photos framework integration
  - [ ] Query PHAssets by date
  - [ ] Query PHAssets by location
  - [ ] Handle limited library access
- [ ] Shared: Photo suggestion algorithm
  - [ ] Time-based matching
  - [ ] Location-based matching
  - [ ] Confidence scoring
- [ ] Build photo attachment UI
  - [ ] Photo picker
  - [ ] Attachment confirmation
  - [ ] Photo preview/details

### Reverse Geocoding
- [ ] Android: Implement Geocoder wrapper
- [ ] iOS: Implement CLGeocoder wrapper
- [ ] Shared: Geocoding cache layer
- [ ] Add city/country extraction
- [ ] Implement POI name detection

### Map Integration
- [ ] Android: Google Maps / Mapbox setup
  - [ ] Route polyline rendering
  - [ ] Place markers
  - [ ] Clustering for many points
- [ ] iOS: MapKit integration
  - [ ] Route overlay
  - [ ] Annotations
  - [ ] Clustering

### Country Detection & Flags
- [ ] Build country code database (ISO 3166)
- [ ] Implement country boundary detection
- [ ] Add country flag assets
- [ ] Create continent groupings
- [ ] Implement auto-detection from location data

---

## Phase 3: Sync Engine & Cloud Backend

### Backend Setup
- [ ] Choose backend architecture (Firebase vs Custom)
- [ ] Set up cloud database (Firestore/Postgres)
- [ ] Configure cloud storage (Firebase Storage/S3)
- [ ] Implement authentication
  - [ ] Email/password
  - [ ] Apple Sign In
  - [ ] Google Sign In
- [ ] Create API endpoints (if custom backend)
  - [ ] User registration/login
  - [ ] Location data sync
  - [ ] Trip/Memory sync
  - [ ] Photo upload
  - [ ] Incremental sync endpoint

### Shared - Sync Engine
- [ ] Design sync protocol
  - [ ] Change tracking (updatedAt, deletedAt)
  - [ ] Conflict resolution strategy (LWW)
  - [ ] Batch operations
- [ ] Implement `SyncEngine` interface
- [ ] Build outgoing change tracker
- [ ] Build incoming change applier
- [ ] Implement conflict resolver
- [ ] Add sync state management
- [ ] Create sync status observables
- [ ] Implement retry logic with exponential backoff

### Shared - Network Layer
- [ ] Set up Ktor client
- [ ] Configure serialization (kotlinx.serialization)
- [ ] Implement authentication interceptor
- [ ] Add request/response logging
- [ ] Implement network error handling
- [ ] Build API client interfaces
  - [ ] LocationSyncApi
  - [ ] TripSyncApi
  - [ ] MemorySyncApi
  - [ ] PhotoSyncApi

### Photo Upload & Storage
- [ ] Implement photo compression
- [ ] Build upload queue
- [ ] Add progress tracking
- [ ] Implement thumbnail generation
- [ ] Handle upload failures and retry
- [ ] Implement cloud URL references
- [ ] Add local cache cleanup

### Android - Sync Integration
- [ ] Schedule periodic sync with WorkManager
- [ ] Implement sync on network change
- [ ] Add manual sync trigger
- [ ] Show sync status in UI
- [ ] Handle sync errors gracefully

### iOS - Sync Integration
- [ ] Schedule sync with BGTaskScheduler
- [ ] Implement sync on network change
- [ ] Add manual sync trigger (pull-to-refresh)
- [ ] Show sync status in UI
- [ ] Handle sync errors gracefully

### Multi-device Support
- [ ] Test cross-platform sync (iOS ↔ Android)
- [ ] Implement device management
- [ ] Add device-specific settings
- [ ] Handle simultaneous edits

---

## Phase 4: Polish & Advanced Features

### Performance Optimization
- [ ] Profile database queries
- [ ] Optimize location processing algorithms
- [ ] Implement pagination for long lists
- [ ] Add lazy loading for photos
- [ ] Optimize map rendering
- [ ] Reduce app size
- [ ] Battery usage optimization
- [ ] Memory leak detection and fixes

### Offline Capabilities
- [ ] Implement offline mode indicators
- [ ] Queue operations when offline
- [ ] Handle offline photo viewing
- [ ] Add offline map tiles (optional)
- [ ] Ensure graceful degradation

### Security & Privacy
- [ ] Implement local encryption (optional)
  - [ ] EncryptedSharedPreferences (Android)
  - [ ] Keychain (iOS)
  - [ ] SQLCipher (optional)
- [ ] Add in-app lock (PIN/biometrics)
- [ ] Implement tracking pause feature
- [ ] Add precision reduction option
- [ ] Build data export features
  - [ ] JSON export
  - [ ] GPX/KML export
  - [ ] Photo archive
- [ ] Implement account deletion
  - [ ] Server-side cleanup
  - [ ] Local data wipe

### Advanced UI/UX
- [ ] Add smooth animations and transitions
- [ ] Implement skeleton loading states
- [ ] Add haptic feedback (iOS)
- [ ] Improve error messages and empty states
- [ ] Add onboarding tooltips
- [ ] Implement search functionality
- [ ] Add filtering and sorting options

### Transport Type Detection
- [ ] Implement speed-based detection
- [ ] Use Activity Recognition API (Android)
- [ ] Add manual transport type override
- [ ] Create transport icons and UI

### Smart Features
- [ ] Auto-suggest Memory creation from trips
- [ ] Generate trip summaries
- [ ] Implement "On this day" reminders
- [ ] Add calendar integration (optional)
- [ ] Smart photo organization

### Widgets & Extensions
- [ ] Android widget (travel stats)
- [ ] iOS widget (WidgetKit)
- [ ] Today view stats
- [ ] Quick actions / shortcuts

---

## Phase 5: Testing & Launch Preparation

### Testing
- [ ] Write unit tests for shared core (>80% coverage)
- [ ] Write Android UI tests (Compose tests)
- [ ] Write iOS UI tests (XCUITest)
- [ ] Perform integration testing
- [ ] Test sync edge cases
- [ ] Test offline scenarios
- [ ] Battery drain testing
- [ ] Test on various devices
  - [ ] Android: different manufacturers, OS versions
  - [ ] iOS: different models, iOS versions
- [ ] Memory leak testing
- [ ] Performance benchmarking

### Quality Assurance
- [ ] Internal dogfooding
- [ ] Beta testing program
  - [ ] TestFlight (iOS)
  - [ ] Google Play Internal Testing (Android)
- [ ] Gather beta feedback
- [ ] Fix critical bugs
- [ ] Accessibility audit
  - [ ] Screen reader support
  - [ ] Dynamic type support
  - [ ] Color contrast validation

### Documentation
- [ ] Write user guide / help documentation
- [ ] Create privacy policy
- [ ] Write terms of service
- [ ] Document API (if public)
- [ ] Create developer documentation
- [ ] Add inline code documentation

### App Store Preparation
- [ ] Create app screenshots
- [ ] Write app store descriptions
- [ ] Design app icon
- [ ] Create promotional graphics
- [ ] Prepare demo video
- [ ] Set up App Store / Play Store listings
- [ ] Configure in-app purchases (if applicable)
- [ ] Set up analytics (optional)

### Launch
- [ ] Final testing round
- [ ] Submit to App Store review
- [ ] Submit to Google Play review
- [ ] Monitor crash reports
- [ ] Prepare launch announcement
- [ ] Set up user support channels

---

## Future Enhancements (Post-Launch)

### Sharing & Collaboration
- [ ] Implement shared trips
- [ ] Add invite links
- [ ] Build collaborative Memories
- [ ] Add commenting on shared content

### AI/ML Features
- [ ] LLM-powered trip summaries
- [ ] Smart photo selection
- [ ] Automatic POI detection
- [ ] Travel recommendations

### Integrations
- [ ] Calendar integration
- [ ] Photos app deep integration
- [ ] Import from other travel apps
- [ ] Export to social media

### Monetization (if applicable)
- [ ] Implement premium features
- [ ] Add cloud storage tiers
- [ ] Build subscription management

---

## Notes

- Tasks marked with `[x]` are completed
- Tasks marked with `[ ]` are pending
- Update this document as tasks are completed
- Use git commits to reference completed tasks
- Review and reprioritize regularly

---

**Last updated:** 2025-11-16
