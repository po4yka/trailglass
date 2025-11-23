import SwiftUI
import shared

/**
 * SwiftUI settings screen with Liquid Glass components.
 * Comprehensive settings with all preference categories and glass styling.
 */
struct EnhancedSettingsView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: SettingsViewModel
    @State private var showClearDataAlert = false

    init(controller: SettingsController, appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: SettingsViewModel(controller: controller))
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Standard navigation bar for modal-style settings
                HStack {
                    Text("Settings")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)

                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
                .background(
                    Rectangle()
                        .fill(.ultraThinMaterial)
                        .overlay(
                            Rectangle()
                                .fill(Color.lightCyan.opacity(0.2))
                        )
                )

                if viewModel.isLoading {
                    GlassLoadingIndicator(variant: .pulsing, color: .coolSteel)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let settings = viewModel.settings {
                    SettingsContent(
                        settings: settings,
                        viewModel: viewModel,
                        appComponent: appComponent,
                        onClearData: { showClearDataAlert = true }
                    )
                } else {
                    EmptySettingsView()
                }
            }
            .alert("Error", isPresented: $viewModel.showError) {
                Button("OK") {
                    viewModel.clearError()
                }
            } message: {
                if let error = viewModel.error {
                    Text(error)
                }
            }
            .alert("Clear All Data", isPresented: $showClearDataAlert) {
                Button("Cancel", role: .cancel) {
                    showClearDataAlert = false
                }
                Button("Clear All Data", role: .destructive) {
                    viewModel.clearAllData()
                    showClearDataAlert = false
                }
            } message: {
                Text("""
                This will permanently delete all your data including trips, locations, photos, and \
                settings. This action cannot be undone.

                Are you sure you want to continue?
                """)
            }
        }
        .navigationBarHidden(true)
    }
}
