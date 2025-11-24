import Foundation
import UserNotifications
import Shared
import Combine

/**
 Manages local notifications for region (geofence) transitions.

 Handles:
 - Requesting notification permissions
 - Showing notifications when entering/exiting regions
 - Configuring notification content and actions
 - Handling notification taps to open the app
 */
class RegionNotificationManager: NSObject, ObservableObject {

    // MARK: - Properties

    private let notificationCenter = UNUserNotificationCenter.current()

    @Published var isAuthorized: Bool = false
    @Published var authorizationStatus: UNAuthorizationStatus = .notDetermined

    let notificationTappedSubject = PassthroughSubject<String, Never>()

    // Notification category and actions
    private let regionTransitionCategoryId = "REGION_TRANSITION"
    private let viewOnMapActionId = "VIEW_ON_MAP"
    private let dismissActionId = "DISMISS"

    // MARK: - Initialization

    override init() {
        super.init()
        notificationCenter.delegate = self
        checkAuthorizationStatus()
        registerNotificationCategories()
    }

    // MARK: - Public Methods

    /**
     Request notification permissions from the user.
     */
    func requestAuthorization() async -> Bool {
        do {
            let granted = try await notificationCenter.requestAuthorization(
                options: [.alert, .sound, .badge]
            )

            await MainActor.run {
                self.isAuthorized = granted
            }

            print("Notification permission: \(granted ? "granted" : "denied")")
            return granted
        } catch {
            print("Failed to request notification authorization: \(error)")
            return false
        }
    }

    /**
     Show notification when entering a region.

     - Parameter region: The region that was entered
     */
    func notifyRegionEntered(_ region: Region) {
        guard region.notificationsEnabled else {
            print("Notifications disabled for region: \(region.name)")
            return
        }

        let content = UNMutableNotificationContent()
        content.title = "Entered \(region.name)"
        content.body = region.description ?? "You've arrived at \(region.name)"
        content.sound = .default
        content.categoryIdentifier = regionTransitionCategoryId

        // Add region info to user info for handling tap
        content.userInfo = [
            "regionId": region.id,
            "regionName": region.name,
            "eventType": "enter",
            "latitude": region.latitude,
            "longitude": region.longitude
        ]

        // Badge increment
        content.badge = NSNumber(value: 1)

        let request = UNNotificationRequest(
            identifier: "region-enter-\(region.id)-\(Date().timeIntervalSince1970)",
            content: content,
            trigger: nil // Show immediately
        )

        notificationCenter.add(request) { error in
            if let error = error {
                print("Failed to show region enter notification: \(error)")
            } else {
                print("Region enter notification scheduled for: \(region.name)")
            }
        }
    }

    /**
     Show notification when exiting a region.

     - Parameter region: The region that was exited
     */
    func notifyRegionExited(_ region: Region) {
        guard region.notificationsEnabled else {
            print("Notifications disabled for region: \(region.name)")
            return
        }

        let content = UNMutableNotificationContent()
        content.title = "Left \(region.name)"
        content.body = region.description ?? "You've left \(region.name)"
        content.sound = .default
        content.categoryIdentifier = regionTransitionCategoryId

        // Add region info to user info for handling tap
        content.userInfo = [
            "regionId": region.id,
            "regionName": region.name,
            "eventType": "exit",
            "latitude": region.latitude,
            "longitude": region.longitude
        ]

        let request = UNNotificationRequest(
            identifier: "region-exit-\(region.id)-\(Date().timeIntervalSince1970)",
            content: content,
            trigger: nil // Show immediately
        )

        notificationCenter.add(request) { error in
            if let error = error {
                print("Failed to show region exit notification: \(error)")
            } else {
                print("Region exit notification scheduled for: \(region.name)")
            }
        }
    }

    /**
     Clear all delivered notifications.
     */
    func clearAllNotifications() {
        notificationCenter.removeAllDeliveredNotifications()
        print("All notifications cleared")
    }

    /**
     Clear notifications for a specific region.

     - Parameter regionId: ID of the region
     */
    func clearNotifications(forRegionId regionId: String) {
        notificationCenter.getDeliveredNotifications { notifications in
            let identifiersToRemove = notifications
                .filter { notification in
                    if let notificationRegionId = notification.request.content.userInfo["regionId"] as? String {
                        return notificationRegionId == regionId
                    }
                    return false
                }
                .map { $0.request.identifier }

            self.notificationCenter.removeDeliveredNotifications(withIdentifiers: identifiersToRemove)
            print("Cleared \(identifiersToRemove.count) notifications for region: \(regionId)")
        }
    }

    /**
     Reset badge count.
     */
    func resetBadgeCount() {
        UNUserNotificationCenter.current().setBadgeCount(0) { error in
            if let error = error {
                print("Failed to reset badge count: \(error)")
            }
        }
    }

    // MARK: - Private Methods

    private func checkAuthorizationStatus() {
        notificationCenter.getNotificationSettings { settings in
            DispatchQueue.main.async {
                self.authorizationStatus = settings.authorizationStatus
                self.isAuthorized = settings.authorizationStatus == .authorized
            }
        }
    }

    private func registerNotificationCategories() {
        // Create actions
        let viewOnMapAction = UNNotificationAction(
            identifier: viewOnMapActionId,
            title: "View on Map",
            options: [.foreground]
        )

        let dismissAction = UNNotificationAction(
            identifier: dismissActionId,
            title: "Dismiss",
            options: []
        )

        // Create category
        let regionTransitionCategory = UNNotificationCategory(
            identifier: regionTransitionCategoryId,
            actions: [viewOnMapAction, dismissAction],
            intentIdentifiers: [],
            options: []
        )

        // Register categories
        notificationCenter.setNotificationCategories([regionTransitionCategory])
        print("Notification categories registered")
    }
}

// MARK: - UNUserNotificationCenterDelegate

extension RegionNotificationManager: UNUserNotificationCenterDelegate {

    /**
     Handle notification when app is in foreground.
     */
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        print("Notification received while app in foreground: \(notification.request.identifier)")

        // Show notification even when app is in foreground
        completionHandler([.banner, .sound, .badge])
    }

    /**
     Handle notification tap.
     */
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo

        print("Notification tapped: \(response.actionIdentifier)")

        switch response.actionIdentifier {
        case UNNotificationDefaultActionIdentifier:
            // User tapped the notification itself
            handleNotificationTap(userInfo: userInfo)

        case viewOnMapActionId:
            // User tapped "View on Map" action
            handleNotificationTap(userInfo: userInfo)

        case dismissActionId:
            // User dismissed the notification
            print("Notification dismissed by user")

        default:
            break
        }

        completionHandler()
    }

    private func handleNotificationTap(userInfo: [AnyHashable: Any]) {
        guard let regionId = userInfo["regionId"] as? String else {
            print("No region ID in notification")
            return
        }

        print("Opening app to show region: \(regionId)")

        // Notify observers that notification was tapped
        notificationTappedSubject.send(regionId)

        // Clear badge when notification is handled
        resetBadgeCount()
    }
}
