package com.po4yka.trailglass.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.MotionConfig
import com.po4yka.trailglass.ui.theme.scaleSpring

/**
 * Material 3 Expressive SplitButton component with primary and secondary actions.
 *
 * A SplitButton visually combines a primary action button with a secondary action button,
 * separated by a subtle divider. This follows Material 3 Expressive design guidelines
 * with spring-based animations and the Silent Waters color palette.
 *
 * Features:
 * - Spring animation on press (Material 3 Expressive)
 * - Visual divider between sections (1dp with 20% opacity)
 * - Proper accessibility with semantic roles and content descriptions
 * - Minimum touch target of 48dp height
 * - Support for filled, outlined, and text variants
 *
 * @param primaryText Text label for the primary action
 * @param primaryIcon Optional icon for the primary action (shown before text)
 * @param onPrimaryClick Callback when primary action is clicked
 * @param secondaryIcon Icon for the secondary action (defaults to more options icon)
 * @param onSecondaryClick Callback when secondary action is clicked
 * @param modifier Modifier for the entire split button
 * @param enabled Whether the button is enabled (affects both sections)
 * @param colors Button colors following Material 3 color scheme
 * @param variant Visual variant: Filled, Outlined, or Text
 */
@Composable
fun SplitButton(
    primaryText: String,
    primaryIcon: ImageVector?,
    onPrimaryClick: () -> Unit,
    secondaryIcon: ImageVector = Icons.Default.MoreVert,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    variant: SplitButtonVariant = SplitButtonVariant.Filled
) {
    val primaryInteractionSource = remember { MutableInteractionSource() }
    val secondaryInteractionSource = remember { MutableInteractionSource() }

    val isPrimaryPressed by primaryInteractionSource.collectIsPressedAsState()
    val isSecondaryPressed by secondaryInteractionSource.collectIsPressedAsState()

    // Spring animation for press state
    val primaryScale by animateFloatAsState(
        targetValue = if (isPrimaryPressed) 0.95f else 1f,
        animationSpec = MotionConfig.scaleSpring(),
        label = "primaryScale"
    )

    val secondaryScale by animateFloatAsState(
        targetValue = if (isSecondaryPressed) 0.95f else 1f,
        animationSpec = MotionConfig.scaleSpring(),
        label = "secondaryScale"
    )

    Surface(
        modifier =
            modifier
                .height(48.dp)
                .semantics {
                    contentDescription = "Split button: $primaryText with options"
                },
        shape = RoundedCornerShape(24.dp),
        color =
            when (variant) {
                SplitButtonVariant.Filled -> colors.containerColor
                SplitButtonVariant.Outlined, SplitButtonVariant.Text -> Color.Transparent
            },
        border =
            when (variant) {
                SplitButtonVariant.Outlined -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                else -> null
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Primary action button
            when (variant) {
                SplitButtonVariant.Filled -> {
                    Button(
                        onClick = onPrimaryClick,
                        enabled = enabled,
                        colors = colors,
                        interactionSource = primaryInteractionSource,
                        shape =
                            RoundedCornerShape(
                                topStart = 24.dp,
                                bottomStart = 24.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            ),
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .scale(primaryScale)
                    ) {
                        PrimaryButtonContent(primaryIcon, primaryText)
                    }
                }
                SplitButtonVariant.Outlined -> {
                    OutlinedButton(
                        onClick = onPrimaryClick,
                        enabled = enabled,
                        colors = colors,
                        interactionSource = primaryInteractionSource,
                        border = null, // Border handled by parent Surface
                        shape =
                            RoundedCornerShape(
                                topStart = 24.dp,
                                bottomStart = 24.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            ),
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .scale(primaryScale)
                    ) {
                        PrimaryButtonContent(primaryIcon, primaryText)
                    }
                }
                SplitButtonVariant.Text -> {
                    TextButton(
                        onClick = onPrimaryClick,
                        enabled = enabled,
                        colors = colors,
                        interactionSource = primaryInteractionSource,
                        shape =
                            RoundedCornerShape(
                                topStart = 24.dp,
                                bottomStart = 24.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            ),
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .scale(primaryScale)
                    ) {
                        PrimaryButtonContent(primaryIcon, primaryText)
                    }
                }
            }

            // Divider between sections (1dp with 20% opacity)
            HorizontalDivider(
                modifier =
                    Modifier
                        .size(width = 1.dp, height = 24.dp),
                color =
                    when (variant) {
                        SplitButtonVariant.Filled -> colors.contentColor.copy(alpha = 0.2f)
                        SplitButtonVariant.Outlined, SplitButtonVariant.Text ->
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
            )

            // Secondary action button (icon only)
            IconButton(
                onClick = onSecondaryClick,
                enabled = enabled,
                interactionSource = secondaryInteractionSource,
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(40.dp)
                        .scale(secondaryScale)
                        .semantics {
                            contentDescription = "More options for $primaryText"
                        }
            ) {
                Icon(
                    imageVector = secondaryIcon,
                    contentDescription = null,
                    tint =
                        when (variant) {
                            SplitButtonVariant.Filled -> colors.contentColor
                            SplitButtonVariant.Outlined, SplitButtonVariant.Text ->
                                MaterialTheme.colorScheme.onSurface
                        },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Helper composable for primary button content (icon + text).
 */
@Composable
private fun PrimaryButtonContent(
    icon: ImageVector?,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Data class representing a menu item in SplitButtonWithMenu.
 *
 * @param text Display text for the menu item
 * @param icon Optional icon for the menu item
 * @param onClick Callback when this menu item is clicked
 */
data class SplitButtonMenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

/**
 * Material 3 Expressive SplitButton with dropdown menu for secondary actions.
 *
 * This variant shows a primary action button on the left and a dropdown trigger on the right.
 * Clicking the dropdown reveals a menu with multiple options.
 *
 * Features:
 * - Spring animations for press states and menu expansion
 * - Material 3 dropdown menu styling
 * - Proper accessibility with semantic roles
 * - Minimum touch target of 48dp height
 *
 * @param primaryText Text label for the primary action
 * @param onPrimaryClick Callback when primary action is clicked
 * @param menuItems List of menu items to show in the dropdown
 * @param modifier Modifier for the entire split button
 * @param primaryIcon Optional icon for the primary action
 * @param enabled Whether the button is enabled
 * @param colors Button colors following Material 3 color scheme
 * @param variant Visual variant: Filled, Outlined, or Text
 */
@Composable
fun SplitButtonWithMenu(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    menuItems: List<SplitButtonMenuItem>,
    modifier: Modifier = Modifier,
    primaryIcon: ImageVector? = null,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    variant: SplitButtonVariant = SplitButtonVariant.Filled
) {
    var expanded by remember { mutableStateOf(false) }

    SplitButton(
        primaryText = primaryText,
        primaryIcon = primaryIcon,
        onPrimaryClick = onPrimaryClick,
        secondaryIcon = Icons.Default.ArrowDropDown,
        onSecondaryClick = { expanded = true },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        variant = variant
    )

    // Dropdown menu for secondary actions
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.text) },
                onClick = {
                    item.onClick()
                    expanded = false
                },
                leadingIcon =
                    item.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
            )
        }
    }
}

/**
 * Visual variants for SplitButton components.
 */
enum class SplitButtonVariant {
    /**
     * Filled variant with solid background color.
     * Default and most prominent variant.
     */
    Filled,

    /**
     * Outlined variant with transparent background and border.
     * Medium emphasis variant.
     */
    Outlined,

    /**
     * Text-only variant with no background or border.
     * Lowest emphasis variant.
     */
    Text
}

/**
 * Creates ButtonColors for route-related actions using the CoastalPath color.
 *
 * This is a convenience function for creating split buttons that relate to map/route actions,
 * using the MaterialTheme.colorScheme.extended.activeRoute color if available,
 * or falling back to a custom CoastalPath color from the Silent Waters palette.
 */
@Composable
fun routeButtonColors(): ButtonColors {
    // Note: Using primary color as extended color scheme is not yet fully integrated
    // Once MaterialExpressiveTheme is enabled, replace with:
    // MaterialTheme.colorScheme.extended.activeRoute
    return ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

/**
 * Preview helpers and example usage.
 */
@Composable
fun SplitButtonExamples() {
    // Example 1: Basic split button with filled style
    SplitButton(
        primaryText = "View on Map",
        primaryIcon = null,
        onPrimaryClick = { /* Open map */ },
        onSecondaryClick = { /* Show options */ }
    )

    // Example 2: Split button with menu
    SplitButtonWithMenu(
        primaryText = "Export All Data",
        onPrimaryClick = { /* Export with default settings */ },
        menuItems =
            listOf(
                SplitButtonMenuItem("Export GPX only") { /* Export GPX */ },
                SplitButtonMenuItem("Export JSON") { /* Export JSON */ },
                SplitButtonMenuItem("Export Photos") { /* Export photos */ },
                SplitButtonMenuItem("Custom...") { /* Show custom dialog */ }
            )
    )

    // Example 3: Outlined variant with route colors
    SplitButton(
        primaryText = "Share Route",
        primaryIcon = null,
        onPrimaryClick = { /* Share immediately */ },
        onSecondaryClick = { /* Show share options */ },
        colors = routeButtonColors(),
        variant = SplitButtonVariant.Outlined
    )
}
