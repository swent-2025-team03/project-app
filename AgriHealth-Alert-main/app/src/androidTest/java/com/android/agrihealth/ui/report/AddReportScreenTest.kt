package com.android.agrihealth.ui.report

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testutil.FakeAddReportViewModel
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.testutil.SlowFakeReportRepository
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.user.UserViewModelContract
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

private val linkedOffices =
    mapOf(
        "Best Office Ever!" to "Deleted office",
        "Meh Office" to "Deleted office",
        "Great Office" to "Deleted office")

private fun fakeFarmerViewModel(): UserViewModelContract =
    FakeUserViewModel(
        Farmer(
            uid = "test_user",
            firstname = "Farmer",
            lastname = "Joe",
            email = "email@email.com",
            address = Location(0.0, 0.0, "123 Farm Lane"),
            linkedOffices = linkedOffices.keys.toList(),
            defaultOffice = linkedOffices.keys.toList().first(),
        ))

class AddReportScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  // -- Helper function --
  private fun fillAndCreateReport(
      title: String = "",
      description: String = "",
      officeId: String? = null,
      address: Location? = null,
      fillQuestions: Boolean = false,
      viewModel: AddReportViewModel? = null,
  ) {
    // 1) Titre
    if (title.isNotBlank()) {
      composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput(title)
    }

    // 2) Description
    if (description.isNotBlank()) {
      composeRule
          .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
          .performTextInput(description)
    }

    // 3) Office (via UI + éventuellement ViewModel)
    if (officeId != null) {
      composeRule
          .onNodeWithTag(AddReportScreenTestTags.OFFICE_DROPDOWN)
          .performScrollTo()
          .performClick()

      composeRule
          .onNodeWithTag(
              AddReportScreenTestTags.getTestTagForOffice(officeId), useUnmergedTree = true)
          .performClick()

      // Pour le cas du vrai AddReportViewModel (overlay test),
      // on force aussi l'état côté VM si on a un viewModel
      viewModel?.setOffice(officeId)
    }

    // 4) Adresse : la UI ne met pas l’adresse dans le VM toute seule en test,
    // donc on passe direct par le ViewModel si fourni.
    if (address != null && viewModel != null) {
      viewModel.setAddress(address)
    }

    // 5) Questions
    if (fillQuestions) {
      val scroll = composeRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
      var index = 0
      while (true) {
        composeRule.waitForIdle()
        when {
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_OPEN")
              .fetchSemanticsNodes()
              .isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag("QUESTION_${index}_OPEN"))
            composeRule.onNodeWithTag("QUESTION_${index}_OPEN").performTextInput("answer $index")
          }
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_YESORNO")
              .fetchSemanticsNodes()
              .isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag("QUESTION_${index}_YESORNO"))
            composeRule.onAllNodesWithTag("QUESTION_${index}_YESORNO")[0].performClick()
          }
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_MCQ")
              .fetchSemanticsNodes()
              .isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag("QUESTION_${index}_MCQ"))
            composeRule.onAllNodesWithTag("QUESTION_${index}_MCQ")[0].performClick()
          }
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_MCQO")
              .fetchSemanticsNodes()
              .isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag("QUESTION_${index}_MCQO"))
            composeRule.onAllNodesWithTag("QUESTION_${index}_MCQO")[0].performClick()
          }
          else -> break
        }
        index++
      }
    }

    // 6) Bouton CREATE
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON)
        .performScrollTo()
        .performClick()
  }

  @Test
  fun displayAllFieldsAndButtons() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(onCreateReport = {}, addReportViewModel = FakeAddReportViewModel())
      }
    }
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()

    val scrollContainer = composeRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)

    val viewModel = FakeAddReportViewModel()
    val questions = viewModel.uiState.value.questionForms
    questions.forEachIndexed { index, question ->
      when (question) {
        is OpenQuestion -> {
          val node =
              composeRule
                  .onAllNodesWithTag("QUESTION_${index}_OPEN")
                  .fetchSemanticsNodes()
                  .firstOrNull()
          if (node != null) {
            scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_OPEN"))
            composeRule.onNodeWithTag("QUESTION_${index}_OPEN").assertIsDisplayed()
          }
        }
        is YesOrNoQuestion -> {
          val nodes =
              composeRule.onAllNodesWithTag("QUESTION_${index}_YESORNO").fetchSemanticsNodes()
          if (nodes.isNotEmpty()) {
            scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_YESORNO"))
            composeRule
                .onAllNodesWithTag("QUESTION_${index}_YESORNO")
                .assertAny(hasAnyAncestor(hasTestTag(AddReportScreenTestTags.SCROLL_CONTAINER)))
          }
        }
        is MCQ -> {
          val nodes = composeRule.onAllNodesWithTag("QUESTION_${index}_MCQ").fetchSemanticsNodes()
          if (nodes.isNotEmpty()) {
            scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_MCQ"))
            composeRule
                .onAllNodesWithTag("QUESTION_${index}_MCQ")
                .assertAny(hasAnyAncestor(hasTestTag(AddReportScreenTestTags.SCROLL_CONTAINER)))
          }
        }
        is MCQO -> {
          val nodes = composeRule.onAllNodesWithTag("QUESTION_${index}_MCQO").fetchSemanticsNodes()
          if (nodes.isNotEmpty()) {
            scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_MCQO"))
            composeRule
                .onAllNodesWithTag("QUESTION_${index}_MCQO")
                .assertAny(hasAnyAncestor(hasTestTag(AddReportScreenTestTags.SCROLL_CONTAINER)))
          }
        }
      }
    }

    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.OFFICE_DROPDOWN))
    composeRule.onNodeWithTag(AddReportScreenTestTags.OFFICE_DROPDOWN).assertIsDisplayed()
    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createButton_showsSnackbar_onEmptyFields() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(onCreateReport = {}, addReportViewModel = FakeAddReportViewModel())
      }
    }
    // Click with fields empty
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule
          .onAllNodesWithText(AddReportFeedbackTexts.FAILURE)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeRule.onNodeWithText(AddReportFeedbackTexts.FAILURE).assertIsDisplayed()
  }

  @Test
  fun selectingOffice_updatesDisplayedOption() {
    val fakeUserViewModel = fakeFarmerViewModel()

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userViewModel = fakeUserViewModel,
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }

    val firstOfficeId = linkedOffices.keys.first()
    val firstOfficeName = linkedOffices[firstOfficeId]!!

    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.OFFICE_DROPDOWN))

    composeRule.onNodeWithTag(AddReportScreenTestTags.OFFICE_DROPDOWN).performClick()
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(
            AddReportScreenTestTags.getTestTagForOffice(firstOfficeId), useUnmergedTree = true)
        .assertExists()
        .performClick()

    composeRule.onNodeWithText(firstOfficeName, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun enteringTitleDescription_showsSuccessDialog() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(onCreateReport = {}, addReportViewModel = FakeAddReportViewModel())
      }
    }

    fillAndCreateReport(
        title = "title",
        description = "description",
    )

    // Check that dialog appears
    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule
          .onAllNodesWithText(AddReportFeedbackTexts.SUCCESS)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun dismissingDialog_callsOnCreateReport() {
    var called = false

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            onCreateReport = { called = true }, addReportViewModel = FakeAddReportViewModel())
      }
    }

    fillAndCreateReport(
        title = "title",
        description = "description",
    )
    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule.onAllNodesWithText("OK").fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("OK").performClick()

    Assert.assertTrue(called)
  }

  @Test
  fun createReport_showsLoadingOverlay() {

    val slowRepo: ReportRepository = SlowFakeReportRepository(delayMs = 1200L)
    val viewModel = AddReportViewModel(userId = "test_user", reportRepository = slowRepo)

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userViewModel = fakeFarmerViewModel(),
            onCreateReport = {},
            addReportViewModel = viewModel)
      }
    }

    fillAndCreateReport(
        title = "Slow Test",
        description = "Desc",
        officeId = "Best Office Ever!",
        address = Location(0.0, 0.0, "Test address"),
        fillQuestions = true,
        viewModel = viewModel,
    )

    composeRule.assertOverlayDuringLoading(
        isLoading = { viewModel.uiState.value.isLoading },
        timeoutStart = TestConstants.DEFAULT_TIMEOUT,
        timeoutEnd = TestConstants.DEFAULT_TIMEOUT,
    )
  }
}
