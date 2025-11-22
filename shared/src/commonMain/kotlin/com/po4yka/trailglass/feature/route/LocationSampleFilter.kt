package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.logging.logger
import kotlin.math.*

/**
 * Filters and validates location samples for route construction.
 * Removes noisy, inaccurate, or duplicate samples.
 */
class LocationSampleFilter(
    private val maxAccuracyMeters: Double = 100.0,
    private val minTimeBetweenSamplesSeconds: Long = 5,
    private val maxSpeedMps: Double = 150.0 // ~540 km/h, filter out obvious GPS errors
) {
    private val logger = logger()

    /**
     * Filter and validate location samples for route construction.
     *
     * @param samples Raw location samples
     * @return Filtered samples suitable for route building
     */
    fun filterAndValidate(samples: List<LocationSample>): List<LocationSample> {
        logger.info { "Filtering ${samples.size} location samples" }

        if (samples.isEmpty()) {
            return emptyList()
        }

        val sorted = samples.sortedBy { it.timestamp }

        var filtered = sorted
        filtered = filterByAccuracy(filtered)
        filtered = filterDuplicates(filtered)
        filtered = filterBySpeed(filtered)
        filtered = filterStaticPoints(filtered)

        val removed = samples.size - filtered.size
        val percentage = (removed.toDouble() / samples.size * 100).toInt()
        logger.info { "Filtered out $removed samples ($percentage%) -> ${filtered.size} remaining" }

        return filtered
    }

    /**
     * Remove samples with poor GPS accuracy.
     */
    private fun filterByAccuracy(samples: List<LocationSample>): List<LocationSample> {
        val filtered =
            samples.filter { sample ->
                sample.accuracy <= maxAccuracyMeters
            }

        val removed = samples.size - filtered.size
        if (removed > 0) {
            logger.debug { "Removed $removed samples with accuracy > ${maxAccuracyMeters}m" }
        }

        return filtered
    }

    /**
     * Remove duplicate or very close consecutive samples.
     * Keeps only samples that are sufficiently spaced in time.
     */
    private fun filterDuplicates(samples: List<LocationSample>): List<LocationSample> {
        if (samples.isEmpty()) return emptyList()

        val filtered = mutableListOf<LocationSample>()
        filtered.add(samples.first())

        for (i in 1 until samples.size) {
            val previous = filtered.last()
            val current = samples[i]

            val timeDiff = (current.timestamp - previous.timestamp).inWholeSeconds

            // Keep sample if enough time has passed OR if location changed significantly
            val distance =
                haversineDistance(
                    previous.latitude,
                    previous.longitude,
                    current.latitude,
                    current.longitude
                )

            if (timeDiff >= minTimeBetweenSamplesSeconds || distance > 10.0) {
                filtered.add(current)
            }
        }

        val removed = samples.size - filtered.size
        if (removed > 0) {
            logger.debug { "Removed $removed duplicate/consecutive samples" }
        }

        return filtered
    }

    /**
     * Remove samples with unrealistic speed (likely GPS errors).
     * Calculates speed between consecutive points and removes outliers.
     */
    private fun filterBySpeed(samples: List<LocationSample>): List<LocationSample> {
        if (samples.size < 2) return samples

        val filtered = mutableListOf<LocationSample>()
        filtered.add(samples.first()) // Always keep first point

        for (i in 1 until samples.size) {
            val previous = filtered.last()
            val current = samples[i]

            val distance =
                haversineDistance(
                    previous.latitude,
                    previous.longitude,
                    current.latitude,
                    current.longitude
                )

            val timeDiff = (current.timestamp - previous.timestamp).inWholeSeconds
            if (timeDiff <= 0) {
                // Skip if time didn't advance
                continue
            }

            val speed = distance / timeDiff

            if (speed <= maxSpeedMps) {
                filtered.add(current)
            } else {
                logger.trace {
                    "Filtered sample with unrealistic speed: ${speed.toInt()} m/s " +
                        "(${(speed * 3.6).toInt()} km/h)"
                }
            }
        }

        val removed = samples.size - filtered.size
        if (removed > 0) {
            logger.debug { "Removed $removed samples with unrealistic speed > ${maxSpeedMps}m/s" }
        }

        return filtered
    }

    /**
     * Remove clusters of static points (user standing still).
     * These should already be captured in PlaceVisits, so we can thin them out.
     */
    private fun filterStaticPoints(samples: List<LocationSample>): List<LocationSample> {
        if (samples.size < 3) return samples

        val filtered = mutableListOf<LocationSample>()
        var i = 0

        while (i < samples.size) {
            val current = samples[i]
            filtered.add(current)

            // Look ahead to see if next points are static
            var j = i + 1
            var staticCount = 0

            while (j < samples.size && j < i + 10) {
                val distance =
                    haversineDistance(
                        current.latitude,
                        current.longitude,
                        samples[j].latitude,
                        samples[j].longitude
                    )

                if (distance < 20.0) { // Within 20m radius
                    staticCount++
                    j++
                } else {
                    break
                }
            }

            // If we found a cluster of static points, skip most of them
            if (staticCount > 2) {
                i = j // Skip to the point where movement resumed
                logger.trace { "Skipped cluster of $staticCount static points" }
            } else {
                i++
            }
        }

        val removed = samples.size - filtered.size
        if (removed > 0) {
            logger.debug { "Removed $removed static points" }
        }

        return filtered
    }

    /**
     * Prefer GPS samples over network samples when both are available.
     * This is a secondary filter that can be applied if desired.
     */
    fun preferGpsSamples(samples: List<LocationSample>): List<LocationSample> {
        // Group samples by approximate time and location
        val grouped =
            samples.groupBy { sample ->
                val roundedTime = (sample.timestamp.epochSeconds / 10) * 10 // Group by 10-sec window
                val roundedLat = (sample.latitude * 100).toInt() // ~1.1km grouping
                val roundedLon = (sample.longitude * 100).toInt()
                Triple(roundedTime, roundedLat, roundedLon)
            }

        // From each group, prefer GPS over NETWORK
        val preferred =
            grouped.values.map { group ->
                group.firstOrNull { it.source == LocationSource.GPS }
                    ?: group.first()
            }

        val removed = samples.size - preferred.size
        if (removed > 0) {
            logger.debug { "Preferred GPS samples, removed $removed network duplicates" }
        }

        return preferred
    }

    /**
     * Calculate Haversine distance between two coordinates.
     */
    private fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a =
            sin(dLat / 2).pow(2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
