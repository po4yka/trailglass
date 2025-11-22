import SwiftUI

// MARK: - Silent Waters Color Palette
// Dewy blues and weathered gray merge into a hushed palette,
// like drifting fog on silent waters.

extension Color {
    // MARK: - Base Palette Colors

    static let lightCyan = Color(hex: "E0FBFC")      // Delicate, sunlit blue tint
    static let lightBlue = Color(hex: "C2DFE3")       // Soft pastel blue
    static let coolSteel = Color(hex: "9DB4C0")       // Cool, steely blue with hint of mist
    static let blueSlate = Color(hex: "5C6B73")       // Blue with hints of cool authority
    static let jetBlack = Color(hex: "253237")        // Intense darkness with enigmatic appeal

    // MARK: - Success/Confirmation Colors

    static let seaGlass = Color(hex: "8BB5A1")        // Muted teal-green for success states
    static let sageGreen = Color(hex: "6A8E7F")       // Deeper confirmation color

    // MARK: - Warning/Attention Colors

    static let driftwood = Color(hex: "C9B896")       // Warm sand tone for warnings
    static let weatheredBrass = Color(hex: "A89968")  // Deeper attention color

    // MARK: - Map/Route Colors

    static let coastalPath = Color(hex: "7A9CAF")     // Active route/current trip
    static let harborBlue = Color(hex: "5C8AA8")      // Historical routes
    static let mistyLavender = Color(hex: "A8B5C7")   // Alternative path suggestion

    // MARK: - Category/Tag Colors

    static let stoneGray = Color(hex: "8C979E")       // Neutral category
    static let seafoamTint = Color(hex: "A8D5D8")     // Water-related trips
    static let duskPurple = Color(hex: "8A90A6")      // Evening/night activities
    static let sunrisePeach = Color(hex: "D4B5A8")    // Morning activities

    // MARK: - Disabled/Inactive States

    static let mist = Color(hex: "D8E2E6")            // Disabled text/icons (light mode)
    static let charcoal = Color(hex: "3A4449")        // Disabled text/icons (dark mode)

    // MARK: - Data Visualization Gradient

    static let gradientStart = lightCyan              // (E0FBFC)
    static let gradientStep1 = lightBlue              // (C2DFE3)
    static let gradientStep2 = coolSteel              // (9DB4C0)
    static let gradientStep3 = coastalPath            // (7A9CAF)
    static let gradientEnd = blueSlate                // (5C6B73)

    // MARK: - Semantic Colors

    // Primary colors (Cool Steel for tranquility and trust)
    static let primaryLight = coolSteel
    static let primaryDark = lightBlue

    // Container colors
    static let primaryContainerLight = lightCyan
    static let primaryContainerDark = blueSlate

    // Secondary colors (Blue Slate for depth and creativity)
    static let secondaryLight = blueSlate
    static let secondaryDark = coolSteel

    // Secondary container colors
    static let secondaryContainerLight = lightBlue
    static let secondaryContainerDark = blueSlate

    // Background colors
    static let backgroundLight = lightCyan
    static let backgroundDark = jetBlack

    // Surface colors
    static let surfaceLight = Color(hex: "F5FEFF")    // Slightly brighter than background
    static let surfaceDark = Color(hex: "1A2428")     // Slightly lighter than background

    // Surface variant colors
    static let surfaceVariantLight = lightBlue
    static let surfaceVariantDark = blueSlate

    // MARK: - Adaptive Colors

    static var adaptivePrimary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(primaryDark) : UIColor(primaryLight)
        })
    }

    static var adaptiveBackground: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(backgroundDark) : UIColor(backgroundLight)
        })
    }

    static var adaptiveSurface: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(surfaceDark) : UIColor(surfaceLight)
        })
    }

    static var adaptiveSurfaceVariant: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(surfaceVariantDark) : UIColor(surfaceVariantLight)
        })
    }

    static var adaptiveSecondary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(secondaryDark) : UIColor(secondaryLight)
        })
    }

    // MARK: - Adaptive Extended Colors

    static var adaptiveSuccess: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(sageGreen) : UIColor(seaGlass)
        })
    }

    static var adaptiveWarning: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(weatheredBrass) : UIColor(driftwood)
        })
    }

    static var adaptiveDisabled: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(charcoal) : UIColor(mist)
        })
    }

    static var adaptiveActiveRoute: Color {
        coastalPath
    }

    static var adaptiveHistoricalRoute: Color {
        harborBlue
    }

    // MARK: - Semantic Helpers

    static var success: Color { seaGlass }
    static var successEmphasis: Color { sageGreen }
    static var warning: Color { driftwood }
    static var warningEmphasis: Color { weatheredBrass }
    static var disabled: Color { mist }
    static var disabledDark: Color { charcoal }

    // MARK: - Category Helpers

    static var neutralCategory: Color { stoneGray }
    static var waterCategory: Color { seafoamTint }
    static var eveningCategory: Color { duskPurple }
    static var morningCategory: Color { sunrisePeach }

    // MARK: - Route Helpers

    static var activeRoute: Color { coastalPath }
    static var historicalRoute: Color { harborBlue }
    static var alternativeRoute: Color { mistyLavender }

    // MARK: - Hex Initializer

    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - UIColor Extension for Adaptive Colors

extension UIColor {
    convenience init(_ color: Color) {
        if #available(iOS 14.0, *) {
            self.init(color)
        } else {
            self.init(red: 0, green: 0, blue: 0, alpha: 1)
        }
    }
}
