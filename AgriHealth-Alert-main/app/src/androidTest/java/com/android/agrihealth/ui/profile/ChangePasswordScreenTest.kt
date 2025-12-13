package com.android.agrihealth.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertLoadingOverlayHidden
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.testutil.FakeChangePasswordViewModel
import com.android.agrihealth.testutil.TestConstants
import junit.framework.TestCase.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChangePasswordScreenTest {

  var success = false
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    success = false
  }

  private fun setContentWithVM(
      vm: ChangePasswordViewModelContract = FakeChangePasswordViewModel("password"),
      userEmail: String = ""
  ) {
    composeTestRule.setContent {
      ChangePasswordScreen(
          onBack = {},
          userEmail = userEmail,
          onUpdatePassword = { success = true },
          changePasswordViewModel = vm)
    }
  }

  @Test
  fun allComponentsAreDisplayed() {
    setContentWithVM()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun emptyFieldsSaysWeakPassword() {
    setContentWithVM()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.NEW_WEAK).assertIsDisplayed()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.OLD_WRONG).assertIsNotDisplayed()
    assert(!success)
  }

  @Test
  fun wrongOldPasswordShowsError() {
    setContentWithVM()
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD)
        .performTextInput("definitelyAStrongEnoughPassword123/()")
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.OLD_WRONG).assertIsDisplayed()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.NEW_WEAK).assertIsNotDisplayed()
    assertFalse(success)
  }

  @Test
  fun goodPasswordsSuccess() {
    setContentWithVM()
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
        .performTextInput("password")
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD)
        .performTextInput("definitelyAStrongEnoughPassword123/()")
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.OLD_WRONG).assertIsNotDisplayed()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.NEW_WEAK).assertIsNotDisplayed()
    assert(success)
  }

  @Test
  fun changePassword_showsAndHidesLoadingOverlay() {
    val fakeRepo = FakeAuthRepository(delayMs = 500)
    val viewModel = ChangePasswordViewModel(repository = fakeRepo)

    setContentWithVM(vm = viewModel)

    composeTestRule.assertLoadingOverlayHidden()

    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
        .performTextInput("oldpass")

    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD)
        .performTextInput("NewPassword123!")

    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.assertOverlayDuringLoading(
        isLoading = { viewModel.uiState.value.isLoading },
        timeoutStart = TestConstants.DEFAULT_TIMEOUT,
        timeoutEnd = TestConstants.DEFAULT_TIMEOUT)
  }
}
