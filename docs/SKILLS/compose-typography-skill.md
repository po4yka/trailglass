# Compose Typography Skill

**Purpose:** Guide AI assistants to create distinctive typography systems for Jetpack Compose that avoid generic defaults and establish strong visual hierarchy.

## The Problem

Default Compose typography uses `FontFamily.Default` (Roboto), which creates generic, forgettable interfaces. Without guidance, AI models will default to this safe choice.

## Typography Strategy for TrailGlass

### Font Selection Principles

**Display/Headline Fonts (Large, Bold, Distinctive):**
- **Playfair Display** - Elegant serif, perfect for travel/adventure themes
- **Crimson Pro** - Editorial serif with character
- **Bricolage Grotesque** - Modern sans-serif with personality
- **Newsreader** - Readable serif for longer content

**Body Fonts (Readable, Versatile):**
- **Source Sans 3** - Clean, professional sans-serif
- **Inter** - Only if paired with distinctive display font
- **IBM Plex Sans** - Technical, trustworthy feel

**Monospace Fonts (Data, Stats, Code):**
- **JetBrains Mono** - Modern monospace, excellent readability
- **Fira Code** - Programming-inspired, great for stats

### Font Pairing Strategy

**High Contrast Pairings:**
- Serif Display + Sans-serif Body (e.g., Playfair Display + Source Sans 3)
- Geometric Sans Display + Humanist Sans Body (e.g., Bricolage Grotesque + Inter)
- Display Font + Monospace (for stats/data)

**Weight Extremes:**
- Use 100/200 for light, elegant text
- Use 700/800/900 for bold, impactful headlines
- Avoid middle weights (400/500/600) for display text

**Size Jumps:**
- Use 3x+ size differences between display and body (e.g., 48sp display, 16sp body)
- Avoid 1.5x differences (too subtle)

## Compose Implementation

### Step 1: Add Font Resources

Add fonts to `composeApp/src/androidMain/res/font/`:
- `playfair_display_regular.ttf`
- `playfair_display_bold.ttf`
- `source_sans_3_regular.ttf`
- `source_sans_3_semibold.ttf`
- `jetbrains_mono_regular.ttf`
- `jetbrains_mono_bold.ttf`

### Step 2: Define Font Families

```kotlin
package com.po4yka.trailglass.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.po4yka.trailglass.R

// Display font for headlines and large text
val DisplayFont = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

// Body font for readable content
val BodyFont = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold)
)

// Monospace font for stats and data
val MonospaceFont = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)
```

### Step 3: Create Typography System

```kotlin
val Typography = Typography(
    // Display styles - use distinctive font
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles - use distinctive font
    headlineLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - use body font with medium weight
    titleLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - use readable body font
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - use body font
    labelLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Step 4: Custom Text Styles for Stats

```kotlin
// Custom text style for statistics (monospace)
val StatNumberStyle = TextStyle(
    fontFamily = MonospaceFont,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
)

val StatLabelStyle = TextStyle(
    fontFamily = BodyFont,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
```

## Usage Examples

### Good: Distinctive Typography

```kotlin
@Composable
fun TripHeader(trip: Trip) {
    Column {
        // Display font for headline
        Text(
            text = trip.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Body font for description
        Text(
            text = "${trip.duration} days • ${trip.placeCount} places",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatCard(value: String, label: String) {
    Column {
        // Monospace for numbers
        Text(
            text = value,
            style = StatNumberStyle,
            color = MaterialTheme.colorScheme.primary
        )
        // Body font for label
        Text(
            text = label,
            style = StatLabelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Bad: Generic Typography

```kotlin
// Don't do this - generic default font
Text(
    text = trip.name,
    style = MaterialTheme.typography.bodyLarge // Default font
)
```

## Prompt Template

When generating typography for Compose:

```
<compose_typography>
For TrailGlass Compose UI, use distinctive typography:

Display/Headline: Use Playfair Display, Crimson Pro, or Bricolage Grotesque
- Never use FontFamily.Default for display text
- Use bold weights (700/800/900)
- Large sizes (24sp-57sp)

Body: Use Source Sans 3 or Inter (only if paired with distinctive display)
- Normal weight (400)
- Readable sizes (12sp-16sp)

Stats/Data: Use JetBrains Mono or Fira Code
- Monospace for numbers and data
- Bold for emphasis

Font Pairing: High contrast
- Serif display + Sans-serif body
- Display font + Monospace for stats

Weight Extremes: Use 100/200 for light, 700/800/900 for bold
Size Jumps: 3x+ differences between display and body

Never use FontFamily.Default for display/headline text.
</compose_typography>
```

## Resources

- [Google Fonts](https://fonts.google.com/) - Source for fonts
- [Material 3 Typography](https://m3.material.io/styles/typography/overview) - Typography scale
- [Compose Typography](https://developer.android.com/jetpack/compose/text#typography) - Compose documentation

---

**Last Updated:** 2025-11-18
**Version:** 1.0
**Related:** `compose-ui-design-skill.md`

