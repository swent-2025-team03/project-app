package com.android.agrihealth.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.navigation.NavigationActions
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
      val navigationActions = NavigationActions(navController)
      val viewModel = ReportViewModel()
      ReportViewScreen(
          navigationActions = navigationActions, userRole = UserRole.VET, viewModel = viewModel)
    }
  }

  private fun setFarmerScreen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      val viewModel = ReportViewModel()
      ReportViewScreen(
          navigationActions = navigationActions, userRole = UserRole.FARMER, viewModel = viewModel)
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

  // --- TEST 5: Vet can open spam dialog ---
  @Test
  fun vet_canOpenSpamDialog() {
    setVetScreen()
    composeTestRule.onNodeWithTag("SpamButton").performClick()
    composeTestRule.onNodeWithText("Report as SPAM?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
  }

  // --- TEST 6: Vet can cancel spam dialog ---
  @Test
  fun vet_canCancelSpamDialog() {
    setVetScreen()
    composeTestRule.onNodeWithTag("SpamButton").performClick()
    composeTestRule.onNodeWithText("Report as SPAM?").performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()
  }

  // --- TEST 7: Vet can confirm spam ---
  @Test
  fun vet_canConfirmSpam() {
    setVetScreen()
    composeTestRule.onNodeWithTag("SpamButton").performClick()
    composeTestRule.onNodeWithText("Confirm").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("SPAM")
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
  fun dropdown_containsCorrectStatusOptions() {
    setVetScreen()
    composeTestRule.onNodeWithTag("StatusDropdownField").performClick()
    listOf("IN_PROGRESS", "RESOLVED").forEach {
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
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(navigationActions = navigationActions, UserRole.VET, viewModel)
    }
    composeTestRule.runOnUiThread { viewModel.onStatusChange(ReportStatus.RESOLVED) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("RESOLVED")
  }

  // -------------------- Additional tests to increase coverage --------------------

  @Test
  fun vet_autoChangesPendingToInProgress_onLaunch() {
    // When a Vet screen is launched and the ViewModel's report status is PENDING (default),
    // the LaunchedEffect in the composable should auto-change it to IN_PROGRESS.
    val viewModel = ReportViewModel()
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(
          navigationActions = navigationActions, userRole = UserRole.VET, viewModel = viewModel)
    }
    // Wait for composition + LaunchedEffect to run
    composeTestRule.waitForIdle()
    // The status badge should show "IN PROGRESS" (name has underscore replaced by space)
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("IN PROGRESS")
  }

  @Test
  fun farmer_showsVetIdText() {
    // Farmer view shows the Vet ID line
    val viewModel = ReportViewModel()
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(
          navigationActions = navigationActions, userRole = UserRole.FARMER, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
    // Default sample report has vetId "VET_456" (from ReportViewUIState)
    composeTestRule.onNodeWithTag("roleInfoLine").assertTextContains("Vet ID: VET_456")
  }

  @Test
  fun vet_showsFarmerIdText() {
    // Vet view shows the Farmer ID line
    val viewModel = ReportViewModel()
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(
          navigationActions = navigationActions, userRole = UserRole.VET, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
    // Default sample report has farmerId "FARMER_123"
    composeTestRule.onNodeWithText("Farmer ID: FARMER_123").assertIsDisplayed()
  }

  @Test
  fun vet_canSelectInProgressStatus_viaDropdown() {
    // Test selecting IN_PROGRESS via dropdown (complements existing RESOLVED test)
    val viewModel = ReportViewModel()
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(
          navigationActions = navigationActions, userRole = UserRole.VET, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    // Open dropdown and pick IN_PROGRESS
    composeTestRule.onNodeWithTag("StatusDropdownField").performClick()
    composeTestRule.onNodeWithTag("StatusOption_IN_PROGRESS").performClick()

    // Ensure badge text updated
    composeTestRule.onNodeWithTag("StatusBadgeText").assertTextContains("IN PROGRESS")
  }
}
