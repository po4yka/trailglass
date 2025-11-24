import SwiftUI
import shared

struct LogViewerView: View {
    @StateObject private var viewModel = LogViewerViewModel()
    @State private var selectedLevel: LogLevel? = nil
    @State private var showingShareSheet = false
    @State private var showingClearAlert = false

    var body: some View {
        VStack(spacing: 0) {
            filterChipsView

            Divider()

            if filteredEntries.isEmpty {
                emptyStateView
            } else {
                logListView
            }
        }
        .navigationTitle("Logs")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack {
                    Button(action: { showingShareSheet = true }) {
                        Image(systemName: "square.and.arrow.up")
                    }

                    Button(action: { showingClearAlert = true }) {
                        Image(systemName: "trash")
                    }
                }
            }
        }
        .sheet(isPresented: $showingShareSheet) {
            ShareSheet(activityItems: [viewModel.exportLogs()])
        }
        .alert("Clear Logs", isPresented: $showingClearAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Clear", role: .destructive) {
                viewModel.clearLogs()
            }
        } message: {
            Text("Are you sure you want to clear all log entries?")
        }
    }

    private var filterChipsView: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                FilterChip(
                    title: "All",
                    isSelected: selectedLevel == nil,
                    action: { selectedLevel = nil }
                )

                FilterChip(
                    title: "DEBUG",
                    isSelected: selectedLevel == .debug,
                    action: { selectedLevel = .debug }
                )

                FilterChip(
                    title: "INFO",
                    isSelected: selectedLevel == .info,
                    action: { selectedLevel = .info }
                )

                FilterChip(
                    title: "WARNING",
                    isSelected: selectedLevel == .warning,
                    action: { selectedLevel = .warning }
                )

                FilterChip(
                    title: "ERROR",
                    isSelected: selectedLevel == .error,
                    action: { selectedLevel = .error }
                )
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
    }

    private var filteredEntries: [LogEntry] {
        if let selectedLevel = selectedLevel {
            return viewModel.logEntries.filter { $0.level == selectedLevel }
        }
        return viewModel.logEntries
    }

    private var emptyStateView: some View {
        VStack {
            Spacer()
            Text("No log entries")
                .foregroundColor(.secondary)
                .font(.body)
            Spacer()
        }
    }

    private var logListView: some View {
        ScrollView {
            ScrollViewReader { proxy in
                LazyVStack(spacing: 0) {
                    ForEach(Array(filteredEntries.enumerated()), id: \.offset) { index, entry in
                        LogEntryRow(entry: entry)
                            .id(index)
                    }
                }
                .onChange(of: filteredEntries.count) { _ in
                    if !filteredEntries.isEmpty {
                        withAnimation {
                            proxy.scrollTo(filteredEntries.count - 1, anchor: .bottom)
                        }
                    }
                }
            }
        }
    }
}

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.blue : Color.gray.opacity(0.2))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(16)
        }
    }
}

struct LogEntryRow: View {
    let entry: LogEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(formatTimestamp(entry.timestamp))
                    .font(.system(.caption, design: .monospaced))
                    .foregroundColor(.secondary)

                Spacer()

                Text(entry.level.name)
                    .font(.system(.caption, design: .monospaced))
                    .foregroundColor(levelColor(entry.level))
            }

            Text("[\(entry.tag)]")
                .font(.system(.caption, design: .monospaced))
                .foregroundColor(.secondary)

            Text(entry.message)
                .font(.system(.caption, design: .monospaced))
                .foregroundColor(.primary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(levelBackgroundColor(entry.level))
    }

    private func levelColor(_ level: LogLevel) -> Color {
        switch level {
        case .debug: return .gray
        case .info: return .blue
        case .warning: return .orange
        case .error: return .red
        default: return .gray
        }
    }

    private func levelBackgroundColor(_ level: LogLevel) -> Color {
        switch level {
        case .debug: return Color.clear
        case .info: return Color.blue.opacity(0.1)
        case .warning: return Color.orange.opacity(0.1)
        case .error: return Color.red.opacity(0.1)
        default: return Color.clear
        }
    }

    private func formatTimestamp(_ timestamp: Instant) -> String {
        let now = Instant.Companion.shared.now()
        let diff = now.minus(other: timestamp)

        let seconds = diff.inWholeSeconds

        if seconds < 60 {
            return "\(seconds)s ago"
        } else if seconds < 3600 {
            return "\(seconds / 60)m ago"
        } else if seconds < 86400 {
            return "\(seconds / 3600)h ago"
        } else {
            return "\(seconds / 86400)d ago"
        }
    }
}

class LogViewerViewModel: ObservableObject {
    @Published var logEntries: [LogEntry] = []

    init() {
        observeLogEntries()
    }

    private func observeLogEntries() {
        LogBuffer.shared.entries.collect { entries in
            DispatchQueue.main.async {
                self.logEntries = entries as! [LogEntry]
            }
        } onCompletion: { _ in }
    }

    func clearLogs() {
        Task {
            await LogBuffer.shared.clear()
        }
    }

    func exportLogs() -> String {
        var result = ""
        Task {
            result = await LogBuffer.shared.export()
        }
        return result
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(
            activityItems: activityItems,
            applicationActivities: nil
        )
        return controller
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

#Preview {
    NavigationView {
        LogViewerView()
    }
}
