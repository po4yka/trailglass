import SwiftUI
import Shared

struct ManualCheckInSheet: View {
    let onCheckInSaved: (String, PlaceCategory, String, Double?, Double?) -> Void
    let onDismiss: () -> Void

    @State private var placeName = ""
    @State private var category = PlaceCategory.other
    @State private var notes = ""
    @State private var useCurrentLocation = true
    @State private var latitude = ""
    @State private var longitude = ""

    var isFormValid: Bool {
        !placeName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        (useCurrentLocation || (!latitude.isEmpty && !longitude.isEmpty))
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Place Details")) {
                    TextField("Place Name", text: $placeName)
                        .autocapitalization(.words)

                    Picker("Category", selection: $category) {
                        ForEach(PlaceCategory.allCases, id: \.self) { cat in
                            Text(cat.displayName).tag(cat)
                        }
                    }

                    ZStack(alignment: .topLeading) {
                        TextEditor(text: $notes)
                            .frame(minHeight: 60)

                        if notes.isEmpty {
                            Text("Optional notes...")
                                .foregroundColor(.secondary)
                                .padding(.top, 8)
                                .padding(.leading, 4)
                                .allowsHitTesting(false)
                        }
                    }
                }

                Section(header: Text("Location")) {
                    Toggle("Use current location", isOn: $useCurrentLocation)

                    if !useCurrentLocation {
                        TextField("Latitude", text: $latitude)
                            .keyboardType(.decimalPad)

                        TextField("Longitude", text: $longitude)
                            .keyboardType(.decimalPad)
                    }

                    Text("Check-in will be recorded at the current time")
                        .font(.footnote)
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("Manual Check-In")
            .navigationBarItems(
                leading: Button("Cancel") {
                    onDismiss()
                },
                trailing: Button("Check In") {
                    let lat = useCurrentLocation ? nil : Double(latitude)
                    let lon = useCurrentLocation ? nil : Double(longitude)
                    onCheckInSaved(placeName, category, notes, lat, lon)
                }
                .disabled(!isFormValid)
            )
        }
    }
}
