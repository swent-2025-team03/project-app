package com.android.agrihealth.ui.authentification

import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
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
}
