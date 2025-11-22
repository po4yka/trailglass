package com.po4yka.trailglass.di

import com.po4yka.trailglass.domain.permission.PermissionManager
import com.po4yka.trailglass.domain.permission.PermissionRationaleProvider
import com.po4yka.trailglass.feature.permission.PermissionFlowController
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Provides

/**
 * Dependency injection module for permission-related components.
 */
interface PermissionModule {
    /**
     * Provides the platform-specific permission manager.
     * This should be implemented by platform-specific modules.
     */
    val permissionManager: PermissionManager

    /**
     * Provides the permission rationale provider.
     */
    @AppScope
    @Provides
    fun providePermissionRationaleProvider(): PermissionRationaleProvider = PermissionRationaleProvider()

    /**
     * Provides the permission flow controller.
     */
    @AppScope
    @Provides
    fun providePermissionFlowController(
        permissionManager: PermissionManager,
        rationaleProvider: PermissionRationaleProvider,
        coroutineScope: CoroutineScope
    ): PermissionFlowController =
        PermissionFlowController(
            permissionManager = permissionManager,
            rationaleProvider = rationaleProvider,
            coroutineScope = coroutineScope
        )
}
