package me.jakev.nexuscore.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

data class OnboardingUiState(val isLoading: Boolean = false, val error: String? = null)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val api: NexusApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun register(name: String, orgName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
                val token = user.getIdToken(false).await().token
                    ?: throw IllegalStateException("No ID token")
                api.register(RegisterRequest(
                    firebaseToken = token,
                    orgName = orgName,
                    name = name,
                    email = user.email ?: ""
                ))
                onDone()
            } catch (e: Exception) {
                Log.e(TAG, "register failed", e)
                _uiState.update { it.copy(error = e.message ?: "Registration failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
