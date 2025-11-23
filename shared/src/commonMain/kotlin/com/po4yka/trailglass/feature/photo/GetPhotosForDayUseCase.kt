package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

/** Use case for getting photos taken on a specific day. */
@Inject
class GetPhotosForDayUseCase(
    private val photoRepository: PhotoRepository
) {
    private val logger = logger()

    /**
     * Get all photos taken on a specific day.
     *
     * @param date The date to get photos for
     * @param userId User ID
     * @return List of photos sorted by timestamp
     */
    suspend fun execute(
        date: LocalDate,
        userId: String
    ): List<Photo> {
        logger.debug { "Getting photos for $date, user: $userId" }

        val photos = photoRepository.getPhotosForDay(userId, date)

        logger.info { "Found ${photos.size} photos for $date" }
        return photos
    }
}
