import SwiftUI
import Shared
import MapKit

struct RegionsListView: View {
    @StateObject private var viewModel: RegionViewModel
    @State private var showingCreateSheet = false
    @State private var selectedRegion: Region?

    init(viewModel: RegionViewModel) {
        _viewModel = StateObject(wrappedValue: viewModel)
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            if viewModel.isLoading {
                GlassLoadingIndicator(variant: .pulsing, color: .coolSteel)
            } else if viewModel.regions.isEmpty {
                emptyStateView
            } else {
                regionsList
            }
        }
        .navigationTitle("Places")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showingCreateSheet = true }) {
                    Image(systemName: "plus")
                        .foregroundColor(.coastalPath)
                }
            }
        }
        .sheet(isPresented: $showingCreateSheet) {
            NavigationView {
                RegionDetailView(
                    regionViewModel: viewModel,
                    region: nil,
                    onSave: { showingCreateSheet = false }
                )
            }
        }
        .sheet(item: $selectedRegion) { region in
            NavigationView {
                RegionDetailView(
                    regionViewModel: viewModel,
                    region: region,
                    onSave: { selectedRegion = nil }
                )
            }
        }
        .onAppear {
            viewModel.loadRegions()
        }
    }

    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Image(systemName: "mappin.circle")
                .font(.system(size: 64))
                .foregroundColor(.coolSteel)

            Text("No Places Yet")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.primary)

            Text("Create geofenced regions to track when you enter or leave specific locations")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)

            GlassButton(
                title: "Create Your First Place",
                icon: "plus.circle.fill",
                variant: .filled,
                tint: .coastalPath
            ) {
                showingCreateSheet = true
            }
            .padding(.horizontal, 40)
            .padding(.top, 16)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var regionsList: some View {
        ScrollView {
            VStack(spacing: 12) {
                ForEach(viewModel.regions, id: \.id) { region in
                    RegionCard(
                        region: region,
                        onTap: { selectedRegion = region },
                        onToggleNotifications: {
                            viewModel.toggleNotifications(forRegionId: region.id)
                        },
                        onDelete: {
                            viewModel.deleteRegion(id: region.id)
                        }
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
            .padding(.bottom, 96)
        }
    }
}

struct RegionCard: View {
    let region: Region
    let onTap: () -> Void
    let onToggleNotifications: () -> Void
    let onDelete: () -> Void

    var body: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            Button(action: onTap) {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Image(systemName: region.iconName)
                            .foregroundColor(region.displayColor)
                            .font(.title2)

                        VStack(alignment: .leading, spacing: 4) {
                            Text(region.name)
                                .font(.headline)
                                .foregroundColor(.primary)

                            if let description = region.description_ {
                                Text(description)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                    .lineLimit(2)
                            }
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .foregroundColor(.secondary)
                            .font(.caption)
                    }

                    Divider()

                    HStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Radius")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(region.radiusDisplayText)
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.blueSlate)
                        }

                        Spacer()

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Visits")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(region.enterCount)")
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.blueSlate)
                        }

                        Spacer()

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Last Visit")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            if let lastEnteredDate = region.lastEnteredDate {
                                Text(formatDate(lastEnteredDate))
                            } else {
                                Text("Never")
                            }
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.blueSlate)
                        }
                    }

                    HStack(spacing: 12) {
                        Button(action: onToggleNotifications) {
                            HStack(spacing: 6) {
                                Image(systemName: region.notificationsEnabled ? "bell.fill" : "bell.slash.fill")
                                    .foregroundColor(region.notificationsEnabled ? .coastalPath : .secondary)
                                Text(region.notificationsEnabled ? "Notifications On" : "Notifications Off")
                                    .font(.caption)
                                    .foregroundColor(region.notificationsEnabled ? .coastalPath : .secondary)
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(region.notificationsEnabled ? Color.coastalPath.opacity(0.1) : Color.gray.opacity(0.1))
                            )
                        }
                        .buttonStyle(PlainButtonStyle())

                        Spacer()

                        Button(action: onDelete) {
                            HStack(spacing: 6) {
                                Image(systemName: "trash")
                                    .foregroundColor(.red)
                                Text("Delete")
                                    .font(.caption)
                                    .foregroundColor(.red)
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(Color.red.opacity(0.1))
                            )
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
            }
            .buttonStyle(PlainButtonStyle())
        }
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .short
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}

extension Region: @retroactive Identifiable {}

#Preview {
    NavigationView {
        RegionsListView(
            viewModel: RegionViewModel(
                appComponent: InjectIOSAppComponent(platformModule: IOSPlatformModule())
            )
        )
    }
}
