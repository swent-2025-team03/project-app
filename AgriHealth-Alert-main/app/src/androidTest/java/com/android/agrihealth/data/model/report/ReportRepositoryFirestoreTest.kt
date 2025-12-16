package com.android.agrihealth.data.model.report

import android.util.Log
import com.android.agrihealth.data.model.authentification.AuthRepositoryFirebase
import com.android.agrihealth.data.model.report.form.OpenQuestion
import com.android.agrihealth.testhelpers.TestPassword.password1
import com.android.agrihealth.testhelpers.TestPassword.password3
import com.android.agrihealth.testhelpers.TestReport
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.TestUser.farmer2
import com.android.agrihealth.testhelpers.TestUser.office1
import com.android.agrihealth.testhelpers.TestUser.office2
import com.android.agrihealth.testhelpers.TestUser.vet1
import com.android.agrihealth.testhelpers.templates.FirebaseTest
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

class ReportRepositoryFirestoreTest : FirebaseTest() {

  val repository = ReportRepositoryFirestore(Firebase.firestore)
  val authRepository = AuthRepositoryFirebase()

  val now: Instant = Instant.ofEpochSecond(1000000)

  val openQuestion = OpenQuestion("hello", "hi")

  val baseReport = TestReport.report1.copy(createdAt = now, questionForms = listOf(openQuestion))

  val currentUser = farmer1

  val baseReport1 = baseReport.copy(
    id = "rep1",
    title = "report 1",
    description = "desc 1",
    farmerId = currentUser.uid,
    officeId = office1.id,
    status = ReportStatus.PENDING,
    answer = null
  )

  val baseReport2 = baseReport.copy(
    id = "rep2",
    title = "report 2",
    description = "desc 2",
    farmerId = farmer2.uid,
    officeId = office2.id,
    status = ReportStatus.RESOLVED,
    answer = "answering"
  )

  val baseReport3 = baseReport.copy(
    id = "rep3",
    title = "report 3",
    description = "desc 3",
    farmerId = baseReport1.farmerId,
    officeId = baseReport1.officeId,
    status = baseReport1.status,
    answer = baseReport1.answer
  )

  var report1 = baseReport1
  var report2 = baseReport2
  var report3 = baseReport3

  private fun Report.fixUID(): Report = copy(farmerId = currentUser.uid)

  @Before
  fun setUp() {
    runTest { authRepository.signUpWithEmailAndPassword(currentUser.email, password1, currentUser) }
    assertNotNull(Firebase.auth.currentUser)
    val uuid = UUID.randomUUID()
    report1 = baseReport1.copy(id = "${baseReport1.id} $uuid")
    report2 = baseReport2.copy(id = "${baseReport2.id} $uuid")
    report3 = baseReport3.copy(id = "${baseReport3.id} $uuid")
  }

  @Test
  fun canAddReportToRepository() = runTest {
    repository.addReport(report1.fixUID())
    val reports = repository.getAllReports(currentUser.uid)
    assertEquals(1, reports.size)
    assertEquals(report1, reports.first().copy(farmerId = report1.farmerId, createdAt = now))
  }

  @Test
  fun canAddMultipleReportToRepository() = runTest {
    repository.addReport(report1.fixUID())
    repository.addReport(report3.fixUID())

    val reports = repository.getAllReports(currentUser.uid)

    assertEquals(2, reports.size)
    assertEquals(
        listOf(report1, report3).sortedBy { it.title },
        reports.map { it.copy(farmerId = report1.farmerId, createdAt = now) }.sortedBy { it.title })
  }

  @Test
  fun getNewUidReturnsUniqueIDs() = runTest {
    val numberIDs = 100
    val uids = (0 until 100).toSet().map { repository.getNewReportId() }.toSet()
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
    authRepository.signUpWithEmailAndPassword(vet1.email, password3, vet1)

    repository.addReport(report1.copy(farmerId = vet1.uid, officeId = vet1.officeId!!))
    repository.addReport(report2.copy(farmerId = vet1.uid))
    repository.addReport(report3.copy(farmerId = vet1.uid, officeId = vet1.officeId))

    var reports = repository.getAllReports(vet1.uid)
    assertEquals(2, reports.size)

    reports.forEach {
      assertEquals(vet1.officeId, it.officeId)
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

    val reports = repository.getAllReports(currentUser.uid)
    assertEquals(1, reports.size)
    assertEquals(
        editedReport1, reports.first().copy(farmerId = editedReport1.farmerId, createdAt = now))
  }

  @Test
  fun canDeleteReport() = runTest {
    repository.addReport(report1.fixUID())

    repository.deleteReport(report1.id)
    val reports = repository.getAllReports(currentUser.uid)
    assertEquals(0, reports.size)
  }

  @Test
  fun deleteReportDeletesTheRightReport() = runTest {
    repository.addReport(report1.fixUID())
    repository.addReport(report2.fixUID())
    repository.addReport(report3.fixUID())

    repository.deleteReport(report1.id)
    var reports = repository.getAllReports(currentUser.uid)
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
