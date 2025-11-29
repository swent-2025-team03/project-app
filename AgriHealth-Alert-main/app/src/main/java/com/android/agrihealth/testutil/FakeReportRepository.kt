import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.report.ReportViewUIState

class FakeReportRepository(initialReports: List<Report> = emptyList()) : ReportRepository {
  // Used by ReportViewScreenTest to verify that a save occurred
  var editCalled = false
  var lastAddedReport: Report? = null

  private val reports = initialReports.toMutableList()
  private val sample = ReportViewUIState().report

  override fun getNewReportId(): String = "NEW_ID"

  override suspend fun getAllReports(userId: String): List<Report> =
      reports.filter { it.farmerId == userId }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> =
      reports.filter { it.farmerId == farmerId }

  override suspend fun getReportsByOffice(officeId: String): List<Report> =
      reports.filter { it.officeId == officeId }

  override suspend fun getReportById(reportId: String): Report? =
      reports.find { it.id == reportId } ?: sample.copy(id = reportId)

  override suspend fun addReport(report: Report) {
    // Store last added report for unit tests (AddReportViewModelTest)

    lastAddedReport = report
    reports.add(report)
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    editCalled = true
    val idx = reports.indexOfFirst { it.id == reportId }
    if (idx >= 0) reports[idx] = newReport
  }

  override suspend fun deleteReport(reportId: String) {
    reports.removeAll { it.id == reportId }
  }
}
