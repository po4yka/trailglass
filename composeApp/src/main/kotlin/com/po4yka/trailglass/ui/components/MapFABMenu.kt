package com.po4yka.trailglass.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.MotionConfig
import com.po4yka.trailglass.ui.theme.extended

/**
 * Expandable FAB menu for map actions with Material 3 Expressive animations.
 *
 * Provides a primary FAB that expands to show multiple action items:
 * - Toggle follow mode (GPS tracking)
 * - Fit to data (zoom to show all markers/routes)
 * - Refresh map data
 *
 * Features:
 * - Spring-based animations for smooth, expressive transitions
 * - Scrim overlay when expanded for focus
 * - Auto-collapse after selecting an action
 */
@Composable
fun MapFABMenu(
    isFollowModeEnabled: Boolean,
    onToggleFollowMode: () -> Unit,
    onFitToData: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = MotionConfig.expressiveSpring(),
        label = "fab_rotation"
    )

    Box(modifier = modifier) {
        // Menu items column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Menu item 1: Toggle Follow Mode
            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn(animationSpec = MotionConfig.expressiveSpring()) + fadeIn(),
                exit = scaleOut(animationSpec = MotionConfig.expressiveSpring()) + fadeOut()
            ) {
                MapFABMenuItem(
                    icon = if (isFollowModeEnabled) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                    label = if (isFollowModeEnabled) "Stop Following" else "Follow Location",
                    onClick = {
                        onToggleFollowMode()
                        expanded = false
                    }
                )
            }

            // Menu item 2: Fit to Data
            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn(animationSpec = MotionConfig.expressiveSpring()) + fadeIn(),
                exit = scaleOut(animationSpec = MotionConfig.expressiveSpring()) + fadeOut()
            ) {
                MapFABMenuItem(
                    icon = Icons.Default.MyLocation,
                    label = "Fit to Data",
                    onClick = {
                        onFitToData()
                        expanded = false
                    }
                )
            }

            // Menu item 3: Refresh
            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn(animationSpec = MotionConfig.expressiveSpring()) + fadeIn(),
                exit = scaleOut(animationSpec = MotionConfig.expressiveSpring()) + fadeOut()
            ) {
                MapFABMenuItem(
                    icon = Icons.Default.Refresh,
                    label = "Refresh",
                    onClick = {
                        onRefresh()
                        expanded = false
                    }
                )
            }

            // Main FAB
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = MaterialTheme.colorScheme.extended.activeRoute
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (expanded) "Close menu" else "Open menu",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        // Scrim overlay when expanded - positioned absolutely to fill parent
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                    .clickable { expanded = false }
            )
        }
    }
}

/**
 * Individual menu item for the map FAB menu.
 *
 * Displays a labeled small FAB with spring animations.
 */
@Composable
private fun MapFABMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label
        Surface(
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        // Icon button
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        }
    }
}
