package com.android.agrihealth.testhelpers.fakes

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportRepository

/** Generic in-memory implementation of [ReportRepository] for local dev and tests. */
open class InMemoryReportRepository(initialReports: List<Report> = emptyList()) : ReportRepository {

  private val reports: MutableList<Report> = initialReports.toMutableList()
  private var nextId = 0

  override fun getNewReportId(): String = (nextId++).toString()

  override suspend fun getAllReports(userId: String): List<Report> =
      reports.filter { it.farmerId == userId || it.officeId == userId }

  override suspend fun getReportById(reportId: String): Report? =
      reports.find { it.id == reportId }
          ?: throw NoSuchElementException("InMemoryReportRepository: Report not found")

  override suspend fun addReport(report: Report) {
    reports.removeAll { it.id == report.id }
    reports.add(report)
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    val index = reports.indexOfFirst { it.id == reportId }
    if (index != -1) {
      reports[index] = newReport
    } else {
      throw NoSuchElementException("InMemoryReportRepository: report not found")
    }
  }

  override suspend fun deleteReport(reportId: String) {
    val removed = reports.removeIf { it.id == reportId }
    if (!removed) {
      throw NoSuchElementException("InMemoryReportRepository: Report not found")
    }
  }

  override suspend fun assignReportToVet(reportId: String, vetId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun unassignReport(reportId: String) {
    TODO("Not yet implemented")
  }

  fun reset(newReports: List<Report> = emptyList()) {
    reports.clear()
    reports.addAll(newReports)
    nextId = 0
  }
}
