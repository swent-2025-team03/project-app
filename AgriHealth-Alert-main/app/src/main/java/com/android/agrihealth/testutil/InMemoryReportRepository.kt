package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import kotlinx.coroutines.delay

open class InMemoryReportRepository(
    initialReports: List<Report> = emptyList(),
    private val delayMs: Long = 0L
) : ReportRepository {

  private val reports: MutableList<Report> = initialReports.toMutableList()
  private var nextId = 0

  override fun getNewReportId(): String {
    return (nextId++).toString()
  }

  override suspend fun getAllReports(userId: String): List<Report> {
    delay(delayMs)
    return reports.filter { it.farmerId == userId || it.officeId == userId }
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
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
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
    TODO("Not yet implemented")
  }

  override suspend fun unassignReport(reportId: String) {
    delay(delayMs)
    TODO("Not yet implemented")
  }

  fun reset(newReports: List<Report> = emptyList()) {
    reports.clear()
    reports.addAll(newReports)
    nextId = 0
  }
}
