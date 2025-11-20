package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.timeline.EnhancedTimelineController

/**
 * Component for the Timeline screen.
 */
interface TimelineComponent {
    val enhancedTimelineController: EnhancedTimelineController
}

/**
 * Default implementation of TimelineComponent.
 */
class DefaultTimelineComponent(
    componentContext: ComponentContext,
    override val enhancedTimelineController: EnhancedTimelineController
) : TimelineComponent, ComponentContext by componentContext
