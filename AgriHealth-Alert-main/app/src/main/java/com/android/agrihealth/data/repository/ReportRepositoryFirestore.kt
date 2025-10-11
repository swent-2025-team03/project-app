package com.android.agrihealth.data.repository

import android.util.Log
import com.android.agrihealth.data.model.Location
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
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
    val title = doc.getString("title") ?: return null
    val description = doc.getString("description") ?: return null
    val photoUri = doc.getString("photoUri")
    val farmerId = doc.getString("farmerId") ?: return null
    val vetId = doc.getString("vetId")
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

    Report(
        id = id,
        title = title,
        description = description,
        photoUri = photoUri,
        farmerId = farmerId,
        vetId = vetId,
        status = status,
        answer = answer,
        location = location)
  } catch (e: Exception) {
    Log.e("ReportRepositoryFirestore", "Error converting document to Report", e)
    null
  }
}
