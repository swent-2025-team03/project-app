import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.report.ReportViewUIState

class FakeReportRepository(initialReports: List<Report> = emptyList()) : ReportRepository {

  // Used by ReportViewScreenTest to verify that a save occurred
  var editCalled = false

  // Used by AddReportViewModelTest to verify the created report
  var lastAddedReport: Report? = null

  // Internal in-memory list of reports for tests
  private val reports = initialReports.toMutableList()

  // Sample used in some tests — still present if needed, but no longer used to fake missing reports
  private val sample = ReportViewUIState().report

  override fun getNewReportId(): String = "NEW_ID"

  override suspend fun getAllReports(userId: String): List<Report> =
      reports.filter { it.farmerId == userId }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> =
      reports.filter { it.farmerId == farmerId }

  override suspend fun getReportsByOffice(officeId: String): List<Report> =
      reports.filter { it.officeId == officeId }

  override suspend fun getReportById(reportId: String): Report? =
      // IMPORTANT FIX:
      // Previously: returned "sample.copy(id)" which created FAKE reports that never existed.
      // This produced false positives and masked real bugs.
      // Now: behaves like the real repo → returns null if it doesn't exist.
      reports.find { it.id == reportId }

  override suspend fun addReport(report: Report) {
    // Store last added report for unit tests (AddReportViewModelTest)
    lastAddedReport = report
    reports.add(report)
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    // Mark that editReport was triggered (ReportViewScreenTest)
    editCalled = true

    // Replace the report in the list (realistic behaviour)
    val idx = reports.indexOfFirst { it.id == reportId }
    if (idx >= 0) reports[idx] = newReport
  }

  override suspend fun deleteReport(reportId: String) {
    // Remove report by ID (useful for future tests)
    reports.removeAll { it.id == reportId }
  }
}
