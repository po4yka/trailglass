package com.po4yka.trailglass.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject
import platform.Network.*
import platform.darwin.dispatch_queue_create

/**
 * iOS implementation of NetworkConnectivityMonitor using NWPathMonitor.
 */
@Inject
class IOSNetworkConnectivityMonitor : NetworkConnectivityMonitor {

    private val pathMonitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("com.po4yka.trailglass.networkmonitor", null)

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _networkInfo = MutableStateFlow(
        NetworkInfo(
            state = NetworkState.Disconnected,
            type = NetworkType.NONE,
            isMetered = false
        )
    )
    override val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private var isMonitoring = false

    override fun isConnected(): Boolean {
        return networkState.value is NetworkState.Connected
    }

    override fun isMetered(): Boolean {
        return networkInfo.value.isMetered
    }

    override fun startMonitoring() {
        if (isMonitoring) return

        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            updateNetworkState(path)
        }

        nw_path_monitor_set_queue(pathMonitor, queue)
        nw_path_monitor_start(pathMonitor)
        isMonitoring = true

        println("Network monitoring started (iOS)")
    }

    override fun stopMonitoring() {
        if (!isMonitoring) return

        nw_path_monitor_cancel(pathMonitor)
        isMonitoring = false

        println("Network monitoring stopped (iOS)")
    }

    private fun updateNetworkState(path: nw_path_t?) {
        if (path == null) {
            _networkState.value = NetworkState.Disconnected
            _networkInfo.value = NetworkInfo(
                state = NetworkState.Disconnected,
                type = NetworkType.NONE,
                isMetered = false
            )
            return
        }

        val status = nw_path_get_status(path)
        val isExpensive = nw_path_is_expensive(path)
        val isConstrained = nw_path_is_constrained(path)

        val state = when (status) {
            nw_path_status_satisfied -> NetworkState.Connected
            nw_path_status_unsatisfied -> NetworkState.Disconnected
            nw_path_status_satisfiable -> NetworkState.Disconnected
            else -> NetworkState.Disconnected
        }

        val type = when {
            nw_path_uses_interface_type(path, nw_interface_type_wifi) -> NetworkType.WIFI
            nw_path_uses_interface_type(path, nw_interface_type_cellular) -> NetworkType.CELLULAR
            nw_path_uses_interface_type(path, nw_interface_type_wired) -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }

        _networkState.value = state
        _networkInfo.value = NetworkInfo(
            state = state,
            type = type,
            isMetered = isExpensive
        )

        println("Network state updated: state=$state, type=$type, metered=$isExpensive, constrained=$isConstrained")
    }
}
