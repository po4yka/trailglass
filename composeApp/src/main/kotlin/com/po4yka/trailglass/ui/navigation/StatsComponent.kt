package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.po4yka.trailglass.feature.stats.EnhancedStatsController

/** Component for the Stats screen. */
interface StatsComponent {
    val enhancedStatsController: EnhancedStatsController
}

/** Default implementation of StatsComponent. */
class DefaultStatsComponent(
    componentContext: ComponentContext,
    enhancedStatsController: EnhancedStatsController
) : StatsComponent,
    ComponentContext by componentContext {
    private val retained =
        instanceKeeper.getOrCreate {
            RetainedInstance(enhancedStatsController)
        }

    override val enhancedStatsController: EnhancedStatsController
        get() = retained.controller

    private class RetainedInstance(
        val controller: EnhancedStatsController
    ) : InstanceKeeper.Instance {
        override fun onDestroy() {
            controller.cleanup()
        }
    }
}
