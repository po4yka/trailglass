# Design Prompts for Trailglass

This directory contains AI-powered design prompts for generating UI mockups and design assets for the Trailglass KMP application.

## Contents

- **[kmp-mockup-prompt.md](./kmp-mockup-prompt.md)** - Full prompt for generating 8-screen Android/iOS comparison mockups
- **[color-scheme-reference.md](./color-scheme-reference.md)** - Complete color palette and usage guidelines
- **[prompt-usage-guide.md](./prompt-usage-guide.md)** - How to use these prompts effectively

## Overview

These prompts are designed for use with **Gemini 2.5 Flash Image** (nano-banana) at https://aistudio.google.com/ to generate professional-quality UI mockups showcasing Trailglass's cross-platform design.

### What You Can Generate

- **Platform Comparison Mockups**: Side-by-side Android (Material 3 Expressive) and iOS (native/Liquid Glass) screens
- **User Journey Flows**: 4-step horizontal journey showing Timeline → Map → Details → Stats
- **Color-Accurate Designs**: Using the "Silent Waters" palette defined in the codebase
- **Realistic Content**: No lorem ipsum - actual travel data, place names, and metrics

## Quick Start

1. Open https://aistudio.google.com/
2. Copy the full prompt from [kmp-mockup-prompt.md](./kmp-mockup-prompt.md)
3. Paste into Gemini 2.5 Flash Image
4. Generate and iterate as needed

## Design System

### Android (Material 3 Expressive)
- Dynamic color theming with Cool Steel primary
- Tonal elevation system
- Material components (Extended FAB, Bottom Nav, Cards)
- 4dp grid system

### iOS (Native/Liquid Glass)
- Translucent blur effects with Cool Steel tint
- SF Pro typography
- Native iOS components (Tab Bar, Navigation Bar)
- 8pt grid system

## Color Palette

Based on the "Silent Waters" theme:
- **Primary**: Cool Steel (#9DB4C0)
- **Secondary**: Blue Slate (#5C6B73)
- **Background**: Light Cyan (#E0FBFC) light / Jet Black (#253237) dark
- **Accents**: Sea Glass (#8BB5A1), Coastal Path (#7A9CAF), Harbor Blue (#5C8AA8)

See [color-scheme-reference.md](./color-scheme-reference.md) for complete specifications.

## Use Cases

- GitHub repository header/hero image
- Investor pitch decks demonstrating KMP capability
- App Store/Play Store marketing materials
- Design portfolio showcasing cross-platform expertise
- Technical documentation showing platform parity
- Design reviews and stakeholder presentations

## Customization

You can customize the prompts by:
- Changing the 4 screen journey (e.g., Onboarding → Setup → First Trip → Success)
- Adjusting color intensity/saturation
- Modifying layout (horizontal vs vertical comparison)
- Focusing on specific features (Maps, Statistics, Photos, etc.)

## Platform Parity

These mockups demonstrate functional parity while respecting platform-specific design conventions:
- Same data and content across platforms
- Platform-appropriate navigation patterns
- Native component usage (Material vs iOS)
- Consistent color theming adapted to each platform

## Related Documentation

- [../COLOR_PALETTE_USAGE.md](../COLOR_PALETTE_USAGE.md) - How colors are used in the codebase
- [../UI_ARCHITECTURE.md](../UI_ARCHITECTURE.md) - UI architecture and component structure
- [../PLATFORM_DIFFERENCES.md](../PLATFORM_DIFFERENCES.md) - Platform-specific implementations

## Feedback

If you generate mockups using these prompts, consider:
- Saving successful generations to `design-prompts/generated/` (gitignored)
- Documenting any prompt improvements or iterations
- Sharing effective variations that work well
