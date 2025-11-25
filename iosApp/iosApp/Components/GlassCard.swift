import SwiftUI
import Shared

// MARK: - Glass Card Variants
// Reusable glass cards for timeline, stats, and content display

enum GlassCardVariant {
    case visit
    case route
    case stat
    case summary

    var tint: Color {
        switch self {
        case .visit: return .coastalPath
        case .route: return .seaGlass
        case .stat: return .coolSteel
        case .summary: return .blueSlate
        }
    }

    var material: GlassMaterial {
        switch self {
        case .visit, .route: return .regular
        case .stat: return .thick
        case .summary: return .regular
        }
    }
}

// MARK: - Base Glass Card

struct GlassCard<Content: View>: View {
    let variant: GlassCardVariant
    @ViewBuilder let content: () -> Content

    @State private var isPressed = false

    var body: some View {
        content()
            .padding(16)
            .glassEffectTinted(.coastalPath, opacity: 0.6)
            .cornerRadius(12)
            .shadow(radius: 2)
            .scaleEffect(isPressed ? MotionConfig.pressScale : 1.0)
            .animation(MotionConfig.buttonPress, value: isPressed)
            .accessibilityElement(children: .combine)
    }

    func onTap(_ action: @escaping () -> Void) -> some View {
        self.onTapGesture {
            action()
        }
    }
}

// MARK: - Visit Glass Card

struct VisitGlassCard: View {
    let visit: PlaceVisit
    let onTap: (() -> Void)?

    init(visit: PlaceVisit, onTap: (() -> Void)? = nil) {
        self.visit = visit
        self.onTap = onTap
    }

    var body: some View {
        Button {
            onTap?()
        } label: {
            GlassCard(variant: .visit) {
                VStack(alignment: .leading, spacing: 12) {
                    // Header with icon and title
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: categoryIcon(visit.category as? PlaceCategory ?? .other))
                            .font(.title2)
                            .foregroundColor(.coastalPath)

                        VStack(alignment: .leading, spacing: 4) {
                            HStack(spacing: 8) {
                                Text(visit.displayName)
                                    .font(.headline)
                                    .foregroundColor(.primary)

                                if visit.isFavorite {
                                    Image(systemName: "star.fill")
                                        .foregroundColor(.warning)
                                        .font(.caption)
                                }
                            }

                            if let city = visit.city {
                                Text(city)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }

                        Spacer()
                    }

                    // Address (if available)
                    if let address = visit.approximateAddress,
                       visit.userLabel == nil,
                       visit.poiName == nil {
                        Text(address)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    // User notes (if available)
                    if let notes = visit.userNotes {
                        HStack(spacing: 8) {
                            Image(systemName: "note.text")
                                .font(.caption)
                                .foregroundColor(.blueSlate)
                            Text(notes)
                                .font(.caption)
                                .foregroundColor(.primary)
                        }
                        .padding(8)
                        .glassEffectTinted(.coastalPath, opacity: 0.6)
                        .cornerRadius(6)
                    }

                    // Metadata chips
                    HStack(spacing: 8) {
                        MetadataChip(
                            icon: "clock",
                            text: formatDuration(visit.duration)
                        )

                        // Category chip removed - enum access issues
                    }
                }
            }
        }
        .buttonStyle(PlainButtonStyle())
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(.isButton)
    }

    private var accessibilityLabel: String {
        var label = "Visit to \(visit.displayName)"
        if let city = visit.city {
            label += " in \(city)"
        }
        if visit.isFavorite {
            label += ", favorite"
        }
        label += ", duration \(formatDuration(visit.duration))"
        return label
    }
}

// MARK: - Route Glass Card

struct RouteGlassCard: View {
    let route: RouteSegment
    let onTap: (() -> Void)?

    init(route: RouteSegment, onTap: (() -> Void)? = nil) {
        self.route = route
        self.onTap = onTap
    }

    var body: some View {
        Button {
            onTap?()
        } label: {
            GlassCard(variant: .route) {
                HStack(spacing: 12) {
                    Image(systemName: transportIcon(route.transportType))
                        .font(.title2)
                        .foregroundColor(.seaGlass)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(transportName(route.transportType))
                            .font(.headline)
                            .foregroundColor(.primary)

                        HStack(spacing: 8) {
                            Text("\(Int(route.distanceMeters / 1000)) km")
                                .font(.caption)
                                .foregroundColor(.secondary)

                            let duration = Double(route.endTime.epochSeconds) - Double(route.startTime.epochSeconds)
                            if duration > 0 {
                                Text("• \(Int(duration / 60)) min")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }

                    Spacer()

                    // Confidence indicator
                    if route.confidence < 0.7 {
                        Image(systemName: "questionmark.circle")
                            .foregroundColor(.secondary)
                            .font(.title3)
                    }
                }
            }
        }
        .buttonStyle(PlainButtonStyle())
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(.isButton)
    }

    private var accessibilityLabel: String {
        let distance = Int(route.distanceMeters / 1000)
        let duration = Double(route.endTime.epochSeconds) - Double(route.startTime.epochSeconds)
        var label = "Route by \(transportName(route.transportType)), \(distance) kilometers"
        if duration > 0 {
            label += ", \(Int(duration / 60)) minutes"
        }
        if route.confidence < 0.7 {
            label += ", low confidence"
        }
        return label
    }
}

// MARK: - Stat Glass Card

struct StatGlassCard: View {
    let title: String
    let value: String
    let icon: String
    let tint: Color

    init(title: String, value: String, icon: String, tint: Color = .coolSteel) {
        self.title = title
        self.value = value
        self.icon = icon
        self.tint = tint
    }

    var body: some View {
        GlassCard(variant: .stat) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 32))
                    .foregroundColor(tint)

                Text(value)
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.primary)

                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity)
        }
        .accessibilityLabel("\(title): \(value)")
        .accessibilityAddTraits(.isStaticText)
    }
}

// MARK: - Summary Glass Card

struct SummaryGlassCard: View {
    let title: String
    let subtitle: String?
    let icon: String
    let stats: [(icon: String, label: String, value: String)]

    init(
        title: String,
        subtitle: String? = nil,
        icon: String,
        stats: [(icon: String, label: String, value: String)]
    ) {
        self.title = title
        self.subtitle = subtitle
        self.icon = icon
        self.stats = stats
    }

    var body: some View {
        GlassCard(variant: .summary) {
            VStack(alignment: .leading, spacing: 12) {
                // Header
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Image(systemName: icon)
                            .foregroundColor(.blueSlate)
                        Text(title)
                            .font(.headline)
                            .foregroundColor(.primary)
                    }

                    if let subtitle = subtitle {
                        Text(subtitle)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                // Stats
                HStack(spacing: 16) {
                    ForEach(stats.indices, id: \.self) { index in
                        GlassSummaryStatItem(
                            icon: stats[index].icon,
                            label: stats[index].label,
                            value: stats[index].value
                        )
                        .frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(.isSummaryElement)
    }

    private var accessibilityLabel: String {
        var label = title
        if let subtitle = subtitle {
            label += ", \(subtitle)"
        }
        label += ". "
        label += stats.map { "\($0.label): \($0.value)" }.joined(separator: ", ")
        return label
    }
}

// MARK: - Summary Stat Item

private struct GlassSummaryStatItem: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(.coastalPath)
            Text(value)
                .font(.headline)
                .foregroundColor(.primary)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Metadata Chip

private struct MetadataChip: View {
    let icon: String?
    let text: String

    var body: some View {
        HStack(spacing: 4) {
            if let icon = icon {
                Image(systemName: icon)
                    .font(.caption)
            }
            Text(text)
                .font(.caption)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .glassEffectTinted(.coastalPath, opacity: 0.6)
        .cornerRadius(4)
        .foregroundColor(.primary)
    }
}


// formatDuration is defined in SharedUIHelpers.swift

// MARK: - Previews (Disabled - require proper Kotlin types)

/*
#Preview("Visit Card") {
    VStack(spacing: 16) {
        VisitGlassCard(
            visit: PlaceVisit(
                id: 1,
                latitude: 0,
                longitude: 0,
                arrivalTime: Date(),
                departureTime: Date().addingTimeInterval(3600),
                category: .home,
                poiName: "Home",
                approximateAddress: "123 Main St",
                city: "San Francisco",
                country: "USA",
                userLabel: nil,
                userNotes: "Had a great time relaxing",
                isFavorite: true,
                tripId: nil,
                photoCount: 0
            )
        )
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Stat Cards") {
    HStack(spacing: 8) {
        StatGlassCard(
            title: "Distance",
            value: "1,234 km",
            icon: "ruler",
            tint: .coastalPath
        )
        StatGlassCard(
            title: "Countries",
            value: "12",
            icon: "globe",
            tint: .seaGlass
        )
    }
    .padding()
    .background(Color.backgroundLight)
}
*/
