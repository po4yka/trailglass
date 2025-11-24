package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.photo.PhotoController

/** Component for the AddPhoto screen. */
interface AddPhotoComponent {
    val photoController: PhotoController
    val onBack: () -> Unit
}

/** Default implementation of AddPhotoComponent. */
class DefaultAddPhotoComponent(
    componentContext: ComponentContext,
    override val photoController: PhotoController,
    override val onBack: () -> Unit
) : AddPhotoComponent,
    ComponentContext by componentContext

