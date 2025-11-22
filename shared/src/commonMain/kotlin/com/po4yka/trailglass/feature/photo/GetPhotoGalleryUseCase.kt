package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.PhotoGroup
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

/**
 * Use case for getting photos grouped by date for gallery display.
 */
@Inject
class GetPhotoGalleryUseCase(
    private val photoRepository: PhotoRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val photoClusterer: PhotoClusterer,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    private val logger = logger()

    /**
     * Get photos grouped by date for a time range.
     *
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of photo groups sorted by date (newest first)
     */
    suspend fun execute(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PhotoGroup> {
        logger.info { "Getting photo gallery from $startDate to $endDate for user $userId" }

        // Get photos in range
        val startTime = startDate.atStartOfDayIn(timeZone)
        val endTime = endDate.atTime(23, 59, 59).toInstant(timeZone)

        val photos = photoRepository.getPhotosInTimeRange(userId, startTime, endTime)
        logger.debug { "Found ${photos.size} photos in range" }

        if (photos.isEmpty()) {
            return emptyList()
        }

        // Get attachments for all photos
        val photosWithMetadata =
            photos.map { photo ->
                val attachments = photoRepository.getAttachmentsForPhoto(photo.id)
                PhotoWithMetadata(
                    photo = photo,
                    metadata = null, // Would load from metadata repository
                    attachments = attachments,
                    clusterId = null
                )
            }

        // Group by date
        val photosByDate =
            photosWithMetadata.groupBy { photoWithMeta ->
                photoWithMeta.photo.timestamp
                    .toLocalDateTime(timeZone)
                    .date
            }

        // Create photo groups
        val photoGroups =
            photosByDate
                .map { (date, photos) ->
                    // Try to determine location name from most common city
                    val cities =
                        photos.mapNotNull { photo ->
                            // Would get from reverse geocoding or visit association
                            null as String? // Placeholder
                        }
                    val mostCommonCity =
                        cities
                            .groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.key

                    PhotoGroup(
                        date = date,
                        photos = photos.sortedBy { it.photo.timestamp },
                        location = mostCommonCity
                    )
                }.sortedByDescending { it.date }

        logger.info { "Created ${photoGroups.size} photo groups" }

        return photoGroups
    }

    /**
     * Get all photos for a user, grouped by month.
     */
    suspend fun getAllPhotosGroupedByMonth(userId: String): Map<YearMonth, List<PhotoWithMetadata>> {
        logger.info { "Getting all photos grouped by month for user $userId" }

        val photos = photoRepository.getPhotosForUser(userId)
        logger.debug { "Found ${photos.size} total photos" }

        val photosWithMetadata =
            photos.map { photo ->
                val attachments = photoRepository.getAttachmentsForPhoto(photo.id)
                PhotoWithMetadata(
                    photo = photo,
                    metadata = null,
                    attachments = attachments,
                    clusterId = null
                )
            }

        val grouped =
            photosWithMetadata.groupBy { photoWithMeta ->
                val dateTime = photoWithMeta.photo.timestamp.toLocalDateTime(timeZone)
                YearMonth(dateTime.year, dateTime.month)
            }

        // Sort by key (YearMonth) descending
        return grouped
            .toList()
            .sortedByDescending { it.first }
            .toMap()
    }

    data class YearMonth(
        val year: Int,
        val month: Month
    ) : Comparable<YearMonth> {
        override fun compareTo(other: YearMonth): Int =
            if (year != other.year) {
                year.compareTo(other.year)
            } else {
                month.compareTo(other.month)
            }

        override fun toString(): String = "${month.name.lowercase().replaceFirstChar { it.uppercase() }} $year"
    }
}
