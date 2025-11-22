package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.ui.components.*

/**
 * Showcase screen demonstrating Material 3 Expressive components:
 * - TransportModeSelector (ButtonGroup)
 * - SyncLoadingIndicator (WavyLoadingIndicator, ContainedLoadingIndicator)
 * - SplitButton (Primary/Secondary actions with spring animations)
 * - SplitButtonWithMenu (Dropdown menu variant)
 *
 * This screen is for development and demonstration purposes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentShowcaseScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTransportMode by remember { mutableStateOf<TransportType?>(null) }
    var showLoadingIndicator by remember { mutableStateOf(false) }
    var showContainedLoading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material 3 Expressive Components") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Transport Mode Selector Section
            item {
                ComponentSection(
                    title = "Transport Mode Selector",
                    description = "Material 3 Expressive ButtonGroup for selecting transport mode with spring animations"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Selected: ${selectedTransportMode?.name ?: "None"}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        TransportModeSelector(
                            selectedMode = selectedTransportMode,
                            onModeSelected = { selectedTransportMode = it },
                            showLabels = true
                        )

                        HorizontalDivider()

                        Text(
                            text = "Compact Version (Icon-only):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        CompactTransportModeSelector(
                            selectedMode = selectedTransportMode,
                            onModeSelected = { selectedTransportMode = it }
                        )
                    }
                }
            }

            // Wavy Loading Indicator Section
            item {
                ComponentSection(
                    title = "Wavy Loading Indicator",
                    description = "Organic, fluid loading animation inspired by water movements"
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            WavyLoadingIndicator(
                                size = 32.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            WavyLoadingIndicator(
                                size = 48.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            WavyLoadingIndicator(
                                size = 64.dp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Button(
                            onClick = { showLoadingIndicator = !showLoadingIndicator }
                        ) {
                            Text(if (showLoadingIndicator) "Hide" else "Show")
                        }

                        AnimatedLoadingIndicator(
                            visible = showLoadingIndicator,
                            size = 48.dp
                        )
                    }
                }
            }

            // Wavy Linear Progress Indicator Section
            item {
                ComponentSection(
                    title = "Wavy Linear Progress",
                    description = "Animated linear progress for uploads and processing"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        WavyLinearProgressIndicator(
                            progress = uploadProgress,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "${(uploadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { uploadProgress = (uploadProgress - 0.1f).coerceAtLeast(0f) }
                            ) {
                                Text("-")
                            }
                            Button(
                                onClick = { uploadProgress = (uploadProgress + 0.1f).coerceAtMost(1f) }
                            ) {
                                Text("+")
                            }
                            Button(
                                onClick = { uploadProgress = 0f }
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            // Contained Loading Indicator Section
            item {
                ComponentSection(
                    title = "Contained Loading Indicator",
                    description = "Loading indicator with background card for sync operations"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { showContainedLoading = !showContainedLoading }
                        ) {
                            Text(if (showContainedLoading) "Hide" else "Show")
                        }

                        ContainedLoadingIndicator(
                            visible = showContainedLoading,
                            message = "Syncing data...",
                            progress = null
                        )

                        ContainedLoadingIndicator(
                            visible = showContainedLoading,
                            message = "Uploading photos...",
                            progress = uploadProgress
                        )
                    }
                }
            }

            // Sync Operation Indicator Section
            item {
                ComponentSection(
                    title = "Sync Operation Indicator",
                    description = "Combined icon and wavy animation for sync feedback"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { isProcessing = !isProcessing }
                        ) {
                            Text(if (isProcessing) "Stop" else "Start")
                        }

                        SyncOperationIndicator(
                            isActive = isProcessing,
                            message = "Syncing with cloud..."
                        )

                        RouteProcessingIndicator(
                            isProcessing = isProcessing
                        )
                    }
                }
            }

            // Photo Upload Indicator Section
            item {
                ComponentSection(
                    title = "Photo Upload Indicator",
                    description = "Specialized indicator for photo upload progress"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PhotoUploadIndicator(
                            isUploading = showContainedLoading,
                            currentFile = (uploadProgress * 10).toInt().coerceAtLeast(1),
                            totalFiles = 10
                        )
                    }
                }
            }

            // Split Button Section
            item {
                ComponentSection(
                    title = "Split Button",
                    description = "Material 3 Expressive split button with primary and secondary actions"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Filled Variant (Default):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButton(
                            primaryText = "View on Map",
                            primaryIcon = null,
                            onPrimaryClick = { /* Open map */ },
                            onSecondaryClick = { /* Show options */ }
                        )

                        HorizontalDivider()

                        Text(
                            text = "Outlined Variant:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButton(
                            primaryText = "Share Route",
                            primaryIcon = null,
                            onPrimaryClick = { /* Share */ },
                            onSecondaryClick = { /* Options */ },
                            variant = SplitButtonVariant.Outlined
                        )

                        HorizontalDivider()

                        Text(
                            text = "Text Variant:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButton(
                            primaryText = "Export Data",
                            primaryIcon = null,
                            onPrimaryClick = { /* Export */ },
                            onSecondaryClick = { /* Options */ },
                            variant = SplitButtonVariant.Text
                        )

                        HorizontalDivider()

                        Text(
                            text = "Disabled State:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButton(
                            primaryText = "Unavailable Action",
                            primaryIcon = null,
                            onPrimaryClick = { },
                            onSecondaryClick = { },
                            enabled = false
                        )
                    }
                }
            }

            // Split Button with Menu Section
            item {
                ComponentSection(
                    title = "Split Button with Dropdown Menu",
                    description = "Split button with dropdown menu for multiple secondary actions"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Trip Detail Actions:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButtonWithMenu(
                            primaryText = "View Details",
                            onPrimaryClick = { /* Open trip detail */ },
                            menuItems =
                                listOf(
                                    SplitButtonMenuItem("Edit") { /* Edit trip */ },
                                    SplitButtonMenuItem("Delete") { /* Delete trip */ },
                                    SplitButtonMenuItem("Duplicate") { /* Duplicate trip */ },
                                    SplitButtonMenuItem("Export") { /* Export trip */ }
                                )
                        )

                        HorizontalDivider()

                        Text(
                            text = "Export Options:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButtonWithMenu(
                            primaryText = "Export All Data",
                            onPrimaryClick = { /* Export with defaults */ },
                            menuItems =
                                listOf(
                                    SplitButtonMenuItem("Export GPX only") { /* GPX */ },
                                    SplitButtonMenuItem("Export JSON") { /* JSON */ },
                                    SplitButtonMenuItem("Export Photos") { /* Photos */ },
                                    SplitButtonMenuItem("Custom...") { /* Custom */ }
                                ),
                            variant = SplitButtonVariant.Outlined
                        )

                        HorizontalDivider()

                        Text(
                            text = "Photo Actions:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SplitButtonWithMenu(
                            primaryText = "Share Photo",
                            onPrimaryClick = { /* Share immediately */ },
                            menuItems =
                                listOf(
                                    SplitButtonMenuItem("Share as...") { /* Share options */ },
                                    SplitButtonMenuItem("Copy") { /* Copy to clipboard */ },
                                    SplitButtonMenuItem("Save to Device") { /* Save */ }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            content()
        }
    }
}
