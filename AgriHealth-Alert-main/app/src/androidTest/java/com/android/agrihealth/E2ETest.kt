package com.android.agrihealth

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.agrihealth.core.design.theme.Test
import com.android.agrihealth.data.model.authentification.FakeCredentialManager
import com.android.agrihealth.data.model.authentification.FakeJwtGenerator
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.authentification.RoleSelectionScreenTestTags
import com.android.agrihealth.ui.authentification.SignInErrorMsg
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags
import com.android.agrihealth.ui.map.MapScreenTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import com.android.agrihealth.ui.profile.ChangePasswordScreenTestTags
import com.android.agrihealth.ui.profile.EditProfileScreenTestTags
import com.android.agrihealth.ui.profile.ProfileScreenTestTags
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
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ETest : FirebaseEmulatorsTest() {

  private val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val ruleChain: TestRule =
      RuleChain.outerRule(
              GrantPermissionRule.grant(
                  android.Manifest.permission.ACCESS_FINE_LOCATION,
                  android.Manifest.permission.ACCESS_COARSE_LOCATION))
          .around(composeTestRule)

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, "12345678", user1) }
    authRepository.signOut()
  }

  // Important: this function needs to be used each time the screen changes in order to avoid
  // failing the CI tests due to slow rendering
  private fun waitUntilTestTag(tag: String) {
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(tag).isDisplayed()
    }
  }

  // ----------- End to End Tests -----------

  // ----------- Scenario: Vet -----------
  // For this test to work, don't forget to go on the Firebase Emulator console and enable multiple
  // accounts with the same email address
  @Test
  fun testVet_SignUp_Logout_SignIn() {
    val email = "vet@example.com"
    val pwd = "StrongPwd!123"
    val fakeGoogleIdToken = FakeJwtGenerator.createFakeGoogleIdToken("12345", email = email)
    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)
    composeTestRule.setContent { AgriHealthApp(credentialManager = fakeCredentialManager) }
    composeTestRule.waitForIdle()

    signInWithGoogle()
    chooseRole()
    checkEditProfileScreenIsDisplayed()
    checkIsGoogleAccount()
    completeEditProfile()
    signOutFromScreen()
    var uid = Firebase.auth.uid
    completeSignUp(email, pwd, isVet = true)
    checkEditProfileScreenIsDisplayed()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    assert(uid != Firebase.auth.uid)
    uid = Firebase.auth.uid
    signOutFromScreen()
    completeSignIn(email, pwd)
    checkOverviewScreenIsDisplayed()
    assert(uid == Firebase.auth.uid)
  }

  @Test
  fun testVet_SignIn_CreateOffice() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()
    val email = "vet@example.com"
    val pwd = "StrongPwd!123"

    completeSignUp(email, pwd, isVet = true)
    checkEditProfileScreenIsDisplayed()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    goToProfileFromOverview()
    checkManageOfficeWhenNoOffice()
    createOffice()
    leaveOffice()
  }

  // ----------- Scenario: Farmer -----------

  @Test
  fun testFarmer_SignIn_ClickReport_Back_Logout() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()

    checkWrongSignIn()
    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()
    goToProfileFromOverview()
    clickAddVetCode()
    goBack()
    clickEditProfile()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    createReport("Report title", "Report description", "Best Vet Ever!")
    clickFirstReportItem()
    reportViewClickViewOnMap()
    mapClickViewReport()
    goBack()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    signOutFromScreen()
  }

  @Test
  fun testFarmer_OverviewFilters_WorkCorrectly() {
    val vet1 = "Best Vet Ever!"
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()
    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()
    createReport("Report 1", "Description 1", "Best Vet Ever!")
    createReport("Report 2", "Description 2", "Meh Vet")

    // Report 1 appears when filtering for "Pending"
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

    signOutFromScreen()
  }

  @Test
  fun testFarmer_OverviewAlertCards_Navigation() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()

    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()

    composeTestRule.onNodeWithTag("ALERT_ITEM_0").assertIsDisplayed()

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.alertItemTag(1)).performClick()
    composeTestRule.onNodeWithTag("ALERT_ITEM_1").assertIsDisplayed()

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.alertItemTag(0)).performClick()
    composeTestRule.onNodeWithTag("ALERT_ITEM_0").assertIsDisplayed()

    signOutFromScreen()
  }

  @Test
  fun testVetFarmerLinkAndPasswordChange() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()
    val farmerEmail = "farmer.link@example.com"
    val password = "Password!123"
    val newPassword = "NewPassword!456"
    val vet =
        Vet(
            uid = "vet_001",
            firstname = "Dr",
            lastname = "Vet",
            email = "vet@test.com",
            address = null,
            validCodes = emptyList())
    val userViewModel = UserViewModel(initialUser = vet)
    val profileViewModel =
        ProfileViewModel(userViewModel, ConnectionRepositoryProvider.farmerToOfficeRepository)
    profileViewModel.generateVetCode()
    // Wait for the code to appear in StateFlow
    val vetCode = runBlocking { profileViewModel.generatedCode.first { it != null } }

    completeSignUp(farmerEmail, password, isVet = false)
    checkEditProfileScreenIsDisplayed()
    useCode(vetCode)
    checkLinkedVetIsNotEmpty()
    clickChangePassword()
    changePassword(password, newPassword)
  }

  // ----------- Helper functions -----------

  private fun completeSignUp(email: String, password: String, isVet: Boolean) {
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON)
        .assertIsDisplayed()
        .performClick()
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

  private fun useCode(vetCode: String?) {
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.CODE_FIELD)
        .assertIsDisplayed()
        .performTextInput((vetCode)!!)
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.ADD_CODE_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun checkManageOfficeWhenNoOffice() {
    openManageOfficeFromProfile()

    waitUntilTestTag(ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON)
  }

  private fun openManageOfficeFromProfile() {
    waitUntilTestTag(ProfileScreenTestTags.MANAGE_OFFICE_BUTTON)

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.MANAGE_OFFICE_BUTTON).performClick()

    waitUntilTestTag(ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON)
  }

  private fun createOffice() {
    waitUntilTestTag(ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON)

    composeTestRule.onNodeWithTag(ManageOfficeScreenTestTags.JOIN_OFFICE_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    waitUntilTestTag(CreateOfficeScreenTestTags.NAME_FIELD)

    composeTestRule
        .onNodeWithTag(CreateOfficeScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("Vet Office")
    composeTestRule.onNodeWithTag(CreateOfficeScreenTestTags.ADDRESS_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateOfficeScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()

    waitUntilTestTag(CreateOfficeScreenTestTags.CREATE_BUTTON)

    composeTestRule
        .onNodeWithTag(CreateOfficeScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    waitUntilTestTag(ManageOfficeScreenTestTags.OFFICE_NAME)
  }

  private fun leaveOffice() {
    waitUntilTestTag(ManageOfficeScreenTestTags.LEAVE_OFFICE_BUTTON)

    composeTestRule.onNodeWithTag(ManageOfficeScreenTestTags.LEAVE_OFFICE_BUTTON).performClick()

    waitUntilTestTag(ManageOfficeScreenTestTags.CONFIRM_LEAVE)

    composeTestRule.onNodeWithTag(ManageOfficeScreenTestTags.CONFIRM_LEAVE).performClick()

    waitUntilTestTag(ProfileScreenTestTags.PROFILE_IMAGE)
  }

  private fun checkLinkedVetIsNotEmpty() {
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  private fun clickChangePassword() {
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.PASSWORD_BUTTON)
        .assertIsDisplayed()
        .performClick()

    waitUntilTestTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
  }

  private fun changePassword(oldPassword: String, newPassword: String) {
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
        .assertIsDisplayed()
        .assertIsEnabled()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.OLD_PASSWORD)
        .performTextInput(oldPassword)
    composeTestRule.onNodeWithText(oldPassword).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ChangePasswordScreenTestTags.NEW_PASSWORD)
        .performTextInput(newPassword)
    composeTestRule.onNodeWithText(newPassword).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ChangePasswordScreenTestTags.SAVE_BUTTON).performClick()

    waitUntilTestTag(EditProfileScreenTestTags.FIRSTNAME_FIELD)
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

  private fun completeEditProfile() {
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("userFirstName")

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("userLastName")

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()

    waitUntilTestTag(ProfileScreenTestTags.PROFILE_IMAGE)
  }

  private fun checkIsGoogleAccount() {
    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).assertIsNotDisplayed()
  }

  private fun signOutFromScreen() {
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON)
        .assertIsDisplayed()
        .performClick()

    waitUntilTestTag(SignInScreenTestTags.SCREEN)
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

    // --- Answer all questions ---
    val scrollContainer = composeTestRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
    var index = 0
    while (true) {
      composeTestRule.waitForIdle()

      // OpenQuestion
      val openNode =
          composeTestRule
              .onAllNodesWithTag("QUESTION_${index}_OPEN")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (openNode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_OPEN"))
        composeTestRule.onNodeWithTag("QUESTION_${index}_OPEN").performTextInput("answer $index")
        index++
        continue
      }

      // YesOrNo
      val yesNode =
          composeTestRule
              .onAllNodesWithTag("QUESTION_${index}_YESORNO")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (yesNode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_YESORNO"))
        val options = composeTestRule.onAllNodesWithTag("QUESTION_${index}_YESORNO")
        options[0].performClick()
        index++
        continue
      }

      // MCQ
      val mcqNode =
          composeTestRule
              .onAllNodesWithTag("QUESTION_${index}_MCQ")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (mcqNode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_MCQ"))
        val options = composeTestRule.onAllNodesWithTag("QUESTION_${index}_MCQ")
        options[0].performClick()
        index++
        continue
      }

      // MCQO
      val mcqONode =
          composeTestRule
              .onAllNodesWithTag("QUESTION_${index}_MCQO")
              .fetchSemanticsNodes()
              .firstOrNull()
      if (mcqONode != null) {
        scrollContainer.performScrollToNode(hasTestTag("QUESTION_${index}_MCQO"))
        val options = composeTestRule.onAllNodesWithTag("QUESTION_${index}_MCQO")
        options[0].performClick()
        index++
        continue
      }
      break
    }

    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.VET_DROPDOWN))
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithText(vetId).assertIsDisplayed().performClick()

    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText(AddReportFeedbackTexts.SUCCESS).isDisplayed()
    }
    composeTestRule.onNodeWithText("OK").assertIsDisplayed().performClick()
  }

  private fun clickFirstReportItem() {
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)[0].isDisplayed()
    }
    composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)[0].performClick()
  }

  private fun checkOverviewScreenIsDisplayed() {
    waitUntilTestTag(OverviewScreenTestTags.SCREEN)
  }

  private fun checkEditProfileScreenIsDisplayed() {
    waitUntilTestTag(EditProfileScreenTestTags.FIRSTNAME_FIELD)
  }

  private fun goBack() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun reportViewClickViewOnMap() {
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(ReportViewScreenTestTags.VIEW_ON_MAP))
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.VIEW_ON_MAP)
        .assertIsDisplayed()
        .performClick()
    waitUntilTestTag(MapScreenTestTags.GOOGLE_MAP_SCREEN)
  }

  private fun mapClickViewReport() {
    waitUntilTestTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON)
    composeTestRule.onNodeWithTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON).performClick()
  }

  private fun goToProfileFromOverview() {
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.PROFILE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    waitUntilTestTag(ProfileScreenTestTags.PROFILE_IMAGE)
  }

  private fun clickAddVetCode() {
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.CODE_BUTTON_FARMER)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.CODE_FIELD).assertIsDisplayed()
  }

  private fun clickEditProfile() {
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).assertIsDisplayed()
  }

  private fun signInWithGoogle() {
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  private fun chooseRole() {
    waitUntilTestTag(RoleSelectionScreenTestTags.VET)
    composeTestRule.onNodeWithTag(RoleSelectionScreenTestTags.VET).performClick()
  }

  private fun checkWrongSignIn() {
    completeSignIn(user2.email, "12345678")
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText(SignInErrorMsg.INVALID_CREDENTIALS).isDisplayed()
    }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_FIELD).performTextClearance()
  }

  // To fix E2E test clicking on random report marker on map (without needing to know its ID)
  fun ComposeTestRule.clickFirstReportMarker() {
    waitUntil(TestConstants.LONG_TIMEOUT) {
      onAllNodes(hasTestTagThatStartsWith("reportMarker_")).fetchSemanticsNodes().isNotEmpty()
    }
    val allMarkers = onAllNodes(hasTestTagThatStartsWith("reportMarker_"))
    val markerNodes = allMarkers.fetchSemanticsNodes()
    if (markerNodes.isEmpty()) {
      throw AssertionError("No report markers found on map!")
    }
    allMarkers[0].performClick()
  }

  fun hasTestTagThatStartsWith(prefix: String): SemanticsMatcher {
    return SemanticsMatcher("${SemanticsProperties.TestTag.name} starts with $prefix") { node ->
      val tag = node.config.getOrNull(SemanticsProperties.TestTag)
      tag?.startsWith(prefix) == true
    }
  }
}
