package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.places.PlacesController

/** Component for places screen. Now accessible via nested navigation with back support. */
interface PlacesComponent {
    val placesController: PlacesController
    val onBack: () -> Unit
}

/** Default implementation of PlacesComponent. */
class DefaultPlacesComponent(
    componentContext: ComponentContext,
    override val placesController: PlacesController,
    override val onBack: () -> Unit
) : PlacesComponent,
    ComponentContext by componentContext
