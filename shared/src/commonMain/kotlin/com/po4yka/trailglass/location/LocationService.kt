package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.location.geocoding.CachedReverseGeocoder
import com.po4yka.trailglass.location.geocoding.ReverseGeocoder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central location service that coordinates location recording and processing. This service integrates reverse
 * geocoding into the location workflow.
 */
class LocationService(
    reverseGeocoder: ReverseGeocoder
) : LocationRecorder {
    private val cachedGeocoder = CachedReverseGeocoder(reverseGeocoder)
    private val placeVisitProcessor = PlaceVisitProcessor(cachedGeocoder)

    private val samples = mutableListOf<LocationSample>()
    private val placeVisits = mutableListOf<PlaceVisit>()
    private val mutex = Mutex()

    override suspend fun recordSample(sample: LocationSample) {
        mutex.withLock {
            samples.add(sample)
        }
    }

    override suspend fun processSamples() {
        val samplesToProcess =
            mutex.withLock {
                samples.toList()
            }

        if (samplesToProcess.isEmpty()) return

        // Detect place visits with reverse geocoding
        val detectedVisits = placeVisitProcessor.detectPlaceVisits(samplesToProcess)

        mutex.withLock {
            placeVisits.addAll(detectedVisits)
        }
    }

    /** Get all recorded location samples. */
    suspend fun getSamples(): List<LocationSample> =
        mutex.withLock {
            samples.toList()
        }

    /** Get all detected place visits. */
    suspend fun getPlaceVisits(): List<PlaceVisit> =
        mutex.withLock {
            placeVisits.toList()
        }

    /** Clear all recorded data. */
    suspend fun clear() {
        mutex.withLock {
            samples.clear()
            placeVisits.clear()
        }
        cachedGeocoder.clearCache()
    }

    /** Clear expired geocoding cache entries. */
    suspend fun clearExpiredCache() {
        cachedGeocoder.clearExpiredCache()
    }
}
