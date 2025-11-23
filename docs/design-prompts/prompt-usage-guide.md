# Design Prompt Usage Guide

Complete guide for generating high-quality UI mockups for Trailglass using AI image generation (Gemini 2.5 Flash).

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Step-by-Step Process](#step-by-step-process)
3. [Customization Strategies](#customization-strategies)
4. [Troubleshooting Common Issues](#troubleshooting-common-issues)
5. [Advanced Techniques](#advanced-techniques)
6. [Best Practices](#best-practices)
7. [Example Variations](#example-variations)

---

## Getting Started

### Prerequisites

- Access to **Google AI Studio**: https://aistudio.google.com/
- The full prompt from [kmp-mockup-prompt.md](./kmp-mockup-prompt.md)
- Color reference from [color-scheme-reference.md](./color-scheme-reference.md)

### What You'll Generate

**Default Output**:
- 8 device screens (4 Android + 4 iOS)
- Side-by-side platform comparison
- Complete user journey from Timeline → Map → Details → Stats
- Professional presentation quality (7680×2160px minimum)

**Typical Use Cases**:
- GitHub repository header/hero image
- Investor presentations and pitch decks
- App store screenshots and marketing
- Design reviews and stakeholder alignment
- Portfolio pieces showcasing KMP expertise

---

## Step-by-Step Process

### Step 1: Access Google AI Studio

1. Navigate to https://aistudio.google.com/
2. Sign in with your Google account
3. Select **Gemini 2.5 Flash Image** (nano-banana) model
4. Ensure you're in image generation mode

### Step 2: Prepare the Prompt

1. Open [kmp-mockup-prompt.md](./kmp-mockup-prompt.md)
2. Copy the **entire prompt** (from `<Create a professional KMP app mockup figure>` to the end)
3. Review the 4 screen specifications to ensure they match your desired journey

### Step 3: Generate Initial Mockup

1. Paste the full prompt into Gemini AI Studio
2. Click **Generate** or **Run**
3. Wait 30-60 seconds for generation (varies by load)
4. Review the output

### Step 4: Evaluate the Output

Check for:
- ✓ 8 devices arranged horizontally (4 pairs)
- ✓ Android on left, iOS on right in each pair
- ✓ Colors match "Silent Waters" palette
- ✓ Realistic content (no lorem ipsum)
- ✓ Consistent data across platforms
- ✓ Proper device frames (Pixel 8 Pro, iPhone 15 Pro Max)
- ✓ Professional shadows and lighting

### Step 5: Iterate (if needed)

If the output has issues, see [Troubleshooting](#troubleshooting-common-issues) below.

Common first-iteration adjustments:
- Increase color saturation
- Adjust spacing between devices
- Sharpen typography
- Fix content consistency

### Step 6: Export and Use

1. Download the generated image (usually PNG format)
2. Verify resolution (should be 7680×2160 or higher)
3. Optionally compress for web use
4. Use in documentation, presentations, or marketing

---

## Customization Strategies

### Changing the User Journey

The default journey is: **Timeline → Map → Trip Details → Statistics**

To customize, replace the **SCREEN-BY-SCREEN CONTENT SPECIFICATION** section with your desired flow.

#### Example: Onboarding Flow

Replace Pair 1-4 with:

```
Pair 1: Welcome Screen
- Hero image with app logo and tagline
- "Track your travels privately" headline
- "Get Started" button in Cool Steel

Pair 2: Permissions Request
- Location permission explanation
- "Always Allow" permission dialog
- Privacy assurance text

Pair 3: First Trip Setup
- Empty timeline state
- "Start your first trip" prompt
- FAB/button to begin tracking

Pair 4: Success State
- First trip logged on timeline
- Success badge in Sea Glass
- "You're all set!" message
```

#### Example: Photo Feature Flow

```
Pair 1: Photo Gallery Grid
- Grid of attached photos (3×4)
- Filter chips by trip/date
- "12 photos" count

Pair 2: Photo Attachment Dialog
- Trip selection list
- "Attach to Weekend Hike" selection
- Confirm button

Pair 3: Photo Detail View
- Full-screen photo
- Location and timestamp overlay
- Share/edit buttons

Pair 4: Photo Map View
- Map with photo markers
- Clustered photo locations
- Tap to view photo
```

### Adjusting Visual Style

#### Higher Contrast

Add to the **ADDITIONAL VISUAL REQUIREMENTS** section:

```
- Increase contrast by 20% for all UI elements
- Use deeper shadows (opacity from 15% to 25%)
- Boost color saturation by 15%
```

#### Softer, More Muted

```
- Reduce saturation by 10% across all colors
- Softer shadows (opacity from 15% to 8%)
- Increased blur radius on translucent elements (iOS)
```

#### Dark Mode Focus

Replace light mode specifications with:

```
Show ONLY dark mode:
- Android: Jet Black (#253237) background, Light Cyan (#E0FBFC) text
- iOS: Jet Black background with translucent overlays
- All cards and surfaces using dark color scheme
```

### Layout Variations

#### Vertical Comparison (2 Rows)

Replace **Layout Configuration** with:

```
Layout Configuration:
- Arrange in 2 rows of 4 screens each
- Top row: Android (Pixel 8 Pro) - Timeline, Map, Details, Stats
- Bottom row: iOS (iPhone 15 Pro Max) - same 4 screens
- Vertical alignment showing corresponding screens stacked
- Column labels: "Timeline", "Map", "Details", "Stats"
- Row labels: "Android (Material 3)" and "iOS (Native)"
```

#### Single Platform (4 Screens)

Remove all references to the other platform:

**Android Only**:
```
showing exactly 4 Android device screens arranged horizontally
[Remove all iOS specifications]
[Increase inter-device spacing to 100px]
```

**iOS Only**:
```
showing exactly 4 iPhone screens arranged horizontally
[Remove all Android specifications]
[Increase inter-device spacing to 100px]
```

#### Focus View (2 Screens, 1 Platform)

```
showing exactly 2 iPhone 15 Pro Max screens arranged horizontally
[Specify only 2 screens, e.g., Map View and Trip Details]
[Increase device size and detail]
[Add more granular UI specifications]
```

### Content Customization

#### Different Geographic Region

Replace San Francisco references with your region:

```
Realistic Travel Data:
- Trip examples: "Morning Run: Central Park Loop", "Lunch at Shake Shack", "Museum Visit: MoMA"
- Place names: "Brooklyn Bridge", "Times Square", "Battery Park"
- Map center: New York City coordinates
```

#### Different Activity Types

```
- Trip examples: "Mountain Biking: Moab Slickrock", "Rock Climbing: Smith Rock", "Camping: Yosemite Valley"
- Transport modes: Hiking, Biking, Climbing, Camping icons
- Categories: Mountain (#...), Desert (#...), Forest (#...)
```

---

## Troubleshooting Common Issues

### Issue: Colors Look Washed Out

**Solution**: Add color boost specification

```
ADDITIONAL COLOR REQUIREMENTS:
- Exact hex color matching with NO desaturation
- Vibrant, saturated colors at 100% intensity
- High contrast with WCAG AAA compliance
- No color fading or washing out
```

### Issue: Text Is Blurry or Unreadable

**Solution**: Enhance typography clarity

```
TYPOGRAPHY REQUIREMENTS:
- Ultra-sharp text rendering at 300dpi
- Anti-aliased typography with subpixel rendering
- Crisp, readable text at all sizes
- High-contrast text (12.8:1 ratio minimum)
```

### Issue: Devices Look Unrealistic

**Solution**: Specify photorealistic rendering

```
DEVICE RENDERING:
- Photorealistic Pixel 8 Pro and iPhone 15 Pro Max
- Accurate materials: glass, aluminum (Pixel), titanium (iPhone)
- Realistic reflections and edge highlights
- Perfect screen-to-frame integration
- No visible rendering artifacts
```

### Issue: Content Inconsistent Between Platforms

**Solution**: Emphasize data parity

```
DATA CONSISTENCY (CRITICAL):
- IDENTICAL trip names on corresponding Android/iOS screens
- SAME timestamps, distances, durations
- MATCHING photo counts and thumbnails
- SYNCHRONIZED map viewport and coordinates
- IDENTICAL user profile information
```

### Issue: Spacing Too Tight

**Solution**: Increase gaps

```
Layout Configuration:
- Inter-pair gap: 180px (increased from 120px)
- Intra-pair gap: 80px (increased from 60px)
- Margin around entire composition: 100px
```

### Issue: Wrong Screen Count

**Solution**: Reinforce count specification

```
CRITICAL REQUIREMENT:
Generate EXACTLY 8 device screens:
- 4 Android devices (Pixel 8 Pro)
- 4 iOS devices (iPhone 15 Pro Max)
- Arranged as 4 side-by-side pairs
- No more, no less
```

### Issue: Platform Design Not Authentic

**Solution**: Add platform-specific references

```
Android MUST use:
- Material Design 3 components from material.io/components
- Google's Material motion specifications
- Roboto font family (not San Francisco or other fonts)

iOS MUST use:
- SwiftUI native components per Apple HIG
- SF Pro font family (not Roboto or other fonts)
- SF Symbols for icons
```

---

## Advanced Techniques

### Multi-Journey Comparison

Generate multiple mockup sets to compare different user flows:

1. **Journey A**: Onboarding flow (4 screens)
2. **Journey B**: Core usage flow (4 screens)
3. **Journey C**: Settings/configuration (4 screens)

Then combine in presentation software to show complete user experience.

### Animation Indicators

Add motion hints to show interactive elements:

```
ANIMATION INDICATORS:
- Subtle motion blur on FAB to suggest tap interaction
- Swipe gesture indicator on cards (horizontal arrow fade)
- Pull-to-refresh indicator at top of timeline
- Transition hints between screens (fade/slide arrows)
```

### Empty vs Filled States

Generate two versions:

**Version 1 - Empty States**:
```
Show EMPTY states:
- Timeline: "No trips yet" empty state illustration
- Map: User location only, no routes or places
- Stats: "Start tracking to see insights" placeholder
```

**Version 2 - Filled States** (default):
```
Show FILLED states with rich data
```

### Platform Feature Highlighting

Emphasize platform-specific capabilities:

**Android Dynamic Color**:
```
Android screens MUST show:
- Material You dynamic color adaptation
- Wallpaper-influenced color scheme
- Note in caption: "Adapts to wallpaper on Android 12+"
```

**iOS Live Activities**:
```
iOS screens MUST show:
- Live Activity for active trip tracking
- Dynamic Island integration
- Note in caption: "Real-time tracking in Dynamic Island"
```

### Different Device Models

**Older Devices**:
```
Android: Google Pixel 6 with centered camera hole
iOS: iPhone 14 Pro with notch (not Dynamic Island)
```

**Larger Screens**:
```
Android: Google Pixel Tablet (10.95")
iOS: iPad Pro 12.9" with Magic Keyboard
```

---

## Best Practices

### Do's

✓ **Use realistic, domain-specific content**
- Real place names, actual distances, believable timestamps
- No "lorem ipsum" or placeholder text

✓ **Maintain platform parity**
- Same data across Android and iOS
- Identical trip names, photos, metrics

✓ **Specify exact colors**
- Use hex codes from color-scheme-reference.md
- Don't rely on color names alone

✓ **Request consistent lighting**
- Soft, diffused lighting for professionalism
- No harsh shadows or glare

✓ **Emphasize the journey**
- Each screen should tell part of a story
- Logical flow from left to right

✓ **Iterate incrementally**
- Change one variable at a time
- Document successful variations

### Don'ts

✗ **Don't mix design systems**
- No Material components on iOS
- No iOS controls on Android

✗ **Don't use generic placeholders**
- "Trip 1", "Trip 2" ❌
- "Weekend Hike: Mt. Tam" ✓

✗ **Don't overcrowd screens**
- Limit to 6-8 distinct elements per screen
- Use whitespace generously

✗ **Don't ignore accessibility**
- Maintain WCAG AA contrast ratios
- Don't use color alone to convey information

✗ **Don't request impossible features**
- No unreleased OS features
- No fantasy UI components

---

## Example Variations

### Variation 1: Dark Mode Focus

**Changes**:
- Replace all light mode specs with dark mode
- Emphasize translucency and depth
- Highlight glow effects

**Result**: Professional dark mode mockup suitable for dark-themed marketing.

### Variation 2: Map Feature Showcase

**Changes**:
- All 4 screens show map variations
- Screen 1: Standard map view
- Screen 2: Satellite view
- Screen 3: Route visualization
- Screen 4: Place clustering

**Result**: Deep dive into mapping capabilities.

### Variation 3: Photo Feature Focus

**Changes**:
- Screen 1: Photo gallery grid
- Screen 2: Photo detail with EXIF
- Screen 3: Photo on map
- Screen 4: Photo timeline integration

**Result**: Showcases photo attachment feature comprehensively.

### Variation 4: Settings & Configuration

**Changes**:
- Screen 1: Main settings list
- Screen 2: Algorithm parameters
- Screen 3: Privacy settings
- Screen 4: Device management

**Result**: Shows configuration depth and privacy features.

---

## Iteration Workflow

### Recommended Process

1. **Generation 1**: Use prompt as-is
   - Evaluate overall layout and content

2. **Generation 2**: Adjust ONE variable
   - If colors off → boost saturation
   - If spacing tight → increase gaps
   - If text blurry → enhance typography

3. **Generation 3**: Fine-tune based on Gen 2
   - Refine the successful change
   - Don't introduce new variables yet

4. **Generation 4**: Add secondary improvements
   - Now adjust lighting, shadows, or depth
   - Keep successful changes from Gen 2-3

5. **Generation 5**: Final polish
   - Small tweaks only
   - Lock in the best version

### Tracking Iterations

Create a log:

```
Generation 1: Baseline (colors washed out)
Generation 2: +20% saturation (better, but spacing tight)
Generation 3: +20% saturation, spacing 150px (much better)
Generation 4: +20% sat, spacing 150px, sharper text (WINNER)
Generation 5: Same as Gen 4 with subtle shadows (slight improvement)
```

---

## Integration with Documentation

### Embedding in README

```markdown
# Trailglass

![App Mockup](docs/design-prompts/generated/trailglass-mockup-v1.png)

*Privacy-first travel logging for iOS and Android*
```

### Using in Presentations

**Slide 1**: Full 8-screen mockup showing platform parity

**Slide 2**: Crop to Android screens only with callouts

**Slide 3**: Crop to iOS screens only with callouts

**Slide 4**: Side-by-side detail comparison (1 Android + 1 iOS)

### Portfolio Presentation

```
Project: Trailglass KMP
Role: Cross-platform architecture

[8-screen mockup]

Demonstrates:
- Material 3 Expressive design system (Android)
- Native iOS/Liquid Glass design (iOS)
- Functional parity with platform-appropriate UX
- "Silent Waters" custom color palette
```

---

## Next Steps

1. **Generate your first mockup** using the default prompt
2. **Save successful outputs** to `docs/design-prompts/generated/` (add to .gitignore)
3. **Document effective variations** in this guide
4. **Share with the team** for feedback and iteration

For questions or improvements to this guide, open an issue or PR on the repository.

---

## Related Documentation

- [kmp-mockup-prompt.md](./kmp-mockup-prompt.md) - Full prompt template
- [color-scheme-reference.md](./color-scheme-reference.md) - Complete color specifications
- [README.md](./README.md) - Design prompts overview
