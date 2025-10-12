package com.android.agrihealth.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import com.android.agrihealth.data.model.UserRole
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [OverviewScreen]. These tests verify that key elements are displayed correctly and
 * behave as expected for different user roles.
 */
class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // --- Dummy data for testing ---
  private val dummyReports =
      listOf(
          Report(
              id = "1",
              title = "Cow coughing",
              description = "Coughing and nasal discharge observed",
              photoUri = null,
              farmerId = "farmer_001",
              vetId = null,
              status = ReportStatus.IN_PROGRESS,
              answer = null,
              location = null),
          Report(
              id = "2",
              title = "Sheep limping",
              description = "Limping observed in the rear leg; mild swelling noted",
              photoUri = null,
              farmerId = "farmer_002",
              vetId = null,
              status = ReportStatus.PENDING,
              answer = null,
              location = null))

  // --- Helper functions to set up screens ---
  private fun setFarmerScreen() {
    composeTestRule.setContent {
      OverviewScreen(userRole = UserRole.FARMER, reports = dummyReports)
    }
  }

  private fun setVetScreen() {
    composeTestRule.setContent { OverviewScreen(userRole = UserRole.VET, reports = dummyReports) }
  }

  // --- TEST 1: Farmer sees "Create a new report" button ---
  @Test
  fun farmer_seesAddReportButton() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).assertIsDisplayed()
  }

  // --- TEST 2: Vet does not see "Create a new report" button ---
  @Test
  fun vet_doesNotSeeAddReportButton() {
    setVetScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).assertDoesNotExist()
  }

  // --- TEST 3: Logout button is visible ---
  @Test
  fun logoutButton_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON).assertIsDisplayed()
  }

  // --- TEST 4: Past reports list is rendered ---
  @Test
  fun pastReports_areDisplayed() {
    setFarmerScreen()
    dummyReports.forEach { composeTestRule.onNodeWithText(it.title).assertIsDisplayed() }
  }

  // --- TEST 5: Status tags are rendered correctly ---
  @Test
  fun statusTags_areDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("IN PROGRESS").assertIsDisplayed()
    composeTestRule.onNodeWithText("PENDING").assertIsDisplayed()
  }

  // --- TEST 6: Latest alert section is visible ---
  @Test
  fun latestAlert_isDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("Influenza Detected").assertIsDisplayed()
    composeTestRule.onNodeWithText("Outbreak: 08/10/2025").assertIsDisplayed()
  }
}
