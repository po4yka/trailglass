import SwiftUI

/// Flexible navigation bar that collapses on scroll
/// Supports three variants: Large (180pt), Medium (128pt), Compact (64pt)
/// Features parallax background, fading subtitle, and glass material
struct FlexibleNavigationBar<Title: View, Subtitle: View, Background: View>: View {
    let title: Title
    let subtitle: Subtitle?
    let backgroundContent: Background?
    let scrollOffset: CGFloat
    let variant: NavigationBarVariant

    init(
        variant: NavigationBarVariant = .large,
        scrollOffset: CGFloat,
        @ViewBuilder title: () -> Title,
        @ViewBuilder subtitle: () -> Subtitle = { EmptyView() as! Subtitle },
        @ViewBuilder backgroundContent: () -> Background = { EmptyView() as! Background }
    ) {
        self.variant = variant
        self.scrollOffset = scrollOffset
        self.title = title()
        self.subtitle = subtitle()
        self.backgroundContent = backgroundContent()
    }

    private var maxHeight: CGFloat {
        variant.maxHeight
    }

    private var minHeight: CGFloat = 64

    private var currentHeight: CGFloat {
        max(minHeight, maxHeight + scrollOffset)
    }

    private var progress: CGFloat {
        let range = maxHeight - minHeight
        return max(0, min(1, -scrollOffset / range))
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            // Background with parallax effect
            if backgroundContent != nil {
                GeometryReader { geometry in
                    backgroundContent
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .offset(y: -scrollOffset * 0.5) // Parallax effect
                        .opacity(1 - progress)
                }
            }

            // Glass material that intensifies when collapsed
            GlassNavigationMaterial(intensity: 0.7 + (progress * 0.3))

            // Content
            VStack(spacing: 0) {
                Spacer()

                // Title and subtitle
                VStack(spacing: 4) {
                    title
                        .font(titleFont)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                        .lineLimit(1)

                    if subtitle != nil {
                        subtitle
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .opacity(1 - progress) // Fade out on collapse
                            .scaleEffect(1 - (progress * 0.2), anchor: .center)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 12)
            }
        }
        .frame(height: currentHeight)
        .animation(.spring(response: 0.3, dampingFraction: 0.8), value: currentHeight)
    }

    private var titleFont: Font {
        let collapsedSize: CGFloat = 20
        let expandedSize: CGFloat = variant == .large ? 34 : (variant == .medium ? 28 : 24)
        let size = expandedSize - ((expandedSize - collapsedSize) * progress)
        return .system(size: size, weight: .bold)
    }
}

/// Navigation bar variant sizes
enum NavigationBarVariant {
    case large
    case medium
    case compact

    var maxHeight: CGFloat {
        switch self {
        case .large: return 180
        case .medium: return 128
        case .compact: return 64
        }
    }
}

/// Glass material for navigation bar
private struct GlassNavigationMaterial: View {
    let intensity: Double

    var body: some View {
        ZStack {
            // Base color
            Color.lightCyan.opacity(0.4 * intensity)

            // Blur effect
            if #available(iOS 15.0, *) {
                Rectangle()
                    .fill(.ultraThinMaterial)
                    .opacity(intensity)
            } else {
                BlurView(style: .systemUltraThinMaterial)
                    .opacity(intensity)
            }

            // Subtle gradient overlay
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.lightCyan.opacity(0.2),
                    Color.coolSteel.opacity(0.15)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .blendMode(.overlay)
        }
    }
}

/// Large flexible navigation bar (180pt height)
/// Ideal for main screens with hero content
struct LargeFlexibleNavigationBar<Subtitle: View, Background: View>: View {
    let title: String
    let subtitle: Subtitle?
    let backgroundContent: Background?
    let scrollOffset: CGFloat
    let actions: [NavigationAction]?

    init(
        title: String,
        scrollOffset: CGFloat,
        actions: [NavigationAction]? = nil,
        @ViewBuilder subtitle: () -> Subtitle = { EmptyView() as! Subtitle },
        @ViewBuilder backgroundContent: () -> Background = { EmptyView() as! Background }
    ) {
        self.title = title
        self.scrollOffset = scrollOffset
        self.actions = actions
        self.subtitle = subtitle()
        self.backgroundContent = backgroundContent()
    }

    var body: some View {
        ZStack(alignment: .top) {
            FlexibleNavigationBar(
                variant: .large,
                scrollOffset: scrollOffset,
                title: { Text(title) },
                subtitle: { subtitle },
                backgroundContent: { backgroundContent }
            )

            // Action buttons overlay
            if let actions = actions {
                HStack {
                    Spacer()
                    HStack(spacing: 16) {
                        ForEach(actions) { action in
                            Button(action: action.action) {
                                Image(systemName: action.icon)
                                    .font(.system(size: 20, weight: .medium))
                                    .foregroundColor(.coolSteel)
                            }
                        }
                    }
                    .padding(.trailing, 16)
                }
                .padding(.top, 8)
            }
        }
    }
}

/// Medium flexible navigation bar (128pt height)
/// Ideal for section screens
struct MediumFlexibleNavigationBar<Subtitle: View>: View {
    let title: String
    let subtitle: Subtitle?
    let scrollOffset: CGFloat
    let actions: [NavigationAction]?

    init(
        title: String,
        scrollOffset: CGFloat,
        actions: [NavigationAction]? = nil,
        @ViewBuilder subtitle: () -> Subtitle = { EmptyView() as! Subtitle }
    ) {
        self.title = title
        self.scrollOffset = scrollOffset
        self.actions = actions
        self.subtitle = subtitle()
    }

    var body: some View {
        ZStack(alignment: .top) {
            FlexibleNavigationBar(
                variant: .medium,
                scrollOffset: scrollOffset,
                title: { Text(title) },
                subtitle: { subtitle },
                backgroundContent: { EmptyView() }
            )

            // Action buttons overlay
            if let actions = actions {
                HStack {
                    Spacer()
                    HStack(spacing: 16) {
                        ForEach(actions) { action in
                            Button(action: action.action) {
                                Image(systemName: action.icon)
                                    .font(.system(size: 18, weight: .medium))
                                    .foregroundColor(.coolSteel)
                            }
                        }
                    }
                    .padding(.trailing, 16)
                }
                .padding(.top, 8)
            }
        }
    }
}

/// Navigation action definition
struct NavigationAction: Identifiable {
    let id = UUID()
    let icon: String
    let action: () -> Void
}

/// Convenience extension for common navigation bar patterns
extension View {
    /// Adds a large flexible navigation bar with hero background
    func largeNavigationBar(
        title: String,
        subtitle: String? = nil,
        scrollOffset: CGFloat,
        actions: [NavigationAction]? = nil,
        @ViewBuilder background: @escaping () -> some View = { EmptyView() }
    ) -> some View {
        VStack(spacing: 0) {
            if let subtitle = subtitle {
                LargeFlexibleNavigationBar(
                    title: title,
                    scrollOffset: scrollOffset,
                    actions: actions,
                    subtitle: { Text(subtitle) },
                    backgroundContent: background
                )
            } else {
                LargeFlexibleNavigationBar(
                    title: title,
                    scrollOffset: scrollOffset,
                    actions: actions,
                    subtitle: { EmptyView() },
                    backgroundContent: background
                )
            }

            self
        }
    }

    /// Adds a medium flexible navigation bar
    func mediumNavigationBar(
        title: String,
        subtitle: String? = nil,
        scrollOffset: CGFloat,
        actions: [NavigationAction]? = nil
    ) -> some View {
        VStack(spacing: 0) {
            if let subtitle = subtitle {
                MediumFlexibleNavigationBar(
                    title: title,
                    scrollOffset: scrollOffset,
                    actions: actions,
                    subtitle: { Text(subtitle) }
                )
            } else {
                MediumFlexibleNavigationBar(
                    title: title,
                    scrollOffset: scrollOffset,
                    actions: actions,
                    subtitle: { EmptyView() }
                )
            }

            self
        }
    }
}

/// Hero gradient background helper
struct HeroGradientBackground: View {
    let startColor: Color
    let endColor: Color

    var body: some View {
        LinearGradient(
            gradient: Gradient(colors: [startColor, endColor]),
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
}

/// UIKit blur view wrapper for iOS 14 compatibility
private struct BlurView: UIViewRepresentable {
    let style: UIBlurEffect.Style

    func makeUIView(context: Context) -> UIVisualEffectView {
        UIVisualEffectView(effect: UIBlurEffect(style: style))
    }

    func updateUIView(_ uiView: UIVisualEffectView, context: Context) {
        uiView.effect = UIBlurEffect(style: style)
    }
}
