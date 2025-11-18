# iOS Privacy Manifest - TrailGlass

**Created:** 2025-11-18
**Status:** ✅ Complete
**iOS Version Required:** 17.0+

---

## Overview

Apple requires all iOS apps to declare their data collection and API usage through privacy manifests starting with iOS 17. This document explains TrailGlass's privacy manifest configuration.

---

## Files Added/Updated

### 1. ✅ `iosApp/iosApp/Info.plist`
**Updated with permission descriptions:**
- `NSLocationWhenInUseUsageDescription` - Location tracking during app use
- `NSLocationAlwaysAndWhenInUseUsageDescription` - Background location tracking
- `NSLocationAlwaysUsageDescription` - Background trip tracking
- `NSCameraUsageDescription` - Photo capture for trips
- `NSPhotoLibraryUsageDescription` - Access to attach photos
- `NSPhotoLibraryAddUsageDescription` - Save captured photos
- `NSUserNotificationsUsageDescription` - Trip updates and notifications
- `NSMotionUsageDescription` - Activity recognition (walking, driving, etc.)

### 2. ✅ `iosApp/iosApp/PrivacyInfo.xcprivacy`
**New file - Apple's modern privacy manifest format**

Contains declarations for:
- Privacy tracking status
- Collected data types
- Required Reason APIs accessed

---

## Privacy Manifest Details

### Tracking Declaration

```xml
<key>NSPrivacyTracking</key>
<false/>
```

**TrailGlass does NOT track users** for advertising or analytics purposes. All location data is collected solely for the app's core functionality (journey tracking).

### Data Collection

TrailGlass collects the following data types:

#### 1. Precise Location
- **Purpose:** App Functionality
- **Linked to User:** Yes
- **Used for Tracking:** No
- **Why:** Core feature - tracks user journeys and visits

#### 2. Photos/Videos
- **Purpose:** App Functionality
- **Linked to User:** Yes
- **Used for Tracking:** No
- **Why:** Allows users to attach photos to trips and places

#### 3. User Content (Trips, Places, Notes)
- **Purpose:** App Functionality
- **Linked to User:** Yes
- **Used for Tracking:** No
- **Why:** Stores user's travel history and journal entries

#### 4. Device ID
- **Purpose:** App Functionality
- **Linked to User:** Yes
- **Used for Tracking:** No
- **Why:** Enables multi-device sync and device management

#### 5. Email Address
- **Purpose:** App Functionality
- **Linked to User:** Yes
- **Used for Tracking:** No
- **Why:** User authentication and account management

#### 6. Diagnostic Data
- **Purpose:** App Functionality, Analytics
- **Linked to User:** No (anonymized)
- **Used for Tracking:** No
- **Why:** Crash reports and performance monitoring

---

## Required Reason APIs

Apple requires apps to declare why they use certain "privacy-sensitive" APIs:

### 1. UserDefaults API
- **Reason Code:** `CA92.1`
- **Description:** Access app-specific user defaults
- **Usage:** Store user preferences, settings, and app state

### 2. File Timestamp APIs
- **Reason Codes:** `C617.1`, `0A2A.1`
- **Description:** Access file creation/modification times
- **Usage:**
  - Local database sync (know which data is newer)
  - Display trip/photo timestamps to users
  - Cache management

### 3. System Boot Time
- **Reason Code:** `35F9.1`
- **Description:** Measure elapsed time
- **Usage:** Calculate accurate time intervals for location tracking (uptime-based timestamps)

### 4. Disk Space
- **Reason Code:** `E174.1`
- **Description:** Check available storage
- **Usage:**
  - Warn users when storage is low
  - Manage local database size
  - Optimize photo storage

### 5. Active Keyboards
- **Reason Code:** `54BD.1`
- **Description:** Custom keyboard functionality
- **Usage:** Text input for trip names, notes, search queries

---

## Xcode Integration Steps

### ⚠️ IMPORTANT: Manual Step Required

The `PrivacyInfo.xcprivacy` file must be added to the Xcode project:

1. Open `iosApp.xcodeproj` in Xcode
2. Right-click on the `iosApp` folder in Project Navigator
3. Select "Add Files to iosApp..."
4. Navigate to `iosApp/iosApp/PrivacyInfo.xcprivacy`
5. Ensure "Copy items if needed" is **unchecked** (file is already in correct location)
6. Ensure "iosApp" target is **checked**
7. Click "Add"

### Verify Integration

1. Select the `iosApp` target in Xcode
2. Go to "Build Phases" tab
3. Expand "Copy Bundle Resources"
4. Verify `PrivacyInfo.xcprivacy` is listed

---

## App Store Requirements

### App Privacy Details (App Store Connect)

When submitting to the App Store, you'll need to configure privacy details matching this manifest:

#### Data Collected:
- ✅ Location (Precise)
  - Purpose: App Functionality
  - Linked to User

- ✅ Photos and Videos
  - Purpose: App Functionality
  - Linked to User

- ✅ User Content
  - Purpose: App Functionality
  - Linked to User

- ✅ Email Address
  - Purpose: App Functionality, Account Management
  - Linked to User

- ✅ Device ID
  - Purpose: App Functionality (multi-device sync)
  - Linked to User

- ✅ Crash Data
  - Purpose: Analytics, App Functionality
  - Not Linked to User

#### Tracking:
- ❌ This app does not track you

---

## Third-Party SDKs

### Current Dependencies

Check if any third-party frameworks need their own privacy manifests:

#### ✅ Kotlin Multiplatform Shared - No manifest needed (app-specific code)
#### ✅ SQLDelight - No manifest needed (local database)
#### ✅ Ktor Client - May need manifest if using analytics

### Action Items

If adding any of these in the future, verify they have privacy manifests:
- Firebase (Analytics, Crashlytics) - **Requires manifest**
- Google Analytics - **Requires manifest**
- Facebook SDK - **Requires manifest**
- AdMob - **Requires manifest**

---

## Privacy Policy Requirements

### ⚠️ Privacy Policy Needed

Apple requires a publicly accessible privacy policy URL. The policy should cover:

1. **What data is collected:**
   - Precise location data
   - Photos attached to trips
   - User-generated content (trip names, notes, places)
   - Email address for authentication
   - Device information for multi-device sync

2. **How data is used:**
   - Display user's travel history
   - Sync across devices
   - Generate statistics and insights
   - Export trips as GPX/KML

3. **How data is shared:**
   - Not shared with third parties (unless user explicitly shares)
   - Stored on TrailGlass backend server (encrypted in transit)
   - User controls data deletion

4. **User rights:**
   - Access all data
   - Export all data
   - Delete account and all data
   - Opt-out of optional features

5. **Data retention:**
   - Data retained until user deletes account
   - Backups retained for 30 days after deletion

### Example Privacy Policy URL Structure
```
https://trailglass.po4yka.com/privacy
```

---

## GDPR Compliance

If targeting EU users, ensure:

- ✅ Clear consent for data collection
- ✅ Right to access data (export feature)
- ✅ Right to deletion (account deletion)
- ✅ Right to portability (GPX/KML export)
- ✅ Data processing agreement (privacy policy)
- ⚠️ Cookie consent (if using web analytics)
- ⚠️ Data breach notification process

---

## Testing Privacy Manifest

### Before App Store Submission

1. **Build and Archive:**
   ```bash
   xcodebuild archive -workspace iosApp.xcworkspace \
                     -scheme iosApp \
                     -archivePath build/iosApp.xcarchive
   ```

2. **Check Privacy Report:**
   - Xcode will generate a privacy report during archiving
   - Review in Organizer → Archives → [Your Archive] → Privacy Report

3. **Validate with App Store:**
   - Use Xcode's "Validate App" before uploading
   - Checks for privacy manifest completeness

### Test Permission Flows

1. **Fresh Install:** Test on device that has never had the app
2. **Check Permission Prompts:** Ensure custom descriptions appear
3. **Test Denials:** Verify app handles denied permissions gracefully
4. **Test Settings Link:** Verify "Open Settings" flows work

---

## Common Issues & Solutions

### Issue: Privacy manifest not found during validation

**Solution:** Ensure `PrivacyInfo.xcprivacy` is:
1. In the correct directory (`iosApp/iosApp/`)
2. Added to Xcode project
3. Listed in "Copy Bundle Resources" build phase
4. Not in `.gitignore`

### Issue: Required Reason API warning

**Solution:** Add the API type and reason code to `NSPrivacyAccessedAPITypes` array in the manifest.

### Issue: Data type mismatch with App Store Connect

**Solution:** Ensure privacy details in App Store Connect exactly match the `NSPrivacyCollectedDataTypes` in the manifest.

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-18 | Initial privacy manifest creation |
| | | Added all required permission descriptions |
| | | Declared Required Reason APIs |
| | | Configured data collection types |

---

## Next Steps

### Immediate (Before Beta):
1. ✅ Add `PrivacyInfo.xcprivacy` to Xcode project (manual step)
2. ✅ Verify all permission descriptions in Info.plist
3. ⚠️ Test permission flows on real device
4. ⚠️ Create privacy policy webpage

### Before App Store Submission:
1. ⚠️ Add privacy policy URL to App Store Connect
2. ⚠️ Configure App Privacy Details in App Store Connect
3. ⚠️ Run privacy report validation in Xcode
4. ⚠️ Test on iOS 17+ device
5. ⚠️ Review and update for any new data collection

### Ongoing:
1. ⚠️ Update manifest when adding new features
2. ⚠️ Update manifest when adding third-party SDKs
3. ⚠️ Review annually for compliance
4. ⚠️ Update privacy policy when manifest changes

---

## References

- [Apple Privacy Manifest Documentation](https://developer.apple.com/documentation/bundleresources/privacy_manifest_files)
- [Required Reason API List](https://developer.apple.com/documentation/bundleresources/privacy_manifest_files/describing_use_of_required_reason_api)
- [App Privacy Details](https://developer.apple.com/app-store/app-privacy-details/)
- [GDPR Compliance Guide](https://gdpr.eu/compliance/)

---

**Maintained by:** TrailGlass Development Team
**Last Updated:** 2025-11-18
