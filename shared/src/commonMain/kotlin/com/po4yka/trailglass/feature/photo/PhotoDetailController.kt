package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.photo.PhotoMetadataExtractor
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
 * Controller for photo detail view.
 * Handles loading and displaying a single photo with full metadata.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class PhotoDetailController(
    private val photoRepository: PhotoRepository,
    private val metadataExtractor: PhotoMetadataExtractor,
    private val attachPhotoUseCase: AttachPhotoToVisitUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /**
     * Photo detail UI state.
     */
    data class DetailState(
        val photo: PhotoWithMetadata? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val showAttachDialog: Boolean = false,
        val showShareSheet: Boolean = false,
        val showDeleteConfirmation: Boolean = false
    )

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    /**
     * Load photo by ID with full metadata.
     */
    fun loadPhoto(photoId: String) {
        logger.debug { "Loading photo: $photoId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            try {
                val photo = photoRepository.getPhotoById(photoId)

                if (photo == null) {
                    _state.update {
                        it.copy(
                            error = "Photo not found",
                            isLoading = false
                        )
                    }
                    logger.warn { "Photo $photoId not found" }
                    return@launch
                }

                // Check authorization
                if (photo.userId != userId) {
                    _state.update {
                        it.copy(
                            error = "Unauthorized to view this photo",
                            isLoading = false
                        )
                    }
                    logger.warn { "User $userId unauthorized to view photo $photoId (owner: ${photo.userId})" }
                    return@launch
                }

                // Load attachments
                val attachments = photoRepository.getAttachmentsForPhoto(photoId)

                // Extract metadata if not already available
                val metadata =
                    try {
                        metadataExtractor.extractMetadata(photo.uri, photoId)
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to extract metadata for photo $photoId" }
                        null
                    }

                val photoWithMetadata =
                    PhotoWithMetadata(
                        photo = photo,
                        metadata = metadata,
                        attachments = attachments,
                        clusterId = null // Would be loaded from cluster association
                    )

                _state.update {
                    it.copy(
                        photo = photoWithMetadata,
                        isLoading = false
                    )
                }

                logger.info { "Loaded photo $photoId successfully" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load photo $photoId" }
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Show dialog to attach photo to a visit.
     */
    fun showAttachmentDialog() {
        logger.info { "Showing attachment dialog" }
        _state.update { it.copy(showAttachDialog = true) }
    }

    /**
     * Hide attachment dialog.
     */
    fun dismissAttachmentDialog() {
        _state.update { it.copy(showAttachDialog = false) }
    }

    /**
     * Attach photo to a visit.
     */
    fun attachToVisit(
        visitId: String,
        caption: String? = null
    ) {
        val photoId =
            _state.value.photo
                ?.photo
                ?.id
        if (photoId == null) {
            logger.warn { "Cannot attach photo: no photo loaded" }
            return
        }

        logger.debug { "Attaching photo $photoId to visit $visitId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            when (val result = attachPhotoUseCase.execute(photoId, visitId, caption)) {
                is AttachPhotoToVisitUseCase.Result.Success -> {
                    logger.info { "Successfully attached photo $photoId to visit $visitId" }

                    // Reload photo to get updated attachments
                    loadPhoto(photoId)

                    _state.update {
                        it.copy(
                            showAttachDialog = false,
                            isLoading = false
                        )
                    }
                }
                is AttachPhotoToVisitUseCase.Result.AlreadyAttached -> {
                    logger.warn { "Photo $photoId already attached to visit $visitId" }
                    _state.update {
                        it.copy(
                            error = "Photo already attached to this visit",
                            isLoading = false
                        )
                    }
                }
                is AttachPhotoToVisitUseCase.Result.Error -> {
                    logger.error { "Failed to attach photo: ${result.message}" }
                    _state.update {
                        it.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Share photo (platform-specific implementation required).
     */
    fun sharePhoto() {
        logger.info { "User requested to share photo" }
        _state.update { it.copy(showShareSheet = true) }
    }

    /**
     * Hide share sheet.
     */
    fun dismissShareSheet() {
        _state.update { it.copy(showShareSheet = false) }
    }

    /**
     * Show delete confirmation dialog.
     */
    fun showDeleteConfirmation() {
        logger.info { "Showing delete confirmation" }
        _state.update { it.copy(showDeleteConfirmation = true) }
    }

    /**
     * Hide delete confirmation dialog.
     */
    fun dismissDeleteConfirmation() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }

    /**
     * Delete photo.
     */
    fun deletePhoto() {
        val photoId =
            _state.value.photo
                ?.photo
                ?.id
        if (photoId == null) {
            logger.warn { "Cannot delete photo: no photo loaded" }
            return
        }

        logger.debug { "Deleting photo: $photoId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            try {
                // Delete all attachments first
                photoRepository.deleteAttachmentsForPhoto(photoId)

                // Delete the photo
                photoRepository.deletePhoto(photoId)

                logger.info { "Deleted photo $photoId successfully" }

                _state.update {
                    it.copy(
                        isLoading = false,
                        showDeleteConfirmation = false,
                        photo = null // Clear photo after deletion
                    )
                }

                // Platform-specific code should navigate back after deletion
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete photo $photoId" }
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up PhotoDetailController" }
        controllerScope.cancel()
        logger.debug { "PhotoDetailController cleanup complete" }
    }
}
