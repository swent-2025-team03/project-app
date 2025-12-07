package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.report.Report
import kotlinx.coroutines.delay

class SlowFakeReportRepository(
    reports: List<Report> = emptyList(),
    private val delayMs: Long = 1200L,
) : InMemoryReportRepository(reports) {

  override suspend fun addReport(report: Report) {
    delay(delayMs)
    super.addReport(report)
  }

  override suspend fun getReportById(id: String): Report? {
    delay(delayMs)
    return super.getReportById(id)
  }
}
