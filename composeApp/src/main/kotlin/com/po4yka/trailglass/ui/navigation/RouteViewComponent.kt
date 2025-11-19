package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.route.RouteViewController

/**
 * Component for the Route View screen.
 */
interface RouteViewComponent {
    val tripId: String
    val routeViewController: RouteViewController
    val onNavigateToReplay: (String) -> Unit
    val onNavigateToStatistics: (String) -> Unit
    val onBack: () -> Unit
}

/**
 * Default implementation of RouteViewComponent.
 */
class DefaultRouteViewComponent(
    componentContext: ComponentContext,
    override val tripId: String,
    override val routeViewController: RouteViewController,
    override val onNavigateToReplay: (String) -> Unit,
    override val onNavigateToStatistics: (String) -> Unit,
    override val onBack: () -> Unit
) : RouteViewComponent, ComponentContext by componentContext
