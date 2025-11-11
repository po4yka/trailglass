package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Get photos attached to a specific visit for timeline integration.
 */
@Inject
class GetPhotosForVisitUseCase(
    private val photoRepository: PhotoRepository
) {

    private val logger = logger()

    /**
     * Get all photos attached to a visit.
     *
     * @param visitId Place visit ID
     * @return List of photos
     */
    suspend fun execute(visitId: String): List<Photo> {
        logger.debug { "Getting photos for visit $visitId" }

        return try {
            val photos = photoRepository.getPhotosForVisit(visitId)
            logger.debug { "Found ${photos.size} photos for visit $visitId" }
            photos
        } catch (e: Exception) {
            logger.error(e) { "Failed to get photos for visit $visitId" }
            emptyList()
        }
    }
}
