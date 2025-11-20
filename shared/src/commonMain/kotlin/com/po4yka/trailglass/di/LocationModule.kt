package com.po4yka.trailglass.di

import com.po4yka.trailglass.location.PlaceVisitProcessor
import com.po4yka.trailglass.location.RouteSegmentBuilder
import com.po4yka.trailglass.location.LocationProcessor
import com.po4yka.trailglass.location.geocoding.CachedReverseGeocoder
import com.po4yka.trailglass.location.geocoding.GeocodingCache
import com.po4yka.trailglass.location.geocoding.ReverseGeocoder
import com.po4yka.trailglass.location.geocoding.createReverseGeocoder
import com.po4yka.trailglass.location.trip.TripDetector
import com.po4yka.trailglass.location.trip.TripDayAggregator
import com.po4yka.trailglass.location.tracking.DefaultLocationTracker
import com.po4yka.trailglass.location.tracking.LocationTracker
import me.tatarka.inject.annotations.Provides

/**
 * Location processing dependency injection module.
 * Provides location processors and geocoding services.
 */
interface LocationModule {

    /**
     * Provides platform-specific ReverseGeocoder.
     */
    @AppScope
    @Provides
    fun provideReverseGeocoder(): ReverseGeocoder = createReverseGeocoder()

    /**
     * Provides GeocodingCache for in-memory geocoding cache.
     */
    @AppScope
    @Provides
    fun provideGeocodingCache(): GeocodingCache {
        return GeocodingCache()
    }

    /**
     * Provides cached reverse geocoder.
     */
    @AppScope
    @Provides
    fun provideCachedReverseGeocoder(
        geocoder: ReverseGeocoder,
        cache: GeocodingCache
    ): CachedReverseGeocoder {
        return CachedReverseGeocoder(geocoder, cache)
    }

    /**
     * Provides PlaceVisitProcessor.
     */
    @AppScope
    @Provides
    fun providePlaceVisitProcessor(
        reverseGeocoder: CachedReverseGeocoder
    ): PlaceVisitProcessor {
        return PlaceVisitProcessor(reverseGeocoder)
    }

    /**
     * Provides RouteSegmentBuilder.
     */
    @AppScope
    @Provides
    fun provideRouteSegmentBuilder(): RouteSegmentBuilder {
        return RouteSegmentBuilder()
    }

    /**
     * Provides TripDetector.
     */
    @AppScope
    @Provides
    fun provideTripDetector(): TripDetector {
        return TripDetector()
    }

    /**
     * Provides TripDayAggregator.
     */
    @AppScope
    @Provides
    fun provideTripDayAggregator(): TripDayAggregator {
        return TripDayAggregator()
    }

    /**
     * Provides LocationProcessor.
     */
    @AppScope
    @Provides
    fun provideLocationProcessor(
        placeVisitProcessor: PlaceVisitProcessor,
        routeSegmentBuilder: RouteSegmentBuilder,
        tripDetector: TripDetector,
        tripDayAggregator: TripDayAggregator
    ): LocationProcessor {
        return LocationProcessor(
            placeVisitProcessor = placeVisitProcessor,
            routeSegmentBuilder = routeSegmentBuilder,
            tripDetector = tripDetector,
            tripDayAggregator = tripDayAggregator
        )
    }

    /**
     * Provides LocationTracker.
     */
    @AppScope
    @Provides
    fun provideLocationTracker(
        locationService: com.po4yka.trailglass.domain.service.LocationService,
        locationRepository: com.po4yka.trailglass.data.repository.LocationRepository,
        coroutineScope: kotlinx.coroutines.CoroutineScope,
        userId: String,
        deviceId: String
    ): LocationTracker {
        return DefaultLocationTracker(
            locationService = locationService,
            locationRepository = locationRepository,
            coroutineScope = coroutineScope,
            userId = userId,
            deviceId = deviceId
        )
    }
}
