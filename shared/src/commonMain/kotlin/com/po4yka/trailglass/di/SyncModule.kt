package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.remote.ApiConfig
import com.po4yka.trailglass.data.remote.DeviceInfoProvider
import com.po4yka.trailglass.data.remote.TokenProvider
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.auth.SecureTokenStorage
import com.po4yka.trailglass.data.remote.auth.TokenStorageProvider
import com.po4yka.trailglass.data.remote.device.PlatformDeviceInfoProvider
import com.po4yka.trailglass.data.repository.SyncableLocationRepository
import com.po4yka.trailglass.data.sync.ConflictResolver
import com.po4yka.trailglass.data.sync.DefaultConflictResolver
import com.po4yka.trailglass.data.sync.SyncCoordinator
import com.po4yka.trailglass.data.sync.SyncStateRepository
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.data.sync.SyncableEntity
import me.tatarka.inject.annotations.Provides

/**
 * Sync and backend API dependency injection module.
 *
 * Provides:
 * - Backend API client
 * - Token storage and authentication
 * - Device information
 * - Sync coordination
 * - Conflict resolution
 * - Syncable repositories
 */
interface SyncModule {

    /**
     * Provides API configuration.
     * Can be overridden for different environments (dev, staging, prod).
     */
    @Provides
    fun provideApiConfig(): ApiConfig {
        return ApiConfig(
            baseUrl = "https://trailglass.po4yka.com/api/v1",
            timeout = 30000,
            enableLogging = true // Should be false in production
        )
    }

    /**
     * Provides secure token storage.
     * Platform-specific implementation.
     */
    @AppScope
    @Provides
    fun provideSecureTokenStorage(impl: SecureTokenStorage): SecureTokenStorage = impl

    /**
     * Provides token provider for authentication.
     */
    @AppScope
    @Provides
    fun provideTokenProvider(secureStorage: SecureTokenStorage): TokenProvider {
        return TokenStorageProvider(secureStorage)
    }

    /**
     * Provides platform device information provider.
     */
    @AppScope
    @Provides
    fun provideDeviceInfoProvider(impl: PlatformDeviceInfoProvider): DeviceInfoProvider = impl

    /**
     * Provides the main API client.
     */
    @AppScope
    @Provides
    fun provideApiClient(
        config: ApiConfig,
        tokenProvider: TokenProvider,
        deviceInfoProvider: DeviceInfoProvider
    ): TrailGlassApiClient {
        return TrailGlassApiClient(config, tokenProvider, deviceInfoProvider)
    }

    /**
     * Provides sync state repository.
     * Platform-specific implementation.
     */
    @AppScope
    @Provides
    fun provideSyncStateRepository(impl: SyncStateRepositoryImpl): SyncStateRepository = impl

    /**
     * Provides sync coordinator.
     */
    @AppScope
    @Provides
    fun provideSyncCoordinator(
        apiClient: TrailGlassApiClient,
        syncStateRepository: SyncStateRepository
    ): SyncCoordinator {
        return SyncCoordinator(apiClient, syncStateRepository)
    }

    /**
     * Provides default conflict resolver.
     */
    @AppScope
    @Provides
    fun provideConflictResolver(): ConflictResolver<SyncableEntity> {
        return DefaultConflictResolver()
    }

    /**
     * Provides syncable location repository.
     */
    @AppScope
    @Provides
    fun provideSyncableLocationRepository(
        locationRepository: com.po4yka.trailglass.data.repository.LocationRepository,
        apiClient: TrailGlassApiClient,
        syncCoordinator: SyncCoordinator,
        deviceId: String
    ): SyncableLocationRepository {
        // Get the DAO from the repository
        val dao = (locationRepository as com.po4yka.trailglass.data.repository.impl.LocationRepositoryImpl).locationDao

        return SyncableLocationRepository(
            locationDao = dao,
            apiClient = apiClient,
            syncCoordinator = syncCoordinator,
            deviceId = deviceId
        )
    }
}
