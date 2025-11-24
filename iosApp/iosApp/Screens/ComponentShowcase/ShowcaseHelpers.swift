import SwiftUI
import Shared

// MARK: - Showcase Header

struct ShowcaseHeader: View {
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

struct ShowcaseSection<Content: View>: View {
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

// MARK: - Glass Variant Demo Views

struct GlassVariantDemo: View {
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

struct TintedGlassDemo: View {
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

// MARK: - Shape Demo Views

struct ShapeDemo: View {
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

struct ShapeSemanticDemo: View {
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

// MARK: - Sample Data Functions

// func samplePlaceVisit() -> PlaceVisit {
//     PlaceVisit(...)
// } // Commented out - requires proper Kotlin types

// func sampleRouteSegment() -> RouteSegment {
//     RouteSegment(...)
// } // Commented out - requires proper Kotlin types

func samplePieData() -> [PieData] {
    [
        PieData(label: "Walk", value: 35, color: .seaGlass),
        PieData(label: "Bike", value: 25, color: .coastalPath),
        PieData(label: "Car", value: 20, color: .driftwood),
        PieData(label: "Train", value: 15, color: .duskPurple),
        PieData(label: "Other", value: 5, color: .neutralCategory)
    ]
}
