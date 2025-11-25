package com.android.agrihealth.ui.report

import android.net.Uri
import com.android.agrihealth.data.model.report.QuestionForm
import kotlinx.coroutines.flow.StateFlow

interface AddReportViewModelContract {
  val uiState: StateFlow<AddReportUiState>

  fun setTitle(newTitle: String)

  fun setDescription(newDescription: String)

  fun setVet(vetId: String)

  fun updateQuestion(index: Int, updated: QuestionForm)

  suspend fun createReport(): Boolean

  fun clearInputs()

  fun setPhoto(uri: Uri?) // TODO: Change to "String?"

  fun removePhoto()

  fun setUploadedImagePath(path: String?)
}
