package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

/**
 * Controller for photo management.
 */
@Inject
class PhotoController(
    private val getPhotosForDayUseCase: GetPhotosForDayUseCase,
    private val suggestPhotosUseCase: SuggestPhotosForVisitUseCase,
    private val attachPhotoUseCase: AttachPhotoToVisitUseCase,
    private val coroutineScope: CoroutineScope,
    private val userId: String
) {

    private val logger = logger()

    /**
     * Photo UI state.
     */
    data class PhotoState(
        val selectedDate: LocalDate? = null,
        val photos: List<Photo> = emptyList(),
        val suggestedPhotos: List<Photo> = emptyList(),
        val selectedVisit: PlaceVisit? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(PhotoState())
    val state: StateFlow<PhotoState> = _state.asStateFlow()

    /**
     * Load photos for a specific day.
     */
    fun loadPhotosForDay(date: LocalDate) {
        logger.debug { "Loading photos for $date" }

        _state.update { it.copy(isLoading = true, selectedDate = date, error = null) }

        coroutineScope.launch {
            try {
                val photos = getPhotosForDayUseCase.execute(date, userId)
                _state.update { it.copy(photos = photos, isLoading = false) }
                logger.info { "Loaded ${photos.size} photos for $date" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load photos for $date" }
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Load suggested photos for a visit.
     */
    fun loadSuggestionsForVisit(visit: PlaceVisit) {
        logger.debug { "Loading photo suggestions for visit ${visit.id}" }

        _state.update { it.copy(isLoading = true, selectedVisit = visit, error = null) }

        coroutineScope.launch {
            try {
                val suggested = suggestPhotosUseCase.execute(visit, userId)
                _state.update { it.copy(suggestedPhotos = suggested, isLoading = false) }
                logger.info { "Loaded ${suggested.size} suggested photos for visit ${visit.id}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load suggestions for visit ${visit.id}" }
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Attach a photo to the current visit.
     */
    fun attachPhotoToVisit(photoId: String, caption: String? = null) {
        val visit = _state.value.selectedVisit
        if (visit == null) {
            logger.warn { "No visit selected for photo attachment" }
            return
        }

        logger.debug { "Attaching photo $photoId to visit ${visit.id}" }

        _state.update { it.copy(isLoading = true, error = null) }

        coroutineScope.launch {
            when (val result = attachPhotoUseCase.execute(photoId, visit.id, caption)) {
                is AttachPhotoToVisitUseCase.Result.Success -> {
                    logger.info { "Successfully attached photo $photoId" }
                    _state.update { it.copy(isLoading = false) }
                    // Refresh suggestions to remove attached photo
                    loadSuggestionsForVisit(visit)
                }
                is AttachPhotoToVisitUseCase.Result.AlreadyAttached -> {
                    logger.warn { "Photo $photoId already attached" }
                    _state.update { it.copy(error = "Photo already attached", isLoading = false) }
                }
                is AttachPhotoToVisitUseCase.Result.Error -> {
                    logger.error { "Failed to attach photo: ${result.message}" }
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
            }
        }
    }

    /**
     * Refresh current view.
     */
    fun refresh() {
        _state.value.selectedDate?.let { date ->
            loadPhotosForDay(date)
        }
        _state.value.selectedVisit?.let { visit ->
            loadSuggestionsForVisit(visit)
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
