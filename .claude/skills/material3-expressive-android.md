---
name: material3-expressive-android
description: Expert guidance for implementing Material 3 Expressive design system on Android with Jetpack Compose. Covers spring animations, emphasized typography, shape morphing, flexible app bars, and component creation following Google's latest design language.
version: 1.0.0
type: project
tags:
  - android
  - material-design
  - jetpack-compose
  - ui-design
  - animations
---

# Material 3 Expressive Android Design Expert

You are an expert in implementing Material 3 Expressive design system on Android using Jetpack Compose. Material 3 Expressive was announced at Google I/O 2025 and represents Google's most dynamic and fluid design language.

## Core Principles

### 1. Spring-Based Motion
- Use physics-based spring animations for all transitions
- Configure via `MotionConfig.kt` with standardized spring specifications
- Key parameters: dampingRatio (0.5-0.85), stiffness (Spring.StiffnessLow to Spring.StiffnessMedium)
- Common configurations:
  - `expressiveSpring`: Response 0.5s, damping 0.65 (default for most transitions)
  - `quickSpring`: Response 0.3s, damping 0.8 (immediate feedback)
  - `bouncySpring`: Response 0.4s, damping 0.5 (playful interactions)

### 2. Emphasized Typography
- Dual typography system: Baseline + Emphasized
- Emphasized variants are +1-2sp larger and one weight heavier
- Example: `displayLarge` (57sp) → `displayLargeEmphasized` (59sp, Medium weight)
- Access via `MaterialTheme.typography.emphasized.headlineLarge`
- Use emphasized for primary content, baseline for secondary

### 3. Extended Color System
- Extend Material 3 ColorScheme with semantic colors
- Access via `MaterialTheme.colorScheme.extended.success`
- Categories: success/warning, routes, categories, disabled states
- All colors adapt to light/dark mode automatically
- Integration with project color palette (e.g., Silent Waters)

### 4. Shape Morphing
- Custom path-based interpolation system (60 points for smoothness)
- Shapes: Circle, Triangle, Hexagon, RoundedSquare, Wave, Petal
- Semantic mapping: categories → shapes, transport types → shapes
- Use `animateShapeMorph(targetShape, animationSpec = expressiveSpring())`
- Performance: Limit active morphs to 5-8 simultaneous for 60fps

### 5. Component Patterns

#### Floating Action Button Menu
```kotlin
@Composable
fun ExpandableFABMenu(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    items: List<FABMenuItem>
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Scrim overlay when expanded
    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onExpandChange(false) }
        )
    }

    // Menu items with scale + opacity animations
    items.forEachIndexed { index, item ->
        AnimatedVisibility(
            visible = expanded,
            enter = scaleIn(animationSpec = spring(...)) + fadeIn(),
            exit = scaleOut(animationSpec = spring(...)) + fadeOut()
        ) {
            // Item content
        }
    }
}
```

#### Loading Indicators
- Wavy spinner: Circular with sin/cos wave distortion
- Pulsing: Concentric rings with staggered scale animations
- Morphing: Shape cycling with smooth transitions
- All use infinite repeatable animations with spring physics

#### Button Groups / Segmented Controls
```kotlin
@Composable
fun <T> SegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            val containerColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.Transparent,
                animationSpec = spring(dampingRatio = 0.7)
            )

            Button(
                onClick = { onItemSelected(item) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor
                )
            ) {
                // Content
            }
        }
    }
}
```

#### Flexible Top App Bars
- Large (180dp), Medium (128dp), Compact (64dp) variants
- Collapse on scroll using `TopAppBarScrollBehavior`
- Parallax backgrounds: Move at 0.5x scroll speed
- Emphasized typography for titles (collapses to baseline)
- Hero content with gradients or images

### 6. File Organization

**Theme Files:**
- `ui/theme/Color.kt` - ColorScheme definitions with project colors
- `ui/theme/ColorExtensions.kt` - ExtendedColors data class
- `ui/theme/Type.kt` - EmphasizedTypography system
- `ui/theme/MotionConfig.kt` - Spring animation configurations
- `ui/theme/ShapeMorphing.kt` - Shape morphing system

**Component Files:**
- `ui/components/` - Reusable components
- Name pattern: `ComponentName.kt` (e.g., `TrackingFABMenu.kt`)
- Include preview providers for all components

**Screen Files:**
- `ui/screens/` - Feature screens
- Use ViewModel pattern with StateFlow
- Integrate components with proper state management

### 7. Best Practices

**Animations:**
- Always use MotionConfig spring specs, never hardcoded values
- Limit simultaneous animations (max 10 for 60fps)
- Use `animateXAsState` for simple value animations
- Use `updateTransition` for coordinated multi-property animations

**Colors:**
- Use `MaterialTheme.colorScheme.extended.X` for semantic colors
- Never hardcode color values in composables
- Support both light and dark modes
- Ensure WCAG AA contrast ratios (4.5:1 normal text, 3:1 large text)

**Performance:**
- Use `remember` to cache expensive calculations
- Use `derivedStateOf` for computed values
- Avoid recomposition in tight loops (use `key()` parameter)
- Profile with Layout Inspector and GPU rendering

**Accessibility:**
- All interactive elements ≥ 48dp touch target
- Provide contentDescription for icons
- Support Dynamic Type (use MaterialTheme.typography)
- Test with TalkBack enabled

## Implementation Workflow

1. **Setup Theme:**
   - Create MotionConfig.kt with spring configurations
   - Extend ColorScheme with ExtendedColors
   - Create EmphasizedTypography variants

2. **Create Components:**
   - Start with basic Compose structure
   - Apply spring animations from MotionConfig
   - Use extended colors for semantic meaning
   - Add emphasized typography for hierarchy
   - Include preview providers

3. **Integrate:**
   - Use components in screens
   - Connect to ViewModels with StateFlow
   - Test animations with different data states
   - Verify accessibility

4. **Polish:**
   - Fine-tune spring parameters
   - Add shape morphing for visual interest
   - Optimize for 60fps
   - Test on multiple device sizes

## Common Patterns

### Staggered List Animations
```kotlin
LazyColumn {
    itemsIndexed(items) { index, item ->
        val animatedProgress = remember {
            Animatable(0f).apply {
                launch {
                    delay(index * 50L)
                    animateTo(1f, animationSpec = spring(...))
                }
            }
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = (1f - animatedProgress.value) * 100f
                    alpha = animatedProgress.value
                }
        ) {
            // Item content
        }
    }
}
```

### Interactive Press Effects
```kotlin
val pressed = remember { mutableStateOf(false) }
val scale by animateFloatAsState(
    targetValue = if (pressed.value) 0.97f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)

Box(
    modifier = Modifier
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed.value = true
                    tryAwaitRelease()
                    pressed.value = false
                }
            )
        }
)
```

## References

- Material 3 Expressive launched May 2025 as part of Android 16
- Based on Google's research into dynamic, fluid interfaces
- Emphasizes natural motion through spring physics
- Part of broader Material You evolution
- Used across Google apps (Messages, Photos, Gmail)

## Quick Reference

**Access Extended Colors:**
```kotlin
MaterialTheme.colorScheme.extended.success
MaterialTheme.colorScheme.extended.activeRoute
MaterialTheme.colorScheme.extended.warning
```

**Access Emphasized Typography:**
```kotlin
MaterialTheme.typography.emphasized.headlineLarge
MaterialTheme.typography.emphasized.titleMedium
```

**Apply Spring Animation:**
```kotlin
val value by animateFloatAsState(
    targetValue = target,
    animationSpec = MotionConfig.expressiveSpring
)
```

**Shape Morphing:**
```kotlin
val shape by animateShapeMorph(
    targetShape = MorphableShapes.Circle,
    animationSpec = expressiveSpring()
)
```

---

When implementing Material 3 Expressive components, always prioritize:
1. Spring-based motion (never linear)
2. Emphasized typography for hierarchy
3. Extended color system for semantics
4. 60fps performance
5. Accessibility compliance (WCAG AA)
