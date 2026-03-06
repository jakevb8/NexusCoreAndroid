package me.jakev.nexuscore.ui.assets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.jakev.nexuscore.data.model.Asset
import me.jakev.nexuscore.data.model.AssetStatus
import me.jakev.nexuscore.ui.Screen
import me.jakev.nexuscore.ui.components.AppScaffold

@Composable
fun AssetsScreen(
    navController: NavController,
    viewModel: AssetsViewModel = hiltViewModel(),
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    AppScaffold(
        title = "Assets",
        navController = navController,
        showBack = true,
        actions = {
            if (uiState.isManager) {
                IconButton(onClick = { navController.navigate(Screen.AssetDetail.route("new")) }) {
                    Icon(Icons.Default.Add, "New Asset")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = search,
                onValueChange = { search = it; viewModel.loadAssets(search = it.ifBlank { null }) },
                placeholder = { Text("Search assets…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                singleLine = true
            )

            // Messages
            uiState.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    Text(it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            uiState.importResult?.let { result ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    Text("Import: ${result.created} created, ${result.skipped} skipped${if (result.limitReached) " (limit reached)" else ""}",
                        modifier = Modifier.padding(12.dp))
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.assets, key = { it.id }) { asset ->
                        AssetRow(
                            asset = asset,
                            isManager = uiState.isManager,
                            onEdit = { navController.navigate(Screen.AssetDetail.route(asset.id)) },
                            onDelete = { showDeleteConfirm = asset.id }
                        )
                        HorizontalDivider()
                    }
                }

                // Pagination
                if (uiState.total > 20) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.loadAssets(uiState.page - 1) },
                            enabled = uiState.page > 1
                        ) { Text("Prev") }
                        Text("Page ${uiState.page}")
                        TextButton(
                            onClick = { viewModel.loadAssets(uiState.page + 1) },
                            enabled = uiState.page * 20 < uiState.total
                        ) { Text("Next") }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteConfirm?.let { assetId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete asset?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAsset(assetId)
                    showDeleteConfirm = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AssetRow(
    asset: Asset,
    isManager: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.name, style = MaterialTheme.typography.bodyLarge)
            Text(asset.sku, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        StatusChip(asset.status)
        if (isManager) {
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete",
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun StatusChip(status: AssetStatus) {
    val (label, color) = when (status) {
        AssetStatus.AVAILABLE -> "Available" to Color(0xFF16A34A)
        AssetStatus.IN_USE -> "In Use" to Color(0xFF2563EB)
        AssetStatus.MAINTENANCE -> "Maintenance" to Color(0xFFD97706)
        AssetStatus.RETIRED -> "Retired" to Color(0xFF6B7280)
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(label, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall)
    }
}
