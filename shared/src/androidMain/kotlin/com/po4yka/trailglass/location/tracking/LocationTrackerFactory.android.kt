package com.po4yka.trailglass.location.tracking

import android.content.Context
import com.po4yka.trailglass.data.repository.LocationRepository

/** Android implementation of LocationTrackerFactory. */
actual class LocationTrackerFactory(
    private val context: Context,
    private val deviceId: String,
    private val userId: String
) {
    actual fun create(
        repository: LocationRepository,
        configuration: TrackingConfiguration,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ): LocationTracker {
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
