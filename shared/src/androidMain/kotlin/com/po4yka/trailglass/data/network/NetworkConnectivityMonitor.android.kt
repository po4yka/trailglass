package com.po4yka.trailglass.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

/** Android implementation of NetworkConnectivityMonitor using ConnectivityManager. */
@Inject
class AndroidNetworkConnectivityMonitor(
    private val context: Context
) : NetworkConnectivityMonitor {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow<NetworkState>(getCurrentNetworkState())
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _networkInfo = MutableStateFlow(getCurrentNetworkInfo())
    override val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private var isMonitoring = false

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateNetworkState()
                println("Network available: $network")
            }

            override fun onLost(network: Network) {
                updateNetworkState()
                println("Network lost: $network")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetworkState()
                println("Network capabilities changed: $networkCapabilities")
            }

            override fun onUnavailable() {
                _networkState.value = NetworkState.Disconnected
                _networkInfo.value =
                    NetworkInfo(
                        state = NetworkState.Disconnected,
                        type = NetworkType.NONE,
                        isMetered = false
                    )
                println("Network unavailable")
            }
        }

    override fun isConnected(): Boolean = networkState.value is NetworkState.Connected

    override fun isMetered(): Boolean = networkInfo.value.isMetered

    override fun startMonitoring() {
        if (isMonitoring) return

        val networkRequest =
            NetworkRequest
                .Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        isMonitoring = true

        // Update initial state
        updateNetworkState()

        println("Network monitoring started")
    }

    override fun stopMonitoring() {
        if (!isMonitoring) return

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isMonitoring = false
            println("Network monitoring stopped")
        } catch (e: Exception) {
            println("Error stopping network monitoring: ${e.message}")
        }
    }

    private fun updateNetworkState() {
        val state = getCurrentNetworkState()
        val info = getCurrentNetworkInfo()

        _networkState.value = state
        _networkInfo.value = info
    }

    private fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        return if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                NetworkState.Connected
            } else {
                NetworkState.Limited("No internet access")
            }
        } else {
            NetworkState.Disconnected
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
                else -> NetworkType.NONE
            }

        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        val state =
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    NetworkState.Connected
                } else {
                    NetworkState.Limited("No internet access")
                }
            } else {
                NetworkState.Disconnected
            }

        return NetworkInfo(
            state = state,
            type = type,
            isMetered = isMetered
        )
    }
}
