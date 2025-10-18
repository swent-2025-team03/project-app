package com.android.agrihealth.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenVet: String = "", // TODO: Shouldn't be a string! Temporary measure
)

class AddReportViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddReportUiState())
  val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()

  fun setTitle(newTitle: String) {
    _uiState.value = _uiState.value.copy(title = newTitle)
  }

  fun setDescription(newDescription: String) {
    _uiState.value = _uiState.value.copy(description = newDescription)
  }

  fun setVet(option: String) {
    _uiState.value = _uiState.value.copy(chosenVet = option)
  }

  fun createReport(): Boolean {
    val uiState = _uiState.value
    if (uiState.title.isBlank() || uiState.description.isBlank()) {
      return false
    }

    val newReport =
        Report(
            id = reportRepository.getNewReportId(),
            title = uiState.title,
            description = uiState.description,
            photoUri = null, // currently unused
            farmerId = "currentUserId", //
            vetId = uiState.chosenVet.takeIf { it.isNotBlank() },
            status = ReportStatus.PENDING,
            answer = null,
            location = null // optional until implemented
            )

    viewModelScope.launch { reportRepository.addReport(newReport) }

    // Clears all the fields
    clearInputs()

    return true
  }

  fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
