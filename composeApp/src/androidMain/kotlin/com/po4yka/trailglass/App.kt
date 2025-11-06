package com.po4yka.trailglass

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.ui.navigation.MainScaffold
import com.po4yka.trailglass.ui.navigation.RootComponent
import com.po4yka.trailglass.ui.theme.TrailGlassTheme

/**
 * Main application composable.
 * This is the entry point for the UI, integrating Decompose navigation.
 */
@Composable
fun App(
    rootComponent: RootComponent,
    networkConnectivityMonitor: NetworkConnectivityMonitor
) {
    TrailGlassTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScaffold(
                rootComponent = rootComponent,
                networkConnectivityMonitor = networkConnectivityMonitor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
