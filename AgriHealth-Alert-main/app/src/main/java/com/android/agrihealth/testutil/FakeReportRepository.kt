import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository

class FakeReportRepository(initialReports: List<Report> = emptyList()) : ReportRepository {

  // Used by ReportViewScreenTest to verify that a save occurred
  var editCalled = false

  // Used by AddReportViewModelTest to verify the created report
  var lastAddedReport: Report? = null

  // Internal in-memory list of reports for tests
  private val reports = initialReports.toMutableList()

  override fun getNewReportId(): String = "NEW_ID"

  override suspend fun getAllReports(userId: String): List<Report> =
      reports.filter { it.farmerId == userId }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> =
      reports.filter { it.farmerId == farmerId }

  override suspend fun getReportsByOffice(officeId: String): List<Report> =
      reports.filter { it.officeId == officeId }

  override suspend fun getReportById(reportId: String): Report? = reports.find { it.id == reportId }

  override suspend fun addReport(report: Report) {
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
