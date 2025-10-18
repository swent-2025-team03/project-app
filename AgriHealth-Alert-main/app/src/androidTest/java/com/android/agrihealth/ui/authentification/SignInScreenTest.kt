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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.model.authentification.FirebaseEmulatorsTest
import java.util.function.Predicate.not
import org.hamcrest.Matchers.not
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
    composeTestRule.setContent { AgriHealthApp() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_DIVIDER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SNACKBAR).assertIsNotDisplayed()
  }

  @Test
  fun signInWithEmptyFieldsFail() {
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SNACKBAR).isDisplayed()
    composeTestRule.onNodeWithText(SignInErrorMsg.EMPTY_EMAIL_OR_PASSWORD).isDisplayed()
  }

  @Test
  fun signInWithUnregisteredAccountFails() {
    completeSignIn(user4.email, password4)
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SNACKBAR).isDisplayed()
    composeTestRule.onNodeWithText(SignInErrorMsg.INVALID_CREDENTIALS).isDisplayed()
  }

  @Test
  fun signInWithNoInternetFails() {
    setNetworkEnabled(false)
    completeSignIn(user4.email, password4)
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SNACKBAR).isDisplayed()
    composeTestRule.onNodeWithText(SignInErrorMsg.TIMEOUT).isDisplayed()
    setNetworkEnabled(true)
  }
}
