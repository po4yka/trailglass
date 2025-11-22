package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.places.PlacesController

/**
 * Component for place detail screen.
 */
interface PlaceDetailComponent {
    val placeId: String
    val placesController: PlacesController
    val onBack: () -> Unit
}

/**
 * Default implementation of PlaceDetailComponent.
 */
class DefaultPlaceDetailComponent(
    componentContext: ComponentContext,
    override val placeId: String,
    override val placesController: PlacesController,
    override val onBack: () -> Unit
) : PlaceDetailComponent,
    ComponentContext by componentContext
