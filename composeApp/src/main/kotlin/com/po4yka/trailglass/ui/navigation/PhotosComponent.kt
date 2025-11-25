package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.photo.PhotoGalleryController

/** Component for the Photos screen. Now accessible via nested navigation with back support. */
interface PhotosComponent {
    val photoGalleryController: PhotoGalleryController
    val onBack: () -> Unit
}

/** Default implementation of PhotosComponent. */
class DefaultPhotosComponent(
    componentContext: ComponentContext,
    override val photoGalleryController: PhotoGalleryController,
    override val onBack: () -> Unit
) : PhotosComponent,
    ComponentContext by componentContext
