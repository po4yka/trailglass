package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.photo.PhotoDetailController

/**
 * Component for the Photo Detail screen.
 */
interface PhotoDetailComponent {
    val photoDetailController: PhotoDetailController
    val photoId: String
    val onBack: () -> Unit
}

/**
 * Default implementation of PhotoDetailComponent.
 */
class DefaultPhotoDetailComponent(
    componentContext: ComponentContext,
    override val photoDetailController: PhotoDetailController,
    override val photoId: String,
    override val onBack: () -> Unit
) : PhotoDetailComponent, ComponentContext by componentContext
