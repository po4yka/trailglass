package com.po4yka.trailglass.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define Outfit Font Family
val OutfitFontFamily = FontFamily(
    Font(com.po4yka.trailglass.R.font.outfit, FontWeight.Normal),
    Font(com.po4yka.trailglass.R.font.outfit, FontWeight.Medium),
    Font(com.po4yka.trailglass.R.font.outfit, FontWeight.SemiBold),
    Font(com.po4yka.trailglass.R.font.outfit, FontWeight.Bold)
)

// Material 3 Baseline Typography scale
val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp
            ),
        displayMedium =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp
            ),
        displaySmall =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp
            ),
        headlineLarge =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            ),
        headlineMedium =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            ),
        headlineSmall =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp
            ),
        titleLarge =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
        titleMedium =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            ),
        titleSmall =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
        bodyLarge =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
        bodyMedium =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
        bodySmall =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            ),
        labelLarge =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
        labelMedium =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
        labelSmall =
            TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
    )

/**
 * Material 3 Expressive Emphasized Typography
 *
 * Provides emphasized variants of baseline typography for visual hierarchy and emphasis. Emphasized styles feature:
 * - Slightly larger font sizes (1-2 sp increase)
 * - Bolder font weights (e.g., Normal → Medium, Medium → SemiBold)
 * - Optionally increased letter spacing for prominence
 *
 * Usage:
 * ```kotlin
 * Text(
 *     text = "Emphasized Headline",
 *     style = MaterialTheme.typography.emphasized.headlineLarge
 * )
 * ```
 */
data class EmphasizedTypography(
    val displayLargeEmphasized: TextStyle,
    val displayMediumEmphasized: TextStyle,
    val displaySmallEmphasized: TextStyle,
    val headlineLargeEmphasized: TextStyle,
    val headlineMediumEmphasized: TextStyle,
    val headlineSmallEmphasized: TextStyle,
    val titleLargeEmphasized: TextStyle,
    val titleMediumEmphasized: TextStyle,
    val titleSmallEmphasized: TextStyle,
    val bodyLargeEmphasized: TextStyle,
    val bodyMediumEmphasized: TextStyle,
    val labelLargeEmphasized: TextStyle,
    val labelMediumEmphasized: TextStyle
)

/**
 * Extension property to access emphasized typography variants from MaterialTheme.
 *
 * Example usage:
 * ```kotlin
 * // In a Composable function
 * val emphasizedStyle = MaterialTheme.typography.emphasized.titleLarge
 *
 * Text(
 *     text = "Important Title",
 *     style = emphasizedStyle
 * )
 *
 * // Or inline
 * Text(
 *     text = "Critical Alert",
 *     style = MaterialTheme.typography.emphasized.headlineMedium
 * )
 * ```
 *
 * Recommended use cases:
 * - Hero sections and primary headlines
 * - Call-to-action text
 * - Important notifications or alerts
 * - Key metrics or statistics
 * - Section headers that need extra prominence
 */
val Typography.emphasized: EmphasizedTypography
    @Composable
    @ReadOnlyComposable
    get() =
        EmphasizedTypography(
            // Display styles: Normal → Medium weight, +2sp size
            displayLargeEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 59.sp,
                    lineHeight = 66.sp,
                    letterSpacing = (-0.2).sp
                ),
            displayMediumEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 47.sp,
                    lineHeight = 54.sp,
                    letterSpacing = 0.sp
                ),
            displaySmallEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 38.sp,
                    lineHeight = 46.sp,
                    letterSpacing = 0.sp
                ),
            // Headline styles: Normal → Medium weight, +2sp size
            headlineLargeEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    letterSpacing = 0.sp
                ),
            headlineMediumEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    letterSpacing = 0.sp
                ),
            headlineSmallEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp,
                    lineHeight = 34.sp,
                    letterSpacing = 0.sp
                ),
            // Title styles: Medium → SemiBold weight, +1sp size, slight letter spacing increase
            titleLargeEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 23.sp,
                    lineHeight = 29.sp,
                    letterSpacing = 0.05.sp
                ),
            titleMediumEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    letterSpacing = 0.2.sp
                ),
            titleSmallEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.15.sp
                ),
            // Body styles: Normal → Medium weight, +1sp size
            bodyLargeEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    letterSpacing = 0.55.sp
                ),
            bodyMediumEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.3.sp
                ),
            // Label styles: Medium → SemiBold weight, +1sp size
            labelLargeEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.15.sp
                ),
            labelMediumEmphasized =
                TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    letterSpacing = 0.55.sp
                )
        )
