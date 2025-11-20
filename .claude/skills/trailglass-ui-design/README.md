# Trailglass UI Design Skill

A specialized Claude Code skill for implementing platform-native UI designs in the Trailglass Kotlin Multiplatform project.

## What This Skill Does

This skill helps Claude Code create visually distinctive, platform-appropriate UI components for Trailglass:

- **Android**: Material 3 Expressive design with dynamic color, bold typography, and purposeful motion
- **iOS**: Liquid Glass aesthetic with blur, depth, translucency, and fluid spring animations

The skill addresses "distributional convergence" - avoiding generic AI-generated designs (purple gradients, Inter fonts, scattered animations) by providing specific, high-quality design patterns for each platform.

## When It Activates

Claude Code will automatically load this skill when you're:
- Creating or modifying UI screens
- Implementing visual components
- Working with Compose or SwiftUI files
- Asking about design patterns or styling
- Requesting platform-specific UI implementations

## What's Included

### Core Files

- **SKILL.md** - Main instructions with design principles, patterns, and anti-patterns
- **material3-reference.md** - Detailed Material 3 Expressive specifications
- **liquid-glass-reference.md** - Detailed iOS Liquid Glass specifications
- **examples.md** - Practical code examples for common patterns
- **README.md** - This file

### Key Concepts Covered

**Material 3 Expressive (Android):**
- Dynamic color system that adapts to user wallpaper
- Bold, high-contrast typography with extreme size variations
- Purposeful, orchestrated animations at key moments
- Elevated surfaces with tonal color containers
- 16dp rounded corners and proper spacing

**Liquid Glass (iOS):**
- Glassmorphic backgrounds with blur and translucency
- Soft gradients creating atmospheric depth
- SF Pro Rounded typography with strong hierarchy
- Spring-based, physics-driven animations
- Shadows and layering for visual depth

**Cross-Platform:**
- Functional parity with visual distinction
- Shared business logic (controllers/use cases)
- Platform-specific UI implementations
- Never mix design languages

## Design Philosophy

### Avoid "AI Slop"

The skill explicitly warns against generic AI patterns:
- ❌ Purple/neon gradients everywhere
- ❌ Generic Inter/Roboto without weight variation
- ❌ Flat, lifeless cards
- ❌ Micro-animations on every element
- ❌ Mixing design languages across platforms

### Embrace Platform Excellence

Instead, follow platform conventions:
- ✅ Dynamic/system color schemes
- ✅ Strong typographic hierarchy (large bold vs small light)
- ✅ High-impact animations at key moments
- ✅ Layered backgrounds (gradients + blur or elevation)
- ✅ Native patterns and components

## Usage Examples

### Creating a New Screen

When you ask: "Create a trip detail screen"

Claude Code will:
1. Reference this skill automatically
2. Design with Material 3 for Android (Compose)
3. Design with Liquid Glass for iOS (SwiftUI)
4. Ensure functional parity between platforms
5. Use shared controllers from commonMain
6. Apply proper spacing, typography, and motion

### Reviewing Existing UI

When you ask: "Review the StatsScreen design"

Claude Code will:
1. Check adherence to platform design guidelines
2. Verify typography hierarchy and contrast
3. Assess color usage (dynamic/system colors)
4. Review animation appropriateness
5. Suggest improvements following the skill's patterns

### Implementing Components

When you ask: "Create a visit card component"

Claude Code will:
1. Implement Material 3 version for Android
2. Implement Liquid Glass version for iOS
3. Follow the patterns from examples.md
4. Use proper elevation/depth techniques
5. Add appropriate animations

## Architecture Integration

This skill works with Trailglass's architecture:

```
Shared Controllers (StateFlow) ← Both platforms observe
    ↓
┌───────────────────┬───────────────────┐
│   Android UI      │      iOS UI       │
│  Material 3       │   Liquid Glass    │
│  (Compose)        │   (SwiftUI)       │
└───────────────────┴───────────────────┘
```

## File Organization

When working with UI, Claude Code knows where to find files:

**Android:**
- Screens: `composeApp/src/main/kotlin/ui/screens/`
- Components: `composeApp/src/main/kotlin/ui/components/`
- Theme: `composeApp/src/main/kotlin/ui/theme/`

**iOS:**
- Views: `iosApp/iosApp/Views/`
- Components: `iosApp/iosApp/Components/`

**Shared:**
- Controllers: `shared/src/commonMain/kotlin/feature/*/`
- Use cases: `shared/src/commonMain/kotlin/feature/*/`

## Quick Reference

### Material 3 Essentials

```kotlin
// Dynamic color
MaterialTheme.colorScheme.primaryContainer

// Bold typography
style = MaterialTheme.typography.displayMedium
fontWeight = FontWeight.Bold

// Rounded corners
shape = RoundedCornerShape(16.dp)

// Purposeful animation
animationSpec = tween(300, easing = FastOutSlowInEasing)
```

### Liquid Glass Essentials

```swift
// Blur material
.background(.ultraThinMaterial)

// SF Rounded typography
.font(.system(size: 32, weight: .bold, design: .rounded))

// Spring animation
withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) { }

// Atmospheric gradient
LinearGradient(
    colors: [Color.blue.opacity(0.3), Color.purple.opacity(0.2)],
    startPoint: .topLeading,
    endPoint: .bottomTrailing
)
```

## Testing Design

### Android
- Use Compose Preview in Android Studio
- Test with dynamic color (different wallpapers)
- Verify dark mode
- Test on real device

### iOS
- Use SwiftUI Preview in Xcode
- Test with Dynamic Type (accessibility sizes)
- Verify dark mode
- Test on real device

## Related Documentation

- `docs/UI_IMPLEMENTATION.md` - Full UI architecture
- `AGENTS.md` - General project instructions
- `.cursor/rules/android-compose.mdc` - Compose rules
- Material 3 Guidelines: https://m3.material.io/
- iOS HIG: https://developer.apple.com/design/human-interface-guidelines/

## Skill Metadata

- **Name**: trailglass-ui-design
- **Type**: Project-specific design system skill
- **Platforms**: Android (Material 3) + iOS (Liquid Glass)
- **Framework**: Kotlin Multiplatform
- **Auto-loads**: When working with UI files or design requests

---

*This skill ensures Trailglass maintains high-quality, platform-appropriate visual design while avoiding generic AI-generated patterns.*
