package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.location.Location
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

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
    val photoURL: String?,
    val questionForms: List<QuestionForm>,
    val farmerId: String,
    val officeId: String,
    val status: ReportStatus,
    val answer: String?,
    val location: Location?,
    val createdAt: Instant = Instant.now(), // Auto-set creation timestamp
    val startTime: LocalDateTime? = null,
    val duration: LocalTime? = null,
)

fun ReportStatus.displayString(): String =
    name
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .replace("_", " ")
