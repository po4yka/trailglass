import UIKit
import BackgroundTasks
import Shared

class AppDelegate: UIResponder, UIApplicationDelegate {
    // Application DI component
    lazy var appComponent: AppComponent = {
        return InjectIOSAppComponent(platformModule: IOSPlatformModule())
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        AppLogger.info("TrailGlass iOS app starting up...", category: "AppDelegate")

        // Initialize logging configuration
        initializeLogging()

        // Initialize sync coordinator
        initializeSyncCoordinator()

        // Register background tasks
        BackgroundSyncManager.shared.registerBackgroundTasks()

        // Configure BackgroundSyncManager with SyncManager from DI
        BackgroundSyncManager.shared.configure(syncManager: appComponent.syncManager)

        // Schedule first background sync
        BackgroundSyncManager.shared.scheduleBackgroundSync()

        AppLogger.info("TrailGlass iOS app initialized successfully", category: "AppDelegate")

        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        AppLogger.debug("App entering background", category: "AppDelegate")
        // Schedule background sync when app goes to background
        BackgroundSyncManager.shared.scheduleBackgroundSync()
    }

    // MARK: - Private Methods

    private func initializeLogging() {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif

        // Initialize Kotlin logging configuration
        LoggingConfig.shared.initialize(isDebugBuild: isDebug)
        AppLogger.debug("Logging initialized: isDebug=\(isDebug)", category: "AppDelegate")
    }

    private func initializeSyncCoordinator() {
        // Initialize sync coordinator on app startup
        Task {
            do {
                // Access syncCoordinator to ensure it's initialized
                // The actual initialization happens in the DI component
                _ = appComponent.syncCoordinator
                AppLogger.info("SyncCoordinator initialized successfully", category: "Sync")
            } catch {
                AppLogger.error("Failed to initialize SyncCoordinator: \(error)", category: "Sync")
            }
        }
    }

    // Public method to trigger immediate sync (can be called from UI)
    func triggerImmediateSync() {
        Task {
            let success = await BackgroundSyncManager.shared.triggerImmediateSync()
            AppLogger.info("Immediate sync triggered: \(success ? "success" : "failed")", category: "Sync")
        }
    }
}
