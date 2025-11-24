package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.logging.LogBuffer
import com.po4yka.trailglass.logging.LogEntry
import com.po4yka.trailglass.logging.LogLevel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logEntries by LogBuffer.entries.collectAsState()
    val listState = rememberLazyListState()

    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }

    val filteredEntries =
        remember(logEntries, selectedLevel) {
            if (selectedLevel == null) {
                logEntries
            } else {
                logEntries.filter { it.level == selectedLevel }
            }
        }

    LaunchedEffect(filteredEntries.size) {
        if (filteredEntries.isNotEmpty()) {
            listState.animateScrollToItem(filteredEntries.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val exported = LogBuffer.export()
                                shareText(context, exported, "TrailGlass Logs")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                LogBuffer.clear()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedLevel == null,
                    onClick = { selectedLevel = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.DEBUG,
                    onClick = { selectedLevel = LogLevel.DEBUG },
                    label = { Text("DEBUG") }
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.INFO,
                    onClick = { selectedLevel = LogLevel.INFO },
                    label = { Text("INFO") }
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.WARNING,
                    onClick = { selectedLevel = LogLevel.WARNING },
                    label = { Text("WARNING") }
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.ERROR,
                    onClick = { selectedLevel = LogLevel.ERROR },
                    label = { Text("ERROR") }
                )
            }

            HorizontalDivider()

            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No log entries",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredEntries,
                        key = { "${it.timestamp}-${it.tag}-${it.message.hashCode()}" }
                    ) { entry ->
                        LogEntryItem(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: LogEntry) {
    val backgroundColor =
        when (entry.level) {
            LogLevel.DEBUG -> Color.Transparent
            LogLevel.INFO -> Color(0xFF1976D2).copy(alpha = 0.1f)
            LogLevel.WARNING -> Color(0xFFF57C00).copy(alpha = 0.1f)
            LogLevel.ERROR -> Color(0xFFD32F2F).copy(alpha = 0.1f)
        }

    val textColor =
        when (entry.level) {
            LogLevel.DEBUG -> Color.Gray
            LogLevel.INFO -> Color(0xFF1976D2)
            LogLevel.WARNING -> Color(0xFFF57C00)
            LogLevel.ERROR -> Color(0xFFD32F2F)
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTimestamp(entry.timestamp),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.level.name,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
        }
        Text(
            text = "[${entry.tag}]",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Instant): String {
    val now = Clock.System.now()
    val diff = now - timestamp

    return when {
        diff < 60.seconds -> "${diff.inWholeSeconds}s ago"
        diff < 3600.seconds -> "${diff.inWholeMinutes}m ago"
        diff < 86400.seconds -> "${diff.inWholeHours}h ago"
        else -> "${diff.inWholeDays}d ago"
    }
}

private fun shareText(
    context: android.content.Context,
    text: String,
    title: String
) {
    val sendIntent =
        android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
    val shareIntent = android.content.Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
