import ActivityKit
import Foundation

/// Attributes defining the Live Activity for location tracking
/// These define the static and dynamic content shown in the Live Activity
struct TrackingActivityAttributes: ActivityAttributes {
    /// Static content that doesn't change during the activity
    public struct ContentState: Codable, Hashable {
        /// Current tracking status
        var status: TrackingStatus

        /// Distance traveled in meters
        var distanceMeters: Double

        /// Duration in seconds
        var durationSeconds: Int

        /// Current speed in meters per second
        var currentSpeedMps: Double?

        /// Number of locations recorded
        var locationCount: Int

        /// Last updated timestamp
        var lastUpdate: Date

        /// Current location name (if available)
        var currentLocation: String?

        // Computed properties for display
        var formattedDistance: String {
            let km = distanceMeters / 1000.0
            if km < 1.0 {
                return String(format: "%.0f m", distanceMeters)
            } else {
                return String(format: "%.2f km", km)
            }
        }

        var formattedDuration: String {
            let hours = durationSeconds / 3600
            let minutes = (durationSeconds % 3600) / 60
            let seconds = durationSeconds % 60

            if hours > 0 {
                return String(format: "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                return String(format: "%d:%02d", minutes, seconds)
            }
        }

        var formattedSpeed: String? {
            guard let speed = currentSpeedMps else { return nil }
            let kmh = speed * 3.6
            return String(format: "%.1f km/h", kmh)
        }
    }

    /// Tracking mode/configuration (static)
    var trackingMode: String

    /// Trip name or identifier (static)
    var tripName: String?
}

/// Tracking status enum
enum TrackingStatus: String, Codable {
    case active = "active"
    case paused = "paused"
    case stopped = "stopped"
}
