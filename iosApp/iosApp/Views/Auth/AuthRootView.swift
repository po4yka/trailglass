import SwiftUI
import shared

/// Root view for authentication flow managing navigation between auth screens
struct AuthRootView: View {
    @StateObject private var viewModel: AuthRootViewModel

    init(authController: AuthController) {
        _viewModel = StateObject(wrappedValue: AuthRootViewModel(authController: authController))
    }

    var body: some View {
        NavigationStack(path: $viewModel.navigationPath) {
            LoginView(
                authController: viewModel.authController,
                onNavigateToRegister: {
                    viewModel.navigationPath.append(AuthDestination.register)
                },
                onNavigateToForgotPassword: {
                    viewModel.navigationPath.append(AuthDestination.forgotPassword)
                }
            )
            .navigationDestination(for: AuthDestination.self) { destination in
                switch destination {
                case .register:
                    RegisterView(
                        authController: viewModel.authController,
                        onNavigateToLogin: {
                            viewModel.navigationPath.removeLast()
                        },
                        onNavigateBack: {
                            viewModel.navigationPath.removeLast()
                        }
                    )

                case .forgotPassword:
                    ForgotPasswordView(
                        onNavigateBack: {
                            viewModel.navigationPath.removeLast()
                        }
                    )
                }
            }
        }
    }
}

/// Navigation destinations for authentication flow
enum AuthDestination: Hashable {
    case register
    case forgotPassword
}

/// ViewModel for AuthRootView
class AuthRootViewModel: ObservableObject {
    let authController: AuthController

    @Published var navigationPath: [AuthDestination] = []

    init(authController: AuthController) {
        self.authController = authController
    }
}

#Preview {
    Text("AuthRootView Preview - Requires DI setup")
}
