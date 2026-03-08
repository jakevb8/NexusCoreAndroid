package me.jakev.nexuscore.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.jakev.nexuscore.data.api.BackendChoice
import me.jakev.nexuscore.data.api.BackendPreference
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.AuthUser
import com.firebase.ui.auth.AuthUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
     * Sign out using AuthUI so that Google's credential cache is cleared.
     * This forces the account chooser on the next sign-in attempt instead of
     * silently re-authenticating with the same account.
     */
    fun signOut(context: Context, onSignedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                AuthUI.getInstance().signOut(context).await()
            } catch (e: Exception) {
                Log.e(TAG, "signOut failed", e)
            } finally {
                onSignedOut()
            }
        }
    }

    /**
     * Delete the account via DELETE /auth/me, then sign out via AuthUI so the
     * Google credential cache is cleared and the account chooser appears next login.
     */
    fun deleteAccount(context: Context, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            try {
                api.deleteAccount()
                AuthUI.getInstance().signOut(context).await()
                onDeleted()
            } catch (e: Exception) {
                Log.e(TAG, "deleteAccount failed", e)
                _uiState.update { it.copy(isDeletingAccount = false, error = e.message) }
            }
        }
    }
}
