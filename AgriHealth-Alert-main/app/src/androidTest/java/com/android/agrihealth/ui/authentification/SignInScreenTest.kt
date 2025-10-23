package com.android.agrihealth.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.model.authentification.FakeCredentialManager
import com.android.agrihealth.model.authentification.FakeJwtGenerator
import com.android.agrihealth.model.authentification.FirebaseEmulatorsTest
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import org.junit.Rule
import org.junit.Test

class SignInScreenTest : FirebaseEmulatorsTest() {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun displayAllComponents() {
    composeRule.setContent { MaterialTheme { SignInScreen() } }
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.LOGIN_DIVIDER).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun canSignInWithGoogle() {
    super.setUp()
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    composeRule.setContent { AgriHealthApp(credentialManager = fakeCredentialManager) }
    composeRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeRule.waitUntil(5000) {
      composeRule.onNodeWithTag(OverviewScreenTestTags.TOP_APP_BAR_TITLE).isDisplayed()
    }
  }
}
