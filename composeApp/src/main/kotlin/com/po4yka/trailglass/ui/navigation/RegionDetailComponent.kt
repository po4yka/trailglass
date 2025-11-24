package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.regions.RegionsController

/** Component for the RegionDetail screen. */
interface RegionDetailComponent {
    val regionsController: RegionsController
    val regionId: String?
    val onBack: () -> Unit
    val onNavigateToMapPicker: (Double, Double, Double) -> Unit
}

/** Default implementation of RegionDetailComponent. */
class DefaultRegionDetailComponent(
    componentContext: ComponentContext,
    override val regionsController: RegionsController,
    override val regionId: String?,
    override val onBack: () -> Unit,
    override val onNavigateToMapPicker: (Double, Double, Double) -> Unit
) : RegionDetailComponent,
    ComponentContext by componentContext
