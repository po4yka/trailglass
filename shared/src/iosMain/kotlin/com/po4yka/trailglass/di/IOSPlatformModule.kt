package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
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
    override val databaseDriverFactory: DatabaseDriverFactory
        get() = DatabaseDriverFactory()

    @Provides
    override val locationService: LocationService
        get() = IosLocationService()

    @Provides
    override val applicationScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    override val userId: String
        get() = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

    @Provides
    override val deviceId: String
        get() = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"

    /**
     * Provides iOS-specific permission manager.
     */
    @Provides
    override val permissionManager: PermissionManager
        get() = PermissionManager()

    /**
     * Provides iOS-specific encryption service.
     */
    @Provides
    override val encryptionService: com.po4yka.trailglass.data.security.EncryptionService
        get() = com.po4yka.trailglass.data.security.EncryptionService()

    /**
     * Provides iOS-specific photo metadata extractor.
     */
    @Provides
    override val photoMetadataExtractor: com.po4yka.trailglass.photo.PhotoMetadataExtractor
        get() = com.po4yka.trailglass.photo.IOSPhotoMetadataExtractor()

    /**
     * Provides iOS-specific settings storage.
     */
    @Provides
    override val settingsStorage: com.po4yka.trailglass.data.storage.SettingsStorage
        get() = com.po4yka.trailglass.data.storage.SettingsStorage()

    @Provides
    override val secureTokenStorage: SecureTokenStorage
        get() = SecureTokenStorage()

    @Provides
    override val platformDeviceInfoProvider: PlatformDeviceInfoProvider
        get() = PlatformDeviceInfoProvider()

    @Provides
    override val syncStateRepositoryImpl: SyncStateRepositoryImpl
        get() = SyncStateRepositoryImpl()

    @Provides
    override val networkConnectivityMonitor: NetworkConnectivityMonitor
        get() = IOSNetworkConnectivityMonitor()
}
