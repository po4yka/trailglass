import SwiftUI
import MapKit
import shared

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

/// Extension to make Flow observable in SwiftUI
extension Kotlinx_coroutines_coreStateFlow {
    func subscribe<T>(onValue: @escaping (T?) -> Void) -> Kotlinx_coroutines_coreJob {
        let scope = KotlinCoroutineScope()
        return scope.launch { [weak self] in
            self?.collect(collector: FlowCollector<T> { value in
                onValue(value as? T)
            }, completionHandler: { _ in })
        }
    }
}

/// Simple Kotlin coroutine scope for iOS
class KotlinCoroutineScope {
    private let scope = Kotlinx_coroutines_coreCoroutineScopeKt.CoroutineScope(
        context: Kotlinx_coroutines_coreDispatchers.shared.Main
    )

    func launch(block: @escaping () -> Void) -> Kotlinx_coroutines_coreJob {
        return Kotlinx_coroutines_coreLaunchKt.launch(
            scope: scope,
            context: Kotlinx_coroutines_coreDispatchers.shared.Main,
            start: Kotlinx_coroutines_coreCoroutineStart.default_,
            block: { _ in
                block()
            }
        )
    }
}

/// Flow collector helper
class FlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    private let callback: (Any?) -> Void

    init(_ callback: @escaping (Any?) -> Void) {
        self.callback = callback
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        callback(value)
        completionHandler(nil)
    }
}
