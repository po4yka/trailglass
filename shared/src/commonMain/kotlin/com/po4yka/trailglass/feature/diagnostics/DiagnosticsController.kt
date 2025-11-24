package com.po4yka.trailglass.feature.diagnostics

import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.sync.SyncCoordinator
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

data class LocationStatus(
    val trackingMode: TrackingMode = TrackingMode.IDLE,
    val lastLocationUpdate: Instant? = null,
    val locationAccuracy: Double? = null,
    val gpsSatellites: Int? = null,
    val locationPermissionGranted: Boolean = false,
    val backgroundLocationPermissionGranted: Boolean = false
)

data class DatabaseStatus(
    val locationSamplesCount: Long = 0,
    val placeVisitsCount: Long = 0,
    val routeSegmentsCount: Long = 0,
    val tripsCount: Long = 0,
    val photosCount: Long = 0,
    val regionsCount: Long = 0,
    val databaseSizeMB: Double = 0.0
)

data class SyncStatus(
    val lastSyncTimestamp: Instant? = null,
    val syncEnabled: Boolean = false,
    val pendingSyncItems: Int = 0,
    val syncErrorsCount: Int = 0
)

data class SystemStatus(
    val appVersion: String = "",
    val buildNumber: String = "",
    val osVersion: String = "",
    val deviceModel: String = "",
    val networkConnectivity: NetworkConnectivity = NetworkConnectivity.UNKNOWN,
    val batteryLevel: Float? = null,
    val batteryOptimizationDisabled: Boolean? = null,
    val lowPowerMode: Boolean? = null
)

enum class NetworkConnectivity {
    WIFI,
    MOBILE,
    OFFLINE,
    UNKNOWN
}

data class PermissionsStatus(
    val locationPermissionGranted: Boolean = false,
    val backgroundLocationPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val photoLibraryPermissionGranted: Boolean = false
)

data class DiagnosticsState(
    val locationStatus: LocationStatus = LocationStatus(),
    val databaseStatus: DatabaseStatus = DatabaseStatus(),
    val syncStatus: SyncStatus = SyncStatus(),
    val systemStatus: SystemStatus = SystemStatus(),
    val permissionsStatus: PermissionsStatus = PermissionsStatus(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Inject
class DiagnosticsController(
    private val locationTracker: LocationTracker,
    private val locationRepository: LocationRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val routeSegmentRepository: RouteSegmentRepository,
    private val tripRepository: TripRepository,
    private val photoRepository: PhotoRepository,
    private val syncCoordinator: SyncCoordinator,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    private val platformDiagnostics: PlatformDiagnostics,
    coroutineScope: CoroutineScope
) {
    private val logger = logger()

    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()

    init {
        // Observe location tracker state
        controllerScope.launch {
            locationTracker.trackingState.collect { trackingState ->
                _state.update { currentState ->
                    currentState.copy(
                        locationStatus = currentState.locationStatus.copy(
                            trackingMode = trackingState.mode,
                            lastLocationUpdate = trackingState.lastLocation?.timestamp
                        )
                    )
                }
            }
        }

        // Observe sync state
        controllerScope.launch {
            syncCoordinator.syncState.collect { syncState ->
                _state.update { currentState ->
                    currentState.copy(
                        syncStatus = SyncStatus(
                            lastSyncTimestamp = syncState.lastSyncTimestamp,
                            syncEnabled = !syncState.isSyncing,
                            pendingSyncItems = syncState.pendingChanges,
                            syncErrorsCount = if (syncState.error != null) 1 else 0
                        )
                    )
                }
            }
        }

        // Observe network connectivity
        controllerScope.launch {
            networkConnectivityMonitor.networkState.collect { networkState ->
                _state.update { currentState ->
                    currentState.copy(
                        systemStatus = currentState.systemStatus.copy(
                            networkConnectivity = when (networkState) {
                                is com.po4yka.trailglass.data.network.NetworkState.Connected -> NetworkConnectivity.WIFI
                                is com.po4yka.trailglass.data.network.NetworkState.Disconnected -> NetworkConnectivity.OFFLINE
                                is com.po4yka.trailglass.data.network.NetworkState.Limited -> NetworkConnectivity.MOBILE
                            }
                        )
                    )
                }
            }
        }

        // Initial refresh
        refreshAll()
    }

    fun refreshAll() {
        controllerScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Refresh database status
                refreshDatabaseStatus()

                // Refresh system status
                refreshSystemStatus()

                // Refresh permissions status
                refreshPermissionsStatus()

                // Refresh location status
                refreshLocationStatus()

                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                logger.error(e) { "Error refreshing diagnostics" }
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private suspend fun refreshDatabaseStatus() {
        try {
            val locationCount = when (val result = locationRepository.getUnprocessedSamples("", Int.MAX_VALUE)) {
                is Result.Success -> result.data.size.toLong()
                is Result.Error -> 0L
            }

            val placeVisitsCount = try {
                placeVisitRepository.getVisitsByUser("", Int.MAX_VALUE, 0).size.toLong()
            } catch (e: Exception) {
                0L
            }

            val routeSegmentsCount = try {
                routeSegmentRepository.getRouteSegmentCount()
            } catch (e: Exception) {
                0L
            }

            val tripsCount = try {
                tripRepository.getTripCount("")
            } catch (e: Exception) {
                0L
            }

            val photosCount = photoRepository.getPhotoCount("")

            val databaseSizeMB = platformDiagnostics.getDatabaseSizeMB()

            _state.update { currentState ->
                currentState.copy(
                    databaseStatus = DatabaseStatus(
                        locationSamplesCount = locationCount,
                        placeVisitsCount = placeVisitsCount,
                        routeSegmentsCount = routeSegmentsCount,
                        tripsCount = tripsCount,
                        photosCount = photosCount,
                        regionsCount = 0L,
                        databaseSizeMB = databaseSizeMB
                    )
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error refreshing database status" }
        }
    }

    private suspend fun refreshSystemStatus() {
        try {
            val systemInfo = platformDiagnostics.getSystemInfo()
            val batteryInfo = platformDiagnostics.getBatteryInfo()

            _state.update { currentState ->
                currentState.copy(
                    systemStatus = currentState.systemStatus.copy(
                        appVersion = systemInfo.appVersion,
                        buildNumber = systemInfo.buildNumber,
                        osVersion = systemInfo.osVersion,
                        deviceModel = systemInfo.deviceModel,
                        batteryLevel = batteryInfo.batteryLevel,
                        batteryOptimizationDisabled = batteryInfo.batteryOptimizationDisabled,
                        lowPowerMode = batteryInfo.lowPowerMode
                    )
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error refreshing system status" }
        }
    }

    private suspend fun refreshPermissionsStatus() {
        try {
            val permissions = platformDiagnostics.getPermissionsStatus()

            _state.update { currentState ->
                currentState.copy(
                    permissionsStatus = permissions
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error refreshing permissions status" }
        }
    }

    private suspend fun refreshLocationStatus() {
        try {
            val locationInfo = platformDiagnostics.getLocationInfo()

            _state.update { currentState ->
                currentState.copy(
                    locationStatus = currentState.locationStatus.copy(
                        locationAccuracy = locationInfo.accuracy,
                        gpsSatellites = locationInfo.satellites,
                        locationPermissionGranted = locationInfo.locationPermissionGranted,
                        backgroundLocationPermissionGranted = locationInfo.backgroundLocationPermissionGranted
                    )
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error refreshing location status" }
        }
    }

    fun exportDiagnostics(): String {
        val currentState = _state.value
        return buildString {
            appendLine("TrailGlass Diagnostics Report")
            appendLine("Generated at: ${Clock.System.now()}")
            appendLine()

            appendLine("=== LOCATION STATUS ===")
            appendLine("Tracking Mode: ${currentState.locationStatus.trackingMode}")
            appendLine("Last Update: ${currentState.locationStatus.lastLocationUpdate ?: "Never"}")
            appendLine("Accuracy: ${currentState.locationStatus.locationAccuracy?.let { "%.2f m".format(it) } ?: "N/A"}")
            appendLine("GPS Satellites: ${currentState.locationStatus.gpsSatellites ?: "N/A"}")
            appendLine("Location Permission: ${if (currentState.locationStatus.locationPermissionGranted) "Granted" else "Denied"}")
            appendLine("Background Permission: ${if (currentState.locationStatus.backgroundLocationPermissionGranted) "Granted" else "Denied"}")
            appendLine()

            appendLine("=== DATABASE STATUS ===")
            appendLine("Location Samples: ${currentState.databaseStatus.locationSamplesCount}")
            appendLine("Place Visits: ${currentState.databaseStatus.placeVisitsCount}")
            appendLine("Route Segments: ${currentState.databaseStatus.routeSegmentsCount}")
            appendLine("Trips: ${currentState.databaseStatus.tripsCount}")
            appendLine("Photos: ${currentState.databaseStatus.photosCount}")
            appendLine("Regions: ${currentState.databaseStatus.regionsCount}")
            appendLine("Database Size: ${"%.2f".format(currentState.databaseStatus.databaseSizeMB)} MB")
            appendLine()

            appendLine("=== SYNC STATUS ===")
            appendLine("Last Sync: ${currentState.syncStatus.lastSyncTimestamp ?: "Never"}")
            appendLine("Sync Enabled: ${currentState.syncStatus.syncEnabled}")
            appendLine("Pending Items: ${currentState.syncStatus.pendingSyncItems}")
            appendLine("Errors: ${currentState.syncStatus.syncErrorsCount}")
            appendLine()

            appendLine("=== SYSTEM STATUS ===")
            appendLine("App Version: ${currentState.systemStatus.appVersion}")
            appendLine("Build Number: ${currentState.systemStatus.buildNumber}")
            appendLine("OS Version: ${currentState.systemStatus.osVersion}")
            appendLine("Device Model: ${currentState.systemStatus.deviceModel}")
            appendLine("Network: ${currentState.systemStatus.networkConnectivity}")
            appendLine("Battery Level: ${currentState.systemStatus.batteryLevel?.let { "${(it * 100).toInt()}%" } ?: "N/A"}")
            appendLine("Battery Optimization: ${currentState.systemStatus.batteryOptimizationDisabled?.let { if (it) "Disabled" else "Enabled" } ?: "N/A"}")
            appendLine("Low Power Mode: ${currentState.systemStatus.lowPowerMode?.let { if (it) "Yes" else "No" } ?: "N/A"}")
            appendLine()

            appendLine("=== PERMISSIONS ===")
            appendLine("Location: ${if (currentState.permissionsStatus.locationPermissionGranted) "Granted" else "Denied"}")
            appendLine("Background Location: ${if (currentState.permissionsStatus.backgroundLocationPermissionGranted) "Granted" else "Denied"}")
            appendLine("Notifications: ${if (currentState.permissionsStatus.notificationsPermissionGranted) "Granted" else "Denied"}")
            appendLine("Photo Library: ${if (currentState.permissionsStatus.photoLibraryPermissionGranted) "Granted" else "Denied"}")
        }
    }

    fun cleanup() {
        logger.info { "Cleaning up DiagnosticsController" }
        controllerScope.cancel()
    }
}

data class SystemInfo(
    val appVersion: String,
    val buildNumber: String,
    val osVersion: String,
    val deviceModel: String
)

data class BatteryInfo(
    val batteryLevel: Float?,
    val batteryOptimizationDisabled: Boolean?,
    val lowPowerMode: Boolean?
)

data class LocationInfo(
    val accuracy: Double?,
    val satellites: Int?,
    val locationPermissionGranted: Boolean,
    val backgroundLocationPermissionGranted: Boolean
)

expect class PlatformDiagnostics {
    suspend fun getSystemInfo(): SystemInfo
    suspend fun getBatteryInfo(): BatteryInfo
    suspend fun getPermissionsStatus(): PermissionsStatus
    suspend fun getLocationInfo(): LocationInfo
    suspend fun getDatabaseSizeMB(): Double
}
