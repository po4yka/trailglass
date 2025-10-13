package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import kotlinx.coroutines.CoroutineScope

/**
 * Platform-specific dependency injection module.
 *
 * This interface defines dependencies that must be provided by platform-specific code:
 * - DatabaseDriverFactory (Android/iOS specific)
 * - CoroutineScope (application lifecycle scope)
 * - User ID (from platform authentication)
 * - Device ID (platform-specific identifier)
 *
 * Each platform (Android, iOS) should create a concrete implementation of this interface.
 */
interface PlatformModule {
    /**
     * Platform-specific database driver factory.
     */
    val databaseDriverFactory: DatabaseDriverFactory

    /**
     * Application-level coroutine scope.
     * Should be tied to application lifecycle.
     */
    val applicationScope: CoroutineScope

    /**
     * Current user ID.
     */
    val userId: String

    /**
     * Device identifier.
     */
    val deviceId: String
}
