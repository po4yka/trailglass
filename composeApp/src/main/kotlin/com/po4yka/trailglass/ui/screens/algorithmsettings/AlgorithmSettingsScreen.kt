package com.po4yka.trailglass.ui.screens.algorithmsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.algorithm.BearingAlgorithmType
import com.po4yka.trailglass.domain.algorithm.DistanceAlgorithmType
import com.po4yka.trailglass.domain.algorithm.InterpolationAlgorithmType
import com.po4yka.trailglass.feature.settings.SettingsController

/**
 * Screen for configuring geographic calculation algorithms. Allows users to choose between different distance, bearing,
 * and interpolation algorithms based on their accuracy and performance needs.
 *
 * @param controller The settings controller for managing algorithm preferences
 * @param onBack Callback invoked when the back button is pressed
 * @param modifier Optional modifier for the screen
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
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introduction
            item {
                IntroductionSection()
            }

            // Distance Algorithm Section
            item {
                SectionHeader("Distance Calculation")
            }

            item {
                DistanceAlgorithmCard(
                    settings = settings,
                    onClick = { showDistanceDialog = true }
                )
            }

            // Bearing Algorithm Section
            item {
                SectionHeader("Direction Calculation")
            }

            item {
                BearingAlgorithmCard(
                    settings = settings,
                    onClick = { showBearingDialog = true }
                )
            }

            // Interpolation Algorithm Section
            item {
                SectionHeader("Path Interpolation")
            }

            item {
                InterpolationAlgorithmCard(
                    settings = settings,
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
            getOptionLabel = {
                it.name.lowercase().replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase()
                    } else {
                        it
                            .toString()
                    }
                }
            },
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
            getOptionLabel = {
                it.name
                    .replace(
                        '_',
                        ' '
                    ).lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            },
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
            getOptionLabel = {
                it.name.lowercase().replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase()
                    } else {
                        it
                            .toString()
                    }
                }
            },
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
