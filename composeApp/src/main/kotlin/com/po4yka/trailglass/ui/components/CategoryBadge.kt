package com.po4yka.trailglass.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.CategoryShapes
import com.po4yka.trailglass.ui.theme.animateShapeMorph

/**
 * Category badge with shape morphing based on category type. Follows Material 3 Expressive design with semantic shape
 * associations.
 *
 * Shape mappings:
 * - Water activities: Wave shape
 * - Transport: Hexagon
 * - Food/Dining: Petal/organic shape
 * - Work: Rounded square
 * - Default: Circle
 *
 * @param category Category name or type
 * @param onClick Click handler
 * @param modifier Modifier for the badge
 * @param enabled Whether the badge is clickable
 */
@Composable
fun CategoryBadge(
    category: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Determine shape based on category
    val badgeShape by animateShapeMorph(
        targetShape = CategoryShapes.forCategory(category),
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
    )

    // Get category icon
    val icon = getCategoryIcon(category)

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon =
            if (icon != null) {
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                null
            },
        shape = badgeShape,
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * Compact category badge for dense layouts. Shows only icon with morphing shape.
 *
 * @param category Category name or type
 * @param modifier Modifier for the badge
 */
@Composable
fun CompactCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val badgeShape by animateShapeMorph(
        targetShape = CategoryShapes.forCategory(category),
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
    )

    val icon = getCategoryIcon(category)

    Surface(
        shape = badgeShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier.size(24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = category,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Category badge with morphing animation between different categories. Useful for interactive category selection or
 * filtering.
 *
 * @param selectedCategory Currently selected category
 * @param categories Available categories
 * @param onCategorySelected Callback when category is selected
 * @param modifier Modifier for the badge group
 */
@Composable
fun CategoryBadgeGroup(
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    categories.forEach { category ->
        val isSelected = category == selectedCategory
        CategoryBadge(
            category = category,
            onClick = { onCategorySelected(category) },
            modifier = modifier,
            enabled = !isSelected
        )
    }
}

/** Get icon for category type. Maps common category names to appropriate Material Icons. */
private fun getCategoryIcon(category: String): ImageVector? =
    when (category.lowercase()) {
        "water", "swimming", "beach", "ocean" -> Icons.Default.WaterDrop
        "transport", "car", "bus", "train" -> Icons.Default.DirectionsCar
        "flight", "plane", "airport" -> Icons.Default.Flight
        "bike", "cycling", "bicycle" -> Icons.AutoMirrored.Filled.DirectionsBike
        "food", "restaurant", "dining" -> Icons.Default.Restaurant
        "cafe", "coffee" -> Icons.Default.LocalCafe
        "work", "office", "meeting" -> Icons.Default.Work
        "home" -> Icons.Default.Home
        "hotel", "accommodation" -> Icons.Default.Hotel
        "shopping", "shop", "mall" -> Icons.Default.ShoppingBag
        "nature", "park", "hiking" -> Icons.Default.Park
        "entertainment", "fun", "leisure" -> Icons.Default.TheaterComedy
        "fitness", "gym", "exercise" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Place
    }

/** Category color mapping for additional visual differentiation. Returns appropriate color scheme based on category. */
@Composable
fun getCategoryColors(category: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (category.lowercase()) {
        "water", "swimming", "beach", "ocean" ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer

        "transport", "car", "bus", "train", "flight", "bike" ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        "food", "restaurant", "dining", "cafe" ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer

        "work", "office", "meeting" ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant

        else ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
