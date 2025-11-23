package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.route.TripStatisticsController

/** Component for the Trip Statistics screen. */
interface TripStatisticsComponent {
    val tripId: String
    val tripStatisticsController: TripStatisticsController
    val onBack: () -> Unit
}

/** Default implementation of TripStatisticsComponent. */
class DefaultTripStatisticsComponent(
    componentContext: ComponentContext,
    override val tripId: String,
    override val tripStatisticsController: TripStatisticsController,
    override val onBack: () -> Unit
) : TripStatisticsComponent,
    ComponentContext by componentContext
