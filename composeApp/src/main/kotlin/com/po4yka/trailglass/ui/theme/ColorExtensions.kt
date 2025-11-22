package com.po4yka.trailglass.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Extended color palette for TrailGlass beyond Material 3 ColorScheme.
 * Provides semantic access to success, warning, route, category, and visualization colors.
 */
data class ExtendedColors(
    // Success states
    val success: Color,
    val successEmphasis: Color,
    val onSuccess: Color,
    // Warning states
    val warning: Color,
    val warningEmphasis: Color,
    val onWarning: Color,
    // Disabled states
    val disabled: Color,
    val onDisabled: Color,
    // Route colors
    val activeRoute: Color,
    val historicalRoute: Color,
    val alternativeRoute: Color,
    // Category colors
    val neutralCategory: Color,
    val waterCategory: Color,
    val eveningCategory: Color,
    val morningCategory: Color,
    // Data visualization gradient
    val gradientColors: List<Color>
)

val LightExtendedColors =
    ExtendedColors(
        success = SeaGlass,
        successEmphasis = SageGreen,
        onSuccess = Color.White,
        warning = Driftwood,
        warningEmphasis = WeatheredBrass,
        onWarning = JetBlack,
        disabled = Mist,
        onDisabled = StoneGray,
        activeRoute = CoastalPath,
        historicalRoute = HarborBlue,
        alternativeRoute = MistyLavender,
        neutralCategory = StoneGray,
        waterCategory = SeafoamTint,
        eveningCategory = DuskPurple,
        morningCategory = SunrisePeach,
        gradientColors =
            listOf(
                GradientStart,
                GradientStep1,
                GradientStep2,
                GradientStep3,
                GradientEnd
            )
    )

val DarkExtendedColors =
    ExtendedColors(
        success = SageGreen,
        successEmphasis = SeaGlass,
        onSuccess = Color.White,
        warning = WeatheredBrass,
        warningEmphasis = Driftwood,
        onWarning = Color.White,
        disabled = Charcoal,
        onDisabled = StoneGray,
        activeRoute = CoastalPath,
        historicalRoute = HarborBlue,
        alternativeRoute = MistyLavender,
        neutralCategory = StoneGray,
        waterCategory = SeafoamTint,
        eveningCategory = DuskPurple,
        morningCategory = SunrisePeach,
        gradientColors =
            listOf(
                GradientStart,
                GradientStep1,
                GradientStep2,
                GradientStep3,
                GradientEnd
            )
    )

/**
 * Extension property to access extended colors from Material Theme.
 * Usage: MaterialTheme.extendedColors.success
 */
val ColorScheme.extended: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() =
        if (this.surface == SurfaceLight) {
            LightExtendedColors
        } else {
            DarkExtendedColors
        }
