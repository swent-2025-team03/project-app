package com.android.agrihealth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.agrihealth.data.model.authentification.FirebaseEmulatorsTest
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
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
    authRepository.signOut()

    composeTestRule.setContent { AgriHealthApp() }
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
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON)
        .assertIsDisplayed()
        .performClick()

    completeSignUp(email, pwd, isVet = true)
    checkOverviewScreenIsDisplayed()
    signOutFromOverview()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
    completeSignIn(email, pwd)
    checkOverviewScreenIsDisplayed()
  }

  // ----------- Scenario: Farmer -----------
  @Test
  fun testFarmer_SignIn_ClickReport_Back_Logout() {
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
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
