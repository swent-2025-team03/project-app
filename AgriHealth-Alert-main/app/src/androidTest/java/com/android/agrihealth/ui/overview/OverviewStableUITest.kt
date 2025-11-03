package com.android.agrihealth.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeOverviewRepository
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

  // --- TEST 7: Verify "Filter by Vet ID" is shown for farmers ---
  @Test
  fun vetIdFilter_isDisplayedForFarmer() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("Filter by Vet ID").assertIsDisplayed()
  }

  // --- TEST 8: Verify "Filter by Farmer ID" is shown for vets ---
  @Test
  fun farmerIdFilter_isDisplayedForVet() {
    setVetScreen()
    composeTestRule.onNodeWithText("Filter by Farmer ID").assertIsDisplayed()
  }

  // --- TEST 9: Verify "Filter by Status" is always shown ---
  @Test
  fun statusFilter_isDisplayedForFarmer() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("Filter by Status").assertIsDisplayed()
  }

  @Test
  fun statusFilter_isDisplayedForVet() {
    setVetScreen()
    composeTestRule.onNodeWithText("Filter by Status").assertIsDisplayed()
  }

  @Test
  fun dropdownMenuWrapper_selectOption_callsOnOptionSelected() {
    var selectedOption: String? = null
    val options = listOf("Option 1", "Option 2")

    composeTestRule.setContent {
      DropdownMenuWrapper(
          options = options, selectedOption = null, onOptionSelected = { selectedOption = it })
    }

    composeTestRule.onNodeWithText("All").performClick()

    composeTestRule.onNodeWithText("Option 1").performClick()

    assertEquals("Option 1", selectedOption)
  }
}
