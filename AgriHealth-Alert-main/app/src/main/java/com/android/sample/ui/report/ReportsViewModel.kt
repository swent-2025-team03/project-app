package com.android.sample.ui.report

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.android.sample.data.model.*

/**
 * ViewModel holding the state of a report being viewed. Currently uses mock data and local state
 * only.
 */
class ReportViewModel : ViewModel() {

  // ---- Mock report for testing ----
  private val _report =
      mutableStateOf(
          Report(
              id = "RPT001",
              title = "My sheep is acting strange",
              description = "Since this morning, my sheep keeps getting on its front knees.",
              photoUri = null, // Placeholder for now
              farmerId = "FARMER_123",
              vetId = "VET_456",
              status = ReportStatus.PENDING,
              answer = null,
              location = Location(46.5191, 6.5668, "Lausanne Farm")))

  // Expose immutable version of report
  val report: State<Report> = _report

  // ---- Local mutable state for vet actions ----
  private val _answerText = mutableStateOf(_report.value.answer ?: "")
  val answerText: State<String> = _answerText

  private val _status = mutableStateOf(_report.value.status)
  val status: State<ReportStatus> = _status

  // ---- Update functions ----
  fun onAnswerChange(newText: String) {
    _answerText.value = newText
  }

  fun onStatusChange(newStatus: ReportStatus) {
    _status.value = newStatus
  }

  fun onEscalate() {
    _status.value = ReportStatus.ESCALATED
  }

  // Save logic will be implemented later (Firebase, etc.)
  fun onSave() {
    // For now, do nothing
  }
}
