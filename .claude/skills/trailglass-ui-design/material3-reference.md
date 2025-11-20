# Material 3 Expressive Design Reference

Detailed reference for implementing Material 3 Expressive design in Trailglass Android app.

## Color System

### Dynamic Color

Material 3 uses a dynamic color system that adapts to the user's wallpaper:

```kotlin
// In Theme.kt
@Composable
fun TrailglassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Enable dynamic color
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
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

### Color Roles

Material 3 provides semantic color roles:

**Primary Colors:**
- `primary`: Main brand color
- `onPrimary`: Content on primary
- `primaryContainer`: Tonal variant
- `onPrimaryContainer`: Content on container

**Secondary Colors:**
- `secondary`: Accent color
- `onSecondary`: Content on secondary
- `secondaryContainer`: Tonal variant
- `onSecondaryContainer`: Content on container

**Tertiary Colors:**
- `tertiary`: Additional accent
- `onTertiary`: Content on tertiary
- `tertiaryContainer`: Tonal variant
- `onTertiaryContainer`: Content on container

**Surface Colors:**
- `surface`: Default background
- `surfaceDim`: Slightly dimmed
- `surfaceBright`: Slightly brightened
- `surfaceContainerLowest`: Lowest elevation
- `surfaceContainerLow`: Low elevation
- `surfaceContainer`: Medium elevation
- `surfaceContainerHigh`: High elevation
- `surfaceContainerHighest`: Highest elevation

**Usage:**
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
) { /* content */ }
```

## Typography Scale

### Material 3 Type Scale

```kotlin
val Typography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Normal
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Normal
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Normal
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)
```

### Expressive Typography Usage

Create strong hierarchies with extreme contrast:

```kotlin
@Composable
fun VisitHeader(visit: PlaceVisit) {
    Column {
        // Large, bold title (display)
        Text(
            text = visit.city ?: "Unknown Location",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Small, light subtitle (body) - extreme contrast
        Text(
            text = "${visit.country} • ${formatDuration(visit)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

## Shape System

Material 3 uses rounded corners extensively:

```kotlin
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Usage
Card(
    shape = MaterialTheme.shapes.large  // 16.dp rounded corners
) { /* content */ }
```

## Elevation

Material 3 uses tonal elevation (color) rather than shadow elevation:

```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp,
        pressedElevation = 8.dp,
        hoveredElevation = 4.dp
    )
) { /* content */ }
```

## Motion Specifications

### Duration

- **Extra Short:** 50-100ms (simple icon transitions)
- **Short:** 100-200ms (small component changes)
- **Medium:** 200-300ms (most transitions)
- **Long:** 300-400ms (complex transitions)
- **Extra Long:** 400-500ms (major layout changes)

### Easing

```kotlin
import androidx.compose.animation.core.*

// Material easing curves
val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
val AccelerateEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
val DecelerateEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

// Compose built-ins
FastOutSlowInEasing  // Standard
LinearOutSlowInEasing  // Decelerate
FastOutLinearInEasing  // Accelerate
```

### Transitions

```kotlin
// Enter/Exit transitions
val enterTransition = fadeIn(
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + slideInVertically(
    animationSpec = tween(300, easing = FastOutSlowInEasing),
    initialOffsetY = { it / 2 }
)

val exitTransition = fadeOut(
    animationSpec = tween(200, easing = FastOutLinearInEasing)
) + slideOutVertically(
    animationSpec = tween(200, easing = FastOutLinearInEasing),
    targetOffsetY = { -it / 2 }
)
```

## Component Examples

### Bottom Sheet

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailsBottomSheet(
    visit: PlaceVisit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = visit.city ?: "Unknown",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            // More content...
        }
    }
}
```

### Navigation Bar

```kotlin
@Composable
fun TrailglassNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Filled.BarChart, "Stats") },
            label = { Text("Stats") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Filled.Timeline, "Timeline") },
            label = { Text("Timeline") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Filled.Map, "Map") },
            label = { Text("Map") }
        )
    }
}
```

### Search Bar

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSearch() },
        active = false,
        onActiveChange = {},
        placeholder = { Text("Search trips...") },
        leadingIcon = { Icon(Icons.Filled.Search, "Search") },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        // Search suggestions
    }
}
```

## Accessibility

### Minimum Touch Targets

Material 3 requires 48dp minimum touch target size:

```kotlin
IconButton(
    onClick = { /* action */ },
    modifier = Modifier.size(48.dp)  // Minimum touch target
) {
    Icon(Icons.Filled.Favorite, "Favorite")
}
```

### Contrast

Ensure 4.5:1 contrast ratio for text:

```kotlin
Text(
    text = "Important information",
    color = MaterialTheme.colorScheme.onSurface,  // Guaranteed contrast
    style = MaterialTheme.typography.bodyLarge
)
```

### Semantics

Add content descriptions for accessibility:

```kotlin
Icon(
    imageVector = Icons.Filled.LocationOn,
    contentDescription = "Location: ${visit.city}",
    tint = MaterialTheme.colorScheme.primary
)
```

## Resources

- [Material 3 Design Kit](https://m3.material.io/)
- [Compose Material 3 Docs](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Color System](https://m3.material.io/styles/color/system/overview)
- [Typography](https://m3.material.io/styles/typography/overview)
- [Motion Guidelines](https://m3.material.io/styles/motion/overview)
