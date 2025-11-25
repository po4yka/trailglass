import SwiftUI
import Shared

// MARK: - Map Visualization Selector
// Liquid Glass visualization mode selector for map

struct MapVisualizationSelector: View {
    @Binding var selectedMode: MapVisualizationMode
    let modes: [MapVisualizationMode] = [.markers, .clusters, .heatmap, .hybrid]

    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        HStack(spacing: 8) {
            ForEach(modes, id: \.self) { mode in
                ModeButton(
                    mode: mode,
                    isSelected: mode == selectedMode,
                    colorScheme: colorScheme
                ) {
                    withAnimation(MotionConfig.standardSpring) {
                        selectedMode = mode
                    }
                }
            }
        }
        .padding(8)
        .glassEffect(variant: .regular)
    }
}

private struct ModeButton: View {
    let mode: MapVisualizationMode
    let isSelected: Bool
    let colorScheme: ColorScheme
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: iconName(for: mode))
                .font(.system(size: 20))
                .frame(width: 44, height: 44)
        }
        .background(
            backgroundColor
                .cornerRadius(8)
        )
        .scaleEffect(isSelected ? 1.0 : 0.95)
        .animation(MotionConfig.quickSpring, value: isSelected)
        .accessibilityLabel(accessibilityLabelForMode(mode))
        .accessibilityHint(isSelected ? "Currently selected" : "Double tap to switch to this visualization mode")
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
    }

    private func accessibilityLabelForMode(_ mode: MapVisualizationMode) -> String {
        switch mode {
        case .markers: return "Markers view"
        case .clusters: return "Clusters view"
        case .heatmap: return "Heatmap view"
        case .hybrid: return "Hybrid view"
        default: return "Map view"
        }
    }

    private var backgroundColor: Color {
        if isSelected {
            return Color.coastalPath.opacity(DarkModeConfig.glassOpacity(for: colorScheme))
        } else {
            return Color.clear
        }
    }

    private func iconName(for mode: MapVisualizationMode) -> String {
        switch mode {
        case .markers: return "mappin.circle.fill"
        case .clusters: return "circle.grid.3x3.fill"
        case .heatmap: return "flame.fill"
        case .hybrid: return "square.stack.3d.up.fill"
        default: return "map.fill"
        }
    }
}

// MARK: - Map Options Panel
// Glass options panel for clustering and heatmap toggles

struct MapOptionsPanel: View {
    @Binding var clusteringEnabled: Bool
    @Binding var heatmapEnabled: Bool

    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        VStack(alignment: .trailing, spacing: 12) {
            MapOptionToggle(
                icon: "circle.grid.3x3.fill",
                label: "Cluster",
                isOn: $clusteringEnabled,
                colorScheme: colorScheme
            )

            MapOptionToggle(
                icon: "flame.fill",
                label: "Heatmap",
                isOn: $heatmapEnabled,
                colorScheme: colorScheme
            )
        }
        .padding(12)
        .glassEffect(variant: .regular)
    }
}

private struct MapOptionToggle: View {
    let icon: String
    let label: String
    @Binding var isOn: Bool
    let colorScheme: ColorScheme

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(isOn ? .coastalPath : .secondary)

            Text(label)
                .font(.body)
                .foregroundColor(isOn ? .primary : .secondary)

            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(.coastalPath)
                .accessibilityLabel("\(label) toggle")
                .accessibilityHint(isOn ? "Double tap to disable \(label.lowercased())" : "Double tap to enable \(label.lowercased())")
        }
        .animation(MotionConfig.standardSpring, value: isOn)
    }
}

// MARK: - Wavy Loading Indicator
// Glass-themed wavy loading indicator for map loading states

struct WavyLoadingIndicator: View {
    let size: CGFloat
    let color: Color

    @State private var rotation: Double = 0
    @State private var phase: CGFloat = 0

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
                .frame(width: size, height: size)
                .rotationEffect(.degrees(rotation))
                .overlay(
                    WavyShape(phase: phase, frequency: 3, amplitude: size * 0.05)
                        .stroke(color.opacity(0.3), lineWidth: 2)
                )
        }
        .onAppear {
            withAnimation(
                .linear(duration: 1.5)
                .repeatForever(autoreverses: false)
            ) {
                rotation = 360
            }
            withAnimation(
                .linear(duration: 2.0)
                .repeatForever(autoreverses: false)
            ) {
                phase = .pi * 2
            }
        }
    }
}

// MARK: - Previews

#Preview("Map Visualization Selector") {
    VStack(spacing: 32) {
        MapVisualizationSelector(selectedMode: .constant(.markers))
        MapVisualizationSelector(selectedMode: .constant(.clusters))
        MapVisualizationSelector(selectedMode: .constant(.heatmap))
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Map Options Panel") {
    VStack(spacing: 32) {
        MapOptionsPanel(
            clusteringEnabled: .constant(true),
            heatmapEnabled: .constant(false)
        )
        MapOptionsPanel(
            clusteringEnabled: .constant(false),
            heatmapEnabled: .constant(true)
        )
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Wavy Loading Indicator") {
    VStack(spacing: 32) {
        WavyLoadingIndicator(size: 48, color: .coastalPath)
        WavyLoadingIndicator(size: 72, color: .seaGlass)
    }
    .padding()
    .background(Color.backgroundLight)
}
