package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.route.RouteReplayController

/**
 * Component for the Route Replay screen.
 */
interface RouteReplayComponent {
    val tripId: String
    val routeReplayController: RouteReplayController
    val onBack: () -> Unit
}

/**
 * Default implementation of RouteReplayComponent.
 */
class DefaultRouteReplayComponent(
    componentContext: ComponentContext,
    override val tripId: String,
    override val routeReplayController: RouteReplayController,
    override val onBack: () -> Unit
) : RouteReplayComponent,
    ComponentContext by componentContext
