package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.data.repository.*
import com.po4yka.trailglass.data.repository.impl.*
import me.tatarka.inject.annotations.Provides

/**
 * Data layer dependency injection module.
 * Provides repositories and database dependencies.
 */
interface DataModule {

    /**
     * Provides the application database.
     * Scoped to application lifecycle.
     */
    @AppScope
    @Provides
    fun provideDatabase(driverFactory: DatabaseDriverFactory): Database {
        return Database(driverFactory)
    }

    /**
     * Provides LocationRepository implementation.
     */
    @AppScope
    @Provides
    fun provideLocationRepository(impl: LocationRepositoryImpl): LocationRepository = impl

    /**
     * Provides PlaceVisitRepository implementation.
     */
    @AppScope
    @Provides
    fun providePlaceVisitRepository(impl: PlaceVisitRepositoryImpl): PlaceVisitRepository = impl

    /**
     * Provides RouteSegmentRepository implementation.
     */
    @AppScope
    @Provides
    fun provideRouteSegmentRepository(impl: RouteSegmentRepositoryImpl): RouteSegmentRepository = impl

    /**
     * Provides TripRepository implementation.
     */
    @AppScope
    @Provides
    fun provideTripRepository(impl: TripRepositoryImpl): TripRepository = impl

    /**
     * Provides PhotoRepository implementation.
     */
    @AppScope
    @Provides
    fun providePhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository = impl

    /**
     * Provides GeocodingCacheRepository implementation.
     */
    @AppScope
    @Provides
    fun provideGeocodingCacheRepository(database: Database): GeocodingCacheRepository {
        return GeocodingCacheRepositoryImpl(database)
    }

    /**
     * Provides FrequentPlaceRepository implementation.
     */
    @AppScope
    @Provides
    fun provideFrequentPlaceRepository(impl: FrequentPlaceRepositoryImpl): FrequentPlaceRepository = impl

    /**
     * Provides SettingsRepository implementation.
     */
    @AppScope
    @Provides
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}
