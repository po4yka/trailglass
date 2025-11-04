# Network Status Banner - Integration Guide

## Overview

The `NetworkStatusBanner` component has been created to match iOS functionality, displaying network connectivity status to users when offline or limited.

## Components

### 1. NetworkStatusBanner
Full banner shown at the top of screens when network is unavailable or limited.

**File:** `composeApp/src/androidMain/kotlin/com/po4yka/trailglass/ui/components/NetworkStatusBanner.kt`

### 2. NetworkStatusIndicatorCompact
Compact indicator for toolbars showing network status with an icon.

**File:** `composeApp/src/androidMain/kotlin/com/po4yka/trailglass/ui/components/NetworkStatusBanner.kt`

### 3. NetworkStatusWrapper
Wrapper composable to easily add network banner to any screen.

**File:** `composeApp/src/androidMain/kotlin/com/po4yka/trailglass/ui/components/NetworkStatusWrapper.kt`

## Integration Steps

### Step 1: Add NetworkConnectivityMonitor to AppComponent

Edit `shared/src/commonMain/kotlin/com/po4yka/trailglass/di/AppComponent.kt`:

```kotlin
@AppScope
@Component
abstract class AppComponent(
    @Component val platformModule: PlatformModule
) : DataModule, LocationModule, SyncModule, PermissionModule {

    // ... existing code ...

    // Add network connectivity monitor
    abstract val networkConnectivityMonitor: com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
}
```

### Step 2: Update PlatformModule

Ensure the Android platform module provides the network monitor:

```kotlin
// In AndroidPlatformModule or similar
@Provides
@AppScope
fun provideNetworkConnectivityMonitor(context: Context): NetworkConnectivityMonitor {
    return AndroidNetworkConnectivityMonitor(context)
}
```

### Step 3: Start Monitoring in Application Class

In `TrailGlassApplication.kt`:

```kotlin
override fun onCreate() {
    super.onCreate()

    // Start network monitoring
    appComponent.networkConnectivityMonitor.startMonitoring()

    // ... existing initialization code ...
}

override fun onTerminate() {
    super.onTerminate()
    appComponent.networkConnectivityMonitor.stopMonitoring()
}
```

### Step 4: Integrate into UI

#### Option A: Global Integration (Recommended)

Wrap the entire app content in `MainScaffold`:

```kotlin
// In Navigation.kt - MainScaffold function
@Composable
fun MainScaffold(
    rootComponent: RootComponent,
    networkConnectivityMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    // ... existing code ...

    Scaffold(
        modifier = modifier,
        topBar = { /* ... */ },
        bottomBar = { /* ... */ }
    ) { paddingValues ->
        // Wrap content with network status
        NetworkStatusWrapper(
            networkConnectivityMonitor = networkConnectivityMonitor
        ) {
            Children(
                stack = childStack,
                modifier = Modifier.padding(paddingValues)
            ) { child ->
                // ... existing screen rendering ...
            }
        }
    }
}
```

Then update `App.kt` to pass the monitor:

```kotlin
@Composable
fun App(
    rootComponent: RootComponent,
    networkConnectivityMonitor: NetworkConnectivityMonitor
) {
    TrailGlassTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScaffold(
                rootComponent = rootComponent,
                networkConnectivityMonitor = networkConnectivityMonitor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
```

And in `MainActivity.kt`:

```kotlin
setContent {
    App(
        rootComponent = rootComponent,
        networkConnectivityMonitor = appComponent.networkConnectivityMonitor
    )
}
```

#### Option B: Per-Screen Integration

Add to individual screens:

```kotlin
@Composable
fun SomeScreen(
    controller: SomeController,
    networkConnectivityMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    NetworkStatusWrapper(
        networkConnectivityMonitor = networkConnectivityMonitor
    ) {
        // Screen content
    }
}
```

#### Option C: Use Compact Indicator in TopAppBar

```kotlin
TopAppBar(
    title = { Text(currentScreen.title) },
    actions = {
        val networkInfo by networkConnectivityMonitor.networkInfo.collectAsState()
        NetworkStatusIndicatorCompact(
            networkState = networkInfo.state,
            networkType = networkInfo.type
        )
    }
)
```

## Features

### Network States

- **Connected**: No banner shown, green indicator
- **Disconnected**: Red banner with "No internet connection"
- **Limited**: Orange banner with limitation reason

### Network Types

- **WIFI**: WiFi icon
- **CELLULAR**: Cellular icon
- **ETHERNET**: Ethernet cable icon
- **NONE**: No connection

### Metered Connection

When connected via metered network (cellular), shows "Using cellular (metered)" subtitle.

## Platform Parity

This implementation matches the iOS `NetworkStatusBanner.swift` functionality:

| Feature | iOS | Android |
|---------|-----|---------|
| Disconnected banner | ✅ | ✅ |
| Limited connectivity banner | ✅ | ✅ |
| Network type indicator | ✅ | ✅ |
| Metered connection warning | ✅ | ✅ |
| Compact indicator | ✅ | ✅ |
| Animated show/hide | ✅ | ✅ |

## Testing

To test the banner:

1. Turn on airplane mode → Should show "No internet connection" banner
2. Connect to cellular → Should show cellular icon and "metered" notice
3. Connect to WiFi → Banner should hide, shows WiFi icon if using compact indicator
4. Disconnect WiFi but leave data on → Should show network type icon

## Current Status

✅ Components created
✅ Styling matches iOS version
⚠️ **Needs integration** - NetworkConnectivityMonitor not yet in AppComponent
⚠️ **Needs wiring** - Components not yet used in screens

## Next Steps

1. Add `networkConnectivityMonitor` to `AppComponent`
2. Start monitoring in `TrailGlassApplication.onCreate()`
3. Pass monitor to `MainScaffold` via `App` composable
4. Test with various network conditions

## Related Files

- iOS implementation: `iosApp/iosApp/Components/NetworkStatusBanner.swift`
- Network monitor (Android): `shared/src/androidMain/kotlin/.../NetworkConnectivityMonitor.android.kt`
- Network monitor (iOS): `shared/src/iosMain/kotlin/.../NetworkConnectivityMonitor.ios.kt`
- Platform differences doc: `docs/PLATFORM_DIFFERENCES.md`
