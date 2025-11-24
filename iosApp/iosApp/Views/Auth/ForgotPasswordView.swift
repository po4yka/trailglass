import SwiftUI
import Shared

/// Forgot password screen matching Android ForgotPasswordScreen
struct ForgotPasswordView: View {
    let onNavigateBack: () -> Void

    @State private var email: String = ""
    @State private var isSubmitting: Bool = false
    @State private var isSuccess: Bool = false
    @State private var errorMessage: String?

    @FocusState private var emailFocused: Bool

    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Spacer()

                // Icon
                Image(systemName: "lock.rotation")
                    .font(.system(size: 80))
                    .foregroundColor(.blue)

                Text("Reset Password")
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundColor(.blue)

                if isSuccess {
                    // Success state
                    VStack(spacing: 16) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.blue.opacity(0.1))

                            VStack(spacing: 16) {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 48))
                                    .foregroundColor(.blue)

                                Text("Reset Link Sent!")
                                    .font(.title2)
                                    .fontWeight(.bold)

                                Text("If an account exists with this email, you will receive a password reset link shortly.")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal)
                            }
                            .padding(24)
                        }

                        Button(action: onNavigateBack) {
                            Text("Return to Login")
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                        }
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                } else {
                    // Form state
                    Text("Enter your email address and we'll send you a link to reset your password.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)

                    // Error message
                    if let errorMessage = errorMessage {
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

                            TextField("Email", text: $email)
                                .keyboardType(.emailAddress)
                                .textContentType(.emailAddress)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                                .focused($emailFocused)
                                .disabled(isSubmitting)
                                .submitLabel(.done)
                                .onSubmit {
                                    submitResetRequest()
                                }
                                .onChange(of: email) { _ in
                                    errorMessage = nil
                                }
                        }
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(12)
                    }

                    // Submit button
                    Button(action: submitResetRequest) {
                        if isSubmitting {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Send Reset Link")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(canSubmit ? Color.blue : Color.gray)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .disabled(!canSubmit || isSubmitting)

                    Button(action: onNavigateBack) {
                        Text("Back to Login")
                            .fontWeight(.medium)
                    }
                    .disabled(isSubmitting)
                }

                Spacer()
            }
            .padding(24)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onNavigateBack) {
                        Image(systemName: "chevron.left")
                    }
                    .disabled(isSubmitting)
                }
            }
        }
    }

    private var canSubmit: Bool {
        !email.isEmpty && !isSubmitting
    }

    private func submitResetRequest() {
        guard canSubmit else { return }

        emailFocused = false
        isSubmitting = true

        // TODO: Implement password reset API call when backend is ready
        // For now, show not implemented message
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            errorMessage = "Password reset is not yet implemented. Please contact support."
            isSubmitting = false

            // Once backend is ready, uncomment:
            // isSuccess = true
        }
    }
}

#Preview {
    ForgotPasswordView(onNavigateBack: {})
}
