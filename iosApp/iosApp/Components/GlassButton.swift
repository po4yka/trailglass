import SwiftUI

// MARK: - Glass Button Variants
// Interactive glass buttons with shimmer effects and spring animations

enum GlassButtonVariant {
    case filled
    case outlined
    case text

    var material: GlassMaterial {
        switch self {
        case .filled: return .thick
        case .outlined: return .thin
        case .text: return .ultraThin
        }
    }
}

// MARK: - Glass Button

struct GlassButton: View {
    let title: String?
    let icon: String?
    let variant: GlassButtonVariant
    let isSelected: Bool
    let isDisabled: Bool
    let tint: Color
    let action: () -> Void

    @State private var isPressed = false

    init(
        title: String? = nil,
        icon: String? = nil,
        variant: GlassButtonVariant = .filled,
        isSelected: Bool = false,
        isDisabled: Bool = false,
        tint: Color = .adaptivePrimary,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.icon = icon
        self.variant = variant
        self.isSelected = isSelected
        self.isDisabled = isDisabled
        self.tint = tint
        self.action = action
    }

    var body: some View {
        Button {
            if !isDisabled {
                action()
            }
        } label: {
            HStack(spacing: 8) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 16, weight: .medium))
                }
                if let title = title {
                    Text(title)
                        .font(.system(size: 15, weight: .medium))
                }
            }
            .foregroundColor(foregroundColor)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                Group {
                    if variant == .filled || isSelected {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(tint.opacity(isSelected ? 0.3 : 0.2))
                    }
                }
            )
            .glassEffectTinted(.coastalPath, opacity: 0.6)
            .cornerRadius(8)
            .overlay(
                Group {
                    if variant == .outlined && !isSelected {
                        RoundedRectangle(cornerRadius: 8)
                            .strokeBorder(tint.opacity(0.5), lineWidth: 1.5)
                    }
                }
            )
            .shadow(radius: isSelected ? 3 : 1)
            .scaleEffect(isPressed ? MotionConfig.pressScale : 1.0)
            .opacity(isDisabled ? 0.5 : 1.0)
            // .shimmer(style: isPressed ? .interactive : .subtle, isActive: isPressed)
        }
        .buttonStyle(PlainButtonStyle())
        .disabled(isDisabled)
        .accessibilityLabel(title ?? "Button")
        .accessibilityHint(isSelected ? "Selected" : "")
        .accessibilityAddTraits(.isButton)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if !isPressed && !isDisabled {
                        withAnimation(MotionConfig.instant) {
                            isPressed = true
                        }
                    }
                }
                .onEnded { _ in
                    withAnimation(MotionConfig.instant) {
                        isPressed = false
                    }
                }
        )
    }

    private var foregroundColor: Color {
        if isDisabled {
            return .adaptiveDisabled
        }
        if variant == .filled || isSelected {
            return .white
        }
        return tint
    }
}

// MARK: - Glass Filter Chip

struct GlassFilterChip: View {
    let label: String
    let icon: String?
    let isSelected: Bool
    let tint: Color
    let action: () -> Void

    @State private var isPressed = false

    init(
        label: String,
        icon: String? = nil,
        isSelected: Bool = false,
        tint: Color = .adaptivePrimary,
        action: @escaping () -> Void
    ) {
        self.label = label
        self.icon = icon
        self.isSelected = isSelected
        self.tint = tint
        self.action = action
    }

    var body: some View {
        Button {
            action()
        } label: {
            HStack(spacing: 6) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 14, weight: .medium))
                }
                Text(label)
                    .font(.system(size: 13, weight: .medium))

                if isSelected {
                    Image(systemName: "checkmark")
                        .font(.system(size: 12, weight: .bold))
                }
            }
            .foregroundColor(isSelected ? .white : tint)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(
                Group {
                    if isSelected {
                        Capsule()
                            .fill(tint.opacity(0.3))
                    }
                }
            )
            .glassEffectTinted(.coastalPath, opacity: 0.6)
            .cornerRadius(16)
            .overlay(
                Group {
                    if !isSelected {
                        Capsule()
                            .strokeBorder(tint.opacity(0.4), lineWidth: 1)
                    }
                }
            )
            .shadow(radius: isSelected ? 2 : 1)
            .scaleEffect(isPressed ? MotionConfig.pressScale : 1.0)
            // .shimmer(style: isPressed ? .interactive : .subtle, isActive: isPressed)
        }
        .buttonStyle(PlainButtonStyle())
        .accessibilityLabel(label)
        .accessibilityHint(isSelected ? "Selected" : "")
        .accessibilityAddTraits(.isButton)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if !isPressed {
                        withAnimation(MotionConfig.instant) {
                            isPressed = true
                        }
                    }
                }
                .onEnded { _ in
                    withAnimation(MotionConfig.instant) {
                        isPressed = false
                    }
                }
        )
    }
}

// MARK: - Glass Icon Button

struct GlassIconButton: View {
    let icon: String
    let size: CGFloat
    let tint: Color
    let action: () -> Void

    @State private var isPressed = false

    init(
        icon: String,
        size: CGFloat = 44,
        tint: Color = .adaptivePrimary,
        action: @escaping () -> Void
    ) {
        self.icon = icon
        self.size = size
        self.tint = tint
        self.action = action
    }

    var body: some View {
        Button {
            action()
        } label: {
            Image(systemName: icon)
                .font(.system(size: size * 0.5, weight: .medium))
                .foregroundColor(tint)
                .frame(width: size, height: size)
                .glassEffectTinted(.coastalPath, opacity: 0.6)
                .cornerRadius(size / 2)
                .shadow(radius: 2)
                .scaleEffect(isPressed ? MotionConfig.pressScale : 1.0)
                // // .shimmer(style: .interactive, isActive: isPressed)
        }
        .buttonStyle(PlainButtonStyle())
        .accessibilityLabel(accessibilityLabelForIcon(icon))
        .accessibilityAddTraits(.isButton)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if !isPressed {
                        withAnimation(MotionConfig.instant) {
                            isPressed = true
                        }
                    }
                }
                .onEnded { _ in
                    withAnimation(MotionConfig.instant) {
                        isPressed = false
                    }
                }
        )
    }

    private func accessibilityLabelForIcon(_ iconName: String) -> String {
        let components = iconName.split(separator: ".")
        if let first = components.first {
            return String(first).capitalized
        }
        return "Button"
    }
}

// MARK: - Glass Toggle Button

struct GlassToggleButton: View {
    let title: String
    let icon: String
    @Binding var isOn: Bool
    let tint: Color

    init(
        title: String,
        icon: String,
        isOn: Binding<Bool>,
        tint: Color = .adaptivePrimary
    ) {
        self.title = title
        self.icon = icon
        self._isOn = isOn
        self.tint = tint
    }

    var body: some View {
        GlassButton(
            title: title,
            icon: icon,
            variant: .filled,
            isSelected: isOn,
            tint: tint
        ) {
            withAnimation(MotionConfig.bouncy) {
                isOn.toggle()
            }
        }
        .accessibilityAddTraits(.isButton)
        .accessibilityValue(isOn ? "On" : "Off")
        .accessibilityHint("Double tap to toggle")
    }
}

// MARK: - Previews

#Preview("Button Variants") {
    VStack(spacing: 16) {
        Text("Filled")
            .font(.caption)
            .foregroundColor(.secondary)
        GlassButton(
            title: "Filled Button",
            icon: "star.fill",
            variant: .filled,
            tint: .coastalPath
        ) {}

        Text("Outlined")
            .font(.caption)
            .foregroundColor(.secondary)
        GlassButton(
            title: "Outlined Button",
            icon: "heart",
            variant: .outlined,
            tint: .seaGlass
        ) {}

        Text("Text")
            .font(.caption)
            .foregroundColor(.secondary)
        GlassButton(
            title: "Text Button",
            icon: "info.circle",
            variant: .text,
            tint: .blueSlate
        ) {}

        Text("Selected")
            .font(.caption)
            .foregroundColor(.secondary)
        GlassButton(
            title: "Selected",
            icon: "checkmark",
            isSelected: true,
            tint: .adaptivePrimary
        ) {}

        Text("Disabled")
            .font(.caption)
            .foregroundColor(.secondary)
        GlassButton(
            title: "Disabled",
            icon: "xmark",
            isDisabled: true,
            tint: .adaptivePrimary
        ) {}
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Filter Chips") {
    VStack(spacing: 16) {
        HStack(spacing: 8) {
            GlassFilterChip(
                label: "Walk",
                icon: "figure.walk",
                isSelected: true,
                tint: .seaGlass
            ) {}

            GlassFilterChip(
                label: "Bike",
                icon: "bicycle",
                tint: .coastalPath
            ) {}

            GlassFilterChip(
                label: "Car",
                icon: "car",
                tint: .driftwood
            ) {}
        }

        HStack(spacing: 8) {
            GlassFilterChip(
                label: "Food",
                icon: "fork.knife",
                isSelected: true,
                tint: .morningCategory
            ) {}

            GlassFilterChip(
                label: "Shopping",
                icon: "cart",
                tint: .coolSteel
            ) {}
        }
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Icon Buttons") {
    HStack(spacing: 16) {
        GlassIconButton(
            icon: "heart.fill",
            tint: .adaptiveWarning
        ) {}

        GlassIconButton(
            icon: "star.fill",
            size: 52,
            tint: .warning
        ) {}

        GlassIconButton(
            icon: "location.fill",
            size: 36,
            tint: .coastalPath
        ) {}
    }
    .padding()
    .background(Color.backgroundLight)
}
