package com.po4yka.trailglass.data.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Network connectivity state.
 */
sealed class NetworkState {
    data object Connected : NetworkState()

    data object Disconnected : NetworkState()

    data class Limited(
        val reason: String
    ) : NetworkState() // e.g., metered connection
}

/**
 * Network connectivity information.
 */
data class NetworkInfo(
    val state: NetworkState,
    val type: NetworkType,
    val isMetered: Boolean = false
)

/**
 * Monitors network connectivity status.
 *
 * Platform-specific implementations track network state changes
 * and provide real-time updates through StateFlow.
 */
interface NetworkConnectivityMonitor {
    /**
     * Current network connectivity state.
     */
    val networkState: StateFlow<NetworkState>

    /**
     * Current network information.
     */
    val networkInfo: StateFlow<NetworkInfo>

    /**
     * Check if currently connected to network.
     */
    fun isConnected(): Boolean

    /**
     * Check if current network is metered (e.g., cellular data).
     */
    fun isMetered(): Boolean

    /**
     * Start monitoring network connectivity.
     * Should be called when app starts or component is initialized.
     */
    fun startMonitoring()

    /**
     * Stop monitoring network connectivity.
     * Should be called when app stops or component is destroyed.
     */
    fun stopMonitoring()
}

/**
 * Extension to check if network state allows sync.
 */
fun NetworkState.allowsSync(): Boolean =
    when (this) {
        is NetworkState.Connected -> true
        is NetworkState.Disconnected -> false
        is NetworkState.Limited -> false // Can be changed to allow limited sync
    }
