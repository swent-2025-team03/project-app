package com.android.agrihealth.ui.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.*
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsViewUIState(
    val report: Report =
        Report(
            id = "RPT001",
            title = "My sheep is acting strange",
            description = "Since this morning, my sheep keeps getting on its front knees.",
            photoUri = null, // Placeholder for now
            farmerId = "FARMER_123",
            vetId = "VET_456",
            status = ReportStatus.PENDING,
            answer = null,
            location = Location(46.5191, 6.5668, "Lausanne Farm")),
    val answerText: String = "",
    val status: ReportStatus = ReportStatus.PENDING
)

/**
 * ViewModel holding the state of a report being viewed. Currently uses mock data and local state
 * only.
 */
class ReportViewModel(
    private val repository: ReportRepository = ReportRepositoryProvider.repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsViewUIState())
    val uiState: StateFlow<ReportsViewUIState> = _uiState.asStateFlow()

    /**
     * Loads a Report by its ID and updates the _report.
     *
     * @param reportID The ID of the Report to be loaded.
     */
    fun loadReport(reportID: String) {
        viewModelScope.launch {
            try {
                val fetchedReport = repository.getReportById(reportID)
                if(fetchedReport != null) {
                    _uiState.value = ReportsViewUIState(
                        report = fetchedReport,
                        answerText = fetchedReport.answer ?: "",
                        status = fetchedReport.status
                    )
                }else{
                    Log.e("ReportViewModel", "Report with ID $reportID not found.")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error loading Report by ID: $reportID", e)
            }
        }
    }


    // ---- Update functions ----
    fun onAnswerChange(newText: String) {
        _uiState.value = _uiState.value.copy(answerText = newText)
    }

    fun onStatusChange(newStatus: ReportStatus) {
        _uiState.value = _uiState.value.copy(status = newStatus)
    }

    fun onEscalate() {
        _uiState.value = _uiState.value.copy(status = ReportStatus.ESCALATED)
    }


  fun onSave() {
      viewModelScope.launch {
          try {
              val updatedReport = _uiState.value.report.copy(
                  answer = _uiState.value.answerText,
                  status = _uiState.value.status
              )
              repository.editReport(updatedReport.id, updatedReport)
              Log.d("ReportViewModel", "Report saved successfully.")
          } catch (e: Exception) {
              Log.e("ReportViewModel", "Error saving report", e)
          }
      }
  }
}
