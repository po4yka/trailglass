# Design Prompt Variations

Collection of tested prompt variations for different use cases. Each variation builds on the base [kmp-mockup-prompt.md](./kmp-mockup-prompt.md).

---

## Table of Contents

1. [Onboarding Flow](#onboarding-flow)
2. [Photo Features Showcase](#photo-features-showcase)
3. [Settings & Configuration](#settings--configuration)
4. [Map Features Deep Dive](#map-features-deep-dive)
5. [Dark Mode Focused](#dark-mode-focused)
6. [Single Platform (iOS Only)](#single-platform-ios-only)
7. [Single Platform (Android Only)](#single-platform-android-only)
8. [Vertical Comparison Layout](#vertical-comparison-layout)

---

## Onboarding Flow

**Use Case**: Show new user experience from app launch to first trip logged.

**Journey**: Welcome → Permissions → First Trip → Success

### Modifications to Base Prompt

Replace the **Primary Features** section with:

```
Primary Features:
1. Welcome Screen - App logo, tagline "Track Your Travels Privately", value propositions, Get Started button
2. Location Permissions - Permission request dialog, "Always Allow" explanation, privacy guarantees
3. First Trip Setup - Empty timeline state, "Start Tracking" prompt, simple instructions
4. Success State - First trip logged, completion badge, "You're all set!" message with next steps
```

Replace **SCREEN-BY-SCREEN CONTENT SPECIFICATION** with:

```
Pair 1: Welcome Screen
Label: "Welcome"

Android (Left):
- Full-screen hero with Light Cyan gradient background
- App logo (map pin with path icon) centered
- Headline: "Track Your Travels Privately" in Roboto Bold, Jet Black
- Three value propositions with icons:
  - "Automatic trip detection" | figure.walk icon
  - "Photo & journal integration" | camera icon
  - "All data stored locally" | shield icon in Sea Glass (#8BB5A1)
- Filled button: "Get Started" in Cool Steel (#9DB4C0)
- Text button: "Learn More" in Blue Slate (#5C6B73)

iOS (Right):
- Full-screen with Light Cyan background and subtle blur
- Same app logo centered with soft shadow
- Headline: "Track Your Travels Privately" in SF Pro Display Bold
- Same three value propositions with SF Symbols:
  - figure.walk.circle.fill
  - camera.fill
  - shield.lefthalf.filled.fill
- Large filled capsule button: "Get Started" in Cool Steel
- Plain text button: "Learn More" below

Pair 2: Location Permissions
Label: "Permissions"

Android (Left):
- System permission dialog (Android 14 style)
- Title: "Allow Trailglass to access this device's location?"
- Radio options:
  - "Precise" (selected)
  - "Approximate"
- Buttons: "While using the app", "Only this time", "Don't allow"
- Below dialog: Explanation card in Light Blue (#C2DFE3)
  - "Why we need location access"
  - "Always Allow enables automatic trip tracking in the background"
  - Privacy badge with "Data never leaves your device"

iOS (Right):
- iOS permission alert (iOS 17 style)
- Title: "Allow 'Trailglass' to use your location?"
- Explanation text
- Buttons stack:
  - "Allow While Using App"
  - "Allow Once"
  - "Don't Allow"
- Below: Same explanation card in Light Blue with translucent background
  - iOS-styled text with SF Pro
  - Shield icon in Sea Glass

Pair 3: First Trip Setup
Label: "First Trip"

Android (Left):
- Timeline screen (empty state)
- Large illustration: Map with dotted path (Light Blue/Cool Steel colors)
- Headline: "Ready to Track" in Roboto Bold
- Body text: "Tap the button below to start logging your first trip"
- Large Extended FAB: "Start Tracking" with location icon, Cool Steel (#9DB4C0)
- Bottom Navigation: Timeline (active), Map, Stats (all visible but inactive)
- Hint text: "Tracking runs in the background"

iOS (Right):
- Timeline screen (empty state)
- Same illustration styled for iOS with softer edges
- Headline: "Ready to Track" in SF Pro Display Semibold
- Body text in SF Pro
- Large filled button: "Start Tracking" with SF Symbol location.fill
- Tab Bar: clock.fill (active), map, chart.bar, person
- Hint text below with info icon

Pair 4: Success State
Label: "Success"

Android (Left):
- Timeline screen with first trip logged
- Card at top with Sea Glass (#8BB5A1) success badge:
  - Checkmark icon
  - "Trip Logged!" headline
  - "Morning Walk • 1.2 km • 18 min"
  - Small map thumbnail showing route in Coastal Path (#7A9CAF)
- Below: Next steps card
  - "What's next?" headline in Blue Slate
  - "Attach photos" with camera icon
  - "Add journal entry" with edit icon
  - "View on map" with map icon
- Extended FAB: "New Trip" in Cool Steel
- Bottom Navigation: Timeline active

iOS (Right):
- Timeline screen with first trip
- Success card with SF Symbol checkmark.circle.fill in Sea Glass
- Same trip details with iOS styling
- Map thumbnail with iOS MapKit rendering
- Next steps in iOS List style (inset grouped)
  - Each row with chevron.right
  - "Attach Photos"
  - "Add Notes"
  - "View Route"
- Navigation bar + button for new trip
- Tab bar with clock.fill active
```

---

## Photo Features Showcase

**Use Case**: Demonstrate photo attachment, gallery, and map visualization.

**Journey**: Photo Gallery → Attachment → Detail → Map View

### Modifications

Replace **Primary Features**:

```
Primary Features:
1. Photo Gallery - Grid of attached photos organized by trip, filters by date/location
2. Photo Attachment - Dialog showing trip selection and attachment flow
3. Photo Detail View - Full-screen photo with location, timestamp, EXIF data, and sharing
4. Photos on Map - Map view with photo markers, clustering, and tap-to-view functionality
```

### Screen Content

```
Pair 1: Photo Gallery Grid
Pair 2: Attachment Dialog
Pair 3: Photo Detail with EXIF
Pair 4: Photo Map Markers

[Detailed screen specs following same pattern as base prompt]
```

---

## Settings & Configuration

**Use Case**: Show depth of customization and privacy controls.

**Journey**: Main Settings → Algorithm Settings → Privacy → Device Management

### Modifications

```
Primary Features:
1. Main Settings - Account, preferences, algorithm tuning, privacy controls
2. Algorithm Settings - Trip detection parameters, clustering settings, transport mode detection
3. Privacy Settings - Data export, encryption, permissions management
4. Device Management - Sync status, storage usage, connected devices
```

---

## Map Features Deep Dive

**Use Case**: Showcase all mapping capabilities in detail.

**Journey**: Standard Map → Satellite → Route Viz → Clustering

### Modifications

```
Primary Features:
1. Standard Map View - Default map with places and routes, controls visible
2. Satellite View - Same area with satellite imagery, route overlay
3. Route Visualization - Focused route view with elevation profile and transport mode segments
4. Place Clustering - Zoomed out view showing clustering algorithm, tap to expand
```

---

## Dark Mode Focused

**Use Case**: Marketing materials emphasizing dark mode design.

**Changes**: Force all screens to dark mode only.

### Modifications to Base Prompt

In the **APP SPECIFIC CUSTOMIZATION** section, modify themes:

```
Android Theme (Material 3 Expressive - DARK MODE ONLY):
- Primary: Light Blue (#C2DFE3)
- Primary Container: Blue Slate (#5C6B73)
- Background: Jet Black (#253237)
- Surface: Slightly lighter (#1A2428)
- All cards and elements using dark color scheme
- Increased elevation with tonal overlays
- Glow effects on interactive elements

iOS Theme (Native/Liquid Glass - DARK MODE ONLY):
- Primary: Light Blue (#C2DFE3) with adaptive blur
- Background: Jet Black (#253237)
- Translucent overlays at 85% opacity
- Increased blur radius for depth
- Soft shadows with reduced opacity
- Light Cyan text (#E0FBFC) throughout
```

Add to **DETAILED VISUAL SPECIFICATIONS**:

```
DARK MODE REQUIREMENTS:
- Show ONLY dark mode, no light mode elements
- Emphasize depth and translucency
- Subtle glow effects on primary actions (Cool Steel glow)
- Increased contrast for accessibility
- Stars or subtle particles in background gradient (optional)
- Professional dark aesthetic suitable for OLED displays
```

---

## Single Platform (iOS Only)

**Use Case**: iOS-focused mockup for App Store or iOS portfolio.

### Modifications

```
<Create a professional iOS app mockup figure>
showing exactly 4 iPhone 15 Pro Max screens arranged horizontally in a single high-resolution image. The screens demonstrate a complete user journey for a privacy-respectful travel logging application.

[Remove all Android specifications]
[Remove platform comparison language]
[Increase spacing between devices to 120px]
[Focus entirely on iOS native design, SwiftUI, and HIG compliance]
```

### Layout Changes

```
Layout Configuration:
- Arrange 4 iPhone 15 Pro Max devices horizontally
- Equal spacing: 120px between each device
- Slight 3D tilt: 6 degrees for depth
- Background: Light Cyan to Light Blue gradient
```

---

## Single Platform (Android Only)

**Use Case**: Android-focused mockup for Play Store or Material Design portfolio.

### Modifications

```
<Create a professional Android app mockup figure>
showing exactly 4 Google Pixel 8 Pro screens arranged horizontally in a single high-resolution image. The screens demonstrate a complete user journey for a privacy-respectful travel logging application.

[Remove all iOS specifications]
[Remove platform comparison language]
[Increase spacing between devices to 120px]
[Focus entirely on Material 3 Expressive design]
```

---

## Vertical Comparison Layout

**Use Case**: When horizontal space is limited (print materials, mobile-optimized web).

### Modifications

```
Layout Configuration:
- Arrange in 2 ROWS of 4 screens each (8 screens total)
- Top row: Android (Pixel 8 Pro) showing Timeline, Map, Details, Stats
- Bottom row: iOS (iPhone 15 Pro Max) showing same 4 screens
- Vertical alignment so corresponding screens are stacked
- Inter-column gap: 100px
- Inter-row gap: 120px
- Column labels above: "Timeline", "Map", "Trip Details", "Statistics"
- Row labels on left: "Android (Material 3)" and "iOS (Native)"
```

### Visual Specifications

```
Device Arrangement:
Row 1 (Android): [Timeline] [Map] [Details] [Stats]
                      ↓       ↓       ↓        ↓
Row 2 (iOS):     [Timeline] [Map] [Details] [Stats]

- Vertical lines connecting corresponding screens (subtle, Light Blue)
- Each column showing same functional screen
- Easy vertical comparison
```

---

## Testing Matrix

Document your successful variations:

| Variation | Status | Notes | File |
|-----------|--------|-------|------|
| Base KMP 8-screen | ✓ Tested | Default horizontal layout | `generated/base-v1.png` |
| Onboarding Flow | ✓ Tested | Shows new user experience | `generated/onboarding-v1.png` |
| Photo Features | ⏳ In Progress | - | - |
| Dark Mode | ✓ Tested | Beautiful dark aesthetic | `generated/dark-mode-v1.png` |
| iOS Only | ✓ Tested | 4 screens, App Store ready | `generated/ios-only-v1.png` |
| Vertical Layout | ⏳ In Progress | - | - |

---

## Contributing New Variations

When you create a successful variation:

1. Document it in this file following the template
2. Save the generated image to `generated/` with descriptive name
3. Note any specific prompt changes required
4. Add to the testing matrix above

### Variation Template

```markdown
## [Variation Name]

**Use Case**: [What this variation is for]

**Journey**: [Screen 1] → [Screen 2] → [Screen 3] → [Screen 4]

### Modifications to Base Prompt

[Specific changes to make]

### Screen Content

[Detailed screen specifications if needed]

### Notes

[Any special considerations or tips]
```

---

## Related Documentation

- [kmp-mockup-prompt.md](./kmp-mockup-prompt.md) - Base prompt template
- [prompt-usage-guide.md](./prompt-usage-guide.md) - How to use and iterate
- [color-scheme-reference.md](./color-scheme-reference.md) - Color specifications
