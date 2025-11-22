import SwiftUI
import shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var authStateManager = AuthStateManager()

    var body: some Scene {
        WindowGroup {
            AppRootView(
                appComponent: appDelegate.appComponent,
                authStateManager: authStateManager
            )
            .onAppear {
                authStateManager.observe(authController: appDelegate.appComponent.authController)
            }
        }
    }
}

/// Root view that manages authentication state
struct AppRootView: View {
    let appComponent: AppComponent
    @ObservedObject var authStateManager: AuthStateManager

    var body: some View {
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

    private var stateObserver: Kotlinx_coroutines_coreJob?

    func observe(authController: AuthController) {
        // Set initial state
        isAuthenticated = authController.isAuthenticated

        // Observe state changes
        stateObserver = authController.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                if state is AuthController.AuthStateAuthenticated {
                    self.isAuthenticated = true
                } else if state is AuthController.AuthStateUnauthenticated {
                    self.isAuthenticated = false
                }
                // Don't change state for Loading or Error states
            }
        }
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }
}
