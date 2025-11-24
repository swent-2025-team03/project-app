package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.report.ReportViewUIState

class FakeReportRepository : ReportRepository {
  var editCalled = false
  private val sample = ReportViewUIState().report

  override fun getNewReportId(): String = "NEW_ID"

  override suspend fun getAllReports(userId: String): List<Report> = emptyList()

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> = emptyList()

  override suspend fun getReportsByVet(vetId: String): List<Report> = emptyList()

  override suspend fun getReportById(reportId: String): Report? = sample.copy(id = reportId)

  override suspend fun addReport(report: Report) {
    /* no-op */
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    editCalled = true
  }

  override suspend fun deleteReport(reportId: String) {
    /* no-op */
  }
}
