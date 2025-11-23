package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.ui.theme.MotionConfig
import com.po4yka.trailglass.ui.theme.emphasized
import com.po4yka.trailglass.ui.theme.extended

/**
 * Material 3 Expressive animated marker info card.
 *
 * Features:
 * - Spring-based entry/exit animations
 * - Category-based color badges
 * - Emphasized typography
 * - Interactive buttons with press effects
 */
@Composable
fun MarkerInfoCard(
    marker: MapMarker,
    visible: Boolean,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            slideInVertically(
                animationSpec = MotionConfig.expressiveSpring(),
                initialOffsetY = { it / 2 }
            ) +
                scaleIn(
                    animationSpec = MotionConfig.expressiveSpring(),
                    initialScale = 0.8f
                ) +
                fadeIn(
                    animationSpec = MotionConfig.expressiveSpring()
                ),
        exit =
            slideOutVertically(
                animationSpec = MotionConfig.expressiveSpring(),
                targetOffsetY = { it / 2 }
            ) +
                scaleOut(
                    animationSpec = MotionConfig.expressiveSpring(),
                    targetScale = 0.8f
                ) +
                fadeOut(
                    animationSpec = MotionConfig.expressiveSpring()
                )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with title, category badge, and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Category badge (placeholder - would need actual category from place visit)
                    CategoryBadge(category = PlaceCategory.OTHER)

                    // Title and snippet
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = marker.title ?: "Unknown location",
                            style = MaterialTheme.typography.emphasized.titleLargeEmphasized
                        )

                        marker.snippet?.let { snippetText ->
                            Text(
                                text = snippetText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Close button
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedActionButton(
                        onClick = onViewDetails,
                        icon = Icons.Default.Info,
                        label = "Details",
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedActionButton(
                        onClick = onAddPhoto,
                        icon = Icons.Default.AddPhotoAlternate,
                        label = "Add Photo",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Category badge with color based on place category. */
@Composable
private fun CategoryBadge(
    category: PlaceCategory,
    modifier: Modifier = Modifier
) {
    val (color, icon) = getCategoryColorAndIcon(category)

    Box(
        modifier =
            modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.name,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/** Action button with spring animation on press. */
@Composable
private fun AnimatedActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = MotionConfig.quickSpring(),
        label = "button_press_scale"
    )

    OutlinedButton(
        onClick = {
            isPressed = true
            onClick()
            // Reset after animation
            isPressed = false
        },
        modifier = modifier.scale(scale),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.extended.activeRoute
            ),
        border =
            ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush =
                    androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.extended.activeRoute
                    )
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/** Get category color and icon from Silent Waters palette. */
private fun getCategoryColorAndIcon(category: PlaceCategory): Pair<androidx.compose.ui.graphics.Color, ImageVector> =
    when (category) {
        PlaceCategory.HOME ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF9DB4C0), // coolSteel
                Icons.Default.Home
            )

        PlaceCategory.WORK ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF5C6B73), // blueSlate
                Icons.Default.Work
            )

        PlaceCategory.FOOD ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF8BB5A1), // seaGlass
                Icons.Default.Restaurant
            )

        PlaceCategory.SHOPPING ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFFA89968), // weatheredBrass
                Icons.Default.ShoppingBag
            )

        PlaceCategory.FITNESS ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF8BB5A1), // seaGlass
                Icons.Default.FitnessCenter
            )

        PlaceCategory.ENTERTAINMENT ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFFA8B5C7), // mistyLavender
                Icons.Default.Theaters
            )

        PlaceCategory.TRAVEL ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF7A9CAF), // coastalPath
                Icons.Default.Flight
            )

        PlaceCategory.HEALTHCARE ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFFC2DFE3), // lightBlue
                Icons.Default.LocalHospital
            )

        PlaceCategory.EDUCATION ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFFA8B5C7), // mistyLavender
                Icons.Default.School
            )

        PlaceCategory.RELIGIOUS ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF9DB4C0), // coolSteel
                Icons.Default.Place
            )

        PlaceCategory.SOCIAL ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFFA89968), // weatheredBrass
                Icons.Default.People
            )

        PlaceCategory.OUTDOOR ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF8BB5A1), // seaGlass
                Icons.Default.Park
            )

        PlaceCategory.SERVICE ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF7A9CAF), // coastalPath
                Icons.Default.Store
            )

        PlaceCategory.OTHER ->
            Pair(
                androidx.compose.ui.graphics
                    .Color(0xFF9DB4C0), // coolSteel
                Icons.Default.Place
            )
    }
