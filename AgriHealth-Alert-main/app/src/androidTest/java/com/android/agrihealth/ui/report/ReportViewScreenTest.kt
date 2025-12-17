package com.android.agrihealth.ui.report

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testhelpers.FileTestUtils.FAKE_PHOTO_PATH
import com.android.agrihealth.testhelpers.FileTestUtils.addPlaceholderPhotoToRepository
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestReport
import com.android.agrihealth.testhelpers.TestTimeout.LONG_TIMEOUT
import com.android.agrihealth.testhelpers.TestTimeout.SHORT_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.fakes.FakeImageRepository
import com.android.agrihealth.testhelpers.fakes.FakeOverviewViewModel
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.PhotoComponentsTestTags
import com.android.agrihealth.ui.common.PhotoComponentsTexts
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreen
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * UI tests for [ReportViewScreen]. These tests ensure that all interactive and display elements
 * behave as expected in both Farmer and Vet modes.
 */
class ReportViewScreenTest : UITest() {
  val farmer = TestUser.FARMER1.copy()
  val vet = TestUser.VET1.copy()
  val unassignedVet = TestUser.VET2.copy()
  val report = TestReport.REPORT1.copy(assignedVet = vet.uid)

  val reportRepository = FakeReportRepository(initialReports = listOf(report))

  private fun setContentWithVM(
      user: User,
      reportViewModel: ReportViewViewModel = ReportViewViewModel(reportRepository),
      imageViewModel: ImageViewModel = ImageViewModel()
  ) {
    setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)

      ReportViewScreen(
          navigationActions = navigationActions,
          userRole = user.role,
          viewModel = reportViewModel,
          imageViewModel = imageViewModel,
          reportId = report.id,
          user = user)
    }
  }

  @Test
  fun vet_interactableComponentsWork() {
    setContentWithVM(vet)

    with(ReportViewScreenTestTags) {
      // Writing answer
      writeIn(ANSWER_FIELD, "This is my diagnosis.")

      // Changing report status
      val statuses = listOf(ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED)
      val firstStatus = statuses.first()

      scrollFormTo(STATUS_DROPDOWN_FIELD)

      clickOn(STATUS_DROPDOWN_FIELD)
      nodeIsDisplayed(STATUS_DROPDOWN_MENU)
      statuses.forEach { nodeIsDisplayed(getTagForStatusOption(it)) }

      clickOn(getTagForStatusOption(firstStatus))
      textContains(STATUS_BADGE_TEXT, firstStatus.displayString(), ignoreCase = true)

      // Spam - cancel then confirm
      scrollFormTo(SPAM_BUTTON)
      clickOn(SPAM_BUTTON)
      textIsDisplayed("Report as spam?")
      textIsDisplayed("Confirm")
      clickOnText("Cancel")
      textNotDisplayed("Report as spam?")
      textContains(STATUS_BADGE_TEXT, firstStatus.displayString(), ignoreCase = true)

      clickOn(SPAM_BUTTON)
      clickOnText("Confirm")
      textContains(STATUS_BADGE_TEXT, ReportStatus.SPAM.name)

      scrollFormTo(VIEW_ON_MAP)
      scrollFormTo(SAVE_BUTTON)
      scrollFormTo(ROLE_INFO_LINE)
      scrollFormTo(ReportComposableCommonsTestTags.COLLECTED_SWITCH)
    }
  }

  @Test
  fun farmer_interactableComponentsWork() {
    setContentWithVM(farmer)

    with(ReportViewScreenTestTags) {
      // No answer
      textIsDisplayed("No answer yet.")

      // Delete button
      scrollFormTo(DELETE_BUTTON)
      clickOn(DELETE_BUTTON)
      nodeIsDisplayed(DELETE_REPORT_ALERT_BOX)
      textIsDisplayed("Confirm")
      clickOnText("Cancel")
      nodeNotDisplayed(DELETE_REPORT_ALERT_BOX)

      scrollFormTo(VIEW_ON_MAP)
      scrollFormTo(ROLE_INFO_LINE)
      scrollFormTo(ReportComposableCommonsTestTags.COLLECTED_SWITCH)
    }
  }

  @Test
  fun invalidVet_doesNotSeeUnassign() {
    setContentWithVM(unassignedVet)

    with(ReportViewScreenTestTags) {
      nodesNotDisplayed(CLAIM_BUTTON, UNASSIGN_BUTTON)
      textIsDisplayed("This report was claimed by: ")
    }
  }

  // -------------------- Additional tests to increase coverage --------------------

  @Test
  fun vet_saveButton_navigatesBackAfterSuccessfulSave() {
    val fakeRepoWithReport = FakeReportRepository(listOf(report))
    val viewModel = ReportViewViewModel(repository = fakeRepoWithReport)

    setContent {
      val navController = rememberNavController()
      val navigation = NavigationActions(navController)

      NavHost(navController = navController, startDestination = Screen.Overview.route) {
        composable(Screen.Overview.route) {
          OverviewScreen(
              userRole = UserRole.VET,
              user = vet,
              overviewViewModel = FakeOverviewViewModel(),
              navigationActions = navigation)
        }
        composable(Screen.ViewReport.ROUTE) {
          ReportViewScreen(
              navigationActions = navigation,
              userRole = UserRole.VET,
              viewModel = viewModel,
              reportId = report.id,
              user = vet)
        }
      }

      // Manually navigate to report, so that goBack can work
      androidx.compose.runtime.LaunchedEffect(Unit) {
        navigation.navigateTo(Screen.ViewReport(report.id))
      }
    }

    with(ReportViewScreenTestTags) {
      nodeIsDisplayed(ANSWER_FIELD, timeout = LONG_TIMEOUT)
      writeIn(ANSWER_FIELD, "Edited answer")

      scrollFormTo(SAVE_BUTTON)
      clickOn(SAVE_BUTTON)

      nodeIsDisplayed(OverviewScreenTestTags.SCREEN, timeout = LONG_TIMEOUT)
      assertTrue(fakeRepoWithReport.editCalled)
    }
  }

  @Test
  fun vet_unsavedChanges() {
    setContentWithVM(vet)

    with(ReportViewScreenTestTags) {
      // No changes
      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)

      // Change something
      writeIn(ANSWER_FIELD, "wsh")

      // Go back and cancel
      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      nodeIsDisplayed(UNSAVED_ALERT_BOX)
      clickOn(UNSAVED_ALERT_BOX_CANCEL)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)

      // Go back and discard
      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      clickOn(UNSAVED_ALERT_BOX_DISCARD)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)

      // Too lazy to add navigation, so check if the screen consumed the unsaved changes flag
      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)
    }
  }

  @Test
  fun reportView_showsLoadingOverlayWhileFetchingReport() {
    val slowRepo = FakeReportRepository(listOf(report), SHORT_TIMEOUT)
    val slowVM = ReportViewViewModel(repository = slowRepo)

    setContentWithVM(vet, reportViewModel = slowVM)

    composeTestRule.assertOverlayDuringLoading(isLoading = { slowVM.uiState.value.isLoading })
  }

  @Test
  fun photoDisplay_transitionsFromLoadingToSuccess() {
    val dependencies = setUpScreenAndRepositories()

    with(PhotoComponentsTestTags) {
      dependencies.imageRepository.freezeRepoConnection()

      addPlaceholderPhotoToRepository(dependencies.imageRepository)
      nodeIsDisplayed(PHOTO_LOADING_ANIMATION)
      nodesNotDisplayed(PHOTO_RENDER, PHOTO_ERROR_TEXT)

      dependencies.imageRepository.unfreezeRepoConnection()
      nodeIsDisplayed(PHOTO_RENDER)
      nodesNotDisplayed(PHOTO_LOADING_ANIMATION, PHOTO_ERROR_TEXT)
    }
  }

  @Test
  fun photoDisplay_showsErrorText_whenDownloadFails() {
    val dependencies = setUpScreenAndRepositories()
    dependencies.imageRepository.makeRepoThrowError()
    dependencies.imageRepository.unfreezeRepoConnection()

    textContains(PhotoComponentsTestTags.PHOTO_ERROR_TEXT, PhotoComponentsTexts.PHOTO_ERROR_TEXT)
    nodeNotDisplayed(PhotoComponentsTestTags.PHOTO_RENDER)
  }

  @Test
  fun photoDisplay_showsNothing_whenNoPhotoURL() {
    setUpScreenAndRepositories(withPhoto = false)

    with(PhotoComponentsTestTags) {
      nodesNotDisplayed(PHOTO_RENDER, PHOTO_LOADING_ANIMATION, PHOTO_ERROR_TEXT)
    }
  }

  // Returned by the setup function so we can retrieve what was created
  private data class TestDependencies(
      val reportViewModel: ReportViewViewModel,
      val imageViewModel: ImageViewModel,
      val imageRepository: FakeImageRepository
  )

  private fun setUpScreenAndRepositories(withPhoto: Boolean = true): TestDependencies {
    val testReport = report.copy(photoURL = if (withPhoto) FAKE_PHOTO_PATH else null)
    val repoWithPhoto = FakeReportRepository(initialReports = listOf(testReport))
    val reportViewModel = ReportViewViewModel(repository = repoWithPhoto)

    val fakeImageRepository = FakeImageRepository()
    val imageViewModel = ImageViewModel(fakeImageRepository)

    setContentWithVM(vet, reportViewModel, imageViewModel)

    return TestDependencies(reportViewModel, imageViewModel, fakeImageRepository)
  }

  private fun scrollFormTo(tag: String) = scrollTo(ReportViewScreenTestTags.SCROLL_CONTAINER, tag)

  override fun displayAllComponents() {}
}
