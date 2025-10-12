package com.po4yka.trailglass.data.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.*
import platform.darwin.dispatch_queue_create
import platform.posix.DISPATCH_QUEUE_SERIAL

/**
 * iOS implementation of NetworkConnectivity using Network framework.
 */
class IOSNetworkConnectivity : NetworkConnectivity {

    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("com.po4yka.trailglass.network", DISPATCH_QUEUE_SERIAL)

    override val isConnected: Flow<Boolean> = callbackFlow {
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            trySend(status == nw_path_status_satisfied || status == nw_path_status_satisfiable)
        }

        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }

    override suspend fun isNetworkAvailable(): Boolean {
        val path = nw_path_monitor_copy_current_path(monitor)
        val status = nw_path_get_status(path)
        return status == nw_path_status_satisfied || status == nw_path_status_satisfiable
    }

    override suspend fun getNetworkType(): NetworkType {
        val path = nw_path_monitor_copy_current_path(monitor)

        return when {
            nw_path_uses_interface_type(path, nw_interface_type_wifi) -> NetworkType.WIFI
            nw_path_uses_interface_type(path, nw_interface_type_cellular) -> NetworkType.CELLULAR
            nw_path_uses_interface_type(path, nw_interface_type_wired) -> NetworkType.ETHERNET
            else -> {
                val status = nw_path_get_status(path)
                if (status == nw_path_status_satisfied || status == nw_path_status_satisfiable) {
                    NetworkType.OTHER
                } else {
                    NetworkType.NONE
                }
            }
        }
    }
}

actual object NetworkConnectivityFactory {
    actual fun create(): NetworkConnectivity {
        return IOSNetworkConnectivity()
    }
}
