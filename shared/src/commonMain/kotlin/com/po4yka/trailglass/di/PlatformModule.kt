package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.domain.permission.PermissionManager
import com.po4yka.trailglass.domain.service.LocationService
import kotlinx.coroutines.CoroutineScope

interface PlatformModule {
    /** Platform-specific database driver factory. */
    fun databaseDriverFactory(): DatabaseDriverFactory

    /** Platform-specific location service. Provides real-time location tracking functionality. */
    fun locationService(): LocationService

    /** Application-level coroutine scope. Should be tied to application lifecycle. */
    fun applicationScope(): CoroutineScope

    /** Current user ID. */
    fun userId(): String

    /** Device identifier. */
    fun deviceId(): String

    /** Platform-specific secure token storage. */
    fun secureTokenStorage(): SecureTokenStorage

    /** Platform-specific device info provider. */
    fun platformDeviceInfoProvider(): PlatformDeviceInfoProvider

    /** Platform-specific sync state repository. */
    fun syncStateRepositoryImpl(): SyncStateRepositoryImpl

    /** Platform-specific network connectivity monitor. */
    fun networkConnectivityMonitor(): NetworkConnectivityMonitor

    /** Platform-specific permission manager. */
    fun permissionManager(): PermissionManager

    /** Platform-specific encryption service. */
    fun encryptionService(): com.po4yka.trailglass.data.security.EncryptionService

    /** Platform-specific photo metadata extractor. */
    fun photoMetadataExtractor(): com.po4yka.trailglass.photo.PhotoMetadataExtractor

    /** Platform-specific settings storage. */
    fun settingsStorage(): com.po4yka.trailglass.data.storage.SettingsStorage

    /** Platform-specific photo directory provider. */
    fun photoDirectoryProvider(): com.po4yka.trailglass.data.file.PhotoDirectoryProvider

    /** Platform-specific push notification service. */
    fun pushNotificationService(): com.po4yka.trailglass.domain.service.PushNotificationService

    /** Platform-specific crash reporting service. */
    fun crashReportingService(): com.po4yka.trailglass.domain.service.CrashReportingService
}
