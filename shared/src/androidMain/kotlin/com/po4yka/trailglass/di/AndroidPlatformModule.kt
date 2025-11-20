package com.po4yka.trailglass.di

import android.content.Context
import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.network.AndroidNetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.data.security.EncryptionService
import com.po4yka.trailglass.photo.PhotoMetadataExtractor
import com.po4yka.trailglass.photo.AndroidPhotoMetadataExtractor
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
    override fun databaseDriverFactory(): DatabaseDriverFactory =
        DatabaseDriverFactory(context)

    @Provides
    override fun locationService(): LocationService =
        AndroidLocationService(context)

    @Provides
    override fun applicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    override fun userId(): String =
        DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

    @Provides
    override fun deviceId(): String =
        android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

    /**
     * Provides Android-specific secure token storage.
     */
    /**
     * Provides Android-specific secure token storage.
     */
    @Provides
    override fun secureTokenStorage(): SecureTokenStorage =
        SecureTokenStorage(context)

    /**
     * Provides Android-specific device info provider.
     */
    /**
     * Provides Android-specific device info provider.
     */
    @Provides
    override fun platformDeviceInfoProvider(): PlatformDeviceInfoProvider =
        PlatformDeviceInfoProvider(context)

    /**
     * Provides Android-specific sync state repository.
     */
    /**
     * Provides Android-specific sync state repository.
     */
    @Provides
    override fun syncStateRepositoryImpl(): SyncStateRepositoryImpl =
        SyncStateRepositoryImpl(context)

    /**
     * Provides Android-specific network connectivity monitor.
     */
    /**
     * Provides Android-specific network connectivity monitor.
     */
    @Provides
    override fun networkConnectivityMonitor(): NetworkConnectivityMonitor =
        AndroidNetworkConnectivityMonitor(context)

    /**
     * Provides Android-specific permission manager.
     */
    /**
     * Provides Android-specific permission manager.
     */
    @Provides
    override fun permissionManager(): PermissionManager =
        PermissionManager(context)

    /**
     * Provides Android-specific encryption service.
     */
    @Provides
    override fun encryptionService(): EncryptionService =
        EncryptionService()

    /**
     * Provides Android-specific photo metadata extractor.
     */
    @Provides
    override fun photoMetadataExtractor(): PhotoMetadataExtractor =
        AndroidPhotoMetadataExtractor(context)

    /**
     * Provides Android-specific settings storage.
     */
    @Provides
    override fun settingsStorage(): com.po4yka.trailglass.data.storage.SettingsStorage =
        com.po4yka.trailglass.data.storage.SettingsStorage(context)
}
