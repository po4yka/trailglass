package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.domain.service.LocationService
import com.po4yka.trailglass.feature.tracking.PlaceVisitDetector
import com.po4yka.trailglass.feature.tracking.PlaceVisitEvent
import com.po4yka.trailglass.feature.tracking.TransportModeDetector
import com.po4yka.trailglass.feature.tracking.TripDetector
import com.po4yka.trailglass.feature.tracking.TripEvent
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import kotlin.random.Random

/**
 * Default implementation of LocationTracker.
 *
 * Coordinates location updates with trip detection, place visit detection,
 * and transport mode detection.
 */
@Inject
class DefaultLocationTracker(
    private val locationService: LocationService,
    private val locationRepository: LocationRepository,
    private val coroutineScope: CoroutineScope,
    private val userId: String,
    private val deviceId: String
) : LocationTracker {
    private val logger = logger()

    // Detectors
    private val tripDetector = TripDetector()
    private val placeVisitDetector = PlaceVisitDetector()
    private val transportModeDetector = TransportModeDetector()

    // State
    private val _trackingState = MutableStateFlow(TrackingState())
    override val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _locationUpdates = MutableSharedFlow<LocationSample>(replay = 0)
    override val locationUpdates: Flow<LocationSample> = _locationUpdates.asSharedFlow()

    private var trackingJob: Job? = null
    private var currentTripId: String? = null
    private var locationsRecordedToday = 0

    override suspend fun startTracking(mode: TrackingMode) {
        logger.info { "Starting location tracking: $mode" }

        // Check permissions
        if (!locationService.hasLocationPermission()) {
            logger.warn { "Cannot start tracking: no permission" }
            return
        }

        // Stop existing tracking if any
        stopTracking()

        // Start location updates
        trackingJob =
            coroutineScope.launch {
                locationService.locationUpdates.collect { coordinate ->
                    processLocation(coordinate, mode)
                }
            }

        // Update state
        _trackingState.update {
            it.copy(
                mode = mode,
                isTracking = true
            )
        }

        logger.info { "Location tracking started" }
    }

    override suspend fun stopTracking() {
        logger.info { "Stopping location tracking" }

        trackingJob?.cancel()
        trackingJob = null

        // Reset detectors
        tripDetector.reset()
        placeVisitDetector.reset()
        transportModeDetector.reset()

        // Update state
        _trackingState.update {
            it.copy(
                mode = TrackingMode.IDLE,
                isTracking = false
            )
        }

        logger.info { "Location tracking stopped" }
    }

    override suspend fun getCurrentState(): TrackingState = _trackingState.value

    override suspend fun hasPermissions(): Boolean = locationService.hasLocationPermission()

    override suspend fun requestPermissions(): Boolean {
        // Request location permission from the platform-specific service
        // On iOS: This triggers the system permission dialog
        // On Android: This returns false - permissions must be requested from UI layer
        val granted = locationService.requestLocationPermission(background = true)

        if (!granted) {
            logger.info { "Permission request initiated or requires UI-based request" }
        }

        return granted
    }

    /**
     * Process a new location update.
     */
    private suspend fun processLocation(
        coordinate: Coordinate,
        mode: TrackingMode
    ) {
        val now = Clock.System.now()

        // Create Location object for detectors
        // Create Location object for detectors
        val location =
            Location(
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                time = now.toEpochMilliseconds(),
                accuracy = null,
                altitude = null,
                speed = null,
                bearing = null
            )

        // Detect trip state changes
        val tripEvent = tripDetector.processLocation(location)
        handleTripEvent(tripEvent, coordinate, now)

        // Detect place visits
        val placeVisitEvent = placeVisitDetector.processLocation(location)
        handlePlaceVisitEvent(placeVisitEvent)

        // Detect transport mode
        val transportMode = transportModeDetector.detectTransportMode(location)

        // Create and save location sample
        val sample =
            LocationSample(
                id = generateLocationId(),
                timestamp = now,
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                accuracy = 10.0, // Would come from actual location provider
                speed = null,
                bearing = null,
                source = if (mode == TrackingMode.ACTIVE) LocationSource.GPS else LocationSource.NETWORK,
                tripId = currentTripId,
                uploadedAt = null,
                deviceId = deviceId,
                userId = userId
            )

        // Save to repository
        locationRepository.insertSample(sample)
        locationsRecordedToday++

        // Emit to observers
        _locationUpdates.emit(sample)

        // Update tracking state
        _trackingState.update {
            it.copy(
                lastLocation = sample,
                samplesRecordedToday = locationsRecordedToday
            )
        }

        logger.debug { "Location processed: ${coordinate.latitude}, ${coordinate.longitude}" }
    }

    /**
     * Handle trip events (start/end).
     */
    private suspend fun handleTripEvent(
        event: TripEvent?,
        coordinate: Coordinate,
        now: Instant
    ) {
        when (event) {
            is TripEvent.TripStarted -> {
                // Create new trip
                currentTripId = generateTripId()
                logger.info { "Trip started: $currentTripId" }

                // Save trip to repository would happen here
                // For now, just track the ID
            }
            is TripEvent.TripEnded -> {
                // End current trip
                logger.info { "Trip ended: $currentTripId" }
                currentTripId = null

                // Update trip end time in repository would happen here
            }
            null -> {
                // No trip event
            }
        }
    }

    /**
     * Handle place visit events.
     */
    private suspend fun handlePlaceVisitEvent(event: PlaceVisitEvent?) {
        when (event) {
            is PlaceVisitEvent.VisitDetected -> {
                logger.info {
                    "Place visit detected: ${event.location}, duration: ${event.duration / 1000 / 60} minutes"
                }

                // Create PlaceVisit object and save to repository would happen here
                // For now, just log
            }
            null -> {
                // No place visit event
            }
        }
    }

    /**
     * Generate unique location ID.
     */
    private fun generateLocationId(): String = "loc_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000)}"

    /**
     * Generate unique trip ID.
     */
    private fun generateTripId(): String = "trip_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000)}"
}
