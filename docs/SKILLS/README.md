# TrailGlass UI Design Skills

This directory contains specialized "skills" - prompt templates and guidelines - for generating high-quality, distinctive UI components for TrailGlass. These skills are adapted from research on improving frontend design through AI guidance.

## Purpose

These skills help AI assistants (like Claude) generate Compose Multiplatform UI components that:
- Avoid generic "AI slop" aesthetics
- Create distinctive, brand-appropriate interfaces
- Follow Material Design 3 principles
- Establish strong visual hierarchy
- Enhance user experience through purposeful design

## Skills Overview

### 1. [compose-ui-design-skill.md](compose-ui-design-skill.md)
**Comprehensive UI Design Skill**

The main skill document covering all aspects of Compose UI design:
- Typography principles
- Color & theme guidelines
- Motion & animation patterns
- Layout & composition
- Component patterns

**Use when:** Generating any Compose UI component or screen.

### 2. [compose-typography-skill.md](compose-typography-skill.md)
**Typography-Specific Skill**

Detailed guidance on creating distinctive typography systems:
- Font selection (Playfair Display, Source Sans 3, JetBrains Mono)
- Font pairing strategies
- Weight and size guidelines
- Implementation examples

**Use when:** Defining typography systems or generating text-heavy components.

### 3. [compose-motion-skill.md](compose-motion-skill.md)
**Animation & Motion Skill**

Guidelines for purposeful, performant animations:
- Compose Animation APIs (AnimatedVisibility, animateContentSize, etc.)
- Staggered list reveals
- Loading state animations
- Performance best practices

**Use when:** Adding animations to components or creating animated transitions.

### 4. [compose-theme-skill.md](compose-theme-skill.md)
**Theme & Color Skill**

TrailGlass brand color system and Material 3 implementation:
- Brand colors (Teal, Orange, Purple)
- Semantic color usage
- Component color examples
- Accessibility considerations

**Use when:** Defining color schemes or generating colored components.

## How to Use These Skills

### For AI Assistants

When generating Compose UI code, reference the appropriate skill:

```
Please generate a timeline item card component following the
compose-ui-design-skill.md guidelines, specifically using the
typography and theme skills for distinctive styling.
```

### For Developers

1. **Read the comprehensive skill** (`compose-ui-design-skill.md`) for overall understanding
2. **Reference specific skills** when working on particular aspects:
   - Typography → `compose-typography-skill.md`
   - Animations → `compose-motion-skill.md`
   - Colors → `compose-theme-skill.md`
3. **Follow the prompt templates** included in each skill when requesting AI assistance

### Integration with Development Workflow

These skills should be used when:
- Creating new UI components
- Refactoring existing components
- Designing new screens
- Establishing design system patterns
- Requesting AI assistance for UI generation

## Key Principles

### 1. Avoid Generic Defaults
- Never use `FontFamily.Default` for display text
- Avoid generic Material color schemes without customization
- Don't skip animations entirely

### 2. Create Visual Hierarchy
- Use distinctive typography for headlines
- Leverage color contrast for emphasis
- Use spacing and elevation for depth

### 3. Purposeful Design
- Every animation should serve a purpose
- Colors should convey meaning (teal = location, orange = photos)
- Typography should establish hierarchy

### 4. Brand Identity
- Use TrailGlass brand colors consistently
- Create travel/adventure-themed aesthetics
- Maintain cohesive design language

## Examples

### Good: Distinctive Design
```kotlin
// Uses distinctive font, brand colors, purposeful animation
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    modifier = Modifier.animateContentSize()
) {
    Text(
        text = "Trip to Paris",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = DisplayFont,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}
```

### Bad: Generic Design
```kotlin
// Generic default font, no animation, basic colors
Card {
    Text(
        text = "Trip to Paris",
        style = MaterialTheme.typography.bodyLarge // Default font
    )
}
```

## Related Documentation

- [UI Architecture](../UI_ARCHITECTURE.md) - Overall UI architecture
- [UI Implementation](../UI_IMPLEMENTATION.md) - Current UI implementation guide
- [Frontend Design Skills Research](../FRONTEND_DESIGN_SKILLS_RESEARCH.md) - Source research

## Updates

These skills are living documents and should be updated as:
- Design system evolves
- New patterns are established
- Best practices change
- Brand guidelines update

**Last Updated:** 2025-11-18
**Version:** 1.0

