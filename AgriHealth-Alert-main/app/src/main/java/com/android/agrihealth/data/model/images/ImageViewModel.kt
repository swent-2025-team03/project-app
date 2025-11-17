package com.android.agrihealth.data.model.images

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ImageUiState {
  object Idle : ImageUiState()

  object Loading : ImageUiState()

  data class UploadSuccess(val path: String) : ImageUiState()

  data class DownloadSuccess(val imageData: ByteArray) : ImageUiState()

  data class Error(val msg: String) : ImageUiState()
}

class ImageViewModel(private val repository: ImageRepository = ImageRepositoryProvider.repository) : ViewModel() {
  private val _uiState = MutableStateFlow<ImageUiState>(ImageUiState.Idle)
  val uiState: StateFlow<ImageUiState> = _uiState

  fun upload(uri: Uri) =
      viewModelScope.launch {
        _uiState.value = ImageUiState.Loading

        val bytes = repository.reduceFileSize(repository.resolveUri(uri))

        repository
            .uploadImage(bytes)
            .onSuccess { path -> _uiState.value = ImageUiState.UploadSuccess(path) }
            .onFailure { e ->
              _uiState.value = ImageUiState.Error("Error uploading the image: ${e.message}")
            }
      }

  fun download(path: String) =
      viewModelScope.launch {
        _uiState.value = ImageUiState.Loading

        repository
            .downloadImage(path)
            .onSuccess { bytes -> _uiState.value = ImageUiState.DownloadSuccess(bytes) }
            .onFailure { e ->
              _uiState.value = ImageUiState.Error("Error downloading the image: ${e.message}")
            }
      }
}
