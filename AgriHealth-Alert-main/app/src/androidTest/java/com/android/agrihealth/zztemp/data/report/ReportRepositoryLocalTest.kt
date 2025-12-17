package com.android.agrihealth.zztemp.data.report

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ReportRepositoryLocalTest {
  private lateinit var repository: FakeReportRepository

  private val report =
    Report(
      id = "1",
      title = "Test Report",
      description = "This is a test report",
      questionForms = emptyList(),
      photoURL = null,
      farmerId = "farmer123",
      officeId = "off456",
      status = ReportStatus.PENDING,
      answer = "answerTest",
      location = Location(latitude = 10.0, longitude = 20.0, name = "Test Location"),
    )

  @Before
  fun setup() {
    repository = FakeReportRepository()
  }

  /**
   * This test verifies that getNewUid generates a non-empty identifier, and that a second call to
   * getNewUid generates a different identifier.
   */
  @Test
  fun correctlyGeneratesNewUID() {
    val uid = repository.getNewReportId()
    Assert.assertTrue(uid.isNotEmpty()) // Ensure the UID is not empty

    val anotherUid = repository.getNewReportId()
    Assert.assertTrue(uid != anotherUid)
  }

  /**
   * This test verifies that addReport successfully adds a Report item to the local repository It
   * also tests that getAllReport and getReportById successfully retrieve the reports.
   */
  @Test
  fun addReport_succeeds() = runTest {
    repository.addReport(report)

    // Verify that the Report was added
    val repos = repository.getAllReports("farmer123")
    Assert.assertTrue(repos.contains(report)) // Ensure the report is present
    Assert.assertEquals(1, repos.size) // Ensure only one report is present

    val retrievedReport = repository.getReportById(report.id)
    Assert.assertEquals(report, retrievedReport)
  }

  /**
   * This test verifies that updateReport successfully updates an existing Report item in the local
   * repository, and calls the onSuccess callback. It also checks that the old Report item is no
   * longer present and the updated item is present with the correct updated values.
   */
  @Test
  fun editReport_succeeds() = runTest {
    repository.addReport(report)

    val updatedReport = report.copy(title = "Updated Report")

    repository.editReport(report.id, updatedReport)

    // Verify that the Report was updated
    val repos = repository.getAllReports("farmer123")
    Assert.assertTrue(repos.contains(updatedReport)) // Ensure the updated repo is present
    Assert.assertFalse(repos.contains(report)) // Ensure the old repo is not present
    Assert.assertEquals(1, repos.size) // Ensure only one repo is present
  }

  /**
   * This test verifies that updateReport calls onFailure when trying to update a Report item that
   * does not exist in the local repository.
   */
  @Test
  fun updateReport_failsWhenReportNotFound() = runTest {
    var caughtException: Exception? = null
    try {
      repository.editReport(report.id, report)
    } catch (e: Exception) {
      caughtException = e
    }

    Assert.assertTrue(caughtException is NoSuchElementException)
  }

  /**
   * This test verifies that deleteReport successfully removes a Report item from the local
   * repository, and calls the onSuccess callback.
   */
  @Test
  fun deleteReport_callsOnSuccess() = runTest {
    repository.addReport(report)

    repository.deleteReport(report.id)

    // Verify that the Report was deleted
    val repos = repository.getAllReports("farmer123")
    Assert.assertFalse(repos.contains(report)) // Ensure the report is not present
    Assert.assertEquals(0, repos.size) // Ensure no reports are present

    var caughtException: Exception? = null
    try {
      repository.getReportById(report.id)
    } catch (e: Exception) {
      caughtException = e
    }

    Assert.assertTrue(caughtException is NoSuchElementException)
  }

  @Test
  fun deleteReport_deletesTheCorrectReport() = runTest {
    val repo2 = report.copy(id = "2", title = "Second Report")
    repository.addReport(report)
    repository.addReport(repo2)

    repository.deleteReport(report.id)

    // Verify that the correct Report was deleted
    val repos = repository.getAllReports("farmer123")
    Assert.assertFalse(repos.contains(report)) // Ensure the first repo is not present
    Assert.assertTrue(repos.contains(repo2)) // Ensure the second repo is still present
  }

  /**
   * This test verifies that deleteReport calls onFailure when trying to delete a Report item that
   * does not exist in the local repository.
   */
  @Test
  fun deleteReport_callsOnFailure_whenReportNotFound() = runTest {
    var caughtException: Exception? = null
    try {
      repository.deleteReport("non-existent-id")
    } catch (e: Exception) {
      caughtException = e
    }

    Assert.assertTrue(caughtException is NoSuchElementException)
  }

  /**
   * This test verifies that getReportsByFarmer successfully retrieves all Report items associated
   * with a specific farmerId from the local repository.
   */
  @Test
  fun getReportsByFarmer_succeeds() = runTest {
    val report2 = report.copy(id = "2", farmerId = "farmer123")
    val report3 = report.copy(id = "3", farmerId = "farmer456")
    repository.addReport(report)
    repository.addReport(report2)
    repository.addReport(report3)

    val farmerReports = repository.getAllReports("farmer123")
    Assert.assertEquals(2, farmerReports.size)
    Assert.assertTrue(farmerReports.contains(report))
    Assert.assertTrue(farmerReports.contains(report2))
    Assert.assertFalse(farmerReports.contains(report3))
  }
}