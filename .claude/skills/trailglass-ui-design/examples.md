# UI Implementation Examples

Practical examples showing how to implement common Trailglass UI patterns on both platforms.

## Example 1: Visit Card

### Android (Material 3)

```kotlin
@Composable
fun VisitCard(
    visit: PlaceVisit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Bold title
                Text(
                    text = visit.city ?: "Unknown Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Small subtitle - contrast
                Text(
                    text = formatDateRange(visit.startTime, visit.endTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Metadata chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = visit.country ?: "",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = formatDuration(visit),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Trailing icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### iOS (Liquid Glass)

```swift
struct VisitCard: View {
    let visit: PlaceVisit
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 16) {
                // Icon with glass background
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [
                                    Color.blue.opacity(0.3),
                                    Color.purple.opacity(0.2)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 48, height: 48)

                    Image(systemName: "location.fill")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundStyle(.white)
                }

                // Text content
                VStack(alignment: .leading, spacing: 4) {
                    // Bold title
                    Text(visit.city ?? "Unknown Location")
                        .font(.system(size: 18, weight: .bold, design: .rounded))
                        .foregroundStyle(.primary)

                    // Light subtitle - contrast
                    Text(formatDateRange(visit.startTime, visit.endTime))
                        .font(.system(size: 13, weight: .regular))
                        .foregroundStyle(.secondary)

                    // Metadata
                    HStack(spacing: 12) {
                        Label {
                            Text(visit.country ?? "")
                                .font(.caption2)
                        } icon: {
                            Image(systemName: "globe")
                                .font(.caption2)
                        }
                        .foregroundStyle(.tertiary)

                        Label {
                            Text(formatDuration(visit))
                                .font(.caption2)
                        } icon: {
                            Image(systemName: "clock")
                                .font(.caption2)
                        }
                        .foregroundStyle(.tertiary)
                    }
                    .padding(.top, 2)
                }

                Spacer()

                // Chevron
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(.tertiary)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background {
                ZStack {
                    // Glass background
                    LinearGradient(
                        colors: [
                            Color.white.opacity(0.2),
                            Color.white.opacity(0.1)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )

                    Rectangle()
                        .fill(.ultraThinMaterial)
                }
                .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .overlay {
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(.white.opacity(0.2), lineWidth: 1)
            }
            .shadow(color: .black.opacity(0.1), radius: 15, y: 8)
        }
        .buttonStyle(.plain)
    }
}
```

## Example 2: Stats Overview

### Android (Material 3)

```kotlin
@Composable
fun StatsOverview(
    stats: StatsData,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        item {
            StatCard(
                title = "Countries",
                value = stats.countriesVisited.toString(),
                icon = Icons.Filled.Public,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }

        item {
            StatCard(
                title = "Cities",
                value = stats.citiesVisited.toString(),
                icon = Icons.Filled.LocationCity,
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        item {
            StatCard(
                title = "Trips",
                value = stats.totalTrips.toString(),
                icon = Icons.Filled.Flight,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }

        item {
            StatCard(
                title = "Photos",
                value = stats.totalPhotos.toString(),
                icon = Icons.Filled.PhotoLibrary,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Column {
                // Large bold number
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Small label
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### iOS (Liquid Glass)

```swift
struct StatsOverview: View {
    let stats: StatsData

    let columns = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]

    var body: some View {
        LazyVGrid(columns: columns, spacing: 16) {
            StatCard(
                title: "Countries",
                value: "\(stats.countriesVisited)",
                icon: "globe",
                gradient: [.blue, .cyan]
            )

            StatCard(
                title: "Cities",
                value: "\(stats.citiesVisited)",
                icon: "building.2",
                gradient: [.purple, .pink]
            )

            StatCard(
                title: "Trips",
                value: "\(stats.totalTrips)",
                icon: "airplane",
                gradient: [.orange, .red]
            )

            StatCard(
                title: "Photos",
                value: "\(stats.totalPhotos)",
                icon: "photo.stack",
                gradient: [.green, .mint]
            )
        }
        .padding(20)
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let gradient: [Color]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Icon with gradient
            Image(systemName: icon)
                .font(.system(size: 28, weight: .semibold))
                .foregroundStyle(
                    LinearGradient(
                        colors: gradient,
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )

            Spacer()

            VStack(alignment: .leading, spacing: 2) {
                // Large bold number
                Text(value)
                    .font(.system(size: 36, weight: .bold, design: .rounded))
                    .foregroundStyle(.primary)

                // Small label
                Text(title)
                    .font(.system(size: 13, weight: .regular))
                    .foregroundStyle(.secondary)
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .aspectRatio(1.0, contentMode: .fit)
        .background {
            ZStack {
                // Gradient background
                LinearGradient(
                    colors: gradient.map { $0.opacity(0.15) },
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )

                // Glass overlay
                Rectangle()
                    .fill(.ultraThinMaterial)
            }
            .clipShape(RoundedRectangle(cornerRadius: 24))
        }
        .overlay {
            RoundedRectangle(cornerRadius: 24)
                .strokeBorder(.white.opacity(0.2), lineWidth: 1)
        }
        .shadow(color: gradient[0].opacity(0.2), radius: 15, y: 8)
    }
}
```

## Example 3: Timeline Screen

### Android (Material 3)

```kotlin
@Composable
fun TimelineScreen(
    controller: TimelineController,
    onNavigateToVisit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Timeline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { /* filter */ }) {
                        Icon(Icons.Filled.FilterList, "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* add */ },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add visit")
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                ErrorState(
                    message = state.error!!,
                    onRetry = { controller.reload() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 88.dp  // Space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(paddingValues)
                ) {
                    itemsIndexed(
                        items = state.items,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        VisitCard(
                            visit = item,
                            onClick = { onNavigateToVisit(item.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
```

### iOS (Liquid Glass)

```swift
struct TimelineScreen: View {
    @StateObject var viewModel: TimelineViewModel

    var body: some View {
        NavigationStack {
            ZStack {
                // Atmospheric background
                AtmosphericBackground()

                // Content
                if viewModel.isLoading {
                    ProgressView()
                        .tint(.white)
                } else if let error = viewModel.error {
                    ErrorView(message: error) {
                        viewModel.reload()
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(viewModel.items) { item in
                                VisitCard(visit: item) {
                                    viewModel.selectVisit(item.id)
                                }
                                .transition(.liquidSlide)
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 16)
                    }
                }

                // Floating button
                VStack {
                    Spacer()

                    HStack {
                        Spacer()

                        FloatingButton {
                            viewModel.addVisit()
                        }
                        .padding(.trailing, 20)
                        .padding(.bottom, 20)
                    }
                }
            }
            .navigationTitle("Timeline")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        viewModel.showFilter()
                    } label: {
                        Image(systemName: "line.3.horizontal.decrease.circle")
                            .font(.system(size: 18, weight: .semibold))
                    }
                }
            }
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        }
        .onAppear {
            viewModel.loadData()
        }
    }
}
```

## Example 4: Loading States

### Android (Material 3)

```kotlin
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Loading your travels...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Explore,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAction) {
            Text(actionText)
        }
    }
}
```

### iOS (Liquid Glass)

```swift
struct LoadingView: View {
    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .controlSize(.large)
                .tint(.white)

            Text("Loading your travels...")
                .font(.body)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background {
            AtmosphericBackground()
        }
    }
}

struct EmptyStateView: View {
    let title: String
    let message: String
    let actionText: String
    let action: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "globe.americas")
                .font(.system(size: 80, weight: .light))
                .foregroundStyle(
                    LinearGradient(
                        colors: [.blue, .purple],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )

            VStack(spacing: 8) {
                Text(title)
                    .font(.system(size: 24, weight: .bold, design: .rounded))
                    .foregroundStyle(.primary)

                Text(message)
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            }

            Button(action: action) {
                Text(actionText)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 16)
                    .background {
                        Capsule()
                            .fill(
                                LinearGradient(
                                    colors: [.blue, .purple],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .shadow(color: .blue.opacity(0.4), radius: 15, y: 8)
                    }
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
```

---

These examples demonstrate how to implement the same features with platform-appropriate design languages while maintaining functional parity.
