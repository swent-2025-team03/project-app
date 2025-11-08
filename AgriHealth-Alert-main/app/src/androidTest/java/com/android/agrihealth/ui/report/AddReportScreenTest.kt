package com.android.agrihealth.ui.report

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAddReportViewModel
import java.io.File
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

  val FAKE_PICTURE_FILE = "report_image_cat.jpg"

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

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
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
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
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
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

    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).performClick()
    val firstVet = "Best Vet Ever!"
    composeRule.onNodeWithText(firstVet).assertIsDisplayed().performClick()
    composeRule.onNodeWithText(firstVet).assertIsDisplayed()
  }

  @Test
  fun previewComposable_rendersWithoutCrash() {
    composeRule.setContent { AddReportScreenPreview() }

    // Verify that essential UI components render (sample check)
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
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

    // Fill in valid fields
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput("Title")
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput("Description")

    // Click create
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()

    // Check that dialog appears
    composeRule.onNodeWithText(AddReportFeedbackTexts.SUCCESS).assertIsDisplayed()
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

    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput("Valid Title")
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput("Some description")
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()

    composeRule.onNodeWithText("OK").assertIsDisplayed()
    composeRule.onNodeWithText("OK").performClick()

    Assert.assertTrue(called)
  }

  @Test
  fun imagePreview_isNotShownWhenEmpty() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertDoesNotExist()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertTextEquals(AddReport_UploadButtonTexts.UPLOAD_IMAGE)
  }

  @Test
  fun imagePreview_canRemoveImage() {
    val imageUri = getPicture(FAKE_PICTURE_FILE)
    val fakeViewModel = FakeAddReportViewModel()
    fakeViewModel.setPhoto(imageUri)
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = fakeViewModel)
      }
    }
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsDisplayed()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals(AddReport_UploadButtonTexts.REMOVE_IMAGE)
        .performClick()
    composeRule.waitForIdle()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals(AddReport_UploadButtonTexts.UPLOAD_IMAGE)
        .performClick()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertDoesNotExist()
  }

  @Test
  fun imagePreview_isShownWhenUploaded() {
    val imageUri = getPicture(FAKE_PICTURE_FILE)

    val fakeViewModel = FakeAddReportViewModel()
    fakeViewModel.setPhoto(imageUri)
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = fakeViewModel)
      }
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsDisplayed()
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
        .assertTextEquals(AddReport_UploadButtonTexts.REMOVE_IMAGE)
  }

  private fun getPicture(pictureName: String): Uri {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val file = File(context.cacheDir, pictureName)
    val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
    val inputStream = assetManager.open(pictureName)
    inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
    return Uri.fromFile(file)
  }
}
