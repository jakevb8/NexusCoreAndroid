package com.nexuscore.android.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuscore.android.data.api.NexusApi
import com.nexuscore.android.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class AssetsUiState(
    val assets: List<Asset> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val importResult: CsvImportResult? = null,
    val successMessage: String? = null,
    val isManager: Boolean = false
)

@HiltViewModel
class AssetsViewModel @Inject constructor(private val api: NexusApi) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    private var currentSearch: String? = null

    init { loadAssets() }

    fun loadAssets(page: Int = 1, search: String? = currentSearch) {
        currentSearch = search
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = api.getAssets(page, search)
                val me = api.me()
                _uiState.update {
                    it.copy(
                        assets = result.data,
                        total = result.total,
                        page = page,
                        isLoading = false,
                        isManager = me.role == Role.ORG_MANAGER || me.role == Role.SUPERADMIN
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteAsset(id: String) {
        viewModelScope.launch {
            try {
                api.deleteAsset(id)
                _uiState.update { it.copy(successMessage = "Asset deleted") }
                loadAssets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun importCsv(csvBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, importResult = null) }
            try {
                val body = csvBytes.toRequestBody("text/csv".toMediaType())
                val part = MultipartBody.Part.createFormData("file", fileName, body)
                val result = api.importCsv(part)
                _uiState.update { it.copy(importResult = result, isLoading = false) }
                loadAssets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null, importResult = null) }
    }
}
