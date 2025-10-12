package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIDevice

/**
 * iOS implementation of PlatformModule.
 *
 * Provides iOS-specific dependencies including:
 * - DatabaseDriverFactory (using iOS SQLite)
 * - Application-level CoroutineScope
 * - User ID and Device ID
 */
@Inject
class IOSPlatformModule : PlatformModule {

    @Provides
    override val databaseDriverFactory: DatabaseDriverFactory
        get() = DatabaseDriverFactory()

    @Provides
    override val applicationScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    override val userId: String
        get() = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

    @Provides
    override val deviceId: String
        get() = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"
}
