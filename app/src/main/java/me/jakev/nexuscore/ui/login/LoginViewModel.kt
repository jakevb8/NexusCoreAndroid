package me.jakev.nexuscore.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import me.jakev.nexuscore.data.api.BackendChoice
import me.jakev.nexuscore.data.api.BackendPreference
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "NexusCore"

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedBackend: BackendChoice = BackendChoice.JS
)

data class AuthState(val isSignedIn: Boolean = false)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val api: NexusApi,
    private val backendPreference: BackendPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val authState = MutableStateFlow(AuthState(isSignedIn = auth.currentUser != null))

    init {
        viewModelScope.launch {
            val current = backendPreference.get()
            _uiState.update { it.copy(selectedBackend = current) }
        }
    }

    fun selectBackend(choice: BackendChoice) {
        viewModelScope.launch {
            backendPreference.set(choice)
            _uiState.update { it.copy(selectedBackend = choice) }
        }
    }

    fun onFirebaseSignInSuccess(
        onAuthenticated: (orgStatus: String) -> Unit,
        onNeedsOnboarding: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = auth.currentUser ?: throw IllegalStateException("No Firebase user")
                val token = user.getIdToken(false).await().token
                    ?: throw IllegalStateException("No ID token")

                // Try fetching existing profile
                try {
                    val me = api.me()
                    authState.value = AuthState(isSignedIn = true)
                    onAuthenticated(me.organization.status.name)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 404) {
                        // New user — needs onboarding
                        onNeedsOnboarding()
                    } else {
                        throw e
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onFirebaseSignInSuccess failed", e)
                _uiState.update { it.copy(error = e.message ?: "Authentication failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSignInError(message: String) {
        Log.e(TAG, "Google sign-in error: $message")
        _uiState.update { it.copy(error = message, isLoading = false) }
    }
}
