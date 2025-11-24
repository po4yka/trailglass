import SwiftUI
import Shared

/// Place glass card with Liquid Glass styling
struct PlaceGlassCard: View {
    let place: FrequentPlaceItem

    var body: some View {
        GlassCard(variant: .visit) {
            HStack(spacing: 12) {
                // Category icon with glass background
                Image(systemName: place.categoryIcon)
                    .foregroundColor(place.categoryColor)
                    .font(.title2)
                    .frame(width: 44, height: 44)
                    .glassBackground(
                        material: .ultraThin,
                        tint: place.categoryColor,
                        cornerRadius: 10
                    )

                VStack(alignment: .leading, spacing: 6) {
                    // Name and favorite
                    HStack(spacing: 6) {
                        Text(place.displayName)
                            .font(.headline)
                            .foregroundColor(.primary)
                            .lineLimit(1)

                        if place.isFavorite {
                            Image(systemName: "star.fill")
                                .foregroundColor(.warning)
                                .font(.caption)
                        }
                    }

                    // City/location
                    if let city = place.city {
                        Text(city)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    // Visit count and duration
                    HStack(spacing: 12) {
                        HStack(spacing: 4) {
                            Image(systemName: "mappin.circle.fill")
                                .font(.caption)
                            Text("\(place.visitCount)")
                                .font(.caption)
                        }
                        .foregroundColor(.blueSlate)

                        if place.averageDurationMinutes > 0 {
                            HStack(spacing: 4) {
                                Image(systemName: "clock.fill")
                                    .font(.caption)
                                Text(formatDuration(minutes: place.averageDurationMinutes))
                                    .font(.caption)
                            }
                            .foregroundColor(.coolSteel)
                        }
                    }
                }

                Spacer()

                // Significance badge
                VStack(spacing: 4) {
                    Text(place.significanceLabel)
                        .font(.caption2)
                        .fontWeight(.medium)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .glassBackground(
                    material: .ultraThin,
                    tint: place.significanceColor,
                    cornerRadius: 8
                )
                .foregroundColor(place.significanceColor)
            }
        }
    }

    private func formatDuration(minutes: Int) -> String {
        let hours = minutes / 60
        let mins = minutes % 60

        if hours > 0 {
            return "\(hours)h \(mins)m"
        } else {
            return "\(mins)m"
        }
    }
}

/// Empty state view with glass styling
struct EmptyPlacesView: View {
    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "mappin.slash")
                .font(.system(size: 64))
                .foregroundColor(.seaGlass)

            VStack(spacing: 8) {
                Text("No Places Yet")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text("As you travel, TrailGlass will automatically detect and record your frequent places.")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// No search results view with glass styling
struct NoSearchResultsView: View {
    let query: String

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(.blueSlate)

            VStack(spacing: 8) {
                Text("No Results")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text("No places found matching \"\(query)\"")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Error view with glass styling
struct PlacesErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.driftwood)

            VStack(spacing: 8) {
                Text("Error Loading Places")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text(error)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            GlassButton(
                title: "Retry",
                icon: "arrow.clockwise",
                variant: .filled,
                tint: .seaGlass,
                action: onRetry
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
