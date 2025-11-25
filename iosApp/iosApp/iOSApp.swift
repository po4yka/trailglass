import SwiftUI
import Shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var authStateManager = AuthStateManager()

    var body: some Scene {
        WindowGroup {
            AppRootView(appComponent: appDelegate.appComponent, authStateManager: authStateManager)
                .onAppear {
                    print("[iOSApp] onAppear - starting auth observation")
                    authStateManager.startObserving(authController: appDelegate.appComponent.authController)
                }
        }
    }
}

/// Root view that manages authentication state
struct AppRootView: View {
    let appComponent: AppComponent
    @ObservedObject var authStateManager: AuthStateManager

    var body: some View {
        let _ = print("[AppRootView] body - isAuthenticated: \(authStateManager.isAuthenticated)")
        Group {
            if authStateManager.isAuthenticated {
                // Show main app
                MainTabView(appComponent: appComponent)
            } else {
                // Show authentication flow
                AuthRootView(authController: appComponent.authController)
            }
        }
    }
}

/// Manages authentication state observation from Kotlin
/// Uses NotificationCenter to receive state updates broadcast by LoginViewModel
class AuthStateManager: ObservableObject {
    @Published var isAuthenticated: Bool = false

    private var notificationObserver: NSObjectProtocol?
    private var isObserving = false

    func startObserving(authController: AuthController) {
        // Only start observing once
        guard !isObserving else {
            print("[AuthStateManager] Already observing, skipping")
            return
        }
        isObserving = true
        print("[AuthStateManager] startObserving called")

        // Set initial state
        if let initialState = authController.state.value {
            let shouldAuth = initialState is AuthController.AuthStateAuthenticated ||
                             initialState is AuthController.AuthStateGuest
            print("[AuthStateManager] Initial state: \(type(of: initialState)), shouldAuth: \(shouldAuth)")
            isAuthenticated = shouldAuth
        }

        // Listen for auth state changes broadcast by LoginViewModel
        print("[AuthStateManager] Setting up notification observer...")
        notificationObserver = NotificationCenter.default.addObserver(
            forName: .authStateChanged,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self = self,
                  let state = notification.userInfo?["state"] as? AuthState else {
                print("[AuthStateManager] Notification received but state invalid")
                return
            }

            print("[AuthStateManager] Received state via notification: \(type(of: state))")

            if state is AuthController.AuthStateAuthenticated ||
               state is AuthController.AuthStateGuest {
                print("[AuthStateManager] Setting isAuthenticated = true")
                self.isAuthenticated = true
            } else if state is AuthController.AuthStateUnauthenticated {
                print("[AuthStateManager] Setting isAuthenticated = false")
                self.isAuthenticated = false
            }
        }
        print("[AuthStateManager] Notification observer set up")
    }

    deinit {
        print("[AuthStateManager] deinit")
        if let observer = notificationObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
}
