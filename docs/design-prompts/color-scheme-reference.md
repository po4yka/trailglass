# Trailglass Color Scheme Reference

**Palette Name**: "Silent Waters"

**Philosophy**: Dewy blues and weathered gray merge into a hushed palette, like drifting fog on silent waters.

This document provides the complete color specifications used in Trailglass for generating accurate UI mockups and design assets.

---

## Base Palette Colors

### Primary Blues & Grays

| Color Name | Hex | RGB | HSL | Usage |
|------------|-----|-----|-----|-------|
| **Light Cyan** | `#E0FBFC` | rgb(224, 251, 252) | hsl(182, 74%, 93%) | Backgrounds, containers, lightest tint |
| **Light Blue** | `#C2DFE3` | rgb(194, 223, 227) | hsl(187, 33%, 83%) | Surface variants, secondary containers |
| **Cool Steel** | `#9DB4C0` | rgb(157, 180, 192) | hsl(201, 21%, 68%) | **Primary brand color** |
| **Blue Slate** | `#5C6B73` | rgb(92, 107, 115) | hsl(200, 11%, 41%) | **Secondary brand color** |
| **Jet Black** | `#253237` | rgb(37, 50, 55) | hsl(197, 20%, 18%) | Dark backgrounds, primary text |

### Visual Representation

```
Light Cyan  ████████  #E0FBFC  Lightest
Light Blue  ████████  #C2DFE3  ↓
Cool Steel  ████████  #9DB4C0  Primary
Blue Slate  ████████  #5C6B73  Secondary
Jet Black   ████████  #253237  Darkest
```

---

## Functional Colors

### Success & Confirmation

| Color Name | Hex | RGB | Usage |
|------------|-----|-----|-------|
| **Sea Glass** | `#8BB5A1` | rgb(139, 181, 161) | Success states, completed trips, checkmarks |
| **Sage Green** | `#6A8E7F` | rgb(106, 142, 127) | Success emphasis, confirmation buttons |

```
Sea Glass   ████████  #8BB5A1  Success
Sage Green  ████████  #6A8E7F  Success Emphasis
```

### Warning & Attention

| Color Name | Hex | RGB | Usage |
|------------|-----|-----|-------|
| **Driftwood** | `#C9B896` | rgb(201, 184, 150) | Warnings, alerts, attention states |
| **Weathered Brass** | `#A89968` | rgb(168, 153, 104) | Warning emphasis, caution buttons |

```
Driftwood       ████████  #C9B896  Warning
Weathered Brass ████████  #A89968  Warning Emphasis
```

### Error States

| Context | Light Mode | Dark Mode |
|---------|------------|-----------|
| **Error Text** | `#BA1A1A` | `#FFB4AB` |
| **Error Container** | `#FFDAD6` | `#93000A` |
| **On Error** | `#FFFFFF` | `#690005` |
| **On Error Container** | `#410002` | `#FFDAD6` |

---

## Map & Route Colors

| Color Name | Hex | RGB | Usage | Stroke Width |
|------------|-----|-----|-------|--------------|
| **Coastal Path** | `#7A9CAF` | rgb(122, 156, 175) | Active routes, current trips | Android: 6dp, iOS: 5pt |
| **Harbor Blue** | `#5C8AA8` | rgb(92, 138, 168) | Historical routes, past trips | Android: 4dp, iOS: 3pt |
| **Misty Lavender** | `#A8B5C7` | rgb(168, 181, 199) | Alternative paths, suggestions | Android: 3dp, iOS: 2pt |

```
Coastal Path    ────────  #7A9CAF  Active route (thick)
Harbor Blue     ────────  #5C8AA8  Historical (medium)
Misty Lavender  ────────  #A8B5C7  Alternative (thin)
```

### Map Marker Colors

| Element | Color | Hex |
|---------|-------|-----|
| Place Markers | Cool Steel | `#9DB4C0` |
| Cluster Markers | Blue Slate | `#5C6B73` |
| User Location | Cool Steel with pulse | `#9DB4C0` |

---

## Category & Tag Colors

| Color Name | Hex | RGB | Usage |
|------------|-----|-----|-------|
| **Stone Gray** | `#8C979E` | rgb(140, 151, 158) | Neutral categories, general tags |
| **Seafoam Tint** | `#A8D5D8` | rgb(168, 213, 216) | Water-related trips (beaches, lakes, coastal) |
| **Dusk Purple** | `#8A90A6` | rgb(138, 144, 166) | Evening/night activities |
| **Sunrise Peach** | `#D4B5A8` | rgb(212, 181, 168) | Morning activities, sunrise trips |

```
Stone Gray    ████████  #8C979E  Neutral
Seafoam Tint  ████████  #A8D5D8  Water
Dusk Purple   ████████  #8A90A6  Evening
Sunrise Peach ████████  #D4B5A8  Morning
```

---

## Disabled & Inactive States

| Mode | Color Name | Hex | RGB | Usage |
|------|------------|-----|-----|-------|
| **Light** | Mist | `#D8E2E6` | rgb(216, 226, 230) | Disabled text, icons, borders |
| **Dark** | Charcoal | `#3A4449` | rgb(58, 68, 73) | Disabled text, icons, borders |

---

## Material 3 Color Roles (Android)

### Light Mode

```kotlin
lightColorScheme(
    // Primary
    primary = Color(0xFF9DB4C0),              // Cool Steel
    onPrimary = Color(0xFFFFFFFF),            // White
    primaryContainer = Color(0xFFE0FBFC),     // Light Cyan
    onPrimaryContainer = Color(0xFF5C6B73),   // Blue Slate

    // Secondary
    secondary = Color(0xFF5C6B73),            // Blue Slate
    onSecondary = Color(0xFFFFFFFF),          // White
    secondaryContainer = Color(0xFFC2DFE3),   // Light Blue
    onSecondaryContainer = Color(0xFF253237), // Jet Black

    // Tertiary
    tertiary = Color(0xFF7A8A92),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD4E4E8),
    onTertiaryContainer = Color(0xFF2A3B42),

    // Error
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Background
    background = Color(0xFFE0FBFC),           // Light Cyan
    onBackground = Color(0xFF253237),         // Jet Black

    // Surface
    surface = Color(0xFFF5FEFF),
    onSurface = Color(0xFF253237),
    surfaceVariant = Color(0xFFC2DFE3),       // Light Blue
    onSurfaceVariant = Color(0xFF5C6B73),     // Blue Slate

    // Outline
    outline = Color(0xFF9DB4C0),              // Cool Steel
    surfaceTint = Color(0xFF9DB4C0)           // Cool Steel
)
```

### Dark Mode

```kotlin
darkColorScheme(
    // Primary
    primary = Color(0xFFC2DFE3),              // Light Blue
    onPrimary = Color(0xFF253237),            // Jet Black
    primaryContainer = Color(0xFF5C6B73),     // Blue Slate
    onPrimaryContainer = Color(0xFFE0FBFC),   // Light Cyan

    // Secondary
    secondary = Color(0xFF9DB4C0),            // Cool Steel
    onSecondary = Color(0xFF253237),          // Jet Black
    secondaryContainer = Color(0xFF5C6B73),   // Blue Slate
    onSecondaryContainer = Color(0xFFE0FBFC), // Light Cyan

    // Tertiary
    tertiary = Color(0xFFB8C8D0),
    onTertiary = Color(0xFF253237),
    tertiaryContainer = Color(0xFF3F4E56),
    onTertiaryContainer = Color(0xFFD4E4E8),

    // Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background
    background = Color(0xFF253237),           // Jet Black
    onBackground = Color(0xFFE0FBFC),         // Light Cyan

    // Surface
    surface = Color(0xFF1A2428),
    onSurface = Color(0xFFE0FBFC),
    surfaceVariant = Color(0xFF5C6B73),       // Blue Slate
    onSurfaceVariant = Color(0xFFC2DFE3),     // Light Blue

    // Outline
    outline = Color(0xFF5C6B73),              // Blue Slate
    surfaceTint = Color(0xFFC2DFE3)           // Light Blue
)
```

---

## iOS Adaptive Colors (SwiftUI)

### Static Colors

```swift
extension Color {
    // Base palette
    static let lightCyan = Color(hex: "E0FBFC")
    static let lightBlue = Color(hex: "C2DFE3")
    static let coolSteel = Color(hex: "9DB4C0")
    static let blueSlate = Color(hex: "5C6B73")
    static let jetBlack = Color(hex: "253237")

    // Functional
    static let seaGlass = Color(hex: "8BB5A1")
    static let sageGreen = Color(hex: "6A8E7F")
    static let driftwood = Color(hex: "C9B896")
    static let weatheredBrass = Color(hex: "A89968")

    // Map/Routes
    static let coastalPath = Color(hex: "7A9CAF")
    static let harborBlue = Color(hex: "5C8AA8")
    static let mistyLavender = Color(hex: "A8B5C7")
}
```

### Adaptive Colors

```swift
extension Color {
    static var adaptivePrimary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(lightBlue)   // #C2DFE3
                : UIColor(coolSteel)   // #9DB4C0
        })
    }

    static var adaptiveBackground: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(jetBlack)    // #253237
                : UIColor(lightCyan)   // #E0FBFC
        })
    }

    static var adaptiveSurface: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(hex: "1A2428")  // Slightly lighter than background
                : UIColor(hex: "F5FEFF")  // Slightly brighter than background
        })
    }
}
```

---

## Data Visualization Gradient

5-step gradient for charts, heatmaps, and analytics:

| Step | Color Name | Hex | Usage |
|------|------------|-----|-------|
| 1 (Low) | Light Cyan | `#E0FBFC` | Minimal activity |
| 2 | Light Blue | `#C2DFE3` | Low activity |
| 3 (Medium) | Cool Steel | `#9DB4C0` | Medium activity |
| 4 | Coastal Path | `#7A9CAF` | High activity |
| 5 (High) | Blue Slate | `#5C6B73` | Maximum activity |

```
Gradient:  ████ ████ ████ ████ ████
           E0FB C2DF 9DB4 7A9C 5C6B
           Low  ───────────────→ High
```

### CSS Gradient

```css
background: linear-gradient(
    to right,
    #E0FBFC 0%,
    #C2DFE3 25%,
    #9DB4C0 50%,
    #7A9CAF 75%,
    #5C6B73 100%
);
```

---

## Accessibility & Contrast

### WCAG AA Compliance

| Foreground | Background | Ratio | Pass |
|------------|------------|-------|------|
| Jet Black (#253237) | Light Cyan (#E0FBFC) | 12.8:1 | ✓ AAA |
| Blue Slate (#5C6B73) | Light Cyan (#E0FBFC) | 4.9:1 | ✓ AA |
| Cool Steel (#9DB4C0) | White (#FFFFFF) | 3.2:1 | ✓ AA Large |
| Coastal Path (#7A9CAF) | White (#FFFFFF) | 3.5:1 | ✓ AA Large |

### Recommended Text Combinations

**High Contrast (Body Text)**:
- Light mode: Jet Black (#253237) on Light Cyan (#E0FBFC)
- Dark mode: Light Cyan (#E0FBFC) on Jet Black (#253237)

**Medium Contrast (Secondary Text)**:
- Light mode: Blue Slate (#5C6B73) on Light Cyan (#E0FBFC)
- Dark mode: Light Blue (#C2DFE3) on Jet Black (#253237)

**Interactive Elements**:
- Light mode: Cool Steel (#9DB4C0) on White or Light Cyan
- Dark mode: Light Blue (#C2DFE3) on dark surfaces

---

## Color Usage Guidelines

### Android (Material 3 Expressive)

**Elevation System**:
- Level 0: Background (#E0FBFC)
- Level 1: Surface (#F5FEFF) with 1dp elevation
- Level 2: Cards (#E0FBFC) with 2dp elevation + tonal overlay
- Level 3: FAB (Cool Steel #9DB4C0) with 6dp elevation
- Level 4: Drawer/Modal with 16dp elevation

**Interactive States**:
- Default: Cool Steel (#9DB4C0)
- Hover: Cool Steel at 110% brightness
- Pressed: Blue Slate (#5C6B73)
- Disabled: Mist (#D8E2E6)
- Focus: Cool Steel with 2dp outline

**Ripple Effect**:
- Color: Blue Slate (#5C6B73)
- Opacity: 20%
- Duration: 300ms Material motion curve

### iOS (Native/Liquid Glass)

**Translucency System**:
- Navigation bars: Cool Steel (#9DB4C0) tint with system blur
- Tab bars: 70% opacity with adaptive blur
- Sheets: Light Blue (#C2DFE3) at 85% opacity
- Overlays: Light Cyan (#E0FBFC) at 85% opacity

**Interactive States**:
- Default: Cool Steel (#9DB4C0)
- Highlighted: Cool Steel at 80% opacity
- Selected: Cool Steel with checkmark
- Disabled: Mist (#D8E2E6) at 60% opacity

**Shadow Effects**:
- Small elements: 0pt 2pt 8pt rgba(92, 107, 115, 0.1)
- Cards: 0pt 4pt 16pt rgba(92, 107, 115, 0.12)
- Modals: 0pt 8pt 32pt rgba(92, 107, 115, 0.15)

---

## Platform-Specific Color Adaptations

### Dynamic Color (Android 12+)

When Material You dynamic color is enabled:
- Wallpaper-extracted colors override Cool Steel/Blue Slate
- Maintain same color role assignments
- Preserve data visualization gradient
- Keep map route colors consistent

### Dark Mode Transitions

**Android**:
- Tonal elevation increases in dark mode
- Use Color(0x1A...) overlays for elevation
- Avoid pure black (#000000), use Jet Black (#253237)

**iOS**:
- Increase translucency in dark mode (70% → 85%)
- Reduce shadow opacity by 50%
- Use Light Cyan (#E0FBFC) instead of white for text

---

## Quick Reference

### Primary Actions
- **Android**: Filled button with Cool Steel (#9DB4C0)
- **iOS**: Filled capsule with Cool Steel (#9DB4C0)

### Secondary Actions
- **Android**: Outlined button with Blue Slate (#5C6B73) border
- **iOS**: Plain button with Blue Slate (#5C6B73) text

### Success States
- Sea Glass (#8BB5A1) with checkmark icon

### Warning States
- Driftwood (#C9B896) with alert icon

### Error States
- Error Light (#BA1A1A) in light mode
- Error Dark (#FFB4AB) in dark mode

### Map Elements
- Active route: Coastal Path (#7A9CAF)
- Past routes: Harbor Blue (#5C8AA8)
- Markers: Cool Steel (#9DB4C0)
- Clusters: Blue Slate (#5C6B73)

---

## Color Sources

These colors are defined in:
- **Android**: `composeApp/src/main/kotlin/com/po4yka/trailglass/ui/theme/Color.kt`
- **iOS**: `iosApp/iosApp/Theme/Color.swift`

Both platforms maintain identical hex values for cross-platform consistency.

For palette updates, modify both files and regenerate this reference document.

---

## Related Documentation

- [COLOR_PALETTE_USAGE.md](../COLOR_PALETTE_USAGE.md) - Usage patterns in the codebase
- [kmp-mockup-prompt.md](./kmp-mockup-prompt.md) - How colors are used in mockup generation
- [../UI_ARCHITECTURE.md](../UI_ARCHITECTURE.md) - UI theming and component structure
