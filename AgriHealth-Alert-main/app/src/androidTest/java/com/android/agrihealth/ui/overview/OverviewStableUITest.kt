package com.android.agrihealth.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.FakeAlertRepository
import com.android.agrihealth.testutil.FakeOverviewRepository
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [OverviewScreen]. This test class verifies the presence of stable UI elements in the
 * Overview screen. It DOES NOT check the presence of dynamic data such as report items.
 */
class OverviewStableUITest {
  @get:Rule val composeTestRule = createComposeRule()

  // --- Helper functions to set up screens ---

  @Before
  fun setup() {
    AlertRepositoryProvider.repository = FakeAlertRepository()
  }

  private val farmer =
      Farmer("mock_farmer_id", "john", "john", "john@john.john", null, defaultOffice = null)
  private val vet = Vet("mock_vet_id", "john", "john", "john@john.john", null)

  private fun setFarmerScreen() {
    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.FARMER,
          user = farmer,
          overviewViewModel = OverviewViewModel(FakeOverviewRepository()))
    }
  }

  private fun setVetScreen() {
    composeTestRule.setContent {
      OverviewScreen(
          userRole = UserRole.VET,
          user = vet,
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

  // --- TEST 8: Verify the first alert item is displayed ---
  @Test
  fun firstAlertItem_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.alertItemTag(0)).assertIsDisplayed()
  }
}
