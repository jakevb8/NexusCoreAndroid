package com.nexuscore.android.ui.login

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.nexuscore.android.data.api.BackendChoice

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onAuthenticated: (orgStatus: String) -> Unit,
    onNeedsOnboarding: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val signInLauncher = rememberLauncherForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onFirebaseSignInSuccess(onAuthenticated, onNeedsOnboarding)
        } else {
            viewModel.onSignInError(result.idpResponse?.error?.message ?: "Sign-in cancelled")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("NexusCore", fontSize = 32.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Resource Management", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

        Spacer(Modifier.height(48.dp))

        // Backend selector
        Text("Backend", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BackendChoice.entries.forEach { choice ->
                FilterChip(
                    selected = uiState.selectedBackend == choice,
                    onClick = { viewModel.selectBackend(choice) },
                    label = { Text(choice.label, fontSize = 12.sp) }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())
                    val intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()
                    signInLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in with Google")
            }
        }

        uiState.error?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
    }
}
