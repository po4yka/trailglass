import SwiftUI

// MARK: - Glass Button Style

struct GlassButtonStyle: ButtonStyle {
    let variant: GlassVariant
    let size: ButtonSize

    enum ButtonSize {
        case small
        case medium
        case large

        var height: CGFloat {
            switch self {
            case .small: return 36
            case .medium: return 44
            case .large: return 56
            }
        }

        var horizontalPadding: CGFloat {
            switch self {
            case .small: return 16
            case .medium: return 20
            case .large: return 24
            }
        }

        var cornerRadius: CGFloat {
            switch self {
            case .small: return 10
            case .medium: return 12
            case .large: return 16
            }
        }

        var fontSize: CGFloat {
            switch self {
            case .small: return 14
            case .medium: return 16
            case .large: return 18
            }
        }
    }

    init(variant: GlassVariant = .primary, size: ButtonSize = .medium) {
        self.variant = variant
        self.size = size
    }

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: size.fontSize, weight: .medium))
            .frame(minHeight: size.height)
            .padding(.horizontal, size.horizontalPadding)
            .glassEffect(variant: variant)
            .clipShape(RoundedRectangle(cornerRadius: size.cornerRadius, style: .continuous))
            .glassBorder(width: 1, cornerRadius: size.cornerRadius)
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(MotionConfig.buttonPress, value: configuration.isPressed)
    }
}

// MARK: - Glass Icon Button Style

struct GlassIconButtonStyle: ButtonStyle {
    let variant: GlassVariant
    let size: CGFloat

    init(variant: GlassVariant = .regular, size: CGFloat = 44) {
        self.variant = variant
        self.size = size
    }

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(width: size, height: size)
            .glassEffect(variant: variant)
            .clipShape(Circle())
            .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
            .scaleEffect(configuration.isPressed ? 0.92 : 1.0)
            .animation(MotionConfig.buttonPress, value: configuration.isPressed)
    }
}

// MARK: - Glass Card Style

struct GlassCardStyle: ViewModifier {
    let variant: GlassVariant
    let cornerRadius: CGFloat
    let padding: EdgeInsets

    init(
        variant: GlassVariant = .regular,
        cornerRadius: CGFloat = 16,
        padding: EdgeInsets = EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)
    ) {
        self.variant = variant
        self.cornerRadius = cornerRadius
        self.padding = padding
    }

    func body(content: Content) -> some View {
        content
            .padding(padding)
            .glassContainer(
                variant: variant,
                cornerRadius: cornerRadius,
                shadowRadius: 8
            )
            .glassBorder(width: 1, cornerRadius: cornerRadius)
    }
}

// MARK: - Glass Toolbar Style

struct GlassToolbarStyle: ViewModifier {
    let variant: GlassVariant
    let height: CGFloat
    @Environment(\.colorScheme) var colorScheme

    init(variant: GlassVariant = .ultraThin, height: CGFloat = 64) {
        self.variant = variant
        self.height = height
    }

    func body(content: Content) -> some View {
        content
            .frame(height: height)
            .frame(maxWidth: .infinity)
            .glassEffect(variant: variant)
            .overlay(
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [
                                Color.white.opacity(colorScheme == .dark ? 0.1 : 0.2),
                                Color.clear
                            ],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .frame(height: 1),
                alignment: .top
            )
    }
}

// MARK: - Glass Segmented Control Style

struct GlassSegmentedControl<T: Hashable>: View {
    let options: [T]
    let labels: [T: String]
    @Binding var selection: T
    let variant: GlassVariant

    init(
        options: [T],
        labels: [T: String],
        selection: Binding<T>,
        variant: GlassVariant = .regular
    ) {
        self.options = options
        self.labels = labels
        self._selection = selection
        self.variant = variant
    }

    var body: some View {
        HStack(spacing: 4) {
            ForEach(options, id: \.self) { option in
                Button {
                    withAnimation(MotionConfig.quickSpring) {
                        selection = option
                    }
                } label: {
                    Text(labels[option] ?? "")
                        .font(.system(size: 14, weight: .medium))
                        .frame(maxWidth: .infinity)
                        .frame(height: 32)
                }
                .buttonStyle(
                    GlassSegmentButtonStyle(
                        isSelected: selection == option,
                        variant: variant
                    )
                )
            }
        }
        .padding(4)
        .glassEffect(variant: .clear)
        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
    }
}

private struct GlassSegmentButtonStyle: ButtonStyle {
    let isSelected: Bool
    let variant: GlassVariant

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .foregroundColor(isSelected ? .primary : .secondary)
            .background(
                Group {
                    if isSelected {
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(Color.primary.opacity(0.1))
                            .glassEffect(variant: variant)
                    }
                }
            )
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(MotionConfig.quickSpring, value: configuration.isPressed)
    }
}

// MARK: - Glass Bottom Sheet

struct GlassBottomSheet<Content: View>: ViewModifier {
    let isPresented: Bool
    let content: Content
    let detents: Set<PresentationDetent>
    let variant: GlassVariant

    init(
        isPresented: Bool,
        variant: GlassVariant = .regular,
        detents: Set<PresentationDetent> = [.medium, .large],
        @ViewBuilder content: () -> Content
    ) {
        self.isPresented = isPresented
        self.variant = variant
        self.detents = detents
        self.content = content()
    }

    func body(content: Content) -> some View {
        content
            .sheet(isPresented: .constant(isPresented)) {
                self.content
                    .glassEffect(variant: variant)
                    .presentationDetents(detents)
                    .presentationBackgroundInteraction(.enabled)
                    .presentationCornerRadius(24)
            }
    }
}

// MARK: - Glass List Row Style

struct GlassListRowStyle: ViewModifier {
    let variant: GlassVariant
    @Environment(\.colorScheme) var colorScheme

    init(variant: GlassVariant = .clear) {
        self.variant = variant
    }

    func body(content: Content) -> some View {
        content
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .glassEffect(variant: variant)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .strokeBorder(
                        Color.white.opacity(colorScheme == .dark ? 0.1 : 0.2),
                        lineWidth: 0.5
                    )
            )
    }
}

// MARK: - Glass Tag Style

struct GlassTagStyle: ViewModifier {
    let color: Color
    @Environment(\.colorScheme) var colorScheme

    func body(content: Content) -> some View {
        content
            .font(.system(size: 12, weight: .medium))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .glassEffectTinted(color, opacity: colorScheme == .dark ? 0.5 : 0.4)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .strokeBorder(color.opacity(0.3), lineWidth: 0.5)
            )
    }
}

// MARK: - Glass Toast Style

struct GlassToast: View {
    let message: String
    let icon: String?
    let variant: GlassVariant
    @Environment(\.colorScheme) var colorScheme

    init(
        message: String,
        icon: String? = nil,
        variant: GlassVariant = .regular
    ) {
        self.message = message
        self.icon = icon
        self.variant = variant
    }

    var body: some View {
        HStack(spacing: 12) {
            if let icon = icon {
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .semibold))
            }
            Text(message)
                .font(.system(size: 14, weight: .medium))
        }
        .foregroundColor(.primary)
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .glassEffect(variant: variant)
        .clipShape(Capsule())
        .shadow(
            color: Color.black.opacity(colorScheme == .dark ? 0.4 : 0.1),
            radius: 12,
            x: 0,
            y: 4
        )
    }
}

// MARK: - View Extensions

extension View {
    func glassButtonStyle(
        variant: GlassVariant = .primary,
        size: GlassButtonStyle.ButtonSize = .medium
    ) -> some View {
        self.buttonStyle(GlassButtonStyle(variant: variant, size: size))
    }

    func glassIconButtonStyle(
        variant: GlassVariant = .regular,
        size: CGFloat = 44
    ) -> some View {
        self.buttonStyle(GlassIconButtonStyle(variant: variant, size: size))
    }

    func glassCard(
        variant: GlassVariant = .regular,
        cornerRadius: CGFloat = 16,
        padding: EdgeInsets = EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)
    ) -> some View {
        self.modifier(GlassCardStyle(
            variant: variant,
            cornerRadius: cornerRadius,
            padding: padding
        ))
    }

    func glassToolbar(
        variant: GlassVariant = .ultraThin,
        height: CGFloat = 64
    ) -> some View {
        self.modifier(GlassToolbarStyle(variant: variant, height: height))
    }

    func glassListRow(variant: GlassVariant = .clear) -> some View {
        self.modifier(GlassListRowStyle(variant: variant))
    }

    func glassTag(color: Color) -> some View {
        self.modifier(GlassTagStyle(color: color))
    }
}

// MARK: - Preset Styles

extension View {
    func glassPrimaryButton() -> some View {
        self.glassButtonStyle(variant: .primary, size: .medium)
    }

    func glassSecondaryButton() -> some View {
        self.glassButtonStyle(variant: .secondary, size: .medium)
    }

    func glassSuccessButton() -> some View {
        self.glassButtonStyle(variant: .success, size: .medium)
    }

    func glassWarningButton() -> some View {
        self.glassButtonStyle(variant: .warning, size: .medium)
    }

    func glassPrimaryCard() -> some View {
        self.glassCard(variant: .primary)
    }

    func glassSecondaryCard() -> some View {
        self.glassCard(variant: .secondary)
    }

    func glassSurfaceCard() -> some View {
        self.glassCard(variant: .surface)
    }
}
