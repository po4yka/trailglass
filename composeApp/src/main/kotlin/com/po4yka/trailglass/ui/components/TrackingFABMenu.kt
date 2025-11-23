package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.MorphableShapes
import com.po4yka.trailglass.ui.theme.animateShapeMorph
import com.po4yka.trailglass.ui.theme.expressiveShapeMorphSpring
import com.po4yka.trailglass.ui.theme.extended

/**
 * Expandable Floating Action Button Menu for tracking actions.
 *
 * Provides a primary FAB that expands to show multiple action items:
 * - Start/Stop Tracking
 * - Add Photo to current location
 * - Add Note to current location
 * - Manual location check-in
 */
@Composable
fun TrackingFABMenu(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    onAddPhoto: () -> Unit,
    onAddNote: () -> Unit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Scrim overlay when expanded
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        // Menu items
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Menu items (shown when expanded)
            AnimatedVisibility(
                visible = expanded,
                enter =
                    expandVertically(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                        expandFrom = Alignment.Bottom
                    ) + fadeIn(),
                exit =
                    shrinkVertically(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                        shrinkTowards = Alignment.Bottom
                    ) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Manual Check-in
                    FABMenuItem(
                        icon = Icons.Default.LocationOn,
                        label = "Check In",
                        onClick = {
                            onCheckIn()
                            expanded = false
                        }
                    )

                    // Add Note
                    FABMenuItem(
                        icon = Icons.Default.EditNote,
                        label = "Add Note",
                        onClick = {
                            onAddNote()
                            expanded = false
                        }
                    )

                    // Add Photo
                    FABMenuItem(
                        icon = Icons.Default.AddAPhoto,
                        label = "Add Photo",
                        onClick = {
                            onAddPhoto()
                            expanded = false
                        }
                    )

                    // Start/Stop Tracking
                    FABMenuItem(
                        icon = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        label = if (isTracking) "Stop Tracking" else "Start Tracking",
                        onClick = {
                            onToggleTracking()
                            expanded = false
                        },
                        containerColor =
                            if (isTracking) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.extended.activeRoute
                            }
                    )
                }
            }

            // Main FAB with shape morphing
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 45f else 0f,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                label = "fab_rotation"
            )

            // Shape morphing: Circle when idle/expanded, Triangle when tracking
            val fabShape by animateShapeMorph(
                targetShape = if (isTracking && !expanded) MorphableShapes.Triangle else MorphableShapes.Circle,
                animationSpec = expressiveShapeMorphSpring()
            )

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor =
                    if (isTracking && !expanded) {
                        MaterialTheme.colorScheme.extended.activeRoute
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = fabShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (expanded) "Close menu" else "Open menu",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

/** Individual menu item for the FAB menu. */
@Composable
private fun FABMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
        label = "fab_menu_item_scale"
    )

    Row(
        modifier = modifier.scale(scale),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Small FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        }
    }
}
