package com.android.agrihealth.data.model.alert

import android.util.Log
import com.android.agrihealth.data.helper.runWithTimeout
import com.android.agrihealth.data.model.location.Location
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import kotlinx.coroutines.tasks.await

private const val ALERTS_COLLECTION_PATH = "alerts"

/**
 * Firestore-backed AlertRepository.
 *
 * Note:
 * - `getAlerts()` should be called to load the ordered alert list.
 * - `getPreviousAlert()` and `getNextAlert()` rely on the cached order.
 * - `getAlertById()` will update the cache if needed, but does not guarantee ordering.
 */
class AlertRepositoryFirestore(private val db: FirebaseFirestore = Firebase.firestore) :
    AlertRepository {

  private var cachedAlerts: List<Alert> = emptyList()

  override suspend fun getAlerts(): List<Alert> {
    val snapshot =
        try {
          runWithTimeout(
              db.collection(ALERTS_COLLECTION_PATH)
                  .orderBy("createdAt", Query.Direction.DESCENDING)
                  .get())
        } catch (_: Exception) {
          db.collection(ALERTS_COLLECTION_PATH)
              .orderBy("createdAt", Query.Direction.DESCENDING)
              .get(Source.CACHE)
              .await()
        }

    val alerts =
        snapshot.documents.mapNotNull { doc ->
          try {
            val data = doc.data ?: return@mapNotNull null
            alertFromFirestore(doc.id, data)
          } catch (e: Exception) {
            Log.e("AlertRepo", "Failed to parse alert ${doc.id}", e)
            null
          }
        }

    cachedAlerts = alerts
    return alerts
  }

  override suspend fun getAlertById(alertId: String): Alert? {

    val snapshot =
        try {
          runWithTimeout(db.collection(ALERTS_COLLECTION_PATH).document(alertId).get())
        } catch (_: Exception) {
          try {
            db.collection(ALERTS_COLLECTION_PATH).document(alertId).get(Source.CACHE).await()
          } catch (_: Exception) {
            return null
          }
        }
    if (!snapshot.exists()) return null

    val alert = alertFromFirestore(snapshot.id, snapshot.data!!)

    cachedAlerts = (cachedAlerts + alert).distinctBy { it.id }

    return alert
  }

  override fun getPreviousAlert(currentId: String): Alert? {
    val index = cachedAlerts.indexOfFirst { it.id == currentId }
    return if (index > 0) cachedAlerts[index - 1] else null
  }

  override fun getNextAlert(currentId: String): Alert? {
    val index = cachedAlerts.indexOfFirst { it.id == currentId }
    return if (index >= 0 && index < cachedAlerts.size - 1) cachedAlerts[index + 1] else null
  }

  fun alertFromFirestore(id: String, data: Map<String, Any>): Alert {
    val title = data["title"] as? String ?: throw Exception("Missing title")
    val description = data["description"] as? String ?: throw Exception("Missing description")

    val outbreakDateStr = data["outbreakDate"] as? String ?: throw Exception("Missing outbreakDate")

    val region = data["region"] as? String

    val zonesData = data["zones"] as? List<*>
    val zones =
        zonesData?.mapNotNull { zoneRaw ->
          val zone = zoneRaw as? Map<*, *> ?: return@mapNotNull null
          val centerData = zone["center"] as? Map<*, *> ?: return@mapNotNull null

          AlertZone(
              center =
                  Location(
                      latitude = (centerData["latitude"] as? Number)?.toDouble() ?: 0.0,
                      longitude = (centerData["longitude"] as? Number)?.toDouble() ?: 0.0,
                      name = centerData["name"] as? String),
              radiusMeters = (zone["radiusMeters"] as? Number)?.toDouble() ?: 0.0)
        }

    return Alert(
        id = id,
        title = title,
        description = description,
        outbreakDate = LocalDate.parse(outbreakDateStr),
        region = region,
        zones = zones)
  }
}
