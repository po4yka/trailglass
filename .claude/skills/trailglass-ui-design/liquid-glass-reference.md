# Liquid Glass Design Reference

Detailed reference for implementing Liquid Glass aesthetic in Trailglass iOS app.

## Visual Philosophy

Liquid Glass is characterized by:
- **Depth through layering** - Multiple translucent planes
- **Blur and translucency** - Materials that show content behind
- **Soft gradients** - Never harsh or neon
- **Fluid motion** - Spring-based, physics-driven animations
- **Atmospheric lighting** - Subtle shadows and glows

## Material System

### iOS Materials

SwiftUI provides built-in materials for glassmorphic effects:

```swift
// Material hierarchy (thinnest to thickest)
.ultraThinMaterial     // Most transparent, shows background clearly
.thinMaterial          // Slightly more opaque
.regularMaterial       // Balanced translucency
.thickMaterial         // More opaque
.ultraThickMaterial    // Least transparent

// Usage
VStack {
    // Content
}
.background(.ultraThinMaterial)
```

### Custom Glass Effects

Create custom glassmorphic backgrounds:

```swift
struct GlassCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Content
        }
        .padding(20)
        .background {
            ZStack {
                // Gradient layer
                LinearGradient(
                    colors: [
                        Color.white.opacity(0.25),
                        Color.white.opacity(0.10)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )

                // Material blur
                Rectangle()
                    .fill(.ultraThinMaterial)
            }
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .overlay {
            // Glass border
            RoundedRectangle(cornerRadius: 20)
                .strokeBorder(
                    LinearGradient(
                        colors: [
                            Color.white.opacity(0.5),
                            Color.white.opacity(0.1)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1
                )
        }
        .shadow(color: .black.opacity(0.1), radius: 20, y: 10)
    }
}
```

### Atmospheric Backgrounds

Create depth with layered, blurred gradients:

```swift
struct AtmosphericBackground: View {
    var body: some View {
        ZStack {
            // Base layer
            Color.black

            // Atmospheric gradient
            LinearGradient(
                colors: [
                    Color.blue.opacity(0.3),
                    Color.purple.opacity(0.2),
                    Color.pink.opacity(0.1)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .blur(radius: 60)

            // Additional depth layers
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.blue.opacity(0.4),
                            Color.clear
                        ],
                        center: .topLeading,
                        startRadius: 0,
                        endRadius: 400
                    )
                )
                .blur(radius: 40)
                .offset(x: -100, y: -100)

            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.purple.opacity(0.3),
                            Color.clear
                        ],
                        center: .bottomTrailing,
                        startRadius: 0,
                        endRadius: 350
                    )
                )
                .blur(radius: 40)
                .offset(x: 100, y: 100)
        }
        .ignoresSafeArea()
    }
}
```

## Typography

### SF Pro Display/Rounded

```swift
// System font with design variants
Text("Paris")
    .font(.system(size: 32, weight: .bold, design: .rounded))

// Dynamic Type (scales with user preferences)
Text("Trip Timeline")
    .font(.largeTitle)
    .fontWeight(.bold)
    .fontDesign(.rounded)

// Custom font with Dynamic Type support
Text("Details")
    .font(.system(.body, design: .rounded))
    .fontWeight(.regular)
```

### Type Scale

```swift
// Display sizes
.largeTitle       // 34pt - Hero content
.title            // 28pt - Screen titles
.title2           // 22pt - Section headers
.title3           // 20pt - Subsection headers

// Body sizes
.headline         // 17pt bold - Emphasized
.body             // 17pt - Main content
.callout          // 16pt - Supporting text
.subheadline      // 15pt - Secondary content
.footnote         // 13pt - Captions
.caption          // 12pt - Fine print
.caption2         // 11pt - Smallest

// Custom sizes with Dynamic Type
.system(size: 24, weight: .bold, design: .rounded)
```

### Typography Hierarchy

```swift
struct VisitHeader: View {
    let visit: PlaceVisit

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Large, bold title
            Text(visit.city ?? "Unknown")
                .font(.system(size: 32, weight: .bold, design: .rounded))
                .foregroundStyle(.primary)

            // Small, light subtitle - strong contrast
            Text(formatDateRange(visit.startTime, visit.endTime))
                .font(.system(size: 14, weight: .regular))
                .foregroundStyle(.secondary)

            // Even smaller detail
            Text(visit.country ?? "")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
    }
}
```

## Color System

### Semantic Colors

iOS provides semantic colors that adapt to light/dark mode:

```swift
// Primary colors
Color.primary       // Main text
Color.secondary     // Secondary text
Color.tertiary      // Tertiary text

// UI element colors
Color.accentColor   // Accent/tint color
Color.blue, .purple, .pink  // Semantic colors
```

### Custom Color Palette

```swift
extension Color {
    // Liquid Glass specific colors
    static let glassBlue = Color(red: 0.4, green: 0.6, blue: 1.0)
    static let glassPurple = Color(red: 0.6, green: 0.4, blue: 1.0)
    static let glassPink = Color(red: 1.0, green: 0.4, blue: 0.7)

    // Glass overlays
    static let glassOverlay = Color.white.opacity(0.15)
    static let glassBorder = Color.white.opacity(0.3)
}
```

### Gradient Patterns

```swift
// Soft, atmospheric gradients
LinearGradient(
    colors: [
        Color.glassBlue.opacity(0.4),
        Color.glassPurple.opacity(0.2)
    ],
    startPoint: .topLeading,
    endPoint: .bottomTrailing
)

// Multi-stop gradients
LinearGradient(
    stops: [
        .init(color: .blue.opacity(0.3), location: 0.0),
        .init(color: .purple.opacity(0.2), location: 0.5),
        .init(color: .pink.opacity(0.1), location: 1.0)
    ],
    startPoint: .topLeading,
    endPoint: .bottomTrailing
)

// Radial gradients for depth
RadialGradient(
    colors: [
        Color.blue.opacity(0.4),
        Color.purple.opacity(0.2),
        Color.clear
    ],
    center: .center,
    startRadius: 0,
    endRadius: 300
)
```

## Motion & Animation

### Spring Animations

The signature of iOS - physics-based, natural motion:

```swift
// Standard spring
withAnimation(.spring()) {
    isExpanded.toggle()
}

// Custom spring parameters
withAnimation(
    .spring(
        response: 0.6,      // Duration-like parameter
        dampingFraction: 0.8,  // Bounciness (0=bouncy, 1=no bounce)
        blendDuration: 0
    )
) {
    offset = newOffset
}

// Smooth spring (no bounce)
withAnimation(.smooth) {
    scale = 1.0
}

// Bouncy spring
withAnimation(.bouncy) {
    showBadge = true
}
```

### Animation Curves

```swift
// Built-in curves
.easeIn
.easeOut
.easeInOut
.linear

// Custom timing
.timingCurve(0.4, 0.0, 0.2, 1.0)  // Similar to Material ease

// Spring is preferred over timing curves
```

### Transitions

```swift
// Basic transitions
.transition(.opacity)
.transition(.scale)
.transition(.move(edge: .trailing))
.transition(.slide)

// Combined transitions
.transition(
    .asymmetric(
        insertion: .move(edge: .trailing).combined(with: .opacity),
        removal: .move(edge: .leading).combined(with: .opacity)
    )
)

// Custom transitions
extension AnyTransition {
    static var liquidSlide: AnyTransition {
        .asymmetric(
            insertion: .move(edge: .trailing)
                .combined(with: .opacity)
                .combined(with: .scale(scale: 0.9)),
            removal: .move(edge: .leading)
                .combined(with: .opacity)
        )
    }
}
```

### Matched Geometry Effect

Hero transitions between views:

```swift
@Namespace private var animation

// Source view
RoundedRectangle(cornerRadius: 12)
    .fill(.blue)
    .frame(width: 100, height: 100)
    .matchedGeometryEffect(id: "card", in: animation)
    .onTapGesture {
        withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
            isExpanded = true
        }
    }

// Destination view
RoundedRectangle(cornerRadius: 20)
    .fill(.blue)
    .frame(width: 300, height: 400)
    .matchedGeometryEffect(id: "card", in: animation)
```

### Gesture-Driven Animations

```swift
@State private var offset: CGSize = .zero
@State private var isDragging = false

var drag: some Gesture {
    DragGesture()
        .onChanged { gesture in
            isDragging = true
            offset = gesture.translation
        }
        .onEnded { _ in
            isDragging = false
            withAnimation(.spring(response: 0.5, dampingFraction: 0.7)) {
                offset = .zero
            }
        }
}

// Apply to view
CardView()
    .offset(offset)
    .scaleEffect(isDragging ? 0.95 : 1.0)
    .gesture(drag)
```

## Layout & Spacing

### Safe Areas

Always respect safe areas on iOS:

```swift
ScrollView {
    VStack(spacing: 16) {
        // Content
    }
    .padding(.horizontal, 20)  // Screen padding
}
.safeAreaInset(edge: .bottom) {
    // Bottom bar that respects safe area
    BottomBar()
}

// Ignore safe area for backgrounds
ZStack {
    BackgroundGradient()
        .ignoresSafeArea()

    ContentView()
        .safeAreaPadding()  // Respect safe area
}
```

### Spacing Scale

```swift
// iOS standard spacing
4   // Minimal
8   // Tight
12  // Comfortable
16  // Standard
20  // Screen edges (iOS convention)
24  // Section separation
32  // Large gaps
```

### Stack Layouts

```swift
// Vertical stack
VStack(alignment: .leading, spacing: 12) {
    Text("Title")
    Text("Subtitle")
}

// Horizontal stack
HStack(alignment: .center, spacing: 16) {
    Icon()
    Text("Label")
    Spacer()
}

// Lazy stacks for performance
LazyVStack(spacing: 16) {
    ForEach(items) { item in
        ItemView(item: item)
    }
}
```

## Component Patterns

### Glass Navigation Bar

```swift
NavigationStack {
    ContentView()
        .navigationTitle("Timeline")
        .navigationBarTitleDisplayMode(.large)
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
}
```

### Glass Tab Bar

```swift
TabView {
    StatsView()
        .tabItem {
            Label("Stats", systemImage: "chart.bar.fill")
        }

    TimelineView()
        .tabItem {
            Label("Timeline", systemImage: "clock.fill")
        }

    MapView()
        .tabItem {
            Label("Map", systemImage: "map.fill")
        }
}
.toolbarBackground(.ultraThinMaterial, for: .tabBar)
```

### Floating Button

```swift
struct FloatingButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "plus")
                .font(.system(size: 24, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 56, height: 56)
                .background {
                    ZStack {
                        // Gradient background
                        LinearGradient(
                            colors: [.blue, .purple],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )

                        // Subtle overlay
                        Rectangle()
                            .fill(.white.opacity(0.15))
                    }
                    .clipShape(Circle())
                    .shadow(color: .blue.opacity(0.4), radius: 15, y: 8)
                }
        }
    }
}
```

### Sheet with Glass

```swift
.sheet(isPresented: $showSheet) {
    VStack(spacing: 20) {
        // Content
    }
    .padding(24)
    .presentationBackground(.ultraThinMaterial)
    .presentationCornerRadius(30)
    .presentationDragIndicator(.visible)
}
```

### Context Menu

```swift
.contextMenu {
    Button {
        // Edit action
    } label: {
        Label("Edit", systemImage: "pencil")
    }

    Button(role: .destructive) {
        // Delete action
    } label: {
        Label("Delete", systemImage: "trash")
    }
}
```

## Depth & Shadows

### Shadow Layers

```swift
// Subtle depth
.shadow(color: .black.opacity(0.1), radius: 10, y: 5)

// More pronounced
.shadow(color: .black.opacity(0.2), radius: 20, y: 10)

// Colored shadows for glass effect
.shadow(color: .blue.opacity(0.3), radius: 15, y: 8)

// Multiple shadows for depth
.shadow(color: .black.opacity(0.1), radius: 5, y: 2)
.shadow(color: .black.opacity(0.05), radius: 20, y: 10)
```

### Inner Shadows

Simulate with overlays:

```swift
.overlay {
    RoundedRectangle(cornerRadius: 20)
        .strokeBorder(
            LinearGradient(
                colors: [
                    .white.opacity(0.5),
                    .black.opacity(0.1)
                ],
                startPoint: .top,
                endPoint: .bottom
            ),
            lineWidth: 1
        )
}
```

## Accessibility

### Dynamic Type

Support dynamic text sizing:

```swift
// Automatically scales
Text("Important")
    .font(.body)

// Custom with scaling
Text("Custom")
    .font(.system(size: 16))
    .dynamicTypeSize(.large...(.accessibility3))
```

### VoiceOver

Add labels for accessibility:

```swift
Image(systemName: "location.fill")
    .accessibilityLabel("Location: Paris")

Button("") {
    // Action
}
.accessibilityLabel("Add trip")
.accessibilityHint("Creates a new trip entry")
```

### Reduce Motion

Respect motion preferences:

```swift
@Environment(\.accessibilityReduceMotion) var reduceMotion

func animateTransition() {
    if reduceMotion {
        // Instant transition
        isExpanded = true
    } else {
        withAnimation(.spring()) {
            isExpanded = true
        }
    }
}
```

## Resources

- [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui/)
- [SF Symbols](https://developer.apple.com/sf-symbols/)
- [Accessibility Guidelines](https://developer.apple.com/accessibility/)
