package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.db.IosDatabaseDriverFactory
import com.po4yka.trailglass.data.file.IOSPhotoDirectoryProvider
import com.po4yka.trailglass.data.file.PhotoDirectoryProvider
import com.po4yka.trailglass.data.network.IOSNetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.DeviceInfoProvider

import com.po4yka.trailglass.data.remote.auth.TokenStorage
import com.po4yka.trailglass.data.sync.SyncStateRepository
import com.po4yka.trailglass.domain.permission.IOSPermissionManager
import com.po4yka.trailglass.domain.permission.PermissionManager
import com.po4yka.trailglass.domain.service.CrashReportingService
import com.po4yka.trailglass.domain.service.IosCrashReportingService
import com.po4yka.trailglass.domain.service.IosLocationService
import com.po4yka.trailglass.domain.service.IosPerformanceMonitoringService
import com.po4yka.trailglass.domain.service.IosPushNotificationService
import com.po4yka.trailglass.domain.service.LocationService
import com.po4yka.trailglass.domain.service.PerformanceMonitoringService
import com.po4yka.trailglass.domain.service.PushNotificationService
import com.po4yka.trailglass.feature.diagnostics.IOSPlatformDiagnostics
import com.po4yka.trailglass.feature.diagnostics.PlatformDiagnostics
import com.po4yka.trailglass.location.CurrentLocationProvider
import com.po4yka.trailglass.location.IOSCurrentLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIDevice
import com.po4yka.trailglass.data.remote.auth.IOSSecureTokenStorage
import com.po4yka.trailglass.data.remote.device.IOSPlatformDeviceInfoProvider
import com.po4yka.trailglass.data.storage.IOSSettingsStorage
import com.po4yka.trailglass.data.security.IOSEncryptionService

/**
 * iOS implementation of PlatformModule.
 *
 * Provides iOS-specific dependencies including:
 * - DatabaseDriverFactory (using iOS SQLite)
 * - LocationService (using CoreLocation)
 * - Application-level CoroutineScope
 * - User ID and Device ID
 * - Secure token storage (Keychain)
 * - Device info provider
 * - Sync state repository (UserDefaults)
 */
@Inject
class IOSPlatformModule : PlatformModule {
    @Provides
    override fun databaseDriverFactory(): DatabaseDriverFactory = IosDatabaseDriverFactory()

    @Provides
    override fun locationService(): LocationService = IosLocationService()

    @Provides
    @AppScope
    override fun currentLocationProvider(): CurrentLocationProvider = IOSCurrentLocationProvider()

    @Provides
    @AppScope
    override fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    override fun userId(): String = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

    @Provides
    override fun deviceId(): String = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"

    /** Provides iOS-specific permission manager. */
    @Provides
    override fun permissionManager(): PermissionManager = IOSPermissionManager()

    /** Provides iOS-specific encryption service. */
    @Provides
    override fun encryptionService(): com.po4yka.trailglass.data.security.EncryptionService =
        com.po4yka.trailglass.data.security
            .IOSEncryptionService()

    /** Provides iOS-specific photo metadata extractor. */
    @Provides
    override fun photoMetadataExtractor(): com.po4yka.trailglass.photo.PhotoMetadataExtractor =
        com.po4yka.trailglass.photo
            .IosPhotoMetadataExtractor()

    /** Provides iOS-specific settings storage. */
    @Provides
    override fun settingsStorage(): com.po4yka.trailglass.data.storage.SettingsStorage =
        com.po4yka.trailglass.data.storage
            .IOSSettingsStorage()



    @Provides
    override fun secureTokenStorage(): TokenStorage = IOSSecureTokenStorage()

    @Provides
    override fun platformDeviceInfoProvider(): DeviceInfoProvider = IOSPlatformDeviceInfoProvider()

    @Provides
    override fun syncStateRepository(): SyncStateRepository = SyncStateRepositoryImpl()

    @Provides
    override fun networkConnectivityMonitor(): NetworkConnectivityMonitor = IOSNetworkConnectivityMonitor()

    /** Provides iOS-specific photo directory provider. */
    @Provides
    override fun photoDirectoryProvider(): PhotoDirectoryProvider = IOSPhotoDirectoryProvider()

    /** Provides iOS-specific push notification service. */
    @Provides
    override fun pushNotificationService(): PushNotificationService = IosPushNotificationService()

    /** Provides iOS-specific crash reporting service. */
    @Provides
    override fun crashReportingService(): CrashReportingService = IosCrashReportingService()

    /** Provides iOS-specific performance monitoring service. */
    @Provides
    override fun performanceMonitoringService(): PerformanceMonitoringService = IosPerformanceMonitoringService()

    /** Provides iOS-specific platform diagnostics. */
    @Provides
    override fun platformDiagnostics(): PlatformDiagnostics = IOSPlatformDiagnostics()
}
