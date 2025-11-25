import SwiftUI

extension Font {
    /// Custom Outfit font
    static func outfit(size: CGFloat, weight: Font.Weight = .regular) -> Font {
        return Font.custom("Outfit", size: size).weight(weight)
    }

    // MARK: - Material 3 Expressive Typography Scale

    static let displayLarge = outfit(size: 57, weight: .regular)
    static let displayMedium = outfit(size: 45, weight: .regular)
    static let displaySmall = outfit(size: 36, weight: .regular)

    static let headlineLarge = outfit(size: 32, weight: .regular)
    static let headlineMedium = outfit(size: 28, weight: .regular)
    static let headlineSmall = outfit(size: 24, weight: .regular)

    static let titleLarge = outfit(size: 22, weight: .medium)
    static let titleMedium = outfit(size: 16, weight: .medium)
    static let titleSmall = outfit(size: 14, weight: .medium)

    static let bodyLarge = outfit(size: 16, weight: .regular)
    static let bodyMedium = outfit(size: 14, weight: .regular)
    static let bodySmall = outfit(size: 12, weight: .regular)

    static let labelLarge = outfit(size: 14, weight: .medium)
    static let labelMedium = outfit(size: 12, weight: .medium)
    static let labelSmall = outfit(size: 11, weight: .medium)

    // MARK: - Emphasized Styles

    static let displayLargeEmphasized = outfit(size: 59, weight: .medium)
    static let displayMediumEmphasized = outfit(size: 47, weight: .medium)
    static let displaySmallEmphasized = outfit(size: 38, weight: .medium)

    static let headlineLargeEmphasized = outfit(size: 34, weight: .medium)
    static let headlineMediumEmphasized = outfit(size: 30, weight: .medium)
    static let headlineSmallEmphasized = outfit(size: 26, weight: .medium)

    static let titleLargeEmphasized = outfit(size: 23, weight: .semibold)
    static let titleMediumEmphasized = outfit(size: 17, weight: .semibold)
    static let titleSmallEmphasized = outfit(size: 15, weight: .semibold)

    static let bodyLargeEmphasized = outfit(size: 17, weight: .medium)
    static let bodyMediumEmphasized = outfit(size: 15, weight: .medium)

    static let labelLargeEmphasized = outfit(size: 15, weight: .semibold)
    static let labelMediumEmphasized = outfit(size: 13, weight: .semibold)
}
