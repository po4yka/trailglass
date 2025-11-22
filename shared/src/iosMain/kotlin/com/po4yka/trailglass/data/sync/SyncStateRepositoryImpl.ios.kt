package com.po4yka.trailglass.data.sync

import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using UserDefaults.
 */
@Inject
actual class SyncStateRepositoryImpl : SyncStateRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    private object Keys {
        const val LAST_SYNC_TIMESTAMP = "sync_last_sync_timestamp"
        const val LAST_SYNC_VERSION = "sync_last_sync_version"
        const val IS_SYNCING = "sync_is_syncing"
        const val PENDING_CHANGES = "sync_pending_changes"
        const val ERROR = "sync_error"
    }

    actual override suspend fun getSyncState(): SyncState =
        SyncState(
            lastSyncTimestamp =
                userDefaults.stringForKey(Keys.LAST_SYNC_TIMESTAMP)?.let {
                    try {
                        Instant.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            lastSyncVersion = userDefaults.integerForKey(Keys.LAST_SYNC_VERSION),
            isSyncing = userDefaults.boolForKey(Keys.IS_SYNCING),
            pendingChanges = userDefaults.integerForKey(Keys.PENDING_CHANGES).toInt(),
            error = userDefaults.stringForKey(Keys.ERROR)
        )

    actual override suspend fun updateSyncState(state: SyncState) {
        state.lastSyncTimestamp?.let {
            userDefaults.setObject(it.toString(), Keys.LAST_SYNC_TIMESTAMP)
        }
        userDefaults.setInteger(state.lastSyncVersion, Keys.LAST_SYNC_VERSION)
        userDefaults.setBool(state.isSyncing, Keys.IS_SYNCING)
        userDefaults.setInteger(state.pendingChanges.toLong(), Keys.PENDING_CHANGES)
        state.error?.let {
            userDefaults.setObject(it, Keys.ERROR)
        } ?: userDefaults.removeObjectForKey(Keys.ERROR)

        userDefaults.synchronize()
    }
}
