# Compose Theme & Color Skill

**Purpose:** Guide AI assistants to create cohesive, brand-appropriate color systems for TrailGlass using Material Design 3's semantic color system.

## TrailGlass Brand Colors

### Primary Colors (Teal/Cyan)
**Purpose:** Maps, location, travel, navigation

**Light Theme:**
- Primary: `#006A6A` (Deep teal)
- On Primary: `#FFFFFF` (White)
- Primary Container: `#9DF0F0` (Light teal)
- On Primary Container: `#002020` (Dark teal)

**Dark Theme:**
- Primary: `#80D3D3` (Light teal)
- On Primary: `#003737` (Dark teal)
- Primary Container: `#004F4F` (Medium teal)
- On Primary Container: `#9DF0F0` (Light teal)

### Secondary Colors (Warm Orange)
**Purpose:** Photos, memories, highlights, special moments

**Light Theme:**
- Secondary: `#845400` (Deep orange)
- On Secondary: `#FFFFFF` (White)
- Secondary Container: `#FFDDB5` (Light orange)
- On Secondary Container: `#2A1800` (Dark brown)

**Dark Theme:**
- Secondary: `#FFB960` (Light orange)
- On Secondary: `#462A00` (Dark brown)
- Secondary Container: `#653F00` (Medium orange)
- On Secondary Container: `#FFDDB5` (Light orange)

### Tertiary Colors (Purple)
**Purpose:** Special events, achievements, premium features

**Light Theme:**
- Tertiary: `#6B5778` (Muted purple)
- On Tertiary: `#FFFFFF` (White)
- Tertiary Container: `#F3DAFF` (Light purple)
- On Tertiary Container: `#241432` (Dark purple)

**Dark Theme:**
- Tertiary: `#D6BEE4` (Light purple)
- On Tertiary: `#3B2948` (Dark purple)
- Tertiary Container: `#52405F` (Medium purple)
- On Tertiary Container: `#F3DAFF` (Light purple)

## Color Usage Guidelines

### Semantic Color Mapping

**Location/Map Related:**
- Use `primary` and `primaryContainer` for location markers, map controls, navigation
- Example: Map pins, location cards, route lines

**Photo/Memory Related:**
- Use `secondary` and `secondaryContainer` for photo galleries, memory highlights
- Example: Photo thumbnails, memory cards, highlight badges

**Special Events:**
- Use `tertiary` and `tertiaryContainer` for achievements, special trips, premium features
- Example: Achievement badges, premium indicators, special event markers

**Neutral/Content:**
- Use `surface`, `surfaceVariant`, `background` for cards, containers, backgrounds
- Use `onSurface`, `onSurfaceVariant`, `onBackground` for text on surfaces

### Visual Hierarchy Through Color

**Dominant Colors:**
- Use primary/secondary/tertiary for key actions and important elements
- Create contrast with sharp accents, not timid evenly-distributed palettes

**Accent Colors:**
- Use container colors for subtle backgrounds
- Use on-container colors for text on colored backgrounds

**Avoid:**
- Generic purple gradients on white backgrounds
- Overusing primary color throughout the UI
- Low-contrast color schemes that reduce readability

## Material 3 Color System Implementation

### Color Scheme Definition

```kotlin
package com.po4yka.trailglass.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light color scheme
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9DF0F0),
    onPrimaryContainer = Color(0xFF002020),

    secondary = Color(0xFF845400),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDB5),
    onSecondaryContainer = Color(0xFF2A1800),

    tertiary = Color(0xFF6B5778),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3DAFF),
    onTertiaryContainer = Color(0xFF241432),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFAFDFC),
    onBackground = Color(0xFF191C1C),

    surface = Color(0xFFFAFDFC),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E4),
    onSurfaceVariant = Color(0xFF3F4948),

    outline = Color(0xFF6F7978),
    surfaceTint = Color(0xFF006A6A)
)

// Dark color scheme
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80D3D3),
    onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F),
    onPrimaryContainer = Color(0xFF9DF0F0),

    secondary = Color(0xFFFFB960),
    onSecondary = Color(0xFF462A00),
    secondaryContainer = Color(0xFF653F00),
    onSecondaryContainer = Color(0xFFFFDDB5),

    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFF52405F),
    onTertiaryContainer = Color(0xFFF3DAFF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF191C1C),
    onBackground = Color(0xFFE0E3E2),

    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E2),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C8),

    outline = Color(0xFF899392),
    surfaceTint = Color(0xFF80D3D3)
)
```

## Component Color Usage Examples

### Location Card (Primary Theme)

```kotlin
@Composable
fun LocationCard(location: Location) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Location"
            )
            Column {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
```

### Photo Card (Secondary Theme)

```kotlin
@Composable
fun PhotoCard(photo: Photo) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column {
            AsyncImage(
                model = photo.url,
                contentDescription = photo.caption,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = photo.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
```

### Achievement Badge (Tertiary Theme)

```kotlin
@Composable
fun AchievementBadge(achievement: Achievement) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                tint = MaterialTheme.colorScheme.tertiary,
                contentDescription = "Achievement"
            )
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
```

### Stat Card (Neutral with Accent)

```kotlin
@Composable
fun StatCard(value: String, label: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = label
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

## Dynamic Color (Android 12+)

TrailGlass supports Material You dynamic colors on Android 12+ while maintaining brand identity:

```kotlin
@Composable
fun TrailGlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## Color Accessibility

### Contrast Ratios
- **Text on surfaces:** Minimum 4.5:1 (WCAG AA)
- **Large text (18sp+):** Minimum 3:1 (WCAG AA)
- **Interactive elements:** Minimum 3:1 (WCAG AA)

Material 3 color system automatically ensures proper contrast ratios for semantic colors.

### Testing Contrast
Use Material Theme's semantic colors (`onPrimary`, `onSurface`, etc.) to ensure proper contrast:

```kotlin
// Good: Uses semantic color for proper contrast
Text(
    text = "Location",
    color = MaterialTheme.colorScheme.onPrimaryContainer
)

// Bad: Manual color may not have proper contrast
Text(
    text = "Location",
    color = Color(0xFF000000) // May not contrast well
)
```

## Prompt Template

When generating color/theme code for Compose:

```
<compose_theme>
For TrailGlass Compose UI, use Material 3 semantic color system:

Brand Colors:
- Primary (Teal): Maps, location, navigation - use primary/primaryContainer
- Secondary (Orange): Photos, memories - use secondary/secondaryContainer
- Tertiary (Purple): Special events, achievements - use tertiary/tertiaryContainer

Color Usage:
- Location cards: primaryContainer with onPrimaryContainer text
- Photo cards: secondaryContainer with onSecondaryContainer text
- Achievement badges: tertiaryContainer with onTertiaryContainer text
- Stat cards: surfaceVariant with primary accent color

Visual Hierarchy:
- Use dominant colors with sharp accents
- Create contrast through color, not timid palettes
- Use container colors for backgrounds, on-container for text

Avoid:
- Generic purple gradients on white backgrounds
- Overusing primary color throughout UI
- Low-contrast color schemes

Always use MaterialTheme.colorScheme semantic colors for proper contrast and accessibility.
</compose_theme>
```

## Resources

- [Material 3 Color System](https://m3.material.io/styles/color/the-color-system/overview) - Official guidelines
- [Compose Theming](https://developer.android.com/jetpack/compose/themes) - Compose documentation
- [Material Color Tool](https://m2.material.io/design/color/the-color-system.html#tools-for-picking-colors) - Color picker

---

**Last Updated:** 2025-11-18
**Version:** 1.0
**Related:** `compose-ui-design-skill.md`

