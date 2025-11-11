package com.po4yka.trailglass.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mock implementation of NetworkConnectivityMonitor for testing.
 */
class MockNetworkConnectivityMonitor : NetworkConnectivityMonitor {

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Connected)
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _networkInfo = MutableStateFlow(
        NetworkInfo(
            state = NetworkState.Connected,
            type = NetworkType.WIFI,
            isMetered = false
        )
    )
    override val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private var isMonitoringActive = false

    override fun isConnected(): Boolean {
        return networkState.value is NetworkState.Connected
    }

    override fun isMetered(): Boolean {
        return networkInfo.value.isMetered
    }

    override fun startMonitoring() {
        isMonitoringActive = true
    }

    override fun stopMonitoring() {
        isMonitoringActive = false
    }

    // Test helpers

    fun setNetworkState(state: NetworkState) {
        _networkState.value = state
        _networkInfo.value = _networkInfo.value.copy(state = state)
    }

    fun setNetworkType(type: NetworkType) {
        _networkInfo.value = _networkInfo.value.copy(type = type)
    }

    fun setMetered(isMetered: Boolean) {
        _networkInfo.value = _networkInfo.value.copy(isMetered = isMetered)
    }

    fun simulateDisconnect() {
        setNetworkState(NetworkState.Disconnected)
        setNetworkType(NetworkType.NONE)
    }

    fun simulateConnect(type: NetworkType = NetworkType.WIFI, isMetered: Boolean = false) {
        setNetworkState(NetworkState.Connected)
        setNetworkType(type)
        setMetered(isMetered)
    }

    fun simulateLimitedConnection(reason: String = "No internet access") {
        setNetworkState(NetworkState.Limited(reason))
    }

    fun isMonitoring(): Boolean = isMonitoringActive
}
