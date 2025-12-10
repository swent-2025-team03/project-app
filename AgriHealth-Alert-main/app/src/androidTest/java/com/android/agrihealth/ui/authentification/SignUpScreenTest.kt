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
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpScreenTest {

  private val authRepository = FakeAuthRepository()
  private val user =
      Farmer(
          uid = "test_user",
          firstname = "Farmer",
          lastname = "Joe",
          email = "valid@email.com",
          address = Location(0.0, 0.0, "123 Farm Lane"),
          linkedOffices = emptyList(),
          defaultOffice = null,
      )

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun completeSignUp(email: String, password: String) {
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD)
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
  fun setUp() {
    runTest {
      authRepository.signUpWithEmailAndPassword("valid@email.com", "password1", user)
      authRepository.signOut()
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD).assertIsDisplayed()
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
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.EMPTY_FIELDS).isDisplayed()
    }
  }

  @Test
  fun signUpWithoutRoleFails() {
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(user.email)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput("password1")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput("password1")

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.ROLE_NULL).isDisplayed()
    }
  }

  @Test
  fun signUpWithMalformedEmailFails() {
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    completeSignUp("bad", "credentials")
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.BAD_EMAIL_FORMAT).isDisplayed()
    }
  }

  @Test
  fun signUpWithWeakPasswordFails() {
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    completeSignUp("realvalid@email.gmail", "bad")
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.WEAK_PASSWORD).isDisplayed()
    }
  }

  @Test
  fun signUpWithMismatchedPasswordsFails() {
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput("great@email.yeah")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput("password2")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput("password3")

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FARMER_PILL)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.CNF_PASSWORD_DIFF).isDisplayed()
    }
  }

  @Test
  fun signUpWithAlreadyUsedEmailFails() {
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    completeSignUp(user.email, "password1")

    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.ALREADY_USED_EMAIL).isDisplayed()
    }
  }

  @Test
  fun signUpWithoutInternetFails() {
    authRepository.switchConnection(false)
    composeTestRule.setContent { SignUpScreen(signUpViewModel = SignUpViewModel(authRepository)) }
    completeSignUp(user.email, "password2")
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithText(SignUpErrorMsg.TIMEOUT).isDisplayed()
    }
  }

  @Test
  fun signUpScreen_showsAndHidesLoadingOverlay() {
    val authRepo = FakeAuthRepository(delayMs = 300L)
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
        timeoutStart = TestConstants.DEFAULT_TIMEOUT,
        timeoutEnd = TestConstants.DEFAULT_TIMEOUT,
    )
  }

  @After
  fun turnOnInternet() {
    authRepository.switchConnection(true)
  }
}
