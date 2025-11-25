package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.po4yka.trailglass.feature.stats.EnhancedStatsController

/** Component for the Stats screen. */
interface StatsComponent {
    val enhancedStatsController: EnhancedStatsController
}

/** Default implementation of StatsComponent. */
class DefaultStatsComponent(
    componentContext: ComponentContext,
    override val enhancedStatsController: EnhancedStatsController
) : StatsComponent,
    ComponentContext by componentContext {
    init {
        lifecycle.doOnDestroy {
            enhancedStatsController.cleanup()
        }
    }
}
