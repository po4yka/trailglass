import SwiftUI

// MARK: - Tracking FAB Menu
// Expandable Floating Action Button with glass material and spring animations

struct TrackingFAB: View {
    @Binding var isTracking: Bool
    let onToggleTracking: () -> Void
    let onAddPhoto: () -> Void
    let onAddNote: () -> Void
    let onCheckIn: () -> Void

    @State private var isExpanded = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            // Scrim overlay when expanded
            if isExpanded {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(MotionConfig.smooth) {
                            isExpanded = false
                        }
                    }
                    .transition(.opacity)
            }

            VStack(alignment: .trailing, spacing: 16) {
                // Menu items (shown when expanded)
                if isExpanded {
                    VStack(alignment: .trailing, spacing: 16) {
                        FABMenuItem(
                            icon: "location.fill",
                            label: "Check In",
                            tint: .neutralCategory
                        ) {
                            onCheckIn()
                            withAnimation(MotionConfig.smooth) {
                                isExpanded = false
                            }
                        }
                        .transition(
                            .asymmetric(
                                insertion: .scale(scale: 0.8).combined(with: .opacity),
                                removal: .scale(scale: 0.8).combined(with: .opacity)
                            )
                        )

                        FABMenuItem(
                            icon: "note.text",
                            label: "Add Note",
                            tint: .blueSlate
                        ) {
                            onAddNote()
                            withAnimation(MotionConfig.smooth) {
                                isExpanded = false
                            }
                        }
                        .transition(
                            .asymmetric(
                                insertion: .scale(scale: 0.8).combined(with: .opacity),
                                removal: .scale(scale: 0.8).combined(with: .opacity)
                            )
                        )

                        FABMenuItem(
                            icon: "camera.fill",
                            label: "Add Photo",
                            tint: .coolSteel
                        ) {
                            onAddPhoto()
                            withAnimation(MotionConfig.smooth) {
                                isExpanded = false
                            }
                        }
                        .transition(
                            .asymmetric(
                                insertion: .scale(scale: 0.8).combined(with: .opacity),
                                removal: .scale(scale: 0.8).combined(with: .opacity)
                            )
                        )

                        FABMenuItem(
                            icon: isTracking ? "stop.fill" : "play.fill",
                            label: isTracking ? "Stop Tracking" : "Start Tracking",
                            tint: isTracking ? .adaptiveWarning : .activeRoute
                        ) {
                            onToggleTracking()
                            withAnimation(MotionConfig.smooth) {
                                isExpanded = false
                            }
                        }
                        .transition(
                            .asymmetric(
                                insertion: .scale(scale: 0.8).combined(with: .opacity),
                                removal: .scale(scale: 0.8).combined(with: .opacity)
                            )
                        )
                    }
                }

                // Main FAB
                MainFAB(
                    isExpanded: $isExpanded,
                    isTracking: isTracking
                )
            }
            .padding(16)
        }
    }
}

// MARK: - Main FAB Button

private struct MainFAB: View {
    @Binding var isExpanded: Bool
    let isTracking: Bool

    var body: some View {
        Button {
            withAnimation(MotionConfig.bouncy) {
                isExpanded.toggle()
            }
        } label: {
            Image(systemName: "plus")
                .font(.system(size: 24, weight: .semibold))
                .foregroundColor(.white)
                .frame(width: 56, height: 56)
                .glassEffectTinted(
                    isTracking && !isExpanded ? Color.coastalPath : Color.adaptivePrimary,
                    opacity: 0.8
                )
                .shadow(color: Color.black.opacity(0.2), radius: 4, x: 0, y: 2)
                .rotationEffect(.degrees(isExpanded ? 45 : 0))
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - FAB Menu Item

private struct FABMenuItem: View {
    let icon: String
    let label: String
    let tint: Color
    let action: () -> Void

    @State private var isPressed = false

    var body: some View {
        HStack(spacing: 16) {
            // Label with glass background
            Text(label)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.primary)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .glassEffectTinted(
                    Color.adaptiveSurface,
                    opacity: 0.7
                )
                .shadow(color: Color.black.opacity(0.2), radius: 2, x: 0, y: 1)

            // Small FAB
            Button {
                action()
            } label: {
                Image(systemName: icon)
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
                    .glassEffectTinted(
                        Color.coastalPath,
                        opacity: 0.8
                    )
                    .shadow(color: Color.black.opacity(0.2), radius: 2, x: 0, y: 1)
            }
            .buttonStyle(PressableButtonStyle())
        }
    }
}

// MARK: - Pressable Button Style

private struct PressableButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? MotionConfig.pressScale : 1.0)
            .animation(MotionConfig.buttonPress, value: configuration.isPressed)
    }
}

// MARK: - Preview

#Preview {
    ZStack {
        Color.backgroundLight.ignoresSafeArea()

        VStack {
            Spacer()
            TrackingFAB(
                isTracking: .constant(false),
                onToggleTracking: {},
                onAddPhoto: {},
                onAddNote: {},
                onCheckIn: {}
            )
        }
    }
}

#Preview("Tracking Active") {
    ZStack {
        Color.backgroundLight.ignoresSafeArea()

        VStack {
            Spacer()
            TrackingFAB(
                isTracking: .constant(true),
                onToggleTracking: {},
                onAddPhoto: {},
                onAddNote: {},
                onCheckIn: {}
            )
        }
    }
}
