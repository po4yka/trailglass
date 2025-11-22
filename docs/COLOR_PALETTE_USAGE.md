# Silent Waters Color Palette Usage Guide

This document describes how to use the Silent Waters color palette in the Trailglass app.

## Overview

The Silent Waters palette provides a serene, contemplative aesthetic with dewy blues and weathered grays, perfect for a travel logging app. The palette includes:

- 5 base colors
- Success/confirmation colors
- Warning/attention colors
- Map/route colors
- Category/tag colors
- Disabled state colors
- Data visualization gradient

## Android (Jetpack Compose)

### Material Theme Colors

Standard Material 3 colors are automatically applied through `MaterialTheme.colorScheme`:

```kotlin
// Primary colors
MaterialTheme.colorScheme.primary          // Cool Steel (light) / Light Blue (dark)
MaterialTheme.colorScheme.primaryContainer // Light Cyan (light) / Blue Slate (dark)

// Secondary colors
MaterialTheme.colorScheme.secondary        // Blue Slate (light) / Cool Steel (dark)

// Background & Surface
MaterialTheme.colorScheme.background       // Light Cyan (light) / Jet Black (dark)
MaterialTheme.colorScheme.surface          // Slightly brighter than background
```

### Extended Colors

Access extended colors through the `extended` property:

```kotlin
import com.po4yka.trailglass.ui.theme.extended

// Success states
MaterialTheme.colorScheme.extended.success
MaterialTheme.colorScheme.extended.successEmphasis

// Warning states
MaterialTheme.colorScheme.extended.warning
MaterialTheme.colorScheme.extended.warningEmphasis

// Disabled states
MaterialTheme.colorScheme.extended.disabled

// Route colors
MaterialTheme.colorScheme.extended.activeRoute
MaterialTheme.colorScheme.extended.historicalRoute
MaterialTheme.colorScheme.extended.alternativeRoute

// Category colors
MaterialTheme.colorScheme.extended.neutralCategory
MaterialTheme.colorScheme.extended.waterCategory
MaterialTheme.colorScheme.extended.eveningCategory
MaterialTheme.colorScheme.extended.morningCategory

// Data visualization gradient
MaterialTheme.colorScheme.extended.gradientColors
```

### Direct Palette Access

You can also access colors directly:

```kotlin
import com.po4yka.trailglass.ui.theme.*

// Base colors
LightCyan
LightBlue
CoolSteel
BlueSlate
JetBlack

// Success
SeaGlass
SageGreen

// Warning
Driftwood
WeatheredBrass

// Routes
CoastalPath
HarborBlue
MistyLavender

// Categories
StoneGray
SeafoamTint
DuskPurple
SunrisePeach
```

## iOS (SwiftUI)

### Semantic Colors

Use adaptive colors that automatically adjust for light/dark mode:

```swift
import SwiftUI

// Primary colors
Color.adaptivePrimary
Color.adaptiveBackground
Color.adaptiveSurface
Color.adaptiveSecondary

// Success states
Color.adaptiveSuccess        // Sea Glass (light) / Sage Green (dark)

// Warning states
Color.adaptiveWarning        // Driftwood (light) / Weathered Brass (dark)

// Disabled states
Color.adaptiveDisabled       // Mist (light) / Charcoal (dark)

// Route colors
Color.adaptiveActiveRoute
Color.adaptiveHistoricalRoute
```

### Semantic Helpers

Direct semantic access:

```swift
// Success
Color.success                // Sea Glass
Color.successEmphasis        // Sage Green

// Warning
Color.warning                // Driftwood
Color.warningEmphasis        // Weathered Brass

// Disabled
Color.disabled               // Mist (light mode)
Color.disabledDark           // Charcoal (dark mode)

// Categories
Color.neutralCategory        // Stone Gray
Color.waterCategory          // Seafoam Tint
Color.eveningCategory        // Dusk Purple
Color.morningCategory        // Sunrise Peach

// Routes
Color.activeRoute            // Coastal Path
Color.historicalRoute        // Harbor Blue
Color.alternativeRoute       // Misty Lavender
```

### Direct Palette Access

```swift
// Base colors
Color.lightCyan
Color.lightBlue
Color.coolSteel
Color.blueSlate
Color.jetBlack

// Success
Color.seaGlass
Color.sageGreen

// Warning
Color.driftwood
Color.weatheredBrass

// Routes
Color.coastalPath
Color.harborBlue
Color.mistyLavender

// Categories
Color.stoneGray
Color.seafoamTint
Color.duskPurple
Color.sunrisePeach

// Gradients
Color.gradientStart
Color.gradientStep1
Color.gradientStep2
Color.gradientStep3
Color.gradientEnd
```

## Common Use Cases

### Success Indicators

**Android:**
```kotlin
Icon(
    imageVector = Icons.Default.CheckCircle,
    contentDescription = "Success",
    tint = MaterialTheme.colorScheme.extended.success
)
```

**iOS:**
```swift
Image(systemName: "checkmark.circle.fill")
    .foregroundColor(.success)
```

### Warning Banners

**Android:**
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.extended.warning
    )
) {
    Text(
        "Low battery during tracking",
        color = MaterialTheme.colorScheme.extended.onWarning
    )
}
```

**iOS:**
```swift
Text("Low battery during tracking")
    .padding()
    .background(Color.warning)
    .foregroundColor(.black)
    .cornerRadius(8)
```

### Route Visualization

**Android:**
```kotlin
Canvas(modifier = Modifier.fillMaxSize()) {
    // Active route
    drawPath(
        path = currentRoute,
        color = MaterialTheme.colorScheme.extended.activeRoute,
        style = Stroke(width = 4.dp.toPx())
    )

    // Historical routes
    drawPath(
        path = historicalRoute,
        color = MaterialTheme.colorScheme.extended.historicalRoute,
        style = Stroke(width = 2.dp.toPx())
    )
}
```

**iOS:**
```swift
Path { path in
    // Current route
    path.move(to: startPoint)
    path.addLine(to: endPoint)
}
.stroke(Color.activeRoute, lineWidth: 4)

Path { path in
    // Historical route
    path.move(to: startPoint)
    path.addLine(to: endPoint)
}
.stroke(Color.historicalRoute, lineWidth: 2)
```

### Category Tags

**Android:**
```kotlin
@Composable
fun CategoryChip(category: TripCategory) {
    val categoryColor = when (category) {
        TripCategory.WATER -> MaterialTheme.colorScheme.extended.waterCategory
        TripCategory.EVENING -> MaterialTheme.colorScheme.extended.eveningCategory
        TripCategory.MORNING -> MaterialTheme.colorScheme.extended.morningCategory
        else -> MaterialTheme.colorScheme.extended.neutralCategory
    }

    AssistChip(
        onClick = { /* ... */ },
        label = { Text(category.name) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = categoryColor
        )
    )
}
```

**iOS:**
```swift
func categoryColor(for category: TripCategory) -> Color {
    switch category {
    case .water: return .waterCategory
    case .evening: return .eveningCategory
    case .morning: return .morningCategory
    default: return .neutralCategory
    }
}

Text(category.name)
    .padding(.horizontal, 12)
    .padding(.vertical, 6)
    .background(categoryColor(for: category))
    .cornerRadius(16)
```

### Data Visualization

**Android:**
```kotlin
@Composable
fun HeatMap() {
    val gradientColors = MaterialTheme.colorScheme.extended.gradientColors

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gradient = Brush.verticalGradient(gradientColors)
        drawRect(brush = gradient)
    }
}
```

**iOS:**
```swift
Rectangle()
    .fill(
        LinearGradient(
            colors: [
                .gradientStart,
                .gradientStep1,
                .gradientStep2,
                .gradientStep3,
                .gradientEnd
            ],
            startPoint: .top,
            endPoint: .bottom
        )
    )
```

### Disabled States

**Android:**
```kotlin
Button(
    onClick = { /* ... */ },
    enabled = isEnabled,
    colors = ButtonDefaults.buttonColors(
        disabledContainerColor = MaterialTheme.colorScheme.extended.disabled,
        disabledContentColor = MaterialTheme.colorScheme.extended.onDisabled
    )
) {
    Text("Start Tracking")
}
```

**iOS:**
```swift
Button("Start Tracking") {
    // Action
}
.disabled(!isEnabled)
.foregroundColor(isEnabled ? .primary : .adaptiveDisabled)
```

## Color Meanings

### Base Palette
- **Light Cyan (E0FBFC)**: Tranquility, refreshment, clear skies
- **Light Blue (C2DFE3)**: Purity, trust, open spaces
- **Cool Steel (9DB4C0)**: Contemplation, serene focus
- **Blue Slate (5C6B73)**: Trustworthy depth, creative clarity
- **Jet Black (253237)**: Mystery, strength, drama

### Success Colors
- **Sea Glass (8BB5A1)**: Confirmation, positive completion
- **Sage Green (6A8E7F)**: Deeper success, emphasis

### Warning Colors
- **Driftwood (C9B896)**: Gentle warnings, important information
- **Weathered Brass (A89968)**: Stronger attention needed

### Route Colors
- **Coastal Path (7A9CAF)**: Current/active journey
- **Harbor Blue (5C8AA8)**: Past journeys, history
- **Misty Lavender (A8B5C7)**: Suggestions, alternatives

### Category Colors
- **Stone Gray (8C979E)**: Neutral, general purpose
- **Seafoam Tint (A8D5D8)**: Water-related activities
- **Dusk Purple (8A90A6)**: Evening/night activities
- **Sunrise Peach (D4B5A8)**: Morning/dawn activities

## Accessibility Notes

- All color combinations meet WCAG AA contrast requirements
- Success and warning colors are distinguishable even for colorblind users
- Disabled states have sufficient contrast with backgrounds
- Route colors remain distinct when overlapping on maps
