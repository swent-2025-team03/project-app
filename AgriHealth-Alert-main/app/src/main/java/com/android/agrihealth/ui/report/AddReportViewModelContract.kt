package com.android.agrihealth.ui.report

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface AddReportViewModelContract {
  val uiState: StateFlow<AddReportUiState>

  fun setTitle(newTitle: String)

  fun setDescription(newDescription: String)

  fun setVet(vetId: String)

  suspend fun createReport(): Boolean

  fun clearInputs()

  fun setPhoto(uri: Uri?) // TODO: Change to "String?"

  fun removePhoto()
}
