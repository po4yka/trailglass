package com.po4yka.trailglass.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

private val Context.syncStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sync_state"
)

/** Android implementation using DataStore. */
@Inject
actual class SyncStateRepositoryImpl(
    private val context: Context
) : SyncStateRepository {
    private object Keys {
        val LAST_SYNC_TIMESTAMP = stringPreferencesKey("last_sync_timestamp")
        val LAST_SYNC_VERSION = longPreferencesKey("last_sync_version")
        val IS_SYNCING = booleanPreferencesKey("is_syncing")
        val PENDING_CHANGES = intPreferencesKey("pending_changes")
        val ERROR = stringPreferencesKey("error")
    }

    actual override suspend fun getSyncState(): SyncState =
        context.syncStateDataStore.data
            .map { preferences ->
                SyncState(
                    lastSyncTimestamp =
                        preferences[Keys.LAST_SYNC_TIMESTAMP]?.let {
                            try {
                                Instant.parse(it)
                            } catch (e: IllegalArgumentException) {
                                // Invalid timestamp format, return null
                                null
                            }
                        },
                    lastSyncVersion = preferences[Keys.LAST_SYNC_VERSION] ?: 0,
                    isSyncing = preferences[Keys.IS_SYNCING] ?: false,
                    pendingChanges = preferences[Keys.PENDING_CHANGES] ?: 0,
                    error = preferences[Keys.ERROR]
                )
            }.first()

    actual override suspend fun updateSyncState(state: SyncState) {
        context.syncStateDataStore.edit { preferences ->
            state.lastSyncTimestamp?.let {
                preferences[Keys.LAST_SYNC_TIMESTAMP] = it.toString()
            }
            preferences[Keys.LAST_SYNC_VERSION] = state.lastSyncVersion
            preferences[Keys.IS_SYNCING] = state.isSyncing
            preferences[Keys.PENDING_CHANGES] = state.pendingChanges
            state.error?.let { preferences[Keys.ERROR] = it } ?: preferences.remove(Keys.ERROR)
        }
    }
}
