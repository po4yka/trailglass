package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext

/** Component for the MapPicker screen. */
interface MapPickerComponent {
    val initialLat: Double?
    val initialLon: Double?
    val initialRadius: Double?
    val onBack: () -> Unit
    val onLocationSelected: (Double, Double, Double) -> Unit
}

/** Default implementation of MapPickerComponent. */
class DefaultMapPickerComponent(
    componentContext: ComponentContext,
    override val initialLat: Double?,
    override val initialLon: Double?,
    override val initialRadius: Double?,
    override val onBack: () -> Unit,
    override val onLocationSelected: (Double, Double, Double) -> Unit
) : MapPickerComponent,
    ComponentContext by componentContext

