package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import kotlinx.coroutines.delay

/**
 * A unified fake repository used for tests.
 *
 * delayMs = 0 → behaves like a normal fast fake. delayMs > 0 → behaves like a slow fake to test
 * loading states.
 */
class FakeReportRepository(
    private val reports: List<Report> = emptyList(),
    private val delayMs: Long = 0L
) : ReportRepository {

  override fun getNewReportId(): String = "fake-id"

  override suspend fun getAllReports(uid: String): List<Report> {
    delay(delayMs)
    return reports
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    delay(delayMs)
    return reports.filter { it.farmerId == farmerId }
  }

  override suspend fun getReportsByVet(vetId: String): List<Report> {
    delay(delayMs)
    return reports.filter { it.vetId == vetId }
  }

  override suspend fun getReportById(id: String): Report? {
    delay(delayMs)
    return reports.find { it.id == id }
  }

  override suspend fun addReport(report: Report) {
    /* no-op */
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    /* no-op */
  }

  override suspend fun deleteReport(reportId: String) {
    /* no-op */
  }
}
