package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report

class TestReportRepository(initialReports: List<Report> = emptyList()) :
    InMemoryReportRepository(initialReports) {

  // Hooks de test
  var editCalled: Boolean = false
  var lastAddedReport: Report? = null

  override suspend fun addReport(report: Report) {
    lastAddedReport = report
    super.addReport(report)
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    editCalled = true
    super.editReport(reportId, newReport)
  }
}
