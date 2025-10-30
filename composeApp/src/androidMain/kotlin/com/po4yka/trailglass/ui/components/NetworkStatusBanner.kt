package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.data.network.NetworkState
import com.po4yka.trailglass.data.network.NetworkType

/**
 * Banner showing network connectivity status.
 * Appears at top of screen when offline or network is limited.
 */
@Composable
fun NetworkStatusBanner(
    networkState: NetworkState,
    networkType: NetworkType,
    isMetered: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = networkState !is NetworkState.Connected,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        NetworkBannerContent(
            networkState = networkState,
            networkType = networkType,
            isMetered = isMetered
        )
    }
}

@Composable
private fun NetworkBannerContent(
    networkState: NetworkState,
    networkType: NetworkType,
    isMetered: Boolean
) {
    val (icon, text, backgroundColor, contentColor) = when (networkState) {
        is NetworkState.Disconnected -> {
            Tuple4(
                Icons.Default.CloudOff,
                "No internet connection",
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer
            )
        }
        is NetworkState.Limited -> {
            Tuple4(
                Icons.Default.Warning,
                "Limited connectivity: ${networkState.reason}",
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        is NetworkState.Connected -> {
            // Should not be visible when connected
            return
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )

                if (isMetered && networkType != NetworkType.NONE) {
                    Text(
                        text = "Using ${networkType.name.lowercase()} (metered)",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Inline network status indicator for app bar or bottom bar.
 */
@Composable
fun NetworkStatusIndicatorCompact(
    networkState: NetworkState,
    networkType: NetworkType,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (networkState) {
        is NetworkState.Connected -> {
            val networkIcon = when (networkType) {
                NetworkType.WIFI -> Icons.Default.Wifi
                NetworkType.CELLULAR -> Icons.Default.SignalCellularAlt
                NetworkType.ETHERNET -> Icons.Default.SettingsEthernet
                NetworkType.NONE -> Icons.Default.CloudDone
            }
            networkIcon to MaterialTheme.colorScheme.primary
        }
        is NetworkState.Disconnected -> {
            Icons.Default.CloudOff to MaterialTheme.colorScheme.error
        }
        is NetworkState.Limited -> {
            Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
        }
    }

    Icon(
        imageVector = icon,
        contentDescription = when (networkState) {
            is NetworkState.Connected -> "Connected via ${networkType.name}"
            is NetworkState.Disconnected -> "Disconnected"
            is NetworkState.Limited -> "Limited connection"
        },
        tint = tint,
        modifier = modifier.size(20.dp)
    )
}

// Helper data class for multiple return values
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
