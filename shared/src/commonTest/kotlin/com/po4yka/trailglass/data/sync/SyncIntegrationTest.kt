package com.po4yka.trailglass.data.sync

import app.cash.turbine.test
import com.po4yka.trailglass.data.network.MockNetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkType
import com.po4yka.trailglass.data.remote.MockTrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration tests for end-to-end sync flows including:
 * - Network connectivity
 * - API communication
 * - Conflict resolution
 * - Repository updates
 */
class SyncIntegrationTest {

    @Test
    fun `full sync flow with local and remote changes`() = runTest {
        // Setup
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val placeVisitRepo = MockPlaceVisitRepository()
        val tripRepo = MockTripRepository()
        val apiClient = MockTrailGlassApiClient()

        // Add local changes
        val localVisit = createMockPlaceVisit("visit_1", "Local Place")
        placeVisitRepo.addVisit(localVisit)

        val localTrip = createMockTrip("trip_1", "Local Trip")
        tripRepo.addTrip(localTrip)

        // Setup remote changes
        val remoteVisit = PlaceVisitDto(
            id = "visit_remote",
            placeId = "place_remote",
            placeName = "Remote Place",
            placeLatitude = 40.0,
            placeLongitude = -120.0,
            placeAddress = "Remote Address",
            placeCategory = "Restaurant",
            arrivalTime = Clock.System.now(),
            departureTime = null,
            durationMinutes = null,
            syncAction = SyncAction.CREATE,
            localVersion = 1,
            serverVersion = 1,
            lastModified = Clock.System.now(),
            deviceId = "remote_device"
        )

        apiClient.setMockRemoteChanges(
            RemoteChanges(
                locations = emptyList(),
                placeVisits = listOf(remoteVisit),
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
        )

        val syncManager = createSyncManager(
            networkMonitor = networkMonitor,
            apiClient = apiClient,
            placeVisitRepo = placeVisitRepo,
            tripRepo = tripRepo
        )

        // Mark entities for sync
        syncManager.markForSync("visit_1", EntityType.PLACE_VISIT)
        syncManager.markForSync("trip_1", EntityType.TRIP)

        // Execute sync
        val result = syncManager.performFullSync()

        // Verify
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!

        syncResult.uploaded shouldBe 2 // visit_1 and trip_1
        syncResult.downloaded shouldBe 1 // remote visit
        syncResult.conflicts shouldBe 0
    }

    @Test
    fun `sync with network interruption and recovery`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        val apiClient = MockTrailGlassApiClient()

        // Start with connection
        networkMonitor.simulateConnect()
        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        syncManager.syncProgress.test {
            awaitItem() // Idle

            // Perform successful sync
            syncManager.performFullSync()
            awaitItem() // InProgress
            awaitItem().shouldBeInstanceOf<SyncProgress.Completed>()

            // Simulate network loss
            networkMonitor.simulateDisconnect()

            // Try to sync (should fail)
            syncManager.performFullSync()
            awaitItem() // InProgress (checks network)
            awaitItem().shouldBeInstanceOf<SyncProgress.Failed>()

            // Restore network
            networkMonitor.simulateConnect()

            // Give time for auto-sync to trigger
            kotlinx.coroutines.delay(100)

            // Should see new sync attempt
            awaitItem() // InProgress from auto-sync
            awaitItem().shouldBeInstanceOf<SyncProgress.Completed>()
        }
    }

    @Test
    fun `sync with conflicts requiring manual resolution`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()

        // Setup conflict
        val conflict = SyncConflictDto(
            conflictId = "conflict_visit_1",
            entityType = "PLACE_VISIT",
            entityId = "visit_1",
            localVersion = 2,
            remoteVersion = 3,
            localData = mapOf("placeName" to "Local Name", "durationMinutes" to 30),
            remoteData = mapOf("placeName" to "Remote Name", "durationMinutes" to 45),
            conflictedFields = listOf("placeName", "durationMinutes"),
            suggestedResolution = "MANUAL"
        )

        apiClient.setMockConflicts(listOf(conflict))

        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        // Perform sync
        val syncResult = syncManager.performFullSync()
        assertTrue(syncResult.isSuccess)

        // Verify conflict was stored
        val conflicts = syncManager.getUnresolvedConflicts()
        conflicts.size shouldBe 1
        conflicts[0].conflictId shouldBe "conflict_visit_1"
        conflicts[0].entityType shouldBe EntityType.PLACE_VISIT

        val status = syncManager.getSyncStatus()
        status.conflictCount shouldBe 1

        // Resolve conflict
        val resolutionResult = syncManager.resolveConflict(
            "conflict_visit_1",
            ConflictResolutionChoice.KEEP_LOCAL
        )
        assertTrue(resolutionResult.isSuccess)

        // Verify no more conflicts
        val afterResolution = syncManager.getUnresolvedConflicts()
        afterResolution.size shouldBe 0
    }

    @Test
    fun `sync on cellular network should show metered status`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect(type = NetworkType.CELLULAR, isMetered = true)

        val syncManager = createSyncManager(networkMonitor = networkMonitor)

        val status = syncManager.getSyncStatus()

        status.networkType shouldBe NetworkType.CELLULAR
        status.isNetworkMetered shouldBe true
    }

    @Test
    fun `multiple concurrent entities should sync correctly`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val placeVisitRepo = MockPlaceVisitRepository()
        val tripRepo = MockTripRepository()

        // Add multiple entities
        repeat(5) { i ->
            placeVisitRepo.addVisit(createMockPlaceVisit("visit_$i", "Place $i"))
        }

        repeat(3) { i ->
            tripRepo.addTrip(createMockTrip("trip_$i", "Trip $i"))
        }

        val syncManager = createSyncManager(
            networkMonitor = networkMonitor,
            placeVisitRepo = placeVisitRepo,
            tripRepo = tripRepo
        )

        // Mark all for sync
        repeat(5) { i ->
            syncManager.markForSync("visit_$i", EntityType.PLACE_VISIT)
        }
        repeat(3) { i ->
            syncManager.markForSync("trip_$i", EntityType.TRIP)
        }

        syncManager.getPendingSyncCount() shouldBe 8

        // Perform sync
        val result = syncManager.performFullSync()
        assertTrue(result.isSuccess)

        val syncResult = result.getOrNull()!!
        syncResult.uploaded shouldBe 8 // 5 visits + 3 trips
    }

    @Test
    fun `automatic conflict resolution with MERGE strategy`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()

        // Conflict with MERGE suggestion (should be resolved automatically)
        val conflict = SyncConflictDto(
            conflictId = "conflict_auto",
            entityType = "TRIP",
            entityId = "trip_1",
            localVersion = 1,
            remoteVersion = 2,
            localData = mapOf("name" to "Old Name"),
            remoteData = mapOf("name" to "New Name"),
            conflictedFields = listOf("name"),
            suggestedResolution = "MERGE"
        )

        apiClient.setMockConflicts(listOf(conflict))

        val syncManager = createSyncManager(networkMonitor = networkMonitor, apiClient = apiClient)

        // Perform sync
        val result = syncManager.performFullSync()
        assertTrue(result.isSuccess)

        // Conflict should NOT be in pending (auto-resolved)
        val conflicts = syncManager.getUnresolvedConflicts()
        conflicts.size shouldBe 0
    }

    private fun createSyncManager(
        networkMonitor: MockNetworkConnectivityMonitor,
        apiClient: MockTrailGlassApiClient = MockTrailGlassApiClient(),
        placeVisitRepo: MockPlaceVisitRepository = MockPlaceVisitRepository(),
        tripRepo: MockTripRepository = MockTripRepository()
    ): SyncManager {
        val syncStateRepository = MockSyncStateRepository()
        val syncCoordinator = SyncCoordinator(apiClient, syncStateRepository)

        return SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = SyncMetadataRepositoryImpl(),
            conflictRepository = ConflictRepositoryImpl(),
            networkMonitor = networkMonitor,
            placeVisitRepository = placeVisitRepo,
            tripRepository = tripRepo,
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )
    }
}
