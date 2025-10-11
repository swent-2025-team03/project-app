package com.android.agrihealth.data.repository

import com.android.agrihealth.data.model.Report

/**
 * Local in-memory implementation of ReportRepository for testing and development purposes.
 */
class ReportRepositoryLocal : ReportRepository {
  private val reports: MutableList<Report> = mutableListOf()

  private var nextId = 0

  override fun getNewReportId(): String {
    return (nextId++).toString()
  }

  override suspend fun getAllReports(userId: String): List<Report> {
    return reports.filter { it.farmerId == userId || it.vetId == userId }
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    return reports.filter { it.farmerId == farmerId }
  }

  override suspend fun getReportById(reportId: String): Report? {
    return reports.find { it.id == reportId }
        ?: throw Exception("ReportRepositoryLocal: Report not found")
  }

  override suspend fun addReport(report: Report) {
    reports.add(report)
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    val index = reports.indexOfFirst { it.id == reportId }
    if (index != -1) {
      reports[index] = newReport
    } else {
      throw Exception("ReportRepositoryLocal: Report not found")
    }
  }

  override suspend fun deleteReport(reportId: String) {
    val index = reports.indexOfFirst { it.id == reportId }
    if (index != -1) {
      reports.removeAt(index)
    } else {
      throw Exception("ReportRepositoryLocal: Report not found")
    }
  }
}
