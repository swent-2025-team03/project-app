package com.android.agrihealth.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.ui.profile.FakeChangePasswordViewModel
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
    composeTestRule.setContent {
      ChangePasswordScreen({}, "", { success = true }, FakeChangePasswordViewModel("password"))
    }
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun emptyFieldsSaysWeakPassword() {
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.NEW_WEAK).assertIsDisplayed()
    composeTestRule.onNodeWithText(ChangePasswordFeedbackTexts.OLD_WRONG).assertIsNotDisplayed()
    assert(!success)
  }

  @Test
  fun wrongOldPasswordShowsError() {
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
}
