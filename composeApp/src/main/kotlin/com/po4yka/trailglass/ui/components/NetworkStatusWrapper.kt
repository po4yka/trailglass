package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor

/**
 * Wrapper composable that displays network status banner and content.
 * Shows a banner at the top when network is disconnected or limited.
 */
@Composable
fun NetworkStatusWrapper(
    networkConnectivityMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val networkInfo by networkConnectivityMonitor.networkInfo.collectAsState()

    Column(modifier = modifier) {
        // Network status banner (shows only when not connected)
        NetworkStatusBanner(
            networkState = networkInfo.state,
            networkType = networkInfo.type,
            isMetered = networkInfo.isMetered
        )

        // Main content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
