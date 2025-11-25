package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository

/** iOS implementation of LocationTrackerFactory. */
actual class LocationTrackerFactory(
    private val deviceId: String,
    private val userId: String
) {
    actual fun create(
        repository: LocationRepository,
        configuration: TrackingConfiguration,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ): LocationTracker {
        return IOSLocationTracker(
            repository = repository,
            configuration = configuration,
            deviceId = deviceId,
            userId = userId,
            coroutineScope = coroutineScope
        )
    }
}
