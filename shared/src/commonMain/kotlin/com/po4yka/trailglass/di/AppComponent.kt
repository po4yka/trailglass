package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.repository.*
import com.po4yka.trailglass.feature.map.MapController
import com.po4yka.trailglass.feature.photo.PhotoController
import com.po4yka.trailglass.feature.stats.StatsController
import com.po4yka.trailglass.feature.timeline.TimelineController
import com.po4yka.trailglass.feature.tracking.LocationTrackingController
import com.po4yka.trailglass.location.LocationProcessor
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * Main application dependency injection component.
 *
 * This component provides all application-level dependencies including:
 * - Repositories (data layer)
 * - Location processors
 * - Feature controllers
 *
 * Platform-specific implementations should create a component that includes
 * this interface along with platform-specific modules.
 */
@AppScope
@Component
abstract class AppComponent(
    @Component val platformModule: PlatformModule
) : DataModule, LocationModule {

    /**
     * Provides a CoroutineScope for application-level background work.
     * Platform-specific implementation should provide this.
     */
    abstract val applicationScope: kotlinx.coroutines.CoroutineScope

    /**
     * Provides the current user ID.
     * Platform-specific implementation should provide this.
     */
    abstract val userId: String

    // Repositories (from DataModule)
    abstract val locationRepository: LocationRepository
    abstract val placeVisitRepository: PlaceVisitRepository
    abstract val routeSegmentRepository: RouteSegmentRepository
    abstract val tripRepository: TripRepository
    abstract val photoRepository: PhotoRepository
    abstract val geocodingCacheRepository: GeocodingCacheRepository

    // Location processing (from LocationModule)
    abstract val locationProcessor: LocationProcessor

    // Location service (from PlatformModule)
    abstract val locationService: com.po4yka.trailglass.domain.service.LocationService

    // Feature controllers
    abstract val statsController: StatsController
    abstract val timelineController: TimelineController
    abstract val mapController: MapController
    abstract val photoController: PhotoController
    abstract val locationTrackingController: LocationTrackingController

    companion object
}
