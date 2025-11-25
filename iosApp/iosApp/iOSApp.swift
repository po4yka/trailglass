import SwiftUI
import Shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @ObservedObject private var authObserver = AuthStateObserver.shared

    var body: some Scene {
        WindowGroup {
            AppRootView(appComponent: appDelegate.appComponent)
                .onAppear {
                    // Start the centralized auth observer once at app launch
                    AuthStateObserver.shared.start(authController: appDelegate.appComponent.authController)
                }
        }
    }
}

/// Root view that manages authentication state
struct AppRootView: View {
    let appComponent: AppComponent
    @ObservedObject private var authObserver = AuthStateObserver.shared

    var body: some View {
        Group {
            if authObserver.isAuthenticated {
                MainTabView(appComponent: appComponent)
            } else {
                AuthRootView(authController: appComponent.authController)
            }
        }
    }
}
