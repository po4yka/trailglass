package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.trips.TripsController

/**
 * Component for trips screen.
 */
interface TripsComponent {
    val tripsController: TripsController
}

/**
 * Default implementation of TripsComponent.
 */
class DefaultTripsComponent(
    componentContext: ComponentContext,
    override val tripsController: TripsController
) : TripsComponent, ComponentContext by componentContext
