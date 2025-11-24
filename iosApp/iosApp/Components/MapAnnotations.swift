import SwiftUI
import MapKit
import Shared

/// Map annotation item that can represent either a marker or cluster
enum MapAnnotationItem: Identifiable {
    case marker(EnhancedMapMarker, isSelected: Bool)
    case cluster(MarkerCluster, isSelected: Bool)

    var id: String {
        switch self {
        case .marker(let marker, _):
            return marker.id
        case .cluster(let cluster, _):
            return cluster.id
        }
    }

    var coordinate: CLLocationCoordinate2D {
        switch self {
        case .marker(let marker, _):
            return CLLocationCoordinate2D(
                latitude: marker.coordinate.latitude,
                longitude: marker.coordinate.longitude
            )
        case .cluster(let cluster, _):
            return CLLocationCoordinate2D(
                latitude: cluster.coordinate.latitude,
                longitude: cluster.coordinate.longitude
            )
        }
    }

    @ViewBuilder
    var annotationView: some MapAnnotationProtocol {
        switch self {
        case .marker(let marker, let isSelected):
            MapAnnotation(coordinate: coordinate) {
                EnhancedMarkerView(marker: marker, isSelected: isSelected)
            }
        case .cluster(let cluster, let isSelected):
            MapAnnotation(coordinate: coordinate) {
                ClusterMarkerView(cluster: cluster, isSelected: isSelected)
            }
        }
    }
}

/// Custom marker view with category-based icons
struct EnhancedMarkerView: View {
    let marker: EnhancedMapMarker
    let isSelected: Bool

    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(markerColor)
                    .frame(width: isSelected ? 44 : 36, height: isSelected ? 44 : 36)
                    .shadow(
                        color: markerColor.opacity(0.5),
                        radius: isSelected ? 4 : 2
                    )
                    .glassEffect(variant: .ultraThin)

                Image(systemName: markerIcon)
                    .foregroundColor(.white)
                    .font(.system(size: isSelected ? 20 : 16, weight: .semibold))
            }

            // Pointer
            Image(systemName: "arrowtriangle.down.fill")
                .foregroundColor(markerColor)
                .font(.system(size: 12))
                .offset(y: -8)
        }
        .animation(MotionConfig.standardSpring, value: isSelected)
    }

    private var markerIcon: String {
        switch marker.category {
        case .home: return "house.fill"
        case .work: return "briefcase.fill"
        case .food: return "fork.knife"
        case .transport: return "car.fill"
        case .accommodation: return "bed.double.fill"
        case .entertainment: return "theatermasks.fill"
        case .shopping: return "cart.fill"
        case .other: return "mappin.circle.fill"
        default: return "mappin.circle.fill"
        }
    }

    private var markerColor: Color {
        switch marker.category {
        case .home: return .coolSteel
        case .work: return .blueSlate
        case .food: return .seaGlass
        case .transport: return .coastalPath
        case .accommodation: return .lightBlue
        case .entertainment: return .mistyLavender
        case .shopping: return .weatheredBrass
        case .other: return .coolSteel
        default: return .coolSteel
        }
    }
}

/// Cluster marker view with count badge
struct ClusterMarkerView: View {
    let cluster: MarkerCluster
    let isSelected: Bool

    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        ZStack {
            Circle()
                .fill(
                    LinearGradient(
                        colors: clusterColors,
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: isSelected ? 56 : 48, height: isSelected ? 56 : 48)
                .shadow(
                    color: .black.opacity(DarkModeConfig.shadowOpacity(for: colorScheme)),
                    radius: isSelected ? 4 : 2
                )
                .glassEffect(variant: .regular)

            Text("\(cluster.count)")
                .font(.system(size: isSelected ? 20 : 16, weight: .bold))
                .foregroundColor(.white)
        }
        .animation(MotionConfig.standardSpring, value: isSelected)
    }

    private var clusterColors: [Color] {
        switch cluster.count {
        case 2...10:
            return [Color.lightBlue, Color.coolSteel]
        case 11...50:
            return [Color.coolSteel, Color.blueSlate]
        default:
            return [Color.blueSlate, Color.jetBlack]
        }
    }
}
