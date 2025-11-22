package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.devices.DeviceManagementController

/**
 * Component for the Device Management screen.
 */
interface DeviceManagementComponent {
    val deviceManagementController: DeviceManagementController
    val onBack: () -> Unit
}

/**
 * Default implementation of DeviceManagementComponent.
 */
class DefaultDeviceManagementComponent(
    componentContext: ComponentContext,
    override val deviceManagementController: DeviceManagementController,
    override val onBack: () -> Unit
) : DeviceManagementComponent,
    ComponentContext by componentContext
