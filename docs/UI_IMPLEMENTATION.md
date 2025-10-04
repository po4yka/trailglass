## UI Implementation Guide - Phase 1D

TrailGlass UI is built with Jetpack Compose (Android) following Material 3 design guidelines.

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

**Bottom Navigation** with 3 tabs:
- **Stats**: Travel statistics
- **Timeline**: Daily timeline view
- **Settings**: Tracking and permissions

**Files**:
- `ui/navigation/Navigation.kt` - NavHost and bottom bar

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
- `BarChart` - Stats
- `ViewTimeline` - Timeline
- `Settings` - Settings
- `Public`, `CalendarToday` - Stats cards
- `Place`, `DirectionsWalk`, `Flight` - Timeline items

## Accessibility

- Semantic content descriptions for icons
- Material 3 contrast ratios (4.5:1 minimum)
- Touch targets ≥48dp
- Screen reader support via contentDescription

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
- [Location Processing Pipeline](../shared/src/commonMain/kotlin/com/po4yka/trailglass/location/README.md)
- [Location Tracking](LOCATION_TRACKING.md)
- [Implementation Roadmap](../IMPLEMENTATION_NEXT_STEPS.md)
