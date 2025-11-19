package com.android.agrihealth.ui.report

import android.util.Log
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

/**
 * Represents the UI state for viewing and editing a single report. This data class allows for less
 * subscribing to individual state variables in the ViewModel.
 *
 * @property report The `Report` being viewed or edited. Defaults to a sample report.
 * @property answerText The current text of the answer input field. Defaults to an empty string.
 * @property status The current status of the report. Defaults to `ReportStatus.PENDING`.
 */
data class ReportViewUIState(
    val report: Report =
        Report(
            id = "RPT001",
            title = "My sheep is acting strange",
            description = "Since this morning, my sheep keeps getting on its front knees.",
            questionForms = emptyList(),
            photoUri = null, // Placeholder for now
            farmerId = "FARMER_123",
            vetId = "VET_456",
            status = ReportStatus.PENDING,
            answer = null,
            location = Location(46.5191, 6.5668, "Lausanne Farm")),
    val answerText: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val isLoading: Boolean = false,
)

/**
 * ViewModel holding the state of a report being viewed. Currently uses mock data and local state
 * only.
 */
class ReportViewModel(
    private val repository: ReportRepository = ReportRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReportViewUIState())
  val uiState: StateFlow<ReportViewUIState> = _uiState.asStateFlow()

  // Flag to indicate that saving is completed (success). Observed by the UI for navigation.
  private val _saveCompleted = MutableStateFlow(false)
  val saveCompleted: StateFlow<Boolean> = _saveCompleted.asStateFlow()

  private suspend fun <T> withLoading(block: suspend () -> T): T {
    _uiState.value = _uiState.value.copy(isLoading = true)
    return try {
      block()
    } finally {
      _uiState.value = _uiState.value.copy(isLoading = false)
    }
  }

  /** Loads a report by its ID and updates the state. */
  fun loadReport(reportID: String) {
    viewModelScope.launch {
      withLoading {
        try {
          val fetchedReport = repository.getReportById(reportID)
          if (fetchedReport != null) {
            _uiState.value =
                _uiState.value.copy(
                    report = fetchedReport,
                    answerText = fetchedReport.answer ?: "",
                    status = fetchedReport.status,
                )
          }
          // si null : on laisse le state comme avant (sauf isLoading qui est géré par withLoading)
        } catch (e: Exception) {
          Log.e("ReportViewModel", "Error loading Report by ID: $reportID", e)
        }
      }
    }
  }

  fun onAnswerChange(newText: String) {
    _uiState.value = _uiState.value.copy(answerText = newText)
  }

  fun onStatusChange(newStatus: ReportStatus) {
    _uiState.value = _uiState.value.copy(status = newStatus)
  }

  fun onSpam() {
    _uiState.value = _uiState.value.copy(status = ReportStatus.SPAM)
  }

  /** Saves the modified report, then triggers the saveCompleted flag on success. */
  fun onSave() {
    viewModelScope.launch {
      withLoading {
        try {
          val current = _uiState.value
          val updatedReport =
              current.report.copy(
                  answer = current.answerText,
                  status = current.status,
              )

          repository.editReport(updatedReport.id, updatedReport)
          _saveCompleted.value = true
        } catch (e: Exception) {
          Log.e("ReportViewModel", "Error saving report", e)
        }
      }
    }
  }

  /** Resets the flag after the UI has consumed the navigation event. */
  fun consumeSaveCompleted() {
    _saveCompleted.value = false
  }
}
