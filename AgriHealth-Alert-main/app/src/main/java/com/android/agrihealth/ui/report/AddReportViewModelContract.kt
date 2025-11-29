package com.android.agrihealth.ui.report

import com.android.agrihealth.data.model.report.QuestionForm
import kotlinx.coroutines.flow.StateFlow

interface AddReportViewModelContract {
  val uiState: StateFlow<AddReportUiState>

  fun switchIsCollected()
  fun setTitle(newTitle: String)

  fun setDescription(newDescription: String)

  fun setOffice(officeId: String)

  fun updateQuestion(index: Int, updated: QuestionForm)

  suspend fun createReport(): Boolean

  fun clearInputs()
}
