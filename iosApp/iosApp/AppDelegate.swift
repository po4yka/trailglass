import UIKit
import BackgroundTasks
import shared

class AppDelegate: UIResponder, UIApplicationDelegate {

    // Application DI component
    lazy var appComponent: AppComponent = {
        return CreateKt.createIOSAppComponent()
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {

        print("TrailGlass iOS app starting up...")

        // Initialize sync coordinator
        initializeSyncCoordinator()

        // Register background tasks
        BackgroundSyncManager.shared.registerBackgroundTasks()

        // Configure BackgroundSyncManager with SyncManager from DI
        BackgroundSyncManager.shared.configure(syncManager: appComponent.syncManager)

        // Schedule first background sync
        BackgroundSyncManager.shared.scheduleBackgroundSync()

        print("TrailGlass iOS app initialized successfully")

        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Schedule background sync when app goes to background
        BackgroundSyncManager.shared.scheduleBackgroundSync()
    }

    // MARK: - Private Methods

    private func initializeSyncCoordinator() {
        // Initialize sync coordinator on app startup
        Task {
            do {
                // Access syncCoordinator to ensure it's initialized
                // The actual initialization happens in the DI component
                _ = appComponent.syncCoordinator
                print("SyncCoordinator initialized successfully")
            } catch {
                print("Failed to initialize SyncCoordinator: \(error)")
            }
        }
    }

    // Public method to trigger immediate sync (can be called from UI)
    func triggerImmediateSync() {
        Task {
            let success = await BackgroundSyncManager.shared.triggerImmediateSync()
            print("Immediate sync triggered: \(success ? "success" : "failed")")
        }
    }
}
