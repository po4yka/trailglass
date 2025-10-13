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
import com.po4yka.trailglass.ui.screens.MapScreen
import com.po4yka.trailglass.ui.screens.SettingsScreen
import com.po4yka.trailglass.ui.screens.StatsScreen
import com.po4yka.trailglass.ui.screens.TimelineScreen

/**
 * Main navigation destinations.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
    object Timeline : Screen("timeline", "Timeline", Icons.Default.ViewTimeline)
    object Map : Screen("map", "Map", Icons.Default.Map)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        fun fromConfig(config: RootComponent.Config): Screen = when (config) {
            is RootComponent.Config.Stats -> Stats
            is RootComponent.Config.Timeline -> Timeline
            is RootComponent.Config.Map -> Map
            is RootComponent.Config.Settings -> Settings
        }

        fun toConfig(screen: Screen): RootComponent.Config = when (screen) {
            is Stats -> RootComponent.Config.Stats
            is Timeline -> RootComponent.Config.Timeline
            is Map -> RootComponent.Config.Map
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
    modifier: Modifier = Modifier
) {
    val childStack by rootComponent.childStack.subscribeAsState()
    val activeChild = childStack.active

    // Determine current screen from active child
    val currentScreen = when (activeChild.instance) {
        is RootComponent.Child.Stats -> Screen.Stats
        is RootComponent.Child.Timeline -> Screen.Timeline
        is RootComponent.Child.Map -> Screen.Map
        is RootComponent.Child.Settings -> Screen.Settings
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val screens = listOf(Screen.Stats, Screen.Timeline, Screen.Map, Screen.Settings)

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
    ) { paddingValues ->
        // Screen content using Decompose's Children
        Children(
            stack = childStack,
            modifier = Modifier.padding(paddingValues)
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Stats -> {
                    StatsScreen(
                        controller = instance.component.statsController,
                        modifier = Modifier
                    )
                }

                is RootComponent.Child.Timeline -> {
                    TimelineScreen(
                        controller = instance.component.timelineController,
                        modifier = Modifier
                    )
                }

                is RootComponent.Child.Map -> {
                    MapScreen(
                        controller = instance.component.mapController,
                        modifier = Modifier
                    )
                }

                is RootComponent.Child.Settings -> {
                    SettingsScreen(
                        trackingController = instance.component.locationTrackingController,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}
