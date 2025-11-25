package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.po4yka.trailglass.feature.map.MapController

/** Component for the Map screen. */
interface MapComponent {
    val mapController: MapController
}

/** Default implementation of MapComponent. */
class DefaultMapComponent(
    componentContext: ComponentContext,
    override val mapController: MapController
) : MapComponent,
    ComponentContext by componentContext {
    init {
        lifecycle.doOnDestroy {
            mapController.cleanup()
        }
    }
}
