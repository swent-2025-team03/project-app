package com.android.agrihealth.testutil

import androidx.lifecycle.ViewModel
import com.android.agrihealth.ui.report.AddReportUiState
import com.android.agrihealth.ui.report.AddReportViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAddReportViewModel : ViewModel(), AddReportViewModelContract {
  private val _uiState = MutableStateFlow(AddReportUiState())
  override val uiState: StateFlow<AddReportUiState> = _uiState

  override fun setTitle(newTitle: String) {
    _uiState.value = _uiState.value.copy(title = newTitle)
  }

  override fun setDescription(newDescription: String) {
    _uiState.value = _uiState.value.copy(description = newDescription)
  }

  override fun setVet(option: String) {
    _uiState.value = _uiState.value.copy(chosenVet = option)
  }

  override suspend fun createReport(): Boolean {
    return _uiState.value.title.isNotBlank() && _uiState.value.description.isNotBlank()
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
