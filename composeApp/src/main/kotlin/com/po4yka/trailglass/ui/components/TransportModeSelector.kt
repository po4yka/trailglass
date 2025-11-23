package com.po4yka.trailglass.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TransportType

/**
 * Material 3 Expressive-style button group for selecting transport mode. Implements single-selection toggle behavior
 * with spring animations.
 *
 * Uses Silent Waters color palette for consistent visual identity.
 *
 * @param selectedMode Currently selected transport mode (null if none selected)
 * @param onModeSelected Callback when a mode is selected
 * @param modifier Modifier for the component
 * @param modes List of transport modes to display (defaults to common modes)
 * @param showLabels Whether to show text labels below icons
 */
@Composable
fun TransportModeSelector(
    selectedMode: TransportType?,
    onModeSelected: (TransportType) -> Unit,
    modifier: Modifier = Modifier,
    modes: List<TransportType> =
        listOf(
            TransportType.WALK,
            TransportType.BIKE,
            TransportType.CAR,
            TransportType.TRAIN,
            TransportType.BOAT,
            TransportType.PLANE
        ),
    showLabels: Boolean = true
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { mode ->
            TransportModeButton(
                mode = mode,
                isSelected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                showLabel = showLabels,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual transport mode button with Material 3 Expressive styling. Features spring animations on selection and
 * Silent Waters colors.
 */
@Composable
private fun TransportModeButton(
    mode: TransportType,
    isSelected: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
        label = "container_color"
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
        label = "content_color"
    )

    val borderColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
        label = "border_color"
    )

    Surface(
        onClick = onClick,
        modifier =
            modifier
                .semantics { role = Role.Button }
                .heightIn(min = if (showLabel) 72.dp else 48.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        border =
            BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getTransportIcon(mode),
                contentDescription = getTransportLabel(mode),
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            if (showLabel) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getTransportLabel(mode),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Compact transport mode selector with icon-only buttons. Useful for toolbars and limited space scenarios. */
@Composable
fun CompactTransportModeSelector(
    selectedMode: TransportType?,
    onModeSelected: (TransportType) -> Unit,
    modifier: Modifier = Modifier,
    modes: List<TransportType> =
        listOf(
            TransportType.WALK,
            TransportType.BIKE,
            TransportType.CAR,
            TransportType.TRAIN
        )
) {
    TransportModeSelector(
        selectedMode = selectedMode,
        onModeSelected = onModeSelected,
        modifier = modifier,
        modes = modes,
        showLabels = false
    )
}

/** Get Material icon for transport type. */
private fun getTransportIcon(type: TransportType): ImageVector =
    when (type) {
        TransportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
        TransportType.BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
        TransportType.CAR -> Icons.Default.DirectionsCar
        TransportType.TRAIN -> Icons.Default.Train
        TransportType.PLANE -> Icons.Default.Flight
        TransportType.BOAT -> Icons.Default.DirectionsBoat
        TransportType.UNKNOWN -> Icons.Default.DirectionsCar
    }

/** Get display label for transport type. */
private fun getTransportLabel(type: TransportType): String =
    when (type) {
        TransportType.WALK -> "Walk"
        TransportType.BIKE -> "Bike"
        TransportType.CAR -> "Car"
        TransportType.TRAIN -> "Train"
        TransportType.PLANE -> "Plane"
        TransportType.BOAT -> "Boat"
        TransportType.UNKNOWN -> "Other"
    }
