# TrailGlass Live Activities Setup

This directory contains the implementation of iOS Live Activities for real-time location tracking updates.

## Features

- **Lock Screen Display**: Shows tracking metrics on the Lock Screen
- **Dynamic Island**: Displays compact tracking info in the Dynamic Island (iPhone 14 Pro+)
- **Real-time Updates**: Distance, duration, speed updated every 5 seconds
- **Status Indicators**: Visual feedback for active/paused/stopped states
- **Location Display**: Shows current location name when available

## Requirements

- iOS 16.1 or later
- Xcode 14.1 or later
- iPhone with Dynamic Island support (iPhone 14 Pro/Pro Max or later) for full experience

## Setup Instructions

### 1. Add Widget Extension Target (in Xcode)

1. Open `iosApp.xcodeproj` in Xcode
2. File → New → Target
3. Select "Widget Extension"
4. Name it `TrackingWidgetExtension`
5. **Important**: Check "Include Live Activity"
6. Add the files from this directory to the target

### 2. Configure Info.plist

Add to the **main app** `Info.plist`:

```xml
<key>NSSupportsLiveActivities</key>
<true/>
<key>NSSupportsLiveActivitiesFrequentUpdates</key>
<true/>
```

### 3. Add Capabilities

In **both** the main app and widget extension targets:

1. Signing & Capabilities tab
2. Add "Push Notifications" capability (for remote updates, optional)

### 4. Shared Code

Ensure `TrackingActivityAttributes.swift` is added to **both**:
- Main app target
- Widget extension target

This allows both to access the same activity definitions.

### 5. Integration

In your main app, use `TrackingActivityManager`:

```swift
// Start tracking with Live Activity
if #available(iOS 16.1, *) {
    let manager = TrackingActivityManager.shared
    manager.startActivity(trackingMode: "Active", tripName: "Morning Run")

    // Observe location tracker
    manager.observeTracker(locationTracker)
}

// Update manually
manager.updateActivity(
    distanceMeters: 1500.0,
    durationSeconds: 600,
    currentSpeedMps: 2.5,
    locationCount: 45,
    currentLocation: "Central Park"
)

// End activity
manager.endActivity(finalDistance: 5000.0)
```

## Files

- **TrackingActivityAttributes.swift**: Defines the Live Activity data structure
- **TrackingLiveActivity.swift**: UI for Lock Screen and Dynamic Island
- **TrackingActivityManager.swift**: Manages activity lifecycle and updates

## Metrics Displayed

### Lock Screen
- Distance (meters/kilometers)
- Duration (HH:MM:SS)
- Speed (km/h)
- Location points count
- Current location name
- Tracking status (Active/Paused/Stopped)

### Dynamic Island
- **Compact**: App icon + distance
- **Minimal**: App icon only
- **Expanded**:
  - Distance (leading)
  - Duration (trailing)
  - Speed (center)
  - Current location (bottom)
  - Status indicator

## Testing

1. Build and run the app on a physical device (iOS 16.1+)
2. Start location tracking
3. Lock the device to see the Live Activity
4. Long-press the Dynamic Island to see expanded view

## Troubleshooting

**Live Activity not appearing:**
- Check iOS version (must be 16.1+)
- Verify `NSSupportsLiveActivities` in Info.plist
- Ensure widget extension is properly configured
- Check Live Activities are enabled in Settings → Notifications

**Updates not showing:**
- Verify update frequency (max once every 5 seconds)
- Check console for error messages
- Ensure tracking state is being observed

**Dynamic Island not working:**
- Requires iPhone 14 Pro or later
- Falls back to Lock Screen on other devices

## Performance Notes

- Updates throttled to every 5 seconds to save battery
- Uses `.immediate` dismissal policy when stopped
- Efficient state observation with Combine/Flow bridge
- Low priority notification channel for minimal interruption

## Future Enhancements

- Remote push updates for background updates
- Interactive buttons in Live Activity
- Trip statistics history
- Photo thumbnail in expanded view
- Route visualization preview
