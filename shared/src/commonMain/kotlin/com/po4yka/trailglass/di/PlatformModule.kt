package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.domain.service.LocationService
import kotlinx.coroutines.CoroutineScope

/**
 * Platform-specific dependency injection module.
 *
 * This interface defines dependencies that must be provided by platform-specific code:
 * - DatabaseDriverFactory (Android/iOS specific)
 * - LocationService (Android/iOS specific)
 * - CoroutineScope (application lifecycle scope)
 * - User ID (from platform authentication)
 * - Device ID (platform-specific identifier)
 *
 * Each platform (Android, iOS) should create a concrete implementation of this interface.
 */
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.domain.permission.PermissionManager

interface PlatformModule {
    /**
     * Platform-specific database driver factory.
     */
    val databaseDriverFactory: DatabaseDriverFactory

    /**
     * Platform-specific location service.
     * Provides real-time location tracking functionality.
     */
    val locationService: LocationService

    /**
     * Application-level coroutine scope.
     * Should be tied to application lifecycle.
     */
    val applicationScope: CoroutineScope

    /**
     * Current user ID.
     */
    val userId: String

    /**
     * Device identifier.
     */
    val deviceId: String

    /**
     * Platform-specific secure token storage.
     */
    val secureTokenStorage: SecureTokenStorage

    /**
     * Platform-specific device info provider.
     */
    val platformDeviceInfoProvider: PlatformDeviceInfoProvider

    /**
     * Platform-specific sync state repository.
     */
    val syncStateRepositoryImpl: SyncStateRepositoryImpl

    /**
     * Platform-specific network connectivity monitor.
     */
    val networkConnectivityMonitor: NetworkConnectivityMonitor

    /**
     * Platform-specific permission manager.
     */
    val permissionManager: PermissionManager

    /**
     * Platform-specific encryption service.
     */
    val encryptionService: com.po4yka.trailglass.data.security.EncryptionService

    /**
     * Platform-specific photo metadata extractor.
     */
    val photoMetadataExtractor: com.po4yka.trailglass.photo.PhotoMetadataExtractor
}
