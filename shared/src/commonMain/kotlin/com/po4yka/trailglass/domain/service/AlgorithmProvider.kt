package com.po4yka.trailglass.domain.service

import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.domain.algorithm.BearingAlgorithm
import com.po4yka.trailglass.domain.algorithm.BearingAlgorithmFactory
import com.po4yka.trailglass.domain.algorithm.DistanceAlgorithm
import com.po4yka.trailglass.domain.algorithm.DistanceAlgorithmFactory
import com.po4yka.trailglass.domain.algorithm.InterpolationAlgorithm
import com.po4yka.trailglass.domain.algorithm.InterpolationAlgorithmFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Provides algorithm instances based on user settings. Allows dynamic switching of algorithms without restarting the
 * app.
 */
class AlgorithmProvider(
    private val settingsRepository: SettingsRepository
) {
    /** Get the currently configured distance algorithm as a Flow. Updates automatically when settings change. */
    fun getDistanceAlgorithm(): Flow<DistanceAlgorithm> =
        settingsRepository
            .getSettings()
            .map { settings ->
                DistanceAlgorithmFactory.create(settings.algorithmPreferences.distanceAlgorithm)
            }

    /** Get the currently configured distance algorithm (non-reactive). */
    suspend fun getCurrentDistanceAlgorithm(): DistanceAlgorithm {
        val settings = settingsRepository.getCurrentSettings()
        return DistanceAlgorithmFactory.create(settings.algorithmPreferences.distanceAlgorithm)
    }

    /** Get the currently configured bearing algorithm as a Flow. Updates automatically when settings change. */
    fun getBearingAlgorithm(): Flow<BearingAlgorithm> =
        settingsRepository
            .getSettings()
            .map { settings ->
                BearingAlgorithmFactory.create(settings.algorithmPreferences.bearingAlgorithm)
            }

    /** Get the currently configured bearing algorithm (non-reactive). */
    suspend fun getCurrentBearingAlgorithm(): BearingAlgorithm {
        val settings = settingsRepository.getCurrentSettings()
        return BearingAlgorithmFactory.create(settings.algorithmPreferences.bearingAlgorithm)
    }

    /** Get the currently configured interpolation algorithm as a Flow. Updates automatically when settings change. */
    fun getInterpolationAlgorithm(): Flow<InterpolationAlgorithm> =
        settingsRepository
            .getSettings()
            .map { settings ->
                InterpolationAlgorithmFactory.create(settings.algorithmPreferences.interpolationAlgorithm)
            }

    /** Get the currently configured interpolation algorithm (non-reactive). */
    suspend fun getCurrentInterpolationAlgorithm(): InterpolationAlgorithm {
        val settings = settingsRepository.getCurrentSettings()
        return InterpolationAlgorithmFactory.create(settings.algorithmPreferences.interpolationAlgorithm)
    }
}
