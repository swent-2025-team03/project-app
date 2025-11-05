package com.android.agrihealth

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.agrihealth.data.model.authentification.FakeCredentialManager
import com.android.agrihealth.data.model.authentification.FakeJwtGenerator
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.authentification.RoleSelectionScreenTestTags
import com.android.agrihealth.ui.authentification.SignInErrorMsg
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags
import com.android.agrihealth.ui.map.MapScreenTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import com.android.agrihealth.ui.profile.ChangePasswordScreenTestTags
import com.android.agrihealth.ui.profile.EditProfileScreenTestTags
import com.android.agrihealth.ui.profile.ProfileViewModel
import com.android.agrihealth.ui.report.AddReportFeedbackTexts
import com.android.agrihealth.ui.report.AddReportScreenTestTags
import com.android.agrihealth.ui.report.ReportViewScreenTestTags
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ETest : FirebaseEmulatorsTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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

  private fun completeEditProfile(firstName: String, lastName: String) {
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput(firstName)

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput(firstName)

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun checkIsGoogleAccount() {
    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).assertIsNotDisplayed()
  }

  private fun signOutFromOverview() {
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun signOutFromProfile() {
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun createReport(title: String, description: String, vetId: String) {
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD)
        .assertIsDisplayed()
        .performTextInput(title)
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .assertIsDisplayed()
        .performTextInput(description)
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithText(vetId).assertIsDisplayed().performClick()
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onNodeWithText(AddReportFeedbackTexts.SUCCESS).isDisplayed()
    }
    composeTestRule.onNodeWithText("OK").assertIsDisplayed().performClick()
  }

  private fun clickFirstReportItem() {
    composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)[0].performClick()
  }

  private fun checkOverviewScreenIsDisplayed() {
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.SCREEN).isDisplayed()
    }
  }

  private fun checkEditProfileScreenIsDisplayed() {
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).isDisplayed()
    }
  }

  private fun goBack() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun reportViewClickViewOnMap() {
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.VIEW_ON_MAP)
        .assertIsDisplayed()
        .performClick()
  }

  private fun mapClickViewReport() {
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  // To fix E2E test clicking on random report marker on map (without needing to know its ID)
  fun ComposeTestRule.clickRandomReportMarker() {
    waitUntil {
      onAllNodes(hasTestTagThatStartsWith("reportMarker_")).fetchSemanticsNodes().isNotEmpty()
    }
    val allMarkers = onAllNodes(hasTestTagThatStartsWith("reportMarker_"))
    val randomIndex = (0 until allMarkers.fetchSemanticsNodes().size).random()
    allMarkers[randomIndex].performClick()
  }

  fun hasTestTagThatStartsWith(prefix: String): SemanticsMatcher {
    return SemanticsMatcher("${SemanticsProperties.TestTag.name} starts with $prefix") { node ->
      val tag = node.config.getOrNull(SemanticsProperties.TestTag)
      tag?.startsWith(prefix) == true
    }
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
    checkEditProfileScreenIsDisplayed()
    checkIsGoogleAccount()
    completeEditProfile("VetFirstName", "VetLastName")
    signOutFromProfile()

    var uid = Firebase.auth.uid
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON)
        .assertIsDisplayed()
        .performClick()

    completeSignUp(email, pwd, isVet = true)
    checkEditProfileScreenIsDisplayed()
    goBack()
    goBack()
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
  fun testFarmer_OverviewFilters_WorkCorrectly() {
    composeTestRule.setContent { AgriHealthApp() }
    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()
    val vet1 = "Best Vet Ever!"
    val vet2 = "Meh Vet"
    createReport("Report 1", "Description 1", vet1)
    createReport("Report 2", "Description 2", vet2)

    // Report 1 appears when filtering for "In progress"
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.STATUS_DROPDOWN).performClick()
    composeTestRule.onNodeWithTag("OPTION_PENDING").performClick()
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 1"))

    // Report 1 does not appears when filtering for "Resolved"
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.STATUS_DROPDOWN).performClick()
    composeTestRule.onNodeWithTag("OPTION_RESOLVED").performClick()
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .filter(hasText("Report 1"))
        .assertCountEquals(0)

    // Report 1 appears, report 2 does not appears when filtering for vet1
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.STATUS_DROPDOWN).performClick()
    composeTestRule.onNodeWithTag("OPTION_All").performClick()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.VET_ID_DROPDOWN).performClick()
    composeTestRule.onNodeWithTag("OPTION_$vet1").performClick()
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 1"))

    // Report 1 and 2 both appear when filtering for All
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.VET_ID_DROPDOWN).performClick()
    composeTestRule.onNodeWithTag("OPTION_All").performClick()
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 1"))
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 2"))

    signOutFromOverview()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
  }

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
    val vetId = "Best Vet Ever!"
    createReport("Report title", "Report description", vetId)
    clickFirstReportItem()
    reportViewClickViewOnMap()
    composeTestRule.clickRandomReportMarker()
    mapClickViewReport()
    goBack()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    signOutFromOverview()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testVetFarmerLinkAndPasswordChange() {
    composeTestRule.setContent { AgriHealthApp() }

    val farmerEmail = "farmer.link@example.com"
    val password = "Password!123"

    val vet =
        Vet(
            uid = "vet_001",
            firstname = "Dr",
            lastname = "Vet",
            email = "vet@test.com",
            address = null,
            linkedFarmers = emptyList(),
            validCodes = emptyList())

    val userViewModel = UserViewModel(initialUser = vet)
    val profileViewModel = ProfileViewModel(userViewModel)
    profileViewModel.generateVetCode()

    // Wait for the code to appear in StateFlow
    val vetCode = runBlocking { profileViewModel.generatedCode.first { it != null } }

    println("Generated vet code: $vetCode")

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON).performClick()
    completeSignUp(farmerEmail, password, isVet = false)
    checkEditProfileScreenIsDisplayed()

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.CODE_FIELD)
        .assertIsDisplayed()
        .performTextInput((vetCode)!!)
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.ADD_CODE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(5000) {
      composeTestRule
          .onAllNodesWithTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.PASSWORD_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(6000) {
      try {
        composeTestRule
            .onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
            .assertIsDisplayed()
            .assertIsEnabled()
        true
      } catch (_: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    val newPassword = "NewPassword!456"
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
        .performTextInput(password)
    composeTestRule.onNodeWithText(password).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD)
        .performTextInput(newPassword)
    composeTestRule.onNodeWithText(newPassword).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).isDisplayed()
    }
  }
}
