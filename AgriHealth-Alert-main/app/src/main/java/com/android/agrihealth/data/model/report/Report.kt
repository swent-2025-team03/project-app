package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.location.Location
import java.time.Instant

enum class ReportStatus {
  PENDING,
  IN_PROGRESS,
  RESOLVED,
  SPAM
}

data class Report(
    val id: String,
    val title: String,
    val description: String,
    val photoUri: String?, // For now, unused (will show placeholder)
    val farmerId: String,
    val vetId: String,
    val status: ReportStatus,
    val answer: String?,
    val location: Location?,
    val createdAt: Instant = Instant.now() // Auto-set creation timestamp
)

fun ReportStatus.displayString(): String =
    name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")
