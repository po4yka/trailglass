package com.po4yka.trailglass

import android.app.Application
import com.po4yka.trailglass.di.AppComponent
import com.po4yka.trailglass.di.createAndroidAppComponent
import com.po4yka.trailglass.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * TrailGlass Application class.
 *
 * Initializes the dependency injection component and provides
 * application-wide dependencies.
 */
class TrailGlassApplication : Application() {

    /**
     * Application-level DI component.
     * Provides all application dependencies (repositories, controllers, etc.)
     */
    val appComponent: AppComponent by lazy {
        createAndroidAppComponent(applicationContext)
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize sync coordinator
        initializeSyncCoordinator()

        // Schedule background sync
        scheduleBackgroundSync()
    }

    /**
     * Initialize SyncCoordinator on app startup.
     */
    private fun initializeSyncCoordinator() {
        applicationScope.launch {
            try {
                // Access syncCoordinator to ensure it's initialized
                // The actual initialization happens in the DI component
                appComponent.syncCoordinator
                println("SyncCoordinator initialized successfully")
            } catch (e: Exception) {
                println("Failed to initialize SyncCoordinator: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Schedule periodic background sync using WorkManager.
     */
    private fun scheduleBackgroundSync() {
        try {
            SyncScheduler.schedulePeriodicSync(
                context = applicationContext,
                intervalMinutes = 60
            )
            println("Background sync scheduled successfully")
        } catch (e: Exception) {
            println("Failed to schedule background sync: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Trigger immediate sync (can be called from UI).
     */
    fun triggerImmediateSync() {
        SyncScheduler.triggerImmediateSync(applicationContext)
    }
}
