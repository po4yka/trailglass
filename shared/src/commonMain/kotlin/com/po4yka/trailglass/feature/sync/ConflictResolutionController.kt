package com.po4yka.trailglass.feature.sync

import com.po4yka.trailglass.data.remote.dto.ConflictResolution
import com.po4yka.trailglass.data.remote.dto.SyncConflictDto
import com.po4yka.trailglass.data.sync.SyncCoordinator
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for Conflict Resolution screen.
 * Manages sync conflicts and user resolution choices.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class ConflictResolutionController(
    private val syncCoordinator: SyncCoordinator,
    coroutineScope: CoroutineScope
) : Lifecycle {

    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /**
     * Conflict Resolution UI state.
     */
    data class ConflictResolutionState(
        val conflicts: List<SyncConflictDto> = emptyList(),
        val currentConflictIndex: Int = 0,
        val isResolving: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val resolvedCount: Int = 0
    ) {
        val currentConflict: SyncConflictDto?
            get() = conflicts.getOrNull(currentConflictIndex)

        val hasMoreConflicts: Boolean
            get() = currentConflictIndex < conflicts.size - 1

        val isComplete: Boolean
            get() = resolvedCount == conflicts.size && conflicts.isNotEmpty()
    }

    private val _state = MutableStateFlow(ConflictResolutionState())
    val state: StateFlow<ConflictResolutionState> = _state.asStateFlow()

    /**
     * Set conflicts to be resolved.
     */
    fun setConflicts(conflicts: List<SyncConflictDto>) {
        logger.info { "Setting ${conflicts.size} conflicts for resolution" }

        _state.update {
            ConflictResolutionState(
                conflicts = conflicts,
                currentConflictIndex = 0,
                resolvedCount = 0
            )
        }
    }

    /**
     * Resolve current conflict with the chosen strategy.
     */
    fun resolveConflict(
        resolution: ConflictResolution,
        resolvedEntity: Map<String, String>
    ) {
        val currentConflict = _state.value.currentConflict ?: run {
            logger.warn { "No current conflict to resolve" }
            return
        }

        logger.debug { "Resolving conflict ${currentConflict.entityId} with strategy $resolution" }

        _state.update { it.copy(isResolving = true, error = null) }

        controllerScope.launch {
            val result = syncCoordinator.resolveConflict(
                conflictId = currentConflict.entityId,
                resolution = resolution,
                resolvedEntity = resolvedEntity
            )

            when {
                result.isSuccess -> {
                    logger.info { "Conflict resolved successfully" }

                    _state.update { state ->
                        state.copy(
                            isResolving = false,
                            resolvedCount = state.resolvedCount + 1,
                            currentConflictIndex = if (state.hasMoreConflicts) {
                                state.currentConflictIndex + 1
                            } else {
                                state.currentConflictIndex
                            }
                        )
                    }
                }
                result.isFailure -> {
                    val error = result.exceptionOrNull()?.message ?: "Failed to resolve conflict"
                    logger.error { "Failed to resolve conflict: $error" }

                    _state.update {
                        it.copy(
                            isResolving = false,
                            error = error
                        )
                    }
                }
            }
        }
    }

    /**
     * Resolve conflict by keeping local version.
     */
    fun resolveKeepLocal() {
        val conflict = _state.value.currentConflict ?: return
        resolveConflict(ConflictResolution.KEEP_LOCAL, conflict.localVersion)
    }

    /**
     * Resolve conflict by keeping remote version.
     */
    fun resolveKeepRemote() {
        val conflict = _state.value.currentConflict ?: return
        resolveConflict(ConflictResolution.KEEP_REMOTE, conflict.remoteVersion)
    }

    /**
     * Resolve conflict by merging versions (use suggested resolution).
     */
    fun resolveMerge() {
        val conflict = _state.value.currentConflict ?: return

        // Merge both versions - combine non-conflicting fields
        val mergedEntity = conflict.localVersion.toMutableMap()
        conflict.remoteVersion.forEach { (key, value) ->
            if (!mergedEntity.containsKey(key)) {
                mergedEntity[key] = value
            }
        }

        resolveConflict(ConflictResolution.MERGE, mergedEntity)
    }

    /**
     * Skip current conflict for manual resolution later.
     */
    fun skipConflict() {
        logger.debug { "Skipping current conflict" }

        _state.update { state ->
            if (state.hasMoreConflicts) {
                state.copy(currentConflictIndex = state.currentConflictIndex + 1)
            } else {
                state
            }
        }
    }

    /**
     * Go to previous conflict.
     */
    fun previousConflict() {
        _state.update { state ->
            if (state.currentConflictIndex > 0) {
                state.copy(currentConflictIndex = state.currentConflictIndex - 1)
            } else {
                state
            }
        }
    }

    /**
     * Go to next conflict.
     */
    fun nextConflict() {
        _state.update { state ->
            if (state.hasMoreConflicts) {
                state.copy(currentConflictIndex = state.currentConflictIndex + 1)
            } else {
                state
            }
        }
    }

    /**
     * Reset to first conflict.
     */
    fun resetToFirst() {
        _state.update { it.copy(currentConflictIndex = 0) }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Clear all conflicts and reset state.
     */
    fun clearConflicts() {
        _state.update { ConflictResolutionState() }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up ConflictResolutionController" }
        controllerScope.cancel()
        logger.debug { "ConflictResolutionController cleanup complete" }
    }
}
