package me.jakev.nexuscore.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.KafkaEvent
import javax.inject.Inject

data class EventsUiState(
    val events: List<KafkaEvent> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val api: NexusApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState

    init {
        loadEvents()
    }

    fun loadEvents(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { api.getEvents(page) }
                .onSuccess { result ->
                    _uiState.value = EventsUiState(
                        events = result.data,
                        total = result.resolvedTotal(),
                        page = result.resolvedPage(),
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load events"
                    )
                }
        }
    }
}
