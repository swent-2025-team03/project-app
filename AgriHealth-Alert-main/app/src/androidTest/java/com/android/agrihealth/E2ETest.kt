package com.android.agrihealth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.agrihealth.model.authentification.FirebaseEmulatorsTest
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ETest : FirebaseEmulatorsTest() {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  // ----------- Helpers -----------

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, "12345678", user1) }
  }

  private fun completeSignUp(email: String, password: String, isVet: Boolean) {
    // Remplir tous les champs requis
    composeRule
        .onNodeWithTag(SignUpScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Test")

    composeRule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User")

    composeRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(email)

    composeRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password)

    composeRule
        .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password)

    val roleTag = if (isVet) SignUpScreenTestTags.VET_PILL else SignUpScreenTestTags.FARMER_PILL

    composeRule.onNodeWithTag(roleTag).assertIsDisplayed().performClick()

    composeRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).assertIsDisplayed().performClick()
  }

  private fun completeSignIn(email: String, password: String) {
    composeRule
        .onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performTextInput(email)

    composeRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performTextInput(password)

    composeRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed().performClick()
  }

  private fun signOutFromOverview() {
    composeRule
        .onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun clickFirstReportItem() {
    composeRule.onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)[0].performClick()
  }

  private fun checkOverviewScreenIsDisplayed() {
    composeRule.onNodeWithTag(OverviewScreenTestTags.SCREEN).assertIsDisplayed()
  }

  // ----------- Scenario: Vet -----------
  @Test
  fun testVet_SignUp_Logout_SignIn() {
    val email = "vet@example.com"
    val pwd = "StrongPwd!123"
    composeRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON)
        .assertIsDisplayed()
        .performClick()

    completeSignUp(email, pwd, isVet = true)
    checkOverviewScreenIsDisplayed()
    signOutFromOverview()
    composeRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
    completeSignIn(email, pwd)
    checkOverviewScreenIsDisplayed()
  }

  // ----------- Scenario: Farmer -----------
  @Test
  fun testFarmer_SignIn_ClickReport_Back_Logout() {
    composeRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()
    clickFirstReportItem()
    composeRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsDisplayed().performClick()
    checkOverviewScreenIsDisplayed()
    signOutFromOverview()
    composeRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
