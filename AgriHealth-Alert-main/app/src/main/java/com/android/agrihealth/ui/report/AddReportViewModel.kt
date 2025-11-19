package com.android.agrihealth.ui.report

import android.net.Uri
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
    val chosenVet: String = "", // TODO: Shouldn't be a string! Temporary measure
    val photoUri: Uri? = null
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

  override fun setVet(vetId: String) {
    _uiState.value = _uiState.value.copy(chosenVet = vetId)
  }

  override fun setPhoto(uri: String?) {
    _uiState.value = _uiState.value.copy(photoUri = uri)
  }

  override fun removePhoto() {
    _uiState.value = _uiState.value.copy(photoUri = null)
  }

  override suspend fun createReport(): Boolean {
    val uiState = _uiState.value
    if (uiState.title.isBlank() || uiState.description.isBlank()) {
      return false
    }

    val newReport =
        Report(
            id = reportRepository.getNewReportId(),
            title = uiState.title,
            description = uiState.description,
            questionForms = emptyList(),
            photoUri = uiState.photoUri,
            farmerId = userId,
            vetId = uiState.chosenVet,
            status = ReportStatus.PENDING,
            answer = null,
            location = Location(46.7990813, 6.6259961) // null // optional until implemented
            )

    viewModelScope.launch { reportRepository.addReport(newReport) }

    // Clears all the fields
    clearInputs() // TODO: Call only if addReport succeeds

    return true
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
