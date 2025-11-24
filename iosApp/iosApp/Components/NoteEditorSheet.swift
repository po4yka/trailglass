import SwiftUI

struct NoteEditorSheet: View {
    let onNoteSaved: (String, String, Bool) -> Void
    let onDismiss: () -> Void

    @State private var title = ""
    @State private var content = ""
    @State private var attachLocation = true

    var isFormValid: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Note Details")) {
                    TextField("Title", text: $title)
                        .autocapitalization(.words)

                    ZStack(alignment: .topLeading) {
                        TextEditor(text: $content)
                            .frame(minHeight: 100)

                        if content.isEmpty {
                            Text("Write your note here...")
                                .foregroundColor(.secondary)
                                .padding(.top, 8)
                                .padding(.leading, 4)
                                .allowsHitTesting(false)
                        }
                    }
                }

                Section {
                    Toggle("Attach current location", isOn: $attachLocation)
                    if attachLocation {
                        Text("Your current location will be saved with this note")
                            .font(.footnote)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Add Note")
            .navigationBarItems(
                leading: Button("Cancel") {
                    onDismiss()
                },
                trailing: Button("Save") {
                    onNoteSaved(title, content, attachLocation)
                }
                .disabled(!isFormValid)
            )
        }
    }
}
