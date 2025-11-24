import SwiftUI
import Shared

// MARK: - Category Badge

/**
 * Category badge with semantic shape morphing based on category type.
 * Uses Liquid Glass aesthetic with category-specific colors and shapes.
 */
struct CategoryBadge: View {
    let category: PlaceCategory
    let isSelected: Bool
    let showLabel: Bool
    let size: BadgeSize
    let onTap: (() -> Void)?
    @Environment(\.colorScheme) var colorScheme

    enum BadgeSize {
        case small   // Icon only, 32x32
        case medium  // Icon + label, 40x40
        case large   // Icon + label + description, 56x56

        var iconSize: CGFloat {
            switch self {
            case .small: return 32
            case .medium: return 40
            case .large: return 56
            }
        }

        var fontSize: Font {
            switch self {
            case .small: return .caption2
            case .medium: return .caption
            case .large: return .body
            }
        }
    }

    init(
        category: PlaceCategory,
        isSelected: Bool = false,
        showLabel: Bool = true,
        size: BadgeSize = .medium,
        onTap: (() -> Void)? = nil
    ) {
        self.category = category
        self.isSelected = isSelected
        self.showLabel = showLabel
        self.size = size
        self.onTap = onTap
    }

    private var categoryColor: Color {
        switch category {
        case .home: return .seaGlass
        case .work: return .coolSteel
        // case .food: return .sunrisePeach
        // case .shopping: return .driftwood
        // case .fitness: return .adaptiveSuccess
        // case .entertainment: return .morningCategory
        // case .travel: return .waterCategory
        // case .healthcare: return .adaptiveWarning
        // case .education: return .adaptivePrimary
        // case .religious: return .duskPurple
        // case .social: return .morningCategory
        // case .outdoor: return .adaptiveSuccess
        // case .service: return .neutralCategory
        // case .other: return .neutralCategory
        default: return .neutralCategory
        }
    }

    private var categoryIcon: String {
        switch category {
        case .home: return "house.fill"
        case .work: return "briefcase.fill"
        // case .food: return "fork.knife"
        // case .shopping: return "cart.fill"
        // case .fitness: return "figure.run"
        // case .entertainment: return "film.fill"
        // case .travel: return "airplane"
        // case .healthcare: return "cross.case.fill"
        // case .education: return "book.fill"
        // case .religious: return "building.columns.fill"
        // case .social: return "person.2.fill"
        // case .outdoor: return "tree.fill"
        // case .service: return "hammer.fill"
        // case .other: return "mappin.circle.fill"
        default: return "mappin.circle.fill"
        }
    }

    private var categoryShape: GlassShape {
        GlassShape.forCategory(categoryName(category))
    }

    private var displayName: String {
        categoryName(category)
    }

    var body: some View {
        if showLabel {
            BadgeWithLabel()
        } else {
            BadgeIconOnly()
        }
    }

    @ViewBuilder
    private func BadgeWithLabel() -> some View {
        Button(action: { onTap?() }) {
            HStack(spacing: 8) {
                // Morphing icon
                ZStack {
                    categoryShape.pathData(CGRect(x: 0, y: 0, width: size.iconSize, height: size.iconSize))
                        .fill(
                            LinearGradient(
                                colors: [
                                    categoryColor.opacity(0.6),
                                    categoryColor.opacity(0.4)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .overlay(
                            categoryShape.pathData(CGRect(x: 0, y: 0, width: size.iconSize, height: size.iconSize))
                                .fill(.ultraThinMaterial)
                                .opacity(0.3)
                        )
                        .overlay(
                            categoryShape.pathData(CGRect(x: 0, y: 0, width: size.iconSize, height: size.iconSize))
                                .stroke(
                                    Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                                    lineWidth: 1.5
                                )
                        )

                    Image(systemName: categoryIcon)
                        .font(.system(size: size.iconSize * 0.5))
                        .foregroundColor(.white)
                        .shadow(color: Color.black.opacity(0.3), radius: 2)
                }
                .frame(width: size.iconSize, height: size.iconSize)

                if size != .small {
                    Text(displayName)
                        .font(size.fontSize)
                        .fontWeight(isSelected ? .semibold : .regular)
                        .foregroundColor(isSelected ? categoryColor : .primary)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: size.iconSize / 2, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: size.iconSize / 2, style: .continuous)
                            .fill(categoryColor.opacity(isSelected ? 0.2 : 0.1))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: size.iconSize / 2, style: .continuous)
                            .strokeBorder(
                                isSelected ? categoryColor.opacity(0.6) : Color.white.opacity(colorScheme == .dark ? 0.2 : 0.3),
                                lineWidth: isSelected ? 2 : 1
                            )
                    )
            )
            .shadow(
                color: isSelected ? categoryColor.opacity(0.3) : Color.black.opacity(0.1),
                radius: isSelected ? 8 : 4,
                y: 4
            )
            .scaleEffect(isSelected ? 1.05 : 1.0)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func BadgeIconOnly() -> some View {
        Button(action: { onTap?() }) {
            ZStack {
                categoryShape.pathData(CGRect(x: 0, y: 0, width: size.iconSize, height: size.iconSize))
                    .fill(
                        LinearGradient(
                            colors: [
                                categoryColor.opacity(0.6),
                                categoryColor.opacity(0.4)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        categoryShape.pathData(CGRect(x: 0, y: 0, width: size.iconSize, height: size.iconSize))
                            .fill(.ultraThinMaterial)
                            .opacity(0.3)
                    )
                    .overlay(
                        categoryShape.pathData(CGRect(x: 0, y: 0, width: size.iconSize, height: size.iconSize))
                            .stroke(
                                isSelected ? categoryColor : Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                                lineWidth: isSelected ? 2 : 1.5
                            )
                    )

                Image(systemName: categoryIcon)
                    .font(.system(size: size.iconSize * 0.5))
                    .foregroundColor(.white)
                    .shadow(color: Color.black.opacity(0.3), radius: 2)
            }
            .frame(width: size.iconSize, height: size.iconSize)
            .shadow(
                color: isSelected ? categoryColor.opacity(0.4) : Color.black.opacity(0.1),
                radius: isSelected ? 10 : 6,
                y: 4
            )
            .scaleEffect(isSelected ? 1.1 : 1.0)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Category Badge Grid

/**
 * Grid of category badges for filtering/selection.
 */
struct CategoryBadgeGrid: View {
    let categories: [PlaceCategory]
    let selectedCategories: Set<String>
    let columns: Int
    let badgeSize: CategoryBadge.BadgeSize
    let onCategoryTap: (PlaceCategory) -> Void

    init(
        categories: [PlaceCategory],
        selectedCategories: Set<String> = [],
        columns: Int = 3,
        badgeSize: CategoryBadge.BadgeSize = .medium,
        onCategoryTap: @escaping (PlaceCategory) -> Void
    ) {
        self.categories = categories
        self.selectedCategories = selectedCategories
        self.columns = columns
        self.badgeSize = badgeSize
        self.onCategoryTap = onCategoryTap
    }

    var body: some View {
        LazyVGrid(
            columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: columns),
            spacing: 12
        ) {
            // ForEach(categories, id: \.self) { category in
            //     CategoryBadge(...)
            // }
            Text("Categories not available")
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Scrollable Category Row

/**
 * Horizontal scrollable row of category badges.
 */
struct CategoryBadgeRow: View {
    let categories: [PlaceCategory]
    let selectedCategory: String?
    let badgeSize: CategoryBadge.BadgeSize
    let onCategoryTap: (PlaceCategory) -> Void

    init(
        categories: [PlaceCategory],
        selectedCategory: String? = nil,
        badgeSize: CategoryBadge.BadgeSize = .medium,
        onCategoryTap: @escaping (PlaceCategory) -> Void
    ) {
        self.categories = categories
        self.selectedCategory = selectedCategory
        self.badgeSize = badgeSize
        self.onCategoryTap = onCategoryTap
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                // ForEach(categories, id: \.self) { category in
                //     CategoryBadge(...)
                // }
                Text("Category selection not available")
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 16)
        }
    }
}

// MARK: - Preview Helper

#if DEBUG
struct CategoryBadge_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            // Single badges
            CategoryBadge(
                category: PlaceCategory.home,
                isSelected: false,
                showLabel: true,
                size: .medium
            )

            CategoryBadge(
                category: PlaceCategory.work,
                isSelected: true,
                showLabel: true,
                size: .medium
            )

            // Badge row
            CategoryBadgeRow(
                categories: [.home, .work],
                selectedCategory: "WORK"
            ) { _ in }
        }
        .padding()
        .background(Color.adaptiveBackground)
    }
}
#endif
