import SwiftUI
import MapKit
import Shared

/// Custom marker annotation
class MarkerAnnotation: NSObject, MKAnnotation {
    let id: String
    let coordinate: CLLocationCoordinate2D
    let title: String?
    let subtitle: String?
    let marker: Shared.MapMarker

    init(marker: Shared.MapMarker) {
        self.id = marker.id
        self.coordinate = CLLocationCoordinate2D(
            latitude: marker.coordinate.latitude,
            longitude: marker.coordinate.longitude
        )
        self.title = marker.title
        self.subtitle = marker.snippet
        self.marker = marker
        super.init()
    }
}

/// Custom polyline for routes
class RoutePolyline: MKPolyline {
    var id: String?
    var transportType: String?
    var colorInt: Int32?
}

/// Error banner
struct ErrorBanner: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.white)

            Text(message)
                .foregroundColor(.white)
                .font(.body)

            Spacer()

            Button(action: onDismiss) {
                Image(systemName: "xmark.circle.fill")
                    .foregroundColor(.white)
            }
        }
        .padding()
        .background(Color.adaptiveWarning)
        .cornerRadius(8)
    }
}

/// Location permission prompt
struct LocationPermissionPrompt: View {
    let onRequestPermission: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "location.slash.fill")
                .font(.largeTitle)
                .foregroundColor(.adaptiveWarning)

            Text("Location Permission Required")
                .font(.headline)

            Text("TrailGlass needs access to your location to show you on the map and track your trips.")
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)

            Button(action: onRequestPermission) {
                Text("Enable Location")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}
