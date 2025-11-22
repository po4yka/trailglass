package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.timeline.EnhancedTimelineController
import com.po4yka.trailglass.feature.tracking.LocationTrackingController

/**
 * Component for the Timeline screen.
 */
interface TimelineComponent {
    val enhancedTimelineController: EnhancedTimelineController
    val locationTrackingController: LocationTrackingController
}

/**
 * Default implementation of TimelineComponent.
 */
class DefaultTimelineComponent(
    componentContext: ComponentContext,
    override val enhancedTimelineController: EnhancedTimelineController,
    override val locationTrackingController: LocationTrackingController
) : TimelineComponent, ComponentContext by componentContext
