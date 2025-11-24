package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.diagnostics.DiagnosticsController

/** Component for the Diagnostics screen. */
interface DiagnosticsComponent {
    val diagnosticsController: DiagnosticsController
    val onBack: () -> Unit
}

/** Default implementation of DiagnosticsComponent. */
class DefaultDiagnosticsComponent(
    componentContext: ComponentContext,
    override val diagnosticsController: DiagnosticsController,
    override val onBack: () -> Unit
) : DiagnosticsComponent,
    ComponentContext by componentContext
