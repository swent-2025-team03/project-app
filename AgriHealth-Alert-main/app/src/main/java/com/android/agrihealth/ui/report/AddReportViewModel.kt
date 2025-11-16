package com.android.agrihealth.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenVet: String = "",
    val isLoading: Boolean = false,
)

class AddReportViewModel(
    private val userId: String,
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository
) : ViewModel(), AddReportViewModelContract {
  private val _uiState = MutableStateFlow(AddReportUiState())
  override val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()

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
    val current = _uiState.value
    if (current.title.isBlank() || current.description.isBlank()) {
      return false
    }
    // Set loading true
    _uiState.value = _uiState.value.copy(isLoading = true)

    val newReport = Report(
        id = reportRepository.getNewReportId(),
        title = current.title,
        description = current.description,
        photoUri = null,
        farmerId = userId,
        vetId = current.chosenVet,
        status = ReportStatus.PENDING,
        answer = null,
        location = Location(46.7990813, 6.6259961)
    )

    viewModelScope.launch {
      try {
        reportRepository.addReport(newReport)
        // Success: stop loading
        _uiState.value = _uiState.value.copy(isLoading = false)
        clearInputs() // TODO conditionner au succès réel si nécessaire
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false)
      }
    }

    return true
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
