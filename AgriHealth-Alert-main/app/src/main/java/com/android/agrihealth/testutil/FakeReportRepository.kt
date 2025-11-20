package com.android.agrihealth.fakes

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository

class FakeReportRepository(private val reports: List<Report> = emptyList()) : ReportRepository {

  override fun getNewReportId(): String {
    return "fake-id"
  }

  override suspend fun getAllReports(uid: String): List<Report> {
    // MapViewModel uses only this function for filtering by user
    return reports
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    return reports.filter { it.farmerId == farmerId }
  }

  override suspend fun getReportsByVet(vetId: String): List<Report> {
    return reports.filter { it.vetId == vetId }
  }

  override suspend fun getReportById(id: String): Report? {
    return reports.find { it.id == id }
  }

  override suspend fun addReport(report: Report) {
    // No-op for unit tests
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    // No-op for unit tests
  }

  override suspend fun deleteReport(reportId: String) {
    // No-op for unit tests
  }
}
