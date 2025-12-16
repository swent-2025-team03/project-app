package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.form.QuestionForm
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

/** Report to be filled by farmers and submitted to vets to answer */
data class Report(
    val id: String,
    val title: String,
    val description: String,
    val questionForms: List<QuestionForm>,
    val photoURL: String?,
    val farmerId: String,
    val officeId: String,
    val status: ReportStatus,
    val answer: String?,
    val location: Location?,
    val createdAt: Instant = Instant.now(),
    val startTime: LocalDateTime? = null,
    val duration: LocalTime? = null,
    val collected: Boolean = false,
    val assignedVet: String? = null
)

/** Enum with every status a report can have */
enum class ReportStatus {
  PENDING,
  IN_PROGRESS,
  RESOLVED,
  SPAM
}

/** Displays a report status in a nice way for users to read */
fun ReportStatus.displayString(): String =
    name
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .replace("_", " ")
