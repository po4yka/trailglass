package com.po4yka.trailglass.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/** Android implementation of NetworkConnectivityMonitor using ConnectivityManager. */
@Inject
class AndroidNetworkConnectivityMonitor(
    private val context: Context
) : NetworkConnectivityMonitor {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Debounce delay to prevent rapid state changes from causing UI flicker
    private val debounceDelayMs = 300L
    private var debounceJob: Job? = null

    private val _networkState = MutableStateFlow<NetworkState>(getCurrentNetworkState())
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _networkInfo = MutableStateFlow(getCurrentNetworkInfo())
    override val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private var isMonitoring = false

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                logger.debug { "Network available: $network" }
                scheduleNetworkStateUpdate()
            }

            override fun onLost(network: Network) {
                logger.debug { "Network lost: $network" }
                scheduleNetworkStateUpdate()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                logger.debug { "Network capabilities changed for $network: hasInternet=${networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}, hasValidated=${networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}" }
                scheduleNetworkStateUpdate()
            }

            override fun onUnavailable() {
                logger.debug { "Network unavailable" }
                // Cancel any pending debounce and update immediately for disconnection
                debounceJob?.cancel()
                updateNetworkStateImmediate()
            }
        }

    override fun isConnected(): Boolean = networkState.value is NetworkState.Connected

    override fun isMetered(): Boolean = networkInfo.value.isMetered

    override fun startMonitoring() {
        if (isMonitoring) return

        // Only require internet capability, not validation
        // This prevents delays when network is connected but not yet validated by Android
        val networkRequest =
            NetworkRequest
                .Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        isMonitoring = true

        logger.info { "Network monitoring started" }

        // Update initial state immediately (no debounce for startup)
        updateNetworkStateImmediate()
    }

    override fun stopMonitoring() {
        if (!isMonitoring) return

        try {
            debounceJob?.cancel()
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isMonitoring = false
            logger.info { "Network monitoring stopped" }
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Network callback was not registered" }
        }
    }

    /**
     * Schedule a debounced network state update.
     * This prevents rapid network changes from causing UI flicker.
     */
    private fun scheduleNetworkStateUpdate() {
        debounceJob?.cancel()
        debounceJob =
            scope.launch {
                delay(debounceDelayMs)
                updateNetworkStateImmediate()
            }
    }

    /**
     * Update network state immediately without debouncing.
     * Used for initial state and disconnection events.
     */
    private fun updateNetworkStateImmediate() {
        val state = getCurrentNetworkState()
        val info = getCurrentNetworkInfo()

        _networkState.value = state
        _networkInfo.value = info

        logger.debug { "Network state updated: state=$state, type=${info.type}, metered=${info.isMetered}" }
    }

    private fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        // No active network or no capabilities = disconnected
        if (capabilities == null) {
            return NetworkState.Disconnected
        }

        // Check if network has internet capability
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return when {
            // Connected with validated internet access
            hasInternet && isValidated -> NetworkState.Connected

            // Has internet capability but not yet validated - still consider it connected
            // This prevents showing "no connection" banner during the validation period
            hasInternet -> NetworkState.Connected

            // No internet capability at all
            else -> NetworkState.Disconnected
        }
    }

    private fun getCurrentNetworkInfo(): NetworkInfo {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (capabilities == null) {
            return NetworkInfo(
                state = NetworkState.Disconnected,
                type = NetworkType.NONE,
                isMetered = false
            )
        }

        val type =
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }

        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val state =
            when {
                // Connected with validated internet access
                hasInternet && isValidated -> NetworkState.Connected

                // Has internet capability but not yet validated - still consider it connected
                hasInternet -> NetworkState.Connected

                // No internet capability
                else -> NetworkState.Disconnected
            }

        return NetworkInfo(
            state = state,
            type = type,
            isMetered = isMetered
        )
    }
}
