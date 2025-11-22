package com.po4yka.trailglass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.data.sync.SyncProgress
import com.po4yka.trailglass.data.sync.SyncStatusUiModel
import com.po4yka.trailglass.ui.theme.extended
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Compact sync status indicator that can be placed in app bar or bottom bar.
 */
@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatusUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (syncStatus.progress) {
        is SyncProgress.Idle -> Icons.Default.CloudOff
        is SyncProgress.InProgress -> Icons.Default.CloudSync
        is SyncProgress.Completed -> Icons.Default.CloudDone
        is SyncProgress.Failed -> Icons.Default.CloudOff
    }

    val tint = when (syncStatus.progress) {
        is SyncProgress.Idle -> MaterialTheme.colorScheme.extended.disabled
        is SyncProgress.InProgress -> MaterialTheme.colorScheme.primary
        is SyncProgress.Completed -> MaterialTheme.colorScheme.extended.success
        is SyncProgress.Failed -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Rotating animation for in-progress state
        val infiniteTransition = rememberInfiniteTransition(label = "sync rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = if (syncStatus.isActive) 360f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Icon(
            imageVector = icon,
            contentDescription = "Sync status",
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .then(if (syncStatus.isActive) Modifier.rotate(rotation) else Modifier)
        )

        // Badge for pending count
        if (syncStatus.pendingCount > 0 && !syncStatus.isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (syncStatus.pendingCount > 9) "9+" else syncStatus.pendingCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7f
                )
            }
        }

        // Badge for conflicts
        if (syncStatus.conflictCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.extended.warning),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Conflicts",
                    tint = MaterialTheme.colorScheme.extended.onWarning,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}

/**
 * Detailed sync status card with full information.
 */
@Composable
fun SyncStatusCard(
    syncStatus: SyncStatusUiModel,
    onSyncClick: () -> Unit,
    onViewConflictsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (syncStatus.progress) {
                is SyncProgress.Failed -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sync Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SyncStatusIndicator(
                    syncStatus = syncStatus,
                    onClick = { /* Show details */ }
                )
            }

            // Progress message
            when (val progress = syncStatus.progress) {
                is SyncProgress.Idle -> {
                    Text(
                        text = "Ready to sync",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is SyncProgress.InProgress -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { progress.percentage / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = progress.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is SyncProgress.Completed -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Last sync completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.extended.success
                        )
                        Text(
                            text = "↑ ${progress.result.uploaded} uploaded, " +
                                   "↓ ${progress.result.downloaded} downloaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (progress.result.conflicts > 0) {
                            Text(
                                text = "⚠ ${progress.result.conflicts} conflicts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is SyncProgress.Failed -> {
                    Text(
                        text = "Sync failed: ${progress.error}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Sync info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pending: ${syncStatus.pendingCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    syncStatus.lastSyncTime?.let { lastSync ->
                        Text(
                            text = "Last sync: ${formatLastSyncTime(lastSync)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (syncStatus.conflictCount > 0) {
                    TextButton(onClick = onViewConflictsClick) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${syncStatus.conflictCount} Conflicts")
                    }
                }
            }

            // Action button
            Button(
                onClick = onSyncClick,
                enabled = !syncStatus.isActive,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (syncStatus.isActive) "Syncing..." else "Sync Now")
            }
        }
    }
}

private fun formatLastSyncTime(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
}
