package com.po4yka.trailglass.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.ui.components.NetworkStatusWrapper
import com.po4yka.trailglass.ui.dialogs.CreateTripDialog
import com.po4yka.trailglass.ui.screens.*

/**
 * Main navigation destinations.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
    object Timeline : Screen("timeline", "Timeline", Icons.Default.ViewTimeline)
    object Map : Screen("map", "Map", Icons.Default.Map)
    object Photos : Screen("photos", "Photos", Icons.Default.PhotoLibrary)
    object Trips : Screen("trips", "Trips", Icons.Default.CardTravel)
    object Places : Screen("places", "Places", Icons.Default.Place)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        fun fromConfig(config: RootComponent.Config): Screen? = when (config) {
            is RootComponent.Config.Stats -> Stats
            is RootComponent.Config.Timeline -> Timeline
            is RootComponent.Config.Map -> Map
            is RootComponent.Config.Photos -> Photos
            is RootComponent.Config.Trips -> Trips
            is RootComponent.Config.Places -> Places
            is RootComponent.Config.Settings -> Settings
            else -> null
        }

        fun toConfig(screen: Screen): RootComponent.Config = when (screen) {
            is Stats -> RootComponent.Config.Stats
            is Timeline -> RootComponent.Config.Timeline
            is Map -> RootComponent.Config.Map
            is Photos -> RootComponent.Config.Photos
            is Trips -> RootComponent.Config.Trips
            is Places -> RootComponent.Config.Places
            is Settings -> RootComponent.Config.Settings
        }
    }
}

/**
 * Main app scaffold with bottom navigation using Decompose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    rootComponent: RootComponent,
    networkConnectivityMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    val childStack by rootComponent.childStack.subscribeAsState()
    val activeChild = childStack.active

    // Determine current screen from active child
    val currentScreen = when (activeChild.instance) {
        is RootComponent.Child.Stats -> Screen.Stats
        is RootComponent.Child.Timeline -> Screen.Timeline
        is RootComponent.Child.Map -> Screen.Map
        is RootComponent.Child.Photos -> Screen.Photos
        is RootComponent.Child.Trips -> Screen.Trips
        is RootComponent.Child.Places -> Screen.Places
        is RootComponent.Child.Settings -> Screen.Settings
        is RootComponent.Child.RouteView,
        is RootComponent.Child.RouteReplay,
        is RootComponent.Child.TripStatistics,
        is RootComponent.Child.PhotoDetail,
        is RootComponent.Child.PlaceVisitDetail,
        is RootComponent.Child.PlaceDetail,
        is RootComponent.Child.DeviceManagement -> null // No bottom nav for detail screens
    }

    // Show bottom nav and top bar only for main screens
    val showBottomNav = currentScreen != null

    // Dialog state for trip creation
    var showCreateTripDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showBottomNav && currentScreen != null) {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomNav && currentScreen != null) {
                NavigationBar {
                    val screens = listOf(Screen.Stats, Screen.Timeline, Screen.Map, Screen.Photos, Screen.Trips, Screen.Places, Screen.Settings)

                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = { rootComponent.navigateToScreen(Screen.toConfig(screen)) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Wrap content with network status indicator
        NetworkStatusWrapper(
            networkConnectivityMonitor = networkConnectivityMonitor,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Screen content using Decompose's Children
            Children(
                stack = childStack
            ) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Stats -> {
                        EnhancedStatsScreen(
                            controller = instance.component.enhancedStatsController,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.Timeline -> {
                        EnhancedTimelineScreen(
                            controller = instance.component.enhancedTimelineController,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.Map -> {
                        MapScreen(
                            controller = instance.component.mapController,
                            onNavigateToPlaceVisitDetail = { placeVisitId ->
                                rootComponent.navigateToScreen(RootComponent.Config.PlaceVisitDetail(placeVisitId))
                            },
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.Photos -> {
                        PhotosScreenWrapper(
                            photoGalleryController = instance.component.photoGalleryController,
                            onPhotoClick = { photoId ->
                                rootComponent.navigateToScreen(RootComponent.Config.PhotoDetail(photoId))
                            },
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.Trips -> {
                        TripsScreen(
                            trips = instance.component.tripsController.state.collectAsState().value.filteredTrips,
                            onTripClick = { trip ->
                                rootComponent.navigateToScreen(RootComponent.Config.RouteView(trip.id))
                            },
                            onCreateTrip = {
                                showCreateTripDialog = true
                            },
                            onRefresh = { instance.component.tripsController.refresh() },
                            modifier = Modifier
                        )

                        // Trip creation dialog
                        if (showCreateTripDialog) {
                            CreateTripDialog(
                                userId = rootComponent.appComponent.authRepository.getCurrentUserId() ?: "",
                                onDismiss = { showCreateTripDialog = false },
                                onConfirm = { trip ->
                                    instance.component.tripsController.createTrip(trip)
                                    showCreateTripDialog = false
                                }
                            )
                        }
                    }

                    is RootComponent.Child.Places -> {
                        val placesState = instance.component.placesController.state.collectAsState().value

                        PlacesScreen(
                            places = placesState.places,
                            searchQuery = placesState.searchQuery,
                            onPlaceClick = { place ->
                                rootComponent.navigateToScreen(RootComponent.Config.PlaceDetail(place.id))
                            },
                            onRefresh = { instance.component.placesController.refresh() },
                            onSearch = { query ->
                                instance.component.placesController.search(query)
                            },
                            onClearSearch = {
                                instance.component.placesController.clearSearch()
                            },
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.Settings -> {
                        SettingsScreen(
                            trackingController = instance.component.locationTrackingController,
                            onNavigateToDeviceManagement = {
                                rootComponent.navigateToScreen(RootComponent.Config.DeviceManagement)
                            },
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.PhotoDetail -> {
                        PhotoDetailScreenWrapper(
                            photoId = instance.component.photoId,
                            photoDetailController = instance.component.photoDetailController,
                            onNavigateBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.RouteView -> {
                        RouteViewScreen(
                            tripId = instance.component.tripId,
                            controller = instance.component.routeViewController,
                            onNavigateToReplay = instance.component.onNavigateToReplay,
                            onNavigateToStatistics = instance.component.onNavigateToStatistics,
                            onBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.RouteReplay -> {
                        RouteReplayScreen(
                            tripId = instance.component.tripId,
                            controller = instance.component.routeReplayController,
                            onBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.TripStatistics -> {
                        TripStatisticsScreen(
                            tripId = instance.component.tripId,
                            controller = instance.component.tripStatisticsController,
                            onBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.PlaceVisitDetail -> {
                        PlaceVisitDetailScreen(
                            placeVisitId = instance.component.placeVisitId,
                            apiClient = rootComponent.appComponent.apiClient,
                            onNavigateBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.PlaceDetail -> {
                        val place = remember(instance.component.placeId) {
                            derivedStateOf {
                                instance.component.placesController.state.value.places
                                    .find { it.id == instance.component.placeId }
                            }
                        }

                        PlaceDetailScreen(
                            place = place.value,
                            onToggleFavorite = {
                                instance.component.placesController.toggleFavorite(instance.component.placeId)
                            },
                            onNavigateBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }

                    is RootComponent.Child.DeviceManagement -> {
                        DeviceManagementScreen(
                            controller = instance.component.deviceManagementController,
                            onBack = instance.component.onBack,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}
