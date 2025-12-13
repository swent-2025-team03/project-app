package com.android.agrihealth.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.testhelpers.TestConstants
import org.junit.Rule
import org.junit.Test

class SignInScreenTest {

  private val authRepository = FakeAuthRepository()

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun completeSignIn(email: String, password: String) {
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(email)

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password)

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { SignInScreen(signInViewModel = SignInViewModel(authRepository)) }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_DIVIDER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SNACKBAR).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun signInWithEmptyFieldsFail() {
    composeTestRule.setContent { SignInScreen(signInViewModel = SignInViewModel(authRepository)) }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignInErrorMsg.EMPTY_EMAIL_OR_PASSWORD).isDisplayed()
    }
  }

  @Test
  fun signInWithUnregisteredAccountFails() {
    composeTestRule.setContent { SignInScreen(signInViewModel = SignInViewModel(authRepository)) }
    completeSignIn("bad", "credentials")
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignInErrorMsg.INVALID_CREDENTIALS).isDisplayed()
    }
  }

  @Test
  fun signInWithNoInternetFails() {
    authRepository.switchConnection(false)
    composeTestRule.setContent { SignInScreen(signInViewModel = SignInViewModel(authRepository)) }
    completeSignIn("bad", "credentials")
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignInErrorMsg.TIMEOUT).isDisplayed()
    }
  }
}
