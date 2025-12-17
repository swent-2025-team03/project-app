package com.android.agrihealth.data.model.images

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
 * A union type so that a photo can either be stored in a ByteArray or in a Uri.
 *
 * This is meant as an internal type, it should not appear in any public interface. It is meant to
 * be used internally when overloading a function
 */
sealed interface PhotoType {
  data class ByteArray(val bytes: kotlin.ByteArray) : PhotoType

  data class Uri(val uri: android.net.Uri) : PhotoType
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
   * Uploads an image to the photos backend using a ByteArray directly. Updates UI state to
   * UploadSuccess, containing the online path to the image
   */
  fun upload(byteArray: ByteArray) = uploadImplementation(PhotoType.ByteArray(byteArray))

  /**
   * Uploads an image to the photos backend using a URI to the file content. Updates UI state to
   * UploadSuccess, containing the online path to the image
   */
  fun upload(uri: Uri) = uploadImplementation(PhotoType.Uri(uri))

  // Actual implementation of the upload function
  private fun uploadImplementation(photo: PhotoType) {
    viewModelScope.launch {
      _uiState.value = ImageUIState.Loading

      var bytes =
          when (photo) {
            is PhotoType.ByteArray -> photo.bytes
            is PhotoType.Uri -> repository.resolveUri(photo.uri)
          }
      bytes = repository.reduceFileSize(bytes)

      repository
          .uploadImage(bytes)
          .onSuccess { path -> _uiState.value = ImageUIState.UploadSuccess(path) }
          .onFailure { e -> _uiState.value = ImageUIState.Error(e) }
    }
  }

  /**
   * Starts uploading the given image bytes of the photo and suspends until the upload finishes.
   *
   * @return The URL of the uploaded photo on success.
   * @throws Throwable If the upload fails (rethrows the underlying error).
   */
  suspend fun uploadAndWait(byteArray: ByteArray): String {
    return uploadAndWaitImplementation(PhotoType.ByteArray(byteArray))
  }

  private suspend fun uploadAndWaitImplementation(photo: PhotoType): String {
    uploadImplementation(photo) // start the upload
    val result = uiState.first { it is ImageUIState.UploadSuccess || it is ImageUIState.Error }
    return when (result) {
      is ImageUIState.UploadSuccess -> result.path
      is ImageUIState.Error -> throw result.e
      else -> throw IllegalStateException("Unexpected state")
    }
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
