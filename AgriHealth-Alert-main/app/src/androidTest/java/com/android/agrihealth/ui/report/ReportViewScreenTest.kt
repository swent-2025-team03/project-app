package com.android.agrihealth.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.testutil.FakeOverviewViewModel
import com.android.agrihealth.testutil.TestConstants.LONG_TIMEOUT
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreen
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import com.android.agrihealth.ui.loading.LoadingTestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [ReportViewScreen]. These tests ensure that all interactive and display elements
 * behave as expected in both Farmer and Vet modes.
 */
class ReportViewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private class FakeReportRepository : ReportRepository {
    var editCalled = false
    private val sample = ReportViewUIState().report

    override fun getNewReportId(): String = "NEW_ID"

    override suspend fun getAllReports(userId: String) = emptyList<Report>()

    override suspend fun getReportsByFarmer(farmerId: String) = emptyList<Report>()

    override suspend fun getReportsByVet(vetId: String) = emptyList<Report>()

    override suspend fun getReportById(reportId: String): Report? = sample.copy(id = reportId)

    override suspend fun addReport(report: Report) {}

    override suspend fun editReport(reportId: String, newReport: Report) {
      editCalled = true
    }

    override suspend fun deleteReport(reportId: String) {}
  }

  /** Sets up the ReportViewScreen for a given role (Vet or Farmer). */
  private fun setReportViewScreen(role: UserRole, viewModel: ReportViewModel = ReportViewModel()) {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ReportViewScreen(
          navigationActions = navigationActions, userRole = role, viewModel = viewModel)
    }
  }

  // --- Role-specific helpers (wrappers) ---
  private fun setVetScreen(viewModel: ReportViewModel = ReportViewModel()) =
      setReportViewScreen(UserRole.VET, viewModel)

  private fun setFarmerScreen(viewModel: ReportViewModel = ReportViewModel()) =
      setReportViewScreen(UserRole.FARMER, viewModel)

  // --- TEST 1: Vet typing in answer field ---
  @Test
  fun vet_canTypeInAnswerField() {
    setVetScreen()
    val answerNode = composeTestRule.onNodeWithTag(ReportViewScreenTestTags.ANSWER_FIELD)
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
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_FIELD).performClick()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.STATUS_DROPDOWN_MENU).assertIsDisplayed()
  }

  // --- TEST 4: Vet can select a status ---
  @Test
  fun vet_canSelectResolvedStatus() {
    setVetScreen()
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
    setVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SPAM_BUTTON).performClick()
    composeTestRule.onNodeWithText("Report as spam?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
  }

  // --- TEST 6: Vet can cancel spam dialog ---
  @Test
  fun vet_canCancelSpamDialog() {
    setVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SPAM_BUTTON).performClick()
    composeTestRule.onNodeWithText("Report as spam?").performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()
  }

  // --- TEST 7: Vet can confirm spam ---
  @Test
  fun vet_canConfirmSpam() {
    setVetScreen()
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
    setVetScreen()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.VIEW_ON_MAP).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  // --- TEST 9: Dropdown should contain all expected statuses ---
  @Test
  fun dropdown_containsCorrectStatusOptions() {
    setVetScreen()
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
    val viewModel = ReportViewModel()
    setVetScreen(viewModel)
    composeTestRule.runOnUiThread { viewModel.onStatusChange(ReportStatus.RESOLVED) }
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("RESOLVED")
  }

  // -------------------- Additional tests to increase coverage --------------------

  @Test
  fun vet_autoChangesPendingToInProgress_onLaunch() {
    // When a Vet screen is launched and the ViewModel's report status is PENDING (default),
    // the LaunchedEffect in the composable should auto-change it to IN_PROGRESS.
    val viewModel = ReportViewModel()
    setVetScreen(viewModel)
    // Wait for composition + LaunchedEffect to run
    composeTestRule.waitForIdle()
    // The status badge should show "IN PROGRESS" (name has underscore replaced by space)
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT)
        .assertTextContains("IN PROGRESS")
  }

  @Test
  fun farmer_roleInfoLine_showsVetRole_orIdentifier() {
    setFarmerScreen()
    composeTestRule.waitUntil(LONG_TIMEOUT) {
      composeTestRule
          .onAllNodes(
              hasTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
                  .and(
                      hasAnyDescendant(
                          hasText("Vet", substring = true)
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
                        hasText("Vet", substring = true)
                            .or(hasText("Deleted user"))
                            .or(hasText("Unassigned")))),
            useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun vet_showsFarmerIdText() {
    val viewModel = ReportViewModel()
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
    val viewModel = ReportViewModel()
    setVetScreen(viewModel)
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
    val viewModel = ReportViewModel(repository = fakeRepo)

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigation = NavigationActions(navController)
      val TEST_REPORT_ID = "RPT001"

      NavHost(navController = navController, startDestination = Screen.Overview.route) {
        composable(Screen.Overview.route) {
          OverviewScreen(
              userRole = UserRole.VET,
              userId = "user_123",
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
              reportId = TEST_REPORT_ID)
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
    fun reportView_showsLoadingOverlayWhileFetchingReport() {
        // Fake slow repository that delays getReportById
        val slowRepo = object : ReportRepository {
            private val sample = ReportViewUIState().report
            override fun getNewReportId(): String = "NEW_ID"
            override suspend fun getAllReports(userId: String) = emptyList<Report>()
            override suspend fun getReportsByFarmer(farmerId: String) = emptyList<Report>()
            override suspend fun getReportsByVet(vetId: String) = emptyList<Report>()
            override suspend fun getReportById(reportId: String): Report? {
                kotlinx.coroutines.delay(1200) // simulate slow fetch
                return sample.copy(id = reportId)
            }
            override suspend fun addReport(report: Report) {}
            override suspend fun editReport(reportId: String, newReport: Report) {}
            override suspend fun deleteReport(reportId: String) {}
        }

        val vm = ReportViewModel(repository = slowRepo)

        composeTestRule.setContent {
            val nav = rememberNavController()
            val navigation = NavigationActions(nav)
            ReportViewScreen(
                navigationActions = navigation,
                userRole = UserRole.VET,
                viewModel = vm,
                reportId = "RPT_SLOW"
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) { vm.uiState.value.isLoading }

        composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()

        composeTestRule.waitUntil(timeoutMillis = 5000) { !vm.uiState.value.isLoading }

        composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
    }

}
