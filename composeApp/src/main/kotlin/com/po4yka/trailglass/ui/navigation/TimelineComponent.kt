package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.timeline.TimelineController

/**
 * Component for the Timeline screen.
 */
interface TimelineComponent {
    val timelineController: TimelineController
}

/**
 * Default implementation of TimelineComponent.
 */
class DefaultTimelineComponent(
    componentContext: ComponentContext,
    override val timelineController: TimelineController
) : TimelineComponent, ComponentContext by componentContext
