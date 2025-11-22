import SwiftUI

// MARK: - Dark Mode Configuration
// Helper utilities for adapting Liquid Glass components to dark mode
// Ensures consistent visual quality across light and dark appearances

struct DarkModeConfig {
    // MARK: - Glass Material Opacity

    /// Returns appropriate glass opacity based on color scheme
    /// - Light mode: Higher transparency (0.7-0.9) for lighter feel
    /// - Dark mode: Lower transparency (0.5-0.7) for better contrast
    static func glassOpacity(for colorScheme: ColorScheme, material: GlassMaterial = .regular) -> Double {
        let baseOpacity = material.opacity

        switch colorScheme {
        case .light:
            return baseOpacity
        case .dark:
            // Reduce opacity in dark mode for better contrast
            return baseOpacity * 0.75
        @unknown default:
            return baseOpacity
        }
    }

    // MARK: - Border Opacity

    /// Returns appropriate border opacity based on color scheme
    /// - Light mode: 30-50% opacity
    /// - Dark mode: 20-30% opacity (subtler borders)
    static func borderOpacity(for colorScheme: ColorScheme, isSelected: Bool = false) -> Double {
        switch colorScheme {
        case .light:
            return isSelected ? 0.5 : 0.3
        case .dark:
            return isSelected ? 0.3 : 0.2
        @unknown default:
            return 0.3
        }
    }

    // MARK: - Shimmer Intensity

    /// Returns appropriate shimmer overlay opacity based on color scheme
    /// - Light mode: 10-15% white overlay
    /// - Dark mode: 5-8% white overlay (more subtle)
    static func shimmerOpacity(for colorScheme: ColorScheme, style: ShimmerStyle = .medium) -> Double {
        let baseOpacity = style.opacity

        switch colorScheme {
        case .light:
            return baseOpacity
        case .dark:
            // More subtle shimmer in dark mode
            return baseOpacity * 0.5
        @unknown default:
            return baseOpacity
        }
    }

    // MARK: - Shadow Configuration

    /// Returns appropriate shadow opacity based on color scheme
    /// - Light mode: 10-20% opacity (subtle shadows)
    /// - Dark mode: 30-40% opacity (more pronounced for depth)
    static func shadowOpacity(for colorScheme: ColorScheme, elevation: CGFloat = 2) -> Double {
        let baseOpacity: Double

        switch elevation {
        case 0...1:
            baseOpacity = 0.1
        case 1...2:
            baseOpacity = 0.15
        case 2...3:
            baseOpacity = 0.2
        default:
            baseOpacity = 0.25
        }

        switch colorScheme {
        case .light:
            return baseOpacity
        case .dark:
            // Increase shadow intensity in dark mode for better depth perception
            return baseOpacity * 2.0
        @unknown default:
            return baseOpacity
        }
    }

    /// Returns appropriate shadow color based on color scheme
    static func shadowColor(for colorScheme: ColorScheme, tint: Color? = nil) -> Color {
        switch colorScheme {
        case .light:
            return tint ?? Color.black
        case .dark:
            // Use tinted shadows in dark mode for better visual hierarchy
            return tint ?? Color.black
        @unknown default:
            return Color.black
        }
    }

    // MARK: - Material Tint Adjustment

    /// Adjusts color intensity for dark mode compatibility
    /// Ensures colors remain vibrant but not overwhelming in dark environments
    static func adjustColorForDarkMode(_ color: Color, colorScheme: ColorScheme, intensity: Double = 1.0) -> Color {
        switch colorScheme {
        case .light:
            return color
        case .dark:
            // Slightly desaturate and brighten colors in dark mode
            return color.opacity(0.9 + (0.1 * intensity))
        @unknown default:
            return color
        }
    }

    // MARK: - Gradient Overlay Opacity

    /// Returns appropriate gradient overlay opacity for glass surfaces
    static func gradientOverlayOpacity(for colorScheme: ColorScheme, position: GradientPosition = .top) -> Double {
        switch (colorScheme, position) {
        case (.light, .top):
            return 0.2
        case (.light, .bottom):
            return 0.05
        case (.dark, .top):
            return 0.15
        case (.dark, .bottom):
            return 0.03
        @unknown default:
            return 0.1
        }
    }

    enum GradientPosition {
        case top
        case bottom
    }

    // MARK: - Text Contrast

    /// Returns appropriate text color for glass backgrounds
    static func textColor(for colorScheme: ColorScheme, isSecondary: Bool = false) -> Color {
        switch (colorScheme, isSecondary) {
        case (.light, false):
            return .primary
        case (.light, true):
            return .secondary
        case (.dark, false):
            return .primary
        case (.dark, true):
            return .secondary
        @unknown default:
            return .primary
        }
    }

    // MARK: - Interactive State Adjustments

    /// Returns scale factor for pressed state
    static let pressScale: CGFloat = 0.97

    /// Returns opacity for disabled state
    static func disabledOpacity(for colorScheme: ColorScheme) -> Double {
        switch colorScheme {
        case .light:
            return 0.5
        case .dark:
            return 0.4
        @unknown default:
            return 0.5
        }
    }

    // MARK: - Background Blur Intensity

    /// Returns appropriate blur radius based on color scheme and material
    static func blurRadius(for colorScheme: ColorScheme, material: GlassMaterial = .regular) -> CGFloat {
        let baseBlur = material.blurRadius

        switch colorScheme {
        case .light:
            return baseBlur
        case .dark:
            // Slightly increase blur in dark mode for better glass effect
            return baseBlur * 1.1
        @unknown default:
            return baseBlur
        }
    }

    // MARK: - Color Adaptation Helpers

    /// Creates an adaptive linear gradient for glass borders
    static func adaptiveBorderGradient(for colorScheme: ColorScheme, tint: Color? = nil) -> LinearGradient {
        let topOpacity = borderOpacity(for: colorScheme, isSelected: true)
        let bottomOpacity = borderOpacity(for: colorScheme, isSelected: false)

        if let tint = tint {
            return LinearGradient(
                colors: [
                    tint.opacity(topOpacity),
                    tint.opacity(bottomOpacity)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        } else {
            return LinearGradient(
                colors: [
                    Color.white.opacity(topOpacity),
                    Color.white.opacity(bottomOpacity)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
    }

    /// Creates an adaptive shimmer gradient
    static func adaptiveShimmerGradient(for colorScheme: ColorScheme, style: ShimmerStyle = .medium) -> LinearGradient {
        let opacity = shimmerOpacity(for: colorScheme, style: style)

        return LinearGradient(
            colors: [
                Color.white.opacity(0),
                Color.white.opacity(opacity),
                Color.white.opacity(0)
            ],
            startPoint: .leading,
            endPoint: .trailing
        )
    }

    /// Creates an adaptive surface gradient for glass backgrounds
    static func adaptiveSurfaceGradient(for colorScheme: ColorScheme, tint: Color? = nil) -> LinearGradient {
        let topOpacity = gradientOverlayOpacity(for: colorScheme, position: .top)
        let bottomOpacity = gradientOverlayOpacity(for: colorScheme, position: .bottom)

        let color = tint ?? Color.white

        return LinearGradient(
            colors: [
                color.opacity(topOpacity),
                color.opacity(bottomOpacity)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
}

// MARK: - View Extensions for Dark Mode

extension View {
    /// Applies dark mode aware glass shadow
    func adaptiveShadow(
        colorScheme: ColorScheme,
        elevation: CGFloat = 2,
        tint: Color? = nil
    ) -> some View {
        let opacity = DarkModeConfig.shadowOpacity(for: colorScheme, elevation: elevation)
        let color = DarkModeConfig.shadowColor(for: colorScheme, tint: tint)
        let radius = elevation * 2

        return self.shadow(
            color: color.opacity(opacity),
            radius: radius,
            x: 0,
            y: elevation
        )
    }

    /// Applies dark mode aware border
    func adaptiveBorder(
        colorScheme: ColorScheme,
        tint: Color? = nil,
        cornerRadius: CGFloat = 12,
        lineWidth: CGFloat = 1,
        isSelected: Bool = false
    ) -> some View {
        self.overlay(
            RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                .strokeBorder(
                    DarkModeConfig.adaptiveBorderGradient(for: colorScheme, tint: tint),
                    lineWidth: isSelected ? lineWidth * 1.5 : lineWidth
                )
        )
    }

    /// Applies dark mode aware shimmer effect
    func adaptiveShimmer(
        colorScheme: ColorScheme,
        style: ShimmerStyle = .medium,
        isActive: Bool = true
    ) -> some View {
        self.overlay(
            GeometryReader { geometry in
                if isActive {
                    ShimmerView(
                        colorScheme: colorScheme,
                        style: style,
                        width: geometry.size.width
                    )
                }
            }
            .clipped()
        )
    }
}

// MARK: - Adaptive Shimmer View

private struct ShimmerView: View {
    let colorScheme: ColorScheme
    let style: ShimmerStyle
    let width: CGFloat

    @State private var offset: CGFloat = -200

    var body: some View {
        Rectangle()
            .fill(DarkModeConfig.adaptiveShimmerGradient(for: colorScheme, style: style))
            .frame(width: style.width)
            .rotationEffect(style.angle)
            .offset(x: offset)
            .onAppear {
                withAnimation(
                    .linear(duration: style.duration)
                    .repeatForever(autoreverses: false)
                ) {
                    offset = width + 200
                }
            }
    }
}

// MARK: - Dark Mode Preview Helper

#if DEBUG
/// Preview helper to compare light and dark mode side by side
struct DarkModeComparison<Content: View>: View {
    let content: () -> Content

    init(@ViewBuilder content: @escaping () -> Content) {
        self.content = content
    }

    var body: some View {
        HStack(spacing: 0) {
            // Light mode
            VStack {
                Text("Light Mode")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.top, 8)

                content()
                    .environment(\.colorScheme, .light)
                    .background(Color.backgroundLight)
            }
            .frame(maxWidth: .infinity)

            Divider()

            // Dark mode
            VStack {
                Text("Dark Mode")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.top, 8)

                content()
                    .environment(\.colorScheme, .dark)
                    .background(Color.backgroundDark)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

extension View {
    /// Wraps view in dark mode comparison preview
    func darkModeComparison() -> some View {
        DarkModeComparison {
            self
        }
    }
}
#endif
