package com.po4yka.trailglass.ui.screens.algorithmsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.algorithm.BearingAlgorithmType
import com.po4yka.trailglass.domain.algorithm.DistanceAlgorithmType
import com.po4yka.trailglass.domain.algorithm.InterpolationAlgorithmType
import com.po4yka.trailglass.domain.model.Settings

/**
 * Introduction card explaining the algorithm settings screen.
 */
@Composable
internal fun IntroductionSection() {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Geographic Algorithms",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text =
                    "Choose calculation methods for distance, bearing, and interpolation. " +
                        "Different algorithms offer trade-offs between accuracy and performance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Section header for a category of settings.
 *
 * @param text The header text to display
 */
@Composable
internal fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/**
 * Distance algorithm selection card.
 *
 * @param settings Current settings containing the selected distance algorithm
 * @param onClick Callback invoked when the card is clicked
 */
@Composable
internal fun DistanceAlgorithmCard(
    settings: Settings,
    onClick: () -> Unit
) {
    AlgorithmCard(
        icon = Icons.Default.Straighten,
        title = "Distance Algorithm",
        currentValue =
            settings.algorithmPreferences.distanceAlgorithm.name.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            },
        description = getDistanceAlgorithmDescription(settings.algorithmPreferences.distanceAlgorithm),
        onClick = onClick
    )
}

/**
 * Bearing algorithm selection card.
 *
 * @param settings Current settings containing the selected bearing algorithm
 * @param onClick Callback invoked when the card is clicked
 */
@Composable
internal fun BearingAlgorithmCard(
    settings: Settings,
    onClick: () -> Unit
) {
    AlgorithmCard(
        icon = Icons.AutoMirrored.Filled.ShowChart,
        title = "Bearing Algorithm",
        currentValue =
            settings.algorithmPreferences.bearingAlgorithm.name
                .replace(
                    '_',
                    ' '
                ).lowercase()
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                },
        description = getBearingAlgorithmDescription(settings.algorithmPreferences.bearingAlgorithm),
        onClick = onClick
    )
}

/**
 * Interpolation algorithm selection card.
 *
 * @param settings Current settings containing the selected interpolation algorithm
 * @param onClick Callback invoked when the card is clicked
 */
@Composable
internal fun InterpolationAlgorithmCard(
    settings: Settings,
    onClick: () -> Unit
) {
    AlgorithmCard(
        icon = Icons.Default.GraphicEq,
        title = "Interpolation Algorithm",
        currentValue =
            settings.algorithmPreferences.interpolationAlgorithm.name
                .lowercase()
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                },
        description =
            getInterpolationAlgorithmDescription(
                settings.algorithmPreferences.interpolationAlgorithm
            ),
        onClick = onClick
    )
}

/**
 * Returns a human-readable description of the given distance algorithm.
 *
 * @param algorithm The distance algorithm to describe
 * @return A description explaining the algorithm's characteristics and use cases
 */
internal fun getDistanceAlgorithmDescription(algorithm: DistanceAlgorithmType): String =
    when (algorithm) {
        DistanceAlgorithmType.HAVERSINE ->
            "Fast and accurate (~0.5% error). Best for most uses. Assumes spherical Earth."

        DistanceAlgorithmType.VINCENTY ->
            "Most accurate (~0.5mm error) but slower. Use for precise measurements. Accounts for Earth's ellipsoidal shape."

        DistanceAlgorithmType.SIMPLE ->
            "Fastest but only accurate for short distances (<1km). Error increases with distance and latitude."
    }

/**
 * Returns a human-readable description of the given bearing algorithm.
 *
 * @param algorithm The bearing algorithm to describe
 * @return A description explaining the algorithm's characteristics and use cases
 */
internal fun getBearingAlgorithmDescription(algorithm: BearingAlgorithmType): String =
    when (algorithm) {
        BearingAlgorithmType.INITIAL ->
            "Direction at start point (most common for navigation). Shows compass heading at departure."

        BearingAlgorithmType.FINAL ->
            "Direction at end point. Useful for arrival headings, can differ from initial bearing on long distances."

        BearingAlgorithmType.RHUMB_LINE ->
            "Constant compass bearing throughout journey. Not the shortest path but simpler navigation."
    }

/**
 * Returns a human-readable description of the given interpolation algorithm.
 *
 * @param algorithm The interpolation algorithm to describe
 * @return A description explaining the algorithm's characteristics and use cases
 */
internal fun getInterpolationAlgorithmDescription(algorithm: InterpolationAlgorithmType): String =
    when (algorithm) {
        InterpolationAlgorithmType.LINEAR ->
            "Simple straight line, fast but not geographically accurate over long distances. Good for animations."

        InterpolationAlgorithmType.SLERP ->
            "Spherical interpolation, follows great circle (most accurate). Best for geographic paths."

        InterpolationAlgorithmType.CUBIC ->
            "Smooth curved path with easing. Aesthetically pleasing for animated transitions."
    }
