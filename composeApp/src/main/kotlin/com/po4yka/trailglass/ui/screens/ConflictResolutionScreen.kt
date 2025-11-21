package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.data.remote.dto.ConflictType
import com.po4yka.trailglass.data.remote.dto.EntityType
import com.po4yka.trailglass.feature.sync.ConflictResolutionController

/**
 * Conflict Resolution screen showing sync conflicts one at a time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolutionScreen(
    controller: ConflictResolutionController,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Resolve Sync Conflicts")
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            state.isComplete -> {
                // All conflicts resolved
                CompletionView(
                    resolvedCount = state.resolvedCount,
                    onClose = onClose,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            state.currentConflict != null -> {
                // Show current conflict
                ConflictView(
                    state = state,
                    onKeepLocal = { controller.resolveKeepLocal() },
                    onKeepRemote = { controller.resolveKeepRemote() },
                    onMerge = { controller.resolveMerge() },
                    onSkip = { controller.skipConflict() },
                    onPrevious = { controller.previousConflict() },
                    onNext = { controller.nextConflict() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                // No conflicts
                EmptyConflictsView(
                    onClose = onClose,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }

        // Error snackbar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(paddingValues),
                action = {
                    TextButton(onClick = { controller.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(state.error ?: "Unknown error")
            }
        }
    }
}

@Composable
private fun ConflictView(
    state: ConflictResolutionController.ConflictResolutionState,
    onKeepLocal: () -> Unit,
    onKeepRemote: () -> Unit,
    onMerge: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conflict = state.currentConflict ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (state.currentConflictIndex + 1).toFloat() / state.conflicts.size.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Conflict ${state.currentConflictIndex + 1} of ${state.conflicts.size}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Conflict info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text(
                            text = formatEntityType(conflict.entityType),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatConflictType(conflict.conflictType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Local version
        VersionCard(
            title = "Your Version (Local)",
            icon = Icons.Default.PhoneAndroid,
            data = conflict.localVersion,
            modifier = Modifier.fillMaxWidth()
        )

        // Remote version
        VersionCard(
            title = "Server Version (Remote)",
            icon = Icons.Default.Cloud,
            data = conflict.remoteVersion,
            modifier = Modifier.fillMaxWidth()
        )

        // Resolution options
        Text(
            text = "Choose Resolution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Keep Local button
        Button(
            onClick = onKeepLocal,
            enabled = !state.isResolving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Keep My Version")
        }

        // Keep Remote button
        OutlinedButton(
            onClick = onKeepRemote,
            enabled = !state.isResolving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Cloud, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Keep Server Version")
        }

        // Merge button (if applicable)
        if (conflict.localVersion.size + conflict.remoteVersion.size > conflict.localVersion.keys.intersect(conflict.remoteVersion.keys).size * 2) {
            FilledTonalButton(
                onClick = onMerge,
                enabled = !state.isResolving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.MergeType, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Merge Both")
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                enabled = !state.isResolving && state.hasMoreConflicts,
                modifier = Modifier.weight(1f)
            ) {
                Text("Skip")
                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
            }

            if (state.currentConflictIndex > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = !state.isResolving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                    Text("Previous")
                }
            }
        }

        if (state.isResolving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun VersionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    data: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            data.forEach { (key, value) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "$key:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionView(
    resolvedCount: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "All Conflicts Resolved!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Successfully resolved $resolvedCount ${if (resolvedCount == 1) "conflict" else "conflicts"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = onClose) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun EmptyConflictsView(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "No Conflicts",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "There are no sync conflicts to resolve",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = onClose) {
                Text("Close")
            }
        }
    }
}

private fun formatEntityType(type: EntityType): String {
    return when (type) {
        EntityType.LOCATION -> "Location"
        EntityType.PLACE_VISIT -> "Place Visit"
        EntityType.TRIP -> "Trip"
        EntityType.PHOTO -> "Photo"
        EntityType.SETTINGS -> "Settings"
    }
}

private fun formatConflictType(type: ConflictType): String {
    return when (type) {
        ConflictType.CONCURRENT_MODIFICATION -> "Modified on both devices"
        ConflictType.DELETION_CONFLICT -> "Deleted on one device, modified on another"
        ConflictType.VERSION_MISMATCH -> "Version mismatch detected"
    }
}
