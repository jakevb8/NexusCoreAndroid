package com.nexuscore.android.ui.assets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuscore.android.data.api.NexusApi
import com.nexuscore.android.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetDetailUiState(
    val asset: Asset? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
    // form fields
    val name: String = "",
    val sku: String = "",
    val description: String = "",
    val status: AssetStatus = AssetStatus.AVAILABLE,
    val assignedTo: String = ""
)

@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    private val api: NexusApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assetId: String = checkNotNull(savedStateHandle["assetId"])
    val isNew get() = assetId == "new"

    private val _uiState = MutableStateFlow(AssetDetailUiState())
    val uiState: StateFlow<AssetDetailUiState> = _uiState.asStateFlow()

    init {
        if (!isNew) loadAsset()
    }

    private fun loadAsset() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val assets = api.getAssets()
                val asset = assets.data.firstOrNull { it.id == assetId }
                if (asset != null) {
                    _uiState.update {
                        it.copy(
                            asset = asset, isLoading = false,
                            name = asset.name, sku = asset.sku,
                            description = asset.description ?: "",
                            status = asset.status,
                            assignedTo = asset.assignedTo ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onSkuChange(v: String) = _uiState.update { it.copy(sku = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onStatusChange(v: AssetStatus) = _uiState.update { it.copy(status = v) }
    fun onAssignedToChange(v: String) = _uiState.update { it.copy(assignedTo = v) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                if (isNew) {
                    api.createAsset(CreateAssetRequest(
                        name = state.name, sku = state.sku,
                        description = state.description.ifBlank { null },
                        status = state.status,
                        assignedTo = state.assignedTo.ifBlank { null }
                    ))
                } else {
                    api.updateAsset(assetId, UpdateAssetRequest(
                        name = state.name, sku = state.sku,
                        description = state.description.ifBlank { null },
                        status = state.status,
                        assignedTo = state.assignedTo.ifBlank { null }
                    ))
                }
                _uiState.update { it.copy(isSaving = false, done = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
