package com.android.agrihealth.data.model.alert

import com.android.agrihealth.data.model.helpers.runWithTimeout
import com.android.agrihealth.data.model.location.Location
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.time.LocalDate

private const val ALERTS_COLLECTION_PATH = "alerts"

class AlertRepositoryFirestore(private val db: FirebaseFirestore = Firebase.firestore) :
    AlertRepository {

  private var cachedAlerts: List<Alert> = emptyList()

  override suspend fun getAlerts(): List<Alert> {
    val snapshot = runWithTimeout(db.collection(ALERTS_COLLECTION_PATH).orderBy("createdAt").get())

    val alerts =
        snapshot.documents.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          alertFromFirestore(doc.id, data)
        }

    cachedAlerts = alerts
    return alerts
  }

  override suspend fun getAlertById(alertId: String): Alert? {
    return cachedAlerts.find { it.id == alertId }
        ?: run {
          val snapshot =
              runWithTimeout(db.collection(ALERTS_COLLECTION_PATH).document(alertId).get())

          if (!snapshot.exists()) return null
          alertFromFirestore(snapshot.id, snapshot.data!!)
        }
  }

  override fun getPreviousAlert(currentId: String): Alert? {
    val index = cachedAlerts.indexOfFirst { it.id == currentId }
    return if (index > 0) cachedAlerts[index - 1] else null
  }

  override fun getNextAlert(currentId: String): Alert? {
    val index = cachedAlerts.indexOfFirst { it.id == currentId }
    return if (index >= 0 && index < cachedAlerts.size - 1) cachedAlerts[index + 1] else null
  }

  @Suppress("UNCHECKED_CAST")
  fun alertFromFirestore(id: String, data: Map<String, Any>): Alert {
    val title = data["title"] as? String ?: throw Exception("Missing title")
    val description = data["description"] as? String ?: throw Exception("Missing description")

    val outbreakDateStr = data["outbreakDate"] as? String ?: throw Exception("Missing outbreakDate")

    val region = data["region"] as? String

    val zonesData = data["zones"] as? List<Map<String, Any>>
    val zones =
        zonesData?.map { zone ->
          val centerData = zone["center"] as? Map<*, *> ?: throw Exception("Missing zone center")

          AlertZone(
              center =
                  Location(
                      latitude = centerData["latitude"] as? Double ?: 0.0,
                      longitude = centerData["longitude"] as? Double ?: 0.0,
                      name = centerData["name"] as? String),
              radiusMeters = zone["radiusMeters"] as? Double ?: 0.0)
        }

    return Alert(
        id = id,
        title = title,
        description = description,
        outbreakDate = LocalDate.parse(outbreakDateStr),
        region = region,
        zones = zones)
  }

  fun mapFromAlert(alert: Alert): Map<String, Any?> {
    return mapOf(
        "title" to alert.title,
        "description" to alert.description,
        "region" to alert.region,
        "outbreakDate" to alert.outbreakDate.toString(),
        "createdAt" to com.google.firebase.Timestamp.now(),
        "zones" to
            alert.zones?.map { zone ->
              mapOf(
                  "center" to
                      mapOf(
                          "latitude" to zone.center.latitude,
                          "longitude" to zone.center.longitude,
                          "name" to zone.center.name),
                  "radiusMeters" to zone.radiusMeters)
            })
  }
}
