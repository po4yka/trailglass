package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.AcceptedEntities
import com.po4yka.trailglass.data.remote.dto.DeletedIds
import com.po4yka.trailglass.data.remote.dto.DeltaSyncRequest
import com.po4yka.trailglass.data.remote.dto.DeltaSyncResponse
import com.po4yka.trailglass.data.remote.dto.LoginResponse
import com.po4yka.trailglass.data.remote.dto.RejectedEntities
import com.po4yka.trailglass.data.remote.dto.RemoteChanges
import com.po4yka.trailglass.data.remote.dto.SyncConflictDto

/**
 * Mock implementation for testing sync operations. Note: This doesn't extend TrailGlassApiClient since it's a final
 * class. Instead, this provides a standalone mock for testing sync logic.
 */
class MockTrailGlassApiClient {
    var shouldFailSync = false
    var shouldFailLogin = false
    var syncDelayMs = 0L
    var mockConflicts: List<SyncConflictDto> = emptyList()
    var mockRemoteChanges: RemoteChanges =
        RemoteChanges(
            locations = emptyList(),
            placeVisits = emptyList(),
            trips = emptyList(),
            photos = emptyList(),
            deletedIds =
                DeletedIds(
                    locations = emptyList(),
                    placeVisits = emptyList(),
                    trips = emptyList(),
                    photos = emptyList()
                )
        )

    private val syncHistory = mutableListOf<DeltaSyncRequest>()

    suspend fun performDeltaSync(request: DeltaSyncRequest): Result<DeltaSyncResponse> {
        syncHistory.add(request)

        if (syncDelayMs > 0) {
            kotlinx.coroutines.delay(syncDelayMs)
        }

        return if (shouldFailSync) {
            Result.failure(Exception("Mock sync failure"))
        } else {
            Result.success(
                DeltaSyncResponse(
                    syncVersion = 42L,
                    syncTimestamp =
                        kotlinx.datetime.Clock.System
                            .now()
                            .toString(),
                    conflicts = mockConflicts,
                    remoteChanges = mockRemoteChanges,
                    accepted =
                        AcceptedEntities(
                            locations = request.localChanges.locations.map { it.id },
                            placeVisits = request.localChanges.placeVisits.map { it.id },
                            trips = request.localChanges.trips.map { it.id },
                            photos = request.localChanges.photos.map { it.id }
                        ),
                    rejected =
                        RejectedEntities(
                            locations = emptyList(),
                            placeVisits = emptyList(),
                            trips = emptyList(),
                            photos = emptyList()
                        )
                )
            )
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse> =
        if (shouldFailLogin) {
            Result.failure(Exception("Mock login failure"))
        } else {
            Result.success(
                LoginResponse(
                    userId = "test_user_id",
                    email = email,
                    displayName = "Test User",
                    accessToken = "mock_access_token",
                    refreshToken = "mock_refresh_token",
                    expiresIn = 3600,
                    lastSyncTimestamp =
                        kotlinx.datetime.Clock.System
                            .now()
                            .toString()
                )
            )
        }

    // Test helpers
    fun getSyncHistory(): List<DeltaSyncRequest> = syncHistory.toList()

    fun getLastSyncRequest(): DeltaSyncRequest? = syncHistory.lastOrNull()

    fun clearHistory() {
        syncHistory.clear()
    }
}

class MockTokenProvider : TokenProvider {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}

class MockDeviceInfoProvider : DeviceInfoProvider {
    override fun getDeviceId(): String = "mock_device_id"

    override fun getDeviceName(): String = "Mock Device"

    override fun getPlatform(): String = "MockOS"

    override fun getOsVersion(): String = "Mock OS 1.0"

    override fun getAppVersion(): String = "1.0.0-test"
}
