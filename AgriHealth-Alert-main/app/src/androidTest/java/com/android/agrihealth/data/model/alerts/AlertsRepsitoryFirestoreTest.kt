package com.android.agrihealth.data.model.alert

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AlertRepositoryFirestoreTest : FirebaseEmulatorsTest() {

  private lateinit var db: FirebaseFirestore
  private lateinit var repo: AlertRepositoryFirestore

  @Before
  override fun setUp() {
    super.setUp()
    db = FirebaseFirestore.getInstance()
    runBlocking {
      db.terminate().await()
      db.clearPersistence().await()
    }
    db = FirebaseFirestore.getInstance()
    repo = AlertRepositoryFirestore(db)
  }

  private suspend fun insertAlert(
      id: String,
      title: String = "Alert $id",
      createdAt: Timestamp,
      outbreakDate: String = "2024-01-01",
      region: String? = "Vaud"
  ) {
    db.collection("alerts")
        .document(id)
        .set(
            mapOf(
                "title" to title,
                "description" to "Description $id",
                "createdAt" to createdAt,
                "outbreakDate" to outbreakDate,
                "region" to region,
                "zones" to
                    listOf(
                        mapOf(
                            "center" to
                                mapOf(
                                    "latitude" to 46.5, "longitude" to 6.6, "name" to "Center $id"),
                            "radiusMeters" to 5000))))
        .await()
  }

  @Test
  fun getAlerts_returnsOrderedByCreatedAtDescending() = runBlocking {
    insertAlert("a1", createdAt = Timestamp(10, 0))
    insertAlert("a2", createdAt = Timestamp(20, 0))
    insertAlert("a3", createdAt = Timestamp(30, 0))

    val alerts = repo.getAlerts()

    assertEquals(listOf("a3", "a2", "a1"), alerts.map { it.id })
  }

  @Test
  fun getAlertById_returnsFromCacheWhenAvailable() = runBlocking {
    insertAlert("cached", createdAt = Timestamp.now())

    val all = repo.getAlerts()
    assertEquals(1, all.size)

    val fromCache = repo.getAlertById("cached")
    assertNotNull(fromCache)
    assertEquals("cached", fromCache!!.id)
  }

  @Test
  fun getAlertById_fetchesFromFirestoreWhenNotCached() = runBlocking {
    insertAlert("remote", createdAt = Timestamp.now())

    val alert = repo.getAlertById("remote")

    assertNotNull(alert)
    assertEquals("remote", alert!!.id)
    assertEquals(LocalDate.parse("2024-01-01"), alert.outbreakDate)
  }

  @Test
  fun getAlertById_returnsNullWhenNotFound() = runTest {
    val alert = repo.getAlertById("does-not-exist")
    assertNull(alert)
  }

  @Test
  fun previousAndNextAlert_workBasedOnCachedOrder() = runBlocking {
    insertAlert("a1", createdAt = Timestamp(10, 0))
    insertAlert("a2", createdAt = Timestamp(20, 0))
    insertAlert("a3", createdAt = Timestamp(30, 0))

    val alerts = repo.getAlerts()
    assertEquals(listOf("a3", "a2", "a1"), alerts.map { it.id })

    assertEquals("a2", repo.getNextAlert("a3")!!.id)
    assertEquals("a1", repo.getNextAlert("a2")!!.id)
    assertNull(repo.getNextAlert("a1"))

    assertEquals("a2", repo.getPreviousAlert("a1")!!.id)
    assertEquals("a3", repo.getPreviousAlert("a2")!!.id)
    assertNull(repo.getPreviousAlert("a3"))
  }

  @Test
  fun alertFromFirestore_parsesZonesCorrectly() = runBlocking {
    insertAlert("zones", createdAt = Timestamp.now())

    val alert = repo.getAlertById("zones")!!
    assertNotNull(alert.zones)
    assertEquals(1, alert.zones!!.size)

    val zone = alert.zones.first()
    assertEquals(5000.0, zone.radiusMeters, 0.0)

    val center: Location = zone.center
    assertEquals(46.5, center.latitude, 0.0)
    assertEquals(6.6, center.longitude, 0.0)
    assertEquals("Center zones", center.name)
  }

  @Test
  fun getAlerts_skipsInvalidDocuments() = runTest {
    // Missing title → should be skipped
    db.collection("alerts")
        .document("bad")
        .set(
            mapOf(
                "description" to "oops",
                "createdAt" to Timestamp.now(),
                "outbreakDate" to "2024-01-01"))
        .await()

    insertAlert("good", createdAt = Timestamp.now())

    val alerts = repo.getAlerts()

    assertEquals(1, alerts.size)
    assertEquals("good", alerts.first().id)
  }
}
