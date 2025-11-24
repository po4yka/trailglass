package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.regions.RegionsController

/** Component for the Regions screen. */
interface RegionsComponent {
    val regionsController: RegionsController
}

/** Default implementation of RegionsComponent. */
class DefaultRegionsComponent(
    componentContext: ComponentContext,
    override val regionsController: RegionsController
) : RegionsComponent,
    ComponentContext by componentContext
