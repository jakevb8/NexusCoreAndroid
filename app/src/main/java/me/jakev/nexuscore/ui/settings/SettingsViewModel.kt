package me.jakev.nexuscore.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.jakev.nexuscore.data.api.BackendChoice
import me.jakev.nexuscore.data.api.BackendPreference
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NexusCore"

data class SettingsUiState(
    val me: AuthUser? = null,
    val selectedBackend: BackendChoice = BackendChoice.JS,
    val isLoading: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: NexusApi,
    private val backendPreference: BackendPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val me = api.me()
                val backend = backendPreference.get()
                _uiState.update { it.copy(me = me, selectedBackend = backend, isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "settings load failed", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectBackend(choice: BackendChoice) {
        viewModelScope.launch {
            backendPreference.set(choice)
            _uiState.update { it.copy(selectedBackend = choice) }
        }
    }

    /**
     * Delete the account via DELETE /auth/me, then sign out of Firebase.
     * [onDeleted] is called on success so the caller can navigate to the login screen.
     */
    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            try {
                api.deleteAccount()
                FirebaseAuth.getInstance().signOut()
                onDeleted()
            } catch (e: Exception) {
                Log.e(TAG, "deleteAccount failed", e)
                _uiState.update { it.copy(isDeletingAccount = false, error = e.message) }
            }
        }
    }
}
