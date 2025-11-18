## UI Implementation Guide - Phase 1D

TrailGlass UI is built with Jetpack Compose (Android) following Material 3 design guidelines.

## UI Design Skills

TrailGlass uses specialized design skills to ensure distinctive, high-quality UI components. These skills guide AI assistants and developers to create interfaces that avoid generic "AI slop" aesthetics.

**Available Skills:**
- **[Compose UI Design Skill](SKILLS/compose-ui-design-skill.md)** - Comprehensive UI design guidelines
- **[Typography Skill](SKILLS/compose-typography-skill.md)** - Distinctive font selection and pairing
- **[Motion Skill](SKILLS/compose-motion-skill.md)** - Purposeful animations and transitions
- **[Theme Skill](SKILLS/compose-theme-skill.md)** - Brand colors and Material 3 implementation

**When to Use:**
- Generating new UI components
- Refactoring existing components
- Requesting AI assistance for UI code
- Establishing design system patterns

See [SKILLS/README.md](SKILLS/README.md) for detailed usage instructions.

## Architecture

```
UI Layer (Compose) → Controllers (Shared) → Use Cases (Shared) → Repositories (Shared) → Database
```

**Key Principles:**
- **Shared business logic**: Controllers and Use Cases in commonMain
- **Platform-specific UI**: Compose (Android), SwiftUI (iOS - future)
- **Unidirectional data flow**: StateFlow from controllers to UI
- **Material 3 Expressive**: Modern, accessible design system

## Shared Layer (commonMain)

### Use Cases

**GetTimelineForDayUseCase**:
- Fetches timeline items for a specific date
- Combines visits and routes chronologically
- Returns sealed class `TimelineItemUI` (VisitUI, RouteUI, DayStartUI, DayEndUI)

**GetStatsUseCase**:
- Aggregates statistics for Year, Month, or Custom period
- Returns countries, cities, trips, days counts
- Top countries and cities by visit count

**StartTrackingUseCase** / **StopTrackingUseCase**:
- Manages location tracking lifecycle
- Permission checking
- Returns Result (Success, PermissionDenied, Error)

### Controllers

**TimelineController**:
```kotlin
data class TimelineState(
    val selectedDate: LocalDate?,
    val items: List<TimelineItemUI>,
    val isLoading: Boolean,
    val error: String?
)

fun loadDay(date: LocalDate)
fun refresh()
```

**StatsController**:
```kotlin
data class StatsState(
    val period: Period?,
    val stats: Stats?,
    val isLoading: Boolean,
    val error: String?
)

fun loadPeriod(period: Period)
fun refresh()
```

**LocationTrackingController**:
```kotlin
data class LocationTrackingUIState(
    val trackingState: TrackingState,
    val hasPermissions: Boolean,
    val isProcessing: Boolean,
    val error: String?
)

fun startTracking(mode: TrackingMode)
fun stopTracking()
fun checkPermissions()
```

## Android UI (Compose)

### Theme

**Material 3 Expressive** with dynamic color support (Android 12+):
- **Primary**: Teal/Cyan for maps and travel
- **Secondary**: Orange for photos and memories
- **Tertiary**: Purple for special events

**Files**:
- `ui/theme/Color.kt` - Color palette (light/dark)
- `ui/theme/Theme.kt` - Theme configuration
- `ui/theme/Type.kt` - Typography scale

### Navigation

**Architecture**: Decompose 3.2.0 for lifecycle-aware navigation

**Bottom Navigation** with 4 tabs:
- **Stats**: Travel statistics and analytics
- **Timeline**: Daily timeline view with visits and routes
- **Map**: Interactive map with location markers
- **Settings**: Tracking controls and permissions

**Files**:
- `ui/navigation/RootComponent.kt` - Main navigation component with child stack
- `ui/navigation/Navigation.kt` - Main scaffold with bottom navigation bar
- `ui/navigation/StatsComponent.kt` - Stats screen component wrapper
- `ui/navigation/TimelineComponent.kt` - Timeline screen component wrapper
- `ui/navigation/MapComponent.kt` - Map screen component wrapper
- `ui/navigation/SettingsComponent.kt` - Settings screen component wrapper

**Deep Linking Support**:
- `trailglass://app/stats` - Navigate to Stats screen
- `trailglass://app/timeline` - Navigate to Timeline screen
- `trailglass://app/map` - Navigate to Map screen
- `trailglass://app/settings` - Navigate to Settings screen
- `https://trailglass.app/*` - HTTPS deep links for web integration

**Features**:
- Lifecycle-aware component management
- State preservation across configuration changes
- Type-safe navigation with kotlinx.serialization
- Automatic back button handling
- Deep link intent processing in MainActivity

### Screens

#### 1. Stats Screen

**Features**:
- Year/Month filter chips
- Overview cards (Countries, Days, Trips, Visits)
- Top Countries list
- Top Cities list
- Empty state
- Error handling

**UI Components**:
- FilterChip for period selection
- StatCard with icon, value, title
- ListItem for top locations
- LazyColumn for scrolling

#### 2. Timeline Screen

**Features**:
- Daily timeline items
- Visit cards with location and address
- Route cards with transport type and distance
- Day start/end markers
- Empty state

**UI Components**:
- VisitCard (primary container color)
- RouteCard (secondary container color)
- DayMarkerCard with icons
- Transport icons (walk, bike, car, train, plane, boat)

#### 3. Settings Screen

**Features**:
- Tracking status card
- Start/Stop tracking button
- Permission status
- Permission request flow
- App version info

**UI Components**:
- TrackingStatusCard with live state
- PermissionsCard with status indicator
- About card

## State Management

**Pattern**: Collect StateFlow in Composable

```kotlin
@Composable
fun StatsScreen(controller: StatsController) {
    val state by controller.state.collectAsState()

    LaunchedEffect(Unit) {
        controller.loadPeriod(Period.Year(2025))
    }

    when {
        state.isLoading -> LoadingView()
        state.error != null -> ErrorView()
        state.stats != null -> StatsContent(state.stats!!)
    }
}
```

## Color System

**Light Theme**:
- Primary: `#006A6A` (Teal)
- Secondary: `#845400` (Orange)
- Background: `#FAFDF C` (Off-white)

**Dark Theme**:
- Primary: `#80D3D3` (Light teal)
- Secondary: `#FFB960` (Light orange)
- Background: `#191C1C` (Dark gray)

## Typography

Material 3 Typography scale:
- **Display**: Large headings (36-57sp)
- **Headline**: Section headers (24-32sp)
- **Title**: Card titles (14-22sp)
- **Body**: Content text (12-16sp)
- **Label**: Buttons and labels (11-14sp)

## Iconography

**Material Icons** from `androidx.compose.material.icons`:
- `BarChart` - Stats tab
- `ViewTimeline` - Timeline tab
- `Map` - Map tab
- `Settings` - Settings tab
- `Public`, `CalendarToday`, `Flight`, `Place` - Stats cards
- `Place`, `DirectionsWalk`, `DirectionsBike`, `DirectionsCar`, `Train`, `Flight`, `DirectionsBoat` - Timeline items
- `GpsFixed`, `GpsOff` - Tracking status

## Accessibility

- Semantic content descriptions for icons
- Material 3 contrast ratios (4.5:1 minimum)
- Touch targets ≥48dp
- Screen reader support via contentDescription

## Error Handling

UI layer implements comprehensive error handling following the patterns in [ERROR_HANDLING.md](ERROR_HANDLING.md).

### Error State Display

All screens handle three states: Loading, Error, Success:

```kotlin
@Composable
fun StatsScreen(controller: StatsController) {
    val state by controller.state.collectAsState()

    when {
        state.isLoading -> {
            LoadingView()
        }
        state.error != null -> {
            ErrorView(
                message = state.error!!,  // User-friendly message
                onRetry = { controller.refresh() }
            )
        }
        state.stats != null -> {
            StatsContent(stats = state.stats!!)
        }
        else -> {
            EmptyStateView(
                title = "No data yet",
                message = "Start tracking to see your travel statistics"
            )
        }
    }
}
```

### Result Handling in Controllers

Controllers convert `Result<T>` to UI state:

```kotlin
class TimelineController(
    private val getTimelineUseCase: GetTimelineForDayUseCase,
    private val errorAnalytics: ErrorAnalytics
) {
    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state

    fun loadDay(date: LocalDate) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = getTimelineUseCase.execute(date, userId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            items = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    // Log to analytics
                    result.error.logToAnalytics(
                        errorAnalytics,
                        context = mapOf("operation" to "loadTimeline", "date" to date.toString())
                    )

                    // Update UI with user-friendly message
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.userMessage  // "Unable to load timeline. Please try again."
                        )
                    }
                }
            }
        }
    }
}
```

### Network Errors

UI shows appropriate messages for network errors:

```kotlin
when (val error = result.error) {
    is TrailGlassError.NetworkError.NoConnection -> {
        ErrorView(
            message = "No internet connection",
            icon = Icons.Default.CloudOff,
            action = "Retry" to { controller.refresh() }
        )
    }
    is TrailGlassError.NetworkError.Timeout -> {
        ErrorView(
            message = "Request timed out. Please try again.",
            action = "Retry" to { controller.refresh() }
        )
    }
    else -> {
        ErrorView(
            message = error.userMessage,
            action = "Retry" to { controller.refresh() }
        )
    }
}
```

### Offline Mode Indicator

Show offline status in UI:

```kotlin
@Composable
fun AppScaffold(networkConnectivity: NetworkConnectivity) {
    val isOnline by networkConnectivity.isConnected.collectAsState(initial = true)

    Scaffold(
        topBar = {
            if (!isOnline) {
                OfflineBanner(
                    message = "You're offline. Changes will sync when connected."
                )
            }
        }
    ) { /* Content */ }
}
```

### Retry with Feedback

Show retry progress:

```kotlin
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    var isRetrying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Error, contentDescription = null)
        Text(message, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isRetrying = true
                onRetry()
            },
            enabled = !isRetrying
        ) {
            if (isRetrying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Retry")
            }
        }
    }
}
```

### Error Toast/Snackbar

For non-blocking errors:

```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(state.error) {
    state.error?.let { errorMessage ->
        snackbarHostState.showSnackbar(
            message = errorMessage,
            actionLabel = "Retry",
            duration = SnackbarDuration.Long
        ).let { result ->
            if (result == SnackbarResult.ActionPerformed) {
                controller.refresh()
            }
        }
    }
}
```

## Performance

**Optimizations**:
- LazyColumn for scrolling lists (virtualization)
- remember for state hoisting
- LaunchedEffect for side effects
- collectAsState for Flow observation
- Immutable data classes for state

## Testing

**Unit Tests** (Use Cases):
```kotlin
@Test
fun testGetTimelineForDay() = runTest {
    val useCase = GetTimelineForDayUseCase(mockRepo, mockRepo)
    val items = useCase.execute(LocalDate(2025, 11, 17), "user1")
    assertTrue(items.isNotEmpty())
}
```

**UI Tests** (Compose):
```kotlin
@Test
fun testStatsScreen() {
    composeTestRule.setContent {
        StatsScreen(mockController)
    }
    composeTestRule.onNodeWithText("Countries").assertExists()
}
```

## Future Enhancements

- [ ] Map integration (Google Maps / Mapbox)
- [ ] Photo attachments to visits
- [ ] Trip editing and manual creation
- [ ] Export timeline to PDF/JSON
- [ ] Offline mode indicator
- [ ] Search and filters
- [ ] Dark mode toggle
- [ ] Language localization

## Resources

- [Material 3 Guidelines](https://m3.material.io/)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Material Icons](https://fonts.google.com/icons)

---

**Related Documentation**:
- [Error Handling](ERROR_HANDLING.md) - Comprehensive error handling guide
- [Architecture](ARCHITECTURE.md) - System architecture overview
- [Location Tracking](LOCATION_TRACKING.md) - Platform location tracking
- [Testing](TESTING.md) - Testing strategy and coverage
