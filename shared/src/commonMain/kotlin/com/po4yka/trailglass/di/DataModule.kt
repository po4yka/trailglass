package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.file.FileOperations
import com.po4yka.trailglass.data.file.PhotoFileManager
import com.po4yka.trailglass.data.file.PhotoStorageManager
import com.po4yka.trailglass.data.repository.FrequentPlaceRepository
import com.po4yka.trailglass.data.repository.GeocodingCacheRepository
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.data.repository.SettingsRepositoryImpl
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.repository.WidgetStateRepository
import com.po4yka.trailglass.data.repository.impl.FrequentPlaceRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.WidgetStateRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.GeocodingCacheRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.LocationRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.PhotoRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.PlaceVisitRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.RegionRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.RouteSegmentRepositoryImpl
import com.po4yka.trailglass.data.repository.impl.TripRepositoryImpl
import com.po4yka.trailglass.domain.service.AlgorithmProvider
import com.po4yka.trailglass.logging.LogBuffer
import me.tatarka.inject.annotations.Provides

/** Data layer dependency injection module. Provides repositories and database dependencies. */
interface DataModule {
    /** Provides the application database. Scoped to application lifecycle. */
    @AppScope
    @Provides
    fun provideDatabase(driverFactory: DatabaseDriverFactory): Database = Database(driverFactory)

    /** Provides LocationRepository implementation. */
    @AppScope
    @Provides
    fun provideLocationRepository(impl: LocationRepositoryImpl): LocationRepository = impl

    /** Provides PlaceVisitRepository implementation. */
    @AppScope
    @Provides
    fun providePlaceVisitRepository(impl: PlaceVisitRepositoryImpl): PlaceVisitRepository = impl

    /** Provides RouteSegmentRepository implementation. */
    @AppScope
    @Provides
    fun provideRouteSegmentRepository(impl: RouteSegmentRepositoryImpl): RouteSegmentRepository = impl

    /** Provides TripRepository implementation. */
    @AppScope
    @Provides
    fun provideTripRepository(impl: TripRepositoryImpl): TripRepository = impl

    /** Provides PhotoRepository implementation. */
    @AppScope
    @Provides
    fun providePhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository = impl

    /** Provides GeocodingCacheRepository implementation. */
    @AppScope
    @Provides
    fun provideGeocodingCacheRepository(database: Database): GeocodingCacheRepository = GeocodingCacheRepositoryImpl(database)

    /** Provides FrequentPlaceRepository implementation. */
    @AppScope
    @Provides
    fun provideFrequentPlaceRepository(impl: FrequentPlaceRepositoryImpl): FrequentPlaceRepository = impl

    /** Provides SettingsRepository implementation. */
    @AppScope
    @Provides
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    /** Provides RegionRepository implementation. */
    @AppScope
    @Provides
    fun provideRegionRepository(impl: RegionRepositoryImpl): RegionRepository = impl

    /** Provides WidgetStateRepository implementation. */
    @AppScope
    @Provides
    fun provideWidgetStateRepository(impl: WidgetStateRepositoryImpl): WidgetStateRepository = impl

    /** Provides AlgorithmProvider for dynamic algorithm selection. */
    @AppScope
    @Provides
    fun provideAlgorithmProvider(settingsRepository: SettingsRepository): AlgorithmProvider = AlgorithmProvider(settingsRepository)

    /** Provides FileOperations for cross-platform file I/O. */
    @AppScope
    @Provides
    fun provideFileOperations(): FileOperations = FileOperations()

    /** Provides PhotoFileManager for managing photo file storage. */
    @AppScope
    @Provides
    fun providePhotoFileManager(fileOperations: FileOperations): PhotoFileManager = PhotoFileManager(fileOperations)

    /** Provides ExportManager for exporting data to various formats. */
    @AppScope
    @Provides
    fun provideExportManager(fileOperations: FileOperations): com.po4yka.trailglass.data.file.ExportManager =
        com.po4yka.trailglass.data.file
            .ExportManager(fileOperations)

    /** Provides PhotoStorageManager for coordinating photo file and database operations. */
    @AppScope
    @Provides
    fun providePhotoStorageManager(
        photoFileManager: PhotoFileManager,
        photoRepository: PhotoRepository
    ): PhotoStorageManager = PhotoStorageManager(photoFileManager, photoRepository)

    /** Provides LogBuffer singleton for in-memory log storage. */
    @AppScope
    @Provides
    fun provideLogBuffer(): LogBuffer = LogBuffer
}
