package me.jakev.nexuscore.ui.assets

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.jakev.nexuscore.data.api.NexusApi
import me.jakev.nexuscore.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

private const val TAG = "NexusCore"

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
                        total = result.resolvedTotal(),
                        page = page,
                        isLoading = false,
                        isManager = me.role == Role.ORG_MANAGER || me.role == Role.SUPERADMIN
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAssets failed", e)
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
                Log.e(TAG, "deleteAsset failed id=$id", e)
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
                Log.e(TAG, "importCsv failed", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun downloadSampleCsv(context: Context) {
        viewModelScope.launch {
            try {
                val body = api.downloadSampleCsv()
                val bytes = body.bytes()
                val fileName = "nexuscore_sample.csv"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        resolver.openOutputStream(it)?.use { out -> out.write(bytes) }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(it, values, null, null)
                    }
                } else {
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    File(dir, fileName).writeBytes(bytes)
                }
                _uiState.update { it.copy(successMessage = "Sample CSV saved to Downloads") }
            } catch (e: Exception) {
                Log.e(TAG, "downloadSampleCsv failed", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null, importResult = null) }
    }
}
