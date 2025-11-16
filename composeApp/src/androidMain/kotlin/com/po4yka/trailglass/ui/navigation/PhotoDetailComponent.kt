package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.photo.PhotoController

/**
 * Component for the Photo Detail screen.
 */
interface PhotoDetailComponent {
    val photoController: PhotoController
    val photoId: String
    val onBack: () -> Unit
}

/**
 * Default implementation of PhotoDetailComponent.
 */
class DefaultPhotoDetailComponent(
    componentContext: ComponentContext,
    override val photoController: PhotoController,
    override val photoId: String,
    override val onBack: () -> Unit
) : PhotoDetailComponent, ComponentContext by componentContext
