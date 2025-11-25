import Foundation
import os.log
import Shared

/// Swift-level crash reporting helper for iOS.
///
/// This class provides:
/// - NSException handling for Objective-C exceptions
/// - Signal handling for system crashes (SIGABRT, SIGSEGV, etc.)
/// - Breadcrumb logging for crash analysis
/// - Bridge to Kotlin CrashReportingService
///
/// Usage:
/// ```swift
/// CrashReporter.shared.configure(crashReportingService: appComponent.crashReportingService)
/// ```
final class CrashReporter {
    /// Shared singleton instance
    static let shared = CrashReporter()

    /// Reference to Kotlin crash reporting service
    private var crashReportingService: CrashReportingService?

    /// Previous signal handlers to restore on deinit
    private var previousSignalHandlers: [Int32: (@convention(c) (Int32) -> Void)?] = [:]

    /// Signals we monitor for crashes
    private static let monitoredSignals: [Int32] = [
        SIGABRT,  // Abort program
        SIGBUS,   // Bus error
        SIGFPE,   // Floating-point exception
        SIGILL,   // Illegal instruction
        SIGSEGV,  // Segmentation violation
        SIGTRAP   // Trace/breakpoint trap
    ]

    private init() {}

    /// Configure the crash reporter with the Kotlin service
    /// - Parameter crashReportingService: The Kotlin crash reporting service from DI
    func configure(crashReportingService: CrashReportingService) {
        self.crashReportingService = crashReportingService
        setupSignalHandlers()
        AppLogger.info("CrashReporter configured with service", category: "Crash")
    }

    /// Record a non-fatal exception
    /// - Parameters:
    ///   - error: The error to record
    ///   - context: Additional context information
    func recordException(_ error: Error, context: [String: Any]? = nil) {
        guard let service = crashReportingService else {
            AppLogger.warn("CrashReporter not configured, cannot record exception", category: "Crash")
            return
        }

        // Log context as custom keys
        if let ctx = context {
            for (key, val) in ctx {
                service.setCustomKeyString(key: key, value: String(describing: val))
            }
        }

        // Create a Kotlin exception from Swift error
        let exception = KotlinException(message: error.localizedDescription)
        service.recordException(throwable: exception)

        AppLogger.error("Recorded exception: \(error.localizedDescription)", category: "Crash")
    }

    /// Record a non-fatal NSException
    /// - Parameter exception: The Objective-C exception
    func recordNSException(_ exception: NSException) {
        guard let service = crashReportingService else {
            AppLogger.warn("CrashReporter not configured, cannot record NSException", category: "Crash")
            return
        }

        let message = """
            NSException: \(exception.name.rawValue)
            Reason: \(exception.reason ?? "Unknown")
            UserInfo: \(exception.userInfo ?? [:])
            CallStack: \(exception.callStackSymbols.joined(separator: "\n"))
            """

        service.log(message: "NSException caught: \(exception.name.rawValue)")

        let kotlinException = KotlinException(message: message)
        service.recordException(throwable: kotlinException)

        AppLogger.error("Recorded NSException: \(exception.name.rawValue)", category: "Crash")
    }

    /// Log a breadcrumb for crash analysis
    /// - Parameters:
    ///   - message: The breadcrumb message
    ///   - category: Optional category for grouping
    func logBreadcrumb(_ message: String, category: String? = nil) {
        guard let service = crashReportingService else { return }

        let fullMessage = category.map { "[\($0)] \(message)" } ?? message
        service.log(message: fullMessage)
    }

    /// Set a custom key for crash context
    /// All values are converted to String for simplicity
    /// - Parameters:
    ///   - key: The key name
    ///   - value: The value (will be converted to String)
    func setCustomKey(_ key: String, value: Any) {
        guard let service = crashReportingService else { return }

        // All values are converted to String for Kotlin interop simplicity
        let stringValue = String(describing: value)
        service.setCustomKeyString(key: key, value: stringValue)
    }

    /// Set the user identifier for crash reports
    /// - Parameter userId: The user ID
    func setUserId(_ userId: String) {
        crashReportingService?.setUserId(userId_: userId)
    }

    // MARK: - Signal Handling

    private func setupSignalHandlers() {
        // Store current handlers and install our own
        for sig in CrashReporter.monitoredSignals {
            let previousHandler = signal(sig, crashSignalHandler)
            previousSignalHandlers[sig] = previousHandler
        }
        AppLogger.debug("Signal handlers installed for crash monitoring", category: "Crash")
    }
}

// MARK: - Signal Handler

/// Global signal handler function (must be C-compatible)
private func crashSignalHandler(signal: Int32) {
    let signalName = signalName(for: signal)

    // Log the crash
    AppLogger.fault("FATAL SIGNAL: \(signalName) (\(signal))", category: "Crash")

    // Try to log to crash service
    CrashReporter.shared.logBreadcrumb("Fatal signal received: \(signalName)", category: "Crash")

    // Get stack trace
    let callStack = Thread.callStackSymbols.joined(separator: "\n")
    AppLogger.fault("Stack trace:\n\(callStack)", category: "Crash")

    // Re-raise the signal with default handler to let the system handle it
    Foundation.signal(signal, SIG_DFL)
    raise(signal)
}

/// Convert signal number to human-readable name
private func signalName(for signal: Int32) -> String {
    switch signal {
    case SIGABRT: return "SIGABRT"
    case SIGBUS: return "SIGBUS"
    case SIGFPE: return "SIGFPE"
    case SIGILL: return "SIGILL"
    case SIGSEGV: return "SIGSEGV"
    case SIGTRAP: return "SIGTRAP"
    default: return "SIGNAL_\(signal)"
    }
}
