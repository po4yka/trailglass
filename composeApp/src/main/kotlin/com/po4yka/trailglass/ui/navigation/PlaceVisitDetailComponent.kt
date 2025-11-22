package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext

/**
 * Component for the Place Visit Detail screen.
 */
interface PlaceVisitDetailComponent {
    val placeVisitId: String
    val onBack: () -> Unit
}

/**
 * Default implementation of PlaceVisitDetailComponent.
 */
class DefaultPlaceVisitDetailComponent(
    componentContext: ComponentContext,
    override val placeVisitId: String,
    override val onBack: () -> Unit
) : PlaceVisitDetailComponent,
    ComponentContext by componentContext
