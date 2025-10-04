# Location Tracking Implementation

This document describes the platform-specific location tracking implementation for TrailGlass.

## Overview

TrailGlass implements battery-efficient location tracking on both Android and iOS using platform-native APIs:

- **Android**: FusedLocationProviderClient with Foreground Service
- **iOS**: CLLocationManager with visit monitoring

## Architecture

```
┌──────────────────────────┐
│  LocationTracker         │  ← Platform-agnostic interface
│  (Common)                │
└──────────────────────────┘
           ↑
    ┌──────┴──────┐
    ↓             ↓
┌─────────┐  ┌─────────┐
│ Android │  │   iOS   │
│ Tracker │  │ Tracker │
└─────────┘  └─────────┘
     ↓             ↓
FusedLocation  CLLocation
 Provider       Manager
```

## Tracking Modes

### 1. IDLE
- No location tracking
- Minimal battery usage
- Use when: User has disabled tracking

### 2. PASSIVE
- Significant location changes only
- **Android**: ~500m or 5 minutes
- **iOS**: Significant location changes + visit monitoring
- Battery efficient for all-day tracking
- Use when: Background tracking, normal daily use

### 3. ACTIVE
- Continuous high-accuracy updates
- **Android**: ~10m or 30 seconds
- **iOS**: Continuous updates with best accuracy
- Higher battery usage
- Use when: Active trip recording, real-time navigation

## Configuration

### TrackingConfiguration

```kotlin
data class TrackingConfiguration(
    // PASSIVE mode settings
    val passiveInterval: Duration = 5.minutes,
    val passiveDistance: Double = 500.0,  // meters
    val passiveAccuracy: Double = 100.0,  // meters

    // ACTIVE mode settings
    val activeInterval: Duration = 30.seconds,
    val activeDistance: Double = 10.0,    // meters
    val activeAccuracy: Double = 10.0,    // meters

    // Background location
    val requireBackgroundLocation: Boolean = true,

    // Notification (Android only)
    val showNotification: Boolean = true,
    val notificationTitle: String = "TrailGlass",
    val notificationMessage: String = "Recording your location"
)
```

### Preset Configurations

```kotlin
// Battery-optimized (recommended for most users)
TrackingConfigurations.BATTERY_OPTIMIZED

// Balanced (good compromise)
TrackingConfigurations.BALANCED

// High accuracy (for active tracking)
TrackingConfigurations.HIGH_ACCURACY
```

## Platform Implementation

### Android

#### Components

1. **AndroidLocationTracker**
   - Wraps `FusedLocationProviderClient`
   - Handles location updates and permission checks
   - Saves samples to repository

2. **LocationTrackingService**
   - Foreground service for continuous tracking
   - Displays persistent notification
   - Keeps app alive in background

3. **LocationPermissions**
   - Helper for permission management
   - Handles runtime permission requests
   - Settings deep link for manual permission

4. **LocationProcessingWorker**
   - WorkManager periodic worker
   - Processes samples every 6 hours
   - Runs only when connected to network and battery not low

#### Required Permissions

**AndroidManifest.xml**:
```xml
<!-- Location permissions -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Background location (Android 10+) -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Foreground service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

<!-- Notifications (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Internet for geocoding -->
<uses-permission android:name="android.permission.INTERNET" />
```

#### Service Declaration

```xml
<service
    android:name=".location.LocationTrackingService"
    android:foregroundServiceType="location"
    android:exported="false" />
```

#### Usage Example

```kotlin
// Create tracker
val trackerFactory = LocationTrackerFactory(context)
val tracker = trackerFactory.create(
    repository = locationRepository,
    configuration = TrackingConfigurations.BALANCED
)

// Check permissions
if (tracker.hasPermissions()) {
    // Start tracking
    tracker.startTracking(TrackingMode.PASSIVE)
} else {
    // Request permissions from Activity
    val permissions = LocationPermissions(activity)
    permissions.requestLocationPermissions { granted ->
        if (granted) {
            tracker.startTracking(TrackingMode.PASSIVE)
        }
    }
}

// Observe location updates
tracker.locationUpdates.collect { sample ->
    println("New location: ${sample.latitude}, ${sample.longitude}")
}

// Stop tracking
tracker.stopTracking()
```

#### Foreground Service

```kotlin
// Start foreground service for active tracking
LocationTrackingService.start(context, TrackingMode.ACTIVE)

// Stop service
LocationTrackingService.stop(context)
```

#### Background Processing

```kotlin
// Schedule periodic processing (every 6 hours)
LocationProcessingWorker.schedule(context, intervalHours = 6)

// Trigger immediate processing
LocationProcessingWorker.triggerImmediate(context)

// Cancel scheduled processing
LocationProcessingWorker.cancel(context)
```

#### Battery Optimization

Android may restrict background location access to save battery. Guide users to:

1. **Disable battery optimization** for TrailGlass:
   - Settings → Apps → TrailGlass → Battery → Unrestricted

2. **Allow background location**:
   - Settings → Apps → TrailGlass → Permissions → Location → Allow all the time

3. **Disable battery saver mode** when needed

### iOS

#### Components

1. **IOSLocationTracker**
   - Wraps `CLLocationManager`
   - Supports significant changes and visit monitoring
   - Handles authorization changes

2. **LocationManagerDelegate**
   - Implements `CLLocationManagerDelegate`
   - Processes location updates and visits
   - Handles authorization status changes

#### Required Info.plist Keys

```xml
<!-- Location Permission Descriptions -->
<key>NSLocationWhenInUseUsageDescription</key>
<string>TrailGlass needs your location to track your travels and create your timeline.</string>

<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>TrailGlass tracks your location in the background to automatically record your trips.</string>

<key>NSLocationAlwaysUsageDescription</key>
<string>TrailGlass needs background location access to automatically track your trips.</string>

<!-- Background Modes -->
<key>UIBackgroundModes</key>
<array>
    <string>location</string>
    <string>processing</string>
</array>
```

#### Capabilities

In Xcode, enable:
- **Background Modes**:
  - ✅ Location updates
  - ✅ Background processing

#### Usage Example

```kotlin
// Create tracker
val trackerFactory = LocationTrackerFactory()
val tracker = trackerFactory.create(
    repository = locationRepository,
    configuration = TrackingConfigurations.BALANCED
)

// Request permissions
tracker.requestPermissions()

// Start tracking
tracker.startTracking(TrackingMode.PASSIVE)

// Observe location updates
tracker.locationUpdates.collect { sample ->
    println("New location: ${sample.latitude}, ${sample.longitude}")
}

// Stop tracking
tracker.stopTracking()
```

#### Authorization Flow

1. **When In Use**: User must grant "While Using App" first
2. **Always**: Request "Always Allow" for background tracking
3. **Provisional Always**: iOS may grant provisional access initially

iOS shows permission dialogs automatically when calling:
- `requestWhenInUseAuthorization()` - For "While Using App"
- `requestAlwaysAuthorization()` - For "Always Allow"

#### Background Processing

iOS automatically handles background location updates with:

1. **Significant Location Changes**: Updates when user moves ~500m
2. **Visit Monitoring**: Detects when user arrives/departs locations
3. **Region Monitoring**: Geofence-based updates (future enhancement)

#### Background Task Registration

For additional background processing (e.g., data sync), register a BGTask:

```swift
// In AppDelegate
import BackgroundTasks

func application(_ application: UIApplication,
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

    // Register background task
    BGTaskScheduler.shared.register(
        forTaskWithIdentifier: "com.po4yka.trailglass.processing",
        using: nil
    ) { task in
        self.handleProcessingTask(task: task as! BGProcessingTask)
    }

    return true
}

func handleProcessingTask(task: BGProcessingTask) {
    // Schedule next execution
    scheduleProcessing()

    // Perform processing
    task.expirationHandler = {
        // Handle early termination
    }

    // Mark task complete when done
    task.setTaskCompleted(success: true)
}

func scheduleProcessing() {
    let request = BGProcessingTaskRequest(identifier: "com.po4yka.trailglass.processing")
    request.earliestBeginDate = Date(timeIntervalSinceNow: 6 * 60 * 60) // 6 hours
    request.requiresNetworkConnectivity = true
    request.requiresExternalPower = false

    try? BGTaskScheduler.shared.submit(request)
}
```

## Data Flow

```
1. Platform Location API
   ↓
2. LocationTracker
   ↓
3. LocationSample (domain model)
   ↓
4. LocationRepository
   ↓
5. SQLDelight Database
   ↓
6. LocationProcessor (periodic)
   ↓
7. PlaceVisits, Routes, Trips
```

## Performance

### Battery Usage

| Mode      | Android                | iOS                    |
|-----------|------------------------|------------------------|
| PASSIVE   | ~3-5% per day          | ~2-4% per day          |
| ACTIVE    | ~15-25% per hour       | ~10-20% per hour       |

### Update Frequency

| Mode      | Android Updates        | iOS Updates            |
|-----------|------------------------|------------------------|
| PASSIVE   | Every ~5 min or 500m   | Significant changes    |
| ACTIVE    | Every ~30 sec or 10m   | Continuous (best)      |

### Storage

- **Location Sample**: ~200 bytes
- **Daily Samples**: ~5,000-10,000 (PASSIVE mode)
- **Daily Storage**: ~1-2 MB uncompressed

## Best Practices

### 1. Permission Request Timing

**Don't** request permissions immediately on app launch:
```kotlin
// ❌ Bad
override fun onCreate() {
    tracker.requestPermissions() // Too early!
}
```

**Do** request when user initiates tracking:
```kotlin
// ✅ Good
fun onStartTrackingClicked() {
    if (!tracker.hasPermissions()) {
        tracker.requestPermissions()
    }
}
```

### 2. Explain Before Requesting

Show a rationale dialog explaining why you need the permission:

```kotlin
if (permissions.shouldShowLocationRationale()) {
    showRationaleDialog {
        permissions.requestLocationPermissions { granted ->
            // Handle result
        }
    }
} else {
    permissions.requestLocationPermissions { granted ->
        // Handle result
    }
}
```

### 3. Graceful Degradation

Handle missing permissions gracefully:

```kotlin
when {
    tracker.hasPermissions() -> {
        // Full functionality
        tracker.startTracking(TrackingMode.PASSIVE)
    }
    tracker.hasPartialPermissions() -> {
        // Limited functionality (foreground only)
        tracker.startTracking(TrackingMode.ACTIVE)
    }
    else -> {
        // Manual entry mode
        showManualEntryUI()
    }
}
```

### 4. Battery Optimization Guidance

Guide users to disable battery optimization:

```kotlin
fun checkBatteryOptimization(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
    return true
}

fun requestBatteryOptimizationExemption(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}
```

### 5. Monitor Tracking State

```kotlin
tracker.trackingState.collect { state ->
    when {
        state.isTracking -> {
            updateUI(status = "Tracking: ${state.mode}")
        }
        !state.isTracking && state.mode != TrackingMode.IDLE -> {
            updateUI(status = "Tracking stopped (permissions?)")
        }
        else -> {
            updateUI(status = "Tracking disabled")
        }
    }
}
```

## Troubleshooting

### Android

**Location not updating**:
1. Check permissions in Settings
2. Verify Foreground Service is running
3. Check battery optimization settings
4. Ensure Google Play Services is up to date

**High battery usage**:
1. Switch to PASSIVE mode
2. Use BATTERY_OPTIMIZED configuration
3. Reduce update frequency
4. Check for location wakelock issues

### iOS

**Location not updating**:
1. Check authorization status
2. Verify "Always Allow" is granted
3. Check Background Modes in Xcode
4. Ensure Info.plist keys are present

**Authorization not working**:
1. Delete and reinstall app (resets permissions)
2. Check iOS Settings → Privacy → Location Services
3. Verify permission description strings in Info.plist

## Testing

### Android

```bash
# Simulate location updates
adb shell am start -a android.intent.action.VIEW \
  -d "geo:37.7749,-122.4194"

# Check foreground service
adb shell dumpsys activity services LocationTrackingService

# Monitor location updates
adb logcat | grep "AndroidLocationTracker"
```

### iOS

```bash
# Simulate location in Simulator
xcrun simctl location <device> set <lat> <lon>

# Monitor location updates in console
log stream --predicate 'subsystem == "com.po4yka.trailglass"'
```

## Future Enhancements

- [ ] Adaptive tracking (adjust frequency based on movement)
- [ ] Geofencing for automatic mode switching
- [ ] ML-based transport detection
- [ ] Offline location storage with sync
- [ ] Location mock detection
- [ ] Privacy zones (don't track certain areas)
- [ ] Battery usage analytics

## Resources

- [Android Location Best Practices](https://developer.android.com/training/location)
- [iOS Location Services](https://developer.apple.com/documentation/corelocation)
- [FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)
- [CLLocationManager](https://developer.apple.com/documentation/corelocation/cllocationmanager)

---

**Related Documentation**:
- [Location Processing Pipeline](../shared/src/commonMain/kotlin/com/po4yka/trailglass/location/README.md)
- [Database Layer](../shared/src/commonMain/kotlin/com/po4yka/trailglass/data/README.md)
- [Implementation Roadmap](../IMPLEMENTATION_NEXT_STEPS.md)
