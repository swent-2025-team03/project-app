package com.android.agrihealth.data.repository

import com.android.agrihealth.data.model.report.Report

/** Repository interface for managing reports. */
interface ReportRepository {

  /** Generate a unique ID for a new report. */
  fun getNewReportId(): String

  /**
   * Retrieves all reports concerning this userId from the repository.
   *
   * @param userId The ID of the user.
   * @return List of all reports.
   */
  suspend fun getAllReports(userId: String): List<Report>

  /**
   * Retrieves reports associated with a specific farmer.
   *
   * @param farmerId The ID of the farmer.
   * @return List of reports for the specified farmer.
   */
  suspend fun getReportsByFarmer(farmerId: String): List<Report>

  /**
   * Retrieves a report by its unique ID.
   *
   * @param reportId The ID of the report.
   * @return The report if found, null otherwise.
   */
  suspend fun getReportById(reportId: String): Report?

  /**
   * Adds a new report to the repository.
   *
   * @param report The report to be added.
   */
  suspend fun addReport(report: Report)

  /**
   * Edits an existing report in the repository.
   *
   * @param newReport The report with updated information.
   */
  suspend fun editReport(reportId: String, newReport: Report)

  /**
   * Deletes a report from the repository.
   *
   * @param reportId The ID of the report to be deleted.
   */
  suspend fun deleteReport(reportId: String)
}
