package com.po4yka.trailglass.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.po4yka.trailglass.TrailGlassApplication
import com.po4yka.trailglass.data.repository.RegionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that restores geofences after device reboot.
 * Geofences are removed when the device is restarted, so this receiver
 * re-registers all active geofences.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.i(TAG, "Device boot completed, restoring geofences")

        scope.launch {
            try {
                val application = context.applicationContext as? TrailGlassApplication
                if (application == null) {
                    Log.e(TAG, "Application is not TrailGlassApplication")
                    return@launch
                }

                // Get repositories from DI
                val regionRepository = getRegionRepository(application)
                val geofencingClientWrapper = GeofencingClientWrapper(context)

                // Get user ID (this should come from your auth system)
                val userId = getCurrentUserId(application)
                if (userId == null) {
                    Log.w(TAG, "No user logged in, skipping geofence restoration")
                    return@launch
                }

                // Get all active regions
                val activeRegions = regionRepository.getActiveRegions(userId)

                if (activeRegions.isEmpty()) {
                    Log.i(TAG, "No active regions to restore")
                    return@launch
                }

                // Re-register geofences
                geofencingClientWrapper.addGeofences(activeRegions)

                Log.i(TAG, "Restored ${activeRegions.size} geofences after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring geofences after boot", e)
            }
        }
    }

    /**
     * Get RegionRepository from the application component.
     * This is a placeholder - implement based on your DI setup.
     */
    private fun getRegionRepository(application: TrailGlassApplication): RegionRepository {
        // TODO: Get from DI container
        // Example: application.appComponent.regionRepository
        throw NotImplementedError("RegionRepository DI integration needed")
    }

    /**
     * Get current user ID from the application.
     * This is a placeholder - implement based on your auth system.
     */
    private fun getCurrentUserId(application: TrailGlassApplication): String? {
        // TODO: Get from auth service
        // Example: application.appComponent.authService.getCurrentUserId()
        return null
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
