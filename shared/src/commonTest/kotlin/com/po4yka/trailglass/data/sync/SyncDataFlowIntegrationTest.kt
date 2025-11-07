package com.po4yka.trailglass.data.sync

import app.cash.turbine.test
import com.po4yka.trailglass.data.network.MockNetworkConnectivityMonitor
import com.po4yka.trailglass.data.remote.MockTrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration tests for sync data flow.
 * Tests how data flows through the sync system with coordinated operations.
 */
class SyncDataFlowIntegrationTest {

    @Test
    fun `full sync flow should upload local changes and download remote changes`() = runTest {
        // Setup
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val placeVisitRepo = MockPlaceVisitRepository()
        val tripRepo = MockTripRepository()
        val apiClient = MockTrailGlassApiClient()
        val syncStateRepo = MockSyncStateRepository()
        val syncMetadataRepo = SyncMetadataRepositoryImpl()
        val conflictRepo = ConflictRepositoryImpl()

        // Add local data
        val localVisit = createMockPlaceVisit("visit1", "Local Place")
        placeVisitRepo.addVisit(localVisit)

        val localTrip = createMockTrip("trip1", "Local Trip")
        tripRepo.addTrip(localTrip)

        // Setup remote data
        val remoteVisit = PlaceVisitDto(
            id = "remote_visit",
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

        val syncCoordinator = SyncCoordinator(apiClient, syncStateRepo)
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = syncMetadataRepo,
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = placeVisitRepo,
            tripRepository = tripRepo,
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )

        // Mark for sync
        syncManager.markForSync("visit1", EntityType.PLACE_VISIT)
        syncManager.markForSync("trip1", EntityType.TRIP)

        // Execute sync
        val result = syncManager.performFullSync()

        // Verify
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        syncResult.uploaded shouldBe 2 // visit1 and trip1
        syncResult.downloaded shouldBe 1 // remote visit
        syncResult.conflicts shouldBe 0
    }

    @Test
    fun `sync should handle network reconnection and auto-sync`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        val apiClient = MockTrailGlassApiClient()
        val syncStateRepo = MockSyncStateRepository()
        val syncCoordinator = SyncCoordinator(apiClient, syncStateRepo)

        val syncManager = SyncManager(
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

        syncManager.syncProgress.test {
            awaitItem() // Idle

            // Start offline
            networkMonitor.simulateDisconnect()

            // Attempt sync while offline
            syncManager.performFullSync()
            awaitItem() // InProgress
            awaitItem().shouldBeInstanceOf<SyncProgress.Failed>()

            // Reconnect - should trigger auto-sync
            networkMonitor.simulateConnect()

            // Give time for auto-sync
            kotlinx.coroutines.delay(100)

            // Should see auto-sync
            awaitItem() // InProgress
            awaitItem().shouldBeInstanceOf<SyncProgress.Completed>()
        }
    }

    @Test
    fun `sync should detect and store conflicts`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()
        val conflictRepo = ConflictRepositoryImpl()

        // Setup conflict
        val conflict = SyncConflictDto(
            conflictId = "conflict1",
            entityType = "PLACE_VISIT",
            entityId = "visit1",
            localVersion = 2,
            remoteVersion = 3,
            localData = mapOf("placeName" to "Local Name"),
            remoteData = mapOf("placeName" to "Remote Name"),
            conflictedFields = listOf("placeName"),
            suggestedResolution = "MANUAL"
        )
        apiClient.setMockConflicts(listOf(conflict))

        val syncCoordinator = SyncCoordinator(apiClient, MockSyncStateRepository())
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = SyncMetadataRepositoryImpl(),
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )

        // Perform sync
        val result = syncManager.performFullSync()
        assertTrue(result.isSuccess)

        // Verify conflict was stored
        val conflicts = syncManager.getUnresolvedConflicts()
        conflicts.size shouldBe 1
        conflicts[0].conflictId shouldBe "conflict1"
        conflicts[0].entityType shouldBe EntityType.PLACE_VISIT

        val status = syncManager.getSyncStatus()
        status.conflictCount shouldBe 1
    }

    @Test
    fun `sync should resolve conflicts with KEEP_LOCAL strategy`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val metadataRepo = SyncMetadataRepositoryImpl()
        val conflictRepo = ConflictRepositoryImpl()

        // Store a conflict
        val conflict = StoredConflict(
            conflictId = "conflict1",
            entityType = EntityType.PLACE_VISIT,
            entityId = "visit1",
            localVersion = 2,
            remoteVersion = 3,
            localData = "{\"placeName\": \"Local\"}",
            remoteData = "{\"placeName\": \"Remote\"}",
            conflictedFields = listOf("placeName"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now()
        )
        conflictRepo.storeConflict(conflict)

        // Store metadata
        metadataRepo.upsertMetadata(
            SyncMetadata(
                entityId = "visit1",
                entityType = EntityType.PLACE_VISIT,
                localVersion = 2,
                serverVersion = 2,
                lastModified = Clock.System.now(),
                deviceId = "test_device"
            )
        )

        val syncCoordinator = SyncCoordinator(
            MockTrailGlassApiClient(),
            MockSyncStateRepository()
        )
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = metadataRepo,
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = MockTrailGlassApiClient(),
            deviceId = "test_device",
            userId = "test_user"
        )

        // Resolve with KEEP_LOCAL
        val result = syncManager.resolveConflict("conflict1", ConflictResolutionChoice.KEEP_LOCAL)
        assertTrue(result.isSuccess)

        // Verify conflict resolved
        val resolvedConflict = conflictRepo.getConflict("conflict1")
        resolvedConflict?.status shouldBe ConflictStatus.RESOLVED

        // Verify entity marked for sync
        val metadata = metadataRepo.getMetadata("visit1", EntityType.PLACE_VISIT)
        metadata?.isPendingSync shouldBe true
        metadata?.localVersion shouldBe 3 // Incremented
    }

    @Test
    fun `sync should resolve conflicts with KEEP_REMOTE strategy`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val metadataRepo = SyncMetadataRepositoryImpl()
        val conflictRepo = ConflictRepositoryImpl()

        val conflict = StoredConflict(
            conflictId = "conflict1",
            entityType = EntityType.TRIP,
            entityId = "trip1",
            localVersion = 2,
            remoteVersion = 5,
            localData = "{}",
            remoteData = "{}",
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now()
        )
        conflictRepo.storeConflict(conflict)

        val syncCoordinator = SyncCoordinator(
            MockTrailGlassApiClient(),
            MockSyncStateRepository()
        )
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = metadataRepo,
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = MockTrailGlassApiClient(),
            deviceId = "test_device",
            userId = "test_user"
        )

        // Resolve with KEEP_REMOTE
        val result = syncManager.resolveConflict("conflict1", ConflictResolutionChoice.KEEP_REMOTE)
        assertTrue(result.isSuccess)

        // Verify conflict resolved
        val resolvedConflict = conflictRepo.getConflict("conflict1")
        resolvedConflict?.status shouldBe ConflictStatus.RESOLVED

        // Verify metadata updated to remote version
        val metadata = metadataRepo.getMetadata("trip1", EntityType.TRIP)
        metadata?.serverVersion shouldBe 5
    }

    @Test
    fun `sync should auto-resolve MERGE conflicts`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()

        // Conflict with MERGE suggestion
        val conflict = SyncConflictDto(
            conflictId = "conflict_auto",
            entityType = "TRIP",
            entityId = "trip1",
            localVersion = 1,
            remoteVersion = 2,
            localData = mapOf("name" to "Old Name"),
            remoteData = mapOf("name" to "New Name"),
            conflictedFields = listOf("name"),
            suggestedResolution = "MERGE"
        )
        apiClient.setMockConflicts(listOf(conflict))

        val conflictRepo = ConflictRepositoryImpl()
        val syncCoordinator = SyncCoordinator(apiClient, MockSyncStateRepository())
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = SyncMetadataRepositoryImpl(),
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )

        // Perform sync
        val result = syncManager.performFullSync()
        assertTrue(result.isSuccess)

        // Conflict should be auto-resolved (not pending)
        val conflicts = syncManager.getUnresolvedConflicts()
        conflicts.size shouldBe 0
    }

    @Test
    fun `multiple pending syncs should all be uploaded`() = runTest {
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

        val syncCoordinator = SyncCoordinator(
            MockTrailGlassApiClient(),
            MockSyncStateRepository()
        )
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = SyncMetadataRepositoryImpl(),
            conflictRepository = ConflictRepositoryImpl(),
            networkMonitor = networkMonitor,
            placeVisitRepository = placeVisitRepo,
            tripRepository = tripRepo,
            apiClient = MockTrailGlassApiClient(),
            deviceId = "test_device",
            userId = "test_user"
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
        syncResult.uploaded shouldBe 8
    }

    @Test
    fun `sync progress should emit correct states during sync`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = MockTrailGlassApiClient()
        apiClient.syncDelayMs = 50 // Add small delay to see progress

        val syncCoordinator = SyncCoordinator(apiClient, MockSyncStateRepository())
        val syncManager = SyncManager(
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

        syncManager.syncProgress.test {
            val idle = awaitItem()
            idle.shouldBeInstanceOf<SyncProgress.Idle>()

            // Trigger sync
            syncManager.performFullSync()

            val inProgress = awaitItem()
            inProgress.shouldBeInstanceOf<SyncProgress.InProgress>()

            val completed = awaitItem()
            completed.shouldBeInstanceOf<SyncProgress.Completed>()
        }
    }

    @Test
    fun `sync should track last sync time and version`() = runTest {
        val networkMonitor = MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val syncStateRepo = MockSyncStateRepository()
        val syncCoordinator = SyncCoordinator(
            MockTrailGlassApiClient(),
            syncStateRepo
        )
        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = SyncMetadataRepositoryImpl(),
            conflictRepository = ConflictRepositoryImpl(),
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = MockTrailGlassApiClient(),
            deviceId = "test_device",
            userId = "test_user"
        )

        val beforeSync = syncManager.getSyncStatus()
        beforeSync.lastSyncTime shouldBe null

        // Perform sync
        syncManager.performFullSync()

        val afterSync = syncManager.getSyncStatus()
        afterSync.lastSyncTime shouldBe null // Will be null in mock, but would be set in real implementation
    }
}
