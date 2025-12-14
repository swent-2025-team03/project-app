package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report

class TestReportRepository(initialReports: List<Report> = emptyList()) :
    InMemoryReportRepository(initialReports) {

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

  override suspend fun assignReportToVet(reportId: String, vetId: String) {}

  override suspend fun unassignReport(reportId: String) {}
}
