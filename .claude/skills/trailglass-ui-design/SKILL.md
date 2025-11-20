---
name: trailglass-ui-design
description: Specialized UI design guidance for Trailglass KMP project with Material 3 Expressive (Android) and Liquid Glass (iOS) design systems. Use when creating or modifying UI components, screens, or visual design.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
---

# Trailglass UI Design Skill

Expert guidance for implementing native UI designs in Trailglass, a Kotlin Multiplatform travel logging app with distinct platform-specific design languages.

## Design Philosophy

Trailglass uses **separate native design systems** to honor platform conventions while maintaining functional parity:

- **Android**: Material 3 Expressive with dynamic color, bold typography, and purposeful motion
- **iOS**: Liquid Glass aesthetic with blur, depth, translucency, and fluid animations

**Critical Rule**: Never mix design languages. Android uses Material 3 components; iOS uses SwiftUI with Liquid Glass styling.

## Android: Material 3 Expressive

### Color System

Use Material 3 dynamic color with extended color schemes:

```kotlin
// Theme colors from system
MaterialTheme.colorScheme.primaryContainer
MaterialTheme.colorScheme.onPrimaryContainer
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.surfaceVariant

// Avoid flat backgrounds - use containers
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
)
```

**Principles:**
- Use dynamic color system (adapts to user wallpaper)
- High contrast for accessibility
- Purposeful use of primary/secondary/tertiary colors
- Avoid generic grays - use surface variants

### Typography (Material 3 Expressive)

Material 3 Expressive emphasizes **bold, high-contrast typography**:

```kotlin
// Avoid: Generic Inter/Roboto everywhere
Text(
    text = "Paris",
    style = MaterialTheme.typography.displayLarge  // Bold, expressive
)

// Use variety and contrast
Column {
    Text(
        text = "Trip to Paris",
        style = MaterialTheme.typography.displayMedium,  // Large, bold
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "January 15-20, 2024",
        style = MaterialTheme.typography.bodySmall,  // Small, contrast
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

**Typography Scale:**
- displayLarge/Medium/Small: Hero content, major headings
- headlineLarge/Medium/Small: Section headers
- titleLarge/Medium/Small: Card titles, list headers
- bodyLarge/Medium/Small: Body text, descriptions
- labelLarge/Medium/Small: Buttons, captions

**Expressive Principles:**
- Extreme size contrast (displayLarge vs bodySmall)
- Bold weights for emphasis
- Limited font families (stick to system fonts)

### Layout & Spacing

Use Material 3 spacing for consistency:

```kotlin
// Standard Material spacing
modifier = Modifier
    .fillMaxWidth()
    .padding(16.dp)  // Standard padding

// Card spacing
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(visits) { visit ->
        VisitCard(visit)
    }
}
```

**Spacing Scale:**
- 4dp: Minimal (between related elements)
- 8dp: Tight (within components)
- 12dp: Comfortable (between cards/items)
- 16dp: Standard (screen padding)
- 24dp: Spacious (section separation)

### Motion & Animation

Material 3 Expressive uses **purposeful, orchestrated motion**:

```kotlin
// Avoid scattered micro-animations
// Instead: High-impact transitions

// Navigation transitions
AnimatedContent(
    targetState = selectedScreen,
    transitionSpec = {
        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
            slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) togetherWith
            fadeOut(animationSpec = tween(300))
    }
) { screen ->
    ScreenContent(screen)
}

// List item entrance animations
LazyColumn {
    itemsIndexed(items) { index, item ->
        val animationDelay = (index * 50).coerceAtMost(300)
        VisitCard(
            visit = item,
            modifier = Modifier.animateEnterExit(
                enter = fadeIn(tween(300, delayMillis = animationDelay)) +
                       slideInVertically(tween(400, delayMillis = animationDelay))
            )
        )
    }
}
```

**Motion Principles:**
- Use shared element transitions for continuity
- Orchestrate multiple elements (stagger animations)
- Easing curves: FastOutSlowInEasing, LinearOutSlowInEasing
- Duration: 200-400ms (avoid too fast or slow)
- Focus on key moments (navigation, state changes)

### Component Patterns

```kotlin
// Top App Bar with dynamic color
@Composable
fun TimelineTopBar() {
    TopAppBar(
        title = {
            Text(
                "Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {
            IconButton(onClick = { /* filter */ }) {
                Icon(Icons.Filled.FilterList, "Filter")
            }
        }
    )
}

// Cards with elevation and shape
@Composable
fun VisitCard(visit: PlaceVisit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),  // Material 3 rounded corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bold title
            Text(
                text = visit.city ?: "Unknown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Contrasting subtitle
            Text(
                text = formatDateRange(visit.startTime, visit.endTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Supporting details
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailChip(
                    icon = Icons.Filled.LocationOn,
                    text = visit.country ?: ""
                )
                DetailChip(
                    icon = Icons.Filled.AccessTime,
                    text = formatDuration(visit.startTime, visit.endTime)
                )
            }
        }
    }
}

// Floating Action Button
FloatingActionButton(
    onClick = { /* add */ },
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Icon(Icons.Filled.Add, "Add visit")
}
```

**Anti-Patterns (Avoid):**
- Purple gradients and neon colors (AI slop)
- Generic Inter/Roboto without weight variation
- Flat, lifeless cards without elevation
- Micro-animations on every element
- Ignoring dynamic color system

## iOS: Liquid Glass Design

### Visual Language

Liquid Glass emphasizes **depth, translucency, and fluidity**:

```swift
// Glassmorphic backgrounds
.background {
    ZStack {
        LinearGradient(
            colors: [
                Color.blue.opacity(0.3),
                Color.purple.opacity(0.2)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .blur(radius: 30)

        Rectangle()
            .fill(.ultraThinMaterial)
    }
}

// Depth through shadows and blur
VStack {
    // Content
}
.background {
    RoundedRectangle(cornerRadius: 20)
        .fill(.ultraThinMaterial)
        .shadow(color: .black.opacity(0.1), radius: 20, y: 10)
}
```

**Core Principles:**
- Layered, translucent surfaces
- Blur effects (.ultraThinMaterial, .regularMaterial)
- Subtle gradients (never harsh)
- Depth through shadows and hierarchy
- Fluid, continuous animations

### Typography (SF Pro/Rounded)

iOS uses San Francisco with emphasis on hierarchy:

```swift
VStack(alignment: .leading, spacing: 4) {
    // Large, bold title
    Text("Paris")
        .font(.system(size: 32, weight: .bold, design: .rounded))
        .foregroundStyle(.primary)

    // Small, light subtitle - high contrast
    Text("January 15-20, 2024")
        .font(.system(size: 14, weight: .regular, design: .rounded))
        .foregroundStyle(.secondary)
}
```

**Typography Scale:**
- .largeTitle: Hero content (34pt)
- .title: Section headers (28pt)
- .title2: Subsection headers (22pt)
- .title3: Card headers (20pt)
- .headline: Emphasized body (17pt bold)
- .body: Body text (17pt)
- .callout: Supporting text (16pt)
- .subheadline: Captions (15pt)
- .footnote: Fine print (13pt)

**Design Principles:**
- SF Rounded for friendly, modern feel
- Weight variation (ultraLight to black)
- Dynamic Type support for accessibility

### Layout & Spacing

SwiftUI spacing with iOS conventions:

```swift
VStack(spacing: 12) {
    ForEach(visits) { visit in
        VisitCard(visit: visit)
    }
}
.padding(.horizontal, 20)  // Screen padding
.padding(.vertical, 16)

// Safe area insets
ScrollView {
    LazyVStack(spacing: 16) {
        // Content
    }
}
.safeAreaInset(edge: .bottom) {
    // Bottom bar
}
```

**Spacing Scale:**
- 4: Minimal
- 8: Tight
- 12: Comfortable
- 16: Standard
- 20: Screen edges (iOS convention)
- 24: Section separation

### Motion & Gestures

Liquid Glass uses **fluid, physics-based animations**:

```swift
// Spring animations (iOS signature)
withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
    selectedView = newView
}

// Gesture-driven interactions
.gesture(
    DragGesture()
        .onChanged { gesture in
            // Fluid, continuous feedback
            offset = gesture.translation
        }
        .onEnded { _ in
            withAnimation(.spring()) {
                offset = .zero
            }
        }
)

// View transitions
.transition(
    .asymmetric(
        insertion: .move(edge: .trailing).combined(with: .opacity),
        removal: .move(edge: .leading).combined(with: .opacity)
    )
)

// Matched geometry for hero transitions
@Namespace private var animation

// Source view
RoundedRectangle(cornerRadius: 10)
    .matchedGeometryEffect(id: "card", in: animation)

// Destination view
RoundedRectangle(cornerRadius: 20)
    .matchedGeometryEffect(id: "card", in: animation)
```

**Motion Principles:**
- Spring animations (feel natural, physics-based)
- Gesture-driven (follow finger)
- Matched geometry for continuity
- Duration: 0.3-0.6s with spring curves
- Interruptible (can cancel mid-animation)

### Component Patterns

```swift
// Glassmorphic Card
struct VisitCard: View {
    let visit: PlaceVisit

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Bold title
            Text(visit.city ?? "Unknown")
                .font(.system(size: 24, weight: .bold, design: .rounded))
                .foregroundStyle(.primary)

            // Light subtitle - contrast
            Text(formatDateRange(visit.startTime, visit.endTime))
                .font(.system(size: 14, weight: .regular))
                .foregroundStyle(.secondary)

            Divider()
                .opacity(0.5)

            // Details with icons
            HStack(spacing: 20) {
                Label(visit.country ?? "", systemImage: "location.fill")
                Label(formatDuration(visit), systemImage: "clock.fill")
            }
            .font(.caption)
            .foregroundStyle(.secondary)
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background {
            RoundedRectangle(cornerRadius: 20)
                .fill(.ultraThinMaterial)
                .shadow(color: .black.opacity(0.1), radius: 15, y: 8)
        }
        .overlay {
            RoundedRectangle(cornerRadius: 20)
                .strokeBorder(.white.opacity(0.2), lineWidth: 1)
        }
    }
}

// Navigation with blur
NavigationStack {
    ScrollView {
        LazyVStack(spacing: 16) {
            ForEach(visits) { visit in
                VisitCard(visit: visit)
            }
        }
        .padding(.horizontal, 20)
    }
    .background {
        // Atmospheric gradient
        LinearGradient(
            colors: [
                Color.blue.opacity(0.15),
                Color.purple.opacity(0.1)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
        .ignoresSafeArea()
    }
    .navigationTitle("Timeline")
    .navigationBarTitleDisplayMode(.large)
}

// Floating button with glass effect
Button(action: { /* add */ }) {
    Image(systemName: "plus")
        .font(.system(size: 24, weight: .semibold))
        .foregroundStyle(.white)
        .frame(width: 56, height: 56)
        .background {
            Circle()
                .fill(
                    LinearGradient(
                        colors: [Color.blue, Color.purple],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .shadow(color: .blue.opacity(0.4), radius: 15, y: 8)
        }
}
```

**Anti-Patterns (Avoid):**
- Material Design components on iOS
- Hard edges without blur/depth
- Flat colors without gradients/materials
- Abrupt animations (use springs)
- Ignoring safe area insets

## Cross-Platform Considerations

### Functional Parity, Visual Distinction

Both platforms show the same data and features but with native visual language:

```
Feature: Trip Timeline
├── Android: Material 3 Card with elevation, dynamic color
└── iOS: Glassmorphic Card with blur, depth, shadow

Feature: Map View
├── Android: Google Maps with Material FAB, bottom sheet
└── iOS: MapKit with glass overlay, native gestures

Feature: Photo Gallery
├── Android: Grid with Material transitions, Coil loading
└── iOS: Grid with matched geometry, native Photos integration
```

### Shared Logic, Native UI

Controllers and use cases are 100% shared (Kotlin Multiplatform):

```kotlin
// Shared: commonMain
class TimelineController @Inject constructor(...) {
    val state: StateFlow<TimelineState> = ...
    fun selectDate(date: LocalDate) { ... }
}

// Android: Compose observes StateFlow
@Composable
fun TimelineScreen(controller: TimelineController) {
    val state by controller.state.collectAsState()
    // Material 3 UI
}

// iOS: SwiftUI observes via wrapper
struct TimelineView: View {
    @StateObject var viewModel: TimelineViewModel
    var body: some View {
        // Liquid Glass UI
    }
}
```

## Implementation Guidelines

### Starting a New Screen

1. **Define shared state in controller** (commonMain)
2. **Implement Android UI** with Material 3 Expressive
3. **Implement iOS UI** with Liquid Glass
4. **Verify functional parity** (same features, different visuals)
5. **Test on both platforms**

### When Creating Components

**Ask:**
- Does this follow platform conventions?
- Am I mixing design languages? (NO!)
- Does typography have strong contrast?
- Are animations purposeful and fluid?
- Do colors use dynamic/system schemes?

### Design System Files

Reference these files for context:

- Android theme: `composeApp/src/main/kotlin/ui/theme/Theme.kt`
- Android typography: `composeApp/src/main/kotlin/ui/theme/Type.kt`
- iOS views: `iosApp/iosApp/Views/`
- Shared controllers: `shared/src/commonMain/kotlin/feature/*/`

### Testing Design

- **Android**: Preview in Android Studio, test on device with dark mode
- **iOS**: Preview in Xcode, test with Dynamic Type sizes
- **Both**: Verify animations are smooth (60fps), test on actual hardware

## Common Mistakes to Avoid

### ❌ Don't
- Use Material components on iOS
- Use iOS SF Symbols on Android
- Mix purple gradients with generic fonts
- Ignore platform spacing conventions
- Create identical UI on both platforms
- Use flat colors without depth/material
- Add animations to every element
- Hardcode colors (use theme/system)

### ✅ Do
- Honor platform design languages
- Use dynamic/system color schemes
- Create strong typographic hierarchy
- Animate key moments, not everything
- Layer backgrounds (gradients + blur/elevation)
- Follow platform spacing scales
- Test on actual devices
- Maintain functional parity

## Quick Reference

**Material 3 Expressive:**
- Dynamic color system
- Bold, high-contrast typography
- Elevation and surface variants
- Purposeful, orchestrated motion
- Rounded corners (16dp)

**Liquid Glass:**
- Blur and translucency (.ultraThinMaterial)
- Subtle gradients with depth
- SF Pro Rounded typography
- Spring animations (physics-based)
- Shadows for hierarchy

**Both:**
- Avoid AI slop (purple, generic fonts, scattered animations)
- Strong typographic contrast (large bold vs small light)
- Purposeful motion at key moments
- System/dynamic colors
- Native platform conventions

---

*Use this skill when designing or implementing UI screens, components, or visual elements for Trailglass. It ensures platform-appropriate design while maintaining functional consistency.*
