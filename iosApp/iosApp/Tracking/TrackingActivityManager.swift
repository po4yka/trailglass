import ActivityKit
import Foundation
import shared

/// Manages Live Activities for location tracking
/// This class bridges between Kotlin location tracking and iOS Live Activities
@available(iOS 16.1, *)
class TrackingActivityManager: ObservableObject {
    static let shared = TrackingActivityManager()

    @Published private(set) var currentActivity: Activity<TrackingActivityAttributes>?
    @Published private(set) var isActivityActive: Bool = false

    private var stateObserver: Kotlinx_coroutines_coreJob?

    private init() {}

    /// Start a Live Activity for tracking
    /// - Parameters:
    ///   - trackingMode: The tracking mode being used
    ///   - tripName: Optional name for the trip
    func startActivity(trackingMode: String, tripName: String? = nil) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else {
            print("Live Activities are not enabled")
            return
        }

        // Check if already has active activity
        if let existingActivity = Activity<TrackingActivityAttributes>.activities.first {
            currentActivity = existingActivity
            isActivityActive = true
            return
        }

        let attributes = TrackingActivityAttributes(
            trackingMode: trackingMode,
            tripName: tripName
        )

        let initialState = TrackingActivityAttributes.ContentState(
            status: .active,
            distanceMeters: 0.0,
            durationSeconds: 0,
            currentSpeedMps: nil,
            locationCount: 0,
            lastUpdate: Date(),
            currentLocation: nil
        )

        do {
            let activity = try Activity<TrackingActivityAttributes>.request(
                attributes: attributes,
                contentState: initialState,
                pushType: nil
            )

            currentActivity = activity
            isActivityActive = true

            print("Started Live Activity: \(activity.id)")
        } catch {
            print("Failed to start Live Activity: \(error.localizedDescription)")
        }
    }

    /// Update the Live Activity with new tracking data
    /// - Parameters:
    ///   - distanceMeters: Distance traveled in meters
    ///   - durationSeconds: Tracking duration in seconds
    ///   - currentSpeedMps: Current speed in meters per second
    ///   - locationCount: Number of locations recorded
    ///   - currentLocation: Current location name
    ///   - status: Tracking status
    func updateActivity(
        distanceMeters: Double,
        durationSeconds: Int,
        currentSpeedMps: Double? = nil,
        locationCount: Int,
        currentLocation: String? = nil,
        status: TrackingStatus = .active
    ) {
        guard let activity = currentActivity else {
            print("No active Live Activity to update")
            return
        }

        let updatedState = TrackingActivityAttributes.ContentState(
            status: status,
            distanceMeters: distanceMeters,
            durationSeconds: durationSeconds,
            currentSpeedMps: currentSpeedMps,
            locationCount: locationCount,
            lastUpdate: Date(),
            currentLocation: currentLocation
        )

        Task {
            await activity.update(using: updatedState)
        }
    }

    /// Pause the tracking (updates status to paused)
    func pauseActivity() {
        guard let activity = currentActivity else { return }

        var updatedState = activity.contentState
        updatedState.status = .paused

        Task {
            await activity.update(using: updatedState)
        }
    }

    /// Resume the tracking (updates status to active)
    func resumeActivity() {
        guard let activity = currentActivity else { return }

        var updatedState = activity.contentState
        updatedState.status = .active

        Task {
            await activity.update(using: updatedState)
        }
    }

    /// Stop and end the Live Activity
    /// - Parameter finalDistance: Final distance to display
    func endActivity(finalDistance: Double? = nil) {
        guard let activity = currentActivity else { return }

        Task {
            var finalState = activity.contentState
            finalState.status = .stopped
            if let distance = finalDistance {
                finalState.distanceMeters = distance
            }

            await activity.end(using: finalState, dismissalPolicy: .immediate)

            DispatchQueue.main.async {
                self.currentActivity = nil
                self.isActivityActive = false
            }
        }
    }

    /// Observe location tracker state and update Live Activity
    /// - Parameter tracker: The location tracker to observe
    func observeTracker(_ tracker: LocationTracker) {
        var lastUpdateTime = Date()
        var startTime: Date?

        stateObserver = tracker.trackingState.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                // Start activity when tracking begins
                if state.isTracking && !self.isActivityActive {
                    startTime = Date()
                    self.startActivity(trackingMode: state.currentMode.name)
                }

                // Update activity with tracking data
                if self.isActivityActive {
                    // Calculate duration
                    let duration = startTime.map { Int(Date().timeIntervalSince($0)) } ?? 0

                    // Update every 5 seconds to avoid too frequent updates
                    if Date().timeIntervalSince(lastUpdateTime) >= 5.0 {
                        self.updateActivity(
                            distanceMeters: state.totalDistance,
                            durationSeconds: duration,
                            currentSpeedMps: state.currentSpeed,
                            locationCount: state.locationCount,
                            currentLocation: state.lastLocation?.placeName,
                            status: state.isTracking ? .active : .paused
                        )
                        lastUpdateTime = Date()
                    }
                }

                // End activity when tracking stops
                if !state.isTracking && self.isActivityActive {
                    self.endActivity(finalDistance: state.totalDistance)
                }
            }
        }
    }

    /// Stop observing the tracker
    func stopObserving() {
        stateObserver?.cancel(cause: nil)
        stateObserver = nil
    }

    deinit {
        stopObserving()
    }
}
