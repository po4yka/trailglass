import Foundation
import Shared

// MARK: - KotlinDuration Extensions

extension KotlinDuration {
    var inWholeHours: Int64 {
        return self.inWholeMilliseconds / (1000 * 60 * 60)
    }

    var inWholeMinutes: Int64 {
        return self.inWholeMilliseconds / (1000 * 60)
    }
}

// MARK: - Kotlinx_coroutines_coreStateFlow Extensions

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

// MARK: - Kotlin Coroutine Helpers

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
