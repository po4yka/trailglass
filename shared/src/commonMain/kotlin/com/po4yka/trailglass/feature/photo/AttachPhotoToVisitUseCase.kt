package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.PhotoAttachment
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.util.UuidGenerator
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Use case for attaching a photo to a place visit.
 */
@Inject
class AttachPhotoToVisitUseCase(
    private val photoRepository: PhotoRepository
) {
    private val logger = logger()

    /**
     * Attach a photo to a place visit.
     *
     * @param photoId Photo ID
     * @param visitId Place visit ID
     * @param caption Optional caption for the attachment
     * @return Result of the operation
     */
    suspend fun execute(
        photoId: String,
        visitId: String,
        caption: String? = null
    ): Result {
        logger.debug { "Attaching photo $photoId to visit $visitId" }

        // Check if already attached
        val isAttached = photoRepository.isPhotoAttachedToVisit(photoId, visitId)
        if (isAttached) {
            logger.warn { "Photo $photoId is already attached to visit $visitId" }
            return Result.AlreadyAttached
        }

        return try {
            val attachment =
                PhotoAttachment(
                    id = "attachment_${UuidGenerator.randomUUID()}",
                    photoId = photoId,
                    placeVisitId = visitId,
                    attachedAt = Clock.System.now(),
                    caption = caption
                )

            photoRepository.attachPhotoToVisit(attachment)
            logger.info { "Successfully attached photo $photoId to visit $visitId" }
            Result.Success
        } catch (e: Exception) {
            logger.error(e) { "Failed to attach photo $photoId to visit $visitId" }
            Result.Error(e.message ?: "Unknown error")
        }
    }

    sealed class Result {
        object Success : Result()

        object AlreadyAttached : Result()

        data class Error(
            val message: String
        ) : Result()
    }
}
