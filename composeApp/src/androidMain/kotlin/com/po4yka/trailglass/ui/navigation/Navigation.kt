package com.po4yka.trailglass.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Main navigation destinations.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
    object Timeline : Screen("timeline", "Timeline", Icons.Default.ViewTimeline)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

/**
 * Main app scaffold with bottom navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Stats) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(selectedScreen.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val screens = listOf(Screen.Stats, Screen.Timeline, Screen.Settings)

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Screen content
        when (selectedScreen) {
            is Screen.Stats -> StatsScreenPlaceholder(Modifier.padding(paddingValues))
            is Screen.Timeline -> TimelineScreenPlaceholder(Modifier.padding(paddingValues))
            is Screen.Settings -> SettingsScreenPlaceholder(Modifier.padding(paddingValues))
        }
    }
}

@Composable
private fun StatsScreenPlaceholder(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text("Stats Screen - Coming soon")
    }
}

@Composable
private fun TimelineScreenPlaceholder(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text("Timeline Screen - Coming soon")
    }
}

@Composable
private fun SettingsScreenPlaceholder(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text("Settings Screen - Coming soon")
    }
}
