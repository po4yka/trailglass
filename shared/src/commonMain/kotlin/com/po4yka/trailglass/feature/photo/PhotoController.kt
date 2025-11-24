package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.permission.PermissionResult
import com.po4yka.trailglass.domain.permission.PermissionType
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.feature.permission.PermissionFlowController
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

/**
 * Controller for photo management. Handles photo loading, selection, capture, and attachment with permission
 * management.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class PhotoController(
    private val getPhotosForDayUseCase: GetPhotosForDayUseCase,
    private val suggestPhotosUseCase: SuggestPhotosForVisitUseCase,
    private val attachPhotoUseCase: AttachPhotoToVisitUseCase,
    val importPhotoUseCase: ImportPhotoUseCase, // Exposed for UI
    private val permissionFlow: PermissionFlowController,
    coroutineScope: CoroutineScope,
    val userId: String // Exposed for UI
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** Photo action that is pending permission grant. */
    enum class PendingPhotoAction {
        TAKE_PHOTO,
        SELECT_FROM_LIBRARY
    }

    /** Photo UI state. */
    data class PhotoState(
        val selectedDate: LocalDate? = null,
        val photos: List<Photo> = emptyList(),
        val suggestedPhotos: List<Photo> = emptyList(),
        val selectedVisit: PlaceVisit? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val pendingAction: PendingPhotoAction? = null,
        val hasCameraPermission: Boolean = false,
        val hasPhotoLibraryPermission: Boolean = false
    )

    private val _state = MutableStateFlow(PhotoState())
    val state: StateFlow<PhotoState> = _state.asStateFlow()

    init {
        // Observe permission results
        controllerScope.launch {
            permissionFlow.state.collect { permState ->
                when (permState.lastResult) {
                    is PermissionResult.Granted -> {
                        logger.info { "Photo permission granted" }
                        val pendingAction = _state.value.pendingAction
                        if (pendingAction != null) {
                            _state.update {
                                it.copy(
                                    pendingAction = null,
                                    hasCameraPermission =
                                        permState.currentRequest?.permissionType == PermissionType.CAMERA,
                                    hasPhotoLibraryPermission =
                                        permState.currentRequest?.permissionType == PermissionType.PHOTO_LIBRARY
                                )
                            }
                            // Execute the pending action
                            when (pendingAction) {
                                PendingPhotoAction.TAKE_PHOTO -> executePhotoCapture()
                                PendingPhotoAction.SELECT_FROM_LIBRARY -> executePhotoSelection()
                            }
                        }
                    }

                    is PermissionResult.Denied,
                    is PermissionResult.PermanentlyDenied -> {
                        logger.warn { "Photo permission denied" }
                        _state.update {
                            it.copy(
                                pendingAction = null,
                                error =
                                    when (permState.currentRequest?.permissionType) {
                                        PermissionType.CAMERA -> "Camera permission is required to take photos"
                                        PermissionType.PHOTO_LIBRARY -> "Photo library permission is required to select photos"
                                        else -> "Permission denied"
                                    }
                            )
                        }
                    }

                    is PermissionResult.Cancelled -> {
                        logger.info { "Photo permission request cancelled" }
                        _state.update { it.copy(pendingAction = null) }
                    }

                    is PermissionResult.Error -> {
                        logger.error { "Photo permission error: ${permState.lastResult.message}" }
                        _state.update {
                            it.copy(
                                pendingAction = null,
                                error = permState.lastResult.message
                            )
                        }
                    }

                    null -> {
                        // No result yet
                    }
                }
            }
        }

        // Check permissions on init
        checkPermissions()
    }

    /** Load photos for a specific day. */
    fun loadPhotosForDay(date: LocalDate) {
        logger.debug { "Loading photos for $date" }

        _state.update { it.copy(isLoading = true, selectedDate = date, error = null) }

        controllerScope.launch {
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

    /** Load suggested photos for a visit. This requires photo library permission. */
    fun loadSuggestionsForVisit(visit: PlaceVisit) {
        logger.debug { "Loading photo suggestions for visit ${visit.id}" }

        controllerScope.launch {
            val hasPermission = permissionFlow.isPermissionGranted(PermissionType.PHOTO_LIBRARY)

            if (!hasPermission) {
                logger.info { "Photo library permission not granted, requesting..." }
                _state.update {
                    it.copy(
                        selectedVisit = visit,
                        error = null
                    )
                }
                permissionFlow.startPermissionFlow(PermissionType.PHOTO_LIBRARY)
                return@launch
            }

            _state.update { it.copy(isLoading = true, selectedVisit = visit, error = null) }

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

    /** Take a photo using the camera. This will request camera permission if not already granted. */
    fun takePhoto() {
        logger.info { "User requested to take photo" }

        controllerScope.launch {
            val hasPermission = permissionFlow.isPermissionGranted(PermissionType.CAMERA)

            if (hasPermission) {
                executePhotoCapture()
            } else {
                logger.info { "Camera permission not granted, requesting..." }
                _state.update {
                    it.copy(
                        pendingAction = PendingPhotoAction.TAKE_PHOTO,
                        error = null
                    )
                }
                permissionFlow.startPermissionFlow(PermissionType.CAMERA)
            }
        }
    }

    /** Select a photo from the photo library. This will request photo library permission if not already granted. */
    fun selectFromLibrary() {
        logger.info { "User requested to select from library" }

        controllerScope.launch {
            val hasPermission = permissionFlow.isPermissionGranted(PermissionType.PHOTO_LIBRARY)

            if (hasPermission) {
                executePhotoSelection()
            } else {
                logger.info { "Photo library permission not granted, requesting..." }
                _state.update {
                    it.copy(
                        pendingAction = PendingPhotoAction.SELECT_FROM_LIBRARY,
                        error = null
                    )
                }
                permissionFlow.startPermissionFlow(PermissionType.PHOTO_LIBRARY)
            }
        }
    }

    /**
     * Execute photo capture after permission is granted. Platform-specific implementation would handle the actual
     * camera capture.
     */
    private fun executePhotoCapture() {
        logger.info { "Executing photo capture" }
        // Platform-specific implementation would:
        // 1. Open camera
        // 2. Capture photo
        // 3. Save to file
        // 4. Call addPhoto() with file path

        // For now, just log that we would open the camera
        logger.debug { "Camera would be opened here (platform-specific)" }
    }

    /**
     * Execute photo selection after permission is granted. Platform-specific implementation would handle the actual
     * picker.
     */
    private fun executePhotoSelection() {
        logger.info { "Executing photo selection" }
        // Platform-specific implementation would:
        // 1. Open photo picker
        // 2. Let user select photo
        // 3. Copy to app storage
        // 4. Call addPhoto() with file path

        // For now, just log that we would open the picker
        logger.debug { "Photo picker would be opened here (platform-specific)" }
    }

    /** Add a photo that was captured or selected. Called by platform-specific code after photo is obtained. */
    fun addPhoto(photo: Photo) {
        logger.info { "Adding photo: ${photo.id}" }
        _state.update { it.copy(photos = it.photos + photo) }
    }

    /** Attach a photo to the current visit. */
    fun attachPhotoToVisit(
        photoId: String,
        caption: String? = null
    ) {
        val visit = _state.value.selectedVisit
        if (visit == null) {
            logger.warn { "No visit selected for photo attachment" }
            return
        }

        logger.debug { "Attaching photo $photoId to visit ${visit.id}" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
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

    /** Check if permissions are granted. */
    fun checkPermissions() {
        controllerScope.launch {
            val hasCameraPermission = permissionFlow.isPermissionGranted(PermissionType.CAMERA)
            val hasPhotoLibraryPermission = permissionFlow.isPermissionGranted(PermissionType.PHOTO_LIBRARY)

            _state.update {
                it.copy(
                    hasCameraPermission = hasCameraPermission,
                    hasPhotoLibraryPermission = hasPhotoLibraryPermission
                )
            }

            logger.debug { "Permissions check - Camera: $hasCameraPermission, Library: $hasPhotoLibraryPermission" }
        }
    }

    /** Refresh current view. */
    fun refresh() {
        _state.value.selectedDate?.let { date ->
            loadPhotosForDay(date)
        }
        _state.value.selectedVisit?.let { visit ->
            loadSuggestionsForVisit(visit)
        }
    }

    /** Clear error state. */
    fun clearError() {
        _state.update { it.copy(error = null) }
        permissionFlow.clearError()
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up PhotoController" }
        controllerScope.cancel()
        logger.debug { "PhotoController cleanup complete" }
    }
}
