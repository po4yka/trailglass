# Quick Reference - Design Prompts

Fast reference for common tasks when generating UI mockups.

---

## Quick Start (30 seconds)

1. Go to https://aistudio.google.com/
2. Select **Gemini 2.5 Flash Image**
3. Copy prompt from [kmp-mockup-prompt.md](./kmp-mockup-prompt.md)
4. Paste and generate
5. Download result

---

## Essential Color Codes

```
Primary:     #9DB4C0  (Cool Steel)
Secondary:   #5C6B73  (Blue Slate)
Background:  #E0FBFC  (Light Cyan - light mode)
             #253237  (Jet Black - dark mode)
Success:     #8BB5A1  (Sea Glass)
Warning:     #C9B896  (Driftwood)
Active Route:#7A9CAF  (Coastal Path)
Past Route:  #5C8AA8  (Harbor Blue)
```

---

## Common Modifications

### Make Colors More Vibrant

Add after color specifications:
```
- Increase saturation by 15-20%
- High contrast with exact hex matching
- No color desaturation or fading
```

### Increase Spacing

Change in Layout Configuration:
```
- Inter-pair gap: 150px (was 120px)
- Intra-pair gap: 80px (was 60px)
```

### Sharpen Text

Add to Typography Requirements:
```
- Ultra-sharp rendering at 300dpi
- Crisp anti-aliased text at all sizes
- High contrast text (12:1 minimum ratio)
```

### Dark Mode Only

Replace theme sections with:
```
Show ONLY dark mode:
- Background: Jet Black (#253237)
- Text: Light Cyan (#E0FBFC)
- All surfaces use dark color scheme
```

### Single Platform (iOS)

Replace opening with:
```
showing exactly 4 iPhone 15 Pro Max screens
[Remove all Android specifications]
```

### Single Platform (Android)

Replace opening with:
```
showing exactly 4 Google Pixel 8 Pro screens
[Remove all iOS specifications]
```

---

## Screen Content Templates

### Timeline Screen

```
- Top App Bar: "Timeline" title
- Date headers: "Today", "Yesterday"
- Trip cards:
  - "Morning Commute" | Walk icon | "2.4 km • 45 min"
  - "Weekend Hike" | Hike icon | "8.7 mi • 3h 20m" | "12 photos"
- Extended FAB: "New Trip" (Android) or Nav bar button (iOS)
- Bottom Nav/Tab Bar active on Timeline
```

### Map Screen

```
- Map centered on San Francisco Bay Area
- User location: Blue dot with Cool Steel pulse
- 8-10 place markers in Cool Steel
- Active route: Coastal Path polyline
- Historical routes: Harbor Blue with opacity
- Cluster marker: "5" in Blue Slate circle
- Bottom sheet: "Current Location: Ferry Building"
```

### Trip Details

```
- Hero image: Landscape photo (Mt. Tam vista)
- Stats: "8.7 mi" | "3h 20m" | "1,240 ft gain"
- Route map thumbnail
- Photos section: 12 photos, horizontal scroll
- Places visited list:
  - "Pantoll Trailhead" | "9:00 AM"
  - "Summit" | "11:15 AM" | Success badge
- Journal entry text
```

### Statistics Screen

```
- Overview cards: "127 km" | "18 places" | "23 trips"
- Activity heatmap: 7×5 grid with gradient
- Top places bar chart:
  - "Golden Gate Park" | "8 visits"
  - "Ferry Building" | "6 visits"
- Transport modes donut chart:
  - Walking 45% | Driving 30% | Cycling 15% | Transit 10%
```

---

## Realistic Content Examples

### Trip Names
```
"Morning Commute to Office"
"Weekend Hike: Mt. Tamalpais Summit Trail"
"Coffee Break at Blue Bottle Hayes Valley"
"Lunch at Ferry Building Marketplace"
"Evening Run: Golden Gate Park Loop"
"Grocery Shopping at Trader Joe's"
```

### Place Names
```
"Golden Gate Park"
"Ferry Building"
"Dolores Park"
"Blue Bottle Coffee - Hayes Valley"
"Pantoll Trailhead"
"Mt. Tamalpais Summit"
"Crissy Field"
"Lands End Trail"
```

### Metrics
```
Distances: "2.4 km", "8.7 mi", "450 m", "12.3 km"
Durations: "45 min", "3h 20m", "1h 15m", "22 min"
Times: "8:42 AM", "Yesterday, 2:15 PM", "Dec 15, 2024"
Photo counts: "12 photos", "3 photos", "No photos"
```

---

## Troubleshooting Fast Fixes

| Problem | Quick Fix |
|---------|-----------|
| Colors washed out | Add: "Exact hex colors, 100% saturation, no fading" |
| Text blurry | Add: "Ultra-sharp 300dpi typography" |
| Spacing too tight | Change gaps to 150px and 80px |
| Wrong device count | Emphasize: "EXACTLY 8 screens" at start |
| Devices look fake | Add: "Photorealistic Pixel 8 Pro and iPhone 15 Pro Max" |
| Content inconsistent | Add: "IDENTICAL data across platforms" |
| Platform styles mixed | Add: "Material 3 for Android ONLY, iOS native for iOS ONLY" |

---

## File Organization

```
docs/design-prompts/
├── README.md                      # Overview and quick start
├── kmp-mockup-prompt.md          # Full base prompt ⭐
├── color-scheme-reference.md     # All colors and codes
├── prompt-usage-guide.md         # Detailed how-to
├── prompt-variations.md          # Pre-made variations
├── quick-reference.md            # This file
└── generated/                    # Your generated images (gitignored)
    ├── README.md
    ├── trailglass-mockup-base-v1.png
    ├── trailglass-mockup-onboarding-v1.png
    └── ...
```

---

## Iteration Checklist

Before generating:
- [ ] Colors defined with hex codes
- [ ] Journey makes logical sense (flow)
- [ ] Content is realistic (no lorem ipsum)
- [ ] Platform specs are authentic
- [ ] Screen count specified (4, 8, etc.)

After generating:
- [ ] Check color accuracy vs reference
- [ ] Verify platform design authenticity
- [ ] Confirm content consistency
- [ ] Review spacing and layout
- [ ] Test readability at different sizes

If iterating:
- [ ] Change ONE variable at a time
- [ ] Document what you changed
- [ ] Save successful versions
- [ ] Note the change in filename (v1, v2, etc.)

---

## Useful Snippets

### Add to Prompt for Higher Quality

```
OUTPUT REQUIREMENTS:
- Ultra-high resolution (minimum 8K width)
- Photorealistic device rendering
- Professional studio lighting
- Perfect color accuracy to hex codes
- Crisp, sharp typography at all sizes
- No rendering artifacts or compression
```

### Emphasize Platform Authenticity

```
CRITICAL PLATFORM REQUIREMENTS:
Android MUST use:
- Material Design 3 components only
- Roboto font family
- Material icons
- Material motion specifications

iOS MUST use:
- Native iOS 17+ components only
- SF Pro font family
- SF Symbols
- Apple HIG specifications
```

### Add Privacy Emphasis

```
PRIVACY INDICATORS:
- Background location icon in status bar
- "All data stored locally" visible on screens
- Shield icons in Sea Glass (#8BB5A1) color
- Privacy messaging throughout UI
- No cloud/sync indicators (local-only)
```

---

## Generation Time Estimates

| Variation | Typical Time | Complexity |
|-----------|--------------|------------|
| 4 screens, 1 platform | 30-45 sec | Low |
| 8 screens, 2 platforms | 45-75 sec | Medium |
| 8 screens, dark mode | 60-90 sec | Medium |
| Custom journey | 45-75 sec | Medium |
| With animations/motion | 90-120 sec | High |

---

## Common Use Cases

### For GitHub README
```
Goal: Hero image showing platform parity
Variation: Base 8-screen KMP mockup
Orientation: Horizontal
Resolution: 7680×2160 minimum
Format: PNG
```

### For App Store / Play Store
```
Goal: Platform-specific screenshots
Variation: iOS-only or Android-only (4 screens)
Orientation: Portrait device screenshots
Resolution: Platform-specific requirements
Format: PNG (uncompressed)
```

### For Pitch Deck
```
Goal: Show technical capability
Variation: Side-by-side comparison (8 screens)
Orientation: Horizontal (16:9 aspect)
Resolution: 1920×1080 or higher
Format: PNG or high-quality JPG
```

### For Design Portfolio
```
Goal: Showcase design system knowledge
Variation: Dark mode + Light mode (2 sets)
Orientation: Horizontal
Resolution: 4K minimum
Format: PNG
```

---

## Quick Color Reference Card

Print this for easy reference:

```
┌─────────────────────────────────────────────┐
│  TRAILGLASS "SILENT WATERS" COLOR PALETTE  │
├─────────────────────────────────────────────┤
│                                             │
│  Primary:     #9DB4C0  ████ Cool Steel     │
│  Secondary:   #5C6B73  ████ Blue Slate     │
│  Background:  #E0FBFC  ████ Light Cyan     │
│  Dark BG:     #253237  ████ Jet Black      │
│                                             │
│  Success:     #8BB5A1  ████ Sea Glass      │
│  Warning:     #C9B896  ████ Driftwood      │
│                                             │
│  Route Active:#7A9CAF  ████ Coastal Path   │
│  Route Past:  #5C8AA8  ████ Harbor Blue    │
│                                             │
│  Categories:                                │
│    Water:     #A8D5D8  ████ Seafoam Tint   │
│    Evening:   #8A90A6  ████ Dusk Purple    │
│    Morning:   #D4B5A8  ████ Sunrise Peach  │
│                                             │
└─────────────────────────────────────────────┘
```

---

## Links

- **Full Prompt**: [kmp-mockup-prompt.md](./kmp-mockup-prompt.md)
- **Detailed Guide**: [prompt-usage-guide.md](./prompt-usage-guide.md)
- **Color Reference**: [color-scheme-reference.md](./color-scheme-reference.md)
- **Variations**: [prompt-variations.md](./prompt-variations.md)
- **Gemini Studio**: https://aistudio.google.com/

---

## Support

If you encounter issues or have questions:

1. Check [prompt-usage-guide.md](./prompt-usage-guide.md) troubleshooting section
2. Review [prompt-variations.md](./prompt-variations.md) for examples
3. Verify colors in [color-scheme-reference.md](./color-scheme-reference.md)
4. Open an issue on the repository

---

**Last Updated**: 2025-11-23
