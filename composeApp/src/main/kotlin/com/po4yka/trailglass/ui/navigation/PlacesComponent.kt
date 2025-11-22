package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.places.PlacesController

/**
 * Component for places screen.
 */
interface PlacesComponent {
    val placesController: PlacesController
}

/**
 * Default implementation of PlacesComponent.
 */
class DefaultPlacesComponent(
    componentContext: ComponentContext,
    override val placesController: PlacesController
) : PlacesComponent,
    ComponentContext by componentContext
