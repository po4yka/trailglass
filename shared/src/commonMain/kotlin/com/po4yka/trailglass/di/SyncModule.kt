package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.remote.ApiConfig
import com.po4yka.trailglass.data.remote.DeviceInfoProvider

import com.po4yka.trailglass.data.remote.TokenProvider
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.auth.TokenStorage
import com.po4yka.trailglass.data.remote.auth.TokenStorageProvider
import com.po4yka.trailglass.data.security.EncryptionService
import com.po4yka.trailglass.data.security.SyncDataEncryption
import com.po4yka.trailglass.data.sync.ConflictResolver
import com.po4yka.trailglass.data.sync.DefaultConflictResolver
import com.po4yka.trailglass.data.sync.SyncCoordinator
import com.po4yka.trailglass.data.sync.SyncStateRepository
import com.po4yka.trailglass.data.sync.SyncStateRepositoryImpl
import com.po4yka.trailglass.data.sync.SyncableEntity
import kotlinx.serialization.json.Json
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
    /** Provides API configuration. Can be overridden for different environments (dev, staging, prod). */
    @Provides
    fun provideApiConfig(): ApiConfig =
        ApiConfig(
            baseUrl = "https://trailglass.po4yka.com/api/v1",
            timeout = 30000,
            enableLogging = true // Should be false in production
        )

    /** Provides secure token storage. Platform-specific implementation. */
    @AppScope
    @Provides
    fun provideSecureTokenStorage(storage: TokenStorage): TokenStorage = storage

    /** Provides token provider for authentication. */
    @AppScope
    @Provides
    fun provideTokenProvider(tokenStorage: TokenStorage): TokenProvider = TokenStorageProvider(tokenStorage)

    /** Provides platform device information provider. */
    @AppScope
    @Provides
    fun provideDeviceInfoProvider(deviceInfoProvider: DeviceInfoProvider): DeviceInfoProvider = deviceInfoProvider

    /** Provides the main API client. */
    @AppScope
    @Provides
    fun provideApiClient(
        config: ApiConfig,
        tokenProvider: TokenProvider,
        deviceInfoProvider: DeviceInfoProvider
    ): TrailGlassApiClient = TrailGlassApiClient(config, tokenProvider, deviceInfoProvider)

    /** Provides sync state repository. Platform-specific implementation. */
    @AppScope
    @Provides
    fun provideSyncStateRepository(repository: SyncStateRepository): SyncStateRepository = repository

    /** Provides sync coordinator. */
    @AppScope
    @Provides
    fun provideSyncCoordinator(
        apiClient: TrailGlassApiClient,
        syncStateRepository: SyncStateRepository
    ): SyncCoordinator = SyncCoordinator(apiClient, syncStateRepository)

    /** Provides default conflict resolver. */
    @AppScope
    @Provides
    fun provideConflictResolver(): ConflictResolver<SyncableEntity> = DefaultConflictResolver()

    /** Provides sync metadata repository. */
    @AppScope
    @Provides
    fun provideSyncMetadataRepository(
        impl: com.po4yka.trailglass.data.sync.SyncMetadataRepositoryImpl
    ): com.po4yka.trailglass.data.sync.SyncMetadataRepository = impl

    /** Provides conflict repository for manual resolution. */
    @AppScope
    @Provides
    fun provideConflictRepository(
        impl: com.po4yka.trailglass.data.sync.ConflictRepositoryImpl
    ): com.po4yka.trailglass.data.sync.ConflictRepository = impl

    /** Provides centralized sync manager. */
    @AppScope
    @Provides
    fun provideSyncManager(
        syncCoordinator: SyncCoordinator,
        syncMetadataRepository: com.po4yka.trailglass.data.sync.SyncMetadataRepository,
        conflictRepository: com.po4yka.trailglass.data.sync.ConflictRepository,
        networkMonitor: com.po4yka.trailglass.data.network.NetworkConnectivityMonitor,
        placeVisitRepository: com.po4yka.trailglass.data.repository.PlaceVisitRepository,
        tripRepository: com.po4yka.trailglass.data.repository.TripRepository,
        locationRepository: com.po4yka.trailglass.data.repository.LocationRepository,
        photoRepository: com.po4yka.trailglass.data.repository.PhotoRepository,
        settingsRepository: com.po4yka.trailglass.data.repository.SettingsRepository,
        apiClient: TrailGlassApiClient,
        deviceId: String,
        userId: String
    ): com.po4yka.trailglass.data.sync.SyncManager =
        com.po4yka.trailglass.data.sync.SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = syncMetadataRepository,
            conflictRepository = conflictRepository,
            networkMonitor = networkMonitor,
            placeVisitRepository = placeVisitRepository,
            tripRepository = tripRepository,
            locationRepository = locationRepository,
            photoRepository = photoRepository,
            settingsRepository = settingsRepository,
            apiClient = apiClient,
            deviceId = deviceId,
            userId = userId
        )

    /** Provides JSON serializer for encryption. */
    @AppScope
    @Provides
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

    /** Provides sync data encryption for E2E encryption. */
    @AppScope
    @Provides
    fun provideSyncDataEncryption(
        encryptionService: EncryptionService,
        json: Json
    ): SyncDataEncryption = SyncDataEncryption(encryptionService, json)
}
