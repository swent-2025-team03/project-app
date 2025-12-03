package com.android.agrihealth.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.FakeOverviewViewModel
import com.android.agrihealth.testutil.FakeReportRepository
import com.android.agrihealth.testutil.TestConstants.LONG_TIMEOUT
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreen
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [ReportViewScreen]. These tests ensure that all interactive and display elements
 * behave as expected in both Farmer and Vet modes.
 */
class ReportViewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  /** Sets up the ReportViewScreen for a given role (Vet or Farmer). */
  private fun setReportViewScreen(
      role: UserRole,
      viewModel: ReportViewViewModel = ReportViewViewModel(FakeReportRepository()),
      user: User? = null
  ) {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(
          navigationActions = navigationActions,
          userRole = role,
          viewModel = viewModel,
          user = user)
    }
  }

  // --- Role-specific helpers (wrappers) ---
  private fun setVetScreen(
      viewModel: ReportViewViewModel = ReportViewViewModel(FakeReportRepository())
  ) =
      setReportViewScreen(
          role = UserRole.VET,
          viewModel,
          user =
              Vet(
                  uid = "vet_id",
                  firstname = "alice",
                  lastname = "alice",
                  email = "mail@mail",
                  address = null,
                  officeId = "OFF_456"))

  private fun setValidVetScreen(
      viewModel: ReportViewViewModel = ReportViewViewModel(FakeReportRepository())
  ) =
      setReportViewScreen(
          role = UserRole.VET,
          viewModel,
          user =
              Vet(
                  uid = "valid_vet_id",
                  firstname = "alice",
                  lastname = "alice",
                  email = "mail@mail",
                  address = null,
                  officeId = "OFF_456"))

  private fun setFarmerScreen(
      viewModel: ReportViewViewModel = ReportViewViewModel(FakeReportRepository())
  ) =
      setReportViewScreen(
          role = UserRole.FARMER,
          viewModel,
          user =
              Farmer(
                  uid = "farmer_id",
                  firstname = "bob",
                  lastname = "bob",
                  email = "mail@mail",
                  address = null,
                  defaultOffice = "OFF_456"))

  // --- TEST 1: Vet typing in answer field ---
  @Test
  fun vet_canTypeInAnswerField() {
    setValidVetScreen()
    val answerNode = composeTestRule.onNodeWithTag(ReportViewScreenTestTags.ANSWER_FIELD)
    answerNode.performTextInput("This is my diagnosis.")
    answerNode.assertTextContains("This is my diagnosis.")
  }

  // --- TEST 2: Farmer sees read-only answer text---
  @Test
  fun farmer_seesReadOnlyAnswerText() {
    setFarmerScreen()
    composeTestRule.onNodeWithText("No answer yet.").assertIsDisplayed()
  }

  // --- TEST 3: Vet can open dropdown menu ---
  @Test
  fun vet_canOpenStatusDropdown() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_FIELD).performClick()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_MENU).assertIsDisplayed()
  }

  // --- TEST 4: Vet can select a status ---
  @Test
  fun vet_canSelectResolvedStatus() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.getTagForStatusOption("RESOLVED"))
        .performClick()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("RESOLVED")
  }

  // --- TEST 5: Vet can open spam dialog ---
  @Test
  fun vet_canOpenSpamDialog() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SPAM_BUTTON).performClick()
    composeTestRule.onNodeWithText("Report as spam?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
  }

  // --- TEST 6: Vet can cancel spam dialog ---
  @Test
  fun vet_canCancelSpamDialog() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SPAM_BUTTON).performClick()
    composeTestRule.onNodeWithText("Report as spam?").performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()
  }

  // --- TEST 7: Vet can confirm spam ---
  @Test
  fun vet_canConfirmSpam() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SPAM_BUTTON).performClick()
    composeTestRule.onNodeWithText("Confirm").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("SPAM")
  }

  // --- TEST 8: Vet sees both bottom buttons ---
  @Test
  fun bottomButtons_areDisplayed() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.VIEW_ON_MAP).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  // --- TEST 9: Dropdown should contain all expected statuses ---
  @Test
  fun dropdown_containsCorrectStatusOptions() {
    setValidVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_FIELD).performClick()
    listOf("IN_PROGRESS", "RESOLVED").forEach {
      composeTestRule
          .onNodeWithTag(ReportViewScreenTestTags.getTagForStatusOption(it), useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  // --- TEST 10: Status color logic (indirectly tested) ---
  // We can’t directly check colors easily in Compose tests without semantics,
  // but this serves as a sanity check that status text changes.
  @Test
  fun statusTextReflectsViewModelChange() {
    val viewModel = ReportViewViewModel()
    setVetScreen(viewModel)
    composeTestRule.runOnUiThread { viewModel.onStatusChange(ReportStatus.RESOLVED) }
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("RESOLVED")
  }

  // --- TEST 11: Farmer can open Delete dialog ---
  @Test
  fun farmer_canOpenDeleteDialog() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.DELETE_REPORT_ALERT_BOX)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  // --- TEST 12: Farmer can cancel Delete dialog ---
  @Test
  fun farmer_canCancelDeleteDialog() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.DELETE_REPORT_ALERT_BOX)
        .assertDoesNotExist()
  }

  // --- TEST 13: Farmer can confirm delete ---
  @Test
  fun farmer_canConfirmDelete() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.onNodeWithText("Confirm").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.DELETE_REPORT_ALERT_BOX)
        .assertDoesNotExist()
  }

  // --- TEST 14: Farmer sees both bottom buttons ---
  @Test
  fun farmer_bottomButtons_areDisplayed() {
    setFarmerScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.VIEW_ON_MAP).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.DELETE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun vet_canClaimAndUnassignReport() {
    setValidVetScreen()

    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.UNASSIGN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.UNASSIGN_BUTTON).performClick()
  }

  // -------------------- Additional tests to increase coverage --------------------

  @Test
  fun vet_autoChangesPendingToInProgress_onLaunch() {
    // When a Vet screen is launched and the ViewModel's report status is PENDING (default),
    // the LaunchedEffect in the composable should auto-change it to IN_PROGRESS.
    val viewModel = ReportViewViewModel()
    setVetScreen(viewModel)
    // Wait for composition + LaunchedEffect to run
    composeTestRule.waitForIdle()
    // The status badge should show "IN PROGRESS" (name has underscore replaced by space)
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("IN PROGRESS")
  }

  @Test
  fun farmer_roleInfoLine_showsOfficeName() {
    setFarmerScreen()
    composeTestRule.waitUntil(LONG_TIMEOUT) {
      composeTestRule
          .onAllNodes(
              hasTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
                  .and(hasAnyDescendant((hasText("Deleted office")).or(hasText("Unassigned")))),
              useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(
            hasTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
                .and(hasAnyDescendant((hasText("Deleted office")).or(hasText("Unassigned")))),
            useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun vet_showsFarmerIdText() {
    val viewModel = ReportViewViewModel()
    setVetScreen(viewModel)

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(LONG_TIMEOUT) {
      composeTestRule
          .onAllNodes(
              hasTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
                  .and(
                      hasAnyDescendant(
                          hasText("Deleted user")
                              .or(hasText("Unassigned"))
                              .or(hasText("Farmer", substring = true)))),
              useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.ROLE_INFO_LINE, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun vet_roleInfoLine_showsFarmerRole_orIdentifier() {
    setVetScreen()

    composeTestRule.waitUntil(LONG_TIMEOUT) {
      composeTestRule
          .onAllNodes(
              hasTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
                  .and(
                      hasAnyDescendant(
                          hasText("Farmer", substring = true)
                              .or(hasText("Deleted user"))
                              .or(hasText("Unassigned")))),
              useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(
            hasTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
                .and(
                    hasAnyDescendant(
                        hasText("Farmer", substring = true)
                            .or(hasText("Deleted user"))
                            .or(hasText("Unassigned")))),
            useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun vet_canSelectInProgressStatus_viaDropdown() {
    // Test selecting IN_PROGRESS via dropdown (complements existing RESOLVED test)
    val viewModel = ReportViewViewModel()
    setValidVetScreen(viewModel)
    composeTestRule.waitForIdle()

    // Open dropdown and pick IN_PROGRESS
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.getTagForStatusOption("IN_PROGRESS"))
        .performClick()

    // Ensure badge text updated
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("IN PROGRESS")
  }

  @Test
  fun vet_saveButton_navigatesBackAfterSuccessfulSave() {
    val fakeRepo = FakeReportRepository()
    val viewModel = ReportViewViewModel(repository = fakeRepo)

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigation = NavigationActions(navController)
      val TEST_REPORT_ID = "RPT001"
      val vet = Vet("valid_vet_id", "john", "john", "john@john.john", null)

      NavHost(navController = navController, startDestination = Screen.Overview.route) {
        composable(Screen.Overview.route) {
          OverviewScreen(
              userRole = UserRole.VET,
              user = vet,
              overviewViewModel = FakeOverviewViewModel(),
              onAddReport = {},
              onReportClick = {},
              navigationActions = navigation)
        }
        composable(Screen.ViewReport.route) { backStackEntry ->
          ReportViewScreen(
              navigationActions = navigation,
              userRole = UserRole.VET,
              viewModel = viewModel,
              reportId = TEST_REPORT_ID,
              user = vet)
        }
      }

      // Navigate to the report detail when composition starts
      androidx.compose.runtime.LaunchedEffect(Unit) {
        navigation.navigateTo(Screen.ViewReport(TEST_REPORT_ID))
      }
    }

    composeTestRule.waitUntil(LONG_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(ReportViewScreenTestTags.ANSWER_FIELD)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Edit answer to simulate changes
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.ANSWER_FIELD)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("Edited answer")

    // Click Save via testTag (more robust than text)
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait until Overview screen is displayed (after save triggers goBack())
    composeTestRule.waitUntil(LONG_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(OverviewScreenTestTags.SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SCREEN).assertIsDisplayed()
    assertTrue(fakeRepo.editCalled)
  }

  @Test
  fun vet_unsavedChanges() {
    setValidVetScreen()

    val alertBox = composeTestRule.onNodeWithTag(ReportViewScreenTestTags.UNSAVED_ALERT_BOX)
    val backButton = composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)

    // Change something
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.ANSWER_FIELD).performTextInput("wsh")

    // Try to go back and cancel
    backButton.performClick()
    alertBox.assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.UNSAVED_ALERT_BOX_CANCEL)
        .assertIsDisplayed()
        .performClick()
    alertBox.assertIsNotDisplayed()

    // Try to go back and discard
    backButton.performClick()
    alertBox.assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.UNSAVED_ALERT_BOX_DISCARD)
        .assertIsDisplayed()
        .performClick()
    alertBox.assertIsNotDisplayed()

    // Too lazy to add navigation, so check if the screen consumed the unsaved changes flag
    backButton.performClick()
    alertBox.assertIsNotDisplayed()
  }

  @Test
  fun vet_collectedSwitch() {
    setVetScreen()
    composeTestRule
        .onNodeWithTag(ReportComposableCommonsTestTags.COLLECTED_SWITCH)
        .assertIsDisplayed()
  }
}
