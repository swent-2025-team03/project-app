package com.android.agrihealth.model.report

import com.android.agrihealth.data.model.Location
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import com.android.agrihealth.data.repository.ReportRepositoryLocal
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows

//file taken and modified from: https://github.com/swent-epfl/bootcamp-25-B3-Solution/blob/main/app/src/test/java/com/github/se/bootcamp/model/todo/ToDosRepositoryLocalTest.kt

class ReportRepositoryLocalTest {
    private lateinit var reportRepositoryLocal: ReportRepositoryLocal

    private val repo =
        Report(
            id = "1",
            title = "Test Report",
            description = "This is a test report",
            photoUri = "http://example.com/photo.jpg",
            farmerId = "farmer123",
            vetId = "vet456",
            status = ReportStatus.PENDING,
            answer = "answerTest",
            location = Location(latitude = 10.0, longitude = 20.0, name = "Test Location"),
        )

    @Before
    fun setup() {
        //MockitoAnnotations.openMocks(this)

        reportRepositoryLocal = ReportRepositoryLocal()
    }

    /**
     * This test verifies that getNewUid generates a non-empty identifier, and that a second call to
     * getNewUid generates a different identifier.
     */
    @Test
    fun correctlyGeneratesNewUID() {
        val uid = reportRepositoryLocal.getNewReportId()
        assertTrue(uid.isNotEmpty()) // Ensure the UID is not empty

        val anotherUid = reportRepositoryLocal.getNewReportId()
        assertTrue(uid != anotherUid)
    }

    /**
     * This test verifies that addReport successfully adds a Report item to the local repository It also
     * tests that getAllReport and getReportById successfully retrieve the reports.
     */
    @Test
    fun addReport_succeeds() = runTest {
        reportRepositoryLocal.addReport(repo)

        // Verify that the Report was added
        val repos = reportRepositoryLocal.getAllReports("farmer123")
        assertTrue(repos.contains(repo)) // Ensure the report is present
        assertEquals(1, repos.size) // Ensure only one report is present

        val retrievedReport = reportRepositoryLocal.getReportById(repo.id)
        assertEquals(repo, retrievedReport)
    }

    /**
     * This test verifies that updateReport successfully updates an existing Report item in the local
     * repository, and calls the onSuccess callback. It also checks that the old Report item is no
     * longer present and the updated item is present with the correct updated values.
     */
    @Test
    fun editReport_succeeds() = runTest {
        reportRepositoryLocal.addReport(repo)

        val updatedReport = repo.copy(title = "Updated Report")

        reportRepositoryLocal.editReport(repo.id, updatedReport)

        // Verify that the Report was updated
        val repos = reportRepositoryLocal.getAllReports("farmer123")
        assertTrue(repos.contains(updatedReport)) // Ensure the updated repo is present
        assertTrue(!repos.contains(repo)) // Ensure the old repo is not present
        assertEquals(1, repos.size) // Ensure only one repo is present
    }

    /**
     * This test verifies that updateReport calls onFailure when trying to update a Report item that does
     * not exist in the local repository.
     */
    @Test
    fun updateReport_failsWhenReportNotFound() {
        assertThrows(Exception::class.java) {
            runTest { reportRepositoryLocal.editReport(repo.id, repo) }
        }
    }

    /**
     * This test verifies that deleteReport successfully removes a Report item from the local
     * repository, and calls the onSuccess callback.
     */
    @Test
    fun deleteReport_callsOnSuccess() = runTest {
        reportRepositoryLocal.addReport(repo)

        reportRepositoryLocal.deleteReport(repo.id)

        // Verify that the Report was deleted
        val repos = reportRepositoryLocal.getAllReports("farmer123")
        assertTrue(!repos.contains(repo)) // Ensure the report is not present
        assertEquals(0, repos.size) // Ensure no reports are present

        assertThrows(Exception::class.java) { runBlocking { reportRepositoryLocal.getReportById(repo.id) } }
    }

    @Test
    fun deleteReport_deletesTheCorrectReport() = runTest {
        val repo2 = repo.copy(id = "2", title = "Second Report")
        reportRepositoryLocal.addReport(repo)
        reportRepositoryLocal.addReport(repo2)

        reportRepositoryLocal.deleteReport(repo.id)

        // Verify that the correct Report was deleted
        val repos = reportRepositoryLocal.getAllReports("farmer123")
        assertTrue(!repos.contains(repo)) // Ensure the first repo is not present
        assertTrue(repos.contains(repo2)) // Ensure the second repo is still present
    }

    /**
     * This test verifies that deleteReport calls onFailure when trying to delete a Report item that
     * does not exist in the local repository.
     */
    @Test
    fun deleteReport_callsOnFailure_whenReportNotFound() {
        assertThrows(Exception::class.java) {
            runBlocking { reportRepositoryLocal.deleteReport("non-existent-id") }
        }
    }

    /**
     * This test verifies that getReportsByFarmer successfully retrieves all Report items associated with a
     * specific farmerId from the local repository.
     */
    @Test
    fun getReportsByFarmer_succeeds() = runTest {
        val repo2 = repo.copy(id = "2", farmerId = "farmer123")
        val repo3 = repo.copy(id = "3", farmerId = "farmer456")
        reportRepositoryLocal.addReport(repo)
        reportRepositoryLocal.addReport(repo2)
        reportRepositoryLocal.addReport(repo3)

        val farmerReports = reportRepositoryLocal.getReportsByFarmer("farmer123")
        assertEquals(2, farmerReports.size)
        assertTrue(farmerReports.contains(repo))
        assertTrue(farmerReports.contains(repo2))
        assertTrue(!farmerReports.contains(repo3))
    }
}