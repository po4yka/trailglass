# Travel Logger – Product & Technical Specification (EN)

(Full specification for a travel logging application that tracks trips in the background, builds timelines and memories, and syncs data across iOS and Android. The app uses native UI on each platform, a shared Kotlin Multiplatform core, Material 3 Expressive design on Android, and Liquid Glass styling on iOS.)

---

## 1. Vision & Product Overview

**Core idea**  
A personal, smart travel journal that automatically tracks the user’s movement, builds trip timelines, suggests adding photos and notes, and shows travel progress across the world. All data is synchronized between iOS and Android through a cloud backend.

**Guiding principles**
- **Auto first, manual friendly** – the app automatically collects raw data (location, days/years, countries) but lets the user create curated, beautifully presented stories (Memories).
- **Offline‑first** – everything works without a constant internet connection; sync is asynchronous and done as a background task.
- **Privacy‑first** – the user controls tracking modes, can pause or disable tracking, and can export and delete data.
- **Native UX** – each platform feels truly native: Material 3 Expressive on Android, Liquid Glass on iOS.
- **Shared brain** – business logic, data model, and sync are implemented in Kotlin Multiplatform (KMP); UI and system APIs are native.

---

## 2. Core Product Entities

### 2.1. LocationSample (raw points)
Raw GPS/network points recorded by the app:
- `id`
- `timestamp`
- `lat`, `lon`
- `accuracy`
- `speed`, `bearing` (optional)
- `source` (gps / network / visit / significant‑change)
- `tripId` (nullable)
- `uploadedAt` (nullable)
- `deviceId`, `userId`

All higher‑level timelines are derived from this data.

### 2.2. PlaceVisit (stop)
A cluster of points where the user stayed for a noticeable time:
- `id`
- `startTime`, `endTime`
- `centerLat`, `centerLon`
- `approxAddress` / `poiName` (via reverse geocoding)
- `city`, `countryCode`
- `locationSampleIds[]`

Used in timelines and inside Memories.

### 2.3. RouteSegment (travel leg)
A segment between two PlaceVisits or between day start/end:
- `id`
- `startTime`, `endTime`
- `fromPlaceVisitId` (nullable)
- `toPlaceVisitId` (nullable)
- `locationSampleIds[]`
- `transportType` (inferred: walk, car, train, plane, unknown)

### 2.4. Trip & TripDay
Logical grouping of movement: a trip, vacation, business travel. Can be auto‑detected or created manually.

**Trip**
- `id`
- `name` (optional)
- `startTime`, `endTime` (nullable until finished)
- `days: List<TripDay>`
- `primaryCountry`

**TripDay**
- `id`
- `tripId`
- `date`
- `items: List<TimelineItem>` (see below)

### 2.5. TimelineItem
An item on the daily timeline:
- type: `PlaceVisit`, `RouteSegment`, `JournalEntry`, `PhotoGroup`, (optionally `SystemEvent`)
- reference to the underlying entity

### 2.6. Memories (curated stories)
User‑created projects – mini storylines on top of raw data.

**Memory**
- `id`
- `name` (e.g., "Summer in Europe", "Vienna New Year")
- `startDate`, `endDate`
- `coverPhotoId` (optional)
- `description` (optional)
- optional linkage to `Trip` / `TripDay`

**MemoryDay**
- `id`
- `memoryId`
- `date`
- `journalEntryId` (optional day‑level note)
- `photoIds[]`
- `memorabiliaIds[]`

### 2.7. JournalEntry
A journal note:
- `id`
- `createdAt`, `updatedAt`
- `tripDayId` or `memoryDayId`
- `placeVisitId` / `routeSegmentId` (optional)
- `text` (plain/markdown)
- `mood`, `tags[]` (optional)

### 2.8. PhotoAttachment
A photo attached to a day, place, route, or Memory:
- `id`
- `localIdentifier` (PHAsset id / contentUri)
- `remoteUrl` (when uploaded to cloud)
- `thumbnailUrl` (optional)
- `ownerEntryId` / `placeVisitId` / `memoryDayId`
- `takenAt`, `lat`, `lon` (when available from EXIF)

### 2.9. Memorabilia
Small “artifacts”: ticket, receipt, postcard – usually represented as sticker‑style crops from photos.
- `id`
- `memoryDayId`
- `imageId`
- `title`
- `note`

### 2.10. CountryVisit & WorldStats
Used by the World Meter section.

**CountryVisit**
- `id`
- `countryCode`
- `firstVisitedAt`
- `lastVisitedAt`
- `totalDays`
- `visitsCount`

**WorldStats**
- number of countries, % of world visited, breakdown by continents.

---

## 3. Key User Scenarios

### 3.1. Automatic trip journal
1. User enables background tracking.
2. App records `LocationSample` in background with different modes (Idle/Passive/Active).
3. Shared core periodically:
   - smooths the raw track
   - groups points into `PlaceVisit`
   - builds `RouteSegment`
   - groups them into days and trips.
4. In the Stats/Timeline UI the user sees:
   - days on the move
   - cities/countries
   - route map.

### 3.2. Quick day journaling
1. User opens Timeline or a Memory.
2. In the "Day X" block taps "Tap to add Day journal".
3. Enters free‑form text (plain or markdown).
4. A `JournalEntry` is created and shown under that day.

### 3.3. Photo suggestions
1. Algorithm scans the photo library by time and (when available) location.
2. For each `TripDay` or `PlaceVisit`, relevant photos are picked.
3. User sees a prompt: "You took 12 photos near Tbilisi Old Town – add them to your Memory?".
4. Selected photos create `PhotoAttachment` and/or `PhotoGroup` items in the timeline/Memory.

### 3.4. Creating a Memory (curated story)
1. User goes to **Memories** tab.
2. Taps "+ Create Memory".
3. Fills in:
   - `name` (e.g. "Vienna New Year 2025")
   - `date range` via calendar picker.
4. On Memory detail screen:
   - sets a cover photo
   - attaches photos per day
   - writes notes
   - adds memorabilia (e.g. concert ticket photo, restaurant receipt).
5. App may auto‑suggest a draft Memory based on trip data.

### 3.5. World Meter – travel progress
1. **World Meter** tab shows:
   - number of visited countries
   - % of world visited
   - a world map with highlighted countries
   - list of countries (with flags and last visit date).
2. "Manage Countries" screen:
   - user can manually mark visited countries
   - or adjust auto‑detected ones.
3. Data is stored as `CountryVisit` and combines automatic detection with manual overrides.

### 3.6. Cross‑device workflow
1. User signs in (email/Apple/Google).
2. Enables tracking on iPhone.
3. Later opens the app on an Android tablet:
   - sees the same timeline, trips, Memories, and World Meter data
   - can edit entries and create new Memories.
4. All changes are synced via the backend.

---

## 4. Information Architecture & Navigation

### 4.1. Bottom navigation (TabBar)

Three main sections:

1. **Stats**
   - Segmented control: Month / Year
   - Metric cards:
     - Countries (number of unique countries)
     - Memories (number of stories)
     - Days traveling (days on the move + progress bar for the year)
   - Blocks:
     - Timeline preview (e.g. "Tbilisi – 1 day today")
     - Top countries (link to detailed view).

2. **Memories**
   - List of Memories (or empty state with CTA)
   - FAB / "+ Create Memory" button
   - Memory detail:
     - header: name, date range, duration, "Last day" / "Ongoing" status
     - map of the route for this Memory (if data is available)
     - Memorabilia section
     - Photos section (cover + gallery)
     - Day 1/Day 2/... blocks with journaling and photos.

3. **World Meter**
   - Main screen with world map, visit counter, % of world
   - List of countries, grouped by continent (with flags and last visit date)
   - "Manage Countries" screen with continent chips and list of countries to toggle.

### 4.2. Additional screens

- **Full Timeline**
  - global view of days
  - optionally grouped by trips or years.

- **Settings / Privacy**
  - tracking mode (Always / Trips only / Off)
  - sampling frequency / precision
  - local storage & export
  - account management.

- **Permissions onboarding**
  - explains why the app needs background location and photo access
  - shows clear, user‑centric value of granting those permissions.

---

## 5. System Architecture (KMP + native shells)

### 5.1. Overall approach

- **KMP shared module** – the “brain” of the app:
  - domain model
  - use cases
  - repositories
  - sync engine
  - location processing and statistics.

- **Android app** – Jetpack Compose + Material 3 Expressive:
  - UI, navigation, DI (Hilt)
  - FusedLocationProvider, Foreground Service, WorkManager
  - access to camera/photos, notifications
  - integration with shared via ViewModels + Flow.

- **iOS app** – SwiftUI + Liquid Glass:
  - UI, navigation, Swift Concurrency
  - CoreLocation, Background Tasks
  - Photos framework, notifications
  - integration with shared via generated Kotlin framework + ObservableObject.

### 5.2. KMP module structure

Example layout:

- `:shared`
  - `:core`
    - `domain` – entities and use cases
    - `data` – repositories, SQLDelight, Ktor client
    - `sync` – sync engine, queues
    - `location` – raw location processing and aggregation
    - `world` – CountryVisit and WorldStats calculations
  - `:feature-timeline`
  - `:feature-memories`
  - `:feature-worldmeter`

Public API is exposed through controllers/facades:

```kotlin
class TimelineController(
    private val getYearStats: GetYearStatsUseCase,
    private val getTimelineForDay: GetTimelineForDayUseCase,
) {
    val state: StateFlow<TimelineState>
    suspend fun loadYear(year: Int)
    suspend fun selectDay(date: LocalDate)
}
```

Android and iOS consume these controllers without knowing implementation details.

### 5.3. expect/actual abstractions

Shared code uses expect/actual for platform‑specific details:

- `currentInstant()` – current timestamp
- `Log` – logging utility
- file system access for temporary media files.

Example:

```kotlin
expect fun currentInstant(): Instant

expect object Log {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
```

---

## 6. Data Layer: Local Storage & Sync

### 6.1. Local database

**SQLDelight** as the primary DB technology:
- single schema, multiple drivers:
  - `AndroidSqliteDriver`
  - `NativeSqliteDriver` (iOS).

Tables:
- `location_samples`
- `place_visits`
- `route_segments`
- `trips`, `trip_days`
- `memories`, `memory_days`
- `journal_entries`
- `photo_attachments`
- `memorabilia`
- `country_visits`

Each entity includes:
- `id`
- `user_id`
- `device_id`
- `created_at`
- `updated_at`
- `deleted_at` (for soft delete).

### 6.2. Cloud backend

Possible options:

1. **Firestore + Cloud Storage** (MVP)
   - fast to start, ready‑made SDKs
   - collections per entity
   - files in Storage.

2. **Custom backend** (Ktor/Node/Rails + Postgres + S3‑compatible storage)
   - REST/GraphQL API for syncing
   - full control + self‑hosting later.

### 6.3. SyncEngine (shared)

Responsibilities:
- track local changes (`updatedAt`/`deletedAt`)
- build outgoing batches
- fetch remote changes with `updatedAt > lastPulledAt`
- resolve conflicts.

Base approach:
- **Last Writer Wins (LWW)** using `updatedAt` for most entities.
- Optional advanced merges (e.g. for `JournalEntry` by creating comments instead of overwriting).

Interface:

```kotlin
interface SyncEngine {
    suspend fun syncOnce()
    fun observeStatus(): Flow<SyncStatus>
}
```

Android/iOS schedule `syncOnce()` via WorkManager / BGTaskScheduler.

---

## 7. Location & Battery Strategy

### 7.1. Tracking modes (state machine)

States:
- `Idle` – low activity, only significant changes
- `Passive` – normal city movement
- `Active` – intense movement (trip in progress).

Transitions:
- Idle → Passive: user moved > X meters over Y minutes, or manually switched to "In trip".
- Passive → Active: continuous high speed or major changes of area/city.
- Active → Passive/Idle: long‑term inactivity.

### 7.2. iOS implementation

- `startMonitoringSignificantLocationChanges` for Idle
- `startMonitoringVisits` to detect PlaceVisits
- In Active mode: `startUpdatingLocation` with `allowsBackgroundLocationUpdates = true`, appropriate `desiredAccuracy` and `distanceFilter`.
- Use Location background mode and `BGTaskScheduler` for periodic processing/sync.

### 7.3. Android implementation

- **Foreground Service** in Active mode (with ongoing notification).
- `FusedLocationProviderClient` with different `LocationRequest`:
  - Idle: `PRIORITY_PASSIVE` / `PRIORITY_BALANCED_POWER_ACCURACY`, large interval
  - Active: `PRIORITY_HIGH_ACCURACY`, smaller interval & minDistance.
- Activity Recognition API (when available) to infer transport type.
- WorkManager/AlarmManager to revive tracking if it gets killed.

### 7.4. Processing raw location in KMP

Platform code receives `RawLocationSample` and forwards it to shared module:

```kotlin
interface LocationRecorder {
    suspend fun onRawSample(sample: RawLocationSample)
}
```

Shared layer:
- stores samples in DB
- periodically runs a processing pipeline:
  - clustering into PlaceVisits
  - building RouteSegments
  - grouping by day and Trip.

---

## 8. Photos & Media

### 8.1. Access to photo library

- iOS: Photos framework (PHAsset), with limited‑scope permissions when possible.
- Android: MediaStore / Photo Picker API.

The app does **not** import the entire library:
- it filters candidate photos by time + (optionally) location
- suggests them to the user as attachments for specific days or Memories.

### 8.2. Storage model

Only metadata is stored in DB:
- `localIdentifier` (PHAsset id / contentUri)
- `remoteUrl` (in cloud storage).

Files:
- temporarily stored during upload
- uploaded to Cloud Storage or similar
- cleaned up locally afterwards.

### 8.3. Relation to Memories & Timeline

- `TripDay` and `MemoryDay` hold lists of `PhotoAttachment`.
- Memory cover uses a special photo with `isCover` flag.

---

## 9. Security, Privacy & Export

### 9.1. Privacy modes

- Tracking modes:
  - `Always` – continuous logging
  - `Trips only` – logging between explicit "Start trip" / "End trip"
  - `Off` – tracking disabled.

- Additional settings:
  - reduced precision for certain views (e.g. rounding location on maps/World Meter)
  - temporary tracking pause.

### 9.2. Local protection

- Rely on OS disk encryption (iOS Data Protection, Android secure storage) and optionally EncryptedFile/SQLCipher.
- Optional in‑app lock (PIN/biometrics).

### 9.3. Cloud protection

- All traffic over HTTPS.
- Authentication via tokens (OAuth/JWT/Firebase Auth etc.).

### 9.4. Export & deletion

- Export single trip:
  - JSON (all entities)
  - GPX/KML for route
  - zip with thumbnails.
- Export full archive.
- Full account deletion with all associated data on server and locally.

---

## 10. UI/UX: Visual Style & Platform‑specific Design

### 10.1. Android – Material 3 Expressive

- Build on Material 3 with Expressive direction:
  - bold shapes and expressive surfaces
  - rich color palette
  - motion that underlines transitions between Stats, Memories, and World Meter.
- Theme:
  - dynamic color (Material You) + custom travel palette
  - large stat cards, smooth corner radii, outlined and filled buttons.
- Compose:
  - use `MaterialTheme` tokens for typography, shapes, and colors
  - define custom components such as `StatCard`, `MemoryCard`, `CountryChip`.

### 10.2. iOS – Liquid Glass

- Liquid Glass visual language:
  - semi‑transparent layers
  - blurred background content
  - sense of depth and stacked materials.
- SwiftUI:
  - heavy use of `.background(.ultraThinMaterial)` or similar materials
  - large titles, smooth transitions.
- Important: contrast & readability
  - ensure high contrast for text and icons on glass backgrounds
  - provide fallbacks for older/low‑power devices (reduced blur/transparency).

### 10.3. Cross‑platform consistency

Even though styles differ, preserve:
- same information architecture (Stats / Memories / World Meter)
- consistent naming of entities and screens
- similar overall mood (dark theme, accent colors from similar spectrum).

---

## 11. KMP Adoption Strategy

### Phase 1 – Shared brain without sync

- Implement domain model and use cases in KMP for:
  - timeline building
  - stats computation
  - Memories & World Meter.
- Location capture and local storage can initially be platform‑specific; processing moves into shared.

### Phase 2 – Shared database

- Move storage to SQLDelight.
- Platforms forward raw samples and photo metadata into shared repositories.
- All screens consume data via shared use cases/controllers.

### Phase 3 – Sync & media uploads

- Add Ktor client and cloud backend integration.
- Implement SyncEngine.
- Add file uploads for photos and cloud references.

---

## 12. Future Directions

- **Smart summaries** – auto‑generated trip summaries (can later be powered by LLMs).
- **Shared trips** – invite links, collaborative Memories.
- **Widgets & reminders** – "On this day 3 years ago you were in…".
- **Calendar & photos integration** – linking Memories with calendar events and photos.

---

This document is the English version of the core specification used for product design, backend planning, KMP core implementation, and native Android/iOS UI development.

