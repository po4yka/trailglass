package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.location.geocoding.ReverseGeocoder
import kotlinx.datetime.Instant
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Processes location samples to detect and create PlaceVisits with geocoded information.
 * Uses DBSCAN-like clustering to group nearby points in time and space.
 */
class PlaceVisitProcessor(
    private val reverseGeocoder: ReverseGeocoder,
    private val minDurationThreshold: Duration = 10.minutes,
    private val spatialThresholdMeters: Double = 100.0
) {

    /**
     * Process a list of location samples to detect place visits.
     *
     * @param samples List of location samples to process
     * @return List of detected PlaceVisits with geocoded information
     */
    suspend fun detectPlaceVisits(samples: List<LocationSample>): List<PlaceVisit> {
        if (samples.isEmpty()) return emptyList()

        // Sort samples by timestamp
        val sortedSamples = samples.sortedBy { it.timestamp }

        // Group samples into clusters
        val clusters = clusterSamples(sortedSamples)

        // Convert clusters to PlaceVisits with geocoding
        return clusters.mapNotNull { cluster ->
            createPlaceVisit(cluster)
        }
    }

    /**
     * Cluster samples using spatial and temporal proximity.
     */
    private fun clusterSamples(samples: List<LocationSample>): List<List<LocationSample>> {
        val clusters = mutableListOf<MutableList<LocationSample>>()
        var currentCluster = mutableListOf<LocationSample>()

        for (i in samples.indices) {
            val current = samples[i]

            if (currentCluster.isEmpty()) {
                currentCluster.add(current)
                continue
            }

            val last = currentCluster.last()
            val distance = calculateDistance(
                last.latitude, last.longitude,
                current.latitude, current.longitude
            )

            // If sample is within spatial threshold, add to current cluster
            if (distance < spatialThresholdMeters) {
                currentCluster.add(current)
            } else {
                // Start a new cluster
                if (currentCluster.isNotEmpty()) {
                    clusters.add(currentCluster)
                }
                currentCluster = mutableListOf(current)
            }
        }

        // Add the last cluster
        if (currentCluster.isNotEmpty()) {
            clusters.add(currentCluster)
        }

        return clusters
    }

    /**
     * Create a PlaceVisit from a cluster of samples, with reverse geocoding.
     */
    private suspend fun createPlaceVisit(samples: List<LocationSample>): PlaceVisit? {
        if (samples.isEmpty()) return null

        val startTime = samples.first().timestamp
        val endTime = samples.last().timestamp
        val duration = endTime - startTime

        // Filter out short visits
        if (duration < minDurationThreshold) {
            return null
        }

        // Calculate center point (simple mean)
        val centerLat = samples.map { it.latitude }.average()
        val centerLon = samples.map { it.longitude }.average()

        // Perform reverse geocoding
        val geocoded = reverseGeocoder.reverseGeocode(centerLat, centerLon)

        return PlaceVisit(
            id = generatePlaceVisitId(startTime, centerLat, centerLon),
            startTime = startTime,
            endTime = endTime,
            centerLatitude = centerLat,
            centerLongitude = centerLon,
            approximateAddress = geocoded?.formattedAddress,
            poiName = geocoded?.poiName,
            city = geocoded?.city,
            countryCode = geocoded?.countryCode,
            locationSampleIds = samples.map { it.id }
        )
    }

    /**
     * Generate a deterministic ID for a PlaceVisit.
     */
    private fun generatePlaceVisitId(time: Instant, lat: Double, lon: Double): String {
        return "visit_${time.toEpochMilliseconds()}_${lat.hashCode()}_${lon.hashCode()}"
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     *
     * @return Distance in meters
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
