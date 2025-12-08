package com.android.agrihealth.data.model.images

import android.net.Uri
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface ImageViewModelContract {
  val uiState: StateFlow<ImageUIState>
  fun upload(uri: Uri): Job
  fun download(path: String): Job
}
