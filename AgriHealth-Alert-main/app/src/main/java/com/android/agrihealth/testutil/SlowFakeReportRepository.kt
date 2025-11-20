package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository

class SlowFakeReportRepository(
    private val reports: List<Report> = emptyList(),
    private val delayMs: Long = 1200
) : ReportRepository {

  override fun getNewReportId(): String = "slow-id"

  override suspend fun getAllReports(uid: String): List<Report> {
    kotlinx.coroutines.delay(delayMs)
    return reports
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    kotlinx.coroutines.delay(delayMs)
    return reports.filter { it.farmerId == farmerId }
  }

  override suspend fun getReportsByVet(vetId: String): List<Report> {
    kotlinx.coroutines.delay(delayMs)
    return reports.filter { it.vetId == vetId }
  }

  override suspend fun getReportById(id: String): Report? {
    kotlinx.coroutines.delay(delayMs)
    return reports.find { it.id == id }
  }

  override suspend fun addReport(report: Report) {}

  override suspend fun editReport(reportId: String, newReport: Report) {}

  override suspend fun deleteReport(reportId: String) {}
}
