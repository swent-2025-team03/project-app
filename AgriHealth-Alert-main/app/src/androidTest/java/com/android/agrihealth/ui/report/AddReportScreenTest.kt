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
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAddReportViewModel
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.utils.TestAssetUtils.cleanupTestAssets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

private fun fakeFarmerViewModel(): UserViewModel {
  return object : UserViewModel() {
    private val fakeUserFlow =
        MutableStateFlow(
            Farmer(
                uid = "test_user",
                firstname = "Farmer",
                lastname = "Joe",
                email = "email@email.com",
                address = Location(0.0, 0.0, "123 Farm Lane"),
                linkedVets = listOf("Best Vet Ever!", "Meh Vet", "Great Vet"),
                defaultVet = null))

    override var user: StateFlow<User> = fakeUserFlow.asStateFlow()
  }
}

class AddReportScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @After
  fun cleanup() {
    cleanupTestAssets()
  }

  // -- Helper function --
  private fun createReport(title: String, description: String) {
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput(title)
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput(description)
    val scrollContainer = composeRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
    var index = 0
    while (true) {
      composeRule.waitForIdle()
      val openNode =
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_OPEN")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (openNode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_OPEN"))
        composeRule.onNodeWithTag("QUESTION_${index}_OPEN").performTextInput("answer $index")
        index++
        continue
      }
      val yesNode =
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_YESORNO")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (yesNode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_YESORNO"))
        val options = composeRule.onAllNodesWithTag("QUESTION_${index}_YESORNO")
        options[0].performClick()
        index++
        continue
      }
      val mcqNode =
          composeRule.onAllNodesWithTag("QUESTION_${index}_MCQ").fetchSemanticsNodes().firstOrNull()
      if (mcqNode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_MCQ"))
        val options = composeRule.onAllNodesWithTag("QUESTION_${index}_MCQ")
        options[0].performClick()
        index++
        continue
      }
      val mcqONode =
          composeRule
              .onAllNodesWithTag("QUESTION_${index}_MCQO")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (mcqONode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_MCQO"))
        val options = composeRule.onAllNodesWithTag("QUESTION_${index}_MCQO")
        options[0].performClick()
        index++
        continue
      }
      break
    }

    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
  }

  @Test
  fun displayAllFieldsAndButtons() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
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

    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.VET_DROPDOWN))
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createButton_showsSnackbar_onEmptyFields() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
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
  fun selectingVet_updatesDisplayedOption() {
    val fakeUserViewModel = fakeFarmerViewModel()

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userViewModel = fakeUserViewModel,
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.VET_DROPDOWN))
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).performClick()
    val firstVet = "Best Vet Ever!"
    composeRule.onNodeWithText(firstVet).assertIsDisplayed().performClick()
    composeRule.onNodeWithText(firstVet).assertIsDisplayed()
  }

  @Test
  fun enteringTitleDescription_showsSuccessDialog() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }
    createReport("title", "description")
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
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = { called = true },
            addReportViewModel = FakeAddReportViewModel())
      }
    }

    createReport("title", "description")
    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule.onAllNodesWithText("OK").fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("OK").performClick()

    Assert.assertTrue(called)
  }
}
