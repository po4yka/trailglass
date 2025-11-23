package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.LocationSample

/**
 * Interface for recording raw location samples. Platform-specific implementations forward location updates to this
 * interface.
 */
interface LocationRecorder {
    /**
     * Record a raw location sample from the device.
     *
     * @param sample The location sample to record
     */
    suspend fun recordSample(sample: LocationSample)

    /** Process accumulated samples to build higher-level entities (PlaceVisits, RouteSegments, etc.) */
    suspend fun processSamples()
}
