package com.android.agrihealth.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testutil.FakeAlertRepository
import com.android.agrihealth.testutil.FakeOverviewViewModel
import com.android.agrihealth.testutil.InMemoryReportRepository
import com.android.agrihealth.testutil.TestConstants
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

  @Test
  fun overviewScreen_showsAndHidesLoadingOverlay_duringLoadAlerts() {
    val alertRepo = FakeAlertRepository(delayMs = 500)
    val reportRepo = InMemoryReportRepository(delayMs = 500)

    val viewModel = OverviewViewModel(reportRepository = reportRepo, alertRepository = alertRepo)

    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.FARMER,
          user = farmer,
          overviewViewModel = viewModel,
          onAddReport = {},
          onReportClick = {},
          onAlertClick = {})
    }

    composeTestRule.assertOverlayDuringLoading(
        isLoading = { viewModel.uiState.value.isLoading },
        timeoutStart = TestConstants.DEFAULT_TIMEOUT,
        timeoutEnd = TestConstants.DEFAULT_TIMEOUT)
  }

  // --- TEST 9: Verify Assignee Filter presence ---
  @Test
  fun assigneeFilterDropdown_isDisplayedAndSelectable() {
    setVetScreen()

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.FILTERS_TOGGLE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.FILTERS_TOGGLE).performClick()

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER).isDisplayed()
    }

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER).performClick()
    composeTestRule.onNodeWithText("Assigned to Me").performClick()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER)
        .assertTextEquals("Assigned to Me")

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER).performClick()
    composeTestRule.onNodeWithText("Unassigned").performClick()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER)
        .assertTextEquals("Unassigned")

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER).performClick()
    composeTestRule.onNodeWithText("Assigned to Others").performClick()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.ASSIGNEE_FILTER)
        .assertTextEquals("Assigned to Others")
  }
}
