package me.jakev.nexuscore.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NexusCore"

data class OnboardingUiState(val isLoading: Boolean = false, val error: String? = null)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val api: NexusApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Derives a valid slug from an org name: lowercase, spaces/special chars → hyphens, trim. */
    fun slugify(orgName: String): String =
        orgName.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')

    fun register(displayName: String, orgName: String, orgSlug: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.register(RegisterRequest(
                    organizationName = orgName.trim(),
                    organizationSlug = orgSlug.trim(),
                    displayName = displayName.trim().ifEmpty { null }
                ))
                if (response.isSuccessful) {
                    onDone()
                } else {
                    val msg = response.errorBody()?.string() ?: "Registration failed (${response.code()})"
                    _uiState.update { it.copy(error = msg) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "register failed", e)
                _uiState.update { it.copy(error = e.message ?: "Registration failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
