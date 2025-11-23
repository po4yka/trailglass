import SwiftUI
import shared

/// Photo detail screen showing comprehensive photo information with metadata
struct PhotoDetailView: View {
    let photoId: String
    @StateObject private var viewModel: PhotoDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(photoId: String, appComponent: AppComponent) {
        self.photoId = photoId
        _viewModel = StateObject(wrappedValue: PhotoDetailViewModel(
            photoId: photoId,
            controller: appComponent.photoDetailController
        ))
    }

    var body: some View {
        NavigationView {
            Group {
                if viewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 200)
                } else if let error = viewModel.errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 48))
                            .foregroundColor(.red)
                        Text(error)
                            .foregroundColor(.secondary)
                        Button("Retry", action: { viewModel.loadPhoto() })
                            .buttonStyle(.borderedProminent)
                    }
                    .padding()
                } else if let photo = viewModel.photoWithMetadata {
                    PhotoDetailContent(
                        photo: photo,
                        onAttachToVisit: { viewModel.showAttachmentDialog() }
                    )
                }
            }
            .navigationTitle("Photo Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: { viewModel.sharePhoto() }) {
                            Label("Share", systemImage: "square.and.arrow.up")
                        }

                        Divider()

                        Button(role: .destructive, action: { viewModel.showDeleteAlert = true }) {
                            Label("Delete Photo", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            .alert("Delete Photo?", isPresented: $viewModel.showDeleteAlert) {
                Button("Cancel", role: .cancel) { }
                Button("Delete", role: .destructive, action: { viewModel.deletePhoto() })
            } message: {
                Text("This action cannot be undone. The photo will be permanently deleted.")
            }
        }
        .onAppear {
            viewModel.loadPhoto()
        }
    }
}

#Preview {
    Text("PhotoDetailView Preview - Requires DI setup")
}
