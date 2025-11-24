import SwiftUI
import PhotosUI

struct PhotoPickerSheet: View {
    let onPhotoSelected: ([PhotosPickerItem]) -> Void
    let onDismiss: () -> Void

    @State private var selectedPhotos: [PhotosPickerItem] = []

    var body: some View {
        NavigationView {
            VStack {
                Text("Select Photos")
                    .font(.title2)
                    .padding()

                PhotosPicker(
                    selection: $selectedPhotos,
                    maxSelectionCount: 10,
                    matching: .images,
                    photoLibrary: .shared()
                ) {
                    VStack {
                        Image(systemName: "photo.on.rectangle.angled")
                            .font(.largeTitle)
                            .foregroundColor(.blue)
                        Text("Choose from Library")
                            .foregroundColor(.blue)
                    }
                    .frame(maxWidth: .infinity, minHeight: 100)
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(8)
                    .padding(.horizontal)
                }

                if !selectedPhotos.isEmpty {
                    Text("\(selectedPhotos.count) photo(s) selected")
                        .foregroundColor(.secondary)
                        .padding(.top, 8)
                }

                Spacer()
            }
            .navigationBarItems(
                leading: Button("Cancel") {
                    onDismiss()
                },
                trailing: Button("Import") {
                    onPhotoSelected(selectedPhotos)
                    onDismiss()
                }
                .disabled(selectedPhotos.isEmpty)
            )
        }
    }
}
