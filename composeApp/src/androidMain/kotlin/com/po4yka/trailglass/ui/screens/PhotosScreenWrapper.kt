package com.po4yka.trailglass.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.po4yka.trailglass.domain.model.PhotoGroup
import com.po4yka.trailglass.feature.photo.PhotoController
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Wrapper for PhotoGalleryScreen that adapts PhotoController to PhotoGalleryController interface.
 */
@Composable
fun PhotosScreenWrapper(
    photoController: PhotoController,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val controller = object : PhotoGalleryController {
        override val state: StateFlow<PhotoGalleryState> = kotlinx.coroutines.flow.MutableStateFlow(
            PhotoGalleryState(
                photoGroups = emptyList(),
                isLoading = false,
                error = null
            )
        ).apply {
            // Map PhotoController state to PhotoGalleryState
            kotlinx.coroutines.GlobalScope.launch {
                photoController.state.collect { photoState ->
                    value = PhotoGalleryState(
                        photoGroups = if (photoState.photos.isNotEmpty()) {
                            listOf(PhotoGroup(
                                date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                photos = photoState.photos.map { photo ->
                                    com.po4yka.trailglass.domain.model.PhotoWithMetadata(
                                        photo = photo,
                                        metadata = null,
                                        attachments = emptyList(),
                                        clusterId = null
                                    )
                                }
                            ))
                        } else {
                            emptyList()
                        },
                        isLoading = photoState.isLoading,
                        error = photoState.error
                    )
                }
            }
        }

        override fun loadGallery() {
            // Load photos for the current date
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            photoController.loadPhotosForDay(today)
        }

        override fun importPhotos() {
            photoController.requestSelectFromLibrary()
        }

        override fun refresh() {
            loadGallery()
        }
    }

    PhotoGalleryScreen(
        controller = controller,
        onPhotoClick = onPhotoClick,
        modifier = modifier
    )
}
