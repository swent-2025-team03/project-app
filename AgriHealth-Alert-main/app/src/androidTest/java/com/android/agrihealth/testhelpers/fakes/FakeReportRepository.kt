package com.android.agrihealth.testhelpers.fakes

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportRepository
import kotlinx.coroutines.delay

/** Generic in-memory implementation of [ReportRepository] for local dev and tests. */
open class FakeReportRepository(
    initialReports: List<Report> = emptyList(),
    private val delayMs: Long = 0L
) : ReportRepository {

  private val reports: MutableList<Report> = initialReports.toMutableList()
  private var nextId = 0
  var editCalled: Boolean = false
  var lastAddedReport: Report? = null

  override fun getNewReportId(): String {
    return (nextId++).toString()
  }

  override suspend fun getAllReports(userId: String): List<Report> {
    delay(delayMs)
    return reports.filter { it.farmerId == userId || it.assignedVet == userId }
  }

  override suspend fun getReportById(reportId: String): Report? {
    delay(delayMs)
    return reports.find { it.id == reportId }
        ?: throw NoSuchElementException("InMemoryReportRepository: Report not found")
  }

  override suspend fun addReport(report: Report) {
    delay(delayMs)
    reports.removeAll { it.id == report.id }
    reports.add(report)
    lastAddedReport = report
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    editCalled = true
    delay(delayMs)
    val index = reports.indexOfFirst { it.id == reportId }
    if (index != -1) {
      reports[index] = newReport
    } else {
      throw NoSuchElementException("InMemoryReportRepository: report not found")
    }
  }

  override suspend fun deleteReport(reportId: String) {
    delay(delayMs)
    val removed = reports.removeIf { it.id == reportId }
    if (!removed) {
      throw NoSuchElementException("InMemoryReportRepository: Report not found")
    }
  }

  override suspend fun assignReportToVet(reportId: String, vetId: String) {
    delay(delayMs)
  }

  override suspend fun unassignReport(reportId: String) {
    delay(delayMs)
  }
}
