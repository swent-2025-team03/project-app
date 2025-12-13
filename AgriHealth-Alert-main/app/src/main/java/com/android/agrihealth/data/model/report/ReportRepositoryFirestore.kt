package com.android.agrihealth.data.model.report

import android.util.Log
import com.android.agrihealth.data.model.user.UserRepositoryProvider
import com.android.agrihealth.data.model.location.locationFromMap
import com.android.agrihealth.data.model.report.form.MCQ
import com.android.agrihealth.data.model.report.form.MCQO
import com.android.agrihealth.data.model.report.form.OpenQuestion
import com.android.agrihealth.data.model.report.form.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import kotlin.collections.get

const val REPORTS_COLLECTION_PATH = "reports"

class ReportRepositoryFirestore(private val db: FirebaseFirestore) : ReportRepository {

  override fun getNewReportId(): String {
    return db.collection(REPORTS_COLLECTION_PATH).document().id
  }

  override suspend fun getAllReports(userId: String): List<Report> {

    val user = UserRepositoryProvider.repository.getUserFromId(userId).getOrNull()!!
    val filter =
        when (user) {
          is Vet -> Filter.equalTo("officeId", user.officeId)
          is Farmer -> Filter.equalTo("farmerId", userId)
        }

    val snapshot = db.collection(REPORTS_COLLECTION_PATH).where(filter).get().await()

    return snapshot.documents.mapNotNull { docToReport(it) }
  }

  override suspend fun getReportById(reportId: String): Report? {
    val doc = db.collection(REPORTS_COLLECTION_PATH).document(reportId).get().await()

    return docToReport(doc) ?: throw Exception("ReportRepositoryFirestore: Report not found")
  }

  override suspend fun addReport(report: Report) {

    db.collection(REPORTS_COLLECTION_PATH).document(report.id).set(report).await()
  }

  override suspend fun editReport(reportId: String, newReport: Report) {
    db.collection(REPORTS_COLLECTION_PATH).document(reportId).set(newReport).await()
  }

  override suspend fun deleteReport(reportId: String) {
    db.collection(REPORTS_COLLECTION_PATH).document(reportId).delete().await()
  }

  override suspend fun assignReportToVet(reportId: String, vetId: String) {
    db.collection(REPORTS_COLLECTION_PATH).document(reportId).update("assignedVet", vetId).await()
  }

  override suspend fun unassignReport(reportId: String) {
    db.collection(REPORTS_COLLECTION_PATH).document(reportId).update("assignedVet", null).await()
  }
}

/**
 * Converts a Firestore document to a Report object.
 *
 * @param doc The Firestore document.
 * @return The corresponding Report object, or null if conversion fails.
 */
private fun docToReport(doc: DocumentSnapshot): Report? {
  return try {
    val id = doc.id
    val title = doc.getString("title") ?: ""
    val description = doc.getString("description") ?: ""
    val questionFormsData = doc.get("questionForms") as? List<Map<*, *>> ?: emptyList()
    val questionForms =
        questionFormsData.mapNotNull { questionForm ->
          val questionType = questionForm["questionType"] as String
          val question = questionForm["question"] as String
          val answers = questionForm["answers"] as List<String>
          val answerIndex = (questionForm["answerIndex"] as Long).toInt()
          val userAnswer = questionForm["userAnswer"] as String
          when (questionType) {
            "OPEN" -> OpenQuestion(question = question, userAnswer = userAnswer)
            "YESORNO" -> YesOrNoQuestion(question = question, answerIndex = answerIndex)
            "MCQ" -> MCQ(question = question, answers = answers, answerIndex = answerIndex)
            "MCQO" ->
                MCQO(
                    question = question,
                    answers = answers,
                    answerIndex = answerIndex,
                    userAnswer = userAnswer)
            else -> null
          }
        }
    val photoURL = doc.getString("photoURL")
    val farmerId = doc.getString("farmerId") ?: return null
    val officeId = doc.getString("officeId") ?: return null
    val statusStr = doc.getString("status") ?: return null
    val status = ReportStatus.valueOf(statusStr)
    val answer = doc.getString("answer")
    val locationData = doc.get("location") as? Map<*, *>
    val location = locationFromMap(locationData)
    val createdAtData = doc.get("createdAt") as? Map<*, *>
    val createdAt =
        createdAtData?.let { Instant.ofEpochSecond(it["epochSecond"] as? Long ?: 0) }
            ?: Instant.now()
    val startTimeData = doc.get("startTime") as? Map<*, *>
    val startTime =
        startTimeData?.let {
          LocalDateTime.of(
              (it["year"] as? Long ?: 0).toInt(),
              (it["monthValue"] as? Long ?: 0).toInt(),
              (it["dayOfMonth"] as? Long ?: 0).toInt(),
              (it["hour"] as? Long ?: 0).toInt(),
              (it["minute"] as? Long ?: 0).toInt(),
              (it["second"] as? Long ?: 0).toInt())
        }
    val durationData = doc.get("duration") as? Map<*, *>
    val duration =
        durationData?.let {
          LocalTime.of(
              (it["hour"] as? Long ?: 0).toInt(),
              (it["minute"] as? Long ?: 0).toInt(),
              (it["second"] as? Long ?: 0).toInt())
        }
    val collected = doc.get("collected") as? Boolean ?: false
    val assignedVet = doc.getString("assignedVet")

    Report(
        id = id,
        title = title,
        description = description,
        photoURL = photoURL,
        questionForms = questionForms,
        farmerId = farmerId,
        officeId = officeId,
        status = status,
        answer = answer,
        location = location,
        createdAt = createdAt,
        startTime = startTime,
        duration = duration,
        collected = collected,
        assignedVet = assignedVet)
  } catch (e: Exception) {
    Log.e("ReportRepositoryFirestore", "Error converting document ${doc.id} to Report", e)
    null
  }
}
