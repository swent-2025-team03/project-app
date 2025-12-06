package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/** Slow wrapper around InMemoryReportRepository to simulate a slow backend. */
class SlowFakeReportRepository(
    reports: List<Report> = emptyList(),
    private val delayMs: Long = 1200
) : InMemoryReportRepository() {

  init {
    // Preload initial reports without delay, inside a blocking coroutine
    runBlocking { reports.forEach { super.addReport(it) } }
  }

  override fun getNewReportId(): String {
    return "slow-id"
  }

  override suspend fun getAllReports(uid: String): List<Report> {
    delay(delayMs)
    return super.getAllReports(uid)
  }

  override suspend fun getReportById(id: String): Report? {
    delay(delayMs)
    return super.getReportById(id)
  }

  override suspend fun addReport(report: Report) {
    delay(delayMs)
    super.addReport(report)
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    delay(delayMs)
    super.editReport(reportId, newReport)
  }

  override suspend fun deleteReport(reportId: String) {
    delay(delayMs)
    super.deleteReport(reportId)
  }

  override suspend fun assignReportToVet(reportId: String, vetId: String) {
    delay(delayMs)
    super.assignReportToVet(reportId, vetId)
  }

  override suspend fun unassignReport(reportId: String) {
    super.unassignReport(reportId)
  }
}
