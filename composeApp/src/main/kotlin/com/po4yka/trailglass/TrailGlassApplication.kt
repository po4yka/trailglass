package com.po4yka.trailglass

import android.app.Application
import android.os.StrictMode
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
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

/**
 * TrailGlass Application class.
 *
 * Initializes the dependency injection component and provides application-wide dependencies.
 * Heavy initialization is moved off the main thread to improve startup performance.
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
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Track if background initialization is complete */
    private val backgroundInitComplete = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()

        // Critical path: Only install crash handler synchronously
        // This ensures we capture any crashes during the rest of initialization
        installCrashHandler()

        // Move all heavy initialization to background thread
        ioScope.launch {
            initializeInBackground()
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        // Stop network connectivity monitoring
        stopNetworkMonitoring()
    }

    /**
     * Perform heavy initialization tasks in background.
     * This keeps the main thread responsive during app startup.
     */
    private suspend fun initializeInBackground() {
        val startTime = System.currentTimeMillis()
        logger.info { "Starting background initialization..." }

        try {
            // Initialize Firebase services (can be slow due to network calls)
            initializeCrashlytics()
            initializePerformanceMonitoring()

            // Start network connectivity monitoring
            startNetworkMonitoring()

            // Initialize sync coordinator
            initializeSyncCoordinator()

            // Schedule background sync (WorkManager operations)
            scheduleBackgroundSync()

            backgroundInitComplete.set(true)

            val duration = System.currentTimeMillis() - startTime
            logger.info { "Background initialization completed in ${duration}ms" }
        } catch (e: Exception) {
            logger.error(e) { "Error during background initialization: ${e.message}" }
        }
    }

    /** Initialize SyncCoordinator on app startup. */
    private suspend fun initializeSyncCoordinator() {
        try {
            // Access syncCoordinator to ensure it's initialized
            // The actual initialization happens in the DI component
            appComponent.syncCoordinator
            logger.info { "SyncCoordinator initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize SyncCoordinator: ${e.message}" }
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

    /** Check if background initialization is complete. */
    fun isBackgroundInitComplete(): Boolean = backgroundInitComplete.get()

    /** Trigger immediate sync (can be called from UI). */
    fun triggerImmediateSync() {
        if (backgroundInitComplete.get()) {
            SyncScheduler.triggerImmediateSync(applicationContext)
        } else {
            // Queue for later execution when init is complete
            ioScope.launch {
                // Wait for background init to complete
                while (!backgroundInitComplete.get()) {
                    kotlinx.coroutines.delay(100)
                }
                SyncScheduler.triggerImmediateSync(applicationContext)
            }
        }
    }
}
