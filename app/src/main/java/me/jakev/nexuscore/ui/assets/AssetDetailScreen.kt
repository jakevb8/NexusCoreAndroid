package me.jakev.nexuscore.ui.assets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.jakev.nexuscore.data.model.AssetStatus
import me.jakev.nexuscore.ui.components.AppScaffold

@Composable
fun AssetDetailScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: AssetDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.done) {
        if (uiState.done) onBack()
    }

    AppScaffold(
        title = if (viewModel.isNew) "New Asset" else "Edit Asset",
        showBack = true,
        actions = {
            TextButton(
                onClick = { viewModel.save() },
                enabled = !uiState.isSaving && uiState.name.isNotBlank() && uiState.sku.isNotBlank()
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = uiState.name, onValueChange = viewModel::onNameChange,
                label = { Text("Name *") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.sku, onValueChange = viewModel::onSkuChange,
                label = { Text("SKU *") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.description, onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            OutlinedTextField(
                value = uiState.assignedTo, onValueChange = viewModel::onAssignedToChange,
                label = { Text("Assigned To") }, modifier = Modifier.fillMaxWidth()
            )

            Text("Status", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssetStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.status == status,
                        onClick = { viewModel.onStatusChange(status) },
                        label = { Text(status.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (uiState.isSaving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
