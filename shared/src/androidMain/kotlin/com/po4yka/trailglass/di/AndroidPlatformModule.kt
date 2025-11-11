package com.po4yka.trailglass.di

import android.content.Context
import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.network.AndroidNetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.domain.permission.PermissionManager
import com.po4yka.trailglass.domain.service.AndroidLocationService
import com.po4yka.trailglass.domain.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

/**
 * Android implementation of PlatformModule.
 *
 * Provides Android-specific dependencies including:
 * - DatabaseDriverFactory (using Android SQLite)
 * - LocationService (using Google Play Services)
 * - Application-level CoroutineScope
 * - User ID and Device ID
 * - Secure token storage (EncryptedSharedPreferences)
 * - Device info provider
 * - Sync state repository (DataStore)
 */
@Inject
class AndroidPlatformModule(
    private val context: Context
) : PlatformModule {

    @Provides
    override val databaseDriverFactory: DatabaseDriverFactory
        get() = DatabaseDriverFactory(context)

    @Provides
    override val locationService: LocationService
        get() = AndroidLocationService(context)

    @Provides
    override val applicationScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    override val userId: String
        get() = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

    @Provides
    override val deviceId: String
        get() = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

    /**
     * Provides Android-specific secure token storage.
     */
    @AppScope
    @Provides
    fun provideSecureTokenStorage(): SecureTokenStorage {
        return SecureTokenStorage(context)
    }

    /**
     * Provides Android-specific device info provider.
     */
    @AppScope
    @Provides
    fun providePlatformDeviceInfoProvider(): PlatformDeviceInfoProvider {
        return PlatformDeviceInfoProvider(context)
    }

    /**
     * Provides Android-specific sync state repository.
     */
    @AppScope
    @Provides
    fun provideSyncStateRepositoryImpl(): SyncStateRepositoryImpl {
        return SyncStateRepositoryImpl(context)
    }

    /**
     * Provides Android-specific network connectivity monitor.
     */
    @AppScope
    @Provides
    fun provideNetworkConnectivityMonitor(): NetworkConnectivityMonitor {
        return AndroidNetworkConnectivityMonitor(context)
    }

    /**
     * Provides Android-specific permission manager.
     */
    @AppScope
    @Provides
    fun providePermissionManager(): PermissionManager {
        return PermissionManager(context)
    }
}
