package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.location.Location

enum class ReportStatus {
  PENDING,
  IN_PROGRESS,
  RESOLVED,
  ESCALATED
}

data class Report(
    val id: String,
    val title: String,
    val description: String,
    val photoUri: String?, // For now, unused (will show placeholder)
    val farmerId: String,
    val vetId: String?,
    val status: ReportStatus,
    val answer: String?,
    val location: Location?
)
