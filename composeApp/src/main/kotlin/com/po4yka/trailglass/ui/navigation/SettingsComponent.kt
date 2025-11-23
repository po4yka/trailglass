package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.tracking.LocationTrackingController

/** Component for the Settings screen. */
interface SettingsComponent {
    val locationTrackingController: LocationTrackingController
}

/** Default implementation of SettingsComponent. */
class DefaultSettingsComponent(
    componentContext: ComponentContext,
    override val locationTrackingController: LocationTrackingController
) : SettingsComponent,
    ComponentContext by componentContext
