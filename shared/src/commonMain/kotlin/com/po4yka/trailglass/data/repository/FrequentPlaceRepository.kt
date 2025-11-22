package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance

/**
 * Repository for managing frequently visited places.
 */
interface FrequentPlaceRepository {
    /**
     * Insert a new frequent place.
     */
    suspend fun insertPlace(place: FrequentPlace)

    /**
     * Get a frequent place by ID.
     */
    suspend fun getPlaceById(id: String): FrequentPlace?

    /**
     * Get all frequent places for a user.
     */
    suspend fun getPlacesByUser(userId: String): List<FrequentPlace>

    /**
     * Get frequent places by category.
     */
    suspend fun getPlacesByCategory(
        userId: String,
        category: PlaceCategory
    ): List<FrequentPlace>

    /**
     * Get frequent places by significance level.
     */
    suspend fun getPlacesBySignificance(
        userId: String,
        significance: PlaceSignificance
    ): List<FrequentPlace>

    /**
     * Update an existing frequent place.
     */
    suspend fun updatePlace(place: FrequentPlace)

    /**
     * Delete a frequent place.
     */
    suspend fun deletePlace(id: String)

    /**
     * Get favorite frequent places for a user.
     */
    suspend fun getFavoritePlaces(userId: String): List<FrequentPlace>
}
