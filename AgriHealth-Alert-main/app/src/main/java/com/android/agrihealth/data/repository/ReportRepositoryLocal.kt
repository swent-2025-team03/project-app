package com.android.agrihealth.data.repository

import com.android.agrihealth.data.model.report.Report

/** Local in-memory implementation of ReportRepository for testing and development purposes. */
class ReportRepositoryLocal : ReportRepository {
  private val reports: MutableList<Report> = mutableListOf()

  private var nextId = 0

  override fun getNewReportId(): String {
    return (nextId++).toString()
  }

  override suspend fun getAllReports(userId: String): List<Report> {
    return reports.filter { it.farmerId == userId || it.officeId == userId }
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    return reports.filter { it.farmerId == farmerId }
  }

  override suspend fun getReportsByVet(vetId: String): List<Report> {
    return reports.filter { it.officeId == vetId }
  }

  override suspend fun getReportById(reportId: String): Report? {
    return reports.find { it.id == reportId }
        ?: throw NoSuchElementException("ReportRepositoryLocal: Report not found")
  }

  override suspend fun addReport(report: Report) {
    try {
      editReport(report.id, report)
    } catch (e: Exception) {
      reports.add(report)
    }
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    val index = reports.indexOfFirst { it.id == reportId }
    if (index != -1) {
      reports[index] = newReport
    } else {
      throw NoSuchElementException("ReportRepositoryLocal: Report not found")
    }
  }

  override suspend fun deleteReport(reportId: String) {
    val index = reports.indexOfFirst { it.id == reportId }
    if (index != -1) {
      reports.removeAt(index)
    } else {
      throw NoSuchElementException("ReportRepositoryLocal: Report not found")
    }
  }
}
