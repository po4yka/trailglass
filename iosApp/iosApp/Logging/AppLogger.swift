import Foundation
import os.log
import Shared

/// Centralized logging for the iOS app with os_log integration.
///
/// This logger provides:
/// - Consistent logging across Swift code
/// - Integration with Apple's os_log system
/// - Bridge to Kotlin LogBuffer for in-app log viewing
///
/// Usage:
/// ```swift
/// AppLogger.info("App started", category: "AppDelegate")
/// AppLogger.debug("User tapped button", category: "UI")
/// AppLogger.error("Failed to load data", category: "Sync")
/// ```
enum AppLogger {
    /// The app's bundle identifier used as os_log subsystem
    private static let subsystem = "com.po4yka.trailglass"

    /// Cache of os_log instances by category
    private static var loggers: [String: OSLog] = [:]

    /// Gets or creates an OSLog instance for the specified category
    private static func getLogger(category: String) -> OSLog {
        if let logger = loggers[category] {
            return logger
        }
        let logger = OSLog(subsystem: subsystem, category: category)
        loggers[category] = logger
        return logger
    }

    // MARK: - Public Logging Methods

    /// Logs a debug message (only visible in debug builds)
    static func debug(_ message: String, category: String = "Default") {
        let logger = getLogger(category: category)
        os_log(.debug, log: logger, "%{public}@", message)

        #if DEBUG
        addToLogBuffer(level: Shared.LogLevel.debug, tag: category, message: message)
        #endif
    }

    /// Logs an informational message
    static func info(_ message: String, category: String = "Default") {
        let logger = getLogger(category: category)
        os_log(.info, log: logger, "%{public}@", message)
        addToLogBuffer(level: Shared.LogLevel.info, tag: category, message: message)
    }

    /// Logs a notice message (default level)
    static func notice(_ message: String, category: String = "Default") {
        let logger = getLogger(category: category)
        os_log(.default, log: logger, "%{public}@", message)
        addToLogBuffer(level: Shared.LogLevel.info, tag: category, message: message)
    }

    /// Logs a warning message
    static func warn(_ message: String, category: String = "Default") {
        let logger = getLogger(category: category)
        os_log(.error, log: logger, "[WARN] %{public}@", message)
        addToLogBuffer(level: Shared.LogLevel.warning, tag: category, message: message)
    }

    /// Logs an error message
    static func error(_ message: String, category: String = "Default") {
        let logger = getLogger(category: category)
        os_log(.error, log: logger, "%{public}@", message)
        addToLogBuffer(level: Shared.LogLevel.error, tag: category, message: message)
    }

    /// Logs a fault message (critical errors)
    static func fault(_ message: String, category: String = "Default") {
        let logger = getLogger(category: category)
        os_log(.fault, log: logger, "%{public}@", message)
        addToLogBuffer(level: Shared.LogLevel.error, tag: category, message: message)
    }

    // MARK: - Error Logging with Swift Error

    /// Logs an error with a Swift Error object
    static func error(_ error: Error, message: String? = nil, category: String = "Default") {
        let fullMessage = message.map { "\($0): \(error.localizedDescription)" } ?? error.localizedDescription
        self.error(fullMessage, category: category)

        // Also add stack trace for errors
        let stackTrace = Thread.callStackSymbols.joined(separator: "\n")
        addErrorToLogBuffer(tag: category, message: fullMessage, stackTrace: stackTrace)
    }

    // MARK: - Breadcrumb Logging

    /// Logs a breadcrumb for crash analysis.
    /// Breadcrumbs help understand the sequence of events leading to a crash.
    static func breadcrumb(_ message: String, category: String = "Breadcrumb") {
        let logger = getLogger(category: category)
        os_log(.info, log: logger, "[BREADCRUMB] %{public}@", message)
        addBreadcrumbToLogBuffer(tag: category, message: message)

        // Also log to CrashReporter for crash analysis
        CrashReporter.shared.logBreadcrumb(message, category: category)
    }

    /// Logs a user action as a breadcrumb
    static func userAction(_ action: String, screen: String? = nil) {
        let message = screen.map { "[\($0)] \(action)" } ?? action
        breadcrumb(message, category: "UserAction")
    }

    /// Logs a navigation event as a breadcrumb
    static func navigation(from: String?, to: String) {
        let message = from.map { "Navigate: \($0) -> \(to)" } ?? "Navigate to: \(to)"
        breadcrumb(message, category: "Navigation")
    }

    /// Logs a network request as a breadcrumb
    static func networkRequest(_ method: String, url: String, status: Int? = nil) {
        var message = "\(method) \(url)"
        if let status = status {
            message += " -> \(status)"
        }
        breadcrumb(message, category: "Network")
    }

    // MARK: - Private Helpers

    /// Adds a log entry to the Kotlin LogBuffer for in-app viewing
    private static func addToLogBuffer(level: Shared.LogLevel, tag: String, message: String) {
        Task {
            do {
                try await LogBuffer.shared.add(level: level, tag: tag, message: message, stackTrace: nil, isBreadcrumb: false)
            } catch {
                // Silently ignore LogBuffer errors to avoid recursion
            }
        }
    }

    /// Adds a breadcrumb to the LogBuffer
    private static func addBreadcrumbToLogBuffer(tag: String, message: String) {
        Task {
            do {
                try await LogBuffer.shared.addBreadcrumb(tag: tag, message: message)
            } catch {
                // Silently ignore LogBuffer errors to avoid recursion
            }
        }
    }

    /// Adds an error with stack trace to the LogBuffer
    private static func addErrorToLogBuffer(tag: String, message: String, stackTrace: String) {
        Task {
            do {
                try await LogBuffer.shared.add(
                    level: Shared.LogLevel.error,
                    tag: tag,
                    message: message,
                    stackTrace: stackTrace,
                    isBreadcrumb: false
                )
            } catch {
                // Silently ignore LogBuffer errors to avoid recursion
            }
        }
    }
}
