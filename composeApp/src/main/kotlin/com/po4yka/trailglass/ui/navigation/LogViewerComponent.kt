package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext

/** Component for the LogViewer screen. */
interface LogViewerComponent {
    val onBack: () -> Unit
}

/** Default implementation of LogViewerComponent. */
class DefaultLogViewerComponent(
    componentContext: ComponentContext,
    override val onBack: () -> Unit
) : LogViewerComponent,
    ComponentContext by componentContext
