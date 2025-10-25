package com.po4yka.trailglass.data.sync

import app.cash.turbine.test
import com.po4yka.trailglass.data.network.MockNetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkState
import com.po4yka.trailglass.data.network.NetworkType
import com.po4yka.trailglass.data.remote.MockTrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.SyncConflictDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SyncManagerTest {

    private fun createSyncManager(
        networkMonitor: MockNetworkConnectivityMonitor = MockNetworkConnectivityMonitor(),
        apiClient: MockTrailGlassApiClient = MockTrailGlassApiClient()
    ): SyncManager {
        val syncStateRepository = MockSyncStateRepository()
        val syncCoordinator = SyncCoordinator(apiClient, syncStateRepository)

        return SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = SyncMetadataRepositoryImpl(),
            conflictRepository = ConflictRepositoryImpl(),
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )
    }

    @Test
    fun `sync should fail when network is disconnected`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateDisconnect()

        val syncManager = createSyncManager(networkMonitor = networkMonitor)

        val result = syncManager.performFullSync()

        assertTrue(result.isFailure)
        result.exceptionOrNull()?.message shouldBe "No network connection"
    }

    @Test
    fun `sync should succeed when network is connected`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()
        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        val result = syncManager.performFullSync()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `sync should emit progress states`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val syncManager = createSyncManager(networkMonitor = networkMonitor)

        syncManager.syncProgress.test {
            // Initial state
            awaitItem().shouldBeInstanceOf<SyncProgress.Idle>()

            // Trigger sync
            syncManager.performFullSync()

            // Should emit in progress
            val inProgress = awaitItem()
            inProgress.shouldBeInstanceOf<SyncProgress.InProgress>()
            (inProgress as SyncProgress.InProgress).message shouldBe "Collecting local changes..."

            // Should complete
            awaitItem().shouldBeInstanceOf<SyncProgress.Completed>()
        }
    }

    @Test
    fun `auto-sync should trigger when network reconnects`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        val apiClient = MockTrailGlassApiClient()

        // Start offline
        networkMonitor.simulateDisconnect()
        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        // Try to sync while offline (should fail and set auto-sync flag)
        val offlineResult = syncManager.performFullSync()
        assertTrue(offlineResult.isFailure)

        // Reconnect
        networkMonitor.simulateConnect()

        // Give time for the network state change to trigger auto-sync
        kotlinx.coroutines.delay(100)

        // Verify sync was attempted
        apiClient.getSyncHistory().size shouldBe 1
    }

    @Test
    fun `getSyncStatus should include network information`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect(type = NetworkType.CELLULAR, isMetered = true)

        val syncManager = createSyncManager(networkMonitor = networkMonitor)

        val status = syncManager.getSyncStatus()

        status.networkState.shouldBeInstanceOf<NetworkState.Connected>()
        status.networkType shouldBe NetworkType.CELLULAR
        status.isNetworkMetered shouldBe true
    }

    @Test
    fun `sync should handle conflicts`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()
        val conflict = SyncConflictDto(
            conflictId = "conflict_1",
            entityType = "PLACE_VISIT",
            entityId = "visit_1",
            localVersion = 1,
            remoteVersion = 2,
            localData = mapOf("name" to "Local Name"),
            remoteData = mapOf("name" to "Remote Name"),
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL"
        )
        apiClient.setMockConflicts(listOf(conflict))

        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        val result = syncManager.performFullSync()

        assertTrue(result.isSuccess)

        // Check conflicts were stored
        val conflicts = syncManager.getUnresolvedConflicts()
        conflicts.size shouldBe 1
        conflicts[0].conflictId shouldBe "conflict_1"
    }

    @Test
    fun `limited network should prevent sync`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateLimitedConnection("No internet access")

        val syncManager = createSyncManager(networkMonitor = networkMonitor)

        val result = syncManager.performFullSync()

        assertTrue(result.isFailure)
    }

    @Test
    fun `sync progress should be Failed when sync fails`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()
        apiClient.shouldFailSync = true

        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        syncManager.syncProgress.test {
            awaitItem() // Idle

            syncManager.performFullSync()

            awaitItem() // InProgress
            val failed = awaitItem()
            failed.shouldBeInstanceOf<SyncProgress.Failed>()
        }
    }

    @Test
    fun `getPendingSyncCount should return correct count`() = runTest {
        val syncManager = createSyncManager()

        // Initially zero
        syncManager.getPendingSyncCount() shouldBe 0

        // Mark some entities for sync
        syncManager.markForSync("entity_1", EntityType.PLACE_VISIT)
        syncManager.markForSync("entity_2", EntityType.TRIP)

        syncManager.getPendingSyncCount() shouldBe 2
    }
}

/**
 * Mock SyncStateRepository for testing.
 */
class MockSyncStateRepository : SyncStateRepository {
    private var lastSyncVersion: Long = 0
    private var isSyncNeeded: Boolean = false

    override suspend fun getLastSyncVersion(): Long = lastSyncVersion

    override suspend fun setLastSyncVersion(version: Long) {
        lastSyncVersion = version
    }

    override suspend fun isSyncNeeded(): Boolean = isSyncNeeded

    override suspend fun markSyncNeeded() {
        isSyncNeeded = true
    }

    override suspend fun clearSyncNeeded() {
        isSyncNeeded = false
    }
}
