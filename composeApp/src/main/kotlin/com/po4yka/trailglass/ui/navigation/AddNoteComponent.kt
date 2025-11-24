package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext

/** Component for the AddNote screen. */
interface AddNoteComponent {
    val onBack: () -> Unit
}

/** Default implementation of AddNoteComponent. */
class DefaultAddNoteComponent(
    componentContext: ComponentContext,
    override val onBack: () -> Unit
) : AddNoteComponent,
    ComponentContext by componentContext

