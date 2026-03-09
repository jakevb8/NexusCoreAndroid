package me.jakev.nexuscore.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var displayName by remember { mutableStateOf("") }
    var orgName by remember { mutableStateOf("") }
    // Auto-derived slug, user can override
    var orgSlug by remember { mutableStateOf("") }
    var slugEdited by remember { mutableStateOf(false) }

    // Auto-update slug when orgName changes (unless user has manually edited slug)
    LaunchedEffect(orgName) {
        if (!slugEdited) {
            orgSlug = viewModel.slugify(orgName)
        }
    }

    val slugValid = orgSlug.length >= 3 && orgSlug.matches(Regex("^[a-z0-9-]+$"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create your organization", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = orgName,
            onValueChange = { orgName = it },
            label = { Text("Organization name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = orgSlug,
            onValueChange = {
                orgSlug = it.lowercase().replace(Regex("[^a-z0-9-]"), "")
                slugEdited = true
            },
            label = { Text("Organization slug") },
            supportingText = {
                Text(
                    "Lowercase letters, numbers, and hyphens only (min 3 chars)",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            isError = orgSlug.isNotEmpty() && !slugValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.register(displayName, orgName, orgSlug, onDone) },
                enabled = displayName.isNotBlank() && orgName.isNotBlank() && slugValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Organization")
            }
        }

        uiState.error?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
