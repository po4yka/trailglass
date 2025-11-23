# Trailglass KMP App Mockup Prompt

**Target**: Gemini 2.5 Flash Image (nano-banana) at https://aistudio.google.com/

**Output**: 8-screen horizontal comparison showing Android (Material 3 Expressive) and iOS (native) side-by-side

---

## Full Prompt

```
<Create a professional KMP app mockup figure>
showing exactly 8 device screens arranged horizontally in a single high-resolution image. The screens demonstrate a complete user journey for a privacy-respectful travel logging application, with each step shown on BOTH Android and iOS platforms side-by-side for direct comparison.

<APP SPECIFIC CUSTOMIZATION>
Application Core: Privacy-first location tracking and travel journaling with automated trip detection, timeline building, photo attachment, and local-only data storage

Primary Features:
1. Timeline View - Daily activity stream showing trips, place visits, route segments with duration, distance, and transport mode detection
2. Interactive Map - Real-time location display with visited places, travel routes, clustering, and map style options
3. Trip Details - Individual trip breakdown with attached photos, visited locations, transport modes, route visualization, and journal entries
4. Activity Statistics - Weekly/monthly heatmap, travel patterns, favorite places, distance traveled, and activity insights

Android Theme (Material 3 Expressive):
- Primary: Cool Steel (#9DB4C0) with Light Cyan (#E0FBFC) containers
- Secondary: Blue Slate (#5C6B73) with Light Blue (#C2DFE3) containers
- Background: Light Cyan (#E0FBFC) light mode, Jet Black (#253237) dark mode
- Surface: Slightly brighter (#F5FEFF) with tonal elevation system
- Accent colors: Sea Glass (#8BB5A1) for success/completed trips, Driftwood (#C9B896) for warnings
- Map routes: Coastal Path (#7A9CAF) for active routes, Harbor Blue (#5C8AA8) for historical
- Typography: Roboto with high contrast - Jet Black (#253237) on light backgrounds
- Components: Extended FAB, Bottom Navigation, elevated Material cards, ripple effects
- Motion: Spring animations, smooth transitions, Material motion specifications

iOS Theme (Native/Liquid Glass Design):
- Primary: Cool Steel (#9DB4C0) with adaptive blur effects
- Secondary: Blue Slate (#5C6B73) adapting to light/dark mode
- Background: Light Cyan (#E0FBFC) 85% opacity blur light mode, Jet Black (#253237) dark mode
- Surface: Translucent layers with Light Blue (#C2DFE3) tint and soft shadows
- Accent colors: Sea Glass (#8BB5A1) for success, Driftwood (#C9B896) for alerts
- Map routes: Coastal Path (#7A9CAF) matching Android, Harbor Blue (#5C8AA8) for history
- Typography: SF Pro with Jet Black (#253237) text, clear hierarchy
- Components: Native tab bars, navigation bars, standard iOS controls, context menus
- Effects: Translucent backgrounds, depth blur, native iOS animations, haptic feedback indicators

Target Audience: Privacy-conscious travelers and location enthusiasts aged 25-45 who value data ownership and detailed travel tracking
<END CUSTOMIZATION>

DETAILED VISUAL SPECIFICATIONS:

Device Frames:
- Android: Google Pixel 8 Pro with Material You design, edge-to-edge display, triple camera setup
- iOS: iPhone 15 Pro Max with Dynamic Island, edge-to-edge OLED, titanium frame

Screen Resolution:
- Android: 2992×1344 pixels per screen, crisp LTPO OLED rendering
- iOS: 2796×1290 pixels per screen, Super Retina XDR quality

Layout Configuration:
- Arrange as 4 pairs of screens horizontally (8 screens total)
- Each pair: Android device on LEFT, iOS device on RIGHT
- Equal spacing between devices (80-100px gap)
- Slight 3D perspective tilt (5-7 degrees) for depth

Background Setting:
- Subtle gradient from Cool Steel (#9DB4C0) to Light Blue (#C2DFE3) at edges
- Professional studio backdrop with soft depth-of-field blur
- Complementary to "Silent Waters" color palette

Lighting:
- Soft, diffused lighting from upper left (45-degree angle)
- Minimal shadows beneath devices for floating effect
- No harsh reflections on screens
- Professional presentation quality

PLATFORM-SPECIFIC UI/UX REQUIREMENTS:

Android (Material 3 Expressive) - LEFT DEVICES:

Core Components:
- Material Design 3 with dynamic color support (Android 12+ wallpaper theming capability)
- Roboto font family: Regular for body, Medium for buttons, Bold for headlines
- Typography scale: Display (57sp), Headline (32sp), Title (22sp), Body (16sp), Label (14sp)
- 4dp grid system following Material Design guidelines
- Minimum touch target: 48dp × 48dp

Navigation & Structure:
- Top App Bar: Medium height (64dp) with Cool Steel (#9DB4C0) background, scroll behavior
- Bottom Navigation: 3-4 items (Map, Timeline, Stats) with Blue Slate (#5C6B73) active state
- Extended FAB: Cool Steel (#9DB4C0) with "Start Tracking" or "New Trip" label
- Navigation drawer (if applicable): Light Cyan (#E0FBFC) background with account switcher

Interactive Elements:
- Buttons: Filled (Cool Steel), Outlined (Blue Slate border), Text (Blue Slate)
- Cards: Light Cyan (#E0FBFC) with 8dp corner radius, 2dp elevation, subtle shadows
- Chips: Blue Slate (#5C6B73) for filters, categories
- Switches/Toggles: Cool Steel (#9DB4C0) when active
- Ripple effects: Blue Slate (#5C6B73) at 20% opacity

Elevation System:
- Level 0: Background (#E0FBFC)
- Level 1: Surface (#F5FEFF) - 1dp elevation
- Level 2: Cards - 2dp elevation with tonal overlay
- Level 3: FAB - 6dp elevation
- Level 4: Navigation drawer - 16dp elevation

Status Bar & System UI:
- Android 14+ status bar with location indicator icon
- Status bar icons: Jet Black (#253237) in light mode
- Navigation gestures: 3-button or gesture navigation visible
- Edge-to-edge content with proper insets

Map Styling (Google Maps):
- Active route: Coastal Path (#7A9CAF) 6dp stroke
- Historical route: Harbor Blue (#5C8AA8) 4dp stroke with 50% opacity
- Place markers: Cool Steel (#9DB4C0) pins with drop shadow
- Cluster markers: Blue Slate (#5C6B73) circles with count
- Map style: Light mode with muted roads, emphasized water features

iOS (Native/Liquid Glass) - RIGHT DEVICES:

Core Components:
- SwiftUI native components following iOS 17+ Human Interface Guidelines
- SF Pro font family: Regular for body, Semibold for emphasis, Bold for titles
- Typography scale: Large Title (34pt), Title (28pt), Headline (17pt), Body (17pt), Caption (12pt)
- 8pt grid system following Apple guidelines
- Minimum touch target: 44pt × 44pt

Navigation & Structure:
- Navigation Bar: Large title style with Cool Steel (#9DB4C0) tint, translucent blur background
- Tab Bar: Translucent material with Cool Steel active tint, Blue Slate (#5C6B73) inactive
- SF Symbols: map.fill, clock.fill, figure.walk, chart.bar.fill icons
- Swipe gestures: Back gesture from left edge, context menu long-press

Interactive Elements:
- Buttons: Filled capsule (Cool Steel), bordered (Blue Slate), plain text (Blue Slate)
- Cards: Light Blue (#C2DFE3) with 12pt corner radius, translucent background (85% opacity)
- Tags/Pills: Blue Slate (#5C6B73) with 16pt corner radius
- Toggles: iOS native switch with Cool Steel (#9DB4C0) active state
- Lists: Inset grouped style with Light Cyan (#E0FBFC) background

Translucency & Depth:
- Navigation bars: Adaptive material with Cool Steel tint, auto-blur
- Tab bars: Translucent background with 70% opacity
- Sheets: Light Blue (#C2DFE3) background with blur effect
- Overlays: Light Cyan (#E0FBFC) at 85% opacity for modal presentations
- Shadows: Soft, subtle (2pt offset, 8pt blur, 10% opacity)

Status Bar & System UI:
- iOS 17+ status bar respecting Dynamic Island
- Status bar style: Dark content in light mode
- Safe area insets: Proper spacing around Dynamic Island and bottom indicator
- Native pull-to-refresh with haptic feedback indicator

Map Styling (Apple MapKit):
- Active route: Coastal Path (#7A9CAF) 5pt stroke with rounded caps
- Historical route: Harbor Blue (#5C8AA8) 3pt stroke with 60% opacity
- Place annotations: Cool Steel (#9DB4C0) custom pins with SF Symbols
- Cluster annotations: Blue Slate (#5C6B73) circles with white count text
- Map style: Standard with muted labels, natural terrain emphasis

SHARED CONTENT REQUIREMENTS:

Realistic Travel Data (SAME across both platforms):
- Trip examples: "Morning Commute to Office", "Weekend Hike: Mt. Tamalpais Summit Trail", "Coffee Break at Blue Bottle Hayes Valley"
- Dates & times: "Today, 8:42 AM", "Yesterday, 2:15 PM", "Dec 15, 2024"
- Distances: "2.4 km", "8.7 mi", "450 m"
- Durations: "45 min", "3h 20m", "1h 15m"
- Transport modes: Walking (figure.walk icon), Driving (car.fill), Cycling (bicycle), Transit (tram.fill)
- Place names: Real locations like "Golden Gate Park", "Ferry Building", "Dolores Park"
- Photo counts: "12 photos", "3 photos", "No photos"
- Statistics: "127 km this week", "18 places visited", "23 trips logged"

Data Consistency Rules:
- Identical trip names, timestamps, and metrics on corresponding Android/iOS screens
- Same map view center coordinates and zoom level
- Matching photo thumbnails in trip details
- Consistent activity heatmap data visualization
- Same transport mode icons (adapted to platform style - Material icons vs SF Symbols)

Privacy Indicators:
- Background location icon in status bar (both platforms)
- "All data stored locally" message visible in settings/about screens
- "Location tracking active" persistent notification (Android) / Live Activity indicator (iOS)

No Lorem Ipsum:
- All text must be realistic and contextual
- Trip descriptions: actual travel narratives
- Place names: real geographic locations
- User profile: realistic name like "Alex Chen" or "Sarah Martinez"
- Timestamps: believable dates within current month/year

SCREEN-BY-SCREEN CONTENT SPECIFICATION:

Pair 1: Timeline View
Label: "Timeline View"

Android (Left):
- Top App Bar: "Timeline" title, profile icon (right), filter icon (right)
- Date section headers: "Today", "Yesterday", "Dec 15"
- Card 1: "Morning Commute" | Walk icon | "8:42 AM - 9:27 AM" | "2.4 km • 45 min" | Blue Slate (#5C6B73) accent line
- Card 2: "Weekend Hike: Mt. Tamalpais" | Hike icon | "Yesterday, 8:00 AM" | "8.7 mi • 3h 20m" | Sea Glass (#8BB5A1) completion badge | "12 photos" with thumbnail grid (2x2)
- Card 3: Place visit "Blue Bottle Cafe" | Coffee icon | "2 hrs 15 min" | Driftwood (#C9B896) time indicator
- Extended FAB: "New Trip" with plus icon, Cool Steel (#9DB4C0)
- Bottom Navigation: Timeline (active, Blue Slate), Map, Stats, Profile

iOS (Right):
- Navigation Bar: Large title "Timeline", translucent blur, filter button (trailing)
- Same timeline cards with iOS styling:
  - Cards: Light Blue (#C2DFE3) translucent background, 12pt radius
  - SF Symbols: figure.walk, mountain.2.fill, cup.and.saucer.fill
  - Typography: SF Pro Semibold for trip names
  - Dividers: Light stroke between sections
- No FAB (iOS convention) - add trip via navigation bar + button
- Tab Bar: clock.fill (active), map.fill, chart.bar.fill, person.fill with Cool Steel tint

Pair 2: Interactive Map
Label: "Map View"

Android (Left):
- Top App Bar: Collapsed with "Map" title, layer switcher icon, search icon
- Google Maps showing San Francisco Bay Area
- User location: Blue dot with Cool Steel (#9DB4C0) pulse animation
- Place markers: 8-10 pins in Cool Steel (#9DB4C0), custom trip marker icons
- Active route: Coastal Path (#7A9CAF) polyline showing current trip path
- Historical routes: Harbor Blue (#5C8AA8) faded polylines (3-4 past trips)
- Cluster marker: Blue Slate (#5C6B73) circle "5" showing grouped nearby places
- Bottom sheet: Peek showing "Current Location: Ferry Building" with swipe handle
- FAB menu: Center location, add place, start tracking - stacked vertically
- Map controls: Zoom, compass on right side

iOS (Right):
- Navigation Bar: Translucent over map, "Map" title, layers icon (trailing)
- Apple MapKit showing same San Francisco view
- User location: Blue dot with Cool Steel pulse (iOS native style)
- Annotations: Cool Steel pins with SF Symbol glyphs (mappin.circle.fill)
- Active route: Coastal Path (#7A9CAF) with rounded line caps (iOS style)
- Historical routes: Harbor Blue (#5C8AA8) dashed style with opacity
- Cluster: Blue Slate (#5C6B73) with white "5" text
- Bottom sheet: Card-style "Ferry Building" with blur background (Light Cyan 85%)
- No FAB - floating button group (semitransparent circles) top-right
- Apple Map controls: Native zoom controls, compass, 3D toggle

Pair 3: Trip Details
Label: "Trip Details"

Android (Left):
- Top App Bar: Back arrow, "Weekend Hike" title, share icon, overflow menu
- Hero image: Landscape photo from hike (Mt. Tamalpais vista), 16:9 aspect ratio
- Stats row: Material cards showing "8.7 mi" | "3h 20m" | "1,240 ft gain" with icons
- Section "Route Map": Embedded small map showing trail in Coastal Path (#7A9CAF)
- Section "Photos" (12): Horizontal scrolling grid, 3 visible thumbnails, Light Blue (#C2DFE3) cards
- Section "Places Visited":
  - List item: "Pantoll Trailhead" | "9:00 AM" | Dot marker
  - List item: "Summit" | "11:15 AM" | Sea Glass (#8BB5A1) success badge
  - List item: "Mountain Home Inn" | "12:30 PM" | Dot marker
- Section "Journal Entry": Card with text "Amazing views from the summit. Weather was perfect..." in italics
- FAB: Edit (pencil icon), Cool Steel (#9DB4C0)

iOS (Right):
- Navigation Bar: Large title "Weekend Hike", back button, share button (trailing)
- Hero image: Same landscape photo, full-width with safe area insets
- Stats: Three iOS-styled cards (Light Blue translucent) in horizontal stack
- Section "Route": Small map preview with Coastal Path route, tap to expand
- Section "Photos": Horizontal scroll with 12 photos, iOS photo grid style, 8pt spacing
- Section "Visited Places": iOS List inset grouped style
  - Row: "Pantoll Trailhead" | "9:00 AM" | chevron.right
  - Row: "Summit" | "11:15 AM" | checkmark.circle.fill (Sea Glass)
  - Row: "Mountain Home Inn" | "12:30 PM" | chevron.right
- Section "Notes": Text field with journal entry, Light Cyan (#E0FBFC) background
- Toolbar: Bottom with edit button (Cool Steel tint)

Pair 4: Activity Statistics
Label: "Activity Stats"

Android (Left):
- Top App Bar: "Statistics" title, date range picker "Last 30 Days", filter icon
- Card 1 "Overview": Three Material stat chips in row
  - "127 km traveled" | route icon
  - "18 places visited" | location icon
  - "23 trips" | bag icon
  - All using Blue Slate (#5C6B73) backgrounds
- Card 2 "Activity Heatmap": Calendar-style heatmap (7×5 grid for weeks/days)
  - Gradient: Light Cyan → Light Blue → Cool Steel → Coastal Path → Blue Slate
  - Legend: "Low activity" to "High activity"
  - Today highlighted with Cool Steel outline
- Card 3 "Top Places": Horizontal bar chart
  - "Golden Gate Park" | Cool Steel (#9DB4C0) bar | "8 visits"
  - "Ferry Building" | Blue Slate (#5C6B73) bar | "6 visits"
  - "Dolores Park" | Coastal Path (#7A9CAF) bar | "5 visits"
- Card 4 "Transport Modes": Donut chart
  - Walking (Sea Glass #8BB5A1) 45%
  - Driving (Blue Slate #5C6B73) 30%
  - Cycling (Coastal Path #7A9CAF) 15%
  - Transit (Misty Lavender #A8B5C7) 10%

iOS (Right):
- Navigation Bar: Large title "Statistics", date picker "Last 30 Days" (trailing)
- Card 1 "Summary": Three iOS stat cards (Light Blue translucent)
  - Same metrics with SF Symbols (figure.walk, mappin.circle, bag.fill)
  - SF Pro Rounded numbers
- Card 2 "Activity Calendar": iOS-styled calendar heatmap
  - Same gradient scale adapted to iOS style
  - Rounded squares (8pt radius) with spacing
  - Today: Cool Steel ring around cell
  - Tap states visible with haptic indication
- Card 3 "Favorite Places": iOS list with horizontal bars
  - Each row: place name | bar visualization | visit count
  - Bars: Cool Steel, Blue Slate, Coastal Path colors
  - chevron.right for detail navigation
- Card 4 "How You Travel": Ring chart (iOS Charts framework style)
  - Same percentages and colors as Android
  - Center: Total trips count
  - Legend below with colored dots and SF Pro text

COMPOSITION AND STYLE:

Device Arrangement:
- 8 devices total in single horizontal row
- Grouping: [Android-iOS] spacing [Android-iOS] spacing [Android-iOS] spacing [Android-iOS]
- Inter-pair gap: 120px
- Intra-pair gap: 60px (Android and iOS closely paired)
- Slight 3D tilt: 6 degrees for depth, all devices same angle

Screen Labels:
- Positioned ABOVE each device pair
- Text: "Timeline View", "Map View", "Trip Details", "Activity Stats"
- Font: SF Pro Display Semibold 18pt
- Color: Blue Slate (#5C6B73)
- Platform indicators below: "Android | iOS" in smaller caption text

Platform Badges:
- Small badge on each device frame
- Android: Material symbol with "Material 3" text, Cool Steel background
- iOS: Apple logo with "iOS 17" text, Blue Slate background
- Positioned: Top-left corner of device frame

Visual Consistency:
- Same trip data across corresponding screens
- Matching scroll positions where applicable
- Synchronized map viewport coordinates
- Identical photo thumbnails
- Consistent user avatar/profile
- Same completion states (badges, checkmarks)

Professional Mockup Effects:
- Soft drop shadow: 0px 20px 60px rgba(92, 107, 115, 0.15) beneath each device
- Subtle reflection: 10% opacity beneath devices fading down
- Edge highlight: Thin white highlight on device edges for realism
- Screen glare: Very subtle gradient overlay (5% opacity) suggesting glass
- Perfect screen integration: No bezels between frame and content

State Indicators:
- Show active states where relevant (toggled switches, selected tabs)
- Loading states avoided - show filled, complete data
- Timestamps: Recent (Today, Yesterday) for immediacy
- Battery/signal: Full bars, high battery (90%+) for polish
- No notification badges or clutter

Platform-Specific Visual Differences:
- Android: Sharper shadows, more geometric elevation
- iOS: Softer blur, translucent layers visible
- Android: Ripple effect hints on interactive elements
- iOS: Subtle depth and layering visible
- Different but equivalent information architecture

OUTPUT FORMAT:
Single ultra-high-resolution horizontal image (minimum 7680×2160 pixels), professional presentation quality suitable for:
- GitHub repository header/documentation
- Investor pitch deck showcasing cross-platform capability
- App Store/Play Store marketing materials
- Design portfolio demonstrating KMP expertise
- Technical documentation showing platform parity

Photorealistic device rendering with:
- Pixel-perfect screen integration
- Accurate device proportions (Pixel 8 Pro and iPhone 15 Pro Max)
- Realistic materials (glass, aluminum/titanium frames)
- Professional lighting and shadows
- Clean, distraction-free composition
- Clearly demonstrates functional parity while respecting Material 3 Expressive (Android) and native iOS/Liquid Glass design systems
- "Silent Waters" color palette evident and consistent across all 8 screens
```

---

## Customization Tips

### Changing the User Journey

Replace the 4 screen pairs with different flows:

**Onboarding Flow**:
1. Welcome screen
2. Location permissions
3. First trip setup
4. Success/ready state

**Photo Feature Flow**:
1. Photo gallery grid
2. Photo attachment to trip
3. Photo detail view
4. Photo map visualization

**Settings Flow**:
1. Main settings list
2. Algorithm settings
3. Privacy settings
4. About/device management

### Adjusting Visual Intensity

If colors appear washed out, add to the prompt:
```
ADDITIONAL VISUAL REQUIREMENTS:
- High saturation and contrast for all UI elements
- Crisp, anti-aliased typography with proper rendering
- Vibrant color accuracy matching hex codes exactly
- No color desaturation or washing out
```

### Layout Variations

**Vertical Comparison** (2 rows instead of horizontal):
```
Layout Configuration:
- Arrange in 2 rows of 4 screens each
- Top row: Android (Pixel 8 Pro) - 4 journey steps
- Bottom row: iOS (iPhone 15 Pro Max) - same 4 journey steps
- Vertical alignment showing same screen function
```

**Single Platform Focus** (4 screens, one platform):
```
showing exactly 4 iPhone screens arranged horizontally
[Remove all Android specifications]
```

## Iteration Strategy

1. **First generation**: Use prompt as-is
2. **If spacing is tight**: Increase inter-pair gap to 150px
3. **If text is blurry**: Add "ultra-sharp typography with 300dpi clarity"
4. **If colors don't match**: Add "exact hex color matching with no variation"
5. **If devices look fake**: Add "photorealistic device rendering with real materials"

## Related Files

- [color-scheme-reference.md](./color-scheme-reference.md) - Complete color specifications
- [prompt-usage-guide.md](./prompt-usage-guide.md) - Detailed usage instructions
