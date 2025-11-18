package com.po4yka.trailglass.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.feature.photo.PhotoController
import kotlinx.coroutines.flow.StateFlow

/**
 * Wrapper for PhotoDetailScreen that adapts PhotoController to PhotoDetailController interface.
 */
@Composable
fun PhotoDetailScreenWrapper(
    photoId: String,
    photoController: PhotoController,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controller = object : PhotoDetailController {
        override val state: StateFlow<PhotoDetailState> = kotlinx.coroutines.flow.MutableStateFlow(
            PhotoDetailState(
                photo = null,
                isLoading = false,
                error = null
            )
        ).apply {
            // Map PhotoController state to PhotoDetailState
            kotlinx.coroutines.GlobalScope.launch {
                photoController.state.collect { photoState ->
                    val matchingPhoto = photoState.photos.find { it.id == photoId }
                    value = PhotoDetailState(
                        photo = matchingPhoto?.let { photo ->
                            PhotoWithMetadata(
                                photo = photo,
                                metadata = null,
                                attachments = emptyList(),
                                clusterId = null
                            )
                        },
                        isLoading = photoState.isLoading,
                        error = photoState.error
                    )
                }
            }
        }

        override fun loadPhoto(photoId: String) {
            // Photo is already loaded via PhotoController state
            // In a real implementation, this would fetch a specific photo
        }

        override fun sharePhoto() {
            // TODO: Implement share functionality
        }

        override fun deletePhoto() {
            // TODO: Implement delete functionality
        }

        override fun showAttachmentDialog() {
            // TODO: Implement attachment dialog
        }
    }

    PhotoDetailScreen(
        photoId = photoId,
        controller = controller,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
