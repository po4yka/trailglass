package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.stats.StatsController

/**
 * Component for the Stats screen.
 */
interface StatsComponent {
    val statsController: StatsController
}

/**
 * Default implementation of StatsComponent.
 */
class DefaultStatsComponent(
    componentContext: ComponentContext,
    override val statsController: StatsController
) : StatsComponent, ComponentContext by componentContext
