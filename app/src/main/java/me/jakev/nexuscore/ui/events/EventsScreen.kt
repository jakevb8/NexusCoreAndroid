package me.jakev.nexuscore.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.jakev.nexuscore.data.model.KafkaEvent
import me.jakev.nexuscore.ui.Screen
import me.jakev.nexuscore.ui.components.AppScaffold

@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reload whenever this screen comes into focus
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route == Screen.Events.route) {
                viewModel.loadEvents()
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    AppScaffold(
        title = "Events",
        navController = navController,
        showBack = true
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            uiState.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.events.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No events yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.events, key = { it.id }) { event ->
                        EventRow(event)
                        HorizontalDivider()
                    }
                }

                // Pagination
                if (uiState.total > 50) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.loadEvents(uiState.page - 1) },
                            enabled = uiState.page > 1
                        ) { Text("Prev") }
                        Text("Page ${uiState.page}")
                        TextButton(
                            onClick = { viewModel.loadEvents(uiState.page + 1) },
                            enabled = uiState.page * 50 < uiState.total
                        ) { Text("Next") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: KafkaEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                event.assetName ?: event.assetId ?: "Unknown asset",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                formatStatusChange(event.previousStatus, event.newStatus),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                formatTimestamp(event.occurredAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        event.newStatus?.let { StatusBadge(it) }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "AVAILABLE" -> Color(0xFF16A34A)
        "IN_USE" -> Color(0xFF2563EB)
        "MAINTENANCE" -> Color(0xFFD97706)
        "RETIRED" -> Color(0xFF6B7280)
        else -> Color(0xFF6B7280)
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            status.replace("_", " "),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun formatStatusChange(prev: String?, next: String?): String {
    val p = prev?.replace("_", " ") ?: "?"
    val n = next?.replace("_", " ") ?: "?"
    return "$p → $n"
}

private fun formatTimestamp(iso: String): String {
    // Show just the date and time portion (trim trailing Z/offset)
    return iso.take(19).replace("T", " ")
}
