import Foundation
import MapKit
import Shared

/// Custom overlay for rendering a heatmap on MKMapView
class HeatmapOverlay: NSObject, MKOverlay {
    let points: [HeatmapPoint]
    let radius: Int
    let opacity: Float
    let gradient: HeatmapGradient

    var coordinate: CLLocationCoordinate2D
    var boundingMapRect: MKMapRect

    init(data: HeatmapData) {
        self.points = data.points
        self.radius = Int(data.radius)
        self.opacity = data.opacity
        self.gradient = data.gradient

        // Calculate bounding box
        if points.isEmpty {
            self.coordinate = CLLocationCoordinate2D(latitude: 0, longitude: 0)
            self.boundingMapRect = MKMapRect.null
        } else {
            var minLat = 90.0
            var maxLat = -90.0
            var minLon = 180.0
            var maxLon = -180.0

            for point in points {
                minLat = min(minLat, point.coordinate.latitude)
                maxLat = max(maxLat, point.coordinate.latitude)
                minLon = min(minLon, point.coordinate.longitude)
                maxLon = max(maxLon, point.coordinate.longitude)
            }

            let centerLat = (minLat + maxLat) / 2
            let centerLon = (minLon + maxLon) / 2
            self.coordinate = CLLocationCoordinate2D(latitude: centerLat, longitude: centerLon)

            let topLeft = MKMapPoint(CLLocationCoordinate2D(latitude: maxLat, longitude: minLon))
            let bottomRight = MKMapPoint(CLLocationCoordinate2D(latitude: minLat, longitude: maxLon))

            // Add padding for radius
            let padding = Double(radius) * 1000.0 // Approximate padding in map points

            self.boundingMapRect = MKMapRect(
                x: topLeft.x - padding,
                y: topLeft.y - padding,
                width: abs(bottomRight.x - topLeft.x) + (padding * 2),
                height: abs(bottomRight.y - topLeft.y) + (padding * 2)
            )
        }

        super.init()
    }
}
