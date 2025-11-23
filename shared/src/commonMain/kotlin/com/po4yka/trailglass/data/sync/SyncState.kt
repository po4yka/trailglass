package com.po4yka.trailglass.data.sync

/**
 * Sync progress state for tracking synchronization progress.
 */
sealed class SyncProgress {
    data object Idle : SyncProgress()

    data class InProgress(
        val percentage: Int,
        val message: String
    ) : SyncProgress()

    data class Completed(
        val result: SyncResultSummary
    ) : SyncProgress()

    data class Failed(
        val error: String
    ) : SyncProgress()
}

/**
 * Summary of synchronization results.
 */
data class SyncResultSummary(
    val uploaded: Int,
    val downloaded: Int,
    val conflicts: Int,
    val conflictsResolved: Int,
    val errors: Int
)
