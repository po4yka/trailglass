package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import platform.CoreLocation.*
import platform.darwin.NSObject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * iOS implementation of LocationTracker using CLLocationManager.
 *
 * IMPORTANT: Call [cleanup] when this tracker is no longer needed to prevent memory leaks.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalUuidApi::class)
class IOSLocationTracker(
    private val repository: LocationRepository,
    private val configuration: TrackingConfiguration,
    private val deviceId: String,
    private val userId: String,
    private val coroutineScope: kotlinx.coroutines.CoroutineScope
) : LocationTracker, Lifecycle {
    private val logger = logger()
    private val locationManager = CLLocationManager()
    private val delegate = LocationManagerDelegate()

    private val _trackingState = MutableStateFlow(TrackingState())
    override val trackingState: Flow<TrackingState> = _trackingState.asStateFlow()

    private val _locationUpdates = MutableSharedFlow<LocationSample>(replay = 0)
    override val locationUpdates: Flow<LocationSample> = _locationUpdates.asSharedFlow()

    init {
        locationManager.delegate = delegate
        delegate.locationTracker = this
    }

    override suspend fun startTracking(mode: TrackingMode) {
        logger.info { "Starting location tracking in $mode mode" }

        if (!hasPermissions()) {
            logger.warn { "Cannot start tracking - missing location permissions" }
            return
        }

        configureLocationManager(mode)

        when (mode) {
            TrackingMode.SIGNIFICANT -> {
                // Use significant location changes only - extremely battery efficient
                // iOS monitors WiFi/cell tower changes automatically
                locationManager.startMonitoringSignificantLocationChanges()
                logger.debug { "Started significant location changes monitoring (WiFi/Cell changes only)" }
            }

            TrackingMode.PASSIVE -> {
                // Use significant location changes for battery efficiency
                locationManager.startMonitoringSignificantLocationChanges()
                // Also monitor visits
                locationManager.startMonitoringVisits()
                logger.debug { "Started significant location changes and visit monitoring" }
            }

            TrackingMode.ACTIVE -> {
                // Use continuous location updates
                locationManager.startUpdatingLocation()
                logger.debug { "Started continuous location updates" }
            }

            TrackingMode.IDLE -> {
                // Do nothing
            }
        }

        _trackingState.update { state ->
            state.copy(
                mode = mode,
                isTracking = true
            )
        }

        logger.info { "Location tracking started successfully" }
    }

    override suspend fun stopTracking() {
        logger.info { "Stopping location tracking" }

        locationManager.stopUpdatingLocation()
        locationManager.stopMonitoringSignificantLocationChanges()
        locationManager.stopMonitoringVisits()

        _trackingState.update { state ->
            state.copy(
                mode = TrackingMode.IDLE,
                isTracking = false
            )
        }

        logger.info { "Location tracking stopped" }
    }

    /**
     * Cleanup and release all resources held by this tracker.
     *
     * This breaks the retain cycle between the delegate and the tracker by clearing
     * the delegate reference from the locationManager and the tracker reference from the delegate.
     *
     * IMPORTANT: Call this method when the tracker is no longer needed to prevent memory leaks.
     * After cleanup, this tracker should not be used again.
     */
    override fun cleanup() {
        logger.info { "Cleaning up IOSLocationTracker" }

        // Break the retain cycle by clearing references
        locationManager.delegate = null
        delegate.locationTracker = null

        logger.debug { "IOSLocationTracker cleanup complete" }
    }

    override suspend fun hasPermissions(): Boolean {
        val authStatus = locationManager.authorizationStatus

        val hasBasicPermission =
            authStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                authStatus == kCLAuthorizationStatusAuthorizedAlways

        val hasBackgroundPermission =
            if (configuration.requireBackgroundLocation) {
                authStatus == kCLAuthorizationStatusAuthorizedAlways
            } else {
                true
            }

        val hasAll = hasBasicPermission && hasBackgroundPermission
        logger.debug { "Permission check: authStatus=$authStatus, hasAll=$hasAll" }

        return hasAll
    }

    override suspend fun requestPermissions(): Boolean {
        logger.info { "Requesting location permissions" }

        // Request "When In Use" permission first
        locationManager.requestWhenInUseAuthorization()

        // If background location is required, request "Always" permission
        if (configuration.requireBackgroundLocation) {
            locationManager.requestAlwaysAuthorization()
        }

        // Note: The actual result comes asynchronously via the delegate
        // This method returns false as we can't wait for the async result here
        return false
    }

    override suspend fun getCurrentState(): TrackingState = _trackingState.value

    /** Configure CLLocationManager based on tracking mode. */
    private fun configureLocationManager(mode: TrackingMode) {
        when (mode) {
            TrackingMode.SIGNIFICANT -> {
                // For significant location changes, minimal configuration needed
                // iOS handles this automatically, monitoring WiFi/cell tower changes
                locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
                locationManager.distanceFilter = configuration.significantDistance
                locationManager.allowsBackgroundLocationUpdates = true
                locationManager.pausesLocationUpdatesAutomatically = true
                locationManager.activityType = CLActivityTypeOtherNavigation
            }

            TrackingMode.PASSIVE -> {
                locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
                locationManager.distanceFilter = configuration.passiveDistance
                locationManager.allowsBackgroundLocationUpdates = true
                locationManager.pausesLocationUpdatesAutomatically = true
                locationManager.activityType = CLActivityTypeOtherNavigation
            }

            TrackingMode.ACTIVE -> {
                locationManager.desiredAccuracy = kCLLocationAccuracyBest
                locationManager.distanceFilter = configuration.activeDistance
                locationManager.allowsBackgroundLocationUpdates = true
                locationManager.pausesLocationUpdatesAutomatically = false
                locationManager.activityType = CLActivityTypeFitness
            }

            TrackingMode.IDLE -> {
                // No configuration needed
            }
        }
    }

    /** Process a location update from CLLocationManager. */
    internal fun processLocation(location: CLLocation) {
        val lat = location.coordinate.useContents { latitude }
        val lng = location.coordinate.useContents { longitude }
        logger.debug {
            "Received location: ($lat, $lng), " +
                "accuracy: ${location.horizontalAccuracy}m"
        }

        val sample =
            LocationSample(
                id = "sample_${Uuid.random()}",
                timestamp = Clock.System.now(),
                latitude = location.coordinate.useContents { latitude },
                longitude = location.coordinate.useContents { longitude },
                accuracy = location.horizontalAccuracy,
                speed = if (location.speed >= 0) location.speed else null,
                bearing = if (location.course >= 0) location.course else null,
                source = LocationSource.GPS, // iOS doesn't distinguish between GPS and network
                deviceId = deviceId,
                userId = userId
            )

        // Emit to flow
        _locationUpdates.tryEmit(sample)

        // Update state
        _trackingState.update { state ->
            state.copy(
                lastLocation = sample,
                samplesRecordedToday = state.samplesRecordedToday + 1
            )
        }

        // Save to repository using injected coroutine scope
        coroutineScope.launch {
            repository
                .insertSample(sample)
                .onSuccess {
                    logger.trace { "Location sample saved: ${sample.id}" }
                }.onError { error ->
                    logger.error { "Failed to save location sample ${sample.id}: ${error.userMessage}" }
                }
        }
    }

    /** Process a visit from CLLocationManager. */
    internal fun processVisit(visit: CLVisit) {
        val lat = visit.coordinate.useContents { latitude }
        val lng = visit.coordinate.useContents { longitude }
        logger.debug {
            "Received visit: ($lat, $lng), " +
                "arrival: ${visit.arrivalDate}, departure: ${visit.departureDate}"
        }

        // Create a location sample from the visit
        val sample =
            LocationSample(
                id = "sample_visit_${Uuid.random()}",
                timestamp = Clock.System.now(),
                latitude = lat,
                longitude = lng,
                accuracy = visit.horizontalAccuracy,
                speed = null,
                bearing = null,
                source = LocationSource.VISIT,
                deviceId = deviceId,
                userId = userId
            )

        // Emit to flow
        _locationUpdates.tryEmit(sample)

        // Update state
        _trackingState.update { state ->
            state.copy(
                lastLocation = sample,
                samplesRecordedToday = state.samplesRecordedToday + 1
            )
        }

        // Save to repository using injected coroutine scope
        coroutineScope.launch {
            repository
                .insertSample(sample)
                .onSuccess {
                    logger.trace { "Visit sample saved: ${sample.id}" }
                }.onError { error ->
                    logger.error { "Failed to save visit sample ${sample.id}: ${error.userMessage}" }
                }
        }
    }

    /** CLLocationManagerDelegate implementation. */
    private class LocationManagerDelegate :
        NSObject(),
        CLLocationManagerDelegateProtocol {
        var locationTracker: IOSLocationTracker? = null

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>
        ) {
            @Suppress("UNCHECKED_CAST")
            val locations = didUpdateLocations as List<CLLocation>
            locations.forEach { location ->
                locationTracker?.processLocation(location)
            }
        }

        override fun locationManager(
            manager: CLLocationManager,
            didVisit: CLVisit
        ) {
            locationTracker?.processVisit(didVisit)
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: platform.Foundation.NSError
        ) {
            locationTracker?.logger?.error { "Location manager failed: ${didFailWithError.localizedDescription}" }
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            locationTracker?.logger?.info { "Authorization changed: $status" }
        }
    }
}
