package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.repository.ReportRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ReportRepositoryFirestoreTest : FirebaseEmulatorsTest() {

  val repository = ReportRepositoryFirestore(Firebase.firestore)

  val now: Instant = Instant.ofEpochSecond(1000000)

  val report1 =
      Report(
          id = "0",
          title = "report1",
          description = "description1",
          photoUri = null,
          farmerId = user1.uid,
          vetId = user3.uid,
          status = ReportStatus.PENDING,
          answer = null,
          location = null,
          createdAt = now)

  val report2 =
      Report(
          id = "1",
          title = "report2",
          description = "description2",
          photoUri = "url to the photo",
          farmerId = user2.uid,
          vetId = "Vet2",
          status = ReportStatus.RESOLVED,
          answer = "this is the answer",
          location = Location(42.0, 6.7, "the nice farm were all the dogs go when they are old"),
          createdAt = now)

  val report3 = report1.copy(id = "2", title = "report3", description = "description3")

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, password1, user1) }
    assertNotNull(Firebase.auth.currentUser)
  }

  @Test
  fun canAddReportToRepository() = runTest {
    repository.addReport(report1.copy(farmerId = user1.uid))
    val reports = repository.getReportsByFarmer(user1.uid)
    assertEquals(1, reports.size)
    assertEquals(report1, reports.first().copy(farmerId = report1.farmerId, createdAt = now))
  }

  @Test
  fun canAddMultipleReportToRepository() = runTest {
    repository.addReport(report1.copy(farmerId = user1.uid))
    repository.addReport(report3.copy(farmerId = user1.uid))

    val reports = repository.getReportsByFarmer(user1.uid)

    assertEquals(2, reports.size)
    assertEquals(
        listOf<Report>(report1, report3).sortedBy { it.title },
        reports.map { it.copy(farmerId = report1.farmerId, createdAt = now) }.sortedBy { it.title })
  }

  @Test
  fun getNewUidReturnsUniqueIDs() = runTest {
    val numberIDs = 100
    val uids = (0 until 100).toSet<Int>().map { repository.getNewReportId() }.toSet()
    assertEquals(uids.size, numberIDs)
  }

  @Test
  fun canGetReportsByFarmer() = runTest {
    repository.addReport(report1.copy(farmerId = user1.uid))
    repository.addReport(report2.copy(farmerId = user1.uid))
    repository.addReport(report3.copy(farmerId = user1.uid))

    assertEquals(
        3, repository.getReportsByFarmer(Firebase.auth.currentUser?.uid ?: "no current user").size)
  }

  @Test
  fun canGetReportsByVet() = runTest {
    authRepository.signOut()
    authRepository.signUpWithEmailAndPassword(user3.email, password3, user3)

    repository.addReport(report1.copy(farmerId = user3.uid, vetId = user3.uid))
    repository.addReport(report2.copy(farmerId = user3.uid))
    repository.addReport(report3.copy(farmerId = user3.uid, vetId = user3.uid))

    var reports = repository.getReportsByVet(user3.uid)
    assertEquals(2, reports.size)

    reports.forEach { assertEquals(user3.uid, it.vetId) }

    reports =
        reports.map { it.copy(farmerId = report1.farmerId, vetId = report1.vetId, createdAt = now) }
    val expectedReports = setOf(report1, report3)
    assertEquals(expectedReports, reports.toSet())

    authRepository.signOut()
  }

  @Test
  fun canGetReportById() = runTest {
    repository.addReport(report1.copy(farmerId = user1.uid))
    repository.addReport(report2.copy(farmerId = user1.uid))
    repository.addReport(report3.copy(farmerId = user1.uid))

    assertEquals(
        3, repository.getReportsByFarmer(Firebase.auth.currentUser?.uid ?: "no current user").size)
    val report = repository.getReportById(report1.id)
    assertEquals(report1, report?.copy(farmerId = report1.farmerId, createdAt = now))
  }

  @Test
  fun canEditReport() = runTest {
    val editedReport1 = report1.copy(description = "new description")
    repository.addReport(report1.copy(farmerId = user1.uid))
    repository.editReport(report1.id, editedReport1.copy(farmerId = user1.uid))

    val reports = repository.getReportsByFarmer(user1.uid)
    assertEquals(1, reports.size)
    assertEquals(
        editedReport1, reports.first().copy(farmerId = editedReport1.farmerId, createdAt = now))
  }

  @Test
  fun canDeleteReport() = runTest {
    repository.addReport(report1.copy(farmerId = user1.uid))

    repository.deleteReport(report1.id)
    val reports = repository.getReportsByFarmer(user1.uid)
    assertEquals(0, reports.size)
  }

  @Test
  fun deleteReportDeletesTheRightReport() = runTest {
    repository.addReport(report1.copy(farmerId = user1.uid))
    repository.addReport(report2.copy(farmerId = user1.uid))
    repository.addReport(report3.copy(farmerId = user1.uid))

    repository.deleteReport(report1.id)
    var reports = repository.getReportsByFarmer(user1.uid)
    assertEquals(2, reports.size)

    reports =
        reports.map {
          it.copy(
              farmerId = if (it.id == "1") report2.farmerId else report3.farmerId, createdAt = now)
        }
    val expectedReports = setOf(report2, report3)
    assertEquals(expectedReports, reports.toSet())
  }
}
