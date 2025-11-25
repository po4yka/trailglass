import Foundation
import Combine
import Shared

/// Centralized singleton that manages a single subscription to the Kotlin AuthController StateFlow.
/// This solves the limitation where Kotlin/Native StateFlow only delivers updates to the first Swift subscriber.
/// All components that need auth state should observe this class instead of subscribing directly.
final class AuthStateObserver: ObservableObject {
    static let shared = AuthStateObserver()

    /// Current authentication state
    @Published private(set) var currentState: AuthState?

    /// Convenience property for checking if user is authenticated (including guest mode)
    @Published private(set) var isAuthenticated: Bool = false

    /// Convenience property for checking if user is in guest mode
    var isGuest: Bool {
        currentState is AuthController.AuthStateGuest
    }

    /// Convenience property for checking if authentication is loading
    var isLoading: Bool {
        currentState is AuthController.AuthStateLoading
    }

    /// Current error message if in error state
    var errorMessage: String? {
        (currentState as? AuthController.AuthStateError)?.message
    }

    private var stateObserver: KotlinJob?
    private var isStarted = false
    /// Strong reference to auth controller - it's a long-lived singleton from DI
    private var authController: AuthController?

    private init() {}

    /// Start observing the auth controller. Should be called once at app startup.
    /// - Parameter authController: The Kotlin AuthController to observe
    func start(authController: AuthController) {
        AppLogger.info("AuthStateObserver.start() called, isStarted: \(isStarted)", category: "Auth")
        guard !isStarted else {
            AppLogger.debug("AuthStateObserver already started, skipping", category: "Auth")
            return
        }
        isStarted = true
        self.authController = authController
        AppLogger.info("AuthController stored in observer", category: "Auth")

        // Set initial state synchronously
        // Note: StateFlow.value returns Any? in Swift, need to cast to AuthState
        if let initialState = authController.state.value as? AuthState {
            AppLogger.debug("Initial auth state: \(type(of: initialState))", category: "Auth")
            updateState(initialState)
        } else {
            AppLogger.warn("Could not get initial auth state", category: "Auth")
        }

        // Subscribe to state changes
        stateObserver = authController.state.subscribe { [weak self] (state: AuthState?) in
            guard let self = self, let state = state else { return }
            DispatchQueue.main.async {
                AppLogger.debug("Auth state changed to: \(type(of: state))", category: "Auth")
                self.updateState(state)
            }
        }
    }

    /// Stop observing (typically not needed, but available for cleanup)
    func stop() {
        stateObserver?.cancel()
        stateObserver = nil
        isStarted = false
        authController = nil
    }

    private func updateState(_ state: AuthState) {
        currentState = state
        isAuthenticated = state is AuthController.AuthStateAuthenticated ||
                          state is AuthController.AuthStateGuest

        // Also post notification for any legacy observers
        NotificationCenter.default.post(
            name: .authStateChanged,
            object: self,
            userInfo: ["state": state]
        )
    }

    // MARK: - Auth Actions (delegate to controller)

    func login(email: String, password: String) {
        authController?.login(email: email, password: password)
    }

    func register(email: String, password: String, displayName: String) {
        authController?.register(email: email, password: password, displayName: displayName)
    }

    func continueAsGuest() {
        AppLogger.debug("continueAsGuest called, authController: \(authController != nil ? "present" : "nil")", category: "Auth")
        if let controller = authController {
            AppLogger.info("Calling authController.continueAsGuest()", category: "Auth")
            controller.continueAsGuest()
        } else {
            AppLogger.error("authController is nil in continueAsGuest()", category: "Auth")
        }
    }

    func logout() {
        authController?.logout()
    }

    func clearError() {
        authController?.clearError()
    }
}
