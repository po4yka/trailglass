package com.po4yka.trailglass.data.network

import kotlinx.coroutines.flow.Flow

/**
 * Network connectivity monitor.
 *
 * Provides real-time network status and connectivity checks.
 */
interface NetworkConnectivity {
    /** Flow of network connection status. Emits true when connected, false when disconnected. */
    val isConnected: Flow<Boolean>

    /**
     * Check current network connection status.
     *
     * @return true if network is available, false otherwise
     */
    suspend fun isNetworkAvailable(): Boolean

    /**
     * Get network type (WiFi, Cellular, etc.).
     *
     * @return NetworkType enum indicating connection type
     */
    suspend fun getNetworkType(): NetworkType
}

/** Network connection types. */
enum class NetworkType {
    /** No network connection */
    NONE,

    /** WiFi connection */
    WIFI,

    /** Cellular/mobile data connection */
    CELLULAR,

    /** Ethernet connection */
    ETHERNET,

    /** Other/unknown connection type */
    OTHER
}

/** Factory for creating platform-specific NetworkConnectivity instances. */
expect object NetworkConnectivityFactory {
    fun create(): NetworkConnectivity
}
