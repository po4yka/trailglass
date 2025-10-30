package com.po4yka.trailglass.data.sync

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue

class ConflictResolutionTest {

    @Test
    fun `conflict repository should store conflicts`() = runTest {
        val repository = ConflictRepositoryImpl()

        val conflict = StoredConflict(
            conflictId = "conflict_1",
            entityType = EntityType.PLACE_VISIT,
            entityId = "visit_1",
            localVersion = 1,
            remoteVersion = 2,
            localData = "{\"name\": \"Local\"}",
            remoteData = "{\"name\": \"Remote\"}",
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now()
        )

        repository.storeConflict(conflict)

        val retrieved = repository.getConflict("conflict_1")
        retrieved shouldBe conflict
    }

    @Test
    fun `should retrieve pending conflicts`() = runTest {
        val repository = ConflictRepositoryImpl()

        val conflict1 = StoredConflict(
            conflictId = "conflict_1",
            entityType = EntityType.PLACE_VISIT,
            entityId = "visit_1",
            localVersion = 1,
            remoteVersion = 2,
            localData = "{}",
            remoteData = "{}",
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now(),
            status = ConflictStatus.PENDING
        )

        val conflict2 = StoredConflict(
            conflictId = "conflict_2",
            entityType = EntityType.TRIP,
            entityId = "trip_1",
            localVersion = 3,
            remoteVersion = 4,
            localData = "{}",
            remoteData = "{}",
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now(),
            status = ConflictStatus.RESOLVED
        )

        repository.storeConflict(conflict1)
        repository.storeConflict(conflict2)

        val pending = repository.getPendingConflicts()
        pending.size shouldBe 1
        pending[0].conflictId shouldBe "conflict_1"
    }

    @Test
    fun `should mark conflict as resolved`() = runTest {
        val repository = ConflictRepositoryImpl()

        val conflict = StoredConflict(
            conflictId = "conflict_1",
            entityType = EntityType.PLACE_VISIT,
            entityId = "visit_1",
            localVersion = 1,
            remoteVersion = 2,
            localData = "{}",
            remoteData = "{}",
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now()
        )

        repository.storeConflict(conflict)
        repository.markAsResolved("conflict_1")

        val retrieved = repository.getConflict("conflict_1")
        retrieved?.status shouldBe ConflictStatus.RESOLVED
    }

    @Test
    fun `should count pending conflicts correctly`() = runTest {
        val repository = ConflictRepositoryImpl()

        repository.getConflictCount() shouldBe 0

        repository.storeConflict(createTestConflict("c1", ConflictStatus.PENDING))
        repository.getConflictCount() shouldBe 1

        repository.storeConflict(createTestConflict("c2", ConflictStatus.PENDING))
        repository.getConflictCount() shouldBe 2

        repository.markAsResolved("c1")
        repository.getConflictCount() shouldBe 1
    }

    @Test
    fun `should delete conflict`() = runTest {
        val repository = ConflictRepositoryImpl()

        val conflict = createTestConflict("conflict_1")
        repository.storeConflict(conflict)

        repository.getConflict("conflict_1") shouldBe conflict

        repository.deleteConflict("conflict_1")
        repository.getConflict("conflict_1") shouldBe null
    }

    @Test
    fun `should clear resolved conflicts`() = runTest {
        val repository = ConflictRepositoryImpl()

        repository.storeConflict(createTestConflict("c1", ConflictStatus.PENDING))
        repository.storeConflict(createTestConflict("c2", ConflictStatus.RESOLVED))
        repository.storeConflict(createTestConflict("c3", ConflictStatus.RESOLVED))

        repository.clearResolvedConflicts()

        val pending = repository.getPendingConflicts()
        pending.size shouldBe 1
        pending[0].conflictId shouldBe "c1"
    }

    @Test
    fun `SyncManager should resolve KEEP_LOCAL conflicts`() = runTest {
        val networkMonitor = com.po4yka.trailglass.data.network.MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = com.po4yka.trailglass.data.remote.MockTrailGlassApiClient()
        val syncCoordinator = SyncCoordinator(apiClient, MockSyncStateRepository())
        val metadataRepo = SyncMetadataRepositoryImpl()
        val conflictRepo = ConflictRepositoryImpl()

        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = metadataRepo,
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )

        // Store a conflict
        val conflict = createTestConflict("c1")
        conflictRepo.storeConflict(conflict)

        // Store metadata for the entity
        metadataRepo.upsertMetadata(
            SyncMetadata(
                entityId = "visit_1",
                entityType = EntityType.PLACE_VISIT,
                localVersion = 1,
                serverVersion = null,
                lastModified = Clock.System.now(),
                deviceId = "test_device"
            )
        )

        // Resolve with KEEP_LOCAL
        val result = syncManager.resolveConflict("c1", ConflictResolutionChoice.KEEP_LOCAL)

        assertTrue(result.isSuccess)

        // Conflict should be marked as resolved
        val retrievedConflict = conflictRepo.getConflict("c1")
        retrievedConflict?.status shouldBe ConflictStatus.RESOLVED

        // Entity should be marked for sync
        val metadata = metadataRepo.getMetadata("visit_1", EntityType.PLACE_VISIT)
        metadata?.isPendingSync shouldBe true
    }

    @Test
    fun `SyncManager should resolve KEEP_REMOTE conflicts`() = runTest {
        val networkMonitor = com.po4yka.trailglass.data.network.MockNetworkConnectivityMonitor()
        networkMonitor.simulateConnect()

        val apiClient = com.po4yka.trailglass.data.remote.MockTrailGlassApiClient()
        val syncCoordinator = SyncCoordinator(apiClient, MockSyncStateRepository())
        val metadataRepo = SyncMetadataRepositoryImpl()
        val conflictRepo = ConflictRepositoryImpl()

        val syncManager = SyncManager(
            syncCoordinator = syncCoordinator,
            syncMetadataRepository = metadataRepo,
            conflictRepository = conflictRepo,
            networkMonitor = networkMonitor,
            placeVisitRepository = MockPlaceVisitRepository(),
            tripRepository = MockTripRepository(),
            apiClient = apiClient,
            deviceId = "test_device",
            userId = "test_user"
        )

        // Store a conflict
        val conflict = createTestConflict("c1", remoteVersion = 5)
        conflictRepo.storeConflict(conflict)

        // Resolve with KEEP_REMOTE
        val result = syncManager.resolveConflict("c1", ConflictResolutionChoice.KEEP_REMOTE)

        assertTrue(result.isSuccess)

        // Conflict should be marked as resolved
        val retrievedConflict = conflictRepo.getConflict("c1")
        retrievedConflict?.status shouldBe ConflictStatus.RESOLVED
    }

    private fun createTestConflict(
        id: String,
        status: ConflictStatus = ConflictStatus.PENDING,
        remoteVersion: Long = 2
    ): StoredConflict {
        return StoredConflict(
            conflictId = id,
            entityType = EntityType.PLACE_VISIT,
            entityId = "visit_1",
            localVersion = 1,
            remoteVersion = remoteVersion,
            localData = "{\"name\": \"Local\"}",
            remoteData = "{\"name\": \"Remote\"}",
            conflictedFields = listOf("name"),
            suggestedResolution = "MANUAL",
            createdAt = Clock.System.now(),
            status = status
        )
    }
}
