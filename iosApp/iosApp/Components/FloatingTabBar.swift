import SwiftUI

/// Tab item definition for FloatingTabBar
struct TabItem: Identifiable {
    let id = UUID()
    let title: String
    let icon: String
    let tag: Int
}

/// Floating tab bar with Liquid Glass material that minimizes on scroll
/// Features:
/// - Glass effect with blur and transparency
/// - Minimizes from 72pt to 56pt on scroll
/// - Spring animations for smooth transitions
/// - Silent Waters Cool Steel tint
struct FloatingTabBar: View {
    @Binding var selection: Int
    @Binding var scrollOffset: CGFloat
    let tabs: [TabItem]

    private let normalHeight: CGFloat = 72
    private let minimizedHeight: CGFloat = 56
    private let scrollThreshold: CGFloat = 50

    private var isMinimized: Bool {
        scrollOffset > scrollThreshold
    }

    private var currentHeight: CGFloat {
        isMinimized ? minimizedHeight : normalHeight
    }

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            HStack(spacing: 0) {
                ForEach(tabs) { tab in
                    TabBarButton(
                        tab: tab,
                        isSelected: selection == tab.tag,
                        isMinimized: isMinimized,
                        action: {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                selection = tab.tag
                            }
                        }
                    )
                }
            }
            .frame(height: currentHeight)
            .background(
                GlassBackground(intensity: isMinimized ? 0.95 : 0.85)
            )
            .cornerRadius(isMinimized ? 24 : 28)
            .shadow(color: Color.black.opacity(0.1), radius: 20, y: 10)
            .padding(.horizontal, 16)
            .padding(.bottom, 8)
            .animation(.spring(response: 0.35, dampingFraction: 0.75), value: isMinimized)
        }
        .ignoresSafeArea(.keyboard)
    }
}

/// Individual tab bar button
private struct TabBarButton: View {
    let tab: TabItem
    let isSelected: Bool
    let isMinimized: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: isMinimized ? 2 : 4) {
                Image(systemName: tab.icon)
                    .font(.system(size: isMinimized ? 22 : 24, weight: isSelected ? .semibold : .regular))
                    .symbolRenderingMode(.hierarchical)
                    .foregroundColor(isSelected ? Color.coolSteel : Color.blueSlate.opacity(0.6))

                if !isMinimized {
                    Text(tab.title)
                        .font(.system(size: 10, weight: isSelected ? .semibold : .regular))
                        .foregroundColor(isSelected ? Color.coolSteel : Color.blueSlate.opacity(0.6))
                        .lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity)
            .contentShape(Rectangle())
        }
        .buttonStyle(TabButtonStyle())
    }
}

/// Custom button style for tab buttons
private struct TabButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.9 : 1.0)
            .animation(.spring(response: 0.2, dampingFraction: 0.6), value: configuration.isPressed)
    }
}

/// Glass background with blur and transparency
private struct GlassBackground: View {
    let intensity: Double

    var body: some View {
        ZStack {
            // Base glass layer
            Color.lightCyan.opacity(0.3 * intensity)

            // Blur layer
            if #available(iOS 15.0, *) {
                Rectangle()
                    .fill(.ultraThinMaterial)
                    .opacity(intensity)
            } else {
                BlurView(style: .systemUltraThinMaterial)
                    .opacity(intensity)
            }

            // Tint overlay with Silent Waters Cool Steel
            Color.coolSteel.opacity(0.15)
                .blendMode(.overlay)
        }
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

/// Container view that manages scroll offset for tab bar
struct FloatingTabBarContainer<Content: View>: View {
    @Binding var selection: Int
    let tabs: [TabItem]
    @ViewBuilder let content: (Int) -> Content

    @State private var scrollOffset: CGFloat = 0

    var body: some View {
        ZStack {
            // Content
            content(selection)

            // Floating tab bar
            VStack {
                Spacer()
                FloatingTabBar(
                    selection: $selection,
                    scrollOffset: $scrollOffset,
                    tabs: tabs
                )
            }
        }
    }
}

/// Preference key for tracking scroll offset
struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0

    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

/// ViewModifier to track scroll position
struct ScrollOffsetModifier: ViewModifier {
    @Binding var offset: CGFloat

    func body(content: Content) -> some View {
        content
            .overlay(
                GeometryReader { geometry in
                    Color.clear.preference(
                        key: ScrollOffsetPreferenceKey.self,
                        value: geometry.frame(in: .named("scroll")).minY
                    )
                }
            )
    }
}

extension View {
    /// Adds scroll offset tracking to a view
    func trackScrollOffset(_ offset: Binding<CGFloat>) -> some View {
        modifier(ScrollOffsetModifier(offset: offset))
    }
}
