package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.util.UuidGenerator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Android equivalent of iOS CLVisit monitoring.
 *
 * Detects when the user arrives at and departs from significant locations
 * by analyzing location sample patterns. Creates VISIT-type location samples
 * to match iOS's native visit monitoring functionality.
 *
 * iOS Equivalent: CLLocationManager.startMonitoringVisits()
 *
 * Benefits:
 * - Provides semantic parity with iOS visit detection
 * - Creates LocationSource.VISIT samples for consistency
 * - Enables visit-based features to work the same on both platforms
 *
 * @see com.po4yka.trailglass.location.tracking.IOSLocationTracker.startMonitoringVisits
 * @see com.po4yka.trailglass.domain.model.LocationSource.VISIT
 */
class AndroidVisitDetector(
    private val locationRepository: LocationRepository,
    private val uuidGenerator: UuidGenerator,
    private val userId: String,
    private val deviceId: String,
    private val coroutineScope: CoroutineScope
) {
    private val logger = logger()

    private var monitoringJob: Job? = null
    private var currentVisit: OngoingVisit? = null

    // Configuration matching iOS visit detection behavior
    private val minStationaryDuration = 10.minutes
    private val maxStationaryDistance = 100.0 // meters
    private val checkInterval = 60.seconds

    /**
     * Start monitoring for visits.
     * Analyzes recent location samples to detect stationary periods.
     */
    fun startMonitoring() {
        if (monitoringJob?.isActive == true) {
            logger.debug { "Visit monitoring already active" }
            return
        }

        logger.info { "Starting Android visit detection (iOS CLVisit equivalent)" }

        monitoringJob =
            coroutineScope.launch {
                while (isActive) {
                    try {
                        checkForVisits()
                    } catch (e: Exception) {
                        logger.error(e) { "Error in visit detection" }
                    }
                    delay(checkInterval)
                }
            }
    }

    /**
     * Stop monitoring for visits.
     */
    fun stopMonitoring() {
        logger.info { "Stopping Android visit detection" }
        monitoringJob?.cancel()
        monitoringJob = null

        // End any ongoing visit
        currentVisit?.let { visit ->
            coroutineScope.launch {
                endVisit(visit, Clock.System.now())
            }
        }
        currentVisit = null
    }

    /**
     * Check recent location samples for visit patterns.
     */
    private suspend fun checkForVisits() {
        // Get recent location samples (last 30 minutes)
        val lookbackTime = Clock.System.now() - 30.minutes
        val recentSamplesResult =
            locationRepository.getSamples(
                userId = userId,
                startTime = lookbackTime,
                endTime = Clock.System.now()
            )

        val recentSamples = recentSamplesResult.getOrNull() ?: emptyList()

        if (recentSamples.isEmpty()) {
            logger.trace { "No recent samples for visit detection" }
            return
        }

        // Sort by timestamp
        val sorted = recentSamples.sortedBy { it.timestamp }

        // Check if user is stationary
        val isStationary = isUserStationary(sorted)

        when {
            isStationary && currentVisit == null -> {
                // Start of a new visit
                startVisit(sorted.first())
            }
            isStationary && currentVisit != null -> {
                // Visit is ongoing, update it
                updateVisit(sorted.last())
            }
            !isStationary && currentVisit != null -> {
                // Visit has ended
                endVisit(currentVisit!!, sorted.last().timestamp)
                currentVisit = null
            }
        }
    }

    /**
     * Determine if user is stationary based on recent samples.
     */
    private fun isUserStationary(samples: List<LocationSample>): Boolean {
        if (samples.size < 2) return false

        val firstSample = samples.first()
        val duration = samples.last().timestamp - firstSample.timestamp

        // Must have samples spanning at least the minimum duration
        if (duration < minStationaryDuration) return false

        // Check if all samples are within the distance threshold of the first sample
        val centerLat = samples.map { it.latitude }.average()
        val centerLon = samples.map { it.longitude }.average()

        return samples.all { sample ->
            val distance =
                calculateDistance(
                    centerLat,
                    centerLon,
                    sample.latitude,
                    sample.longitude
                )
            distance < maxStationaryDistance
        }
    }

    /**
     * Start tracking a new visit.
     */
    private fun startVisit(arrivalSample: LocationSample) {
        val visit =
            OngoingVisit(
                arrivalTime = arrivalSample.timestamp,
                latitude = arrivalSample.latitude,
                longitude = arrivalSample.longitude,
                tripId = arrivalSample.tripId
            )

        currentVisit = visit

        logger.info {
            "Visit arrival detected at (${visit.latitude}, ${visit.longitude}) " +
                "at ${visit.arrivalTime}"
        }
    }

    /**
     * Update an ongoing visit with new location data.
     */
    private fun updateVisit(latestSample: LocationSample) {
        val visit = currentVisit ?: return

        // Update visit location to be more accurate (using latest sample)
        currentVisit =
            visit.copy(
                latitude = latestSample.latitude,
                longitude = latestSample.longitude
            )

        logger.trace { "Visit ongoing at (${latestSample.latitude}, ${latestSample.longitude})" }
    }

    /**
     * End a visit and create a VISIT-type location sample.
     * This creates parity with iOS CLVisit events.
     */
    private suspend fun endVisit(
        visit: OngoingVisit,
        departureTime: Instant
    ) {
        val duration = departureTime - visit.arrivalTime

        logger.info {
            "Visit departure detected after $duration at " +
                "(${visit.latitude}, ${visit.longitude})"
        }

        // Create a VISIT-type location sample (Android equivalent of iOS CLVisit)
        val visitSample =
            LocationSample(
                id = uuidGenerator.randomUUID(),
                timestamp = visit.arrivalTime, // Use arrival time for visit timestamp
                latitude = visit.latitude,
                longitude = visit.longitude,
                accuracy = maxStationaryDistance, // Use threshold as accuracy estimate
                speed = 0.0, // Stationary
                bearing = null,
                source = LocationSource.VISIT, // ← Android synthetic visit, matching iOS
                tripId = visit.tripId,
                uploadedAt = null,
                deviceId = deviceId,
                userId = userId
            )

        // Save the VISIT sample to database
        try {
            locationRepository.insertSample(visitSample)
            logger.info {
                "Created VISIT-type location sample (Android equivalent of iOS CLVisit) " +
                    "for visit lasting $duration"
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to save VISIT location sample" }
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     */
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }

    /**
     * Represents an ongoing visit being tracked.
     */
    private data class OngoingVisit(
        val arrivalTime: Instant,
        val latitude: Double,
        val longitude: Double,
        val tripId: String?
    )
}
