package me.jakev.nexuscore.ui.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.jakev.nexuscore.data.model.Role
import me.jakev.nexuscore.data.model.TeamMember
import me.jakev.nexuscore.ui.components.AppScaffold

@Composable
fun TeamScreen(
    navController: NavController,
    viewModel: TeamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf<TeamMember?>(null) }
    var showRoleDialog by remember { mutableStateOf<TeamMember?>(null) }

    AppScaffold(
        title = "Team",
        navController = navController,
        showBack = true,
        actions = {
            if (uiState.isManager) {
                IconButton(onClick = { showInviteDialog = true }) {
                    Icon(Icons.Default.PersonAdd, "Invite")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            uiState.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(it, modifier = Modifier.padding(12.dp))
                }
            }
            uiState.successMessage?.let {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(it, modifier = Modifier.padding(12.dp))
                }
            }
            uiState.inviteLink?.let { link ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Email not available — share this link manually:",
                            style = MaterialTheme.typography.bodySmall)
                        Text(link, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        TextButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(link))
                                viewModel.clearMessages()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy link")
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(uiState.members, key = { it.id }) { member ->
                        MemberRow(
                            member = member,
                            isCurrentUser = member.id == uiState.currentUserId,
                            isManager = uiState.isManager,
                            onRemove = { showRemoveConfirm = member },
                            onChangeRole = { showRoleDialog = member }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteDialog(
            onDismiss = { showInviteDialog = false },
            onInvite = { email ->
                viewModel.invite(email)
                showInviteDialog = false
            }
        )
    }

    showRemoveConfirm?.let { member ->
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = null },
            title = { Text("Remove ${member.name ?: member.email}?") },
            text = { Text("They will lose access immediately.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeMember(member.id)
                    showRemoveConfirm = null
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showRemoveConfirm = null }) { Text("Cancel") } }
        )
    }

    showRoleDialog?.let { member ->
        RoleDialog(
            current = member.role,
            onDismiss = { showRoleDialog = null },
            onSelect = { role ->
                viewModel.updateRole(member.id, role)
                showRoleDialog = null
            }
        )
    }
}

@Composable
private fun MemberRow(
    member: TeamMember,
    isCurrentUser: Boolean,
    isManager: Boolean,
    onRemove: () -> Unit,
    onChangeRole: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(member.name ?: member.email, style = MaterialTheme.typography.bodyLarge)
            if (member.name != null) Text(member.email, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(member.role.name, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
        if (isManager && !isCurrentUser && member.role != Role.SUPERADMIN) {
            IconButton(onClick = onChangeRole) { Icon(Icons.Default.Edit, "Change role", modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.PersonRemove, "Remove", tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun InviteDialog(onDismiss: () -> Unit, onInvite: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite member") },
        text = {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email address") }, singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onInvite(email) }, enabled = email.contains("@")) {
                Text("Send Invite")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RoleDialog(current: Role, onDismiss: () -> Unit, onSelect: (Role) -> Unit) {
    val assignableRoles = listOf(Role.VIEWER, Role.ASSET_MANAGER, Role.ORG_MANAGER)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change role") },
        text = {
            Column {
                assignableRoles.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = current == role, onClick = { onSelect(role) })
                        Text(role.name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
