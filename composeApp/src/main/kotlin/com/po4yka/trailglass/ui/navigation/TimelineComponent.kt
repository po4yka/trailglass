package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.po4yka.trailglass.feature.timeline.EnhancedTimelineController
import com.po4yka.trailglass.feature.tracking.LocationTrackingController

/** Component for the Timeline screen. */
interface TimelineComponent {
    val enhancedTimelineController: EnhancedTimelineController
    val locationTrackingController: LocationTrackingController
}

/** Default implementation of TimelineComponent. */
class DefaultTimelineComponent(
    componentContext: ComponentContext,
    enhancedTimelineController: EnhancedTimelineController,
    override val locationTrackingController: LocationTrackingController
) : TimelineComponent,
    ComponentContext by componentContext {
    private val retained =
        instanceKeeper.getOrCreate {
            RetainedInstance(enhancedTimelineController)
        }

    override val enhancedTimelineController: EnhancedTimelineController
        get() = retained.controller

    private class RetainedInstance(
        val controller: EnhancedTimelineController
    ) : InstanceKeeper.Instance {
        override fun onDestroy() {
            controller.cleanup()
        }
    }
}
