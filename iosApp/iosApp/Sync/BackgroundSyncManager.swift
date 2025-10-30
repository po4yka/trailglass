import Foundation
import BackgroundTasks
import shared

/// Manages background synchronization for iOS using BackgroundTasks framework
class BackgroundSyncManager {

    static let shared = BackgroundSyncManager()

    // Background task identifier - must match Info.plist entry
    private let syncTaskIdentifier = "com.po4yka.trailglass.sync"

    private var syncManager: SyncManager?

    private init() {}

    /// Configure background sync with required dependencies
    func configure(syncManager: SyncManager) {
        self.syncManager = syncManager
        print("BackgroundSyncManager configured successfully")
    }

    /// Register background task handlers
    /// Call this in AppDelegate's didFinishLaunchingWithOptions
    func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: syncTaskIdentifier,
            using: nil
        ) { task in
            self.handleSyncTask(task: task as! BGAppRefreshTask)
        }

        print("Background sync task registered: \(syncTaskIdentifier)")
    }

    /// Schedule next background sync
    func scheduleBackgroundSync() {
        let request = BGAppRefreshTaskRequest(identifier: syncTaskIdentifier)

        // Schedule to run in 1 hour
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60)

        do {
            try BGTaskScheduler.shared.submit(request)
            print("Background sync scheduled successfully")
        } catch {
            print("Failed to schedule background sync: \(error)")
        }
    }

    /// Cancel all scheduled background sync tasks
    func cancelBackgroundSync() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: syncTaskIdentifier)
        print("Background sync cancelled")
    }

    /// Handle background sync task
    private func handleSyncTask(task: BGAppRefreshTask) {
        print("Background sync task started")

        // Schedule next sync
        scheduleBackgroundSync()

        guard let syncManager = syncManager else {
            print("SyncManager not configured")
            task.setTaskCompleted(success: false)
            return
        }

        // Create task to monitor
        let syncTask = Task {
            await performBackgroundSync(syncManager: syncManager)
        }

        // Handle task expiration
        task.expirationHandler = {
            print("Background sync task expired")
            syncTask.cancel()
        }

        // Wait for completion
        Task {
            let success = await syncTask.value
            task.setTaskCompleted(success: success)
        }
    }

    /// Perform the actual sync operation
    private func performBackgroundSync(syncManager: SyncManager) async -> Bool {
        print("Performing background sync...")

        do {
            // Perform full sync using SyncManager
            let result = try await syncManager.performFullSync()

            if result.isSuccess() {
                if let syncResult = result.getOrNull() as? SyncResult {
                    print("Background sync completed successfully: " +
                          "\(syncResult.uploaded) uploaded, " +
                          "\(syncResult.downloaded) downloaded, " +
                          "\(syncResult.conflicts) conflicts")
                } else {
                    print("Background sync completed successfully")
                }
                return true
            } else {
                if let error = result.exceptionOrNull() {
                    print("Background sync failed: \(error.localizedDescription)")
                } else {
                    print("Background sync failed")
                }
                return false
            }
        } catch {
            print("Background sync error: \(error)")
            return false
        }
    }

    /// Trigger immediate sync (when app is in foreground)
    func triggerImmediateSync() async -> Bool {
        guard let syncManager = syncManager else {
            print("SyncManager not configured")
            return false
        }

        return await performBackgroundSync(syncManager: syncManager)
    }
}

/// Extension to handle Kotlin Result in Swift
extension KotlinResult {
    func isSuccess() -> Bool {
        return self.exceptionOrNull() == nil
    }

    func isFailure() -> Bool {
        return self.exceptionOrNull() != nil
    }
}

/// Helper to call suspend functions from Swift
extension SyncManager {
    func performFullSync() async throws -> KotlinResult<SyncResult> {
        return try await withCheckedThrowingContinuation { continuation in
            self.performFullSync { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(throwing: NSError(
                        domain: "SyncError",
                        code: -1,
                        userInfo: [NSLocalizedDescriptionKey: "Unknown sync error"]
                    ))
                }
            }
        }
    }
}

// MARK: - Info.plist Configuration
/*
 Add this to your Info.plist:

 <key>BGTaskSchedulerPermittedIdentifiers</key>
 <array>
     <string>com.po4yka.trailglass.sync</string>
 </array>

 And add Background Modes capability with "Background fetch" enabled.
 */

// MARK: - AppDelegate Integration
/*
 In your AppDelegate or App struct:

 func application(
     _ application: UIApplication,
     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
 ) -> Bool {
     // Register background tasks
     BackgroundSyncManager.shared.registerBackgroundTasks()

     // Configure with SyncManager from DI
     let appComponent = createIOSAppComponent()
     BackgroundSyncManager.shared.configure(syncManager: appComponent.syncManager)

     // Schedule first sync
     BackgroundSyncManager.shared.scheduleBackgroundSync()

     return true
 }

 // For testing in simulator
 func applicationDidEnterBackground(_ application: UIApplication) {
     BackgroundSyncManager.shared.scheduleBackgroundSync()
 }
 */
