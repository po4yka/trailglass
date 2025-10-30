package com.po4yka.trailglass.location.tracking

import android.content.Context
import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.repository.LocationRepository

/**
 * Android implementation of LocationTrackerFactory.
 */
actual class LocationTrackerFactory(private val context: Context) {
    actual fun create(
        repository: LocationRepository,
        configuration: TrackingConfiguration,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ): LocationTracker {
        // In a real implementation, deviceId and userId would come from a user session
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

        val userId = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

        return AndroidLocationTracker(
            context = context,
            repository = repository,
            configuration = configuration,
            deviceId = deviceId,
            userId = userId,
            coroutineScope = coroutineScope
        )
    }
}
