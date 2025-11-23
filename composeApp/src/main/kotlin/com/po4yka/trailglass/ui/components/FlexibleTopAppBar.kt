package com.po4yka.trailglass.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.MotionConfig
import com.po4yka.trailglass.ui.theme.emphasized
import kotlin.math.max
import kotlin.math.min

/**
 * Large Flexible Top App Bar for Trailglass detail screens.
 *
 * This component follows Material 3 Expressive guidelines and provides:
 * - Collapsing behavior from large to medium size on scroll
 * - Hero image/background with parallax effect
 * - Title with emphasized typography
 * - Subtitle/metadata that fades out on collapse
 * - Action buttons (share, edit, delete, etc.)
 * - Spring-based scroll animations using MotionConfig.expressiveSpring
 * - Silent Waters color scheme integration
 *
 * Usage example:
 * ```kotlin
 * val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
 * Scaffold(
 *     modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
 *     topBar = {
 *         LargeFlexibleTopAppBar(
 *             title = { Text("Trip to Paris") },
 *             subtitle = { Text("5 days • 234 km • 12 places") },
 *             navigationIcon = {
 *                 IconButton(onClick = onBack) {
 *                     Icon(Icons.Default.ArrowBack, contentDescription = "Back")
 *                 }
 *             },
 *             actions = {
 *                 IconButton(onClick = onShare) {
 *                     Icon(Icons.Default.Share, contentDescription = "Share")
 *                 }
 *             },
 *             scrollBehavior = scrollBehavior,
 *             backgroundContent = {
 *                 // Optional hero image or gradient
 *                 Box(
 *                     modifier = Modifier
 *                         .fillMaxSize()
 *                         .background(
 *                             Brush.verticalGradient(
 *                                 colors = listOf(CoastalPath, BlueSlate)
 *                             )
 *                         )
 *                 )
 *             }
 *         )
 *     }
 * ) { paddingValues ->
 *     LazyColumn(modifier = Modifier.padding(paddingValues)) {
 *         // Content
 *     }
 * }
 * ```
 *
 * @param title The main title text, displayed with emphasized typography
 * @param modifier Modifier for the top app bar
 * @param subtitle Optional subtitle/metadata that fades out during collapse
 * @param navigationIcon Navigation icon (typically back button)
 * @param actions Action buttons displayed at the end
 * @param titleHorizontalAlignment Horizontal alignment of title and subtitle
 * @param collapsedHeight Height when fully collapsed (default: TopAppBarDefaults.LargeAppBarCollapsedHeight)
 * @param expandedHeight Height when fully expanded (default: 180.dp)
 * @param windowInsets Window insets for the app bar
 * @param colors Color configuration for the app bar
 * @param scrollBehavior Scroll behavior for collapse/expand animations
 * @param backgroundContent Optional composable for hero background (image, gradient, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LargeFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    collapsedHeight: Dp = TopAppBarDefaults.LargeAppBarCollapsedHeight,
    expandedHeight: Dp = 180.dp,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backgroundContent: @Composable (BoxScope.() -> Unit)? = null
) {
    // Calculate collapse progress (0 = expanded, 1 = collapsed)
    val collapseProgress = scrollBehavior?.state?.collapsedFraction ?: 0f

    // Animate collapse progress with expressive spring
    val animatedProgress by animateFloatAsState(
        targetValue = collapseProgress,
        animationSpec = MotionConfig.expressiveSpring(),
        label = "topAppBarCollapse"
    )

    // Calculate parallax offset for background (moves slower than content)
    val parallaxOffset =
        if (scrollBehavior != null) {
            scrollBehavior.state.heightOffset * 0.5f
        } else {
            0f
        }

    // Calculate subtitle alpha (fades out as app bar collapses)
    val subtitleAlpha = 1f - min(1f, animatedProgress * 2f)

    // Calculate current height based on scroll
    val currentHeight =
        if (scrollBehavior != null) {
            expandedHeight - collapsedHeight
            val offset = scrollBehavior.state.heightOffset
            max(collapsedHeight.value, expandedHeight.value + offset)
        } else {
            expandedHeight.value
        }

    androidx.compose.material3.Surface(
        modifier = modifier.height(currentHeight.dp),
        color = Color.Transparent
    ) {
        Box {
            // Background layer with parallax effect
            if (backgroundContent != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = parallaxOffset
                                alpha = 1f - (animatedProgress * 0.3f) // Slight fade on collapse
                            }
                ) {
                    backgroundContent()
                }
            } else {
                // Default gradient background using Silent Waters colors
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.surface
                                        )
                                )
                            )
                )
            }

            // Scrim overlay that increases opacity as app bar collapses
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = animatedProgress * 0.9f
                            )
                        )
            )

            // Content layer
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(windowInsets)
            ) {
                // Top controls (navigation + actions)
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(collapsedHeight)
                            .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigation icon
                    Box(modifier = Modifier.size(48.dp)) {
                        navigationIcon()
                    }

                    // Title in collapsed state
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .alpha(animatedProgress)
                                .padding(horizontal = 8.dp)
                    ) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.titleLarge
                        ) {
                            title()
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier.padding(end = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        actions()
                    }
                }

                // Expanded content (title + subtitle)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    horizontalAlignment =
                        when (titleHorizontalAlignment) {
                            Alignment.Start -> Alignment.Start
                            Alignment.CenterHorizontally -> Alignment.CenterHorizontally
                            Alignment.End -> Alignment.End
                            else -> Alignment.Start
                        }
                ) {
                    // Large title (visible when expanded)
                    Box(
                        modifier =
                            Modifier
                                .alpha(1f - animatedProgress)
                                .graphicsLayer {
                                    // Slight scale animation
                                    val scale = 1f - (animatedProgress * 0.1f)
                                    scaleX = scale
                                    scaleY = scale
                                }
                    ) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.emphasized.headlineLargeEmphasized
                        ) {
                            title()
                        }
                    }

                    // Subtitle (fades out on collapse)
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.alpha(subtitleAlpha)
                        ) {
                            ProvideTextStyle(
                                value =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            ) {
                                subtitle()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Medium Flexible Top App Bar for Trailglass detail screens.
 *
 * A more compact variant than LargeFlexibleTopAppBar, suitable for place details and secondary screens. Collapses from
 * medium to small size.
 *
 * Features:
 * - Collapses from medium to compact size on scroll
 * - Icon + title layout
 * - Simpler design than large variant
 * - Spring-based animations
 * - Silent Waters colors
 *
 * Usage example:
 * ```kotlin
 * val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
 * MediumFlexibleTopAppBar(
 *     title = { Text("Favorite Cafe") },
 *     subtitle = { Text("23 visits • Last visit yesterday") },
 *     navigationIcon = {
 *         IconButton(onClick = onBack) {
 *             Icon(Icons.Default.ArrowBack, contentDescription = "Back")
 *         }
 *     },
 *     leadingIcon = {
 *         Icon(
 *             Icons.Default.Restaurant,
 *             contentDescription = null,
 *             modifier = Modifier.size(40.dp)
 *         )
 *     },
 *     scrollBehavior = scrollBehavior
 * )
 * ```
 *
 * @param title The main title text
 * @param modifier Modifier for the top app bar
 * @param subtitle Optional subtitle/metadata
 * @param navigationIcon Navigation icon (back button)
 * @param actions Action buttons
 * @param leadingIcon Optional icon displayed before the title (e.g., place category icon)
 * @param titleHorizontalAlignment Horizontal alignment of title and subtitle
 * @param collapsedHeight Height when collapsed
 * @param expandedHeight Height when expanded (default: 128.dp)
 * @param windowInsets Window insets
 * @param colors Color configuration
 * @param scrollBehavior Scroll behavior for animations
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediumFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,
    titleHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    collapsedHeight: Dp = TopAppBarDefaults.MediumAppBarCollapsedHeight,
    expandedHeight: Dp = 128.dp,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Calculate collapse progress
    val collapseProgress = scrollBehavior?.state?.collapsedFraction ?: 0f

    // Animate with expressive spring
    val animatedProgress by animateFloatAsState(
        targetValue = collapseProgress,
        animationSpec = MotionConfig.expressiveSpring(),
        label = "mediumAppBarCollapse"
    )

    // Subtitle alpha
    val subtitleAlpha = 1f - min(1f, animatedProgress * 2f)

    // Icon alpha (fades out on collapse)
    val iconAlpha = 1f - animatedProgress

    // Calculate current height
    val currentHeight =
        if (scrollBehavior != null) {
            expandedHeight - collapsedHeight
            val offset = scrollBehavior.state.heightOffset
            max(collapsedHeight.value, expandedHeight.value + offset)
        } else {
            expandedHeight.value
        }

    androidx.compose.material3.Surface(
        modifier = modifier.height(currentHeight.dp),
        color = colors.containerColor
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(windowInsets)
        ) {
            // Top controls
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(collapsedHeight)
                        .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation icon
                Box(modifier = Modifier.size(48.dp)) {
                    navigationIcon()
                }

                // Title in collapsed state
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .alpha(animatedProgress)
                            .padding(horizontal = 8.dp)
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.titleLarge
                    ) {
                        title()
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    actions()
                }
            }

            // Expanded content
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading icon (fades out on collapse)
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier.alpha(iconAlpha)
                    ) {
                        leadingIcon()
                    }
                }

                // Title and subtitle column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment =
                        when (titleHorizontalAlignment) {
                            Alignment.Start -> Alignment.Start
                            Alignment.CenterHorizontally -> Alignment.CenterHorizontally
                            Alignment.End -> Alignment.End
                            else -> Alignment.Start
                        }
                ) {
                    // Medium title (visible when expanded)
                    Box(
                        modifier =
                            Modifier
                                .alpha(1f - animatedProgress)
                    ) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.emphasized.headlineMediumEmphasized
                        ) {
                            title()
                        }
                    }

                    // Subtitle
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.alpha(subtitleAlpha)
                        ) {
                            ProvideTextStyle(
                                value =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            ) {
                                subtitle()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Flexible Top App Bar for Trailglass list screens.
 *
 * A simple, non-collapsing app bar for list and overview screens. Maintains consistent height and provides standard
 * navigation + actions layout.
 *
 * Usage example:
 * ```kotlin
 * CompactFlexibleTopAppBar(
 *     title = { Text("My Trips") },
 *     navigationIcon = {
 *         IconButton(onClick = onMenuClick) {
 *             Icon(Icons.Default.Menu, contentDescription = "Menu")
 *         }
 *     },
 *     actions = {
 *         IconButton(onClick = onSearch) {
 *             Icon(Icons.Default.Search, contentDescription = "Search")
 *         }
 *     }
 * )
 * ```
 *
 * @param title The title text
 * @param modifier Modifier for the app bar
 * @param navigationIcon Navigation icon (menu or back button)
 * @param actions Action buttons
 * @param windowInsets Window insets
 * @param colors Color configuration
 * @param scrollBehavior Optional scroll behavior (typically null for compact variant)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            ProvideTextStyle(
                value = MaterialTheme.typography.titleLarge
            ) {
                title()
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

/** Helper object providing default values and utilities for FlexibleTopAppBar components. */
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
