package com.po4yka.trailglass.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.po4yka.trailglass.feature.photo.PhotoDetailController as SharedPhotoDetailController

/**
 * Wrapper for PhotoDetailScreen that adapts shared PhotoDetailController to Android interface. Bridges the shared
 * Kotlin PhotoDetailController to the Android-specific PhotoDetailController interface.
 */
@Composable
fun PhotoDetailScreenWrapper(
    photoId: String,
    photoDetailController: SharedPhotoDetailController,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create adapter that implements Android PhotoDetailController interface
    val androidController =
        object : PhotoDetailController {
            private val adapterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            private val _state =
                MutableStateFlow(
                    PhotoDetailState(
                        photo = null,
                        isLoading = false,
                        error = null
                    )
                )

            override val state: StateFlow<PhotoDetailState> =
                _state.asStateFlow().also {
                    // Collect from shared controller and map to Android state
                    adapterScope.launch {
                        photoDetailController.state.collect { sharedState ->
                            _state.value =
                                PhotoDetailState(
                                    photo = sharedState.photo,
                                    isLoading = sharedState.isLoading,
                                    error = sharedState.error
                                )
                        }
                    }
                }

            override fun loadPhoto(photoId: String) {
                photoDetailController.loadPhoto(photoId = photoId)
            }

            override fun sharePhoto() {
                photoDetailController.sharePhoto()
            }

            override fun deletePhoto() {
                photoDetailController.deletePhoto()
            }

            override fun showAttachmentDialog() {
                photoDetailController.showAttachmentDialog()
            }
        }

    PhotoDetailScreen(
        photoId = photoId,
        controller = androidController,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
