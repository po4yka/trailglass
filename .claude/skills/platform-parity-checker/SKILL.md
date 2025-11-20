---
name: platform-parity-checker
description: Verifies functional parity between Android and iOS implementations in the Trailglass KMP project. Use when implementing platform-specific features or reviewing cross-platform consistency.
allowed-tools:
  - Read
  - Grep
  - Glob
---

# Platform Parity Checker

Ensures Android and iOS implementations provide identical functionality despite different UI implementations.

## Parity Principles

### What Must Be Identical

**Functionality:**
- Same features available on both platforms
- Same data displayed
- Same user actions possible
- Same business logic execution
- Same state management

**Data Flow:**
- Both platforms observe same StateFlow
- Both call same controller methods
- Both use same use cases
- Both access same repositories
- Both store in same database

### What Can Differ

**Visual Design:**
- Android: Material 3 Expressive
- iOS: Liquid Glass aesthetic
- Platform-specific components
- Native animations and transitions
- Platform navigation patterns

**Platform APIs:**
- Android: FusedLocationProvider
- iOS: CLLocationManager
- Different but equivalent functionality

## Parity Checklist

### Feature Parity

**Stats Screen:**
- [ ] Both show countries visited count
- [ ] Both show cities visited count
- [ ] Both show total trips count
- [ ] Both show total photos count
- [ ] Both allow period filtering (week/month/year)
- [ ] Both display charts/visualizations

**Timeline Screen:**
- [ ] Both show chronological list of visits
- [ ] Both allow date selection
- [ ] Both show visit details (city, time, duration)
- [ ] Both navigate to visit detail
- [ ] Both support filtering
- [ ] Both show loading/error states

**Map Screen:**
- [ ] Both display markers for visits
- [ ] Both show routes between visits
- [ ] Both allow marker taps for details
- [ ] Both auto-fit map bounds
- [ ] Both show current location (if permission granted)
- [ ] Both support map interaction (zoom, pan)

**Settings Screen:**
- [ ] Both show tracking mode selection
- [ ] Both allow export options
- [ ] Both show privacy settings
- [ ] Both display app version
- [ ] Both link to documentation

**Photo Feature:**
- [ ] Both access device photo library
- [ ] Both suggest photos for visits (time/location match)
- [ ] Both allow photo attachment
- [ ] Both display attached photos
- [ ] Both support captions

### Data Parity

**Shared Models:**
```kotlin
// Both platforms use identical domain models
data class PlaceVisit(
    val id: String,
    val city: String?,
    val startTime: Instant,
    val endTime: Instant,
    // ... same fields on both platforms
)
```

**Shared Controllers:**
```kotlin
// Both platforms observe same StateFlow
class TimelineController @Inject constructor(...)  {
    val state: StateFlow<TimelineState>  // Same state on both
    fun selectDate(date: LocalDate)  // Same actions on both
}

// Android
@Composable
fun TimelineScreen(controller: TimelineController) {
    val state by controller.state.collectAsState()
    // Material 3 UI
}

// iOS
struct TimelineView: View {
    @StateObject var viewModel: TimelineViewModel
    // Wraps TimelineController
    // Liquid Glass UI
}
```

### Permission Parity

**Location Permissions:**
- Android: ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION
- iOS: NSLocationWhenInUseUsageDescription, NSLocationAlwaysUsageDescription
- Both implement same permission request flow
- Both handle denied permissions identically

**Photo Permissions:**
- Android: READ_MEDIA_IMAGES (API 33+) or READ_EXTERNAL_STORAGE
- iOS: NSPhotoLibraryUsageDescription
- Both request permissions before access
- Both handle denied permissions identically

### Navigation Parity

**Navigation Structure:**
```
Both Platforms:
├── Stats (Tab)
├── Timeline (Tab)
│   └── Visit Detail (Push)
│       └── Photo Gallery (Modal)
├── Map (Tab)
│   └── Visit Detail (Sheet/Modal)
└── Settings (Tab)
```

**Navigation Actions:**
- Same screens accessible
- Same navigation depth
- Same back navigation behavior (platform-appropriate)
- Same modal/sheet presentations (platform-appropriate)

## Parity Verification

### 1. Feature Comparison

**Check Android Implementation:**
```kotlin
// composeApp/src/main/kotlin/ui/screens/TimelineScreen.kt
@Composable
fun TimelineScreen(controller: TimelineController) {
    // What features are implemented?
    // - Date selection ✓
    // - Visit list ✓
    // - Navigation to detail ✓
    // - Loading states ✓
    // - Error handling ✓
}
```

**Check iOS Implementation:**
```swift
// iosApp/iosApp/Views/TimelineView.swift
struct TimelineView: View {
    // Same features?
    // - Date selection ✓
    // - Visit list ✓
    // - Navigation to detail ✓
    // - Loading states ✓
    // - Error handling ✓
}
```

### 2. Controller Usage

**Verify Both Use Same Controller:**
```bash
# Android: Check controller usage
grep -r "TimelineController" composeApp/src/main/kotlin/

# Should show:
# - StateFlow observation
# - Method calls (selectDate, etc.)

# iOS: Check controller bridge
grep -r "TimelineController" iosApp/iosApp/

# Should show:
# - ViewModel wrapping controller
# - Same method calls
```

### 3. State Structure

**Verify Same State Shape:**
```kotlin
// Shared: commonMain
data class TimelineState(
    val selectedDate: LocalDate?,
    val items: List<TimelineItemUI>,
    val isLoading: Boolean,
    val error: String?
)

// Android: Must handle all state fields
when {
    state.isLoading -> LoadingIndicator()
    state.error != null -> ErrorMessage(state.error!!)
    else -> TimelineList(state.items)
}

// iOS: Must handle same state fields
if viewModel.isLoading {
    ProgressView()
} else if let error = viewModel.error {
    ErrorView(message: error)
} else {
    TimelineList(items: viewModel.items)
}
```

### 4. Use Case Access

**Verify Both Use Same Use Cases:**
```kotlin
// Shared controller
class TimelineController @Inject constructor(
    private val getTimelineUseCase: GetTimelineForDayUseCase,  // Shared
    // ...
)

// Both Android and iOS use this same controller
// Therefore both use same use case
// Therefore both have same functionality
```

## Common Parity Issues

### Issue 1: Missing Features

**Problem:**
```kotlin
// Android has filtering
fun TimelineScreen() {
    FilterButton(onClick = { /* ... */ })
}

// iOS missing filtering
struct TimelineView: View {
    // No filter button
}
```

**Solution:**
Add missing feature to iOS with native UI:
```swift
.toolbar {
    ToolbarItem(placement: .topBarTrailing) {
        Button("Filter") {
            viewModel.showFilter()
        }
    }
}
```

### Issue 2: Different Data Shown

**Problem:**
```kotlin
// Android shows city + country
Text("${visit.city}, ${visit.country}")

// iOS shows only city
Text(visit.city ?? "Unknown")
```

**Solution:**
Match data display:
```swift
Text("\(visit.city ?? "Unknown"), \(visit.country ?? "")")
```

### Issue 3: Inconsistent State Handling

**Problem:**
```kotlin
// Android handles error state
state.error != null -> ErrorMessage()

// iOS ignores errors
if viewModel.isLoading { ... }
// Missing error handling
```

**Solution:**
Add error handling to iOS:
```swift
if let error = viewModel.error {
    ErrorView(message: error)
}
```

### Issue 4: Platform-Specific Workarounds

**Problem:**
```kotlin
// Android has workaround for specific issue
if (Build.VERSION.SDK_INT >= 33) {
    // Use new API
} else {
    // Fallback
}

// iOS doesn't need this but might need different workaround
// Risk: different behavior
```

**Solution:**
Ensure workarounds maintain functional parity:
- Same end result for user
- Document platform differences
- Test edge cases on both platforms

## Verification Process

### 1. Manual Testing

**Test Plan:**
- Complete same user flow on both platforms
- Verify same data appears
- Confirm same actions possible
- Check same edge cases
- Validate same error messages

**Example Flow:**
```
1. Open Timeline screen
   ✓ Both show timeline
2. Select date: Jan 15, 2024
   ✓ Both filter to that date
3. Tap on first visit
   ✓ Both navigate to detail
4. View photos
   ✓ Both show attached photos
5. Add new photo
   ✓ Both allow photo attachment
6. Navigate back
   ✓ Both return to timeline
```

### 2. Automated Checks

**Controller Access Verification:**
```bash
# List all controllers
find shared/src/commonMain/kotlin/feature -name "*Controller.kt"

# For each controller, verify:
# 1. Android uses it (search in composeApp/)
# 2. iOS uses it (search in iosApp/)
```

**State Field Coverage:**
```bash
# Extract state data class
grep -A 20 "data class.*State" shared/src/commonMain/kotlin/

# Verify Android handles all fields
# Verify iOS handles all fields
```

### 3. Code Review Checklist

When reviewing platform-specific PRs:
- [ ] Does Android implementation exist?
- [ ] Does iOS implementation exist?
- [ ] Do both use same controller?
- [ ] Do both handle same state fields?
- [ ] Do both provide same functionality?
- [ ] Are platform differences documented?
- [ ] Are both tested?

## Documentation

### Document Platform Differences

**In Code:**
```kotlin
/**
 * Android implementation using FusedLocationProviderClient.
 * iOS uses CLLocationManager but provides identical functionality.
 *
 * Both platforms:
 * - Support same tracking modes (IDLE, PASSIVE, ACTIVE)
 * - Emit LocationSample updates via Flow
 * - Handle permissions consistently
 * - Respect battery optimization
 */
actual class LocationTracker(...)
```

**In PLATFORM_PARITY.md:**
```markdown
## Location Tracking

### Functional Parity: ✓
Both platforms provide identical location tracking functionality.

### Implementation Differences:
- **Android**: FusedLocationProviderClient with Foreground Service
- **iOS**: CLLocationManager with Background Tasks

### API Differences:
- Android: requestLocationUpdates() → callbacks
- iOS: startUpdatingLocation() → delegate methods

### Unified Interface:
Both expose `LocationTracker` with:
- `val locationUpdates: Flow<LocationSample>`
- `suspend fun startTracking(mode: TrackingMode)`
- `suspend fun stopTracking()`
```

## Parity Enforcement

### During Development

1. **Implement on primary platform first** (usually Android)
2. **Document expected functionality**
3. **Implement on secondary platform** (iOS)
4. **Verify parity** with this skill
5. **Test both platforms** with same scenarios

### During Review

1. **Check parity checklist** (above)
2. **Run manual test flow** on both platforms
3. **Verify state handling** matches
4. **Confirm navigation** identical
5. **Validate error cases** consistent

### In CI/CD

```yaml
# Potential automated checks
- name: Verify Controller Usage
  run: ./scripts/check-controller-parity.sh

- name: Verify State Coverage
  run: ./scripts/check-state-coverage.sh

- name: Run Platform Tests
  run: |
    ./gradlew :composeApp:connectedAndroidTest
    xcodebuild test -scheme iosApp
```

## Related Documentation

- `docs/PLATFORM_DIFFERENCES.md` - Known platform differences
- `docs/PLATFORM_PARITY.md` - Parity tracking
- `AGENTS.md` - Cross-platform section

---

*Use this skill to ensure Android and iOS implementations maintain functional parity in Trailglass.*
