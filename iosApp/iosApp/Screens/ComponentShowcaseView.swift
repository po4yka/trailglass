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

// MARK: - Showcase Header

private struct ShowcaseHeader: View {
    @Binding var isDarkMode: Bool
    @Binding var showAnimations: Bool

    var body: some View {
        VStack(spacing: 16) {
            Text("Liquid Glass Components")
                .font(.system(size: 34, weight: .bold))
                .foregroundColor(.primary)

            Text("Trailglass iOS Design System")
                .font(.subheadline)
                .foregroundColor(.secondary)

            HStack(spacing: 12) {
                GlassToggleButton(
                    title: "Dark Mode",
                    icon: isDarkMode ? "moon.fill" : "sun.max.fill",
                    isOn: $isDarkMode,
                    tint: .coastalPath
                )

                GlassToggleButton(
                    title: "Animations",
                    icon: showAnimations ? "waveform" : "waveform.slash",
                    isOn: $showAnimations,
                    tint: .seaGlass
                )
            }
        }
        .padding(.horizontal)
    }
}

// MARK: - Showcase Section

private struct ShowcaseSection<Content: View>: View {
    let title: String
    let icon: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Section header
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(.coastalPath)

                Text(title)
                    .font(.title3)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
            }
            .padding(.horizontal)

            // Section content
            content()
                .padding(.horizontal)
        }
    }
}

// MARK: - 1. Glass Effects Showcase

private struct GlassEffectsShowcase: View {
    @State private var selectedVariant = 0

    var body: some View {
        VStack(spacing: 16) {
            // All glass variants
            Text("Glass Variants")
                .font(.caption)
                .foregroundColor(.secondary)

            HStack(spacing: 12) {
                GlassVariantDemo(variant: .ultraThin, label: "Ultra Thin")
                GlassVariantDemo(variant: .clear, label: "Clear")
                GlassVariantDemo(variant: .regular, label: "Regular")
                GlassVariantDemo(variant: .thick, label: "Thick")
            }

            Divider().padding(.vertical, 8)

            // Silent Waters tinted variants
            Text("Silent Waters Tints")
                .font(.caption)
                .foregroundColor(.secondary)

            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    TintedGlassDemo(variant: .primary, label: "Primary")
                    TintedGlassDemo(variant: .success, label: "Success")
                    TintedGlassDemo(variant: .warning, label: "Warning")
                }
                HStack(spacing: 8) {
                    TintedGlassDemo(variant: .surface, label: "Surface")
                    TintedGlassDemo(variant: .active, label: "Active")
                    TintedGlassDemo(variant: .secondary, label: "Secondary")
                }
            }

            Divider().padding(.vertical, 8)

            // Interactive shimmer
            Text("Interactive Effects")
                .font(.caption)
                .foregroundColor(.secondary)

            VStack(spacing: 8) {
                Text("Press and hold to see shimmer effect")
                    .font(.caption2)
                    .foregroundColor(.secondary)

                Text("Shimmer Demo")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(height: 60)
                    .frame(maxWidth: .infinity)
                    .glassBackground(material: .regular, tint: .coastalPath, cornerRadius: 12)
                    .interactive(tint: .lightBlue)
                    .glassShadow(elevation: 2)
            }

            Divider().padding(.vertical, 8)

            // Glass borders
            Text("Glass Borders")
                .font(.caption)
                .foregroundColor(.secondary)

            HStack(spacing: 12) {
                Text("Border 1pt")
                    .font(.caption)
                    .foregroundColor(.primary)
                    .padding(12)
                    .glassBackground(material: .regular, cornerRadius: 8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .strokeBorder(
                                LinearGradient(
                                    colors: [Color.white.opacity(0.5), Color.white.opacity(0.1)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    )

                Text("Border 2pt")
                    .font(.caption)
                    .foregroundColor(.primary)
                    .padding(12)
                    .glassBackground(material: .regular, cornerRadius: 8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .strokeBorder(
                                LinearGradient(
                                    colors: [Color.white.opacity(0.5), Color.white.opacity(0.1)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 2
                            )
                    )
            }
        }
    }
}

private struct GlassVariantDemo: View {
    let variant: GlassVariant
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.clear)
                .frame(height: 60)
                .glassEffect(variant: variant)
                .overlay(
                    Image(systemName: "sparkles")
                        .foregroundColor(.white)
                )

            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

private struct TintedGlassDemo: View {
    let variant: GlassVariant
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.clear)
                .frame(height: 50)
                .glassEffect(variant: variant)
                .overlay(
                    Text(label)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(.white)
                )
        }
    }
}

// MARK: - 2. Buttons Showcase

private struct ButtonsShowcase: View {
    @State private var isSelected = false
    @State private var chipSelections: Set<Int> = [0]

    var body: some View {
        VStack(spacing: 16) {
            // Glass buttons - variants
            Text("Button Variants")
                .font(.caption)
                .foregroundColor(.secondary)

            VStack(spacing: 8) {
                GlassButton(title: "Filled Button", icon: "star.fill", variant: .filled, tint: .coastalPath) {}
                GlassButton(title: "Outlined Button", icon: "heart", variant: .outlined, tint: .seaGlass) {}
                GlassButton(title: "Text Button", icon: "info.circle", variant: .text, tint: .blueSlate) {}
            }

            Divider().padding(.vertical, 8)

            // Selection states
            Text("Selection & States")
                .font(.caption)
                .foregroundColor(.secondary)

            VStack(spacing: 8) {
                GlassButton(
                    title: isSelected ? "Selected" : "Tap to Select",
                    icon: "checkmark.circle.fill",
                    isSelected: isSelected,
                    tint: .adaptivePrimary
                ) {
                    withAnimation(MotionConfig.bouncy) {
                        isSelected.toggle()
                    }
                }

                GlassButton(title: "Disabled Button", icon: "xmark", isDisabled: true, tint: .adaptivePrimary) {}
            }

            Divider().padding(.vertical, 8)

            // Filter chips
            Text("Filter Chips")
                .font(.caption)
                .foregroundColor(.secondary)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(0..<5) { index in
                        GlassFilterChip(
                            label: ["All", "Walk", "Bike", "Car", "Train"][index],
                            icon: [nil, "figure.walk", "bicycle", "car", "tram"][index],
                            isSelected: chipSelections.contains(index),
                            tint: [.coolSteel, .seaGlass, .coastalPath, .driftwood, .duskPurple][index]
                        ) {
                            if chipSelections.contains(index) {
                                chipSelections.remove(index)
                            } else {
                                chipSelections.insert(index)
                            }
                        }
                    }
                }
                .padding(.horizontal, 4)
            }

            Divider().padding(.vertical, 8)

            // Icon buttons
            Text("Icon Buttons")
                .font(.caption)
                .foregroundColor(.secondary)

            HStack(spacing: 16) {
                GlassIconButton(icon: "heart.fill", size: 36, tint: .adaptiveWarning) {}
                GlassIconButton(icon: "star.fill", size: 44, tint: .warning) {}
                GlassIconButton(icon: "location.fill", size: 52, tint: .coastalPath) {}
            }
        }
    }
}

// MARK: - 3. Cards Showcase

private struct CardsShowcase: View {
    var body: some View {
        VStack(spacing: 16) {
            // Visit card
            Text("Visit Card")
                .font(.caption)
                .foregroundColor(.secondary)

            VisitGlassCard(visit: samplePlaceVisit())

            Divider().padding(.vertical, 8)

            // Route card
            Text("Route Card")
                .font(.caption)
                .foregroundColor(.secondary)

            RouteGlassCard(route: sampleRouteSegment())

            Divider().padding(.vertical, 8)

            // Stat cards
            Text("Stat Cards")
                .font(.caption)
                .foregroundColor(.secondary)

            HStack(spacing: 12) {
                StatGlassCard(
                    title: "Distance",
                    value: "1,234",
                    icon: "ruler",
                    tint: .coastalPath
                )

                StatGlassCard(
                    title: "Countries",
                    value: "12",
                    icon: "globe",
                    tint: .seaGlass
                )
            }

            Divider().padding(.vertical, 8)

            // Summary card
            Text("Summary Card")
                .font(.caption)
                .foregroundColor(.secondary)

            SummaryGlassCard(
                title: "This Week",
                subtitle: "Your travel summary",
                icon: "calendar",
                stats: [
                    (icon: "figure.walk", label: "Steps", value: "12,345"),
                    (icon: "mappin.circle", label: "Places", value: "8"),
                    (icon: "photo", label: "Photos", value: "24")
                ]
            )
        }
    }
}

// MARK: - 4. Loading Indicators Showcase

private struct LoadingIndicatorsShowcase: View {
    @Binding var showAnimations: Bool

    var body: some View {
        VStack(spacing: 16) {
            // Wavy spinner
            Text("Wavy Spinner")
                .font(.caption)
                .foregroundColor(.secondary)

            if showAnimations {
                HStack(spacing: 32) {
                    GlassLoadingIndicator(variant: .wavy, size: 48, color: .coastalPath)
                    GlassLoadingIndicator(variant: .wavy, size: 72, color: .seaGlass)
                }
            } else {
                Text("Enable animations to see indicators")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding()
            }

            Divider().padding(.vertical, 8)

            // Pulsing glass
            Text("Pulsing Glass")
                .font(.caption)
                .foregroundColor(.secondary)

            if showAnimations {
                HStack(spacing: 32) {
                    GlassLoadingIndicator(variant: .pulsing, size: 48, color: .blueSlate)
                    GlassLoadingIndicator(variant: .pulsing, size: 72, color: .coolSteel)
                }
            }

            Divider().padding(.vertical, 8)

            // Morphing shapes
            Text("Morphing Shapes")
                .font(.caption)
                .foregroundColor(.secondary)

            if showAnimations {
                HStack(spacing: 32) {
                    GlassLoadingIndicator(variant: .morphing, size: 48, color: .lightBlue)
                    GlassLoadingIndicator(variant: .morphing, size: 72, color: .coastalPath)
                }
            }

            Divider().padding(.vertical, 8)

            // Linear progress
            Text("Linear Progress")
                .font(.caption)
                .foregroundColor(.secondary)

            if showAnimations {
                VStack(spacing: 16) {
                    GlassLoadingIndicator(variant: .linear, size: 250, color: .coastalPath)
                    GlassLoadingIndicator(variant: .linear, size: 200, color: .seaGlass)
                }
            }
        }
    }
}

// MARK: - 5. Charts Showcase

private struct ChartsShowcase: View {
    var body: some View {
        VStack(spacing: 16) {
            // Pie chart
            Text("Pie Chart")
                .font(.caption)
                .foregroundColor(.secondary)

            PieChartView(data: samplePieData(), showLegend: true)

            Divider().padding(.vertical, 8)

            // Note about other charts
            Text("Bar Chart & Heatmap")
                .font(.caption)
                .foregroundColor(.secondary)

            Text("See BarChartView and ActivityHeatmapView in Components/Charts/")
                .font(.caption2)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding()
                .glassBackground(material: .ultraThin, tint: .lightCyan, cornerRadius: 8)
        }
    }
}

// MARK: - 6. Shape Morphing Showcase

private struct ShapeMorphingShowcase: View {
    @Binding var showAnimations: Bool

    var body: some View {
        VStack(spacing: 16) {
            // All 6 shapes
            Text("Available Shapes")
                .font(.caption)
                .foregroundColor(.secondary)

            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 12) {
                ShapeDemo(shape: .circle, label: "Circle")
                ShapeDemo(shape: .triangle, label: "Triangle")
                ShapeDemo(shape: .hexagon, label: "Hexagon")
                ShapeDemo(shape: .roundedSquare, label: "Square")
                ShapeDemo(shape: .wave, label: "Wave")
                ShapeDemo(shape: .petal, label: "Petal")
            }

            Divider().padding(.vertical, 8)

            // Continuous morphing
            Text("Continuous Morphing")
                .font(.caption)
                .foregroundColor(.secondary)

            if showAnimations {
                ContinuousMorphingView(
                    shapes: [.circle, .triangle, .hexagon, .wave, .petal, .roundedSquare],
                    color: .coastalPath,
                    size: 80,
                    duration: 2.0
                )
                .frame(height: 100)
            } else {
                Text("Enable animations to see morphing")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding()
            }

            Divider().padding(.vertical, 8)

            // Semantic mapping
            Text("Semantic Shape Mapping")
                .font(.caption)
                .foregroundColor(.secondary)

            VStack(spacing: 8) {
                Text("Shapes automatically map to categories and transport types")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)

                HStack(spacing: 8) {
                    ShapeSemanticDemo(shape: .forCategory("FOOD"), label: "Food", color: .sunrisePeach)
                    ShapeSemanticDemo(shape: .forCategory("WORK"), label: "Work", color: .coolSteel)
                    ShapeSemanticDemo(shape: .forTransportType("WALK"), label: "Walk", color: .seaGlass)
                    ShapeSemanticDemo(shape: .forTransportType("PLANE"), label: "Plane", color: .morningCategory)
                }
            }
        }
    }
}

private struct ShapeDemo: View {
    let shape: GlassShape
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            ZStack {
                shape.pathData(CGRect(x: 0, y: 0, width: 60, height: 60))
                    .fill(
                        LinearGradient(
                            colors: [Color.coastalPath.opacity(0.6), Color.coastalPath.opacity(0.4)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        shape.pathData(CGRect(x: 0, y: 0, width: 60, height: 60))
                            .stroke(Color.white.opacity(0.3), lineWidth: 1.5)
                    )
            }
            .frame(width: 60, height: 60)
            .shadow(color: Color.coastalPath.opacity(0.3), radius: 8)

            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

private struct ShapeSemanticDemo: View {
    let shape: GlassShape
    let label: String
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            ZStack {
                shape.pathData(CGRect(x: 0, y: 0, width: 40, height: 40))
                    .fill(
                        LinearGradient(
                            colors: [color.opacity(0.6), color.opacity(0.4)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        shape.pathData(CGRect(x: 0, y: 0, width: 40, height: 40))
                            .stroke(Color.white.opacity(0.3), lineWidth: 1.5)
                    )
            }
            .frame(width: 40, height: 40)

            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - 7. Navigation Showcase

private struct NavigationShowcase: View {
    @State private var scrollOffset: CGFloat = 0

    var body: some View {
        VStack(spacing: 16) {
            // Navigation bar variants
            Text("FlexibleNavigationBar Variants")
                .font(.caption)
                .foregroundColor(.secondary)

            VStack(spacing: 12) {
                // Large
                VStack(alignment: .leading, spacing: 4) {
                    Text("Large (180pt)")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    FlexibleNavigationBar(
                        variant: .large,
                        scrollOffset: 0,
                        title: { Text("Large Title") },
                        subtitle: { Text("With subtitle") }
                    )
                    .frame(height: 180)
                    .glassBackground(material: .regular, cornerRadius: 12)
                }

                // Medium
                VStack(alignment: .leading, spacing: 4) {
                    Text("Medium (128pt)")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    FlexibleNavigationBar(
                        variant: .medium,
                        scrollOffset: 0,
                        title: { Text("Medium Title") },
                        subtitle: { Text("With subtitle") }
                    )
                    .frame(height: 128)
                    .glassBackground(material: .regular, cornerRadius: 12)
                }

                // Compact
                VStack(alignment: .leading, spacing: 4) {
                    Text("Compact (64pt)")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    FlexibleNavigationBar(
                        variant: .compact,
                        scrollOffset: 0,
                        title: { Text("Compact Title") }
                    )
                    .frame(height: 64)
                    .glassBackground(material: .regular, cornerRadius: 12)
                }
            }

            Divider().padding(.vertical, 8)

            Text("Scroll behavior demo available in actual screens")
                .font(.caption2)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
    }
}

// MARK: - 8. Filters & Selectors Showcase

private struct FiltersSelectorsShowcase: View {
    @Binding var selectedTransportMode: TransportType?
    @Binding var selectedCategory: String?

    var body: some View {
        VStack(spacing: 16) {
            // Transport mode selector
            Text("Transport Mode Selector")
                .font(.caption)
                .foregroundColor(.secondary)

            TransportModeSelector(
                selectedMode: $selectedTransportMode,
                showLabels: true
            ) { _ in }

            Divider().padding(.vertical, 8)

            // Compact transport selector
            Text("Compact Transport Selector")
                .font(.caption)
                .foregroundColor(.secondary)

            CompactTransportModeSelector(
                selectedMode: $selectedTransportMode
            ) { _ in }

            Divider().padding(.vertical, 8)

            // Category badges
            Text("Category Badges")
                .font(.caption)
                .foregroundColor(.secondary)

            CategoryBadgeRow(
                categories: [.home, .work, .food, .shopping, .fitness, .entertainment],
                selectedCategory: selectedCategory,
                badgeSize: .medium
            ) { category in
                selectedCategory = selectedCategory == category.name ? nil : category.name
            }

            Divider().padding(.vertical, 8)

            // Glass effect group with chips
            Text("Glass Effect Group")
                .font(.caption)
                .foregroundColor(.secondary)

            GlassEffectGroup(variant: .surface, padding: 16, cornerRadius: 16) {
                VStack(spacing: 12) {
                    Text("Grouped Glass Elements")
                        .font(.headline)
                        .foregroundColor(.primary)

                    HStack(spacing: 8) {
                        ForEach(["Today", "This Week", "This Month"], id: \.self) { label in
                            Text(label)
                                .font(.caption)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .glassBackground(material: .ultraThin, tint: .coolSteel, cornerRadius: 8)
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Sample Data Functions

private func samplePlaceVisit() -> PlaceVisit {
    PlaceVisit(
        id: 1,
        latitude: 37.7749,
        longitude: -122.4194,
        arrivalTime: Date().addingTimeInterval(-7200),
        departureTime: Date().addingTimeInterval(-3600),
        category: .home,
        poiName: "Golden Gate Park",
        approximateAddress: "Golden Gate Park, San Francisco",
        city: "San Francisco",
        country: "USA",
        userLabel: nil,
        userNotes: "Beautiful afternoon walk through the park",
        isFavorite: true,
        tripId: nil,
        photoCount: 3
    )
}

private func sampleRouteSegment() -> RouteSegment {
    RouteSegment(
        id: 1,
        startTime: Date().addingTimeInterval(-1800),
        endTime: Date().addingTimeInterval(-900),
        transportType: .bike,
        distanceMeters: 5280,
        confidence: 0.85,
        startLatitude: 37.7749,
        startLongitude: -122.4194,
        endLatitude: 37.7849,
        endLongitude: -122.4094,
        tripId: nil
    )
}

private func samplePieData() -> [PieData] {
    [
        PieData(label: "Walk", value: 35, color: .seaGlass),
        PieData(label: "Bike", value: 25, color: .coastalPath),
        PieData(label: "Car", value: 20, color: .driftwood),
        PieData(label: "Train", value: 15, color: .duskPurple),
        PieData(label: "Other", value: 5, color: .neutralCategory)
    ]
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
