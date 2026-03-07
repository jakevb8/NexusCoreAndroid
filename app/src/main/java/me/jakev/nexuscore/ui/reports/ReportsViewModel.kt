package me.jakev.nexuscore.ui.reports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.jakev.nexuscore.data.api.BackendChoice
import me.jakev.nexuscore.data.api.BackendPreference
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.ReportsData
import me.jakev.nexuscore.data.model.toReportsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NexusCore"

data class ReportsUiState(
    val data: ReportsData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val api: NexusApi,
    private val backendPreference: BackendPreference
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val backend = backendPreference.get()
                val data = if (backend == BackendChoice.JS) {
                    api.getJsReports().toReportsData()
                } else {
                    api.getDotNetReports().toReportsData()
                }
                _uiState.update { it.copy(data = data, isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "loadReports failed", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
