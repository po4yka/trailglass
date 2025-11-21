package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.algorithm.BearingAlgorithmType
import com.po4yka.trailglass.domain.algorithm.DistanceAlgorithmType
import com.po4yka.trailglass.domain.algorithm.InterpolationAlgorithmType
import com.po4yka.trailglass.domain.model.AlgorithmPreferences
import com.po4yka.trailglass.feature.settings.SettingsController

/**
 * Screen for configuring geographic calculation algorithms.
 * Allows users to choose between different distance, bearing, and interpolation algorithms
 * based on their accuracy and performance needs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgorithmSettingsScreen(
    controller: SettingsController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val settings = state.settings

    if (state.isLoading || settings == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var showDistanceDialog by remember { mutableStateOf(false) }
    var showBearingDialog by remember { mutableStateOf(false) }
    var showInterpolationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Algorithm Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introduction
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Geographic Algorithms",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose calculation methods for distance, bearing, and interpolation. " +
                                    "Different algorithms offer trade-offs between accuracy and performance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Distance Algorithm Section
            item {
                Text(
                    text = "Distance Calculation",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                AlgorithmCard(
                    icon = Icons.Default.Straighten,
                    title = "Distance Algorithm",
                    currentValue = settings.algorithmPreferences.distanceAlgorithm.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    description = getDistanceAlgorithmDescription(settings.algorithmPreferences.distanceAlgorithm),
                    onClick = { showDistanceDialog = true }
                )
            }

            // Bearing Algorithm Section
            item {
                Text(
                    text = "Direction Calculation",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                AlgorithmCard(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    title = "Bearing Algorithm",
                    currentValue = settings.algorithmPreferences.bearingAlgorithm.name.replace('_', ' ').lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    description = getBearingAlgorithmDescription(settings.algorithmPreferences.bearingAlgorithm),
                    onClick = { showBearingDialog = true }
                )
            }

            // Interpolation Algorithm Section
            item {
                Text(
                    text = "Path Interpolation",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                AlgorithmCard(
                    icon = Icons.Default.GraphicEq,
                    title = "Interpolation Algorithm",
                    currentValue = settings.algorithmPreferences.interpolationAlgorithm.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    description = getInterpolationAlgorithmDescription(settings.algorithmPreferences.interpolationAlgorithm),
                    onClick = { showInterpolationDialog = true }
                )
            }
        }
    }

    // Distance Algorithm Dialog
    if (showDistanceDialog) {
        AlgorithmSelectionDialog(
            title = "Distance Algorithm",
            options = DistanceAlgorithmType.values().toList(),
            selectedOption = settings.algorithmPreferences.distanceAlgorithm,
            getOptionLabel = { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } },
            getOptionDescription = { getDistanceAlgorithmDescription(it) },
            onDismiss = { showDistanceDialog = false },
            onSelect = { selected ->
                controller.updateAlgorithmPreferences(
                    settings.algorithmPreferences.copy(distanceAlgorithm = selected)
                )
                showDistanceDialog = false
            }
        )
    }

    // Bearing Algorithm Dialog
    if (showBearingDialog) {
        AlgorithmSelectionDialog(
            title = "Bearing Algorithm",
            options = BearingAlgorithmType.values().toList(),
            selectedOption = settings.algorithmPreferences.bearingAlgorithm,
            getOptionLabel = { it.name.replace('_', ' ').lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } },
            getOptionDescription = { getBearingAlgorithmDescription(it) },
            onDismiss = { showBearingDialog = false },
            onSelect = { selected ->
                controller.updateAlgorithmPreferences(
                    settings.algorithmPreferences.copy(bearingAlgorithm = selected)
                )
                showBearingDialog = false
            }
        )
    }

    // Interpolation Algorithm Dialog
    if (showInterpolationDialog) {
        AlgorithmSelectionDialog(
            title = "Interpolation Algorithm",
            options = InterpolationAlgorithmType.values().toList(),
            selectedOption = settings.algorithmPreferences.interpolationAlgorithm,
            getOptionLabel = { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } },
            getOptionDescription = { getInterpolationAlgorithmDescription(it) },
            onDismiss = { showInterpolationDialog = false },
            onSelect = { selected ->
                controller.updateAlgorithmPreferences(
                    settings.algorithmPreferences.copy(interpolationAlgorithm = selected)
                )
                showInterpolationDialog = false
            }
        )
    }
}

@Composable
private fun AlgorithmCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    currentValue: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun <T> AlgorithmSelectionDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    getOptionLabel: (T) -> String,
    getOptionDescription: (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(options.size) { index ->
                    val option = options[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == option)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { onSelect(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getOptionLabel(option),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedOption == option)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = getOptionDescription(option),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedOption == option)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Helper functions to get algorithm descriptions
private fun getDistanceAlgorithmDescription(algorithm: DistanceAlgorithmType): String {
    return when (algorithm) {
        DistanceAlgorithmType.HAVERSINE ->
            "Fast and accurate (~0.5% error). Best for most uses. Assumes spherical Earth."
        DistanceAlgorithmType.VINCENTY ->
            "Most accurate (~0.5mm error) but slower. Use for precise measurements. Accounts for Earth's ellipsoidal shape."
        DistanceAlgorithmType.SIMPLE ->
            "Fastest but only accurate for short distances (<1km). Error increases with distance and latitude."
    }
}

private fun getBearingAlgorithmDescription(algorithm: BearingAlgorithmType): String {
    return when (algorithm) {
        BearingAlgorithmType.INITIAL ->
            "Direction at start point (most common for navigation). Shows compass heading at departure."
        BearingAlgorithmType.FINAL ->
            "Direction at end point. Useful for arrival headings, can differ from initial bearing on long distances."
        BearingAlgorithmType.RHUMB_LINE ->
            "Constant compass bearing throughout journey. Not the shortest path but simpler navigation."
    }
}

private fun getInterpolationAlgorithmDescription(algorithm: InterpolationAlgorithmType): String {
    return when (algorithm) {
        InterpolationAlgorithmType.LINEAR ->
            "Simple straight line, fast but not geographically accurate over long distances. Good for animations."
        InterpolationAlgorithmType.SLERP ->
            "Spherical interpolation, follows great circle (most accurate). Best for geographic paths."
        InterpolationAlgorithmType.CUBIC ->
            "Smooth curved path with easing. Aesthetically pleasing for animated transitions."
    }
}