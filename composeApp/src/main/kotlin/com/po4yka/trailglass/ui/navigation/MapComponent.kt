package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.po4yka.trailglass.feature.map.MapController

/** Component for the Map screen. */
interface MapComponent {
    val mapController: MapController
}

/** Default implementation of MapComponent. */
class DefaultMapComponent(
    componentContext: ComponentContext,
    mapController: MapController
) : MapComponent,
    ComponentContext by componentContext {
    private val retained =
        instanceKeeper.getOrCreate {
            RetainedInstance(mapController)
        }

    override val mapController: MapController
        get() = retained.controller

    private class RetainedInstance(
        val controller: MapController
    ) : InstanceKeeper.Instance {
        override fun onDestroy() {
            controller.cleanup()
        }
    }
}
