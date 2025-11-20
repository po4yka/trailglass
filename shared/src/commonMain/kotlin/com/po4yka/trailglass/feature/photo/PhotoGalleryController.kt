package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.domain.model.PhotoGroup
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
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

/**
 * Controller for photo gallery view.
 * Handles loading and displaying photos grouped by date.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class PhotoGalleryController(
    private val getPhotoGalleryUseCase: GetPhotoGalleryUseCase,
    private val importPhotoUseCase: ImportPhotoUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {

    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /**
     * Photo gallery UI state.
     */
    data class GalleryState(
        val photoGroups: List<PhotoGroup> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showImportDialog: Boolean = false
    )

    private val _state = MutableStateFlow(GalleryState())
    val state: StateFlow<GalleryState> = _state.asStateFlow()

    /**
     * Load photo gallery for recent time period (last 30 days).
     */
    fun loadGallery() {
        logger.debug { "Loading photo gallery" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            try {
                // Load photos from last 30 days
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                val endDate = now.toLocalDateTime(timeZone).date
                val startDate = endDate.minus(30, kotlinx.datetime.DateTimeUnit.DAY)

                val photoGroups = getPhotoGalleryUseCase.execute(
                    userId = userId,
                    startDate = startDate,
                    endDate = endDate
                )

                _state.update {
                    it.copy(
                        photoGroups = photoGroups,
                        isLoading = false
                    )
                }

                logger.info { "Loaded ${photoGroups.size} photo groups" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load photo gallery" }
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
     * Load all photos (no time limit).
     */
    fun loadAllPhotos() {
        logger.debug { "Loading all photos" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            try {
                val photosByMonth = getPhotoGalleryUseCase.getAllPhotosGroupedByMonth(userId)

                // Flatten to photo groups (convert YearMonth to PhotoGroup)
                val photoGroups = photosByMonth.flatMap { (yearMonth, photos) ->
                    // Group photos by exact date within the month
                    photos.groupBy {
                        it.photo.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }.map { (date, photosForDate) ->
                        PhotoGroup(
                            date = date,
                            photos = photosForDate,
                            location = null
                        )
                    }
                }.sortedByDescending { it.date }

                _state.update {
                    it.copy(
                        photoGroups = photoGroups,
                        isLoading = false
                    )
                }

                logger.info { "Loaded ${photoGroups.size} photo groups (all time)" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load all photos" }
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
     * Show import dialog.
     */
    fun importPhotos() {
        logger.info { "User requested to import photos" }
        _state.update { it.copy(showImportDialog = true) }
    }

    /**
     * Hide import dialog.
     */
    fun dismissImportDialog() {
        _state.update { it.copy(showImportDialog = false) }
    }

    /**
     * Refresh gallery.
     */
    fun refresh() {
        logger.debug { "Refreshing gallery" }
        loadGallery()
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
        logger.info { "Cleaning up PhotoGalleryController" }
        controllerScope.cancel()
        logger.debug { "PhotoGalleryController cleanup complete" }
    }
}
