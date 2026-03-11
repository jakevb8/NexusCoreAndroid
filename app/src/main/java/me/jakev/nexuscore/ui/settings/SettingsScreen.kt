package me.jakev.nexuscore.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.jakev.nexuscore.data.api.BackendChoice
import me.jakev.nexuscore.ui.components.AppScaffold

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AppScaffold(title = "Settings", navController = navController, showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile info
            uiState.me?.let { me ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Account", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary)
                        Text(me.displayName ?: me.email, style = MaterialTheme.typography.bodyLarge)
                        Text(me.email, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("Role: ${me.role.name}", style = MaterialTheme.typography.labelMedium)
                        Text("Organization: ${me.organization?.name ?: "—"}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Backend selector
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Backend", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text("Select which API backend to connect to.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    BackendChoice.entries.forEach { choice ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.selectedBackend == choice,
                                onClick = { viewModel.selectBackend(choice) }
                            )
                            Column {
                                Text(choice.label, style = MaterialTheme.typography.bodyMedium)
                                Text(choice.baseUrl, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Sign out
            OutlinedButton(
                onClick = { viewModel.signOut(context, onSignOut) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }

            // Delete account (danger zone)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Danger Zone", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error)
                    Text(
                        "Permanently delete your account and all associated data. This cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(
                        onClick = { showDeleteConfirm = true },
                        enabled = !uiState.isDeletingAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isDeletingAccount) "Deleting..." else "Delete Account")
                    }
                }
            }

            uiState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // Confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete your account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("This will permanently delete:")
                    Text("• Your user profile")
                    uiState.me?.let {
                        Text("• Organization \"${it.organization?.name ?: "your organization"}\" and all its data, if you are the last member")
                    }
                    Text("• All assets, audit logs, and invites")
                    Spacer(Modifier.height(8.dp))
                    Text("This cannot be undone.", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccount(context = context, onDeleted = onSignOut)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, delete my account")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
