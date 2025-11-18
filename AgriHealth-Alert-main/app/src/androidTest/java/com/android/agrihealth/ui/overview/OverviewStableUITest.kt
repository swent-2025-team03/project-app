package com.android.agrihealth.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.testutil.FakeOverviewRepository
import com.android.agrihealth.ui.loading.LoadingTestTags
import com.android.agrihealth.ui.navigation.NavigationActions
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [OverviewScreen]. This test class verifies the presence of stable UI elements in the
 * Overview screen. It DOES NOT check the presence of dynamic data such as report items.
 */
class OverviewStableUITest {
  @get:Rule val composeTestRule = createComposeRule()

  // --- Helper functions to set up screens ---
  private fun setFarmerScreen() {
    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.FARMER,
          userId = "mock_farmer_id",
          overviewViewModel = OverviewViewModel(FakeOverviewRepository()))
    }
  }

  private fun setVetScreen() {
    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.VET,
          userId = "mock_vet_id",
          overviewViewModel = OverviewViewModel(FakeOverviewRepository()))
    }
  }

  // --- TEST 1: Verify the top app bar title is displayed ---
  @Test
  fun topAppBarTitle_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.TOP_APP_BAR_TITLE).assertIsDisplayed()
  }

  // --- TEST 2: Verify the logout button is displayed ---
  @Test
  fun logoutButton_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON).assertIsDisplayed()
  }

  // --- TEST 3: Verify "Create a new report" button is displayed for farmers ---
  @Test
  fun createReportButton_isDisplayedForFarmer() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).assertIsDisplayed()
  }

  // --- TEST 4: Verify "Create a new report" button is NOT displayed for vets ---
  @Test
  fun createReportButton_isNotDisplayedForVet() {
    setVetScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).assertDoesNotExist()
  }

  // --- TEST 5: Verify stable section header "Latest News / Alerts" is displayed ---
  @Test
  fun latestNewsSection_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("Latest News / Alerts").assertIsDisplayed()
  }

  // --- TEST 6: Verify the profile button is displayed ---
  @Test
  fun profileButton_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.PROFILE_BUTTON).assertIsDisplayed()
  }

  // --- TEST 7: Verify the dropdown
  @Test
  fun dropdownMenuWrapper_selectOption_callsOnOptionSelected() {
    var selectedOption: String? = null
    val options = listOf("Option 1", "Option 2")

    composeTestRule.setContent {
      DropdownMenuWrapper(
          options = options,
          selectedOption = null,
          onOptionSelected = { selectedOption = it },
          placeholder = "All")
    }
    composeTestRule.onNodeWithText("All").performClick()
    composeTestRule.onNodeWithText("Option 1").performClick()

    assertEquals("Option 1", selectedOption)
  }



  @Test
  fun overview_showsLoadingOverlayWhileLoadingReports_fakeRepo() {
    val sample =
      Report(
        id = "R1",
        title = "Coughing cow",
        description = "cough...",
        farmerId = "F1",
        vetId = "V1",
        status = ReportStatus.PENDING,
        questionForms = emptyList(),
        answer = null,
        photoUri = null,
        location = null,
      )

    val slowRepo = SlowFakeReportRepository(
      reports = listOf(sample),
      delayMs = 1200L,
    )

    val vm = OverviewViewModel(reportRepository = slowRepo)

    composeTestRule.setContent {
      val nav = rememberNavController()
      OverviewScreen(
        userRole = UserRole.FARMER,
        userId = "F1",
        overviewViewModel = vm,
        navigationActions = NavigationActions(nav),
      )
    }

    composeTestRule.waitUntil(2_000) { vm.uiState.value.isLoading }

    composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()

    composeTestRule.waitUntil(4_000) { !vm.uiState.value.isLoading }

    composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    composeTestRule.onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
  }




}



class SlowFakeReportRepository(
  private val reports: List<Report> = emptyList(),
  private val delayMs: Long = 1200
) : ReportRepository {

  override fun getNewReportId(): String = "slow-id"

  override suspend fun getAllReports(uid: String): List<Report> {
    kotlinx.coroutines.delay(delayMs)
    return reports
  }

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
    kotlinx.coroutines.delay(delayMs)
    return reports.filter { it.farmerId == farmerId }
  }

  override suspend fun getReportsByVet(vetId: String): List<Report> {
    kotlinx.coroutines.delay(delayMs)
    return reports.filter { it.vetId == vetId }
  }

  override suspend fun getReportById(id: String): Report? {
    kotlinx.coroutines.delay(delayMs)
    return reports.find { it.id == id }
  }

  override suspend fun addReport(report: Report) {}
  override suspend fun editReport(reportId: String, newReport: Report) {}
  override suspend fun deleteReport(reportId: String) {}
}