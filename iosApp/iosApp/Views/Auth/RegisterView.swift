import SwiftUI
import Combine
import Shared

/// Registration screen matching Android RegisterScreen
struct RegisterView: View {
    @StateObject private var viewModel: RegisterViewModel

    let onNavigateToLogin: () -> Void
    let onNavigateBack: () -> Void

    @FocusState private var nameFocused: Bool
    @FocusState private var emailFocused: Bool
    @FocusState private var passwordFocused: Bool
    @FocusState private var confirmPasswordFocused: Bool

    init(
        authController: AuthController,
        onNavigateToLogin: @escaping () -> Void,
        onNavigateBack: @escaping () -> Void
    ) {
        _viewModel = StateObject(wrappedValue: RegisterViewModel(controller: authController))
        self.onNavigateToLogin = onNavigateToLogin
        self.onNavigateBack = onNavigateBack
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    Spacer(minLength: 32)

                    // App logo/title
                    Image(systemName: "map.fill")
                        .font(.system(size: 64))
                        .foregroundColor(.blue)

                    Text("Create Account")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.blue)

                    Text("Start tracking your journeys")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)

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

                    // Display name field
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "person")
                                .foregroundColor(.gray)

                            TextField("Display Name", text: $viewModel.displayName)
                                .textContentType(.name)
                                .focused($nameFocused)
                                .disabled(viewModel.isLoading)
                                .submitLabel(.next)
                                .onSubmit {
                                    emailFocused = true
                                }
                        }
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
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
                                    .submitLabel(.next)
                                    .onSubmit {
                                        confirmPasswordFocused = true
                                    }
                            } else {
                                SecureField("Password", text: $viewModel.password)
                                    .focused($passwordFocused)
                                    .disabled(viewModel.isLoading)
                                    .submitLabel(.next)
                                    .onSubmit {
                                        confirmPasswordFocused = true
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

                        if let error = viewModel.passwordError, !viewModel.password.isEmpty {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(.horizontal, 4)
                        }
                    }

                    // Confirm password field
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "lock")
                                .foregroundColor(.gray)

                            if viewModel.confirmPasswordVisible {
                                TextField("Confirm Password", text: $viewModel.confirmPassword)
                                    .focused($confirmPasswordFocused)
                                    .disabled(viewModel.isLoading)
                                    .submitLabel(.done)
                                    .onSubmit {
                                        viewModel.register()
                                    }
                            } else {
                                SecureField("Confirm Password", text: $viewModel.confirmPassword)
                                    .focused($confirmPasswordFocused)
                                    .disabled(viewModel.isLoading)
                                    .submitLabel(.done)
                                    .onSubmit {
                                        viewModel.register()
                                    }
                            }

                            Button(action: { viewModel.confirmPasswordVisible.toggle() }) {
                                Image(systemName: viewModel.confirmPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(.gray)
                            }
                        }
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(12)
                    }

                    // Register button
                    Button(action: { viewModel.register() }) {
                        if viewModel.isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Create Account")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(viewModel.canRegister ? Color.blue : Color.gray)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .disabled(!viewModel.canRegister || viewModel.isLoading)

                    // Login link
                    HStack {
                        Text("Already have an account?")
                            .foregroundColor(.secondary)

                        Button(action: onNavigateToLogin) {
                            Text("Log in")
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
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onNavigateBack) {
                        Image(systemName: "chevron.left")
                    }
                }
            }
        }
        .onDisappear {
            viewModel.clearError()
        }
    }
}

/// ViewModel for RegisterView using centralized AuthStateObserver
class RegisterViewModel: ObservableObject {
    private var cancellables = Set<AnyCancellable>()

    @Published var displayName: String = ""
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var confirmPassword: String = ""
    @Published var passwordVisible: Bool = false
    @Published var confirmPasswordVisible: Bool = false
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    var passwordMatch: Bool {
        password == confirmPassword
    }

    var passwordError: String? {
        if !password.isEmpty && password.count < 8 {
            return "Password must be at least 8 characters"
        }
        if !confirmPassword.isEmpty && !passwordMatch {
            return "Passwords do not match"
        }
        return nil
    }

    var canRegister: Bool {
        !displayName.isEmpty &&
        !email.isEmpty &&
        !password.isEmpty &&
        passwordMatch &&
        passwordError == nil &&
        !isLoading
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

    func register() {
        guard canRegister else { return }
        AuthStateObserver.shared.register(email: email, password: password, displayName: displayName)
    }

    func continueAsGuest() {
        AuthStateObserver.shared.continueAsGuest()
    }

    func clearError() {
        AuthStateObserver.shared.clearError()
    }
}

#Preview {
    Text("RegisterView Preview - Requires DI setup")
}
