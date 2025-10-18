package com.android.agrihealth.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.model.authentification.FirebaseEmulatorsTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpScreenTest : FirebaseEmulatorsTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun setNetworkEnabled(enabled: Boolean) {
    val state = if (enabled) "enable" else "disable"
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    uiAutomation.executeShellCommand("svc wifi $state").close()
    uiAutomation.executeShellCommand("svc data $state").close()
  }

  private fun completeSignUp(email: String, password: String) {
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(email)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FARMER_PILL)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  @Before
  override fun setUp() {
    super.setUp()
    runTest {
      authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
      authRepository.signOut()
    }
    composeTestRule.setContent { MaterialTheme { SignUpScreen() } }
  }

  @Test
  fun displayAllComponents() {

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SURNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.FARMER_PILL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.VET_PILL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SNACKBAR).assertIsNotDisplayed()
  }

  @Test
  fun signUpWithEmptyFieldsFails() {
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.EMPTY_FIELDS).isDisplayed()
    }
  }

  @Test
  fun signUpWithoutRoleFails() {
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(user2.email)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password2)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password2)

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.ROLE_NULL).isDisplayed()
    }
  }

  @Test
  fun signUpWithMalformedEmailFails() {
    completeSignUp(user4.email, password4)
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.BAD_EMAIL_FORMAT).isDisplayed()
    }
  }

  @Test
  fun signUpWithWeakPasswordFails() {
    completeSignUp(user3.email, password4)
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.WEAK_PASSWORD).isDisplayed()
    }
  }

  @Test
  fun signUpWithMissMatchedPasswordsFails() {
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(user2.email)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password2)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password3)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FARMER_PILL)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.CNF_PASSWORD_DIFF).isDisplayed()
    }
  }

  @Test
  fun signUpWithAlreadyUsedEmailFails() {
    completeSignUp(user1.email, password3)

    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.ALREADY_USED_EMAIL).isDisplayed()
    }
  }

  @Test
  fun signUpWithoutInternetFails() {
    setNetworkEnabled(false)
    completeSignUp(user2.email, password2)
    setNetworkEnabled(true)
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.TIMEOUT).isDisplayed()
    }
  }
}
