package com.android.agrihealth.ui.authentification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestTimeout.DEFAULT_TIMEOUT
import com.android.agrihealth.testhelpers.TestTimeout.LONG_TIMEOUT
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserRepository
import com.android.agrihealth.testhelpers.templates.UITest
import org.junit.Test

class SignInScreenTest : UITest() {

  private val authRepository = FakeAuthRepository()

  private fun setContent() {
    setContent { SignInScreen(signInViewModel = SignInViewModel(authRepository)) }
  }

  private fun completeBadSignIn() {
    val email = "bad"
    val password = "credentials"
    with(SignInScreenTestTags) {
      writeIn(EMAIL_FIELD, email)
      writeIn(PASSWORD_FIELD, password)
      clickOn(LOGIN_BUTTON)
    }
  }

  @Test
  override fun displayAllComponents() {
    setContent()

    with(SignInScreenTestTags) {
      nodesAreDisplayed(
          SIGN_UP_BUTTON,
          EMAIL_FIELD,
          PASSWORD_FIELD,
          LOGIN_BUTTON,
          FORGOT_PASSWORD,
          LOGIN_DIVIDER,
          GOOGLE_LOGIN_BUTTON)
      nodeNotDisplayed(SNACKBAR)
    }
  }

  @Test
  fun signIn_withUnregisteredAccount_orEmptyFields_Fails() {
    setContent()

    clickOn(SignInScreenTestTags.LOGIN_BUTTON)
    textIsDisplayed(SignInErrorMsg.EMPTY_EMAIL_OR_PASSWORD)

    completeBadSignIn()
    textIsDisplayed(SignInErrorMsg.INVALID_CREDENTIALS)
  }

  @Test
  fun signInWithNoInternetFails() {
    authRepository.switchConnection(false)
    setContent()
    completeBadSignIn()
    textIsDisplayed(SignInErrorMsg.TIMEOUT)
  }

  @Test
  fun signInScreen_showsAndHidesLoadingOverlay() {

    val authRepo = FakeAuthRepository(delayMs = DEFAULT_TIMEOUT)
    val userRepo = FakeUserRepository()

    val vm = SignInViewModel(authRepo, userRepo)
    composeTestRule.setContent {
      MaterialTheme {
        SignInScreen(
            signInViewModel = vm,
        )
      }
    }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD)
        .performTextInput("user@example.com")

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD)
        .performTextInput("strongPassword123!")

    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()

    composeTestRule.assertOverlayDuringLoading(
        isLoading = { vm.uiState.value.isLoading },
        timeoutStart = LONG_TIMEOUT,
        timeoutEnd = LONG_TIMEOUT,
    )
  }
}
