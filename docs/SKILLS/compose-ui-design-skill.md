# Compose Multiplatform UI Design Skill

**Purpose:** Guide AI assistants to generate distinctive, high-quality Jetpack Compose UI components that avoid generic "AI slop" aesthetics and create memorable, brand-appropriate mobile interfaces.

**Context:** This skill is specifically adapted for Kotlin Multiplatform projects using Jetpack Compose (Android) and follows Material Design 3 principles while encouraging creative, distinctive design choices.

## The Problem: Distributional Convergence in Mobile UI

When generating Compose UI without guidance, AI models tend to converge toward:
- Default Material Design fonts (Roboto family)
- Generic Material 3 color schemes
- Minimal or no animations
- Flat, predictable layouts
- Cookie-cutter component patterns

This creates interfaces that look generic and lack brand identity, making them immediately recognizable as AI-generated.

## Core Principles

### 1. Typography: Distinctive Font Choices

**Avoid Generic Defaults:**
- Never use `FontFamily.Default` (Roboto) for display/headline text
- Avoid system default fonts without consideration
- Don't use the same font family across all text styles

**Encourage Distinctive Typography:**
- Use Google Fonts via `androidx.compose.ui.text.googleFonts` or custom font resources
- Consider font pairings: Display + Body, Serif + Sans-serif
- Use weight extremes: 100/200 for light, 700/800/900 for bold
- Vary font families between display and body text
- Consider travel/adventure-themed fonts for TrailGlass:
  - **Display/Headline:** Playfair Display, Crimson Pro, Bricolage Grotesque
  - **Body:** Source Sans 3, Inter (only if paired with distinctive display font)
  - **Monospace (for stats/data):** JetBrains Mono, Fira Code

**Compose Implementation:**
```kotlin
// Good: Distinctive font pairing
val DisplayFont = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

val BodyFont = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)
```

### 2. Color & Theme: Cohesive Aesthetic

**TrailGlass Brand Colors:**
- **Primary:** Teal/Cyan (#006A6A light, #80D3D3 dark) - maps and travel
- **Secondary:** Warm Orange (#845400 light, #FFB960 dark) - photos and memories
- **Tertiary:** Purple (#6B5778 light, #D6BEE4 dark) - special events

**Design Principles:**
- Use dominant colors with sharp accents, not timid evenly-distributed palettes
- Leverage Material 3's semantic color system (primary, secondary, tertiary containers)
- Create visual hierarchy through color contrast
- Use color to convey meaning (e.g., teal for location, orange for photos)
- Consider cultural/travel aesthetics for inspiration (Mediterranean blues, desert oranges, forest greens)

**Avoid:**
- Generic purple gradients on white backgrounds
- Overly safe, low-contrast color schemes
- Using only primary color throughout the UI

**Compose Implementation:**
```kotlin
// Good: Leverage Material 3 semantic colors with brand identity
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) {
    // Content
}

// Use color to convey meaning
Icon(
    imageVector = Icons.Default.Place,
    tint = MaterialTheme.colorScheme.primary, // Teal for location
    contentDescription = "Location"
)
```

### 3. Motion: Purposeful Animations

**Animation Principles:**
- Use animations for high-impact moments, not scattered micro-interactions
- Focus on page transitions, list item reveals, and state changes
- Prefer Compose Animation APIs over manual implementations
- Use staggered animations for list reveals (create visual rhythm)
- Consider entrance/exit animations for modals and sheets

**Compose Animation APIs:**
- `AnimatedVisibility` for show/hide transitions
- `animateContentSize` for expanding/collapsing content
- `animateColorAsState` for color transitions
- `Crossfade` for content swapping
- `InfiniteTransition` for loading states
- `AnimatedContent` for complex transitions

**Avoid:**
- Over-animating everything (creates janky feel)
- Ignoring animation performance (use `remember` and proper keys)
- Generic fade-in/fade-out without purpose

**Compose Implementation:**
```kotlin
// Good: Staggered list item reveals
LazyColumn {
    itemsIndexed(items) { index, item ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * 50 // Stagger
                )
            )
        ) {
            TimelineItemCard(item = item)
        }
    }
}

// Good: Purposeful state transitions
var expanded by remember { mutableStateOf(false) }
val backgroundColor by animateColorAsState(
    targetValue = if (expanded)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
)
```

### 4. Layout & Composition: Visual Hierarchy

**Principles:**
- Create clear visual hierarchy through spacing, typography, and color
- Use Material 3 components (Cards, Sheets, Chips) appropriately
- Leverage elevation and surface colors for depth
- Consider spacing scale: 4dp, 8dp, 16dp, 24dp, 32dp
- Use containers (Cards, Surfaces) to group related content

**Avoid:**
- Flat layouts without visual depth
- Inconsistent spacing
- Overusing the same component pattern
- Ignoring Material 3 elevation system

**Compose Implementation:**
```kotlin
// Good: Layered composition with depth
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Trip to Paris",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "3 days • 12 places",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### 5. Component Patterns: Distinctive Design

**TrailGlass-Specific Patterns:**
- **Timeline Items:** Use card-based layout with transport icons, distance badges
- **Stats Cards:** Large numbers with descriptive labels, use color to differentiate metrics
- **Map Markers:** Custom styling that matches brand colors
- **Photo Galleries:** Grid layouts with aspect ratio preservation

**Avoid:**
- Generic Material components without customization
- Copying common patterns without adaptation
- Ignoring platform conventions (Material Design 3)

**Compose Implementation:**
```kotlin
// Good: Custom component with brand identity
@Composable
fun VisitCard(
    visit: PlaceVisit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = visit.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

## Comprehensive Prompt Template

When generating Compose UI components, use this guidance:

```
<compose_ui_design>
You are generating Jetpack Compose UI for TrailGlass, a travel tracking app.

Avoid generic "AI slop" aesthetics. Create distinctive, memorable interfaces.

Typography:
- Never use FontFamily.Default for display/headline text
- Use distinctive Google Fonts (Playfair Display, Crimson Pro, Bricolage Grotesque for display)
- Pair display fonts with readable body fonts (Source Sans 3)
- Use weight extremes: 100/200 for light, 700/800/900 for bold
- Vary font families between display and body text

Color & Theme:
- Use TrailGlass brand colors: Teal (primary), Orange (secondary), Purple (tertiary)
- Leverage Material 3 semantic color system (containers, variants)
- Create visual hierarchy through color contrast
- Use color to convey meaning (teal for location, orange for photos)
- Avoid generic purple gradients on white backgrounds

Motion:
- Use animations for high-impact moments (page transitions, list reveals)
- Prefer Compose Animation APIs (AnimatedVisibility, animateContentSize)
- Use staggered animations for list items (create rhythm)
- Consider entrance/exit animations for modals
- Avoid over-animating everything

Layout & Composition:
- Create clear visual hierarchy through spacing, typography, and color
- Use Material 3 components appropriately (Cards, Sheets, Chips)
- Leverage elevation and surface colors for depth
- Use consistent spacing scale (4dp, 8dp, 16dp, 24dp, 32dp)

Component Patterns:
- Timeline items: Card-based with transport icons and distance badges
- Stats cards: Large numbers with descriptive labels
- Map markers: Custom styling matching brand colors
- Photo galleries: Grid layouts with aspect ratio preservation

Avoid:
- Generic Material components without customization
- Default Roboto fonts for display text
- Flat layouts without visual depth
- Overused patterns (purple gradients, Inter fonts)
- Ignoring Material 3 design system

Think creatively and make unexpected choices that feel genuinely designed for TrailGlass's travel/adventure context.
</compose_ui_design>
```

## Platform Considerations

### Android (Jetpack Compose)
- Follow Material Design 3 guidelines
- Use Material 3 components from `androidx.compose.material3`
- Leverage dynamic color (Android 12+) when appropriate
- Consider edge-to-edge design patterns
- Use Material Icons from `androidx.compose.material.icons`

### iOS (Future: SwiftUI)
- Follow Human Interface Guidelines
- Use SF Symbols for icons
- Leverage SwiftUI's native animation system
- Consider iOS-specific patterns (sheets, navigation)

## Examples of Good vs. Bad

### Bad: Generic Design
```kotlin
// Generic, boring design
Text(
    text = "Trip to Paris",
    style = MaterialTheme.typography.bodyLarge, // Default font
    modifier = Modifier.padding(8.dp)
)
```

### Good: Distinctive Design
```kotlin
// Distinctive, brand-appropriate design
Text(
    text = "Trip to Paris",
    style = MaterialTheme.typography.headlineSmall.copy(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold
    ),
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(16.dp)
)
```

## Integration with TrailGlass

This skill should be used when:
- Generating new Compose UI components
- Refactoring existing UI components
- Creating new screens or features
- Designing custom components

**Related Documentation:**
- `docs/UI_ARCHITECTURE.md` - Overall UI architecture
- `docs/UI_IMPLEMENTATION.md` - Current UI implementation guide
- `docs/FRONTEND_DESIGN_SKILLS_RESEARCH.md` - Source research

---

**Last Updated:** 2025-11-18
**Version:** 1.0
**Status:** Active

