import SwiftUI
import Shared

// MARK: - Transport Mode Selector

/**
 * Transport mode selector with glass buttons.
 * Matches Android version with icon + optional label layout.
 * Used for filtering stats/timeline by transport type.
 */
struct TransportModeSelector: View {
    @Binding var selectedMode: TransportType?
    let modes: [TransportType]
    let showLabels: Bool
    let allowDeselect: Bool
    let onModeSelected: (TransportType?) -> Void
    @Environment(\.colorScheme) var colorScheme

    init(
        selectedMode: Binding<TransportType?>,
        modes: [TransportType] = TransportType.allModes,
        showLabels: Bool = true,
        allowDeselect: Bool = true,
        onModeSelected: @escaping (TransportType?) -> Void = { _ in }
    ) {
        self._selectedMode = selectedMode
        self.modes = modes
        self.showLabels = showLabels
        self.allowDeselect = allowDeselect
        self.onModeSelected = onModeSelected
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(modes, id: \.name) { mode in
                    TransportModeButton(
                        mode: mode,
                        isSelected: selectedMode?.name == mode.name,
                        showLabel: showLabels,
                        onTap: {
                            withAnimation(MotionConfig.expressiveSpring) {
                                if selectedMode?.name == mode.name && allowDeselect {
                                    selectedMode = nil
                                    onModeSelected(nil)
                                } else {
                                    selectedMode = mode
                                    onModeSelected(mode)
                                }
                            }
                        }
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
    }
}

// MARK: - Transport Mode Button

private struct TransportModeButton: View {
    let mode: TransportType
    let isSelected: Bool
    let showLabel: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) var colorScheme

    private var modeColor: Color {
        switch mode.name {
        case "WALK": return .adaptiveSuccess
        case "BIKE": return .adaptivePrimary
        case "CAR": return .adaptiveWarning
        case "TRAIN": return .duskPurple
        case "PLANE": return .morningCategory
        case "BOAT": return .waterCategory
        default: return .neutralCategory
        }
    }

    private var modeIcon: String {
        switch mode.name {
        case "WALK": return "figure.walk"
        case "BIKE": return "bicycle"
        case "CAR": return "car.fill"
        case "TRAIN": return "tram.fill"
        case "PLANE": return "airplane"
        case "BOAT": return "ferry.fill"
        default: return "questionmark.circle"
        }
    }

    private var modeName: String {
        mode.name.lowercased().capitalized
    }

    private var modeShape: GlassShape {
        GlassShape.forTransportType(mode.name)
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 8) {
                // Morphing icon container
                ZStack {
                    modeShape.pathData(CGRect(x: 0, y: 0, width: 40, height: 40))
                        .fill(
                            LinearGradient(
                                colors: [
                                    modeColor.opacity(0.6),
                                    modeColor.opacity(0.4)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .overlay(
                            modeShape.pathData(CGRect(x: 0, y: 0, width: 40, height: 40))
                                .fill(.ultraThinMaterial)
                                .opacity(0.3)
                        )
                        .overlay(
                            modeShape.pathData(CGRect(x: 0, y: 0, width: 40, height: 40))
                                .stroke(
                                    isSelected ? modeColor : Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                                    lineWidth: isSelected ? 2 : 1.5
                                )
                        )

                    Image(systemName: modeIcon)
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .shadow(color: Color.black.opacity(0.3), radius: 2)
                }
                .frame(width: 40, height: 40)

                if showLabel {
                    Text(modeName)
                        .font(.subheadline)
                        .fontWeight(isSelected ? .semibold : .regular)
                        .foregroundColor(isSelected ? modeColor : .primary)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .fill(modeColor.opacity(isSelected ? 0.2 : 0.05))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .strokeBorder(
                                isSelected ? modeColor.opacity(0.6) : Color.white.opacity(colorScheme == .dark ? 0.2 : 0.3),
                                lineWidth: isSelected ? 2 : 1
                            )
                    )
            )
            .shadow(
                color: isSelected ? modeColor.opacity(0.3) : Color.black.opacity(0.1),
                radius: isSelected ? 8 : 4,
                y: 4
            )
            .scaleEffect(isSelected ? 1.05 : 1.0)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Transport Type Extension

extension TransportType {
    static var allModes: [TransportType] {
        [.walk, .bike, .car, .train, .plane, .boat]
    }
}

// MARK: - Compact Transport Mode Selector

/**
 * Compact version with icon-only buttons.
 */
struct CompactTransportModeSelector: View {
    @Binding var selectedMode: TransportType?
    let modes: [TransportType]
    let onModeSelected: (TransportType?) -> Void

    init(
        selectedMode: Binding<TransportType?>,
        modes: [TransportType] = TransportType.allModes,
        onModeSelected: @escaping (TransportType?) -> Void = { _ in }
    ) {
        self._selectedMode = selectedMode
        self.modes = modes
        self.onModeSelected = onModeSelected
    }

    var body: some View {
        HStack(spacing: 8) {
            ForEach(modes, id: \.name) { mode in
                CompactModeButton(
                    mode: mode,
                    isSelected: selectedMode?.name == mode.name,
                    onTap: {
                        withAnimation(MotionConfig.expressiveSpring) {
                            if selectedMode?.name == mode.name {
                                selectedMode = nil
                                onModeSelected(nil)
                            } else {
                                selectedMode = mode
                                onModeSelected(mode)
                            }
                        }
                    }
                )
            }
        }
    }
}

private struct CompactModeButton: View {
    let mode: TransportType
    let isSelected: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) var colorScheme

    private var modeColor: Color {
        switch mode.name {
        case "WALK": return .adaptiveSuccess
        case "BIKE": return .adaptivePrimary
        case "CAR": return .adaptiveWarning
        case "TRAIN": return .duskPurple
        case "PLANE": return .morningCategory
        case "BOAT": return .waterCategory
        default: return .neutralCategory
        }
    }

    private var modeIcon: String {
        switch mode.name {
        case "WALK": return "figure.walk"
        case "BIKE": return "bicycle"
        case "CAR": return "car.fill"
        case "TRAIN": return "tram.fill"
        case "PLANE": return "airplane"
        case "BOAT": return "ferry.fill"
        default: return "questionmark.circle"
        }
    }

    var body: some View {
        Button(action: onTap) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [
                                modeColor.opacity(0.6),
                                modeColor.opacity(0.4)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        Circle()
                            .fill(.ultraThinMaterial)
                            .opacity(0.3)
                    )
                    .overlay(
                        Circle()
                            .strokeBorder(
                                isSelected ? modeColor : Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                                lineWidth: isSelected ? 2 : 1
                            )
                    )

                Image(systemName: modeIcon)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                    .shadow(color: Color.black.opacity(0.3), radius: 2)
            }
            .frame(width: 36, height: 36)
            .shadow(
                color: isSelected ? modeColor.opacity(0.3) : Color.black.opacity(0.1),
                radius: isSelected ? 6 : 3,
                y: 2
            )
            .scaleEffect(isSelected ? 1.1 : 1.0)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Grid Transport Mode Selector

/**
 * Grid layout for transport mode selection.
 */
struct GridTransportModeSelector: View {
    @Binding var selectedModes: Set<String>
    let modes: [TransportType]
    let columns: Int
    let showLabels: Bool
    let onModesChanged: (Set<String>) -> Void

    init(
        selectedModes: Binding<Set<String>>,
        modes: [TransportType] = TransportType.allModes,
        columns: Int = 3,
        showLabels: Bool = true,
        onModesChanged: @escaping (Set<String>) -> Void = { _ in }
    ) {
        self._selectedModes = selectedModes
        self.modes = modes
        self.columns = columns
        self.showLabels = showLabels
        self.onModesChanged = onModesChanged
    }

    var body: some View {
        LazyVGrid(
            columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: columns),
            spacing: 12
        ) {
            ForEach(modes, id: \.name) { mode in
                TransportModeButton(
                    mode: mode,
                    isSelected: selectedModes.contains(mode.name),
                    showLabel: showLabels,
                    onTap: {
                        withAnimation(MotionConfig.expressiveSpring) {
                            if selectedModes.contains(mode.name) {
                                selectedModes.remove(mode.name)
                            } else {
                                selectedModes.insert(mode.name)
                            }
                            onModesChanged(selectedModes)
                        }
                    }
                )
            }
        }
    }
}

// MARK: - Preview Helper

#if DEBUG
struct TransportModeSelector_Previews: PreviewProvider {
    @State static var selectedMode: TransportType? = .walk
    @State static var selectedModes: Set<String> = ["WALK", "BIKE"]

    static var previews: some View {
        VStack(spacing: 32) {
            // Full selector with labels
            TransportModeSelector(
                selectedMode: $selectedMode,
                showLabels: true
            ) { _ in }

            // Compact selector
            CompactTransportModeSelector(
                selectedMode: $selectedMode
            ) { _ in }

            // Grid selector
            GridTransportModeSelector(
                selectedModes: $selectedModes,
                columns: 3,
                showLabels: true
            ) { _ in }
        }
        .padding()
        .background(Color.adaptiveBackground)
    }
}
#endif
