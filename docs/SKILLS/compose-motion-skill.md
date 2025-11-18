# Compose Motion & Animation Skill

**Purpose:** Guide AI assistants to create purposeful, performant animations in Jetpack Compose that enhance user experience without creating janky or over-animated interfaces.

## The Problem

Without guidance, AI-generated Compose UI often lacks animations entirely or includes generic fade-ins that don't serve a purpose. Well-designed motion creates delight and guides user attention.

## Animation Principles

### 1. Purposeful Motion
- Use animations to guide user attention
- Animate state changes (loading, success, error)
- Create visual feedback for user interactions
- Use motion to establish hierarchy and relationships

### 2. Performance First
- Use `remember` to avoid recreating animations
- Prefer Compose Animation APIs over manual implementations
- Consider animation performance on lower-end devices
- Use appropriate animation durations (200-500ms for most interactions)

### 3. High-Impact Moments
- Page/screen transitions
- List item reveals (staggered animations)
- Modal/sheet entrances
- Loading states
- Success/error feedback

### 4. Avoid Over-Animation
- Don't animate everything (creates janky feel)
- Avoid animations that don't serve a purpose
- Don't ignore Material Design motion guidelines

## Compose Animation APIs

### AnimatedVisibility
For showing/hiding content with transitions.

```kotlin
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn() + slideInVertically(),
    exit = fadeOut() + slideOutVertically()
) {
    Content()
}
```

### animateContentSize
For expanding/collapsing content.

```kotlin
Column(
    modifier = Modifier.animateContentSize()
) {
    if (expanded) {
        AdditionalContent()
    }
}
```

### animateColorAsState
For color transitions.

```kotlin
val backgroundColor by animateColorAsState(
    targetValue = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
)
```

### Crossfade
For swapping content.

```kotlin
Crossfade(targetState = currentScreen) { screen ->
    when (screen) {
        Screen.Stats -> StatsScreen()
        Screen.Timeline -> TimelineScreen()
    }
}
```

### InfiniteTransition
For continuous animations (loading, pulsing).

```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "loading")
val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
        animation = tween(2000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
)
```

### AnimatedContent
For complex content transitions.

```kotlin
AnimatedContent(
    targetState = count,
    transitionSpec = {
        slideInVertically { it } + fadeIn() togetherWith
        slideOutVertically { -it } + fadeOut()
    }
) { targetCount ->
    Text("$targetCount")
}
```

## TrailGlass-Specific Animation Patterns

### 1. Staggered List Item Reveals

Create visual rhythm when revealing timeline items or stats.

```kotlin
@Composable
fun TimelineList(items: List<TimelineItem>) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50 // Stagger: 0ms, 50ms, 100ms...
                    )
                )
            ) {
                TimelineItemCard(item = item)
            }
        }
    }
}
```

### 2. Card Expansion Animation

Smooth expansion for trip details, visit cards.

```kotlin
@Composable
fun ExpandableCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.animateContentSize(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title)

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = content,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

### 3. Loading State Animations

Distinctive loading indicators for sync, data fetching.

```kotlin
@Composable
fun SyncIndicator(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isActive) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Icon(
        imageVector = Icons.Default.CloudSync,
        contentDescription = "Syncing",
        modifier = Modifier.rotate(rotation)
    )
}
```

### 4. Success/Error Feedback

Animated feedback for user actions.

```kotlin
@Composable
fun ActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSuccess by remember { mutableStateOf(false) }

    Button(
        onClick = {
            onClick()
            showSuccess = true
        },
        modifier = modifier
    ) {
        AnimatedContent(targetState = showSuccess) { success ->
            if (success) {
                Icon(Icons.Default.Check, "Success")
            } else {
                Text("Save")
            }
        }
    }
}
```

### 5. Page Transitions

Smooth transitions between screens.

```kotlin
@Composable
fun ScreenContainer(currentScreen: Screen) {
    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(300)
    ) { screen ->
        when (screen) {
            Screen.Stats -> StatsScreen()
            Screen.Timeline -> TimelineScreen()
            Screen.Map -> MapScreen()
        }
    }
}
```

## Animation Timing

### Duration Guidelines
- **Micro-interactions:** 150-200ms (button presses, icon changes)
- **Standard transitions:** 300-400ms (card expansions, list reveals)
- **Page transitions:** 400-500ms (screen changes)
- **Complex animations:** 500-800ms (multi-step animations)

### Easing Functions
- **StandardDecelerate:** For entrances (ease out)
- **StandardAccelerate:** For exits (ease in)
- **FastOutSlowIn:** For material motion (recommended)
- **LinearEasing:** For continuous animations (loading spinners)

```kotlin
import androidx.compose.animation.core.FastOutSlowInEasing

animationSpec = tween(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)
```

## Performance Best Practices

### 1. Use remember for Animation State

```kotlin
// Good: Animation state remembered
val infiniteTransition = rememberInfiniteTransition(label = "loading")

// Bad: Recreated on every recomposition
val infiniteTransition = InfiniteTransition()
```

### 2. Use Appropriate Keys

```kotlin
// Good: Key prevents animation restart
AnimatedContent(
    targetState = count,
    key = { it } // Unique key for each count value
) { targetCount ->
    Text("$targetCount")
}
```

### 3. Avoid Unnecessary Animations

```kotlin
// Good: Only animate when needed
if (shouldAnimate) {
    AnimatedVisibility(visible = isVisible) {
        Content()
    }
} else {
    if (isVisible) {
        Content()
    }
}
```

## Prompt Template

When generating animations for Compose:

```
<compose_motion>
For TrailGlass Compose UI, create purposeful animations:

High-Impact Moments:
- Staggered list item reveals (50ms delay between items)
- Card expansion/collapse with animateContentSize
- Loading state animations (rotating icons, pulsing)
- Success/error feedback (icon changes, color transitions)
- Page transitions with Crossfade

Use Compose Animation APIs:
- AnimatedVisibility for show/hide
- animateContentSize for expanding content
- animateColorAsState for color transitions
- InfiniteTransition for loading states
- Crossfade for content swapping

Timing:
- Micro-interactions: 150-200ms
- Standard transitions: 300-400ms
- Page transitions: 400-500ms

Easing: Use FastOutSlowInEasing for Material motion

Performance:
- Use remember for animation state
- Use appropriate keys for AnimatedContent
- Avoid unnecessary animations

Avoid over-animating. Each animation should serve a purpose.
</compose_motion>
```

## Examples

### Good: Purposeful Staggered Animation

```kotlin
LazyColumn {
    itemsIndexed(timelineItems) { index, item ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * 50,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            TimelineItemCard(item = item)
        }
    }
}
```

### Bad: Generic Fade Without Purpose

```kotlin
// Don't do this - animation without purpose
AnimatedVisibility(
    visible = true,
    enter = fadeIn() // Generic fade, no purpose
) {
    StaticContent() // Content doesn't benefit from animation
}
```

## Resources

- [Compose Animation](https://developer.android.com/jetpack/compose/animation) - Official documentation
- [Material Motion](https://m3.material.io/styles/motion/overview) - Material Design motion guidelines
- [Compose Animation Samples](https://github.com/android/compose-samples) - Example code

---

**Last Updated:** 2025-11-18
**Version:** 1.0
**Related:** `compose-ui-design-skill.md`

