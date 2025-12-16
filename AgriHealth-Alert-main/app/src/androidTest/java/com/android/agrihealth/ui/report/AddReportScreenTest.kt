package com.android.agrihealth.ui.report

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.form.QuestionType
import com.android.agrihealth.testhelpers.FileTestUtils.TEST_IMAGE
import com.android.agrihealth.testhelpers.FileTestUtils.getUriFrom
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestReport.report1
import com.android.agrihealth.testhelpers.TestTimeout.SHORT_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.TestUser.office1
import com.android.agrihealth.testhelpers.fakes.FakeAddReportViewModel
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.ImagePickerTestTags
import com.android.agrihealth.ui.common.PhotoComponentsTestTags
import com.android.agrihealth.ui.common.PhotoComponentsTexts
import org.junit.Assert.assertTrue
import org.junit.Test

private val linkedOffices =
    mapOf(
        office1.id to "Deleted office",
        "Meh Office" to "Deleted office",
        "Great Office" to "Deleted office")

class AddReportScreenTest : UITest() {
  val report = report1
  val farmer =
      farmer1.copy(
          linkedOffices = linkedOffices.keys.toList(),
          defaultOffice = linkedOffices.keys.toList().first())
  val addReportVM = FakeAddReportViewModel()
  val userVM = FakeUserViewModel(farmer)

  private fun setContentWithVM(
      addReportViewModel: AddReportViewModelContract = addReportVM,
      onBack: () -> Unit = {},
      onCreate: () -> Unit = {}
  ) {
    setContent {
      AddReportScreen(
          userViewModel = userVM,
          onBack = { onBack() },
          onCreateReport = { onCreate() },
          addReportViewModel = addReportViewModel)
    }
  }

  @Test
  override fun displayAllComponents() {
    setContentWithVM()

    with(AddReportScreenTestTags) {
      nodesAreDisplayed(TITLE_FIELD, DESCRIPTION_FIELD)

      val questions = addReportVM.uiState.value.questionForms
      questions.forEachIndexed { index, question ->
        val tag = getTestTagForQuestion(question.type, index)
        scrollFormTo(tag)
      }

      scrollFormTo(OFFICE_DROPDOWN)
      scrollFormTo(CREATE_BUTTON)
    }
  }

  @Test
  fun createButton_showsSnackbar_onEmptyFields() {
    setContentWithVM()

    scrollFormToUploadSection()
    clickOn(AddReportScreenTestTags.CREATE_BUTTON)
    textIsDisplayed(AddReportFeedbackTexts.INCOMPLETE)
  }

  @Test
  fun selectingOffice_updatesDisplayedOption() {
    setContentWithVM()

    val firstOfficeId = linkedOffices.keys.first()
    val firstOfficeName = linkedOffices[firstOfficeId]!!

    scrollFormTo(AddReportScreenTestTags.OFFICE_DROPDOWN)
    clickOn(AddReportScreenTestTags.OFFICE_DROPDOWN)
    clickOn(AddReportScreenTestTags.getTestTagForOffice(firstOfficeId))
    textIsDisplayed(firstOfficeName)
  }

  @Test
  fun imagePreview_isShownWhenUploaded_canRemoveImage() {
    val imageUri = getUriFrom(TEST_IMAGE)
    addReportVM.setPhoto(imageUri)
    setContentWithVM()

    with(PhotoComponentsTestTags) {
      scrollFormToUploadSection()
      nodeIsDisplayed(IMAGE_PREVIEW)

      textContains(UPLOAD_IMAGE_BUTTON, PhotoComponentsTexts.REMOVE_IMAGE)
      clickOn(UPLOAD_IMAGE_BUTTON)

      textContains(UPLOAD_IMAGE_BUTTON, PhotoComponentsTexts.UPLOAD_IMAGE)
      nodeNotDisplayed(IMAGE_PREVIEW)

      clickOn(UPLOAD_IMAGE_BUTTON)
      nodeIsDisplayed(ImagePickerTestTags.DIALOG)
      clickOn(ImagePickerTestTags.CANCEL_BUTTON)
      nodeNotDisplayed(ImagePickerTestTags.DIALOG)
    }
  }

  @Test
  fun createReport_successDialogWorksCorrectly() {
    var backCalled = false
    var onCreateCalled = false

    setContentWithVM(onBack = { backCalled = true }, onCreate = { onCreateCalled = true })

    fillReportWith(report)

    assertDialogWorks(success = true)
    clickOn(AddReportScreenTestTags.DIALOG_OK)
    nodeNotDisplayed(AddReportScreenTestTags.DIALOG)

    assertTrue(backCalled)
    assertTrue(onCreateCalled)
  }

  @Test
  fun createReport_errorDialogWorksCorrectly() {
    val error = RuntimeException("repository failed")
    val errorVM = FakeAddReportViewModel(overrideResult = CreateReportResult.UploadError(error))

    setContentWithVM(errorVM)

    scrollFormToUploadSection()
    clickOn(AddReportScreenTestTags.CREATE_BUTTON)

    assertDialogWorks(success = false)

    clickOn(AddReportScreenTestTags.DIALOG_OK)
    nodeNotDisplayed(AddReportScreenTestTags.DIALOG)
  }

  @Test
  fun createReport_showsLoadingOverlay() {
    val slowRepo = FakeReportRepository(delayMs = SHORT_TIMEOUT)
    val slowVM = AddReportViewModel(userId = "test_user", reportRepository = slowRepo)

    setContentWithVM(addReportViewModel = slowVM)

    fillReportWith(report, viewModel = slowVM)

    composeTestRule.assertOverlayDuringLoading(isLoading = { slowVM.uiState.value.isLoading })
  }

  private fun assertDialogWorks(success: Boolean) {
    val expectedText =
        if (success) AddReportDialogTexts.TITLE_SUCCESS else AddReportDialogTexts.TITLE_FAILURE

    nodeIsDisplayed(AddReportScreenTestTags.DIALOG)
    textIsDisplayed(expectedText)
    node(AddReportScreenTestTags.DIALOG_OK).assertHasClickAction()
  }

  private fun scrollFormToUploadSection() = scrollFormTo(AddReportScreenTestTags.CREATE_BUTTON)

  private fun scrollFormTo(tag: String) = scrollTo(AddReportScreenTestTags.SCROLL_CONTAINER, tag)

  private fun fillReportWith(
      report: Report,
      doSubmitReport: Boolean = true,
      viewModel: AddReportViewModelContract = addReportVM
  ) {
    fun handleQuestions() {
      fun scrollAndWrite(tag: String, answer: String) {
        scrollFormTo(tag)
        writeIn(tag, answer)
      }

      fun scrollAndClickFirst(tag: String) {
        scrollFormTo(tag)
        composeTestRule.onAllNodesWithTag(tag)[0].performClick()
      }

      var index = 0
      while (true) {
        val questions =
            listOf(
                QuestionType.OPEN to { tag -> scrollAndWrite(tag, "answer $index") },
                QuestionType.YESORNO to ::scrollAndClickFirst,
                QuestionType.MCQ to ::scrollAndClickFirst,
                QuestionType.MCQO to ::scrollAndClickFirst)

        // Gets right tag and action, picks the valid one, and executes it
        val handleQuestion =
            questions
                .map { (type, action) ->
                  AddReportScreenTestTags.getTestTagForQuestion(type, index) to action
                }
                .firstOrNull { (tag, _) -> nodeExists(tag) }
                ?.also { (tag, action) -> action(tag) }

        if (handleQuestion == null) return

        index++
      }
    }

    with(AddReportScreenTestTags) {
      writeIn(TITLE_FIELD, report.title)
      writeIn(DESCRIPTION_FIELD, report.description)
      handleQuestions()

      scrollFormTo(OFFICE_DROPDOWN)
      clickOn(OFFICE_DROPDOWN)
      clickOn(getTestTagForOffice(report.officeId))

      scrollFormTo(LOCATION_BUTTON)
      viewModel.setAddress(report.location)

      if (doSubmitReport) {
        scrollFormToUploadSection()
        clickOn(CREATE_BUTTON)
      }
    }
  }
}
