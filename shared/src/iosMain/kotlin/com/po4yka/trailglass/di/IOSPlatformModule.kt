package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.db.IosDatabaseDriverFactory
import com.po4yka.trailglass.data.network.IOSNetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.domain.permission.PermissionManager
import com.po4yka.trailglass.domain.service.IosLocationService
import com.po4yka.trailglass.domain.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIDevice

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
    override fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    override fun userId(): String = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

    @Provides
    override fun deviceId(): String = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"

    /**
     * Provides iOS-specific permission manager.
     */
    @Provides
    override fun permissionManager(): PermissionManager = PermissionManager()

    /**
     * Provides iOS-specific encryption service.
     */
    @Provides
    override fun encryptionService(): com.po4yka.trailglass.data.security.EncryptionService =
        com.po4yka.trailglass.data.security.EncryptionService()

    /**
     * Provides iOS-specific photo metadata extractor.
     */
    @Provides
    override fun photoMetadataExtractor(): com.po4yka.trailglass.photo.PhotoMetadataExtractor =
        com.po4yka.trailglass.photo.IosPhotoMetadataExtractor()

    /**
     * Provides iOS-specific settings storage.
     */
    @Provides
    override fun settingsStorage(): com.po4yka.trailglass.data.storage.SettingsStorage =
        com.po4yka.trailglass.data.storage.SettingsStorage()

    @Provides
    override fun secureTokenStorage(): SecureTokenStorage = SecureTokenStorage()

    @Provides
    override fun platformDeviceInfoProvider(): PlatformDeviceInfoProvider = PlatformDeviceInfoProvider()

    @Provides
    override fun syncStateRepositoryImpl(): SyncStateRepositoryImpl = SyncStateRepositoryImpl()

    @Provides
    override fun networkConnectivityMonitor(): NetworkConnectivityMonitor = IOSNetworkConnectivityMonitor()
}
