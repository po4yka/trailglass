package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.*

/**
 * Mock implementation of TrailGlassApiClient for testing.
 */
class MockTrailGlassApiClient : TrailGlassApiClient(
    config = ApiConfig(baseUrl = "https://mock.api.com", timeout = 30000, enableLogging = false),
    tokenProvider = MockTokenProvider(),
    deviceInfoProvider = MockDeviceInfoProvider()
) {
    var shouldFailSync = false
    var shouldFailLogin = false
    var syncDelayMs = 0L
    var mockConflicts: List<SyncConflictDto> = emptyList()
    var mockRemoteChanges: RemoteChanges = RemoteChanges(
        locations = emptyList(),
        placeVisits = emptyList(),
        trips = emptyList(),
        photos = emptyList(),
        settings = null,
        deletedIds = DeletedIds(
            locations = emptyList(),
            placeVisits = emptyList(),
            trips = emptyList(),
            photos = emptyList()
        )
    )

    private val syncHistory = mutableListOf<DeltaSyncRequest>()

    override suspend fun performDeltaSync(request: DeltaSyncRequest): Result<DeltaSyncResponse> {
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
                    serverTime = kotlinx.datetime.Clock.System.now(),
                    remoteChanges = mockRemoteChanges,
                    accepted = AcceptedChanges(
                        locations = request.localChanges.locations.map { it.id },
                        placeVisits = request.localChanges.placeVisits.map { it.id },
                        trips = request.localChanges.trips.map { it.id },
                        photos = request.localChanges.photos.map { it.id }
                    ),
                    rejected = RejectedChanges(
                        locations = emptyList(),
                        placeVisits = emptyList(),
                        trips = emptyList(),
                        photos = emptyList()
                    ),
                    conflicts = mockConflicts
                )
            )
        }
    }

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return if (shouldFailLogin) {
            Result.failure(Exception("Mock login failure"))
        } else {
            Result.success(
                LoginResponse(
                    accessToken = "mock_access_token",
                    refreshToken = "mock_refresh_token",
                    expiresIn = 3600,
                    userId = "test_user_id",
                    userProfile = UserProfileDto(
                        id = "test_user_id",
                        email = email,
                        displayName = "Test User",
                        createdAt = kotlinx.datetime.Clock.System.now(),
                        lastLoginAt = kotlinx.datetime.Clock.System.now()
                    )
                )
            )
        }
    }

    // Test helpers
    fun getSyncHistory(): List<DeltaSyncRequest> = syncHistory.toList()

    fun getLastSyncRequest(): DeltaSyncRequest? = syncHistory.lastOrNull()

    fun clearHistory() {
        syncHistory.clear()
    }

    fun setMockConflicts(conflicts: List<SyncConflictDto>) {
        mockConflicts = conflicts
    }

    fun setMockRemoteChanges(changes: RemoteChanges) {
        mockRemoteChanges = changes
    }
}

class MockTokenProvider : TokenProvider {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
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

    override fun getDeviceModel(): String = "Mock Device"

    override fun getOsVersion(): String = "Mock OS 1.0"

    override fun getAppVersion(): String = "1.0.0-test"
}
