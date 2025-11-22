package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.photo.PhotoGalleryController

/**
 * Component for the Photos screen.
 */
interface PhotosComponent {
    val photoGalleryController: PhotoGalleryController
}

/**
 * Default implementation of PhotosComponent.
 */
class DefaultPhotosComponent(
    componentContext: ComponentContext,
    override val photoGalleryController: PhotoGalleryController
) : PhotosComponent,
    ComponentContext by componentContext
