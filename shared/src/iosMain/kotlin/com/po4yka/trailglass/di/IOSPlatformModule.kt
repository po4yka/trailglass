package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
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
     * Provides iOS-specific secure token storage.
     */
    @AppScope
    @Provides
    fun provideSecureTokenStorage(): SecureTokenStorage {
        return SecureTokenStorage()
    }

    /**
     * Provides iOS-specific device info provider.
     */
    @AppScope
    @Provides
    fun providePlatformDeviceInfoProvider(): PlatformDeviceInfoProvider {
        return PlatformDeviceInfoProvider()
    }

    /**
     * Provides iOS-specific sync state repository.
     */
    @AppScope
    @Provides
    fun provideSyncStateRepositoryImpl(): SyncStateRepositoryImpl {
        return SyncStateRepositoryImpl()
    }
}
