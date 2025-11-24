import SwiftUI
import MapKit
import Shared

/// UIColor extension for RGB color from integer
extension UIColor {
    convenience init(rgb: Int) {
        self.init(
            red: CGFloat((rgb >> 16) & 0xFF) / 255.0,
            green: CGFloat((rgb >> 8) & 0xFF) / 255.0,
            blue: CGFloat(rgb & 0xFF) / 255.0,
            alpha: CGFloat((rgb >> 24) & 0xFF) / 255.0
        )
    }
}


