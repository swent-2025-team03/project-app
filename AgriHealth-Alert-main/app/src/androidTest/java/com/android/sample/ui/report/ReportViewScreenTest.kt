package com.android.sample.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.android.sample.data.model.ReportStatus
import com.android.sample.data.model.UserRole
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [ReportViewScreen]. These tests ensure that all interactive and display elements
 * behave as expected in both Farmer and Vet modes.
 */
class ReportViewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // --- Helper functions to set up screens ---
  private fun setVetScreen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val viewModel = ReportViewModel()
      ReportViewScreen(
          navController = navController, userRole = UserRole.VET, viewModel = viewModel)
    }
  }

  private fun setFarmerScreen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val viewModel = ReportViewModel()
      ReportViewScreen(
          navController = navController, userRole = UserRole.FARMER, viewModel = viewModel)
    }
  }

  // --- TEST 1: Vet typing in answer field ---
  @Test
  fun vet_canTypeInAnswerField() {
    setVetScreen()
    val answerNode = composeTestRule.onNodeWithTag("AnswerField")
    answerNode.performTextInput("This is my diagnosis.")
    answerNode.assertTextContains("This is my diagnosis.")
  }

  // --- TEST 2: Farmer sees read-only answer text ---
  @Test
  fun farmer_seesReadOnlyAnswerText() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("No answer yet.").assertIsDisplayed()
  }

  // --- TEST 3: Vet can open dropdown menu ---
  @Test
  fun vet_canOpenStatusDropdown() {
    setVetScreen()
    composeTestRule.onNodeWithTag("StatusDropdownField").performClick()
    composeTestRule.onNodeWithTag("StatusDropdownMenu").assertIsDisplayed()
  }

  // --- TEST 4: Vet can select a status ---
  @Test
  fun vet_canSelectResolvedStatus() {
    setVetScreen()
    composeTestRule.onNodeWithTag("StatusDropdownField").performClick()
    composeTestRule.onNodeWithTag("StatusOption_RESOLVED").performClick()
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("RESOLVED")
  }

  // --- TEST 5: Vet can open escalation dialog ---
  @Test
  fun vet_canOpenEscalationDialog() {
    setVetScreen()
    composeTestRule.onNodeWithTag("EscalateButton").performClick()
    composeTestRule.onNodeWithText("Confirm Escalation").assertIsDisplayed()
  }

  // --- TEST 6: Vet can cancel escalation dialog ---
  @Test
  fun vet_canCancelEscalationDialog() {
    setVetScreen()
    composeTestRule.onNodeWithTag("EscalateButton").performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithText("Confirm Escalation").assertDoesNotExist()
  }

  // --- TEST 7: Vet can confirm escalation ---
  @Test
  fun vet_canConfirmEscalation() {
    setVetScreen()
    composeTestRule.onNodeWithTag("EscalateButton").performClick()
    composeTestRule.onNodeWithText("Yes").performClick()
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("ESCALATED")
  }

  // --- TEST 8: Vet sees both bottom buttons ---
  @Test
  fun bottomButtons_areDisplayed() {
    setVetScreen()
    composeTestRule.onNodeWithText("View on Map").assertIsDisplayed()
    composeTestRule.onNodeWithText("Save").assertIsDisplayed()
  }

  // --- TEST 9: Dropdown should contain all expected statuses ---
  @Test
  fun dropdown_containsAllStatusOptions() {
    setVetScreen()
    composeTestRule.onNodeWithTag("StatusDropdownField").performClick()
    listOf("PENDING", "IN_PROGRESS", "RESOLVED").forEach {
      composeTestRule.onNodeWithTag("StatusOption_$it", useUnmergedTree = true).assertIsDisplayed()
    }
  }

  // --- TEST 10: Status color logic (indirectly tested) ---
  // We can’t directly check colors easily in Compose tests without semantics,
  // but this serves as a sanity check that status text changes.
  @Test
  fun statusTextReflectsViewModelChange() {
    val viewModel = ReportViewModel()
    composeTestRule.setContent {
      val navController = rememberNavController()
      ReportViewScreen(navController, UserRole.VET, viewModel)
    }
    composeTestRule.runOnUiThread { viewModel.onStatusChange(ReportStatus.RESOLVED) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("RESOLVED")
  }
}
