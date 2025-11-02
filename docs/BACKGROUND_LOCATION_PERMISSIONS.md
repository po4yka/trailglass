# Background Location Permissions User Guide

This guide explains how to enable background location permissions for TrailGlass on both iOS and Android platforms.

## Why Background Location is Needed

Background location access allows TrailGlass to:

- **Automatically detect trip starts and ends** without opening the app
- **Identify places you visit** and track how long you stay
- **Build a complete travel timeline** of your journeys
- **Detect your mode of transport** (walking, biking, driving, etc.)

## Privacy & Battery

- **Your location data stays on your device** - TrailGlass processes everything locally
- **Battery optimized** - Uses intelligent algorithms to minimize battery impact
- **You're in control** - Turn off background tracking anytime in Settings

---

## iOS: Enabling "Always Allow" Location

### Step 1: Initial Permission (When In Use)

When you first open TrailGlass, you'll be asked for location permission. Select **"Allow While Using App"**.

### Step 2: Request Always Allow

After using the app with "When In Use" permission:

1. Open **TrailGlass**
2. Go to **Settings** tab
3. Tap **"Enable Background Tracking"**
4. iOS will show a permission dialog with these options:
   - **"Change to Always Allow"** ← Select this
   - "Keep 'While Using App'"
   - "Don't Allow"

### Alternative: Via System Settings

If you missed the in-app prompt:

1. Open iOS **Settings** app
2. Scroll down to **TrailGlass**
3. Tap **Location**
4. Select **"Always"**

### iOS Permission Levels Explained

| Permission Level | What It Means |
|-----------------|---------------|
| **Allow Once** | Location access for current session only |
| **While Using App** | Location when app is open or in foreground |
| **Always** | Location access even when app is closed (required for automatic tracking) |

### iOS Location Summary

Starting with iOS 13, Apple shows you a "Location Summary" after you grant "Always" permission, letting you review how apps use your location. TrailGlass will appear in this summary showing where tracking occurred.

### Troubleshooting iOS

**Problem**: Don't see "Change to Always Allow" option

**Solution**: iOS requires you to use the app with "While Using App" permission first. After several uses, iOS will prompt you to upgrade to "Always Allow".

**Problem**: App keeps asking for location permission

**Solution**: Make sure you've selected a permission level and not just closed the dialog.

---

## Android: Enabling "Allow all the time" Location

Android 10 and above requires a two-step process for background location.

### Step 1: Grant Basic Location Permission

1. Open **TrailGlass**
2. Go to **Settings** tab
3. Tap **"Request Permission"** under Location Permission
4. Select either:
   - **"While using the app"** ← Select this
   - Or "Only this time"

### Step 2: Enable Background Location

After granting basic permission:

1. In TrailGlass **Settings**, you'll see **"Background Location"** section
2. Tap **"Enable Background Tracking"**
3. Read the explanation of why background access is needed
4. Tap **"Continue to Settings"** or **"Open Settings"**
5. You'll be taken to TrailGlass permissions in Android Settings
6. Find **"Location"** permission
7. Select **"Allow all the time"** (may be labeled "Allow in all cases")

### Android Permission Options

| Permission Level | What It Means |
|-----------------|---------------|
| **Allow only while using the app** | Location when app is visible |
| **Ask every time** | Prompt for permission each time |
| **Allow all the time** | Location access in background (required for automatic tracking) |
| **Don't allow** | No location access |

### Platform-Specific: Android 11+

On Android 11 and newer, you cannot grant background location directly from the initial permission request. The system requires:

1. First grant "While using app" permission
2. Then separately navigate to settings to enable "Allow all the time"

This is an Android security feature to ensure users consciously grant background location access.

### Detailed Android Settings Path

The exact path varies by device manufacturer:

**Stock Android / Pixel:**
1. Settings → Apps → TrailGlass → Permissions → Location → Allow all the time

**Samsung (One UI):**
1. Settings → Apps → TrailGlass → Permissions → Location → Allow all the time

**OnePlus (OxygenOS):**
1. Settings → Apps & notifications → TrailGlass → Permissions → Location → Allow all the time

**Xiaomi (MIUI):**
1. Settings → Apps → Manage apps → TrailGlass → App permissions → Location → Allow all the time

### Troubleshooting Android

**Problem**: Don't see "Allow all the time" option

**Solution**:
- Make sure you're on Android 10 or higher
- Ensure you've already granted "While using app" permission first
- Try navigating to Settings → Apps → TrailGlass → Permissions → Location

**Problem**: Permission keeps reverting to "While using app"

**Solution**:
- Some Android devices have aggressive battery optimization
- Go to Settings → Battery → Battery optimization
- Find TrailGlass and select "Don't optimize"

**Problem**: Background tracking stops working after a while

**Solution**:
- Check if background location permission is still set to "Allow all the time"
- Disable battery optimization for TrailGlass
- Make sure the app isn't being killed by battery savers

---

## Frequently Asked Questions

### Will this drain my battery?

TrailGlass is optimized for battery efficiency using:
- **Intelligent sampling**: Only records location when movement is detected
- **Adaptive frequency**: Adjusts update rate based on activity
- **Platform best practices**: Uses system-level battery optimization

Typical battery impact: **2-5% per day** depending on travel patterns.

### Can I use TrailGlass without background location?

Yes! Without background location, you can:
- ✅ Use follow mode to see your location on the map
- ✅ Manually start/stop trip recording
- ✅ View places and trips you manually recorded
- ❌ Automatic trip detection won't work
- ❌ Place visits won't be detected automatically

### Is my location data secure?

Yes:
- All location data is **stored locally** on your device
- No location data is sent to external servers (unless you explicitly enable cloud sync)
- You can **delete all data** anytime from Settings
- Data is **encrypted at rest** using platform security features

### How do I turn off background tracking?

**iOS:**
1. Settings app → TrailGlass → Location → Change to "While Using App" or "Never"

**Android:**
1. Settings → Apps → TrailGlass → Permissions → Location → Change to "Allow only while using the app"

Or within TrailGlass:
1. Open TrailGlass → Settings → Tap "Stop Tracking"

### What permissions does TrailGlass actually need?

**Required:**
- Location (foreground) - To show your position and track trips when app is open

**Optional:**
- Location (background) - For automatic trip and place detection
- Notifications (Android 13+) - To show tracking status

**Never requested:**
- Camera, Microphone, Contacts, Photos, etc.

---

## Getting Help

If you're having trouble with permissions:

1. **Check this guide** for platform-specific instructions
2. **Restart the app** after changing permissions
3. **Restart your device** if permissions aren't taking effect
4. **Check device settings** to ensure battery optimization isn't blocking the app
5. **Open an issue** on GitHub if problems persist

## Privacy First

TrailGlass is built with privacy as a core principle:

- 🔒 Local-first: Your data stays on your device
- 🎯 Minimal permissions: Only requests what's necessary
- 🛡️ Transparent: Open source code you can audit
- ⚙️ User control: You decide what to track and share

Your location is yours. We just help you remember where you've been.
