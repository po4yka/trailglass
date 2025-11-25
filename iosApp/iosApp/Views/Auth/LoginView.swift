import SwiftUI
import Combine
import Shared

/// Login screen for user authentication matching Android LoginScreen
struct LoginView: View {
    @StateObject private var viewModel: LoginViewModel

    let onNavigateToRegister: () -> Void
    let onNavigateToForgotPassword: () -> Void

    @FocusState private var emailFocused: Bool
    @FocusState private var passwordFocused: Bool

    init(
        authController: AuthController,
        onNavigateToRegister: @escaping () -> Void,
        onNavigateToForgotPassword: @escaping () -> Void
    ) {
        _viewModel = StateObject(wrappedValue: LoginViewModel(controller: authController))
        self.onNavigateToRegister = onNavigateToRegister
        self.onNavigateToForgotPassword = onNavigateToForgotPassword
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                Spacer(minLength: 60)

                // App logo/title
                Image(systemName: "map.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.blue)

                Text("TrailGlass")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.blue)

                Text("Your journeys, captured as they unfold")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                // Error message
                if let errorMessage = viewModel.errorMessage {
                    HStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.red)

                        Text(errorMessage)
                            .font(.body)
                            .foregroundColor(.red)

                        Spacer()
                    }
                    .padding()
                    .background(Color.red.opacity(0.1))
                    .cornerRadius(12)
                }

                // Email field
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "envelope")
                            .foregroundColor(.gray)

                        TextField("Email", text: $viewModel.email)
                            .keyboardType(.emailAddress)
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .focused($emailFocused)
                            .disabled(viewModel.isLoading)
                            .submitLabel(.next)
                            .onSubmit {
                                passwordFocused = true
                            }
                            .onChange(of: viewModel.email) { _ in
                                viewModel.clearError()
                            }
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(12)
                }

                // Password field
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "lock")
                            .foregroundColor(.gray)

                        if viewModel.passwordVisible {
                            TextField("Password", text: $viewModel.password)
                                .focused($passwordFocused)
                                .disabled(viewModel.isLoading)
                                .submitLabel(.done)
                                .onSubmit {
                                    viewModel.login()
                                }
                                .onChange(of: viewModel.password) { _ in
                                    viewModel.clearError()
                                }
                        } else {
                            SecureField("Password", text: $viewModel.password)
                                .focused($passwordFocused)
                                .disabled(viewModel.isLoading)
                                .submitLabel(.done)
                                .onSubmit {
                                    viewModel.login()
                                }
                                .onChange(of: viewModel.password) { _ in
                                    viewModel.clearError()
                                }
                        }

                        Button(action: { viewModel.passwordVisible.toggle() }) {
                            Image(systemName: viewModel.passwordVisible ? "eye.slash" : "eye")
                                .foregroundColor(.gray)
                        }
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(12)
                }

                // Forgot password
                HStack {
                    Spacer()
                    Button(action: onNavigateToForgotPassword) {
                        Text("Forgot password?")
                            .font(.body)
                    }
                    .disabled(viewModel.isLoading)
                }

                // Login button
                Button(action: { viewModel.login() }) {
                    if viewModel.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Log In")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(viewModel.canLogin ? Color.blue : Color.gray)
                .foregroundColor(.white)
                .cornerRadius(12)
                .disabled(!viewModel.canLogin || viewModel.isLoading)

                // Register link
                HStack {
                    Text("Don't have an account?")
                        .foregroundColor(.secondary)

                    Button(action: onNavigateToRegister) {
                        Text("Sign up")
                            .fontWeight(.semibold)
                    }
                    .disabled(viewModel.isLoading)
                }

                // Continue as Guest
                Divider()
                    .padding(.vertical, 8)

                Button(action: { viewModel.continueAsGuest() }) {
                    HStack {
                        Image(systemName: "person")
                            .font(.system(size: 16))
                        Text("Continue as Guest")
                    }
                }
                .disabled(viewModel.isLoading)

                Text("No account needed. All data stored locally only.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 4)

                Spacer()
            }
            .padding(24)
        }
    }
}

/// ViewModel for LoginView using centralized AuthStateObserver
class LoginViewModel: ObservableObject {
    private var cancellables = Set<AnyCancellable>()

    @Published var email: String = ""
    @Published var password: String = ""
    @Published var passwordVisible: Bool = false
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    var canLogin: Bool {
        !email.isEmpty && !password.isEmpty && !isLoading
    }

    init(controller: AuthController) {
        // Observe the centralized auth state
        AuthStateObserver.shared.$currentState
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                guard let self = self, let state = state else { return }
                if state is AuthController.AuthStateLoading {
                    self.isLoading = true
                    self.errorMessage = nil
                } else if let errorState = state as? AuthController.AuthStateError {
                    self.isLoading = false
                    self.errorMessage = errorState.message
                } else {
                    self.isLoading = false
                    self.errorMessage = nil
                }
            }
            .store(in: &cancellables)
    }

    func login() {
        guard canLogin else { return }
        AuthStateObserver.shared.login(email: email, password: password)
    }

    func continueAsGuest() {
        AuthStateObserver.shared.continueAsGuest()
    }

    func clearError() {
        AuthStateObserver.shared.clearError()
    }
}

#Preview {
    Text("LoginView Preview - Requires DI setup")
}
