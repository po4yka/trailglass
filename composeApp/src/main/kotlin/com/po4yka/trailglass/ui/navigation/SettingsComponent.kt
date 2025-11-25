package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.tracking.LocationTrackingController

/** Component for the Settings screen. Now a pushed screen with back navigation. */
interface SettingsComponent {
    val locationTrackingController: LocationTrackingController
    val onBack: () -> Unit
}

/** Default implementation of SettingsComponent. */
class DefaultSettingsComponent(
    componentContext: ComponentContext,
    override val locationTrackingController: LocationTrackingController,
    override val onBack: () -> Unit
) : SettingsComponent,
    ComponentContext by componentContext
