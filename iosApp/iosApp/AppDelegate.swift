import UIKit
import BackgroundTasks
import Shared

class AppDelegate: UIResponder, UIApplicationDelegate {
    // Application DI component
    lazy var appComponent: AppComponent = {
        return InjectIOSAppComponent(platformModule: IOSPlatformModule())
    }()

    // Crash handler instance - kept alive for app lifetime
    private var crashHandler: CrashHandler?

    // Track if background initialization is complete
    private var backgroundInitComplete = false

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        let startTime = CFAbsoluteTimeGetCurrent()
        AppLogger.info("TrailGlass iOS app starting up...", category: "AppDelegate")

        // Critical path: Initialize logging first (lightweight)
        initializeLogging()

        // Critical path: Initialize crash reporting early to catch startup crashes
        // This must be synchronous to catch crashes during init
        initializeCrashReporting()

        // Register background tasks (must be done in didFinishLaunching)
        BackgroundSyncManager.shared.registerBackgroundTasks()

        // Move heavy initialization to background
        Task.detached(priority: .utility) { [weak self] in
            await self?.initializeInBackground()
        }

        let duration = (CFAbsoluteTimeGetCurrent() - startTime) * 1000
        AppLogger.info("TrailGlass iOS app main thread init completed in \(Int(duration))ms", category: "AppDelegate")

        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        AppLogger.debug("App entering background", category: "AppDelegate")
        // Schedule background sync when app goes to background
        BackgroundSyncManager.shared.scheduleBackgroundSync()
    }

    // MARK: - Background Initialization

    /// Perform heavy initialization tasks in background.
    /// This keeps the main thread responsive during app startup.
    private func initializeInBackground() async {
        let startTime = CFAbsoluteTimeGetCurrent()
        AppLogger.info("Starting background initialization...", category: "AppDelegate")

        // Configure BackgroundSyncManager with SyncManager from DI
        BackgroundSyncManager.shared.configure(syncManager: appComponent.syncManager)

        // Schedule first background sync
        BackgroundSyncManager.shared.scheduleBackgroundSync()

        // Initialize sync coordinator
        await initializeSyncCoordinatorAsync()

        backgroundInitComplete = true

        let duration = (CFAbsoluteTimeGetCurrent() - startTime) * 1000
        AppLogger.info("Background initialization completed in \(Int(duration))ms", category: "AppDelegate")
    }

    /// Async version of sync coordinator initialization
    private func initializeSyncCoordinatorAsync() async {
        do {
            // Access syncCoordinator to ensure it's initialized
            // The actual initialization happens in the DI component
            _ = appComponent.syncCoordinator
            AppLogger.info("SyncCoordinator initialized successfully", category: "Sync")
        } catch {
            AppLogger.error("Failed to initialize SyncCoordinator: \(error)", category: "Sync")
        }
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

    private func initializeCrashReporting() {
        AppLogger.debug("Initializing crash reporting...", category: "Crash")

        // Create and install CrashHandler from Kotlin shared code
        crashHandler = CrashHandler(crashReportingService: appComponent.crashReportingService)
        crashHandler?.install()

        // Set up Swift-level exception handling
        CrashReporter.shared.configure(crashReportingService: appComponent.crashReportingService)

        // Check if app crashed on previous execution
        if appComponent.crashReportingService.didCrashOnPreviousExecution() {
            AppLogger.warn("App crashed on previous execution", category: "Crash")
        }

        AppLogger.info("Crash reporting initialized", category: "Crash")
    }

    // Public method to trigger immediate sync (can be called from UI)
    func triggerImmediateSync() {
        Task {
            let success = await BackgroundSyncManager.shared.triggerImmediateSync()
            AppLogger.info("Immediate sync triggered: \(success ? "success" : "failed")", category: "Sync")
        }
    }
}
