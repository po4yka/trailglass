package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.po4yka.trailglass.di.AppComponent
import kotlinx.serialization.Serializable

/**
 * Root component for the application navigation.
 * Manages the main navigation stack with bottom navigation screens.
 */
interface RootComponent {
    /**
     * Access to the application component for DI.
     */
    val appComponent: AppComponent

    /**
     * Child stack representing the navigation stack.
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Navigate to a specific screen.
     */
    fun navigateToScreen(config: Config)

    /**
     * Handle deep link navigation.
     * Parses deep link paths and navigates to the appropriate screen.
     *
     * Supported deep links:
     * - trailglass://app/stats
     * - trailglass://app/timeline
     * - trailglass://app/map
     * - trailglass://app/settings
     */
    fun handleDeepLink(path: String)

    /**
     * Sealed class representing the navigation configurations.
     */
    @Serializable
    sealed interface Config {
        @Serializable
        data object Stats : Config

        @Serializable
        data object Timeline : Config

        @Serializable
        data object Map : Config

        @Serializable
        data object Photos : Config

        @Serializable
        data object Trips : Config

        @Serializable
        data object Settings : Config

        @Serializable
        data class RouteView(val tripId: String) : Config

        @Serializable
        data class RouteReplay(val tripId: String) : Config

        @Serializable
        data class TripStatistics(val tripId: String) : Config

        @Serializable
        data class PhotoDetail(val photoId: String) : Config

        @Serializable
        data class PlaceVisitDetail(val placeVisitId: String) : Config
    }

    /**
     * Sealed class representing the child components.
     */
    sealed class Child {
        data class Stats(val component: StatsComponent) : Child()
        data class Timeline(val component: TimelineComponent) : Child()
        data class Map(val component: MapComponent) : Child()
        data class Photos(val component: PhotosComponent) : Child()
        data class Trips(val component: TripsComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
        data class RouteView(val component: RouteViewComponent) : Child()
        data class RouteReplay(val component: RouteReplayComponent) : Child()
        data class TripStatistics(val component: TripStatisticsComponent) : Child()
        data class PhotoDetail(val component: PhotoDetailComponent) : Child()
        data class PlaceVisitDetail(val component: PlaceVisitDetailComponent) : Child()
    }
}

/**
 * Default implementation of RootComponent.
 */
class DefaultRootComponent(
    componentContext: ComponentContext,
    override val appComponent: AppComponent
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<RootComponent.Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = RootComponent.Config.serializer(),
        initialConfiguration = RootComponent.Config.Stats,
        handleBackButton = true,
        childFactory = ::createChild
    )

    override fun navigateToScreen(config: RootComponent.Config) {
        when (config) {
            // Main screens - replace the stack
            is RootComponent.Config.Stats,
            is RootComponent.Config.Timeline,
            is RootComponent.Config.Map,
            is RootComponent.Config.Photos,
            is RootComponent.Config.Trips,
            is RootComponent.Config.Settings -> navigation.replaceAll(config)

            // Detail screens - push onto stack
            is RootComponent.Config.RouteView,
            is RootComponent.Config.RouteReplay,
            is RootComponent.Config.TripStatistics,
            is RootComponent.Config.PhotoDetail,
            is RootComponent.Config.PlaceVisitDetail -> navigation.push(config)
        }
    }

    override fun handleDeepLink(path: String) {
        val config = parseDeepLink(path)
        if (config != null) {
            navigateToScreen(config)
        }
    }

    /**
     * Parse deep link path to navigation configuration.
     * Supports paths like: /stats, /timeline, /map, /photos, /settings
     */
    private fun parseDeepLink(path: String): RootComponent.Config? {
        val cleanPath = path.trim('/').lowercase()
        return when (cleanPath) {
            "stats" -> RootComponent.Config.Stats
            "timeline" -> RootComponent.Config.Timeline
            "map" -> RootComponent.Config.Map
            "photos" -> RootComponent.Config.Photos
            "trips" -> RootComponent.Config.Trips
            "settings" -> RootComponent.Config.Settings
            else -> null
        }
    }

    private fun createChild(
        config: RootComponent.Config,
        componentContext: ComponentContext
    ): RootComponent.Child = when (config) {
        is RootComponent.Config.Stats -> RootComponent.Child.Stats(
            component = DefaultStatsComponent(
                componentContext = componentContext,
                statsController = appComponent.statsController
            )
        )

        is RootComponent.Config.Timeline -> RootComponent.Child.Timeline(
            component = DefaultTimelineComponent(
                componentContext = componentContext,
                timelineController = appComponent.timelineController
            )
        )

        is RootComponent.Config.Map -> RootComponent.Child.Map(
            component = DefaultMapComponent(
                componentContext = componentContext,
                mapController = appComponent.mapController
            )
        )

        is RootComponent.Config.Photos -> RootComponent.Child.Photos(
            component = DefaultPhotosComponent(
                componentContext = componentContext,
                photoGalleryController = appComponent.photoGalleryController
            )
        )

        is RootComponent.Config.Trips -> RootComponent.Child.Trips(
            component = DefaultTripsComponent(
                componentContext = componentContext,
                tripsController = appComponent.tripsController
            )
        )

        is RootComponent.Config.Settings -> RootComponent.Child.Settings(
            component = DefaultSettingsComponent(
                componentContext = componentContext,
                locationTrackingController = appComponent.locationTrackingController
            )
        )

        is RootComponent.Config.RouteView -> RootComponent.Child.RouteView(
            component = DefaultRouteViewComponent(
                componentContext = componentContext,
                tripId = config.tripId,
                routeViewController = appComponent.routeViewController,
                onNavigateToReplay = { tripId -> navigateToScreen(RootComponent.Config.RouteReplay(tripId)) },
                onNavigateToStatistics = { tripId -> navigateToScreen(RootComponent.Config.TripStatistics(tripId)) },
                onBack = { navigation.pop() }
            )
        )

        is RootComponent.Config.RouteReplay -> RootComponent.Child.RouteReplay(
            component = DefaultRouteReplayComponent(
                componentContext = componentContext,
                tripId = config.tripId,
                routeReplayController = appComponent.routeReplayController,
                onBack = { navigation.pop() }
            )
        )

        is RootComponent.Config.TripStatistics -> RootComponent.Child.TripStatistics(
            component = DefaultTripStatisticsComponent(
                componentContext = componentContext,
                tripId = config.tripId,
                tripStatisticsController = appComponent.tripStatisticsController,
                onBack = { navigation.pop() }
            )
        )

        is RootComponent.Config.PhotoDetail -> RootComponent.Child.PhotoDetail(
            component = DefaultPhotoDetailComponent(
                componentContext = componentContext,
                photoDetailController = appComponent.photoDetailController,
                photoId = config.photoId,
                onBack = { navigation.pop() }
            )
        )

        is RootComponent.Config.PlaceVisitDetail -> RootComponent.Child.PlaceVisitDetail(
            component = DefaultPlaceVisitDetailComponent(
                componentContext = componentContext,
                placeVisitId = config.placeVisitId,
                onBack = { navigation.pop() }
            )
        )
    }
}
