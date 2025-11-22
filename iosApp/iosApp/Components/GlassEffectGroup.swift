import SwiftUI

// MARK: - Glass Effect Group

/**
 * Container for grouping glass elements for shared sampling region.
 * Prevents visual inconsistency when glass elements overlap.
 * Maintains depth hierarchy and supports nested grouping.
 */
struct GlassEffectGroup<Content: View>: View {
    let id: String
    let variant: GlassVariant
    let spacing: CGFloat
    let padding: CGFloat
    let cornerRadius: CGFloat
    @ViewBuilder let content: () -> Content
    @Environment(\.colorScheme) var colorScheme

    init(
        id: String = UUID().uuidString,
        variant: GlassVariant = .regular,
        spacing: CGFloat = 8,
        padding: CGFloat = 12,
        cornerRadius: CGFloat = 16,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.id = id
        self.variant = variant
        self.spacing = spacing
        self.padding = padding
        self.cornerRadius = cornerRadius
        self.content = content
    }

    var body: some View {
        content()
            .padding(padding)
            .background(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                            .fill(tintColor.opacity(0.1))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                            .strokeBorder(
                                LinearGradient(
                                    colors: [
                                        Color.white.opacity(colorScheme == .dark ? 0.2 : 0.4),
                                        Color.white.opacity(colorScheme == .dark ? 0.05 : 0.1)
                                    ],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    )
            )
            .shadow(color: Color.black.opacity(0.08), radius: 8, y: 4)
    }

    private var tintColor: Color {
        switch variant {
        case .primary:
            return .coolSteel
        case .success:
            return .seaGlass
        case .warning:
            return .driftwood
        case .surface:
            return .lightCyan
        case .active:
            return .lightBlue
        case .secondary:
            return .blueSlate
        case .tinted(let color, _):
            return color
        default:
            return .lightCyan
        }
    }
}

// MARK: - Horizontal Glass Group

/**
 * Horizontal layout for glass elements with consistent spacing.
 */
struct HorizontalGlassGroup<Content: View>: View {
    let spacing: CGFloat
    let alignment: VerticalAlignment
    @ViewBuilder let content: () -> Content

    init(
        spacing: CGFloat = 8,
        alignment: VerticalAlignment = .center,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.spacing = spacing
        self.alignment = alignment
        self.content = content
    }

    var body: some View {
        HStack(alignment: alignment, spacing: spacing) {
            content()
        }
    }
}

// MARK: - Vertical Glass Group

/**
 * Vertical layout for glass elements with consistent spacing.
 */
struct VerticalGlassGroup<Content: View>: View {
    let spacing: CGFloat
    let alignment: HorizontalAlignment
    @ViewBuilder let content: () -> Content

    init(
        spacing: CGFloat = 8,
        alignment: HorizontalAlignment = .leading,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.spacing = spacing
        self.alignment = alignment
        self.content = content
    }

    var body: some View {
        VStack(alignment: alignment, spacing: spacing) {
            content()
        }
    }
}

// MARK: - Scrollable Glass Group

/**
 * Scrollable horizontal group for filter chips, buttons, etc.
 */
struct ScrollableGlassGroup<Content: View>: View {
    let spacing: CGFloat
    let showsIndicators: Bool
    @ViewBuilder let content: () -> Content

    init(
        spacing: CGFloat = 8,
        showsIndicators: Bool = false,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.spacing = spacing
        self.showsIndicators = showsIndicators
        self.content = content
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: showsIndicators) {
            HStack(spacing: spacing) {
                content()
            }
            .padding(.horizontal, 16)
        }
    }
}

// MARK: - Glass Card Group

/**
 * Group of glass cards with staggered appearance animation.
 */
struct GlassCardGroup<Content: View>: View {
    let spacing: CGFloat
    let columns: Int
    @ViewBuilder let content: () -> Content
    @State private var appearedCards: Set<Int> = []

    init(
        spacing: CGFloat = 12,
        columns: Int = 2,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.spacing = spacing
        self.columns = columns
        self.content = content
    }

    var body: some View {
        LazyVGrid(
            columns: Array(repeating: GridItem(.flexible(), spacing: spacing), count: columns),
            spacing: spacing
        ) {
            content()
        }
    }
}

// MARK: - Glass Toolbar Group

/**
 * Group for toolbar actions with glass background.
 */
struct GlassToolbarGroup<Content: View>: View {
    let position: ToolbarPosition
    @ViewBuilder let content: () -> Content
    @Environment(\.colorScheme) var colorScheme

    enum ToolbarPosition {
        case top
        case bottom
        case leading
        case trailing
    }

    var body: some View {
        HStack(spacing: 8) {
            content()
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(
            Capsule()
                .fill(.ultraThinMaterial)
                .overlay(
                    Capsule()
                        .fill(Color.lightCyan.opacity(0.15))
                )
                .overlay(
                    Capsule()
                        .strokeBorder(
                            Color.white.opacity(colorScheme == .dark ? 0.2 : 0.4),
                            lineWidth: 1
                        )
                )
        )
        .shadow(color: Color.black.opacity(0.1), radius: 8, y: 4)
    }
}

// MARK: - Glass Chip Group

/**
 * Group of chips with wrapping layout.
 */
struct GlassChipGroup: View {
    let chips: [ChipData]
    let selectedChips: Set<String>
    let onChipTap: (String) -> Void
    @Environment(\.colorScheme) var colorScheme

    struct ChipData: Identifiable {
        let id: String
        let label: String
        let icon: String?
        let color: Color

        init(id: String, label: String, icon: String? = nil, color: Color = .coolSteel) {
            self.id = id
            self.label = label
            self.icon = icon
            self.color = color
        }
    }

    var body: some View {
        FlowLayout(spacing: 8) {
            ForEach(chips) { chip in
                GlassChipView(
                    label: chip.label,
                    icon: chip.icon,
                    color: chip.color,
                    isSelected: selectedChips.contains(chip.id),
                    onTap: { onChipTap(chip.id) }
                )
            }
        }
    }
}

// MARK: - Glass Chip View

private struct GlassChipView: View {
    let label: String
    let icon: String?
    let color: Color
    let isSelected: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.caption)
                }
                Text(label)
                    .font(.caption)
                    .fontWeight(isSelected ? .semibold : .regular)
            }
            .foregroundColor(isSelected ? color : .primary)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(.ultraThinMaterial)
                    .overlay(
                        Capsule()
                            .fill(color.opacity(isSelected ? 0.3 : 0.1))
                    )
                    .overlay(
                        Capsule()
                            .strokeBorder(
                                isSelected ? color.opacity(0.6) : Color.white.opacity(colorScheme == .dark ? 0.2 : 0.3),
                                lineWidth: isSelected ? 1.5 : 1
                            )
                    )
            )
            .shadow(
                color: isSelected ? color.opacity(0.3) : Color.clear,
                radius: isSelected ? 6 : 0
            )
            .scaleEffect(isSelected ? 1.02 : 1.0)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Flow Layout

/**
 * Custom layout for wrapping chips.
 */
private struct FlowLayout: Layout {
    let spacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowLayoutResult(
            in: proposal.replacingUnspecifiedDimensions().width,
            subviews: subviews,
            spacing: spacing
        )
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowLayoutResult(
            in: bounds.width,
            subviews: subviews,
            spacing: spacing
        )
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x, y: bounds.minY + result.positions[index].y), proposal: .unspecified)
        }
    }

    struct FlowLayoutResult {
        var size: CGSize = .zero
        var positions: [CGPoint] = []

        init(in maxWidth: CGFloat, subviews: Subviews, spacing: CGFloat) {
            var currentX: CGFloat = 0
            var currentY: CGFloat = 0
            var lineHeight: CGFloat = 0

            for subview in subviews {
                let subviewSize = subview.sizeThatFits(.unspecified)

                if currentX + subviewSize.width > maxWidth && currentX > 0 {
                    currentX = 0
                    currentY += lineHeight + spacing
                    lineHeight = 0
                }

                positions.append(CGPoint(x: currentX, y: currentY))
                lineHeight = max(lineHeight, subviewSize.height)
                currentX += subviewSize.width + spacing
            }

            size = CGSize(width: maxWidth, height: currentY + lineHeight)
        }
    }
}

// MARK: - View Extensions

extension View {
    func inGlassGroup(
        id: String = UUID().uuidString,
        variant: GlassVariant = .surface,
        padding: CGFloat = 12,
        cornerRadius: CGFloat = 16
    ) -> some View {
        GlassEffectGroup(
            id: id,
            variant: variant,
            padding: padding,
            cornerRadius: cornerRadius
        ) {
            self
        }
    }
}
