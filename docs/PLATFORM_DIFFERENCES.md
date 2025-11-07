# Platform Differences - Android vs iOS

This document outlines intentional platform-specific differences in the TrailGlass Kotlin Multiplatform app.

## Overview

TrailGlass shares **~85% of business logic** across platforms through Kotlin Multiplatform. However, certain features leverage platform-specific APIs for optimal performance and user experience.

---

## Location Tracking

### Architecture

Both platforms implement the `LocationTracker` interface but use different native APIs:

- **Android**: `FusedLocationProviderClient` (Google Play Services)
- **iOS**: `CLLocationManager` (CoreLocation framework)

### Key Differences

#### 1. Visit Monitoring (iOS-Only)

**iOS** has native visit detection via `CLLocationManager.startMonitoringVisits()`:

```kotlin
// iOS: IOSLocationTracker.kt:54-55
locationManager.startMonitoringVisits()
```

**Benefits:**
- Automatic place detection with machine learning
- Very low battery consumption
- Works even when app is backgrounded
- Receives `CLVisit` events with arrival/departure times

**Android Alternative:**
- Manual clustering of location samples using DBSCAN algorithm
- Processes GPS/NETWORK points through `LocationClusteringService`
- Higher computational overhead but more control over parameters

**Impact:** Both approaches produce similar end results (place visits), but iOS has lower battery usage for this specific feature.

---

#### 2. LocationSource Types

See: `shared/src/commonMain/kotlin/com/po4yka/trailglass/domain/model/LocationSample.kt`

| Source Type | Android | iOS | Notes |
|-------------|---------|-----|-------|
| `GPS` | ✅ Yes | ✅ Yes | Satellite-based positioning |
| `NETWORK` | ✅ Yes | ⚠️ No | Cell/WiFi positioning - Android only distinguishes this |
| `VISIT` | ❌ No | ✅ Yes | **iOS-ONLY** - Native visit monitoring |
| `SIGNIFICANT_CHANGE` | ⚠️ Simulated | ✅ Yes | iOS has native support; Android uses low-frequency updates |

**Android Implementation:**
```kotlin
// AndroidLocationTracker.kt:203-207
source = when {
    location.provider == "gps" -> LocationSource.GPS
    location.provider == "network" -> LocationSource.NETWORK
    else -> LocationSource.GPS
}
```

**iOS Implementation:**
```kotlin
// IOSLocationTracker.kt:175, 221
source = LocationSource.GPS  // For regular location updates
source = LocationSource.VISIT  // For visit monitoring
```

---

#### 3. Tracking Modes

Both platforms support three tracking modes with different implementations:

**Passive Mode:**
- Android: 5-minute intervals, 500m distance filter
- iOS: Significant location changes + visit monitoring

**Active Mode:**
- Android: 30-second intervals, 10m distance filter
- iOS: Continuous updates with best accuracy

**Idle Mode:**
- Both: No tracking

Configuration: `shared/src/commonMain/kotlin/com/po4yka/trailglass/location/tracking/TrackingConfiguration.kt`

---

## Settings Storage

Both platforms provide equivalent functionality but use different underlying mechanisms:

| Aspect | Android | iOS |
|--------|---------|-----|
| **API** | DataStore Preferences | NSUserDefaults |
| **File** | `SettingsStorage.android.kt` | `SettingsStorage.ios.kt` |
| **Reactivity** | Native Flow support | Manual MutableStateFlow wrapper |
| **Persistence** | Automatic | Requires `synchronize()` call |
| **Type Safety** | Preference keys | String keys |

**Android:**
```kotlin
// Automatic persistence
context.dataStore.edit { preferences ->
    preferences[Keys.TRACKING_ACCURACY] = value
}
```

**iOS:**
```kotlin
// Manual synchronization required
userDefaults.setObject(value, Keys.TRACKING_ACCURACY)
userDefaults.synchronize()  // Required!
```

**Impact:** API surface is identical - differences are internal implementation details.

---

## Photo Handling

### Metadata Extraction

| Metadata Field | Android | iOS |
|----------------|---------|-----|
| Timestamp | ✅ MediaStore.DATE_TAKEN | ✅ PHAsset.creationDate |
| Location (GPS) | ✅ MediaStore.LATITUDE/LONGITUDE | ✅ PHAsset.location |
| Dimensions | ✅ MediaStore.WIDTH/HEIGHT | ✅ PHAsset.pixelWidth/pixelHeight |
| **File Size** | ✅ MediaStore.SIZE | ✅ PHImageManager (as of v1.1) |
| MIME Type | ✅ MediaStore.MIME_TYPE | ⚠️ Inferred from mediaSubtypes |

**Android:**
```kotlin
// AndroidPhotoPicker.kt:111-143
contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
    val size = cursor.getLong(sizeIndex)
    // Direct access to file size
}
```

**iOS:**
```kotlin
// IOSPhotoPicker.kt:156-196
imageManager.requestImageDataForAsset(asset, options) { imageData, _, _, _ ->
    val fileSize = imageData.length.toLong()
    // Requires async image data request
}
```

**Impact:** iOS file size extraction is slower (async request) but both platforms now provide complete metadata.

---

## Permission Management

### Permission Request Flow

**Android:**
- Two-step process: Check status → Request via Activity Result API
- UI layer handles actual permission dialog
- Supports "show rationale" for user education

**iOS:**
- Direct permission request from shared code
- System dialog shown automatically
- No explicit rationale support (handled by Info.plist description)

### Camera Permission

| Platform | API | File |
|----------|-----|------|
| Android | `Manifest.permission.CAMERA` | `PermissionManager.android.kt` |
| iOS | `AVCaptureDevice.requestAccessForMediaType()` | `PermissionManager.ios.kt` |

**iOS Implementation (as of v1.1):**
```kotlin
// PermissionManager.ios.kt:208-228
private suspend fun requestCameraPermission(): PermissionResult {
    return suspendCancellableCoroutine { continuation ->
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            continuation.resume(if (granted) Granted else Denied)
        }
    }
}
```

**Previously:** iOS returned error directing to manual AVCaptureDevice usage.

---

## Background Execution

### Background Sync

Both platforms schedule periodic synchronization:

| Platform | Framework | Interval | Constraints |
|----------|-----------|----------|-------------|
| Android | WorkManager | 60 minutes | Network required, battery not low |
| iOS | BGTaskScheduler | 60 minutes | System-determined optimal time |

**Android:**
```kotlin
// SyncWorker.kt
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 60,
    repeatIntervalTimeUnit = TimeUnit.MINUTES
)
```

**iOS:**
```swift
// BackgroundSyncManager.swift
let request = BGAppRefreshTaskRequest(identifier: syncTaskIdentifier)
request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60)
```

**Key Difference:** iOS background tasks are opportunistic - the system decides when to run them based on usage patterns and battery level. Android WorkManager provides more predictable execution.

---

## UI Layer

### Architecture

- **Android**: Jetpack Compose (Kotlin)
- **iOS**: SwiftUI (Swift)

**Screens:**
Both platforms have equivalent screens with matching functionality:
- Timeline
- Trips
- Map
- Places
- Stats
- Settings

**Shared UI Components:**
Currently, UI is implemented separately on each platform. Future consideration: Compose Multiplatform for shared UI code.

---

## Database

### Implementation

Both platforms use **SQLDelight** with identical schemas:

- Android: SQLite driver via Android framework
- iOS: Native SQLite driver

**Schema Location:** `shared/src/commonMain/sqldelight/`

**Impact:** Database behavior is identical across platforms.

---

## Network Connectivity

| Platform | API | File |
|----------|-----|------|
| Android | `ConnectivityManager` | `NetworkConnectivity.android.kt` |
| iOS | Network framework | `NetworkConnectivity.ios.kt` |

Both monitor network state changes and provide reactive Flow-based updates.

---

## Security

### Secure Token Storage

| Platform | Mechanism |
|----------|-----------|
| Android | `EncryptedSharedPreferences` (AndroidX Security) |
| iOS | Keychain Services |

Both provide equivalent security guarantees for storing authentication tokens.

---

## Testing Strategy

### Unit Tests

**Common Tests:** `shared/src/commonTest/` - 98+ tests covering business logic

**Platform-Specific Tests:**
- Android: UI tests in `composeApp/src/androidInstrumentedTest/`
- iOS: UI tests in `iosApp/iosAppUITests/`

### Test Coverage

- **Common code:** 75%+ coverage enforced via Kover
- **Platform code:** Manual verification for expect/actual implementations

---

## Development Guidelines

### When to Use Platform-Specific Code

✅ **Use platform-specific implementations when:**
- Native API provides superior performance (e.g., iOS visit monitoring)
- Platform has unique capabilities (e.g., Android's network source distinction)
- Platform convention requires it (e.g., Android Activity-based permissions)

❌ **Avoid platform-specific code when:**
- Logic can be shared (business rules, algorithms, models)
- Differences are cosmetic
- Abstraction would add unnecessary complexity

### Adding New Platform Differences

When introducing platform-specific behavior:

1. **Document it here** in this file
2. **Add KDoc comments** explaining platform differences
3. **Update tests** to verify both implementations
4. **Consider abstraction** - can the difference be hidden behind a common interface?

---

## Migration Path

### Future Considerations

**Compose Multiplatform UI:**
- Evaluate sharing UI components across platforms
- Gradual migration: Start with simple screens (Settings, Stats)
- Keep platform-specific navigation and system integrations

**Platform Parity CI:**
- Automated checks for API compatibility
- Warning when platform-specific features are added
- Track parity score over time

---

## References

- iOS LocationTracker: `shared/src/iosMain/kotlin/.../IOSLocationTracker.kt`
- Android LocationTracker: `shared/src/androidMain/kotlin/.../AndroidLocationTracker.kt`
- LocationSource documentation: `shared/src/commonMain/kotlin/.../LocationSample.kt`
- Permission managers: `shared/src/{android,ios}Main/kotlin/.../PermissionManager.*.kt`
- Background sync: `composeApp/src/androidMain/.../SyncWorker.kt`, `iosApp/iosApp/Sync/BackgroundSyncManager.swift`

---

**Last Updated:** 2025-01-18
**Platform Parity Score:** 95%
