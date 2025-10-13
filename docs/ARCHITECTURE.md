# Architecture

TrailGlass uses a **Kotlin Multiplatform (KMP)** architecture with shared business logic and platform-specific UI implementations.

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Layer Responsibilities](#layer-responsibilities)
- [State Management](#state-management)
- [Dependency Injection](#dependency-injection)
- [Navigation](#navigation)
- [Data Flow](#data-flow)
- [Platform Integration](#platform-integration)
- [Design Patterns](#design-patterns)

## Overview

TrailGlass follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Android Compose / iOS SwiftUI)        │
└─────────────────────────────────────────┘
            ↓ Controllers/ViewModels
┌─────────────────────────────────────────┐
│         Domain Layer (Shared)           │
│  (Use Cases, Business Logic, Models)    │
└─────────────────────────────────────────┘
            ↓ Repositories
┌─────────────────────────────────────────┐
│         Data Layer (Shared)             │
│  (Repositories, Database, Services)     │
└─────────────────────────────────────────┘
            ↓ Platform APIs
┌─────────────────────────────────────────┐
│      Platform Layer (expect/actual)     │
│  (Location, Photos, Geocoding, etc.)    │
└─────────────────────────────────────────┘
```

### Architecture Principles

1. **Unidirectional Data Flow**: Data flows down, events flow up
2. **Single Source of Truth**: Database is the single source of truth
3. **Separation of Concerns**: Each layer has a single responsibility
4. **Platform Independence**: Business logic is 100% shared
5. **Testability**: All layers are testable in isolation

## Project Structure

```
trailglass/
├── shared/                          # Shared Kotlin Multiplatform code
│   └── src/
│       ├── commonMain/              # Platform-agnostic code
│       │   ├── kotlin/
│       │   │   └── com/po4yka/trailglass/
│       │   │       ├── data/        # Data layer
│       │   │       │   ├── repository/     # Repository implementations
│       │   │       │   └── service/        # Business services
│       │   │       ├── domain/      # Domain layer
│       │   │       │   ├── model/          # Domain models
│       │   │       │   └── repository/     # Repository interfaces
│       │   │       ├── feature/     # Feature modules
│       │   │       │   ├── stats/          # Statistics feature
│       │   │       │   ├── timeline/       # Timeline feature
│       │   │       │   ├── map/            # Map feature
│       │   │       │   └── photo/          # Photo feature
│       │   │       ├── location/    # Location tracking
│       │   │       │   ├── processing/     # Location processing
│       │   │       │   ├── tracking/       # Location tracking
│       │   │       │   └── geocoding/      # Reverse geocoding
│       │   │       └── logging/     # Structured logging
│       │   └── sqldelight/          # Database schema
│       │       └── com/po4yka/trailglass/db/
│       ├── androidMain/             # Android-specific code
│       │   └── kotlin/
│       │       └── com/po4yka/trailglass/
│       │           ├── location/    # Android location APIs
│       │           ├── photo/       # MediaStore integration
│       │           └── geocoding/   # Android Geocoder
│       ├── iosMain/                 # iOS-specific code
│       │   └── kotlin/
│       │       └── com/po4yka/trailglass/
│       │           ├── location/    # CLLocationManager
│       │           ├── photo/       # Photos framework
│       │           └── geocoding/   # CLGeocoder
│       └── commonTest/              # Shared tests
│           └── kotlin/
│               └── com/po4yka/trailglass/
│                   ├── data/repository/   # Repository tests
│                   └── feature/           # Use case tests
│
├── composeApp/                      # Android app
│   └── src/
│       ├── androidMain/
│       │   └── kotlin/
│       │       └── com/po4yka/trailglass/
│       │           ├── ui/          # UI layer
│       │           │   ├── screens/       # Screen composables
│       │           │   ├── components/    # Reusable components
│       │           │   └── theme/         # Material 3 theme
│       │           └── location/   # Foreground service
│       └── androidInstrumentedTest/  # Android UI tests
│
└── iosApp/                          # iOS app
    ├── iosApp/                      # SwiftUI views
    │   ├── Views/                   # Screen views
    │   ├── Components/              # Reusable components
    │   └── Theme/                   # Design system
    └── iosAppUITests/               # iOS UI tests
```

## Layer Responsibilities

### 1. Domain Layer (Shared - commonMain)

**Responsibility**: Contains business logic, use cases, and domain models.

**Location**: `shared/src/commonMain/kotlin/.../domain/`

**Components**:

#### Domain Models
Pure Kotlin data classes representing business entities:

```kotlin
// Domain model example
data class PlaceVisit(
    val id: String,
    val tripId: String,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val city: String?,
    val country: String?,
    // ... other fields
)
```

**Characteristics**:
- No Android/iOS dependencies
- Immutable data classes
- Business logic validation
- Domain-specific types (not primitives)

#### Repository Interfaces
Define contracts for data access:

```kotlin
interface PlaceVisitRepository {
    fun getPlaceVisitById(id: String, userId: String): PlaceVisit?
    fun getPlaceVisitsForTimeRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<PlaceVisit>>
    suspend fun insertPlaceVisit(visit: PlaceVisit)
    suspend fun updatePlaceVisit(visit: PlaceVisit)
    suspend fun deletePlaceVisit(id: String, userId: String)
}
```

### 2. Data Layer (Shared - commonMain)

**Responsibility**: Implements data access and persistence.

**Location**: `shared/src/commonMain/kotlin/.../data/`

**Components**:

#### Repository Implementations
Implement repository interfaces using SQLDelight:

```kotlin
class PlaceVisitRepositoryImpl(
    private val database: TrailGlassDatabase
) : PlaceVisitRepository {

    override fun getPlaceVisitById(id: String, userId: String): PlaceVisit? {
        return database.placeVisitsQueries
            .getById(id, userId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun getPlaceVisitsForTimeRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<PlaceVisit>> {
        return database.placeVisitsQueries
            .getForTimeRange(userId, startTime, endTime)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { dbModel -> dbModel.toDomainModel() } }
    }
}
```

#### Database (SQLDelight)
Type-safe SQL queries:

```sql
-- PlaceVisits.sq
getById:
SELECT * FROM place_visits
WHERE id = ? AND user_id = ?;

getForTimeRange:
SELECT * FROM place_visits
WHERE user_id = ?
  AND start_time >= ?
  AND end_time <= ?
ORDER BY start_time ASC;
```

**Benefits**:
- Compile-time SQL verification
- Generated type-safe APIs
- Cross-platform (Android, iOS, JVM)
- Flow-based reactive queries

### 3. Feature Layer (Shared - commonMain)

**Responsibility**: Coordinates business logic for specific features.

**Location**: `shared/src/commonMain/kotlin/.../feature/`

**Components**:

#### Use Cases
Single-responsibility operations:

```kotlin
class GetTimelineForDayUseCase(
    private val placeVisitRepository: PlaceVisitRepository,
    private val routeSegmentRepository: RouteSegmentRepository
) {
    suspend fun execute(
        userId: String,
        date: LocalDate
    ): List<TimelineItemUI> {
        // Load visits and routes for the day
        val visits = placeVisitRepository.getPlaceVisitsForDay(userId, date)
        val routes = routeSegmentRepository.getRouteSegmentsForDay(userId, date)

        // Combine and sort chronologically
        return combineTimeline(visits, routes)
    }
}
```

#### Controllers
Manage UI state with StateFlow:

```kotlin
class TimelineController(
    private val getTimelineUseCase: GetTimelineForDayUseCase,
    private val coroutineScope: CoroutineScope,
    private val userId: String
) {
    data class TimelineState(
        val selectedDate: LocalDate?,
        val items: List<TimelineItemUI>,
        val isLoading: Boolean,
        val error: String?
    )

    private val _state = MutableStateFlow(TimelineState(
        selectedDate = null,
        items = emptyList(),
        isLoading = false,
        error = null
    ))
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    fun selectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date, isLoading = true) }

        coroutineScope.launch {
            try {
                val items = getTimelineUseCase.execute(userId, date)
                _state.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

### 4. Platform Layer (expect/actual)

**Responsibility**: Platform-specific implementations.

**Location**: `shared/src/androidMain/` and `shared/src/iosMain/`

**Pattern**: expect/actual

```kotlin
// commonMain - Interface
expect class LocationTracker {
    val locationUpdates: Flow<LocationSample>
    suspend fun startTracking(mode: TrackingMode)
    suspend fun stopTracking()
}

// androidMain - Implementation
actual class LocationTracker(
    private val context: Context
) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    actual val locationUpdates: Flow<LocationSample> = /* ... */

    actual suspend fun startTracking(mode: TrackingMode) {
        // Android FusedLocationProviderClient implementation
    }
}

// iosMain - Implementation
actual class LocationTracker {
    private val locationManager = CLLocationManager()

    actual val locationUpdates: Flow<LocationSample> = /* ... */

    actual suspend fun startTracking(mode: TrackingMode) {
        // iOS CLLocationManager implementation
    }
}
```

### 5. Presentation Layer (Platform-Specific)

**Responsibility**: UI rendering and user interaction.

#### Android (Jetpack Compose)

```kotlin
@Composable
fun TimelineScreen(
    controller: TimelineController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    LaunchedEffect(Unit) {
        controller.selectDate(Clock.System.now().toLocalDateTime(TimeZone.UTC).date)
    }

    Column(modifier = modifier) {
        // Date picker
        DatePicker(
            selectedDate = state.selectedDate,
            onDateSelected = { controller.selectDate(it) }
        )

        // Timeline items
        LazyColumn {
            items(state.items) { item ->
                when (item) {
                    is TimelineItemUI.VisitUI -> VisitCard(item.placeVisit)
                    is TimelineItemUI.RouteUI -> RouteCard(item.routeSegment)
                }
            }
        }
    }
}
```

#### iOS (SwiftUI)

```swift
struct TimelineScreenView: View {
    @StateObject private var viewModel: TimelineViewModel

    var body: some View {
        VStack {
            // Date picker
            DatePicker("Select Date", selection: $viewModel.selectedDate)

            // Timeline items
            List(viewModel.items) { item in
                switch item {
                case .visit(let visit):
                    VisitCardView(visit: visit)
                case .route(let route):
                    RouteCardView(route: route)
                }
            }
        }
        .onAppear {
            viewModel.loadToday()
        }
    }
}
```

## State Management

TrailGlass uses **Unidirectional Data Flow (UDF)** with StateFlow.

### State Flow Pattern

```
┌──────────┐
│   User   │
│ Interaction │
└─────┬────┘
      │ Events
      ↓
┌──────────────┐
│  Controller  │
│   (Shared)   │
└──────┬───────┘
       │ Updates
       ↓
┌──────────────┐
│  StateFlow   │
│   (Shared)   │
└──────┬───────┘
       │ Observes
       ↓
┌──────────────┐
│      UI      │
│  (Platform)  │
└──────────────┘
```

### State Definition

```kotlin
data class StatsState(
    val period: StatsPeriod = StatsPeriod.YEAR,
    val stats: StatsData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**Principles**:
- **Immutable**: State is read-only
- **Single Source**: One StateFlow per screen
- **Predictable**: State changes only through controller methods
- **Testable**: Easy to test state transformations

### State Updates

```kotlin
fun loadStats(period: StatsPeriod) {
    _state.update { it.copy(period = period, isLoading = true) }

    coroutineScope.launch {
        try {
            val stats = getStatsUseCase.execute(userId, period)
            _state.update { it.copy(stats = stats, isLoading = false) }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}
```

## Dependency Injection

TrailGlass uses **manual DI** (no framework) for simplicity and testability.

### Dependency Graph

```kotlin
// Database
val driver = createDatabaseDriver(context)  // Platform-specific
val database = TrailGlassDatabase(driver)

// Repositories
val placeVisitRepository = PlaceVisitRepositoryImpl(database)
val routeSegmentRepository = RouteSegmentRepositoryImpl(database)
val photoRepository = PhotoRepositoryImpl(database)

// Use Cases
val getTimelineUseCase = GetTimelineForDayUseCase(
    placeVisitRepository,
    routeSegmentRepository
)

// Controllers
val timelineController = TimelineController(
    getTimelineUseCase,
    coroutineScope,
    userId
)
```

### Android Setup

```kotlin
class TrailGlassApplication : Application() {
    lateinit var dependencies: AppDependencies

    override fun onCreate() {
        super.onCreate()
        dependencies = AppDependencies(this)
    }
}

class AppDependencies(context: Context) {
    private val database = createDatabase(context)

    val timelineController by lazy {
        TimelineController(
            GetTimelineForDayUseCase(
                PlaceVisitRepositoryImpl(database),
                RouteSegmentRepositoryImpl(database)
            ),
            CoroutineScope(SupervisorJob() + Dispatchers.Main),
            userId = "current_user"  // From auth
        )
    }
}
```

### iOS Setup

```swift
class DependencyContainer {
    private let database: TrailGlassDatabase

    init() {
        self.database = DatabaseKt.createDatabase()
    }

    lazy var timelineController: TimelineController = {
        TimelineController(
            getTimelineUseCase: GetTimelineForDayUseCase(
                placeVisitRepository: PlaceVisitRepositoryImpl(database: database),
                routeSegmentRepository: RouteSegmentRepositoryImpl(database: database)
            ),
            coroutineScope: CoroutineScopeKt.CoroutineScope(
                context: Dispatchers.main
            ),
            userId: "current_user"
        )
    }()
}
```

## Navigation

### Android (Navigation Compose)

```kotlin
sealed class Screen(val route: String) {
    object Stats : Screen("stats")
    object Timeline : Screen("timeline")
    object Map : Screen("map")
    object Settings : Screen("settings")
    data class VisitDetail(val visitId: String) : Screen("visit/{visitId}")
}

@Composable
fun TrailGlassNavHost(
    navController: NavHostController,
    dependencies: AppDependencies
) {
    NavHost(navController, startDestination = Screen.Timeline.route) {
        composable(Screen.Stats.route) {
            StatsScreen(controller = dependencies.statsController)
        }
        composable(Screen.Timeline.route) {
            TimelineScreen(
                controller = dependencies.timelineController,
                onNavigateToVisit = { visitId ->
                    navController.navigate(Screen.VisitDetail(visitId).route)
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen(controller = dependencies.mapController)
        }
        composable(
            route = Screen.VisitDetail.route,
            arguments = listOf(navArgument("visitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getString("visitId")!!
            VisitDetailScreen(visitId = visitId)
        }
    }
}
```

### iOS (SwiftUI NavigationStack)

```swift
enum Route: Hashable {
    case stats
    case timeline
    case map
    case settings
    case visitDetail(visitId: String)
}

struct ContentView: View {
    @State private var navigationPath = NavigationPath()
    @StateObject private var dependencies = DependencyContainer()

    var body: some View {
        NavigationStack(path: $navigationPath) {
            TabView {
                StatsScreenView(controller: dependencies.statsController)
                    .tabItem { Label("Stats", systemImage: "chart.bar") }

                TimelineScreenView(
                    controller: dependencies.timelineController,
                    onNavigateToVisit: { visitId in
                        navigationPath.append(Route.visitDetail(visitId: visitId))
                    }
                )
                .tabItem { Label("Timeline", systemImage: "clock") }

                MapScreenView(controller: dependencies.mapController)
                    .tabItem { Label("Map", systemImage: "map") }
            }
            .navigationDestination(for: Route.self) { route in
                switch route {
                case .visitDetail(let visitId):
                    VisitDetailView(visitId: visitId)
                default:
                    EmptyView()
                }
            }
        }
    }
}
```

## Data Flow

### Read Flow (Query)

```
User Action
    ↓
Controller Method
    ↓
Use Case
    ↓
Repository
    ↓
Database (SQLDelight)
    ↓
Repository (map to domain)
    ↓
Use Case (transform)
    ↓
Controller (update StateFlow)
    ↓
UI (collect StateFlow)
    ↓
Render Update
```

### Write Flow (Command)

```
User Action
    ↓
Controller Method
    ↓
Use Case
    ↓
Repository
    ↓
Database (SQLDelight) ← Write
    ↓
Database Triggers Flow Update
    ↓
Repository Flow Emits
    ↓
Controller Observes Flow
    ↓
StateFlow Updates
    ↓
UI Re-renders
```

### Example: Adding a Photo to a Visit

```kotlin
// 1. User clicks "Add Photo" button
// UI Layer
Button(onClick = {
    photoController.attachPhotoToVisit(photoId, visitId, caption)
})

// 2. Controller processes request
fun attachPhotoToVisit(photoId: String, visitId: String, caption: String?) {
    _state.update { it.copy(isLoading = true) }

    coroutineScope.launch {
        // 3. Use case handles business logic
        when (val result = attachPhotoUseCase.execute(photoId, visitId, caption)) {
            is Result.Success -> {
                _state.update { it.copy(isLoading = false) }
                // 4. Refresh suggestions (triggers DB query)
                loadSuggestionsForVisit(currentVisit)
            }
            is Result.Error -> {
                _state.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }
}

// 5. Repository writes to DB
suspend fun attachPhotoToVisit(attachment: PhotoAttachment) {
    database.photoAttachmentsQueries.insert(
        id = attachment.id,
        photo_id = attachment.photoId,
        place_visit_id = attachment.placeVisitId,
        caption = attachment.caption
    )
}

// 6. Flow observes change and UI updates automatically
```

## Platform Integration

### Android-Specific Features

- **Foreground Service**: Location tracking service
- **WorkManager**: Periodic location processing
- **MediaStore**: Photo access
- **Google Maps**: Map visualization
- **Material 3**: UI components

### iOS-Specific Features

- **CLLocationManager**: Location tracking
- **Photos Framework**: Photo access
- **MapKit**: Map visualization
- **SwiftUI**: UI components
- **Background Tasks**: Location processing

## Design Patterns

### Repository Pattern
Abstracts data sources from business logic.

### Use Case Pattern
Encapsulates single business operations.

### Observer Pattern
StateFlow for reactive state updates.

### Strategy Pattern
Different tracking modes (PASSIVE, ACTIVE).

### Factory Pattern
Platform-specific implementations (expect/actual).

### Builder Pattern
Complex object construction (e.g., PlaceVisit).

## Best Practices

1. **Keep Controllers Thin**: Business logic in use cases
2. **Immutable State**: Never mutate state directly
3. **Single Responsibility**: One use case = one operation
4. **Platform Independence**: Max code sharing in commonMain
5. **Testability**: All layers testable in isolation
6. **Error Handling**: Consistent error propagation
7. **Logging**: Structured logging with kotlin-logging

## Related Documentation

- [DEVELOPMENT.md](DEVELOPMENT.md) - Development setup
- [TESTING.md](TESTING.md) - Testing strategy
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [LOCATION_TRACKING.md](LOCATION_TRACKING.md) - Location tracking details
- [UI_IMPLEMENTATION.md](UI_IMPLEMENTATION.md) - UI architecture
