import SwiftUI
import Shared

// MARK: - Marker Info Card
// Glass marker info card with Liquid Glass styling for iOS

struct MarkerInfoCard: View {
    let marker: MapMarker
    let onDismiss: () -> Void
    let onViewDetails: () -> Void
    let onAddPhoto: () -> Void

    @State private var isVisible = false
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header with close button
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(marker.title ?? "Unknown location")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(.primary)

                    if let snippet = marker.snippet {
                        Text(snippet)
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                Button(action: onDismiss) {
                    Image(systemName: "xmark.circle.fill")
                        .font(.title2)
                        .foregroundColor(.secondary)
                }
                .buttonStyle(GlassButtonPressStyle())
            }

            // Action buttons
            HStack(spacing: 12) {
                ActionButton(
                    icon: "info.circle",
                    label: "Details",
                    action: onViewDetails
                )

                ActionButton(
                    icon: "photo.badge.plus",
                    label: "Add Photo",
                    action: onAddPhoto
                )
            }
        }
        .padding(16)
        .glassEffect(variant: .regular)
        .glassEffectTinted(.coolSteel, opacity: 0.6)
        .cornerRadius(12)
        .shadow(color: .black.opacity(DarkModeConfig.shadowOpacity(for: colorScheme)), radius: 8)
        .padding()
        .scaleEffect(isVisible ? 1.0 : 0.8)
        .offset(y: isVisible ? 0 : 20)
        .opacity(isVisible ? 1 : 0)
        .onAppear {
            withAnimation(MotionConfig.expressiveSpring) {
                isVisible = true
            }
        }
        .onDisappear {
            isVisible = false
        }
    }
}

// MARK: - Action Button

private struct ActionButton: View {
    let icon: String
    let label: String
    let action: () -> Void

    @State private var isPressed = false

    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .font(.system(size: 16))
                Text(label)
                    .font(.body)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
        }
        .glassEffect(variant: .regular)
        .cornerRadius(8)
        .scaleEffect(isPressed ? 0.97 : 1.0)
        .animation(MotionConfig.quickSpring, value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }
}

// MARK: - Glass Button Press Style

struct GlassButtonPressStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(MotionConfig.quickSpring, value: configuration.isPressed)
    }
}

// MARK: - Preview

#Preview {
    ZStack {
        Color.adaptiveBackground.ignoresSafeArea()

        VStack {
            Spacer()
            MarkerInfoCard(
                marker: MapMarker(
                    id: "1",
                    coordinate: LatLng(latitude: 37.7749, longitude: -122.4194),
                    title: "Golden Gate Park",
                    snippet: "Visited 2 hours ago",
                    color: nil,
                    iconResourceName: nil,
                    placeVisitId: nil
                ),
                onDismiss: {},
                onViewDetails: {},
                onAddPhoto: {}
            )
        }
    }
}
