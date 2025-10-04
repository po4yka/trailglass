package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import platform.CoreLocation.*
import platform.Foundation.NSNumber
import platform.darwin.NSObject
import java.util.UUID

/**
 * iOS implementation of LocationTracker using CLLocationManager.
 */
class IOSLocationTracker(
    private val repository: LocationRepository,
    private val configuration: TrackingConfiguration,
    private val deviceId: String,
    private val userId: String
) : LocationTracker {

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

    override suspend fun hasPermissions(): Boolean {
        val authStatus = locationManager.authorizationStatus

        val hasBasicPermission = authStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                                 authStatus == kCLAuthorizationStatusAuthorizedAlways

        val hasBackgroundPermission = if (configuration.requireBackgroundLocation) {
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

    override suspend fun getCurrentState(): TrackingState {
        return _trackingState.value
    }

    /**
     * Configure CLLocationManager based on tracking mode.
     */
    private fun configureLocationManager(mode: TrackingMode) {
        when (mode) {
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

    /**
     * Process a location update from CLLocationManager.
     */
    internal fun processLocation(location: CLLocation) {
        logger.debug {
            "Received location: (${location.coordinate.latitude}, ${location.coordinate.longitude}), " +
            "accuracy: ${location.horizontalAccuracy}m"
        }

        val sample = LocationSample(
            id = "sample_${UUID.randomUUID()}",
            timestamp = Clock.System.now(),
            latitude = location.coordinate.latitude,
            longitude = location.coordinate.longitude,
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

        // Save to repository (fire and forget)
        kotlinx.coroutines.GlobalScope.launch {
            try {
                repository.insertSample(sample)
                logger.trace { "Location sample saved: ${sample.id}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to save location sample ${sample.id}" }
            }
        }
    }

    /**
     * Process a visit from CLLocationManager.
     */
    internal fun processVisit(visit: CLVisit) {
        logger.debug {
            "Received visit: (${visit.coordinate.latitude}, ${visit.coordinate.longitude}), " +
            "arrival: ${visit.arrivalDate}, departure: ${visit.departureDate}"
        }

        // Create a location sample from the visit
        val sample = LocationSample(
            id = "sample_visit_${UUID.randomUUID()}",
            timestamp = Clock.System.now(),
            latitude = visit.coordinate.latitude,
            longitude = visit.coordinate.longitude,
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

        // Save to repository
        kotlinx.coroutines.GlobalScope.launch {
            try {
                repository.insertSample(sample)
                logger.trace { "Visit sample saved: ${sample.id}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to save visit sample ${sample.id}" }
            }
        }
    }

    /**
     * CLLocationManagerDelegate implementation.
     */
    private class LocationManagerDelegate : NSObject(), CLLocationManagerDelegateProtocol {
        var locationTracker: IOSLocationTracker? = null

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            @Suppress("UNCHECKED_CAST")
            val locations = didUpdateLocations as List<CLLocation>
            locations.forEach { location ->
                locationTracker?.processLocation(location)
            }
        }

        override fun locationManager(manager: CLLocationManager, didVisit: CLVisit) {
            locationTracker?.processVisit(didVisit)
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
            locationTracker?.logger?.error { "Location manager failed: ${didFailWithError.localizedDescription}" }
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            locationTracker?.logger?.info { "Authorization changed: $status" }
        }
    }
}

// Suppress the deprecated GlobalScope warning
@Suppress("DEPRECATION")
private fun <T> kotlinx.coroutines.GlobalScope.launch(block: suspend kotlinx.coroutines.CoroutineScope.() -> T) =
    kotlinx.coroutines.GlobalScope.launch { block() }
