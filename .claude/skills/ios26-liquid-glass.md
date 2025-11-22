---
name: ios26-liquid-glass
description: Expert guidance for implementing iOS 26 Liquid Glass design system with SwiftUI. Covers glass materials, lensing effects, spring animations, floating navigation, and interactive components following Apple's latest design language.
version: 1.0.0
type: project
tags:
  - ios
  - swiftui
  - liquid-glass
  - ui-design
  - animations
---

# iOS 26 Liquid Glass Design Expert

You are an expert in implementing iOS 26 Liquid Glass design system using SwiftUI. Liquid Glass was announced at WWDC 2025 (June 9) and represents Apple's unified visual theme across iOS, iPadOS, macOS, visionOS, and watchOS.

## Core Principles

### 1. Glass Materials
- Translucent material that reflects and refracts surrounding content
- Based on `.ultraThinMaterial` with custom tinting and effects
- Key visual properties: Lensing (real-time light bending, not blur), specular highlights, dynamic transparency
- Four standard variants: ultraThin (8pt blur), thin (12pt), regular (16pt), thick (24pt)
- Opacity range: 0.5-0.9 (adaptive for dark mode)

### 2. Lensing Effects
- Real-time light bending vs traditional blur that scatters light
- Implemented via custom path distortion and gradient overlays
- Subtle effect (2-5% distortion) for production use
- Performance: GPU-accelerated via Metal
- Use sparingly on complex views (limit to 3-5 active lensing elements)

### 3. Spring Animations
- Physics-based motion using `dampingFraction` and `stiffness`
- Response times: instant (0.2s), fast (0.3s), medium (0.4s), slow (0.6s), expressive (0.8s)
- Damping fractions: highBounce (0.5), mediumBounce (0.6), lowBounce (0.7), noBounce (1.0)
- Access via `MotionConfig.swift` standardized configurations
- Example: `Animation.spring(response: 0.4, dampingFraction: 0.7)`

### 4. Interactive States
- Press effects: 0.97x scale with shimmer sweep
- Hover effects: 1.05-1.08x scale with enhanced shadow (iPad/Mac)
- Selection: Color-matched emphasis with glass tinting
- Disabled: 0.5 opacity with reduced glass intensity

### 5. Dark Mode Adaptation
- Material opacity: Reduce 25% in dark mode (e.g., 0.8 → 0.6)
- Border opacity: Reduce 33% in dark mode
- Shimmer overlay: Reduce 50% in dark mode
- Shadow intensity: Increase 100% in dark mode (more depth)
- Always check `@Environment(\.colorScheme)` for adaptation

## Component Patterns

### Glass Effect Modifiers

```swift
extension View {
    func glassEffect(variant: GlassVariant = .regular) -> some View {
        self.modifier(GlassEffect(variant: variant))
    }

    func glassEffectInteractive() -> some View {
        self.modifier(InteractiveGlassEffect())
    }

    func glassEffectTinted(_ color: Color, opacity: Double = 0.6) -> some View {
        self.background(
            ZStack {
                Color.clear
                    .background(.ultraThinMaterial)
                color.opacity(opacity)
            }
            .cornerRadius(12)
        )
    }
}
```

### Glass Background Structure

```swift
struct GlassBackground: View {
    let material: GlassMaterial
    let tint: Color
    let cornerRadius: CGFloat

    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        ZStack {
            // Base material
            Color.clear
                .background(baseMaterial)

            // Tint overlay
            tint.opacity(tintOpacity)

            // Shimmer gradient
            LinearGradient(
                colors: [
                    Color.white.opacity(shimmerOpacity),
                    Color.white.opacity(shimmerOpacity * 0.3),
                    Color.clear
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            // Glass border
            RoundedRectangle(cornerRadius: cornerRadius)
                .strokeBorder(
                    LinearGradient(
                        colors: [
                            Color.white.opacity(borderOpacity),
                            Color.white.opacity(borderOpacity * 0.3)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1
                )
        }
        .cornerRadius(cornerRadius)
    }

    private var baseMaterial: Material {
        switch material {
        case .ultraThin: return .ultraThinMaterial
        case .thin: return .thinMaterial
        case .regular: return .regularMaterial
        case .thick: return .thickMaterial
        }
    }

    private var tintOpacity: Double {
        colorScheme == .dark ? 0.5 : 0.7
    }

    private var shimmerOpacity: Double {
        colorScheme == .dark ? 0.05 : 0.10
    }

    private var borderOpacity: Double {
        colorScheme == .dark ? 0.2 : 0.3
    }
}
```

### Interactive Shimmer Effect

```swift
struct ShimmerEffect: ViewModifier {
    let isActive: Bool
    @State private var phase: CGFloat = 0

    func body(content: Content) -> some View {
        content.overlay(
            GeometryReader { geometry in
                if isActive {
                    LinearGradient(
                        colors: [
                            Color.white.opacity(0.0),
                            Color.white.opacity(0.5),
                            Color.white.opacity(0.0)
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: 100)
                    .offset(x: phase)
                    .onAppear {
                        withAnimation(
                            .linear(duration: 1.5)
                            .repeatForever(autoreverses: false)
                        ) {
                            phase = geometry.size.width + 100
                        }
                    }
                }
            }
        )
        .clipped()
    }
}
```

### Floating Tab Bar

```swift
struct FloatingTabBar: View {
    @Binding var selectedTab: Int
    @State private var scrollOffset: CGFloat = 0

    private var isMinimized: Bool {
        scrollOffset > 50
    }

    private var tabBarHeight: CGFloat {
        isMinimized ? 56 : 72
    }

    var body: some View {
        VStack(spacing: 0) {
            // Content area
            TabContentView(selectedTab: selectedTab)
                .trackScrollOffset($scrollOffset)

            // Floating tab bar
            HStack(spacing: 0) {
                ForEach(tabs) { tab in
                    TabButton(
                        tab: tab,
                        isSelected: selectedTab == tab.id,
                        showLabel: !isMinimized
                    ) {
                        withAnimation(.spring(
                            response: 0.3,
                            dampingFraction: 0.75
                        )) {
                            selectedTab = tab.id
                        }
                    }
                }
            }
            .frame(height: tabBarHeight)
            .background(
                GlassBackground(
                    material: .regular,
                    tint: .coolSteel,
                    cornerRadius: 0
                )
                .opacity(isMinimized ? 0.95 : 0.85)
            )
            .animation(.spring(
                response: 0.35,
                dampingFraction: 0.75
            ), value: isMinimized)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
    }
}
```

### Flexible Navigation Bar

```swift
struct LargeFlexibleNavigationBar<Title: View, Subtitle: View, Background: View>: View {
    let title: Title
    let subtitle: Subtitle?
    let backgroundContent: Background?
    let scrollOffset: CGFloat

    private let expandedHeight: CGFloat = 180
    private let collapsedHeight: CGFloat = 64

    private var progress: CGFloat {
        min(max(scrollOffset / (expandedHeight - collapsedHeight), 0), 1)
    }

    private var currentHeight: CGFloat {
        expandedHeight - (progress * (expandedHeight - collapsedHeight))
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            // Background with parallax
            if let background = backgroundContent {
                background
                    .frame(height: expandedHeight)
                    .offset(y: scrollOffset * 0.5)
                    .opacity(1 - (progress * 0.3))
            }

            // Glass material overlay
            Color.clear
                .background(.ultraThinMaterial)
                .opacity(0.7 + (progress * 0.3))

            // Content
            VStack(spacing: 8) {
                Spacer()

                // Title
                title
                    .font(.system(
                        size: 34 - (progress * 14),
                        weight: .bold
                    ))
                    .lineLimit(1)

                // Subtitle (fades out on collapse)
                if let subtitle = subtitle {
                    subtitle
                        .font(.body)
                        .opacity(1 - min(progress * 2, 1))
                }

                Spacer().frame(height: 12)
            }
            .padding(.horizontal, 16)
        }
        .frame(height: currentHeight)
        .animation(.spring(
            response: 0.3,
            dampingFraction: 0.8
        ), value: scrollOffset)
    }
}
```

### Loading Indicators

```swift
// Wavy Spinner
struct WavyLoadingIndicator: View {
    @State private var rotation: Double = 0
    @State private var wavePhase: Double = 0

    var body: some View {
        ZStack {
            Circle()
                .trim(from: 0, to: 0.7)
                .stroke(
                    LinearGradient(
                        colors: [
                            color.opacity(0.8),
                            color.opacity(0.4),
                            color.opacity(0.1)
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    ),
                    style: StrokeStyle(
                        lineWidth: size * 0.1,
                        lineCap: .round
                    )
                )
                .rotationEffect(.degrees(rotation))

            // Wavy overlay
            WavyShape(phase: wavePhase)
                .stroke(color.opacity(0.3), lineWidth: 2)
        }
        .onAppear {
            withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                rotation = 360
            }
            withAnimation(.linear(duration: 2.0).repeatForever(autoreverses: false)) {
                wavePhase = 2 * .pi
            }
        }
    }
}

// Pulsing Glass
struct PulsingGlassIndicator: View {
    @State private var scales: [CGFloat] = [1.0, 1.0, 1.0]

    var body: some View {
        ZStack {
            ForEach(0..<3) { index in
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [
                                color.opacity(0.6 - Double(index) * 0.2),
                                color.opacity(0.2 - Double(index) * 0.1)
                            ],
                            center: .center,
                            startRadius: 0,
                            endRadius: size / 2
                        )
                    )
                    .frame(
                        width: size * [1.0, 0.7, 0.4][index],
                        height: size * [1.0, 0.7, 0.4][index]
                    )
                    .scaleEffect(scales[index])
                    .opacity(2 - scales[index])
            }
        }
        .onAppear {
            for index in 0..<3 {
                withAnimation(
                    .easeInOut(duration: 1.5)
                    .repeatForever(autoreverses: true)
                    .delay(Double(index) * 0.3)
                ) {
                    scales[index] = [1.2, 1.3, 1.4][index]
                }
            }
        }
    }
}
```

## File Organization

**Theme Files:**
- `Theme/GlassEffects.swift` - Glass modifiers and materials
- `Theme/MotionConfig.swift` - Spring animation configurations
- `Theme/GlassStyles.swift` - Reusable component styles
- `Theme/DarkModeSupport.swift` - Dark mode utilities
- `Theme/Color.swift` - Color palette with adaptive variants

**Component Files:**
- `Components/` - Reusable components
- Name pattern: `ComponentName.swift` (e.g., `TrackingFAB.swift`)
- Include `#Preview` for all components

**Screen Files:**
- `Screens/` - Feature screens
- Use `@StateObject` for ViewModels
- Integrate components with proper state management

## Best Practices

### Animations
- Always use MotionConfig spring specs
- Limit simultaneous animations (max 10 for 60fps)
- Use `@State` with animation modifiers for simple values
- Use `withAnimation` for coordinated transitions
- Profile with Instruments (Time Profiler, Core Animation)

### Colors
- Use adaptive color variants (`.adaptivePrimary`, `.adaptiveSuccess`)
- Never hardcode colors in views
- Support both light and dark modes via `@Environment(\.colorScheme)`
- Ensure WCAG AA contrast (4.5:1 normal, 3:1 large)

### Performance
- Glass effects use GPU acceleration (Metal)
- Limit active lensing to 3-5 elements
- Use `@State` sparingly, prefer `@Binding` for child views
- Avoid expensive calculations in `body` (use `@State` or computed properties)
- Test on real devices (simulator doesn't show true performance)

### Accessibility
- All interactive elements ≥ 44pt touch target
- Provide proper labels (`.accessibilityLabel`)
- Support Dynamic Type (use `.font(.system(...))`)
- Respect `@Environment(\.accessibilityReduceTransparency)`
- Respect `@Environment(\.accessibilityReduceMotion)`
- Test with VoiceOver enabled

## Dark Mode Implementation

```swift
struct DarkModeConfig {
    static func glassOpacity(for colorScheme: ColorScheme) -> Double {
        colorScheme == .dark ? 0.6 : 0.8  // 25% reduction
    }

    static func borderOpacity(for colorScheme: ColorScheme) -> Double {
        colorScheme == .dark ? 0.2 : 0.3  // 33% reduction
    }

    static func shimmerOpacity(for colorScheme: ColorScheme) -> Double {
        colorScheme == .dark ? 0.05 : 0.10  // 50% reduction
    }

    static func shadowOpacity(for colorScheme: ColorScheme) -> Double {
        colorScheme == .dark ? 0.4 : 0.2  // 100% increase
    }
}

extension View {
    func adaptiveShadow(radius: CGFloat, colorScheme: ColorScheme) -> some View {
        self.shadow(
            color: .black.opacity(DarkModeConfig.shadowOpacity(for: colorScheme)),
            radius: radius
        )
    }
}
```

## Common Patterns

### Interactive Button with Glass
```swift
struct GlassButton: View {
    let title: String
    let action: () -> Void
    @State private var isPressed = false

    var body: some View {
        Button(action: action) {
            Text(title)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
        }
        .buttonStyle(GlassButtonStyle(isPressed: $isPressed))
    }
}

struct GlassButtonStyle: ButtonStyle {
    @Binding var isPressed: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .background(
                GlassBackground(
                    material: .regular,
                    tint: .adaptivePrimary,
                    cornerRadius: 12
                )
            )
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(
                response: 0.3,
                dampingFraction: 0.6
            ), value: configuration.isPressed)
            .onChange(of: configuration.isPressed) { pressed in
                isPressed = pressed
            }
    }
}
```

### Shape Morphing
```swift
@State private var currentShape: GlassShape = .circle

let shape = MorphingGlassShape(
    startShape: previousShape,
    endShape: currentShape,
    progress: morphProgress
)

Circle()
    .fill(Color.adaptivePrimary)
    .clipShape(shape)
    .animation(.spring(
        response: 0.55,
        dampingFraction: 0.68
    ), value: currentShape)
```

## iOS Version Compatibility

**iOS 26+ (Full Support):**
- Complete Liquid Glass with lensing
- All glass materials and effects
- Real-time device motion response

**iOS 24-25 (Graceful Fallback):**
- Use `.ultraThinMaterial` as base
- Simplified glass effects (no lensing)
- Standard blur instead of refraction

**Minimum iOS 24.0 recommended**

## Quick Reference

**Apply Glass Effect:**
```swift
.glassEffect(variant: .regular)
.glassEffectTinted(.coolSteel, opacity: 0.6)
.glassEffectInteractive()
```

**Spring Animation:**
```swift
.animation(.spring(
    response: 0.4,
    dampingFraction: 0.7
), value: state)
```

**Adaptive Color:**
```swift
Color.adaptivePrimary
Color.adaptiveSuccess
```

**Dark Mode Check:**
```swift
@Environment(\.colorScheme) var colorScheme
let opacity = colorScheme == .dark ? 0.6 : 0.8
```

---

When implementing Liquid Glass components, always prioritize:
1. Glass materials with proper opacity adaptation
2. Spring-based motion (never linear)
3. Interactive feedback (shimmer, scale)
4. Dark mode support
5. Accessibility compliance (WCAG AA, VoiceOver, Reduce Transparency/Motion)
