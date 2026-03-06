package com.nexuscore.android.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nexuscore.android.data.model.AssetStatus
import com.nexuscore.android.ui.assets.StatusChip
import com.nexuscore.android.ui.components.AppScaffold
import kotlin.math.roundToInt

@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AppScaffold(title = "Reports", navController = navController, showBack = true) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val data = uiState.data
            if (data != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Total Assets", data.totalAssets.toString(), Modifier.weight(1f))
                            StatCard("Utilization", "${(data.utilizationRate * 100).roundToInt()}%", Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Assets by Status", style = MaterialTheme.typography.titleMedium)
                    }
                    items(data.byStatus) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusChip(item.status)
                            Text("${item.count}", style = MaterialTheme.typography.bodyLarge)
                        }
                        // Simple bar
                        val maxCount = data.byStatus.maxOfOrNull { it.count } ?: 1
                        LinearProgressIndicator(
                            progress = { item.count.toFloat() / maxCount },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            } else {
                uiState.error?.let {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
