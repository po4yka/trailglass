import Foundation
import MapKit
import Shared

/// Custom renderer for drawing heatmap overlays
class HeatmapRenderer: MKOverlayRenderer {
    private let heatmapOverlay: HeatmapOverlay

    init(overlay: HeatmapOverlay) {
        self.heatmapOverlay = overlay
        super.init(overlay: overlay)
    }

    override func draw(_ mapRect: MKMapRect, zoomScale: MKZoomScale, in context: CGContext) {
        guard !heatmapOverlay.points.isEmpty else { return }

        let points = heatmapOverlay.points
        let radius = CGFloat(heatmapOverlay.radius) / zoomScale // Scale radius by zoom
        let opacity = CGFloat(heatmapOverlay.opacity)

        // 1. Draw intensity map (grayscale)
        // Create an offscreen context for the intensity map
        let scale = UIScreen.main.scale
        let rect = self.rect(for: mapRect)

        // Skip if rect is invalid
        if rect.width <= 0 || rect.height <= 0 { return }

        context.saveGState()

        // Clip to the visible map rect
        context.clip(to: rect)

        // Create a transparency layer for blending
        context.beginTransparencyLayer(auxiliaryInfo: nil)

        // Draw each point as a radial gradient
        for point in points {
            let mapPoint = MKMapPoint(
                CLLocationCoordinate2D(
                    latitude: point.coordinate.latitude,
                    longitude: point.coordinate.longitude
                )
            )

            // Check if point is within visible rect (plus padding)
            let pointRect = MKMapRect(
                x: mapPoint.x, y: mapPoint.y, width: 0, height: 0
            ).insetBy(dx: -Double(heatmapOverlay.radius) * 2000, dy: -Double(heatmapOverlay.radius) * 2000)

            if !mapRect.intersects(pointRect) {
                continue
            }

            let cgPoint = self.point(for: mapPoint)
            let intensity = CGFloat(point.intensity)

            // Draw radial gradient for the point
            // We draw a soft circle with alpha based on intensity
            let colors = [
                UIColor(white: 1.0, alpha: intensity * 0.5).cgColor,
                UIColor(white: 1.0, alpha: 0.0).cgColor
            ] as CFArray

            let locations: [CGFloat] = [0.0, 1.0]

            if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(), colors: colors, locations: locations) {
                context.drawRadialGradient(
                    gradient,
                    startCenter: cgPoint,
                    startRadius: 0,
                    endCenter: cgPoint,
                    endRadius: radius,
                    options: .drawsBeforeStartLocation
                )
            }
        }

        context.endTransparencyLayer()

        // 2. Colorize the intensity map
        // This is a simplified approach: we just draw a colored overlay for now
        // A full implementation would map grayscale values to the gradient colors
        // using a lookup table or pixel manipulation, but that's complex in Core Graphics alone.
        // Instead, we'll use the dominant colors from the gradient to tint the layer.

        // For a true heatmap look without pixel manipulation, we can use the gradient colors
        // but this simplified renderer is a good starting point for mobile performance.

        // Apply the gradient colors (simplified)
        // We'll use the middle color of the gradient to tint the heatmap
        if let middleColorInt = heatmapOverlay.gradient.colors.dropFirst().first {
             let color = UIColor(rgb: Int(middleColorInt)).withAlphaComponent(opacity)
             context.setFillColor(color.cgColor)
             context.setBlendMode(.sourceAtop)
             context.fill(rect)
        }

        context.restoreGState()
    }
}
