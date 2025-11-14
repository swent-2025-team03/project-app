package com.android.agrihealth.data.repository

import android.util.Log
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.QuestionType
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import kotlinx.coroutines.tasks.await

const val REPORTS_COLLECTION_PATH = "reports"

class ReportRepositoryFirestore(private val db: FirebaseFirestore) : ReportRepository {

  override fun getNewReportId(): String {
    return db.collection(REPORTS_COLLECTION_PATH).document().id
  }

  override suspend fun getAllReports(userId: String): List<Report> {

    // Build an OR filter: (vetId == userId) OR (farmerId == userId)
    val filter = Filter.or(Filter.equalTo("vetId", userId), Filter.equalTo("farmerId", userId))

    val snapshot = db.collection(REPORTS_COLLECTION_PATH).where(filter).get().await()

    return snapshot.documents.mapNotNull { docToReport(it) }
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    val snapshot =
        db.collection(REPORTS_COLLECTION_PATH).whereEqualTo("farmerId", farmerId).get().await()

    return snapshot.documents.mapNotNull { docToReport(it) }
  }

  override suspend fun getReportsByVet(vetId: String): List<Report> {
    val snapshot = db.collection(REPORTS_COLLECTION_PATH).whereEqualTo("vetId", vetId).get().await()

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
    val questionFormsData = doc.get("questionForms") as? List<Map<String, Any>> ?: emptyList()
    val questionForms =
        questionFormsData.map { questionForm ->
          val questionType = questionForm["questionType"] as QuestionType
          val question = questionForm["question"] as String
          val answers = questionForm["answers"] as List<String>
          val answerIndex = questionForm["answerIndex"] as Int
          val userAnswer = questionForm["userAnswer"] as String
          when (questionType) {
            QuestionType.OPEN -> OpenQuestion(question = question, userAnswer = userAnswer)
            QuestionType.YESORNO -> YesOrNoQuestion(question = question, answerIndex = answerIndex)
            QuestionType.MCQ ->
                MCQ(question = question, answers = answers, answerIndex = answerIndex)
            QuestionType.MCQO ->
                MCQO(
                    question = question,
                    answers = answers,
                    answerIndex = answerIndex,
                    userAnswer = userAnswer)
          }
        }
    val photoUri = doc.getString("photoUri")
    val farmerId = doc.getString("farmerId") ?: return null
    val vetId = doc.getString("vetId") ?: return null
    val statusStr = doc.getString("status") ?: return null
    val status = ReportStatus.valueOf(statusStr)
    val answer = doc.getString("answer")
    val locationData = doc.get("location") as? Map<*, *>
    val location =
        locationData?.let {
          Location(
              latitude = it["latitude"] as? Double ?: 0.0,
              longitude = it["longitude"] as? Double ?: 0.0,
              name = it["name"] as? String ?: "")
        }
    val createdAtData = doc.get("createdAt") as? Map<*, *>
    val createdAt =
        createdAtData?.let { Instant.ofEpochSecond(it["epochSecond"] as? Long ?: 0) }
            ?: Instant.now()

    Report(
        id = id,
        questionForms = questionForms,
        photoUri = photoUri,
        farmerId = farmerId,
        vetId = vetId,
        status = status,
        answer = answer,
        location = location,
        createdAt = createdAt)
  } catch (e: Exception) {
    Log.e("ReportRepositoryFirestore", "Error converting document ${doc.id} to Report", e)
    null
  }
}
