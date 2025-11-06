package com.android.agrihealth.ui.report

import kotlinx.coroutines.flow.StateFlow

interface AddReportViewModelContract {
  val uiState: StateFlow<AddReportUiState>

  fun setTitle(newTitle: String)

  fun setDescription(newDescription: String)

  fun setVet(vetId: String)

  suspend fun createReport(): Boolean

  fun clearInputs()
}
