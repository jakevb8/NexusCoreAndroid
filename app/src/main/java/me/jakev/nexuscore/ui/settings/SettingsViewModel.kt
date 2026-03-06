package me.jakev.nexuscore.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.jakev.nexuscore.data.api.BackendChoice
import me.jakev.nexuscore.data.api.BackendPreference
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val me: AuthUser? = null,
    val selectedBackend: BackendChoice = BackendChoice.JS,
    val isLoading: Boolean = false,
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
}
