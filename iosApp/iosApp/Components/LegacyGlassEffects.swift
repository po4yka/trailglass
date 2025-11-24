import SwiftUI

// MARK: - Glass Effects for Liquid Glass Design
// iOS 26 Liquid Glass design system - Interactive glass materials with shimmer, blur, and dynamic tinting

/// Glass effect configuration for different material types
enum GlassMaterial {
    case ultraThin
    case thin
    case regular
    case thick
    case chrome

    var blurRadius: CGFloat {
        switch self {
        case .ultraThin: return 8
        case .thin: return 12
        case .regular: return 20
        case .thick: return 30
        case .chrome: return 40
        }
    }

    var opacity: Double {
        switch self {
        case .ultraThin: return 0.6
        case .thin: return 0.7
        case .regular: return 0.8
        case .thick: return 0.85
        case .chrome: return 0.9
        }
    }
}

/// Shimmer effect configuration
enum ShimmerStyle {
    case subtle
    case medium
    case prominent
    case interactive

    var angle: Angle {
        switch self {
        case .subtle, .medium: return .degrees(45)
        case .prominent, .interactive: return .degrees(60)
        }
    }

    var width: CGFloat {
        switch self {
        case .subtle: return 30
        case .medium: return 50
        case .prominent: return 80
        case .interactive: return 100
        }
    }

    var opacity: Double {
        switch self {
        case .subtle: return 0.2
        case .medium: return 0.3
        case .prominent: return 0.4
        case .interactive: return 0.5
        }
    }

    var duration: Double {
        switch self {
        case .subtle: return 3.0
        case .medium: return 2.5
        case .prominent: return 2.0
        case .interactive: return 1.5
        }
    }
}

// MARK: - Glass Background Modifier

struct GlassBackground: ViewModifier {
    let material: GlassMaterial
    let tint: Color?
    let cornerRadius: CGFloat
    @Environment(\.colorScheme) var colorScheme

    func body(content: Content) -> some View {
        content
            .background(
                ZStack {
                    // Base glass layer with dark mode adaptation
                    RoundedRectangle(cornerRadius: cornerRadius)
                        .fill(Color.white.opacity(DarkModeConfig.glassOpacity(for: colorScheme, material: material) * 0.15))
                        .background(
                            .ultraThinMaterial,
                            in: RoundedRectangle(cornerRadius: cornerRadius)
                        )

                    // Tint layer
                    if let tint = tint {
                        RoundedRectangle(cornerRadius: cornerRadius)
                            .fill(tint.opacity(colorScheme == .dark ? 0.15 : 0.1))
                    }

                    // Adaptive gradient overlay
                    DarkModeConfig.adaptiveSurfaceGradient(for: colorScheme, tint: Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
                }
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .strokeBorder(
                        DarkModeConfig.adaptiveBorderGradient(for: colorScheme),
                        lineWidth: 1
                    )
            )
    }
}

// MARK: - Shimmer Effect Modifier

struct ShimmerEffect: ViewModifier {
    let style: ShimmerStyle
    let isActive: Bool
    @Environment(\.colorScheme) var colorScheme

    @State private var offset: CGFloat = -200

    func body(content: Content) -> some View {
        content
            .overlay(
                GeometryReader { geometry in
                    if isActive {
                        Rectangle()
                            .fill(
                                DarkModeConfig.adaptiveShimmerGradient(for: colorScheme, style: style)
                            )
                            .frame(width: style.width)
                            .rotationEffect(style.angle)
                            .offset(x: offset)
                            .onAppear {
                                withAnimation(
                                    .linear(duration: style.duration)
                                    .repeatForever(autoreverses: false)
                                ) {
                                    offset = geometry.size.width + 200
                                }
                            }
                    }
                }
                .clipped()
            )
    }
}

// MARK: - Interactive Glass Modifier

struct InteractiveGlass: ViewModifier {
    @State private var isPressed = false
    let tint: Color

    func body(content: Content) -> some View {
        content
            .scaleEffect(isPressed ? 0.97 : 1.0)
            .brightness(isPressed ? 0.05 : 0)
            .overlay(
                GeometryReader { _ in
                    if isPressed {
                        ShimmerOverlay(tint: tint)
                    }
                }
            )
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { _ in
                        if !isPressed {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                                isPressed = true
                            }
                        }
                    }
                    .onEnded { _ in
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                            isPressed = false
                        }
                    }
            )
    }
}

private struct ShimmerOverlay: View {
    let tint: Color
    @State private var offset: CGFloat = -100

    var body: some View {
        GeometryReader { geometry in
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [
                            tint.opacity(0),
                            tint.opacity(0.3),
                            tint.opacity(0)
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(width: 80)
                .rotationEffect(.degrees(45))
                .offset(x: offset)
                .onAppear {
                    withAnimation(.easeInOut(duration: 0.4)) {
                        offset = geometry.size.width + 100
                    }
                }
        }
        .clipped()
    }
}

// MARK: - View Extension

extension View {
    /// Apply glass background with optional tint
    func glassBackground(
        material: GlassMaterial = .regular,
        tint: Color? = nil,
        cornerRadius: CGFloat = 12
    ) -> some View {
        modifier(GlassBackground(material: material, tint: tint, cornerRadius: cornerRadius))
    }

    /// Apply shimmer effect
    func shimmer(
        style: ShimmerStyle = .medium,
        isActive: Bool = true
    ) -> some View {
        modifier(ShimmerEffect(style: style, isActive: isActive))
    }

    /// Apply interactive glass effect (press with shimmer)
    func interactive(tint: Color = .blue) -> some View {
        modifier(InteractiveGlass(tint: tint))
    }
}

// MARK: - Glass Shadow

struct GlassShadow: ViewModifier {
    let elevation: CGFloat
    @Environment(\.colorScheme) var colorScheme

    var shadowRadius: CGFloat {
        switch elevation {
        case 0...1: return 2
        case 1...2: return 4
        case 2...3: return 8
        default: return 12
        }
    }

    func body(content: Content) -> some View {
        let opacity = DarkModeConfig.shadowOpacity(for: colorScheme, elevation: elevation)

        return content
            .shadow(
                color: Color.black.opacity(opacity),
                radius: shadowRadius,
                x: 0,
                y: elevation
            )
    }
}

extension View {
    /// Apply glass shadow based on elevation
    func glassShadow(elevation: CGFloat = 2) -> some View {
        modifier(GlassShadow(elevation: elevation))
    }
}

// MARK: - Wavy Distortion (for loading indicators)

struct WavyShape: Shape {
    var phase: CGFloat
    var frequency: Double = 2.0
    var amplitude: CGFloat = 10

    var animatableData: CGFloat {
        get { phase }
        set { phase = newValue }
    }

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        let midHeight = height / 2

        path.move(to: CGPoint(x: 0, y: midHeight))

        for x in stride(from: 0, to: width, by: 1) {
            let relativeX = x / width
            let sine = sin((relativeX * frequency * .pi * 2) + phase)
            let y = midHeight + (sine * amplitude)
            path.addLine(to: CGPoint(x: x, y: y))
        }

        return path
    }
}
