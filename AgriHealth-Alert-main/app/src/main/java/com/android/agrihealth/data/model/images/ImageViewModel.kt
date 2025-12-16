package com.android.agrihealth.data.model.images

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI state to tell the UI what the view-model is doing. Meant to be read only through the view
 * model, with a "when ()" on the current state
 */
sealed class ImageUIState {
  object Idle : ImageUIState()

  object Loading : ImageUIState()

  data class UploadSuccess(val path: String) : ImageUIState()

  @Suppress("ArrayInDataClass")
  data class DownloadSuccess(val imageData: ByteArray) : ImageUIState()

  data class Error(val e: Throwable) : ImageUIState()
}

/**
 * View-model to handle image upload/download with the photo storage backend. Designed to use
 * upload() or download() and then use a "when ()" on the UI state to handle response.
 */
class ImageViewModel(private val repository: ImageRepository = ImageRepositoryProvider.repository) :
    ViewModel() {
  private val _uiState = MutableStateFlow<ImageUIState>(ImageUIState.Idle)
  val uiState: StateFlow<ImageUIState> = _uiState

  /**
   * Uploads an image to the photos backend using a URI to the file content. Updates UI state to
   * UploadSuccess, containing the online path to the image
   */
  fun upload(uri: Uri) =
      viewModelScope.launch {
        _uiState.value = ImageUIState.Loading

        val bytes = repository.reduceFileSize(repository.resolveUri(uri))

        repository
            .uploadImage(bytes)
            .onSuccess { path -> _uiState.value = ImageUIState.UploadSuccess(path) }
            .onFailure { e -> _uiState.value = ImageUIState.Error(e) }
      }

  /**
   * Downloads an image from the photos backend using the online path. Updates UI state to
   * DownloadSuccess, containing the ByteArray image data
   */
  fun download(path: String) =
      viewModelScope.launch {
        _uiState.value = ImageUIState.Loading

        repository
            .downloadImage(path)
            .onSuccess { bytes -> _uiState.value = ImageUIState.DownloadSuccess(bytes) }
            .onFailure { e -> _uiState.value = ImageUIState.Error(e) }
      }
}
