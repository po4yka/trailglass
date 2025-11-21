package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.settings.SettingsController

/**
 * Component for the Algorithm Settings screen.
 */
interface AlgorithmSettingsComponent {
    val settingsController: SettingsController
    val onBack: () -> Unit
}

/**
 * Default implementation of AlgorithmSettingsComponent.
 */
class DefaultAlgorithmSettingsComponent(
    componentContext: ComponentContext,
    override val settingsController: SettingsController,
    override val onBack: () -> Unit
) : AlgorithmSettingsComponent, ComponentContext by componentContext
