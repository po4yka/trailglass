# Permission Request Flow

Comprehensive permission request system with user-friendly explanations for TrailGlass.

## Overview

The permission flow system provides a complete, user-friendly experience for requesting and managing app permissions across Android and iOS platforms. It includes:

- **Clear explanations** of why permissions are needed
- **Feature lists** showing what each permission enables
- **Graceful handling** of denied and permanently denied states
- **Step-by-step instructions** for enabling permissions in Settings
- **Platform-specific UI** components (Material 3 for Android, SwiftUI for iOS)

## Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                   PermissionFlowController                   │
│  Orchestrates the complete permission request experience    │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼──────────────┐
        │             │              │
        ▼             ▼              ▼
┌──────────────┐ ┌────────────┐ ┌──────────────────┐
│ Permission   │ │ Permission │ │ Permission       │
│ Manager      │ │ Rationale  │ │ UI Components    │
│ (Platform)   │ │ Provider   │ │ (Platform)       │
└──────────────┘ └────────────┘ └──────────────────┘
```

### Permission States

```kotlin
sealed class PermissionState {
    object Granted                  // Permission granted
    object Denied                   // Denied, can request again
    object PermanentlyDenied        // Must go to Settings
    object NotDetermined            // Never asked before
    object Restricted               // Parental/enterprise controls
}
```

### Permission Flow States

```
NotDetermined → Request → Granted ✓
                    ↓
                  Denied → Show Rationale → Retry → ...
                    ↓
            PermanentlyDenied → Settings Instructions
```

## Permission Types

| Type | Description | Required | Features |
|------|-------------|----------|----------|
| `LOCATION_FINE` | Precise GPS location | Yes | Place detection, route mapping, distance calculation |
| `LOCATION_COARSE` | Approximate location | No | General area visits, lower battery usage |
| `LOCATION_BACKGROUND` | Location when app closed | No | Continuous tracking, seamless multi-day trips |
| `CAMERA` | Take photos | No | Capture trip moments, photo timeline |
| `PHOTO_LIBRARY` | Access existing photos | No | Attach photos to visits, auto-suggestions |
| `NOTIFICATIONS` | Push notifications | No | Tracking alerts, sync updates, trip milestones |

## Usage

### 1. Basic Permission Request

```kotlin
// In your ViewModel or Controller
@Inject
class MyFeatureController(
    private val permissionFlow: PermissionFlowController
) {
    fun onFeatureClicked() {
        // Start the permission flow
        permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)

        // Observe the result
        permissionFlow.state.collect { state ->
            when (state.lastResult) {
                is PermissionResult.Granted -> {
                    // Permission granted, proceed with feature
                    startLocationTracking()
                }
                is PermissionResult.Denied -> {
                    // User denied permission
                    showFeatureUnavailableMessage()
                }
                is PermissionResult.PermanentlyDenied -> {
                    // Already showing settings dialog
                }
                null -> {
                    // No result yet
                }
            }
        }
    }
}
```

### 2. Android UI Integration (Compose)

```kotlin
@Composable
fun MyScreen(
    permissionFlow: PermissionFlowController,
    viewModel: MyViewModel
) {
    val state by permissionFlow.state.collectAsState()

    Box {
        // Your main content
        MainContent(
            onLocationFeatureClick = {
                permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
            }
        )

        // Permission dialogs
        when {
            state.showRationaleDialog && state.currentRequest != null -> {
                PermissionRationaleDialog(
                    requestState = state.currentRequest!!,
                    onAccept = { permissionFlow.onRationaleAccepted() },
                    onDeny = { permissionFlow.onRationaleDenied() }
                )
            }

            state.showDeniedDialog && state.currentRequest != null -> {
                PermissionDeniedDialog(
                    requestState = state.currentRequest!!,
                    onRetry = { permissionFlow.onRetryPermission() },
                    onContinue = { permissionFlow.onContinueWithoutPermission() }
                )
            }

            state.showPermanentlyDeniedDialog &&
            state.currentRequest != null &&
            state.settingsInstructions != null -> {
                PermissionPermanentlyDeniedDialog(
                    requestState = state.currentRequest!!,
                    instructions = state.settingsInstructions!!,
                    onOpenSettings = { permissionFlow.onOpenSettingsClicked() },
                    onDismiss = { permissionFlow.onContinueWithoutPermission() }
                )
            }
        }
    }
}
```

### 3. iOS UI Integration (SwiftUI)

```swift
struct MyView: View {
    @ObservedObject var permissionFlow: PermissionFlowController

    var body: some View {
        VStack {
            Button("Enable Location Tracking") {
                permissionFlow.startPermissionFlow(.locationFine)
            }
        }
        .sheet(isPresented: $permissionFlow.state.showRationaleDialog) {
            if let request = permissionFlow.state.currentRequest {
                PermissionRationaleView(
                    requestState: request,
                    onAccept: { permissionFlow.onRationaleAccepted() },
                    onDeny: { permissionFlow.onRationaleDenied() }
                )
            }
        }
        .sheet(isPresented: $permissionFlow.state.showPermanentlyDeniedDialog) {
            if let request = permissionFlow.state.currentRequest,
               let instructions = permissionFlow.state.settingsInstructions {
                PermissionSettingsInstructionsView(
                    requestState: request,
                    instructions: instructions,
                    onOpenSettings: { permissionFlow.onOpenSettingsClicked() },
                    onDismiss: { permissionFlow.onContinueWithoutPermission() }
                )
            }
        }
    }
}
```

### 4. Compact Banner (Optional)

For less intrusive permission requests:

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Show banner at top
        if (needsLocationPermission) {
            PermissionRequestBanner(
                requestState = permissionRequest,
                onGrant = { permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE) },
                onDismiss = { /* Dismiss */ }
            )
        }

        // Rest of your content
        MainContent()
    }
}
```

## Permission Rationales

### Location Fine

**Title:** "Precise Location Access"

**Description:** "TrailGlass needs precise location access to automatically record your visits and track your journeys."

**Features:**
- Automatically detect when you arrive at and leave places
- Accurately map your routes and paths
- Calculate distances and durations of your trips
- Show your real-time location on the map

**Required:** Yes

### Location Background

**Title:** "Background Location Access"

**Description:** "To automatically track your trips even when the app is closed, TrailGlass needs background location access."

**Features:**
- Track your location continuously without keeping the app open
- Never miss a place visit or route segment
- Seamlessly record multi-day trips
- Automatic tracking start/stop based on movement

**Required:** No

### Camera

**Title:** "Camera Access"

**Description:** "Take photos to remember your visits and journeys."

**Features:**
- Capture moments during your trips
- Attach photos to specific place visits
- Build a visual timeline of your travels

**Required:** No

### Photo Library

**Title:** "Photo Library Access"

**Description:** "Access your existing photos to attach to your visits."

**Features:**
- Select photos from your library
- Attach existing photos to place visits
- Automatically suggest photos based on time and location

**Required:** No

### Notifications

**Title:** "Notifications"

**Description:** "Stay informed about your trips and sync status."

**Features:**
- Get notified when tracking starts or stops
- Receive sync completion notifications
- Stay updated on trip milestones
- Get reminders to review your visits

**Required:** No

## Settings Instructions

When a permission is permanently denied, users see step-by-step instructions:

### Android
1. Open Settings app
2. Find and tap TrailGlass
3. Tap [Permission Name]
4. Select the appropriate permission level

### iOS
1. Open Settings app
2. Find and tap TrailGlass
3. Tap [Permission Name]
4. Select the appropriate permission level

Plus a "Open Settings" button that navigates directly to the app settings page.

## Best Practices

### 1. Request Permissions Contextually

❌ **Don't** request all permissions on app startup

✅ **Do** request permissions when the user tries to use a feature that needs them

```kotlin
// Good - request when needed
fun onStartTrackingClicked() {
    permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
}

// Bad - request on app launch
override fun onCreate() {
    permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
    permissionFlow.startPermissionFlow(PermissionType.CAMERA)
    permissionFlow.startPermissionFlow(PermissionType.PHOTO_LIBRARY)
}
```

### 2. Explain Before Requesting

The `PermissionFlowController` automatically shows rationales when appropriate, but you can also show explanations in your UI:

```kotlin
@Composable
fun LocationTrackingCard() {
    Card {
        Column {
            Text("Automatic Trip Tracking")
            Text("Enable location access to automatically record your visits")

            Button(onClick = {
                permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
            }) {
                Text("Enable")
            }
        }
    }
}
```

### 3. Handle All States

Always handle all permission results:

```kotlin
permissionFlow.state.collect { state ->
    when (state.lastResult) {
        is PermissionResult.Granted -> {
            // Permission granted - proceed
        }
        is PermissionResult.Denied -> {
            // Show feature limitation message
        }
        is PermissionResult.PermanentlyDenied -> {
            // Already showing settings dialog
        }
        is PermissionResult.Cancelled -> {
            // User cancelled - no action needed
        }
        is PermissionResult.Error -> {
            // Show error message
        }
        null -> {
            // No result yet
        }
    }
}
```

### 4. Check Before Using Features

Always check permission before using features:

```kotlin
suspend fun startLocationTracking() {
    val isGranted = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)

    if (!isGranted) {
        permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
        return
    }

    // Permission granted, start tracking
    locationTracker.startTracking()
}
```

## Testing

The permission flow includes comprehensive tests:

```kotlin
@Test
fun `should show rationale when permission was previously denied`() = runTest {
    val manager = MockPermissionManager(
        initialState = PermissionState.Denied,
        shouldShowRationale = true
    )
    val controller = PermissionFlowController(manager, ...)

    controller.startPermissionFlow(PermissionType.LOCATION_FINE)

    controller.state.value.showRationaleDialog shouldBe true
}
```

See `PermissionFlowControllerTest.kt` for complete test coverage.

## Platform-Specific Notes

### Android
- Uses `ActivityCompat.requestPermissions()`
- Requires Activity context for permission requests
- Background location requires separate request on Android 10+
- Photo library permission changed in Android 13 (Tiramisu)

### iOS
- Uses CoreLocation for location permissions
- Uses Photos framework for photo library
- Uses UserNotifications for notification permissions
- Background location shows as "Allow all the time" in settings
- Requires Info.plist entries for permission descriptions

## Troubleshooting

### Permission Request Not Showing

1. Check that permission is not already granted
2. Verify platform-specific permission strings in manifest/Info.plist
3. Ensure you're calling from correct context (Activity on Android)

### Permanently Denied State

If permission goes straight to permanently denied:
- User may have previously selected "Don't ask again" (Android)
- User may have denied multiple times (iOS)
- System may have restrictions in place
- Use Settings dialog to guide user to enable manually

### Permission Granted But Feature Not Working

1. Verify you're checking the permission before use
2. Check for runtime errors in feature code
3. Ensure background permission if needed (for background features)
4. Check device location settings are enabled

## See Also

- [Location Tracking Guide](./LOCATION_TRACKING.md)
- [Background Sync Documentation](./BACKGROUND_SYNC.md)
- [Privacy & Security](./PRIVACY.md)
