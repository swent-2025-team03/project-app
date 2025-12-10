package com.android.agrihealth.ui.report

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
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
import com.android.agrihealth.data.model.report.QuestionForm
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testutil.FakeAddReportViewModel
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.testutil.InMemoryReportRepository
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.user.UserViewModelContract
import com.android.agrihealth.ui.utils.ImagePickerTestTags
import com.android.agrihealth.utils.TestAssetUtils.FAKE_PHOTO_FILE
import com.android.agrihealth.utils.TestAssetUtils.cleanupTestAssets
import com.android.agrihealth.utils.TestAssetUtils.getUriFrom
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
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

// Helper AddReportViewModel used to simulate a specific result of creating a report
// (i.e success, validation error, ...)
private class ResultFakeAddReportViewModel(
    private val resultToReturn: CreateReportResult,
) : AddReportViewModelContract {
  private val _uiState =
      MutableStateFlow(
          AddReportUiState(
              title = "title",
              description = "description",
              questionForms = emptyList(),
          ))

  override val uiState = _uiState

  override fun switchCollected() {}

  override fun setTitle(newTitle: String) {}

  override fun setDescription(newDescription: String) {}

  override fun setOffice(officeId: String) {}

  override fun setAddress(address: Location?) {}

  override fun setPhoto(uri: Uri?) {}

  override fun removePhoto() {}

  override fun setUploadedImagePath(path: String?) {}

  override fun updateQuestion(index: Int, updated: QuestionForm) {}

  override suspend fun createReport(): CreateReportResult = resultToReturn

  override fun clearInputs() {}
}

class AddReportScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  // --- Helpers ---

  private fun assertDialogIsShown(testTag: String) {
    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun assertDialogWorks(
      dialogTestTag: String,
      dialogTitle: String,
      dismissTestTag: String
  ) {
    assertDialogIsShown(dialogTestTag)

    composeRule.onNodeWithText(dialogTitle).assertIsDisplayed()

    composeRule.onNodeWithTag(dismissTestTag).assertHasClickAction()
  }

  private fun scrollToUploadSection() {
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
  }

  private fun fillAndCreateReport(
      title: String = "",
      description: String = "",
      officeId: String? = null,
      address: Location? = null,
      fillQuestions: Boolean = false,
      submit: Boolean = true,
      viewModel: AddReportViewModel? = null,
  ) {

    if (title.isNotBlank()) {
      composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput(title)
    }

    if (description.isNotBlank()) {
      composeRule
          .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
          .performTextInput(description)
    }

    if (officeId != null) {
      composeRule
          .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
          .performScrollToNode(hasTestTag(AddReportScreenTestTags.OFFICE_DROPDOWN))

      composeRule.onNodeWithTag(AddReportScreenTestTags.OFFICE_DROPDOWN).performClick()

      composeRule
          .onNodeWithTag(
              AddReportScreenTestTags.getTestTagForOffice(officeId), useUnmergedTree = true)
          .performClick()

      viewModel?.setOffice(officeId)
    }

    if (address != null && viewModel != null) {
      viewModel.setAddress(address)
    }

    if (fillQuestions) {
      val scroll = composeRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
      var index = 0
      while (true) {
        composeRule.waitForIdle()
        val openTag = "QUESTION_${index}_OPEN"
        val yesNoTag = "QUESTION_${index}_YESORNO"
        val mcqTag = "QUESTION_${index}_MCQ"
        val mcqOTag = "QUESTION_${index}_MCQO"

        when {
          composeRule.onAllNodesWithTag(openTag).fetchSemanticsNodes().isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag(openTag))
            composeRule.onNodeWithTag(openTag).performTextInput("answer $index")
          }
          composeRule.onAllNodesWithTag(yesNoTag).fetchSemanticsNodes().isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag(yesNoTag))
            composeRule.onAllNodesWithTag(yesNoTag)[0].performClick()
          }
          composeRule.onAllNodesWithTag(mcqTag).fetchSemanticsNodes().isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag(mcqTag))
            composeRule.onAllNodesWithTag(mcqTag)[0].performClick()
          }
          composeRule.onAllNodesWithTag(mcqOTag).fetchSemanticsNodes().isNotEmpty() -> {
            scroll.performScrollToNode(hasTestTag(mcqOTag))
            composeRule.onAllNodesWithTag(mcqOTag)[0].performClick()
          }
          else -> break
        }
        index++
      }
    }

    if (submit) {
      composeRule
          .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
          .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
      composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
    }
  }

  // --- Tests ---

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
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule
          .onAllNodesWithText(AddReportFeedbackTexts.INCOMPLETE)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeRule.onNodeWithText(AddReportFeedbackTexts.INCOMPLETE).assertIsDisplayed()
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
        fillQuestions = true,
        submit = true,
    )

    assertDialogWorks(
        AddReportScreenTestTags.DIALOG_SUCCESS,
        AddReportDialogTexts.TITLE_SUCCESS,
        AddReportScreenTestTags.DIALOG_SUCCESS_OK)
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
        fillQuestions = true,
        submit = true,
    )

    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule.onAllNodesWithText(AddReportDialogTexts.OK).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText(AddReportDialogTexts.OK).performClick()

    assertTrue(called)
  }

  @Test
  fun imagePreview_isNotShownWhenEmpty() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(onCreateReport = {}, addReportViewModel = FakeAddReportViewModel())
      }
    }
    scrollToUploadSection()
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertDoesNotExist()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertTextEquals(AddReportUploadButtonTexts.UPLOAD_IMAGE)
  }

  @Test
  fun imagePreview_canRemoveImage() {
    val imageUri = getUriFrom(FAKE_PHOTO_FILE)
    val fakeViewModel = FakeAddReportViewModel()
    fakeViewModel.setPhoto(imageUri)
    composeRule.setContent {
      MaterialTheme { AddReportScreen(onCreateReport = {}, addReportViewModel = fakeViewModel) }
    }
    scrollToUploadSection()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsDisplayed()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals(AddReportUploadButtonTexts.REMOVE_IMAGE)
        .performClick()
    composeRule.waitForIdle()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals(AddReportUploadButtonTexts.UPLOAD_IMAGE)
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertDoesNotExist()
  }

  @Test
  fun imagePreview_isShownWhenUploaded() {
    val imageUri = getUriFrom(FAKE_PHOTO_FILE)
    val fakeViewModel = FakeAddReportViewModel()
    fakeViewModel.setPhoto(imageUri)

    composeRule.setContent {
      MaterialTheme { AddReportScreen(onCreateReport = {}, addReportViewModel = fakeViewModel) }
    }
    scrollToUploadSection()
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsDisplayed()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertTextEquals(AddReportUploadButtonTexts.REMOVE_IMAGE)
  }

  @Test
  fun uploadImageDialog_cancel_dismissedNoPhotoPicked() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(onCreateReport = {}, addReportViewModel = FakeAddReportViewModel())
      }
    }

    scrollToUploadSection()
    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON).performClick()
    composeRule.onNodeWithTag(ImagePickerTestTags.DIALOG).assertIsDisplayed()
    composeRule.onNodeWithTag(ImagePickerTestTags.CANCEL_BUTTON).performClick()
    composeRule.onNodeWithTag(ImagePickerTestTags.DIALOG).assertIsNotDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsNotDisplayed()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertTextEquals(AddReportUploadButtonTexts.UPLOAD_IMAGE)
  }

  @Test
  fun chosenImage_isDisplayedInPreview() {
    cleanupTestAssets()

    composeRule.waitForIdle()
    val imageUri = getUriFrom(FAKE_PHOTO_FILE)
    val fakeViewModel = FakeAddReportViewModel().apply { setPhoto(imageUri) }

    composeRule.waitForIdle()
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            onCreateReport = {},
            addReportViewModel = fakeViewModel,
        )
      }
    }

    composeRule.waitForIdle()
    scrollToUploadSection()

    composeRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeRule
          .onAllNodesWithTag(AddReportScreenTestTags.IMAGE_PREVIEW)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    scrollToUploadSection()

    composeRule.waitForIdle()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals(AddReportUploadButtonTexts.REMOVE_IMAGE)
  }

  @Test
  fun createReport_successDialogWorksCorrectly() {
    var backCalled = false
    var onCreateCalled = false

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            onBack = { backCalled = true },
            onCreateReport = { onCreateCalled = true },
            addReportViewModel = FakeAddReportViewModel(),
        )
      }
    }

    fillAndCreateReport(
        title = "title",
        description = "description",
        fillQuestions = true,
        submit = true,
    )

    assertDialogWorks(
        AddReportScreenTestTags.DIALOG_SUCCESS,
        AddReportDialogTexts.TITLE_SUCCESS,
        AddReportScreenTestTags.DIALOG_SUCCESS_OK)

    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DIALOG_SUCCESS_OK)
        .assertHasClickAction()
        .performClick()

    assertTrue(backCalled)
    assertTrue(onCreateCalled)

    composeRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_SUCCESS).assertDoesNotExist()
  }

  @Test
  fun createReport_errorDialogWorksCorrectly() {
    val errorMessage = "repository failed"
    val error = RuntimeException(errorMessage)
    val fakeViewModel = ResultFakeAddReportViewModel(CreateReportResult.UploadError(error))

    composeRule.setContent {
      MaterialTheme { AddReportScreen(onCreateReport = {}, addReportViewModel = fakeViewModel) }
    }

    composeRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()

    assertDialogWorks(
        AddReportScreenTestTags.DIALOG_FAILURE,
        AddReportDialogTexts.TITLE_FAILURE,
        AddReportScreenTestTags.DIALOG_FAILURE_OK)

    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DIALOG_FAILURE_OK)
        .assertHasClickAction()
        .performClick()

    composeRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_FAILURE).assertDoesNotExist()
  }

  @Test
  fun createReport_showsLoadingOverlay() {
    val slowRepo = InMemoryReportRepository(delayMs = 500L)
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
        submit = true,
        viewModel = viewModel,
    )

    composeRule.assertOverlayDuringLoading(
        isLoading = { viewModel.uiState.value.isLoading },
        timeoutStart = TestConstants.DEFAULT_TIMEOUT,
        timeoutEnd = TestConstants.DEFAULT_TIMEOUT,
    )
  }
}
