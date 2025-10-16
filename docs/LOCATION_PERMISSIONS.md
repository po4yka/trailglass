# Location Permissions Guide

This document explains how location permissions are handled in TrailGlass for both Android and iOS platforms.

## Overview

Location permissions are required for:
- **Follow Mode**: Real-time camera tracking of user location
- **Location Tracking**: Background and foreground location recording
- **Trip Detection**: Automatic trip start/end detection
- **Place Visit Detection**: Identifying places where users spend time

## Platform-Specific Behavior

### iOS

#### Permission Types
1. **When In Use**: Location access only when app is in foreground
2. **Always**: Location access in both foreground and background

#### Implementation

iOS uses CoreLocation framework with direct permission requests:

```kotlin
// IosLocationService.kt
override suspend fun requestLocationPermission(background: Boolean): Boolean {
    if (background) {
        locationManager.requestAlwaysAuthorization()
    } else {
        locationManager.requestWhenInUseAuthorization()
    }
    return hasLocationPermission()
}
```

#### Required Info.plist Keys

```xml
<!-- Foreground location -->
<key>NSLocationWhenInUseUsageDescription</key>
<string>TrailGlass needs access to your location to show your position on the map and track your travels.</string>

<!-- Background location -->
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>TrailGlass needs access to your location to track your travels and show your position on the map.</string>

<key>NSLocationAlwaysUsageDescription</key>
<string>TrailGlass needs access to your location in the background to automatically track your trips.</string>

<!-- Background modes -->
<key>UIBackgroundModes</key>
<array>
    <string>location</string>
</array>
```

#### Permission Flow

1. User taps "Grant Permissions" in Settings screen
2. `IosLocationService.requestLocationPermission()` is called
3. System permission dialog appears automatically
4. User grants or denies permission
5. Result comes back via `locationManagerDidChangeAuthorization` delegate callback
6. App checks permission status and updates UI

### Android

#### Permission Types

**Foreground Permissions** (Android 6.0+):
- `ACCESS_FINE_LOCATION`: Precise location (GPS)
- `ACCESS_COARSE_LOCATION`: Approximate location (Network)

**Background Permission** (Android 10+):
- `ACCESS_BACKGROUND_LOCATION`: Location access when app is in background

**Additional Permissions**:
- `FOREGROUND_SERVICE`: Required for foreground services
- `FOREGROUND_SERVICE_LOCATION`: Specifies location foreground service type
- `POST_NOTIFICATIONS`: For tracking notifications (Android 13+)

#### Implementation

Android requires Activity context for permission requests. The service layer cannot request permissions directly:

```kotlin
// AndroidLocationService.kt
override suspend fun requestLocationPermission(background: Boolean): Boolean {
    // Android requires Activity context - return false to indicate UI-based request needed
    android.util.Log.w(
        "AndroidLocationService",
        "Permission request attempted from service. " +
        "Permissions must be requested from Activity/Compose UI layer."
    )
    return false
}
```

Permission requests are handled in the UI layer using Jetpack Compose:

```kotlin
// LocationPermissionHandler.kt
@Composable
fun rememberLocationPermissionLauncher(
    onResult: (Boolean) -> Unit
): LocationPermissionLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        onResult(granted)
    }

    return remember {
        LocationPermissionLauncher {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
```

#### Required Manifest Permissions

```xml
<!-- Foreground location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Background location (Android 10+) -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Foreground service (Android 9+) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

<!-- Notifications (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

#### Permission Flow

1. User taps "Grant Permissions" in Settings screen
2. `permissionLauncher.launch()` is called from SettingsScreen
3. System permission dialog appears
4. User grants or denies permission
5. Result returned to `onResult` callback
6. `trackingController.checkPermissions()` updates state
7. UI reflects new permission status

#### Background Permission Best Practices (Android 10+)

Google requires a two-step process for background location:

1. **First**: Request foreground permissions (`ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`)
2. **Then**: Request background permission (`ACCESS_BACKGROUND_LOCATION`) separately

This is implemented in `LocationPermissionHandler`:

```kotlin
if (!state.checkPermissions()) {
    // Request foreground permissions first
    launcher.launch(arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ))
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !state.checkBackgroundPermission()) {
    // Then request background permission
    backgroundPermissionLauncher.launch(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
}
```

## Usage in UI

### Settings Screen Example

```kotlin
@Composable
fun SettingsScreen(
    trackingController: LocationTrackingController,
    modifier: Modifier = Modifier
) {
    val uiState by trackingController.uiState.collectAsState()

    // Android permission launcher
    val permissionLauncher = rememberLocationPermissionLauncher { granted ->
        if (granted) {
            trackingController.checkPermissions()
        }
    }

    Button(
        onClick = { permissionLauncher.launch() }
    ) {
        Text("Grant Permissions")
    }
}
```

### Custom Permission Request

```kotlin
@Composable
fun MyScreen() {
    val permissionState = rememberLocationPermissionState { granted ->
        if (granted) {
            // Permission granted - start tracking
        } else {
            // Permission denied - show explanation
        }
    }

    Button(onClick = { permissionState.requestPermissions() }) {
        Text("Enable Location")
    }
}
```

## Architecture

### Service Layer (Platform-Agnostic)

```kotlin
interface LocationService {
    suspend fun hasLocationPermission(): Boolean
    suspend fun requestLocationPermission(background: Boolean): Boolean
}
```

### Tracker Layer

```kotlin
interface LocationTracker : LocationPermissions {
    suspend fun hasPermissions(): Boolean
    suspend fun requestPermissions(): Boolean
}

class DefaultLocationTracker(
    private val locationService: LocationService,
    // ...
) : LocationTracker {
    override suspend fun requestPermissions(): Boolean {
        return locationService.requestLocationPermission(background = true)
    }
}
```

### UI Layer (Android Only)

- `LocationPermissionHandler.kt`: Composable permission helpers
- Uses `ActivityResultContracts.RequestMultiplePermissions`
- Handles rationale dialogs and settings navigation

### Controller Layer

```kotlin
class LocationTrackingController(
    private val locationTracker: LocationTracker
) {
    fun requestPermissions() {
        coroutineScope.launch {
            val granted = locationTracker.requestPermissions()
            _uiState.update { it.copy(hasPermissions = granted) }
        }
    }
}
```

## Testing Permissions

### iOS Simulator
1. Reset permissions: Device → Erase All Content and Settings
2. Or: Settings → Privacy & Security → Location Services → TrailGlass

### Android Emulator
1. Settings → Apps → TrailGlass → Permissions → Location
2. Or use ADB: `adb shell pm revoke com.po4yka.trailglass android.permission.ACCESS_FINE_LOCATION`

## Troubleshooting

### iOS: Permission Dialog Not Showing

**Cause**: Info.plist keys missing or incorrect

**Solution**: Verify all required keys are in `iosApp/iosApp/Info.plist`

### iOS: Background Location Not Working

**Cause**: UIBackgroundModes not configured

**Solution**: Add `location` to UIBackgroundModes array in Info.plist

### Android: Permission Request Not Working

**Cause**: Requesting from service instead of Activity

**Solution**: Use `LocationPermissionHandler` in Composable screens

### Android: Background Permission Denied

**Cause**: Requesting background permission without foreground permission first

**Solution**: Follow two-step process (foreground first, then background)

### Android: "While Using App" Only Option

**Cause**: Android 11+ shows "Only this time", "While using app", "Deny" for initial request

**Solution**: Request background permission separately after foreground permission granted

## Best Practices

1. **Request Minimum Required**: Request when-in-use on iOS if background not needed
2. **Explain Before Asking**: Show rationale before permission request
3. **Handle Denials Gracefully**: Provide alternative workflows or explanations
4. **Check Before Using**: Always check `hasLocationPermission()` before starting tracking
5. **Respect Battery**: Use appropriate tracking modes (PASSIVE vs ACTIVE)
6. **Settings Navigation**: Provide easy way to open app settings if permission denied

## See Also

- [Android Location Permissions Guide](https://developer.android.com/training/location/permissions)
- [iOS Location Services Guide](https://developer.apple.com/documentation/corelocation/requesting_authorization_to_use_location_services)
- [TrailGlass Location Tracking Implementation](./LOCATION_TRACKING.md)
