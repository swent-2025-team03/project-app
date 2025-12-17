package com.android.agrihealth.ui.utils

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.testutil.FakeImageRepository
import com.android.agrihealth.testutil.TestConstants.SHORT_TIMEOUT
import com.android.agrihealth.ui.profile.PhotoComponentsTestTags
import com.android.agrihealth.utils.TestAssetUtils
import com.mr0xf00.easycrop.rememberImageCropper
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfilePictureComponentsTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @After
  fun cleanup() {
    TestAssetUtils.cleanupTestAssets()
  }

  @Test
  fun cropperDialog_isShown() {
    val testPhotoUri = TestAssetUtils.getUriFrom(TestAssetUtils.FAKE_PHOTO_FILE)

    composeTestRule.setContent {
      MaterialTheme {
        val imageCropper = rememberImageCropper()
        val scope = rememberCoroutineScope()

        val imageCropperLauncher =
            rememberDefaultImageCropperLauncher(
                imageCropper = imageCropper,
                scope = scope,
                onCropSuccess = { /* ignore */},
                onCropError = { /* ignore */},
            )

        val croppingIsOngoing = imageCropper.cropState != null
        if (croppingIsOngoing) {
          ShowImageCropperDialog(imageCropper)
        }

        // Start crop once
        LaunchedEffect(Unit) { imageCropperLauncher(testPhotoUri) }
      }
    }

    composeTestRule.waitUntil(SHORT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(PhotoCropperTestTags.CROPPER_WINDOW)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(PhotoCropperTestTags.CROPPER_WINDOW).assertExists()
  }

  @Test
  fun errorDialog_rendersCorrectly() {
    val title = "Error!"
    val message = "Something failed"

    var show by mutableStateOf(true)
    var wasDismissed by mutableStateOf(false)

    composeTestRule.setContent {
      MaterialTheme {
        if (show) {
          ErrorDialog(
              dialogTitle = title,
              errorMessage = message,
              onDismiss = {
                wasDismissed = true
                show = false
              })
        }
      }
    }

    composeTestRule.onNodeWithTag(ProfilePictureComponentsTestTags.ERROR_DIALOG).assertIsDisplayed()

    composeTestRule.onNodeWithText(title).assertIsDisplayed()
    composeTestRule.onNodeWithText(message).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ProfilePictureComponentsTestTags.ERROR_DIALOG_OK_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()
        .assertTextEquals(ProfilePictureComponentsTexts.ERROR_DIALOG_OK)

    wasDismissed = false
    composeTestRule
        .onNodeWithTag(ProfilePictureComponentsTestTags.ERROR_DIALOG_OK_BUTTON)
        .performClick()
    assertTrue(wasDismissed)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ProfilePictureComponentsTestTags.ERROR_DIALOG)
        .assertIsNotDisplayed()
  }

  @Test
  fun profilePicture_defaultIcon_IsShown() {
    composeTestRule.setContent { MaterialTheme { ProfilePicture(photo = PhotoUi.Empty) } }

    composeTestRule.waitUntil(SHORT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(PhotoComponentsTestTags.DEFAULT_ICON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.DEFAULT_ICON).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.IMAGE_PREVIEW).assertIsNotDisplayed()
  }

  @Test
  fun profilePicture_localPhoto_IsShown() {
    val photo = byteArrayOf(1, 2, 3, 4, 5, 6)
    composeTestRule.setContent { MaterialTheme { ProfilePicture(photo = PhotoUi.Local(photo)) } }

    composeTestRule.waitUntil(SHORT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(PhotoComponentsTestTags.IMAGE_PREVIEW)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.IMAGE_PREVIEW).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.DEFAULT_ICON).assertIsNotDisplayed()
  }

  @Test
  fun profilePicture_remotePhoto_IsShown() {
    val fakeImageRepository = FakeImageRepository(connectionIsFrozen = false)
    fakeImageRepository.forceUploadImage(byteArrayOf(1, 2, 3, 4, 5, 6))
    val imageViewModel = ImageViewModel(fakeImageRepository)

    composeTestRule.setContent {
      MaterialTheme {
        ProfilePicture(photo = PhotoUi.Remote("fake/example/path"), imageViewModel = imageViewModel)
      }
    }

    composeTestRule.waitUntil(SHORT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(PhotoComponentsTestTags.PHOTO_RENDER)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.IMAGE_PREVIEW).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.DEFAULT_ICON).assertIsNotDisplayed()
  }
}
