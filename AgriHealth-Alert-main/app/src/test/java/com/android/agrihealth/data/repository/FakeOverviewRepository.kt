package com.android.agrihealth.data.repository

import com.android.agrihealth.data.model.Location
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus

class FakeOverviewRepository : ReportRepository {

    val mockReports = listOf(
        Report(
            id = "RPT001",
            title = "Cow coughing",
            description = "Coughing and nasal discharge observed",
            photoUri = null,
            farmerId = "FARMER_001",
            vetId = null,
            status = ReportStatus.IN_PROGRESS,
            answer = null,
            location = Location(46.5191, 6.5668, "Lausanne Farm")
        ),
        Report(
            id = "RPT002",
            title = "Sheep lost appetite",
            description = "One sheep has not eaten for two days.",
            photoUri = null,
            farmerId = "FARMER_001",
            vetId = "VET_001",
            status = ReportStatus.PENDING,
            answer = null,
            location = Location(46.5210, 6.5650, "Vaud Farm"))
    )

    var throwOnGet = false
    var throwOnAddReport1 = false
    var throwOnAddReport2 = false

    override suspend fun getAllReports(userId: String): List<Report> {
        if (throwOnGet) throw RuntimeException("Test error")
        return mockReports
    }

    override suspend fun addReport(report: Report) {
        if (report.id == "RPT001" && throwOnAddReport1) throw RuntimeException("Add report1 failed")
        if (report.id == "RPT002" && throwOnAddReport2) throw RuntimeException("Add report2 failed")
        // no-op for test
    }

    override suspend fun getReportById(id: String): Report? = null
    override suspend fun editReport(id: String, report: Report) {}
    override suspend fun getReportsByFarmer(farmerId: String): List<Report> = emptyList()
    override suspend fun deleteReport(reportId: String) {}
    override fun getNewReportId(): String = "FAKE_ID_OVERVIEW"
}
