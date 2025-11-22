package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.repository.FrequentPlaceRepository
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
import me.tatarka.inject.annotations.Inject

@Inject
class FrequentPlaceRepositoryImpl : FrequentPlaceRepository {
    private val places = mutableMapOf<String, FrequentPlace>()

    override suspend fun insertPlace(place: FrequentPlace) {
        places[place.id] = place
    }

    override suspend fun getPlaceById(id: String): FrequentPlace? = places[id]

    override suspend fun getPlacesByUser(userId: String): List<FrequentPlace> {
        // In a real impl, we'd filter by userId. Here we assume single user or just return all for now.
        return places.values.toList()
    }

    override suspend fun getPlacesByCategory(
        userId: String,
        category: PlaceCategory
    ): List<FrequentPlace> =
        places.values.filter {
            it.category == category
        }

    override suspend fun getPlacesBySignificance(
        userId: String,
        significance: PlaceSignificance
    ): List<FrequentPlace> =
        places.values.filter {
            it.significance == significance
        }

    override suspend fun updatePlace(place: FrequentPlace) {
        places[place.id] = place
    }

    override suspend fun deletePlace(id: String) {
        places.remove(id)
    }

    override suspend fun getFavoritePlaces(userId: String): List<FrequentPlace> = places.values.filter { it.isFavorite }
}
