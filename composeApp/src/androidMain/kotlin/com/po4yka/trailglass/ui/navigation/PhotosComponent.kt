package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.photo.PhotoController

/**
 * Component for the Photos screen.
 */
interface PhotosComponent {
    val photoController: PhotoController
}

/**
 * Default implementation of PhotosComponent.
 */
class DefaultPhotosComponent(
    componentContext: ComponentContext,
    override val photoController: PhotoController
) : PhotosComponent, ComponentContext by componentContext
