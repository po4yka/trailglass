# Android Enhanced Location Tracking Service

Enhanced foreground service with live notification updates for location tracking, providing an experience similar to iOS Live Activities.

## Features

- **Real-time Metrics**: Distance, duration, and speed updated continuously
- **Rich Notifications**: Expandable notification with detailed stats
- **Action Buttons**: Pause/Resume/Stop controls right in the notification
- **Low Battery Impact**: Efficient updates with minimal overhead
- **Android 14+ Compatibility**: Full support for latest Android features

## Components

### EnhancedLocationTrackingService

Main foreground service that displays live tracking updates in a notification.

**Features:**
- Auto-updating notification with real-time metrics
- Pause/Resume functionality
- Compact and expanded notification layouts
- Persistent across app restarts (START_STICKY)

### Notification Display

**Compact View:**
```
TrailGlass - Tracking
2.54 km  •  30:47  •  4.2 km/h
[Pause] [Stop]
```

**Expanded View:**
```
TrailGlass - Tracking

Distance: 2.54 km
Duration: 30:47
Speed: 4.2 km/h
Points: 152

[Pause] [Stop]
```

## Usage

### Start Tracking

```kotlin
EnhancedLocationTrackingService.startService(
    context = context,
    mode = TrackingMode.ACTIVE
)
```

### Pause Tracking

```kotlin
EnhancedLocationTrackingService.pauseService(context)
```

### Resume Tracking

```kotlin
EnhancedLocationTrackingService.resumeService(context)
```

### Stop Tracking

```kotlin
EnhancedLocationTrackingService.stopService(context)
```

## Notification Actions

The notification includes action buttons:

1. **Pause/Resume**: Toggle tracking without stopping the service
2. **Stop**: End tracking and remove the notification

## Permission Requirements

Declared in `AndroidManifest.xml`:

```xml
<!-- Foreground service for location tracking -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

<!-- Post notifications (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Location permissions -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

## Service Declaration

Add to `AndroidManifest.xml`:

```xml
<service
    android:name=".service.EnhancedLocationTrackingService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="location"
    tools:node="replace" />
```

## Notification Channel

Channel configuration:
- **ID**: `location_tracking_channel`
- **Name**: Location Tracking
- **Importance**: LOW (minimal interruption)
- **Visibility**: PUBLIC (shows on lock screen)
- **Badge**: Disabled

## Integration with LocationTracker

The service observes `LocationTracker.trackingState` flow and updates the notification when:
- Distance changes
- Duration updates (every second)
- Speed changes
- Location count increases

## Metrics Format

### Distance
- `< 1000m`: Displays in meters (e.g., "542 m")
- `>= 1000m`: Displays in kilometers (e.g., "2.54 km")

### Duration
- `< 1 hour`: MM:SS format (e.g., "30:47")
- `>= 1 hour`: H:MM:SS format (e.g., "1:05:23")

### Speed
- Converted from m/s to km/h
- Format: "X.X km/h" (e.g., "4.2 km/h")

## Battery Optimization

The service uses several techniques to minimize battery drain:

1. **Efficient Flow Collection**: Only updates when state changes
2. **Notification Throttling**: Updates respect Android's notification rate limits
3. **Low Priority Channel**: Minimal system resources
4. **Proper Lifecycle**: Cleanly stops when tracking ends

## Error Handling

The service handles common errors:
- Permission denied → Shows error and stops service
- Location unavailable → Logs error and continues
- Tracker initialization failed → Stops service gracefully

## Testing

### Manual Testing
1. Start tracking from the app
2. Navigate away or lock the device
3. Verify notification appears with live updates
4. Test pause/resume buttons
5. Test stop button

### Automated Testing
```kotlin
@Test
fun testServiceStartsWithNotification() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    EnhancedLocationTrackingService.startService(context, TrackingMode.ACTIVE)

    // Verify notification is shown
    // Verify service is in foreground
}
```

## Comparison with iOS Live Activities

| Feature | iOS Live Activities | Android Enhanced Service |
|---------|-------------------|------------------------|
| Lock Screen Display | ✅ | ✅ |
| Expandable Content | ✅ | ✅ |
| Action Buttons | Limited | ✅ (Pause/Resume/Stop) |
| Real-time Updates | ✅ | ✅ |
| Dynamic Island | ✅ (iPhone 14 Pro+) | N/A |
| Battery Impact | Very Low | Low |
| Persistence | Session only | Session + START_STICKY |

## Required Drawable Resources

Add these icon resources to `res/drawable`:

```
ic_location.xml  (location tracking icon)
ic_pause.xml     (pause button)
ic_play.xml      (resume button)
ic_stop.xml      (stop button)
```

Example `ic_location.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7z"/>
</vector>
```

## Future Enhancements

- [ ] Add route preview map in expanded notification
- [ ] Support for quick replies/notes
- [ ] Integration with Wear OS
- [ ] Custom notification styles per Android version
- [ ] Photo/video capture buttons
- [ ] Trip statistics summary on stop

## Troubleshooting

**Notification not showing:**
- Check POST_NOTIFICATIONS permission (Android 13+)
- Verify notification channel is created
- Check if app is in battery optimization whitelist

**Updates not appearing:**
- Verify LocationTracker is emitting state updates
- Check if service is running (adb shell dumpsys activity services)
- Look for crashes in logcat

**High battery usage:**
- Review location update frequency
- Check if service is stopping properly
- Consider using IDLE or PASSIVE tracking mode

## References

- [Android Foreground Services](https://developer.android.com/guide/components/foreground-services)
- [Notification Best Practices](https://developer.android.com/design/ui/mobile/guides/patterns/notifications)
- [Location Best Practices](https://developer.android.com/training/location/permissions)
