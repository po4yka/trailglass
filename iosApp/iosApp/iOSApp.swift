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
class AuthStateManager: ObservableObject {
    @Published var isAuthenticated: Bool = false

    private var stateObserver: KotlinJob?
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
            DispatchQueue.main.async {
                self.isAuthenticated = shouldAuth
            }
        }

        // Subscribe using the extension method
        print("[AuthStateManager] Setting up subscription...")
        stateObserver = authController.state.subscribe { [weak self] (state: AuthState?) in
            if let state = state {
                print("[AuthStateManager] Received state: \(type(of: state))")
            } else {
                print("[AuthStateManager] Received state: nil")
            }
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                if state is AuthController.AuthStateAuthenticated ||
                   state is AuthController.AuthStateGuest {
                    print("[AuthStateManager] Setting isAuthenticated = true")
                    self.isAuthenticated = true
                } else if state is AuthController.AuthStateUnauthenticated {
                    print("[AuthStateManager] Setting isAuthenticated = false")
                    self.isAuthenticated = false
                }
            }
        }
        print("[AuthStateManager] Subscription created: \(stateObserver != nil)")
    }

    deinit {
        print("[AuthStateManager] deinit")
        stateObserver?.cancel()
    }
}
