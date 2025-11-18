import ActivityKit
import SwiftUI
import WidgetKit

/// Live Activity widget for location tracking
/// Displays real-time tracking information on Lock Screen and Dynamic Island
@available(iOS 16.1, *)
struct TrackingLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: TrackingActivityAttributes.self) { context in
            // Lock Screen / Banner UI
            LockScreenLiveActivityView(context: context)
        } dynamicIsland: { context in
            // Dynamic Island UI
            DynamicIsland {
                // Expanded region
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 4) {
                        Label {
                            Text(context.state.formattedDistance)
                                .font(.title3)
                                .fontWeight(.semibold)
                        } icon: {
                            Image(systemName: "location.fill")
                                .foregroundColor(.blue)
                        }
                        Text("Distance")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }

                DynamicIslandExpandedRegion(.trailing) {
                    VStack(alignment: .trailing, spacing: 4) {
                        Text(context.state.formattedDuration)
                            .font(.title3)
                            .fontWeight(.semibold)
                        Text("Duration")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }

                DynamicIslandExpandedRegion(.center) {
                    // Optional: Add speed or other metrics
                    if let speed = context.state.formattedSpeed {
                        VStack(spacing: 4) {
                            Text(speed)
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.blue)
                            Text("Speed")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        .padding(.vertical, 8)
                    }
                }

                DynamicIslandExpandedRegion(.bottom) {
                    HStack {
                        if let location = context.state.currentLocation {
                            HStack(spacing: 6) {
                                Image(systemName: "mappin.circle.fill")
                                    .font(.caption)
                                    .foregroundColor(.blue)
                                Text(location)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            }
                        }

                        Spacer()

                        StatusIndicator(status: context.state.status)
                    }
                    .padding(.top, 8)
                }
            } compactLeading: {
                // Compact leading (when not expanded)
                Image(systemName: "location.fill")
                    .foregroundColor(.blue)
            } compactTrailing: {
                // Compact trailing (when not expanded)
                Text(context.state.formattedDistance)
                    .font(.caption)
                    .fontWeight(.semibold)
            } minimal: {
                // Minimal view (when multiple activities)
                Image(systemName: "location.fill")
                    .foregroundColor(.blue)
            }
        }
    }
}

/// Lock Screen Live Activity View
@available(iOS 16.1, *)
struct LockScreenLiveActivityView: View {
    let context: ActivityViewContext<TrackingActivityAttributes>

    var body: some View {
        VStack(spacing: 16) {
            // Header
            HStack {
                Image(systemName: "location.fill")
                    .foregroundColor(.blue)
                Text("TrailGlass")
                    .font(.headline)
                    .fontWeight(.bold)

                Spacer()

                StatusIndicator(status: context.state.status)
            }

            // Main stats
            HStack(spacing: 0) {
                // Distance
                StatCard(
                    value: context.state.formattedDistance,
                    label: "Distance",
                    icon: "figure.walk"
                )

                Divider()
                    .frame(height: 40)

                // Duration
                StatCard(
                    value: context.state.formattedDuration,
                    label: "Time",
                    icon: "clock.fill"
                )

                if let speed = context.state.formattedSpeed {
                    Divider()
                        .frame(height: 40)

                    // Speed
                    StatCard(
                        value: speed,
                        label: "Speed",
                        icon: "speedometer"
                    )
                }
            }

            // Current location (if available)
            if let location = context.state.currentLocation {
                HStack(spacing: 8) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.caption)
                        .foregroundColor(.blue)
                    Text(location)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                    Spacer()
                    Text("\(context.state.locationCount) points")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(16)
    }
}

/// Stat card component for displaying tracking metrics
struct StatCard: View {
    let value: String
    let label: String
    let icon: String

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundColor(.blue)

            Text(value)
                .font(.title3)
                .fontWeight(.semibold)
                .minimumScaleFactor(0.8)

            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

/// Status indicator showing tracking state
struct StatusIndicator: View {
    let status: TrackingStatus

    var body: some View {
        HStack(spacing: 4) {
            Circle()
                .fill(statusColor)
                .frame(width: 8, height: 8)

            Text(statusText)
                .font(.caption2)
                .fontWeight(.medium)
                .foregroundColor(statusColor)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(statusColor.opacity(0.15))
        .cornerRadius(8)
    }

    private var statusColor: Color {
        switch status {
        case .active:
            return .green
        case .paused:
            return .orange
        case .stopped:
            return .red
        }
    }

    private var statusText: String {
        switch status {
        case .active:
            return "Tracking"
        case .paused:
            return "Paused"
        case .stopped:
            return "Stopped"
        }
    }
}

// Preview
@available(iOS 16.1, *)
struct TrackingLiveActivity_Previews: PreviewProvider {
    static let attributes = TrackingActivityAttributes(
        trackingMode: "Active",
        tripName: "Morning Run"
    )

    static let contentState = TrackingActivityAttributes.ContentState(
        status: .active,
        distanceMeters: 2543.5,
        durationSeconds: 1847,
        currentSpeedMps: 1.8,
        locationCount: 152,
        lastUpdate: Date(),
        currentLocation: "Central Park, NYC"
    )

    static var previews: some View {
        attributes
            .previewContext(contentState, viewKind: .content)
            .previewDisplayName("Lock Screen")

        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.compact))
            .previewDisplayName("Dynamic Island Compact")

        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.expanded))
            .previewDisplayName("Dynamic Island Expanded")
    }
}
