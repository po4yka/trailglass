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
import com.po4yka.trailglass.feature.photo.PhotoGalleryController as SharedPhotoGalleryController

/**
 * Wrapper for PhotoGalleryScreen that adapts shared PhotoGalleryController to Android interface. Bridges the shared
 * Kotlin PhotoGalleryController to the Android-specific PhotoGalleryController interface.
 */
@Composable
fun PhotosScreenWrapper(
    photoGalleryController: SharedPhotoGalleryController,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Create adapter that implements Android PhotoGalleryController interface
    val androidController =
        object : PhotoGalleryController {
            private val adapterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            private val _state =
                MutableStateFlow(
                    PhotoGalleryState(
                        photoGroups = emptyList(),
                        isLoading = false,
                        error = null
                    )
                )

            override val state: StateFlow<PhotoGalleryState> =
                _state.asStateFlow().also {
                    // Collect from shared controller and map to Android state
                    adapterScope.launch {
                        photoGalleryController.state.collect { sharedState ->
                            _state.value =
                                PhotoGalleryState(
                                    photoGroups = sharedState.photoGroups,
                                    isLoading = sharedState.isLoading,
                                    error = sharedState.error,
                                    viewMode = sharedState.viewMode
                                )
                        }
                    }
                }

            override fun loadGallery() {
                photoGalleryController.loadGallery()
            }

            override fun importPhotos() {
                photoGalleryController.importPhotos()
            }

            override fun refresh() {
                photoGalleryController.refresh()
            }

            override fun toggleViewMode() {
                photoGalleryController.toggleViewMode()
            }
        }

    PhotoGalleryScreen(
        controller = androidController,
        onPhotoClick = onPhotoClick,
        modifier = modifier
    )
}
