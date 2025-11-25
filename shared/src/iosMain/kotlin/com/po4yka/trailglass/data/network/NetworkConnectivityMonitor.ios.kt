package com.po4yka.trailglass.data.network

import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_interface_type_wired
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_constrained
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfiable
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_status_unsatisfied
import platform.Network.nw_path_t
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_queue_create

private val logger = logger("IOSNetworkConnectivityMonitor")

/** iOS implementation of NetworkConnectivityMonitor using NWPathMonitor. */
@Inject
class IOSNetworkConnectivityMonitor : NetworkConnectivityMonitor {
    private val pathMonitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("com.po4yka.trailglass.networkmonitor", null)
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Debounce delay to prevent rapid state changes from causing UI flicker
    private val debounceDelayMs = 300L
    private var debounceJob: Job? = null

    // Initialize with current state instead of assuming disconnected
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Connected)
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _networkInfo =
        MutableStateFlow(
            NetworkInfo(
                state = NetworkState.Connected,
                type = NetworkType.WIFI,
                isMetered = false
            )
        )
    override val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private var isMonitoring = false

    override fun isConnected(): Boolean = networkState.value is NetworkState.Connected

    override fun isMetered(): Boolean = networkInfo.value.isMetered

    override fun startMonitoring() {
        if (isMonitoring) return

        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            scheduleNetworkStateUpdate(path)
        }

        nw_path_monitor_set_queue(pathMonitor, queue)
        nw_path_monitor_start(pathMonitor)
        isMonitoring = true

        logger.info { "Network monitoring started (iOS)" }
    }

    override fun stopMonitoring() {
        if (!isMonitoring) return

        debounceJob?.cancel()
        nw_path_monitor_cancel(pathMonitor)
        isMonitoring = false

        logger.info { "Network monitoring stopped (iOS)" }
    }

    /**
     * Schedule a debounced network state update.
     * This prevents rapid network changes from causing UI flicker.
     */
    private fun scheduleNetworkStateUpdate(path: nw_path_t?) {
        val status = path?.let { nw_path_get_status(it) }

        // For disconnection, update immediately; otherwise debounce
        if (status == nw_path_status_unsatisfied) {
            debounceJob?.cancel()
            updateNetworkStateImmediate(path)
        } else {
            debounceJob?.cancel()
            debounceJob =
                scope.launch {
                    delay(debounceDelayMs)
                    updateNetworkStateImmediate(path)
                }
        }
    }

    /**
     * Update network state immediately without debouncing.
     */
    private fun updateNetworkStateImmediate(path: nw_path_t?) {
        updateNetworkState(path)
    }

    private fun updateNetworkState(path: nw_path_t?) {
        if (path == null) {
            _networkState.value = NetworkState.Disconnected
            _networkInfo.value =
                NetworkInfo(
                    state = NetworkState.Disconnected,
                    type = NetworkType.NONE,
                    isMetered = false
                )
            logger.debug { "Network state updated: Disconnected (null path)" }
            return
        }

        val status = nw_path_get_status(path)
        val isExpensive = nw_path_is_expensive(path)
        val isConstrained = nw_path_is_constrained(path)

        val state =
            when (status) {
                nw_path_status_satisfied -> NetworkState.Connected
                nw_path_status_unsatisfied -> NetworkState.Disconnected
                // Treat satisfiable (network available but not currently usable) as disconnected
                nw_path_status_satisfiable -> NetworkState.Disconnected
                else -> NetworkState.Disconnected
            }

        val type =
            when {
                nw_path_uses_interface_type(path, nw_interface_type_wifi) -> NetworkType.WIFI
                nw_path_uses_interface_type(path, nw_interface_type_cellular) -> NetworkType.CELLULAR
                nw_path_uses_interface_type(path, nw_interface_type_wired) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }

        _networkState.value = state
        _networkInfo.value =
            NetworkInfo(
                state = state,
                type = type,
                isMetered = isExpensive
            )

        logger.debug { "Network state updated: state=$state, type=$type, metered=$isExpensive, constrained=$isConstrained" }
    }
}
