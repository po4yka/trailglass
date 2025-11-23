package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.PhotoAttachment
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.util.UuidGenerator
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/** Automatically associates photos with place visits based on location and time. */
@Inject
class AutoAssociatePhotosUseCase(
    private val photoRepository: PhotoRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val photoLocationAssociator: PhotoLocationAssociator
) {
    private val logger = logger()

    /**
     * Auto-associate all unattached photos for a user.
     *
     * @param userId User ID
     * @return Result with association statistics
     */
    suspend fun execute(userId: String): AssociationResult {
        logger.info { "Auto-associating photos for user $userId" }

        try {
            // Get all photos for user
            val allPhotos = photoRepository.getPhotosForUser(userId)
            logger.debug { "Found ${allPhotos.size} total photos" }

            // Filter to unattached photos with location
            val unattachedPhotos =
                allPhotos.filter { photo ->
                    val attachments = photoRepository.getAttachmentsForPhoto(photo.id)
                    attachments.isEmpty() && (photo.latitude != null || photo.longitude != null)
                }

            logger.info { "Found ${unattachedPhotos.size} unattached photos with location" }

            if (unattachedPhotos.isEmpty()) {
                return AssociationResult.Success(0, 0)
            }

            // Get all visits for user (using pagination with high limit to get all)
            val visits = placeVisitRepository.getVisitsByUser(userId, limit = Int.MAX_VALUE, offset = 0)
            logger.debug { "Found ${visits.size} total visits" }

            var associatedCount = 0
            var failedCount = 0

            // Try to associate each photo
            unattachedPhotos.forEach { photo ->
                // Create PhotoWithMetadata (without full metadata for now)
                val photoWithMeta =
                    PhotoWithMetadata(
                        photo = photo,
                        metadata = null, // Would load from metadata repository
                        attachments = emptyList(),
                        clusterId = null
                    )

                // Find best matching visit
                val bestMatch =
                    photoLocationAssociator.findBestMatch(
                        photo = photo,
                        metadata = null,
                        visits = visits
                    )

                if (bestMatch != null) {
                    // Create attachment
                    val attachment =
                        PhotoAttachment(
                            id = UuidGenerator.randomUUID(),
                            photoId = photo.id,
                            placeVisitId = bestMatch.id,
                            attachedAt = Clock.System.now(),
                            caption = "Auto-attached"
                        )

                    try {
                        photoRepository.attachPhotoToVisit(attachment)
                        associatedCount++
                        logger.debug {
                            "Associated photo ${photo.id} with visit ${bestMatch.id}"
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to attach photo ${photo.id}" }
                        failedCount++
                    }
                } else {
                    failedCount++
                }
            }

            logger.info {
                "Auto-association complete: $associatedCount associated, $failedCount failed"
            }

            return AssociationResult.Success(associatedCount, failedCount)
        } catch (e: Exception) {
            logger.error(e) { "Failed to auto-associate photos" }
            return AssociationResult.Error(e.message ?: "Unknown error")
        }
    }

    sealed class AssociationResult {
        data class Success(
            val associatedCount: Int,
            val failedCount: Int
        ) : AssociationResult()

        data class Error(
            val message: String
        ) : AssociationResult()
    }
}
