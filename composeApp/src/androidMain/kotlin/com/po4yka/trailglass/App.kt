package com.po4yka.trailglass

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.ui.navigation.AppRootComponent
import com.po4yka.trailglass.ui.navigation.AuthNavigation
import com.po4yka.trailglass.ui.navigation.MainScaffold
import com.po4yka.trailglass.ui.theme.TrailGlassTheme

/**
 * Main application composable with authentication support.
 * This is the entry point for the UI, integrating Decompose navigation.
 */
@Composable
fun App(
    appRootComponent: AppRootComponent,
    networkConnectivityMonitor: NetworkConnectivityMonitor
) {
    TrailGlassTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                appRootComponent = appRootComponent,
                networkConnectivityMonitor = networkConnectivityMonitor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * App-level navigation that switches between Auth and Main flows.
 */
@Composable
fun AppNavigation(
    appRootComponent: AppRootComponent,
    networkConnectivityMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    val childStack by appRootComponent.childStack.subscribeAsState()

    Children(
        stack = childStack,
        modifier = modifier
    ) { child ->
        when (val instance = child.instance) {
            is AppRootComponent.Child.Auth -> {
                // Show authentication flow
                AuthNavigation(
                    authRootComponent = instance.component,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is AppRootComponent.Child.Main -> {
                // Show main app
                MainScaffold(
                    rootComponent = instance.component,
                    networkConnectivityMonitor = networkConnectivityMonitor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
