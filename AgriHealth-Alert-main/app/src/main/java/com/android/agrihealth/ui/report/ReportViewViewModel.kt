package com.android.agrihealth.ui.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.device.notifications.Notification
import com.android.agrihealth.data.model.device.notifications.NotificationHandlerFirebase
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import com.android.agrihealth.ui.loading.withLoadingState
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
            photoURL = null,
            farmerId = "FARMER_123",
            officeId = "OFF_456",
            status = ReportStatus.PENDING,
            answer = null,
            location = Location(46.5191, 6.5668, "Lausanne Farm"),
            assignedVet = "valid_vet_id"),
    val answerText: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val isLoading: Boolean = false,
)

/**
 * ViewModel holding the state of a report being viewed. Currently uses mock data and local state
 * only.
 */
class ReportViewViewModel(
    private val repository: ReportRepository = ReportRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReportViewUIState())
  val uiState: StateFlow<ReportViewUIState> = _uiState.asStateFlow()

  private val _unsavedChanges = MutableStateFlow(false)
  val unsavedChanges: StateFlow<Boolean> = _unsavedChanges.asStateFlow()

  // Flag to indicate that saving is completed (success). Observed by the UI for navigation.
  private val _saveCompleted = MutableStateFlow(false)
  val saveCompleted: StateFlow<Boolean> = _saveCompleted.asStateFlow()

  /** Loads a report by its ID and updates the state. */
  fun loadReport(reportID: String) {
    viewModelScope.launch {
      _uiState.withLoadingState(
          applyLoading = { state, loading -> state.copy(isLoading = loading) }) {
            try {
              val fetchedReport = repository.getReportById(reportID)
              if (fetchedReport != null) {
                _uiState.value =
                    ReportViewUIState(
                        report = fetchedReport,
                        answerText = fetchedReport.answer ?: "",
                        status = fetchedReport.status)
              } else {
                Log.e("ReportViewModel", "Report with ID $reportID not found.")
              }
            } catch (e: Exception) {
              Log.e("ReportViewModel", "Error loading Report by ID: $reportID", e)
            }
          }
    }
  }

  /**
   * Called when something changed, to notify the user that they didn't save their changes if they
   * try to leave the screen
   */
  private fun flagChanges() {
    _unsavedChanges.value = true
  }

  fun onAnswerChange(newText: String) {
    _uiState.value = _uiState.value.copy(answerText = newText)
    flagChanges()
  }

  fun onStatusChange(newStatus: ReportStatus) {
    _uiState.value = _uiState.value.copy(status = newStatus)
    flagChanges()
  }

  fun onSpam() {
    _uiState.value = _uiState.value.copy(status = ReportStatus.SPAM)
    onSave()
  }

  fun onDelete() {
    viewModelScope.launch { repository.deleteReport(reportId = _uiState.value.report.id) }
  }

  /** Saves the modified report, then triggers the saveCompleted flag on success. */
  fun onSave() {
    viewModelScope.launch {
      _uiState.withLoadingState(
          applyLoading = { state, loading -> state.copy(isLoading = loading) }) {
            try {
              val currentReport = _uiState.value.report
              val newAnswer = _uiState.value.answerText
              val newStatus = _uiState.value.status

              if (currentReport.answer != newAnswer || currentReport.status != newStatus) {
                val updatedReport = currentReport.copy(answer = newAnswer, status = newStatus)
                repository.editReport(updatedReport.id, updatedReport)

                // Send a notification
                val farmerId = updatedReport.farmerId
                val description = "Your report '${updatedReport.title}' has new changes!"
                val notification =
                    Notification.VetAnswer(destinationUid = farmerId, description = description)
                val messagingService = NotificationHandlerFirebase()
                messagingService.uploadNotification(notification)
              }
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
    consumeUnsavedChanges()
  }

  fun consumeUnsavedChanges() {
    _unsavedChanges.value = false
  }

  /** Assigns the report to the current vet. */
  fun assignReportToVet(vetId: String) {
    val reportId = _uiState.value.report.id

    if (!_uiState.value.report.assignedVet.isNullOrEmpty()) return

    viewModelScope.launch {
      repository.assignReportToVet(reportId, vetId)

      // Refresh state after assignment
      loadReport(reportId)
    }
  }

  /** Unassigns the report from the current vet. */
  fun unassign() {
    val report = _uiState.value.report
    val reportId = report.id

    if (!report.answer.isNullOrEmpty()) return

    viewModelScope.launch {
      repository.unassignReport(reportId)

      // Refresh state after unassignment
      loadReport(reportId)
    }
  }
}
