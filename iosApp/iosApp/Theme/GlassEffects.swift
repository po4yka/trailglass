import SwiftUI

// MARK: - Liquid Glass Effects
// iOS 26 Liquid Glass design system with translucent materials,
// real-time lensing, and Silent Waters color integration

// MARK: - Glass Material

enum GlassMaterial {
    case ultraThin
    case thin
    case regular
    case thick
    case clear

    var opacity: Double {
        switch self {
        case .ultraThin: return 0.25
        case .thin: return 0.3
        case .regular: return 0.45
        case .thick: return 0.6
        case .clear: return 0.35
        }
    }

    var blurRadius: CGFloat {
        switch self {
        case .ultraThin: return 8
        case .thin: return 12
        case .regular: return 16
        case .thick: return 24
        case .clear: return 12
        }
    }
}

// MARK: - Glass Variant

enum GlassVariant {
    case regular
    case clear
    case thick
    case ultraThin
    case tinted(Color, opacity: Double)

    // Predefined Silent Waters glass variants
    case primary        // Cool Steel tint
    case success        // Sea Glass tint
    case warning        // Driftwood tint
    case surface        // Light Cyan tint
    case active         // Light Blue tint
    case secondary      // Blue Slate tint
}

// MARK: - Glass Effect View Modifier

struct GlassEffect: ViewModifier {
    let variant: GlassVariant
    @Environment(\.colorScheme) var colorScheme

    private var blurRadius: CGFloat {
        switch variant {
        case .ultraThin:
            return 8
        case .clear:
            return 12
        case .regular, .tinted, .primary, .success, .warning, .surface, .active, .secondary:
            return 16
        case .thick:
            return 24
        }
    }

    private var backgroundOpacity: Double {
        switch variant {
        case .ultraThin:
            return colorScheme == .dark ? 0.15 : 0.25
        case .clear:
            return colorScheme == .dark ? 0.25 : 0.35
        case .regular:
            return colorScheme == .dark ? 0.35 : 0.45
        case .thick:
            return colorScheme == .dark ? 0.5 : 0.6
        case .tinted(_, let opacity):
            return opacity
        case .primary, .success, .warning, .surface, .active, .secondary:
            return colorScheme == .dark ? 0.7 : 0.6
        }
    }

    private var tintColor: Color {
        switch variant {
        case .primary:
            return .coolSteel
        case .success:
            return .seaGlass
        case .warning:
            return .driftwood
        case .surface:
            return .lightCyan
        case .active:
            return .lightBlue
        case .secondary:
            return .blueSlate
        case .tinted(let color, _):
            return color
        case .regular, .clear, .thick, .ultraThin:
            return colorScheme == .dark ? .white : .black
        }
    }

    func body(content: Content) -> some View {
        content
            .background(
                GlassMaterialBackground(
                    blurRadius: blurRadius,
                    tintColor: tintColor,
                    opacity: backgroundOpacity
                )
            )
    }
}

// MARK: - Glass Material Background

private struct GlassMaterialBackground: View {
    let blurRadius: CGFloat
    let tintColor: Color
    let opacity: Double
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        ZStack {
            // Base blur layer
            Rectangle()
                .fill(.ultraThinMaterial)
                .blur(radius: blurRadius * 0.5)

            // Tinted glass layer
            Rectangle()
                .fill(tintColor.opacity(opacity))
                .blur(radius: blurRadius * 0.3)

            // Shimmer overlay for depth
            LinearGradient(
                colors: [
                    Color.white.opacity(colorScheme == .dark ? 0.08 : 0.15),
                    Color.clear,
                    Color.white.opacity(colorScheme == .dark ? 0.05 : 0.1)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
    }
}

// MARK: - Interactive Glass Effect

struct InteractiveGlassEffect: ViewModifier {
    @State private var isPressed = false
    @State private var shimmerOffset: CGFloat = -1
    @Environment(\.colorScheme) var colorScheme

    func body(content: Content) -> some View {
        content
            .scaleEffect(isPressed ? 0.97 : 1.0)
            .overlay(
                // Shimmer effect on press
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [
                                Color.clear,
                                Color.white.opacity(colorScheme == .dark ? 0.2 : 0.3),
                                Color.clear
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .offset(x: shimmerOffset * 300)
                    .opacity(isPressed ? 1 : 0)
            )
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { _ in
                        withAnimation(MotionConfig.quickSpring) {
                            isPressed = true
                        }
                        withAnimation(MotionConfig.expressiveSpring.delay(0.1)) {
                            shimmerOffset = 1
                        }
                    }
                    .onEnded { _ in
                        withAnimation(MotionConfig.bouncySpring) {
                            isPressed = false
                        }
                        shimmerOffset = -1
                    }
            )
    }
}

// MARK: - Glass Effect Container

struct GlassEffectContainer<Content: View>: View {
    let variant: GlassVariant
    let cornerRadius: CGFloat
    let shadowRadius: CGFloat
    let content: Content

    @Environment(\.colorScheme) var colorScheme

    init(
        variant: GlassVariant = .regular,
        cornerRadius: CGFloat = 16,
        shadowRadius: CGFloat = 8,
        @ViewBuilder content: () -> Content
    ) {
        self.variant = variant
        self.cornerRadius = cornerRadius
        self.shadowRadius = shadowRadius
        self.content = content()
    }

    var body: some View {
        content
            .glassEffect(variant: variant)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .shadow(
                color: Color.black.opacity(colorScheme == .dark ? 0.4 : 0.1),
                radius: shadowRadius,
                x: 0,
                y: shadowRadius / 2
            )
    }
}

// MARK: - View Extensions

extension View {
    func glassEffect(variant: GlassVariant = .regular) -> some View {
        self.modifier(GlassEffect(variant: variant))
    }

    func glassEffectInteractive() -> some View {
        self.modifier(InteractiveGlassEffect())
    }

    func glassEffectTinted(_ color: Color, opacity: Double = 0.6) -> some View {
        self.modifier(GlassEffect(variant: .tinted(color, opacity: opacity)))
    }

    func glassContainer(
        variant: GlassVariant = .regular,
        cornerRadius: CGFloat = 16,
        shadowRadius: CGFloat = 8
    ) -> some View {
        GlassEffectContainer(
            variant: variant,
            cornerRadius: cornerRadius,
            shadowRadius: shadowRadius
        ) {
            self
        }
    }
}

// MARK: - Lensing Effect

struct LensingEffect: ViewModifier {
    let strength: CGFloat
    @State private var lensPosition: CGPoint = .zero
    @State private var isActive = false

    func body(content: Content) -> some View {
        content
            .background(
                GeometryReader { _ in
                    ZStack {
                        if isActive {
                            Circle()
                                .fill(
                                    RadialGradient(
                                        colors: [
                                            Color.white.opacity(0.3),
                                            Color.white.opacity(0.1),
                                            Color.clear
                                        ],
                                        center: .center,
                                        startRadius: 0,
                                        endRadius: 80 * strength
                                    )
                                )
                                .frame(width: 160 * strength, height: 160 * strength)
                                .position(lensPosition)
                                .blur(radius: 20)
                        }
                    }
                    .gesture(
                        DragGesture(minimumDistance: 0)
                            .onChanged { value in
                                withAnimation(MotionConfig.quickSpring) {
                                    lensPosition = value.location
                                    isActive = true
                                }
                            }
                            .onEnded { _ in
                                withAnimation(MotionConfig.standardSpring) {
                                    isActive = false
                                }
                            }
                    )
                }
            )
    }
}

extension View {
    func lensingEffect(strength: CGFloat = 1.0) -> some View {
        self.modifier(LensingEffect(strength: strength))
    }
}

// MARK: - Glass Border

struct GlassBorder: ViewModifier {
    let width: CGFloat
    let cornerRadius: CGFloat
    @Environment(\.colorScheme) var colorScheme

    func body(content: Content) -> some View {
        content
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(
                        LinearGradient(
                            colors: [
                                Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                                Color.white.opacity(colorScheme == .dark ? 0.1 : 0.2),
                                Color.clear
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: width
                    )
            )
    }
}

extension View {
    func glassBorder(width: CGFloat = 1, cornerRadius: CGFloat = 16) -> some View {
        self.modifier(GlassBorder(width: width, cornerRadius: cornerRadius))
    }
}
