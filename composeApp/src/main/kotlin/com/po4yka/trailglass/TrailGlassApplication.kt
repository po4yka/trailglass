package com.po4yka.trailglass

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.po4yka.trailglass.crash.CrashHandler
import com.po4yka.trailglass.di.AndroidGeofencingModule
import com.po4yka.trailglass.di.AppComponent
import com.po4yka.trailglass.di.createAndroidAppComponent
import com.po4yka.trailglass.sync.SyncScheduler
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * TrailGlass Application class.
 *
 * Initializes the dependency injection component and provides application-wide dependencies.
 */
class TrailGlassApplication : Application() {
    /** Application-level DI component. Provides all application dependencies (repositories, controllers, etc.) */
    val appComponent: AppComponent by lazy {
        createAndroidAppComponent(applicationContext)
    }

    /** Android-specific geofencing module for region monitoring. */
    val geofencingModule: AndroidGeofencingModule by lazy {
        AndroidGeofencingModule(applicationContext, appComponent)
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase Crashlytics
        initializeCrashlytics()

        // Initialize Firebase Performance Monitoring
        initializePerformanceMonitoring()

        // Install custom crash handler
        installCrashHandler()

        // Start network connectivity monitoring
        startNetworkMonitoring()

        // Initialize sync coordinator
        initializeSyncCoordinator()

        // Schedule background sync
        scheduleBackgroundSync()
    }

    override fun onTerminate() {
        super.onTerminate()

        // Stop network connectivity monitoring
        stopNetworkMonitoring()
    }

    /** Initialize SyncCoordinator on app startup. */
    private fun initializeSyncCoordinator() {
        applicationScope.launch {
            try {
                // Access syncCoordinator to ensure it's initialized
                // The actual initialization happens in the DI component
                appComponent.syncCoordinator
                logger.info { "SyncCoordinator initialized successfully" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to initialize SyncCoordinator: ${e.message}" }
            }
        }
    }

    /** Schedule periodic background sync using WorkManager. */
    private fun scheduleBackgroundSync() {
        try {
            SyncScheduler.schedulePeriodicSync(
                context = applicationContext,
                intervalMinutes = 60
            )
            logger.info { "Background sync scheduled successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to schedule background sync: ${e.message}" }
        }
    }

    /** Start network connectivity monitoring. */
    private fun startNetworkMonitoring() {
        try {
            appComponent.networkConnectivityMonitor.startMonitoring()
            logger.info { "Network connectivity monitoring started successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to start network monitoring: ${e.message}" }
        }
    }

    /** Stop network connectivity monitoring. */
    private fun stopNetworkMonitoring() {
        try {
            appComponent.networkConnectivityMonitor.stopMonitoring()
            logger.info { "Network connectivity monitoring stopped" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to stop network monitoring: ${e.message}" }
        }
    }

    /** Initialize Firebase Crashlytics. */
    private fun initializeCrashlytics() {
        try {
            // Enable Crashlytics collection based on user preference
            // For now, enable by default. Later, tie this to user settings.
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)

            // Set custom keys for better crash analysis
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("debug_mode", BuildConfig.DEBUG)

            logger.info { "Firebase Crashlytics initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Firebase Crashlytics: ${e.message}" }
        }
    }

    /** Initialize Firebase Performance Monitoring. */
    private fun initializePerformanceMonitoring() {
        try {
            // Enable Performance Monitoring collection
            // For now, enable by default. Later, tie this to user settings.
            val performance = FirebasePerformance.getInstance()
            performance.isPerformanceCollectionEnabled = true

            logger.info { "Firebase Performance Monitoring initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Firebase Performance Monitoring: ${e.message}" }
        }
    }

    /** Install custom crash handler for uncaught exceptions. */
    private fun installCrashHandler() {
        try {
            val crashHandler = CrashHandler(appComponent.crashReportingService)
            crashHandler.install()
            logger.info { "Custom crash handler installed successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to install crash handler: ${e.message}" }
        }
    }

    /** Trigger immediate sync (can be called from UI). */
    fun triggerImmediateSync() {
        SyncScheduler.triggerImmediateSync(applicationContext)
    }
}
