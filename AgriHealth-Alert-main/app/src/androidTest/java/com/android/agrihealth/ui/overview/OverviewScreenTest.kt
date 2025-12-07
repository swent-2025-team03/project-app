package com.android.agrihealth.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.FakeAlertRepository
import com.android.agrihealth.testutil.InMemoryReportRepository
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverviewScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  // --- Helper functions to set up screens ---

  @Before
  fun setup() {
    AlertRepositoryProvider.repository = FakeAlertRepository()
  }

  private val farmer =
      Farmer(
          uid = "mock_farmer_id",
          firstname = "John",
          lastname = "Doe",
          email = "john@john.john",
          address = Location(latitude = 46.5191, longitude = 6.5668),
          defaultOffice = null)
  private val vet = Vet("mock_vet_id", "john", "john", "john@john.john", null)

  private fun setFarmerScreen() {
    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.FARMER,
          user = farmer,
          overviewViewModel = OverviewViewModel(InMemoryReportRepository()))
    }
  }

  private fun setVetScreen() {
    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.VET,
          user = vet,
          overviewViewModel = OverviewViewModel(InMemoryReportRepository()))
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
  /*
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

    val slowRepo =
        SlowFakeReportRepository(
            reports = listOf(sample),
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

    composeTestRule.waitUntil(TestConstants.SHORT_TIMEOUT) { vm.uiState.value.isLoading }

    composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) { !vm.uiState.value.isLoading }

    composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    composeTestRule.onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
  }

  // --- TEST 8: Verify that alerts are sorted correctly by proximity ---
  @Test
  fun firstAlert_isCloseForFarmer_flagCheck() {
    val fakeOverviewVM = FakeOverviewViewModel(user = farmer)
    val sortedAlerts = fakeOverviewVM.uiState.value.sortedAlerts

    setFarmerScreen()

    var inZoneFlag = true
    var changeCount = 0

    for (alertUiState in sortedAlerts) {
      val isInZone = alertUiState.distanceToAddress != null
      if (isInZone != inZoneFlag) {
        changeCount++
        assert(changeCount <= 1) { "Alert list is not properly sorted by proximity" }
        inZoneFlag = false
      }
    }
  }
  */

}
