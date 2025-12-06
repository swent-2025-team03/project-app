package com.android.agrihealth.ui.report

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.QuestionForm
import kotlinx.coroutines.flow.StateFlow

interface AddReportViewModelContract {
  val uiState: StateFlow<AddReportUiState>

  fun switchCollected()

  fun setTitle(newTitle: String)

  fun setDescription(newDescription: String)

  fun setOffice(officeId: String)

  fun setAddress(address: Location?)

  fun updateQuestion(index: Int, updated: QuestionForm)

  suspend fun createReport(): Boolean

  fun clearInputs()
}
