package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.report.ReportViewUIState

class FakeReportRepository : ReportRepository {
  // Used by ReportViewScreenTest to verify that a save occurred
  var editCalled = false

  // Used by AddReportViewModelTest to verify the created report
  var lastAddedReport: Report? = null
  private val sample = ReportViewUIState().report

  override fun getNewReportId(): String = "NEW_ID"

  override suspend fun getAllReports(userId: String): List<Report> = emptyList()

  override suspend fun getReportById(reportId: String): Report? = sample.copy(id = reportId)

  override suspend fun addReport(report: Report) {
    // Store last added report for unit tests (AddReportViewModelTest)
    lastAddedReport = report
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    editCalled = true
  }

  override suspend fun deleteReport(reportId: String) {
    // no-op: not required in current tests.
  }
}
