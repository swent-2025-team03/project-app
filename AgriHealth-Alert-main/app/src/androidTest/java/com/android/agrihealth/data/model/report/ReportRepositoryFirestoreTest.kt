package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.repository.ReportRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ReportRepositoryFirestoreTest : FirebaseEmulatorsTest() {

  val repository = ReportRepositoryFirestore(Firebase.firestore)

  val now: Instant = Instant.ofEpochSecond(1000000)

  val openQuestion = OpenQuestion("hello", "hi")

  val baseReport1 =
      Report(
          id = "0",
          title = "report1",
          description = "description1",
          photoURL = null,
          questionForms = listOf(openQuestion),
          farmerId = user1.uid,
          officeId = user3.uid,
          status = ReportStatus.PENDING,
          answer = null,
          location = null,
          createdAt = now)

  val baseReport2 =
      Report(
          id = "1",
          title = "report2",
          description = "description2",
          photoURL = null,
          questionForms = listOf(openQuestion),
          farmerId = user2.uid,
          officeId = "Off2",
          status = ReportStatus.RESOLVED,
          answer = "this is the answer",
          location = Location(42.0, 6.7, "the nice farm were all the dogs go when they are old"),
          createdAt = now)

  val baseReport3 = baseReport1.copy(id = "2", title = "report3", description = "description3")

  var report1 = baseReport1
  var report2 = baseReport2
  var report3 = baseReport3

  private fun Report.fixUID(): Report = copy(farmerId = user1.uid)

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, password1, user1) }
    assertNotNull(Firebase.auth.currentUser)
    val uuid = UUID.randomUUID()
    report1 = baseReport1.copy(id = "${baseReport1.id} $uuid")
    report2 = baseReport2.copy(id = "${baseReport2.id} $uuid")
    report3 = baseReport3.copy(id = "${baseReport3.id} $uuid")
  }

  @Test
  fun canAddReportToRepository() = runTest {
    repository.addReport(report1.fixUID())
    val reports = repository.getAllReports(user1.uid)
    assertEquals(1, reports.size)
    assertEquals(report1, reports.first().copy(farmerId = report1.farmerId, createdAt = now))
  }

  @Test
  fun canAddMultipleReportToRepository() = runTest {
    repository.addReport(report1.fixUID())
    repository.addReport(report3.fixUID())

    val reports = repository.getAllReports(user1.uid)

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
    repository.addReport(report1.fixUID())
    repository.addReport(report2.fixUID())
    repository.addReport(report3.fixUID())

    assertEquals(
        3, repository.getAllReports(Firebase.auth.currentUser?.uid ?: "no current user").size)
  }

  @Test
  fun canGetReportsByVet() = runTest {
    authRepository.signOut()
    authRepository.signUpWithEmailAndPassword(user3.email, password3, user3)

    repository.addReport(report1.copy(farmerId = user3.uid, officeId = user3.officeId!!))
    repository.addReport(report2.copy(farmerId = user3.uid))
    repository.addReport(report3.copy(farmerId = user3.uid, officeId = user3.officeId))

    var reports = repository.getAllReports(user3.uid)
    assertEquals(2, reports.size)

    reports.forEach {
      assertEquals(user3.officeId, it.officeId)
      assertEquals(listOf(openQuestion), it.questionForms)
    }

    reports =
        reports.map {
          it.copy(
              farmerId = report1.farmerId,
              officeId = report1.officeId,
              createdAt = now,
              questionForms = listOf(openQuestion))
        }
    val expectedReports = setOf(report1, report3)
    assertEquals(expectedReports, reports.toSet())

    authRepository.signOut()
  }

  @Test
  fun canGetReportById() = runTest {
    repository.addReport(report1.fixUID())
    repository.addReport(report2.fixUID())
    repository.addReport(report3.fixUID())

    assertEquals(
        3, repository.getAllReports(Firebase.auth.currentUser?.uid ?: "no current user").size)
    val report = repository.getReportById(report1.id)
    assertEquals(report1, report?.copy(farmerId = report1.farmerId, createdAt = now))
  }

  @Test
  fun canEditReport() = runTest {
    val editedReport1 = report1.copy(description = "new description")
    repository.addReport(report1.fixUID())
    repository.editReport(report1.id, editedReport1.fixUID())

    val reports = repository.getAllReports(user1.uid)
    assertEquals(1, reports.size)
    assertEquals(
        editedReport1, reports.first().copy(farmerId = editedReport1.farmerId, createdAt = now))
  }

  @Test
  fun canDeleteReport() = runTest {
    repository.addReport(report1.fixUID())

    repository.deleteReport(report1.id)
    val reports = repository.getAllReports(user1.uid)
    assertEquals(0, reports.size)
  }

  @Test
  fun deleteReportDeletesTheRightReport() = runTest {
    repository.addReport(report1.fixUID())
    repository.addReport(report2.fixUID())
    repository.addReport(report3.fixUID())

    repository.deleteReport(report1.id)
    var reports = repository.getAllReports(user1.uid)
    assertEquals(2, reports.size)

    reports =
        reports.map {
          it.copy(
              farmerId = if (it.id == report2.id) report2.farmerId else report3.farmerId,
              createdAt = now,
              questionForms = listOf(openQuestion))
        }
    val expectedReports = setOf(report2, report3)
    assertEquals(expectedReports, reports.toSet())
  }
}
