# TrailGlass Compose UI Design - Quick Reference

Quick reference guide for generating distinctive Compose UI components for TrailGlass.

## Typography

**Display/Headline:** Playfair Display, Crimson Pro, Bricolage Grotesque
- Never use `FontFamily.Default` for display text
- Use bold weights (700/800/900)
- Large sizes (24sp-57sp)

**Body:** Source Sans 3, Inter (only if paired with distinctive display)
- Normal weight (400)
- Readable sizes (12sp-16sp)

**Stats/Data:** JetBrains Mono, Fira Code
- Monospace for numbers
- Bold for emphasis

## Colors

**Primary (Teal):** Maps, location, navigation
- Light: `#006A6A` / Container: `#9DF0F0`
- Dark: `#80D3D3` / Container: `#004F4F`

**Secondary (Orange):** Photos, memories
- Light: `#845400` / Container: `#FFDDB5`
- Dark: `#FFB960` / Container: `#653F00`

**Tertiary (Purple):** Special events, achievements
- Light: `#6B5778` / Container: `#F3DAFF`
- Dark: `#D6BEE4` / Container: `#52405F`

**Usage:**
- Location cards → `primaryContainer` / `onPrimaryContainer`
- Photo cards → `secondaryContainer` / `onSecondaryContainer`
- Achievement badges → `tertiaryContainer` / `onTertiaryContainer`

## Motion

**High-Impact Animations:**
- Staggered list reveals (50ms delay)
- Card expansion (`animateContentSize`)
- Loading states (`InfiniteTransition`)
- Page transitions (`Crossfade`)

**Timing:**
- Micro-interactions: 150-200ms
- Standard: 300-400ms
- Page transitions: 400-500ms

**Easing:** `FastOutSlowInEasing` for Material motion

## Component Patterns

**Timeline Items:**
- Card-based layout
- Transport icons
- Distance badges
- Primary container color

**Stats Cards:**
- Large numbers (monospace font)
- Descriptive labels
- Primary accent color
- Surface variant background

**Photo Galleries:**
- Grid layouts
- Aspect ratio preservation
- Secondary container color

## Quick Prompt Template

```
Generate Compose UI following TrailGlass design skills:
- Typography: Distinctive fonts (Playfair Display for display, Source Sans 3 for body)
- Colors: Use Material 3 semantic colors (primaryContainer for location, secondaryContainer for photos)
- Motion: Staggered animations for lists, animateContentSize for expansions
- Avoid: FontFamily.Default, generic purple gradients, flat layouts
```

## Key Principles

1. **Avoid Generic Defaults** - Never use default fonts for display text
2. **Create Hierarchy** - Use typography, color, and spacing
3. **Purposeful Motion** - Every animation should serve a purpose
4. **Brand Identity** - Use TrailGlass colors consistently

## Full Documentation

- [Compose UI Design Skill](compose-ui-design-skill.md) - Comprehensive guide
- [Typography Skill](compose-typography-skill.md) - Font details
- [Motion Skill](compose-motion-skill.md) - Animation patterns
- [Theme Skill](compose-theme-skill.md) - Color system

---

**Last Updated:** 2025-11-18

