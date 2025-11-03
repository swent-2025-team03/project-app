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
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.data.model.authentification.FakeCredentialManager
import com.android.agrihealth.data.model.authentification.FakeJwtGenerator
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.ui.profile.EditProfileScreenTestTags
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInScreenTest : FirebaseEmulatorsTest() {

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

  private fun setNetworkEnabled(enabled: Boolean) {
    val state = if (enabled) "enable" else "disable"
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    uiAutomation.executeShellCommand("svc wifi $state").close()
    uiAutomation.executeShellCommand("svc data $state").close()
  }

  @Before
  override fun setUp() {
    super.setUp()
    authRepository.signOut()
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { AgriHealthApp() }
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
  fun canSignInWithGoogle() {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    composeTestRule.setContent { AgriHealthApp(credentialManager = fakeCredentialManager) }
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onNodeWithTag(RoleSelectionScreenTestTags.VET).isDisplayed()
    }
    composeTestRule.onNodeWithTag(RoleSelectionScreenTestTags.VET).performClick()

    composeTestRule.waitUntil(5000) {
      composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).isDisplayed()
    }
  }

  @Test
  fun signInWithEmptyFieldsFail() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignInErrorMsg.EMPTY_EMAIL_OR_PASSWORD).isDisplayed()
    }
  }

  @Test
  fun signInWithUnregisteredAccountFails() {
    composeTestRule.setContent { AgriHealthApp() }
    completeSignIn(user4.email, password4)
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignInErrorMsg.INVALID_CREDENTIALS).isDisplayed()
    }
  }

  @Test
  fun signInWithNoInternetFails() {
    composeTestRule.setContent { AgriHealthApp() }
    setNetworkEnabled(false)
    completeSignIn(user4.email, password4)
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(SignInErrorMsg.TIMEOUT).isDisplayed()
    }
  }

  @After
  fun turnOnInternet() {
    setNetworkEnabled(true)
  }
}
