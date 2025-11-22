package com.po4yka.trailglass.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.MapVisualizationMode
import com.po4yka.trailglass.ui.theme.MotionConfig
import com.po4yka.trailglass.ui.theme.extended

/**
 * Material 3 Expressive visualization mode selector for map.
 *
 * Custom segmented control with spring animations and Silent Waters colors.
 */
@Composable
fun MapVisualizationSelector(
    currentMode: MapVisualizationMode,
    onModeChange: (MapVisualizationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MapVisualizationMode.entries.forEach { mode ->
                ModeButton(
                    mode = mode,
                    isSelected = mode == currentMode,
                    onClick = { onModeChange(mode) }
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    mode: MapVisualizationMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.extended.activeRoute
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = MotionConfig.expressiveSpring(),
        label = "mode_button_color"
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = MotionConfig.expressiveSpring(),
        label = "mode_button_content_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.95f,
        animationSpec = MotionConfig.expressiveSpring(),
        label = "mode_button_scale"
    )

    Button(
        onClick = onClick,
        modifier =
            Modifier
                .size(56.dp)
                .scale(scale),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Icon(
            imageVector = getModeIcon(mode),
            contentDescription = getModeLabel(mode),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Material 3 Expressive options panel for map controls.
 *
 * Glass-style elevated surface with animated toggles.
 */
@Composable
fun MapOptionsPanel(
    clusteringEnabled: Boolean,
    heatmapEnabled: Boolean,
    onToggleClustering: () -> Unit,
    onToggleHeatmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clustering toggle
            MapOptionToggle(
                icon = Icons.Default.GroupWork,
                label = "Cluster",
                checked = clusteringEnabled,
                onCheckedChange = { onToggleClustering() }
            )

            // Heatmap toggle
            MapOptionToggle(
                icon = Icons.Default.Whatshot,
                label = "Heatmap",
                checked = heatmapEnabled,
                onCheckedChange = { onToggleHeatmap() }
            )
        }
    }
}

@Composable
private fun MapOptionToggle(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint =
                if (checked) {
                    MaterialTheme.colorScheme.extended.activeRoute
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (checked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(24.dp),
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.extended.activeRoute,
                    checkedTrackColor =
                        MaterialTheme.colorScheme.extended.activeRoute
                            .copy(alpha = 0.5f)
                )
        )
    }
}

/**
 * Get icon for visualization mode.
 */
private fun getModeIcon(mode: MapVisualizationMode): ImageVector =
    when (mode) {
        MapVisualizationMode.MARKERS -> Icons.Default.Place
        MapVisualizationMode.CLUSTERS -> Icons.Default.GroupWork
        MapVisualizationMode.HEATMAP -> Icons.Default.Whatshot
        MapVisualizationMode.HYBRID -> Icons.Default.Layers
    }

/**
 * Get label for visualization mode.
 */
private fun getModeLabel(mode: MapVisualizationMode): String =
    when (mode) {
        MapVisualizationMode.MARKERS -> "Markers"
        MapVisualizationMode.CLUSTERS -> "Clusters"
        MapVisualizationMode.HEATMAP -> "Heatmap"
        MapVisualizationMode.HYBRID -> "Hybrid"
    }
