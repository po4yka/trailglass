import SwiftUI

// MARK: - Map Action FAB
// Expandable FAB menu for map actions with glass styling

struct MapActionFAB: View {
    let isFollowModeEnabled: Bool
    let onToggleFollowMode: () -> Void
    let onFitToData: () -> Void
    let onRefresh: () -> Void

    @State private var isExpanded = false
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        ZStack {
            // Scrim overlay
            if isExpanded {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(MotionConfig.expressiveSpring) {
                            isExpanded = false
                        }
                    }
            }

            // Menu items and main FAB
            VStack(spacing: 16) {
                // Menu items
                if isExpanded {
                    FABMenuItem(
                        icon: isFollowModeEnabled ? "location.fill" : "location",
                        label: isFollowModeEnabled ? "Stop Following" : "Follow Location",
                        accessibilityLabel: isFollowModeEnabled ? "Stop Following" : "Follow Location",
                        accessibilityHint: isFollowModeEnabled ? "Stop following your current location on the map" : "Center map on your current location and follow as you move"
                    ) {
                        onToggleFollowMode()
                        withAnimation(MotionConfig.expressiveSpring) {
                            isExpanded = false
                        }
                    }
                    .transition(.scale.combined(with: .opacity))

                    FABMenuItem(
                        icon: "arrow.up.left.and.arrow.down.right",
                        label: "Fit to Data",
                        accessibilityLabel: "Fit to Data",
                        accessibilityHint: "Adjust map view to show all visible locations and routes"
                    ) {
                        onFitToData()
                        withAnimation(MotionConfig.expressiveSpring) {
                            isExpanded = false
                        }
                    }
                    .transition(.scale.combined(with: .opacity))

                    FABMenuItem(
                        icon: "arrow.clockwise",
                        label: "Refresh",
                        accessibilityLabel: "Refresh",
                        accessibilityHint: "Reload map data from database"
                    ) {
                        onRefresh()
                        withAnimation(MotionConfig.expressiveSpring) {
                            isExpanded = false
                        }
                    }
                    .transition(.scale.combined(with: .opacity))
                }

                // Main FAB
                Button(action: {
                    withAnimation(MotionConfig.expressiveSpring) {
                        isExpanded.toggle()
                    }
                }) {
                    Image(systemName: "plus")
                        .font(.title2)
                        .foregroundColor(.white)
                        .rotationEffect(.degrees(isExpanded ? 45 : 0))
                        .frame(width: 56, height: 56)
                }
                .glassEffect(variant: .regular)
                .glassEffectTinted(.coastalPath, opacity: 0.8)
                .clipShape(Circle())
                .shadow(color: .black.opacity(DarkModeConfig.shadowOpacity(for: colorScheme)), radius: 8)
                .accessibilityLabel(isExpanded ? "Close map menu" : "Open map menu")
                .accessibilityHint(isExpanded ? "Closes the map action menu" : "Opens menu with map view options")
            }
        }
    }
}

// MARK: - FAB Menu Item

private struct FABMenuItem: View {
    let icon: String
    let label: String
    let accessibilityLabel: String
    let accessibilityHint: String
    let action: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Label
            Text(label)
                .font(.body)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .glassEffect(variant: .regular)
                .cornerRadius(8)

            // Icon button
            Button(action: action) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
            }
            .glassEffect(variant: .regular)
            .glassEffectTinted(.coolSteel, opacity: 0.6)
            .clipShape(Circle())
            .accessibilityLabel(accessibilityLabel)
            .accessibilityHint(accessibilityHint)
        }
    }
}

// MARK: - Preview

#Preview {
    ZStack {
        Color.adaptiveBackground.ignoresSafeArea()

        VStack {
            Spacer()
            HStack {
                Spacer()
                MapActionFAB(
                    isFollowModeEnabled: false,
                    onToggleFollowMode: {},
                    onFitToData: {},
                    onRefresh: {}
                )
                .padding(16)
            }
        }
    }
}

#Preview("Follow Mode Enabled") {
    ZStack {
        Color.adaptiveBackground.ignoresSafeArea()

        VStack {
            Spacer()
            HStack {
                Spacer()
                MapActionFAB(
                    isFollowModeEnabled: true,
                    onToggleFollowMode: {},
                    onFitToData: {},
                    onRefresh: {}
                )
                .padding(16)
            }
        }
    }
}
