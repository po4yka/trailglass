import SwiftUI
import Shared

// MARK: - Component Showcase View

/**
 * Comprehensive showcase of all Liquid Glass components for Trailglass iOS.
 * Demonstrates glass effects, interactive elements, cards, charts, animations, and design system.
 * Only visible in DEBUG builds for development reference.
 */
struct ComponentShowcaseView: View {
    @State private var scrollOffset: CGFloat = 0
    @State private var isDarkMode = false
    @State private var selectedTransportMode: TransportType?
    @State private var selectedCategory: String?
    @State private var showAnimations = true

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                // Header
                ShowcaseHeader(isDarkMode: $isDarkMode, showAnimations: $showAnimations)

                // 1. Glass Effects & Materials
                ShowcaseSection(title: "Glass Effects & Materials", icon: "square.stack.3d.up") {
                    GlassEffectsShowcase()
                }

                // 2. Buttons & Interactive Elements
                ShowcaseSection(title: "Buttons & Interactive Elements", icon: "hand.tap") {
                    ButtonsShowcase()
                }

                // 3. Cards & Containers
                ShowcaseSection(title: "Cards & Containers", icon: "rectangle.stack") {
                    CardsShowcase()
                }

                // 4. Loading Indicators
                ShowcaseSection(title: "Loading Indicators", icon: "circle.dotted") {
                    LoadingIndicatorsShowcase(showAnimations: $showAnimations)
                }

                // 5. Charts & Data Visualization
                ShowcaseSection(title: "Charts & Data Visualization", icon: "chart.bar") {
                    ChartsShowcase()
                }

                // 6. Shape Morphing
                ShowcaseSection(title: "Shape Morphing", icon: "hexagon") {
                    ShapeMorphingShowcase(showAnimations: $showAnimations)
                }

                // 7. Navigation Components
                ShowcaseSection(title: "Navigation Components", icon: "chevron.up.chevron.down") {
                    NavigationShowcase()
                }

                // 8. Filters & Selectors
                ShowcaseSection(title: "Filters & Selectors", icon: "slider.horizontal.3") {
                    FiltersSelectorsShowcase(
                        selectedTransportMode: $selectedTransportMode,
                        selectedCategory: $selectedCategory
                    )
                }
            }
            .padding(.vertical, 24)
            .padding(.bottom, 100)
        }
        .background(Color.adaptiveBackground)
        .preferredColorScheme(isDarkMode ? .dark : .light)
    }
}

// MARK: - Previews

#if DEBUG
struct ComponentShowcaseView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            ComponentShowcaseView()
                .preferredColorScheme(.light)
                .previewDisplayName("Light Mode")

            ComponentShowcaseView()
                .preferredColorScheme(.dark)
                .previewDisplayName("Dark Mode")
        }
    }
}
#endif
