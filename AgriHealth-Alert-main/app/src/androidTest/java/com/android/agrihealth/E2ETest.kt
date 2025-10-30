package com.android.agrihealth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.agrihealth.model.authentification.FakeCredentialManager
import com.android.agrihealth.model.authentification.FakeJwtGenerator
import com.android.agrihealth.model.authentification.FirebaseEmulatorsTest
import com.android.agrihealth.ui.authentification.RoleSelectionScreenTestTags
import com.android.agrihealth.ui.authentification.SignInErrorMsg
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ETest : FirebaseEmulatorsTest(true) {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // ----------- Helpers -----------

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, "12345678", user1) }
    authRepository.signOut()
  }

  private fun completeSignUp(email: String, password: String, isVet: Boolean) {
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

    val roleTag = if (isVet) SignUpScreenTestTags.VET_PILL else SignUpScreenTestTags.FARMER_PILL

    composeTestRule.onNodeWithTag(roleTag).assertIsDisplayed().performClick()

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

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

  private fun signOutFromOverview() {
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun clickFirstReportItem() {
    composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)[0].performClick()
  }

  private fun checkOverviewScreenIsDisplayed() {
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SCREEN).assertIsDisplayed()
  }

  // ----------- Scenario: Vet -----------
  @Test
  fun testVet_SignUp_Logout_SignIn() {
    val email = "vet@example.com"
    val pwd = "StrongPwd!123"
    val fakeGoogleIdToken = FakeJwtGenerator.createFakeGoogleIdToken("12345", email = email)

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

    checkOverviewScreenIsDisplayed()
    signOutFromOverview()
    var uid = Firebase.auth.uid
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON)
        .assertIsDisplayed()
        .performClick()

    completeSignUp(email, pwd, isVet = true)
    checkOverviewScreenIsDisplayed()
    assert(uid != Firebase.auth.uid)
    uid = Firebase.auth.uid
    signOutFromOverview()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
    completeSignIn(email, pwd)
    checkOverviewScreenIsDisplayed()
    assert(uid == Firebase.auth.uid)
  }

  // ----------- Scenario: Farmer -----------
  @Test
  fun testFarmer_SignIn_ClickReport_Back_Logout() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
    completeSignIn(user2.email, "12345678")
    composeTestRule.waitUntil(5_000) {
      composeTestRule.onNodeWithText(SignInErrorMsg.INVALID_CREDENTIALS).isDisplayed()
    }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).performTextClearance()
    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()
    clickFirstReportItem()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    checkOverviewScreenIsDisplayed()
    signOutFromOverview()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
