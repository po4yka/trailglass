package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.data.repository.PlaceVisitRepository

/** Component for the ManualCheckIn screen. */
interface ManualCheckInComponent {
    val placeVisitRepository: PlaceVisitRepository
    val onBack: () -> Unit
}

/** Default implementation of ManualCheckInComponent. */
class DefaultManualCheckInComponent(
    componentContext: ComponentContext,
    override val placeVisitRepository: PlaceVisitRepository,
    override val onBack: () -> Unit
) : ManualCheckInComponent,
    ComponentContext by componentContext

