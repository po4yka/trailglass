import SwiftUI
import Shared

/// List of places
struct PlacesList: View {
    let places: [PlaceItem]
    let onPlaceSelected: (PlaceItem) -> Void

    var body: some View {
        List(places) { place in
            PlaceRow(place: place)
                .onTapGesture {
                    onPlaceSelected(place)
                }
        }
        .listStyle(.plain)
    }
}

/// Individual place row
struct PlaceRow: View {
    let place: PlaceItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                // Category icon
                Image(systemName: place.categoryIcon)
                    .foregroundColor(place.categoryColor)
                    .font(.title2)
                    .frame(width: 44, height: 44)
                    .background(place.categoryColor.opacity(0.15))
                    .cornerRadius(8)

                VStack(alignment: .leading, spacing: 4) {
                    Text(place.name ?? "Unknown Place")
                        .font(.headline)

                    if let address = place.address {
                        Text(address)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    HStack(spacing: 12) {
                        Label("\(place.visitCount) visits", systemImage: "mappin.circle")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        if let duration = place.totalDuration {
                            Label(formatDuration(duration), systemImage: "clock")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }

                Spacer()

                if place.isFavorite {
                    Image(systemName: "star.fill")
                        .foregroundColor(.yellow)
                }

                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }

    private func formatDuration(_ seconds: Int64) -> String {
        let hours = seconds / 3600
        let minutes = (seconds % 3600) / 60

        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
}

/// Empty state view
struct EmptyPlacesView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "mappin.slash")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No Places Yet")
                .font(.title2)
                .fontWeight(.semibold)

            Text("As you travel, TrailGlass will automatically detect and record the places you visit.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Error view
struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(.red)

            Text("Error")
                .font(.title2)
                .fontWeight(.semibold)

            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button(action: onRetry) {
                Text("Retry")
                    .fontWeight(.semibold)
            }
            .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
