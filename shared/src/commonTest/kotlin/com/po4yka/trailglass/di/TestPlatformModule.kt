package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkInfo
import com.po4yka.trailglass.data.network.NetworkState
import com.po4yka.trailglass.data.remote.DeviceInfoProvider
import com.po4yka.trailglass.data.remote.auth.TokenStorage
import com.po4yka.trailglass.data.security.EncryptedData
import com.po4yka.trailglass.data.security.EncryptionService
import com.po4yka.trailglass.data.storage.SettingsStorage
import com.po4yka.trailglass.data.sync.SyncState
import com.po4yka.trailglass.data.sync.SyncStateRepository
import com.po4yka.trailglass.domain.model.AppSettings
import com.po4yka.trailglass.domain.permission.PermissionManager
import com.po4yka.trailglass.domain.permission.PermissionResult
import com.po4yka.trailglass.domain.permission.PermissionState
import com.po4yka.trailglass.domain.permission.PermissionType
import com.po4yka.trailglass.domain.service.CrashReportingService
import com.po4yka.trailglass.domain.service.LocationService
import com.po4yka.trailglass.domain.service.PerformanceMonitoringService
import com.po4yka.trailglass.domain.service.PushNotificationService
import com.po4yka.trailglass.feature.diagnostics.BatteryInfo
import com.po4yka.trailglass.feature.diagnostics.LocationInfo
import com.po4yka.trailglass.feature.diagnostics.PlatformDiagnostics
import com.po4yka.trailglass.feature.diagnostics.PermissionsStatus
import com.po4yka.trailglass.feature.diagnostics.SystemInfo
import com.po4yka.trailglass.location.CurrentLocationProvider
import com.po4yka.trailglass.location.LocationData
import com.po4yka.trailglass.photo.PhotoMetadataExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

/**
 * Platform module tailored for tests. Provides lightweight, deterministic implementations of platform contracts so
 * components can be constructed in unit tests without Android/iOS dependencies.
 */
class TestPlatformModule(
    private val testScope: TestScope = TestScope(StandardTestDispatcher()),
    private val testUserId: String = "test-user",
    private val testDeviceId: String = "test-device"
) : PlatformModule {
    private val connectivityMonitor = FakeNetworkConnectivityMonitor()
    private val locationService = FakeLocationService()
    private val currentLocationProvider = FakeCurrentLocationProvider()

    override fun databaseDriverFactory(): DatabaseDriverFactory = TestDatabaseDriverFactory()

    override fun locationService(): LocationService = locationService

    override fun currentLocationProvider(): CurrentLocationProvider = currentLocationProvider

    override fun applicationScope(): CoroutineScope = testScope

    override fun userId(userSession: UserSession): String = userSession.getCurrentUserId() ?: testUserId

    override fun deviceId(): String = testDeviceId

    override fun secureTokenStorage(): TokenStorage = InMemoryTokenStorage()

    override fun platformDeviceInfoProvider(): DeviceInfoProvider = object : DeviceInfoProvider {
        override fun getDeviceId(): String = testDeviceId
        override fun getDeviceName(): String = "Test Device"
        override fun getPlatform(): String = "test"
        override fun getOsVersion(): String = "test-os"
        override fun getAppVersion(): String = "0.0.0-test"
    }

    override fun syncStateRepository(): SyncStateRepository = InMemorySyncStateRepository()

    override fun networkConnectivityMonitor(): NetworkConnectivityMonitor = connectivityMonitor

    override fun permissionManager(): PermissionManager = FakePermissionManager()

    override fun encryptionService(): EncryptionService = FakeEncryptionService()

    override fun photoMetadataExtractor(): PhotoMetadataExtractor = FakePhotoMetadataExtractor()

    override fun settingsStorage(): SettingsStorage = InMemorySettingsStorage()

    override fun photoDirectoryProvider(): com.po4yka.trailglass.data.file.PhotoDirectoryProvider =
        FakePhotoDirectoryProvider()

    override fun pushNotificationService(): PushNotificationService = FakePushNotificationService()

    override fun crashReportingService(): CrashReportingService = FakeCrashReportingService()

    override fun performanceMonitoringService(): PerformanceMonitoringService = FakePerformanceMonitoringService()

    override fun platformDiagnostics(): PlatformDiagnostics = FakePlatformDiagnostics()
}

class FakeUserSession(
    private var currentUserId: String? = "test-user"
) : UserSession {
    override fun getCurrentUserId(): String? = currentUserId
    override fun isAuthenticated(): Boolean = currentUserId != null
    override fun setUserId(userId: String?) {
        currentUserId = userId
    }
}

class InMemoryTokenStorage : TokenStorage {
    private var tokens: com.po4yka.trailglass.data.remote.auth.AuthTokens? = null

    override suspend fun saveTokens(tokens: com.po4yka.trailglass.data.remote.auth.AuthTokens) {
        this.tokens = tokens
    }

    override suspend fun getTokens(): com.po4yka.trailglass.data.remote.auth.AuthTokens? = tokens

    override suspend fun clearTokens() {
        tokens = null
    }
}

class InMemorySyncStateRepository : SyncStateRepository {
    private var state = SyncState()

    override suspend fun getSyncState(): SyncState = state

    override suspend fun updateSyncState(state: SyncState) {
        this.state = state
    }
}

class InMemorySettingsStorage(
    private val initialSettings: AppSettings = AppSettings()
) : SettingsStorage {
    private val state = MutableStateFlow(initialSettings)

    override fun getSettingsFlow(): StateFlow<AppSettings> = state.asStateFlow()

    override suspend fun getSettings(): AppSettings = state.value

    override suspend fun saveSettings(settings: AppSettings) {
        state.value = settings
    }

    override suspend fun clearSettings() {
        state.value = AppSettings()
    }
}

class FakeNetworkConnectivityMonitor : NetworkConnectivityMonitor {
    private val state = MutableStateFlow<NetworkState>(NetworkState.Connected)
    private val info = MutableStateFlow(NetworkInfo(NetworkState.Connected, type = com.po4yka.trailglass.data.network.NetworkType.WIFI))

    override val networkState: StateFlow<NetworkState> = state.asStateFlow()
    override val networkInfo: StateFlow<NetworkInfo> = info.asStateFlow()

    override fun isConnected(): Boolean = networkState.value is NetworkState.Connected

    override fun isMetered(): Boolean = false

    override fun startMonitoring() {
        // no-op for tests
    }

    override fun stopMonitoring() {
        // no-op for tests
    }
}

class FakePermissionManager : PermissionManager {
    private val states = mutableMapOf<PermissionType, MutableStateFlow<PermissionState>>()

    override suspend fun checkPermission(permissionType: PermissionType): PermissionState =
        states.getOrPut(permissionType) { MutableStateFlow(PermissionState.Granted) }.value

    override suspend fun requestPermission(permissionType: PermissionType): PermissionResult {
        states.getOrPut(permissionType) { MutableStateFlow(PermissionState.Granted) }.value = PermissionState.Granted
        return PermissionResult.Granted
    }

    override suspend fun shouldShowRationale(permissionType: PermissionType): Boolean = false

    override suspend fun openAppSettings() {
        // no-op for tests
    }

    override fun observePermission(permissionType: PermissionType): StateFlow<PermissionState> =
        states.getOrPut(permissionType) { MutableStateFlow(PermissionState.Granted) }.asStateFlow()
}

class FakeEncryptionService : EncryptionService {
    override suspend fun encrypt(plaintext: String): Result<EncryptedData> =
        Result.success(EncryptedData(ciphertext = plaintext, iv = "iv", tag = "tag"))

    override suspend fun decrypt(encryptedData: EncryptedData): Result<String> =
        Result.success(encryptedData.ciphertext)

    override suspend fun hasEncryptionKey(): Boolean = true

    override suspend fun generateKey(): Result<Unit> = Result.success(Unit)

    override suspend fun exportKey(password: String): Result<String> = Result.success("exported:$password")

    override suspend fun importKey(
        encryptedKeyBackup: String,
        password: String
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deleteKey(): Result<Unit> = Result.success(Unit)
}

class FakePhotoMetadataExtractor : PhotoMetadataExtractor {
    override suspend fun extractMetadata(
        photoUri: String,
        photoId: String
    ): com.po4yka.trailglass.domain.model.PhotoMetadata? = null
}

class FakePhotoDirectoryProvider : com.po4yka.trailglass.data.file.PhotoDirectoryProvider {
    override fun getPhotosDirectory(): String = "/tmp/photos"
    override fun getThumbnailsDirectory(): String = "/tmp/photos/thumbs"
    override fun getTempPhotosDirectory(): String = "/tmp/photos/temp"
}

class FakePushNotificationService : PushNotificationService {
    override val tokenFlow: kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.emptyFlow()

    override suspend fun getToken(): String? = null

    override suspend fun requestNotificationPermission(): Boolean = true

    override suspend fun hasNotificationPermission(): Boolean = true

    override suspend fun subscribeToTopic(topic: String) {
        // no-op
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        // no-op
    }

    override suspend fun deleteToken() {
        // no-op
    }
}

class FakeCrashReportingService : CrashReportingService {
    override fun recordException(throwable: Throwable) {
        // no-op
    }

    override fun log(message: String) {
        // no-op
    }

    override fun setCustomKeyString(key: String, value: String) {}
    override fun setCustomKeyInt(key: String, value: Int) {}
    override fun setCustomKeyBool(key: String, value: Boolean) {}
    override fun setCustomKeyFloat(key: String, value: Float) {}
    override fun setCustomKeyDouble(key: String, value: Double) {}
    override fun setCustomKeyLong(key: String, value: Long) {}

    override fun setUserId(userId: String) {
        // no-op
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {}

    override fun isCrashlyticsCollectionEnabled(): Boolean = true

    override fun sendUnsentReports() {}

    override fun deleteUnsentReports() {}

    override fun didCrashOnPreviousExecution(): Boolean = false
}

class FakePerformanceMonitoringService : PerformanceMonitoringService {
    override fun startTrace(name: String) {
        // no-op
    }

    override fun stopTrace(name: String) {
        // no-op
    }

    override fun putMetric(
        traceName: String,
        metricName: String,
        value: Long
    ) {
        // no-op
    }

    override fun incrementMetric(
        traceName: String,
        metricName: String,
        incrementBy: Long
    ) {
        // no-op
    }

    override fun putAttribute(
        traceName: String,
        attribute: String,
        value: String
    ) {
        // no-op
    }

    override fun removeAttribute(
        traceName: String,
        attribute: String
    ) {
        // no-op
    }

    override fun getAttribute(
        traceName: String,
        attribute: String
    ): String? = null

    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        // no-op
    }

    override fun isPerformanceCollectionEnabled(): Boolean = true
}

class FakePlatformDiagnostics : PlatformDiagnostics {
    override suspend fun getSystemInfo(): SystemInfo =
        SystemInfo(
            appVersion = "0.0.0-test",
            buildNumber = "0",
            osVersion = "test-os",
            deviceModel = "test-device"
        )

    override suspend fun getBatteryInfo(): BatteryInfo =
        BatteryInfo(
            batteryLevel = 1.0f,
            batteryOptimizationDisabled = true,
            lowPowerMode = false
        )

    override suspend fun getPermissionsStatus(): PermissionsStatus =
        PermissionsStatus(
            locationPermissionGranted = true,
            backgroundLocationPermissionGranted = true,
            notificationsPermissionGranted = true,
            photoLibraryPermissionGranted = true
        )

    override suspend fun getLocationInfo(): LocationInfo =
        LocationInfo(
            accuracy = null,
            satellites = null,
            locationPermissionGranted = true,
            backgroundLocationPermissionGranted = true
        )

    override suspend fun getDatabaseSizeMB(): Double = 0.0
}

class FakeLocationService : LocationService {
    private val updates = MutableSharedFlow<com.po4yka.trailglass.domain.model.Coordinate>(extraBufferCapacity = 16)
    private var isTracking = false
    private var lastLocation: com.po4yka.trailglass.domain.model.Coordinate? = null

    override val locationUpdates = updates.asSharedFlow()

    override suspend fun getLastKnownLocation(): com.po4yka.trailglass.domain.model.Coordinate? = lastLocation

    override suspend fun startTracking(
        intervalMs: Long,
        fastestIntervalMs: Long
    ) {
        isTracking = true
    }

    override suspend fun stopTracking() {
        isTracking = false
    }

    override suspend fun hasLocationPermission(): Boolean = true

    override suspend fun requestLocationPermission(background: Boolean): Boolean = true

    override fun isTracking(): Boolean = isTracking

    suspend fun emitLocation(coordinate: com.po4yka.trailglass.domain.model.Coordinate) {
        lastLocation = coordinate
        updates.emit(coordinate)
    }
}

class FakeCurrentLocationProvider : CurrentLocationProvider {
    private val updates = MutableSharedFlow<LocationData>(extraBufferCapacity = 8)
    private var lastLocation: LocationData? = null

    override suspend fun getCurrentLocation(): Result<LocationData> =
        lastLocation?.let { Result.success(it) } ?: Result.failure(IllegalStateException("No location"))

    override fun observeLocation(): kotlinx.coroutines.flow.Flow<LocationData> = updates.asSharedFlow()

    override val lastKnownLocation: LocationData?
        get() = lastLocation

    suspend fun emit(location: LocationData) {
        lastLocation = location
        updates.emit(location)
    }
}

/**
 * Test database driver factory hooked into SQLDelight in-memory drivers for the active platform.
 */
expect class TestDatabaseDriverFactory() : DatabaseDriverFactory

/** Convenient helper to build a fully wired AppComponent for tests. */
fun createTestAppComponent(
    userId: String = "test-user",
    deviceId: String = "test-device"
): AppComponent =
    AppComponent::class.create(
        TestPlatformModule(
            testScope = TestScope(StandardTestDispatcher()),
            testUserId = userId,
            testDeviceId = deviceId
        )
    )
