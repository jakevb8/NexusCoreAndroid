package me.jakev.nexuscore.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.jakev.nexuscore.ui.Screen
import me.jakev.nexuscore.ui.components.AppScaffold

@Composable
fun DashboardScreen(navController: NavController) {
    AppScaffold(title = "Dashboard", navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardCard("Assets", Icons.Default.Inventory, "Manage your organization's assets") {
                navController.navigate(Screen.Assets.route)
            }
            DashboardCard("Team", Icons.Default.Group, "Manage members and invitations") {
                navController.navigate(Screen.Team.route)
            }
            DashboardCard("Reports", Icons.Default.BarChart, "View utilization analytics") {
                navController.navigate(Screen.Reports.route)
            }
            DashboardCard("Events", Icons.Default.History, "Browse Kafka asset status change history") {
                navController.navigate(Screen.Events.route)
            }
            DashboardCard("Settings", Icons.Default.Settings, "Backend and account settings") {
                navController.navigate(Screen.Settings.route)
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
