package com.android.agrihealth.ui.report

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAddReportViewModel
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.utils.TestAssetUtils.FAKE_PHOTO_FILE
import com.android.agrihealth.utils.TestAssetUtils.cleanupTestAssets
import com.android.agrihealth.utils.TestAssetUtils.getUriFrom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
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
        .assertTextEquals(AddReportUploadButtonTexts.UPLOAD_IMAGE)
  }

  @Test
  fun imagePreview_canRemoveImage() {
    val imageUri = getUriFrom(FAKE_PHOTO_FILE)
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
        .assertTextEquals(AddReportUploadButtonTexts.REMOVE_IMAGE)
  }

  @Test
  fun uploadImageDialog_cancel_dismissedNoPhotoPicked() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
          userRole = UserRole.FARMER,
          userId = "test_user",
          onCreateReport = {},
          addReportViewModel = FakeAddReportViewModel())
      }
    }

    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON).performClick()
    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_DIALOG).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_CANCEL).performClick()
    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_DIALOG).assertIsNotDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsNotDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON).assertTextEquals(
      AddReportUploadButtonTexts.UPLOAD_IMAGE)
  }


  @Test
  fun uploadImageDialog_gallery_setsPhoto() {
    val fakeViewModel = FakeAddReportViewModel()
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
          userRole = UserRole.FARMER,
          userId = "test_user",
          onCreateReport = {},
          addReportViewModel = fakeViewModel)
      }
    }

    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON).performClick()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_GALLERY).assertHasClickAction()

    // Simulate picking photo from gallery
    val imageUri = getUriFrom(FAKE_PHOTO_FILE)
    fakeViewModel.setPhoto(imageUri)

    composeRule.waitForIdle()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsDisplayed()
    composeRule
      .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
      .assertTextEquals(AddReportUploadButtonTexts.REMOVE_IMAGE)
  }


  @Test
  fun uploadImageDialog_camera_setsPhoto() {
    val fakeViewModel = FakeAddReportViewModel()
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
          userRole = UserRole.FARMER,
          userId = "test_user",
          onCreateReport = {},
          addReportViewModel = fakeViewModel)
      }
    }

    composeRule.onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON).performClick()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_CAMERA).assertHasClickAction()

    // Simulate picking photo with camera
    val cameraUri = getUriFrom(FAKE_PHOTO_FILE)
    fakeViewModel.setPhoto(cameraUri)

    composeRule.waitForIdle()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_PREVIEW).assertIsDisplayed()
    composeRule
      .onNodeWithTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON)
      .assertTextEquals(AddReportUploadButtonTexts.REMOVE_IMAGE)
  }
}
