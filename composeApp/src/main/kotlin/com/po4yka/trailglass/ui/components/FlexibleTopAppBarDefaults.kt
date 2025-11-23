package com.po4yka.trailglass.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Helper object providing default values and utilities for FlexibleTopAppBar components.
 */
object FlexibleTopAppBarDefaults {
    /** Default expanded height for LargeFlexibleTopAppBar. */
    val LargeExpandedHeight = 180.dp

    /** Default expanded height for MediumFlexibleTopAppBar. */
    val MediumExpandedHeight = 128.dp

    /**
     * Creates a TopAppBarScrollBehavior for exit-until-collapsed behavior. This is the recommended scroll behavior for
     * flexible top app bars.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun exitUntilCollapsedScrollBehavior(
        state: TopAppBarState = rememberTopAppBarState(),
        canScroll: () -> Boolean = { true }
    ): TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state, canScroll)

    /**
     * Creates colors for a top app bar with hero background. Makes the container transparent to show the background
     * content.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun heroBackgroundColors(
        containerColor: Color = Color.Transparent,
        scrolledContainerColor: Color = MaterialTheme.colorScheme.surface,
        navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
        titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor: Color = MaterialTheme.colorScheme.onSurface
    ): TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = scrolledContainerColor,
            navigationIconContentColor = navigationIconContentColor,
            titleContentColor = titleContentColor,
            actionIconContentColor = actionIconContentColor
        )

    /** Creates colors for a top app bar with Silent Waters theme. */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun silentWatersColors(
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        scrolledContainerColor: Color = MaterialTheme.colorScheme.surface,
        navigationIconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        titleContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        actionIconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
    ): TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = scrolledContainerColor,
            navigationIconContentColor = navigationIconContentColor,
            titleContentColor = titleContentColor,
            actionIconContentColor = actionIconContentColor
        )

    /** Creates standard top app bar colors. Helper function to provide a cleaner API. */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topAppBarColors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        scrolledContainerColor: Color = MaterialTheme.colorScheme.surface,
        navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
        titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor: Color = MaterialTheme.colorScheme.onSurface
    ): TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = scrolledContainerColor,
            navigationIconContentColor = navigationIconContentColor,
            titleContentColor = titleContentColor,
            actionIconContentColor = actionIconContentColor
        )
}
