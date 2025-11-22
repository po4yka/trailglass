package com.po4yka.trailglass.ui.theme

import androidx.compose.ui.graphics.Color

// Silent Waters color palette
// Dewy blues and weathered gray merge into a hushed palette,
// like drifting fog on silent waters.

// MARK: - Base Palette Colors
val LightCyan = Color(0xFFE0FBFC)      // Delicate, sunlit blue tint
val LightBlue = Color(0xFFC2DFE3)       // Soft pastel blue
val CoolSteel = Color(0xFF9DB4C0)       // Cool, steely blue with hint of mist
val BlueSlate = Color(0xFF5C6B73)       // Blue with hints of cool authority
val JetBlack = Color(0xFF253237)        // Intense darkness with enigmatic appeal

// MARK: - Success/Confirmation Colors
val SeaGlass = Color(0xFF8BB5A1)        // Muted teal-green for success states
val SageGreen = Color(0xFF6A8E7F)       // Deeper confirmation color

// MARK: - Warning/Attention Colors
val Driftwood = Color(0xFFC9B896)       // Warm sand tone for warnings
val WeatheredBrass = Color(0xFFA89968)  // Deeper attention color

// MARK: - Map/Route Colors
val CoastalPath = Color(0xFF7A9CAF)     // Active route/current trip
val HarborBlue = Color(0xFF5C8AA8)      // Historical routes
val MistyLavender = Color(0xFFA8B5C7)   // Alternative path suggestion

// MARK: - Category/Tag Colors
val StoneGray = Color(0xFF8C979E)       // Neutral category
val SeafoamTint = Color(0xFFA8D5D8)     // Water-related trips
val DuskPurple = Color(0xFF8A90A6)      // Evening/night activities
val SunrisePeach = Color(0xFFD4B5A8)    // Morning activities

// MARK: - Disabled/Inactive States
val Mist = Color(0xFFD8E2E6)            // Disabled text/icons (light mode)
val Charcoal = Color(0xFF3A4449)        // Disabled text/icons (dark mode)

// MARK: - Data Visualization Gradient
val GradientStart = LightCyan           // (E0FBFC)
val GradientStep1 = LightBlue           // (C2DFE3)
val GradientStep2 = CoolSteel           // (9DB4C0)
val GradientStep3 = CoastalPath         // (7A9CAF)
val GradientEnd = BlueSlate             // (5C6B73)

// Primary colors (Cool Steel for tranquility and trust)
val PrimaryLight = CoolSteel
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = LightCyan
val OnPrimaryContainerLight = BlueSlate

val PrimaryDark = LightBlue
val OnPrimaryDark = JetBlack
val PrimaryContainerDark = BlueSlate
val OnPrimaryContainerDark = LightCyan

// Secondary colors (Blue Slate for depth and creativity)
val SecondaryLight = BlueSlate
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = LightBlue
val OnSecondaryContainerLight = JetBlack

val SecondaryDark = CoolSteel
val OnSecondaryDark = JetBlack
val SecondaryContainerDark = BlueSlate
val OnSecondaryContainerDark = LightCyan

// Tertiary colors (Accent variations for special elements)
val TertiaryLight = Color(0xFF7A8A92)   // Slightly warmer steel
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFD4E4E8)
val OnTertiaryContainerLight = Color(0xFF2A3B42)

val TertiaryDark = Color(0xFFB8C8D0)
val OnTertiaryDark = JetBlack
val TertiaryContainerDark = Color(0xFF3F4E56)
val OnTertiaryContainerDark = Color(0xFFD4E4E8)

// Error colors (Muted red that harmonizes with the palette)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// Background colors (Light cyan for serene light mode, jet black for dark)
val BackgroundLight = LightCyan
val OnBackgroundLight = JetBlack

val BackgroundDark = JetBlack
val OnBackgroundDark = LightCyan

// Surface colors
val SurfaceLight = Color(0xFFF5FEFF)    // Slightly brighter than background
val OnSurfaceLight = JetBlack
val SurfaceVariantLight = LightBlue
val OnSurfaceVariantLight = BlueSlate

val SurfaceDark = Color(0xFF1A2428)     // Slightly lighter than background
val OnSurfaceDark = LightCyan
val SurfaceVariantDark = BlueSlate
val OnSurfaceVariantDark = LightBlue

// Outline colors
val OutlineLight = CoolSteel
val OutlineDark = BlueSlate

// Surface tint
val SurfaceTintLight = PrimaryLight
val SurfaceTintDark = PrimaryDark
