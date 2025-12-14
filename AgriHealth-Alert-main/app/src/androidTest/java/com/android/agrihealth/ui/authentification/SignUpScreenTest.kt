package com.android.agrihealth.ui.authentification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.data.model.user.UserViewModel
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestPassword.password1
import com.android.agrihealth.testhelpers.TestTimeout.DEFAULT_TIMEOUT
import com.android.agrihealth.testhelpers.TestTimeout.LONG_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.testhelpers.templates.UITest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SignUpScreenTest : UITest() {

  private val authRepository = FakeAuthRepository()
  private val user = farmer1

  private fun setContent() {
    setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
  }

  private fun fillTextForm() {
    with(SignUpScreenTestTags) {
      writeIn(FIRSTNAME_FIELD, user.firstname)
      writeIn(LASTNAME_FIELD, user.lastname)
      writeIn(EMAIL_FIELD, user.email)
      writeIn(PASSWORD_FIELD, password1)
      writeIn(CONFIRM_PASSWORD_FIELD, password1)
    }
  }

  @Before
  fun setUp() {
    runTest {
      authRepository.signUpWithEmailAndPassword(user.email, password1, user)
      authRepository.signOut()
    }
  }

  @Test
  override fun displayAllComponents() {
    setContent()

    with(SignUpScreenTestTags) {
      nodesAreDisplayed(
          BACK_BUTTON,
          TITLE,
          FIRSTNAME_FIELD,
          LASTNAME_FIELD,
          EMAIL_FIELD,
          PASSWORD_FIELD,
          CONFIRM_PASSWORD_FIELD,
          FARMER_PILL,
          VET_PILL,
          SAVE_BUTTON)
      nodeNotDisplayed(SNACKBAR)
    }
  }

  private fun failSignUpWithMsg(message: String) {
    clickOn(SignUpScreenTestTags.SAVE_BUTTON)
    textIsDisplayed(message)
  }

  @Test
  fun signUp_withoutFields_FailsForRightReasons() {
    setContent()

    val email = user.email
    val badEmail = "bad"
    val password = password1
    val badPassword = "weak"

    with(SignUpScreenTestTags) {
      // Empty form
      failSignUpWithMsg(SignUpErrorMsg.EMPTY_FIELDS)

      // No role
      fillTextForm()
      failSignUpWithMsg(SignUpErrorMsg.ROLE_NULL)

      // Bad email
      clickOn(FARMER_PILL)
      writeIn(EMAIL_FIELD, badEmail)
      failSignUpWithMsg(SignUpErrorMsg.BAD_EMAIL_FORMAT)

      // Weak password
      writeIn(EMAIL_FIELD, email)
      writeIn(PASSWORD_FIELD, badPassword)
      writeIn(CONFIRM_PASSWORD_FIELD, badPassword)
      failSignUpWithMsg(SignUpErrorMsg.WEAK_PASSWORD)

      // Mismatched password
      writeIn(PASSWORD_FIELD, password)
      writeIn(CONFIRM_PASSWORD_FIELD, password + "typo")
      failSignUpWithMsg(SignUpErrorMsg.CNF_PASSWORD_DIFF)

      // Already used email
      writeIn(CONFIRM_PASSWORD_FIELD, password)
      failSignUpWithMsg(SignUpErrorMsg.ALREADY_USED_EMAIL)

      // No internet
      authRepository.switchConnection(false)
      failSignUpWithMsg(SignUpErrorMsg.TIMEOUT)
    }
  }

  @Test
  fun signUpScreen_showsAndHidesLoadingOverlay() {
    val authRepo = FakeAuthRepository(delayMs = DEFAULT_TIMEOUT)
    val vm = SignUpViewModel(authRepo)

    composeTestRule.setContent {
      MaterialTheme {
        SignUpScreen(
            signUpViewModel = vm,
            userViewModel = UserViewModel(),
        )
      }
    }

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD).performTextInput("John")

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD).performTextInput("Doe")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .performTextInput("john@example.com")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .performTextInput("StrongPass123")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .performTextInput("StrongPass123")

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.FARMER_PILL).performClick()

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.assertOverlayDuringLoading(
        isLoading = { vm.uiState.value.isLoading },
        timeoutStart = LONG_TIMEOUT,
        timeoutEnd = LONG_TIMEOUT,
    )
  }

  @After
  fun turnOnInternet() {
    authRepository.switchConnection(true)
  }
}
