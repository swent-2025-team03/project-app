package com.android.agrihealth

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.FakeCredentialManager
import com.android.agrihealth.data.model.authentification.FakeJwtGenerator
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.LocationPickerTestTags
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.alert.AlertViewScreenTestTags
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
import com.android.agrihealth.ui.profile.CodeComposableComponentsTestTags
import com.android.agrihealth.ui.profile.CodesViewModel
import com.android.agrihealth.ui.profile.EditProfileScreenTestTags
import com.android.agrihealth.ui.profile.ProfileScreenTestTags
import com.android.agrihealth.ui.report.AddReportScreenTestTags
import com.android.agrihealth.ui.report.ReportViewScreenTestTags
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
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
                  android.Manifest.permission.ACCESS_COARSE_LOCATION,
                  android.Manifest.permission.POST_NOTIFICATIONS))
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
    completeSignUp("Test", "User", email, pwd, isVet = true)
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

    completeSignUp("Test", "User", email, pwd, isVet = true)
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
    createReport("Report title", "Report description", user1.defaultOffice ?: "")
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
    val fakeOfficeRepo = FakeOfficeRepository()
    OfficeRepositoryProvider.set(fakeOfficeRepo)

    val office2 =
        Office(id = user1.linkedOffices.last(), name = "Some Other Office", ownerId = user1.uid)

    runTest { fakeOfficeRepo.addOffice(office2) }

    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()
    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()
    createReport("Report 1", "Description 1", "Deleted Office")
    createReport("Report 2", "Description 2", user1.linkedOffices.last())

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
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OFFICE_ID_DROPDOWN).performClick()
    composeTestRule
        .onAllNodes(hasTestTagThatStartsWith("OPTION_"))
        .filter(hasText("Some Other Office"))
        .onFirst()
        .performClick()
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 2"))

    // Report 1 and 2 both appear when filtering for All
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OFFICE_ID_DROPDOWN).performClick()
    composeTestRule.onNodeWithTag("OPTION_All").performClick()
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 1"))
    composeTestRule
        .onAllNodesWithTag(OverviewScreenTestTags.REPORT_ITEM)
        .assertAny(hasText("Report 2"))

    signOutFromScreen()

    tearDown()
  }

  @Test
  fun testFarmer_OverviewAlertCards_Navigation() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()

    completeSignIn(user1.email, "12345678")
    checkOverviewScreenIsDisplayed()

    composeTestRule.onNodeWithTag("ALERT_ITEM_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.alertItemTag(0)).performClick()

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.containerTag(0)).assertIsDisplayed()

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.NEXT_ALERT_ARROW).performClick()
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.containerTag(1)).assertIsDisplayed()

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.PREVIOUS_ALERT_ARROW).performClick()
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.containerTag(0)).assertIsDisplayed()

    goBack()
    checkOverviewScreenIsDisplayed()
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
            farmerConnectCodes = emptyList(),
            vetConnectCodes = emptyList(),
            officeId = "off1")
    val userViewModel = UserViewModel(initialUser = vet)
    runTest {
      AuthRepositoryProvider.repository.signUpWithEmailAndPassword(vet.email, "123456", vet)
    }
    val codesViewModel =
        CodesViewModel(userViewModel, ConnectionRepositoryProvider.farmerToOfficeRepository)
    codesViewModel.generateCode()
    // Wait for the code to appear in StateFlow
    val officeCode = runBlocking { codesViewModel.generatedCode.first { it != null } }

    completeSignUp("Test", "User", farmerEmail, password, isVet = false)
    checkEditProfileScreenIsDisplayed()
    goBack()
    useCode(officeCode)
    checkLinkedVetIsNotEmpty()
    clickChangePassword()
    changePassword(password, newPassword)
  }

  @Test
  fun vetCreatesCodes_editProfileShowsFarmerDropdowns() {
    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()
    val email = "vet@test.com"
    val password = "123456"
    val vet =
        Vet(
            uid = "vet_001",
            firstname = "Dr",
            lastname = "Vet",
            email = email,
            address = null,
            farmerConnectCodes = emptyList(),
            vetConnectCodes = emptyList(),
            officeId = "off1")
    val userViewModel = UserViewModel(initialUser = vet)
    runTest {
      AuthRepositoryProvider.repository.signUpWithEmailAndPassword(vet.email, "123456", vet)
    }
    val codesViewModel =
        CodesViewModel(userViewModel, ConnectionRepositoryProvider.farmerToOfficeRepository)
    codesViewModel.generateCode()

    completeSignIn(email, password)
    checkOverviewScreenIsDisplayed()
    goToProfileFromOverview()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON)
        .assertIsDisplayed()
        .performClick()
    checkEditProfileScreenIsDisplayed()
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.dropdownTag("FARMER"))
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.dropdownElementTag("FARMER"))
        .assertIsDisplayed()
  }

  // ----------- Scenario: Both Users -----------

  @Test
  fun testCompleteConnectionFlow() {
    val vetEmail = "vet@email.com"
    val vet2Email = "vet2@email.com"
    val farmerEmail = "farm@email.com"

    val vetPassword = "vetvetvet"
    val vet2Password = "vet2vet2"
    val farmerPassword = "farmfarm"

    val clipboard =
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    composeTestRule.setContent { AgriHealthApp() }
    composeTestRule.waitForIdle()

    completeSignUp("Test", "Vet", vetEmail, vetPassword, true)
    checkEditProfileScreenIsDisplayed()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    goToProfileFromOverview()
    goToManageOffice()
    createOffice()
    goBack()
    generateFarmerCode()
    signOutFromScreen()

    val clipData = clipboard.primaryClip
    val copiedCode = clipData?.getItemAt(0)?.text.toString()

    completeSignUp("Test", "Farmer", farmerEmail, farmerPassword, false)
    checkEditProfileScreenIsDisplayed()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    goToProfileFromOverview()
    farmerJoinOffice(copiedCode)
    goBack()
    createReportWithOfficeName("Report 1", "Description 1", "Vet Office")
    createReportWithOfficeName("Report 2", "Description 2", "Vet Office")
    createReportWithOfficeName("Report 3", "Description 3", "Vet Office")
    signOutFromScreen()

    completeSignIn(vetEmail, vetPassword)
    claimReport("Report 1")
    goToProfileFromOverview()
    goToManageOffice()
    generateOfficeCode()
    goBack()
    signOutFromScreen()

    val clipData2 = clipboard.primaryClip
    val copiedCode2 = clipData2?.getItemAt(0)?.text.toString()

    completeSignUp("Test", "Vet2", vet2Email, vet2Password, true)
    checkEditProfileScreenIsDisplayed()
    goBack()
    goBack()
    checkOverviewScreenIsDisplayed()
    goToProfileFromOverview()
    goToManageOffice()
    vetJoinOffice(copiedCode2)
    goBack()
    goBack()
    claimReport("Report 2")
    checkAssignedVetDisplayedOnOverview()
  }

  // ----------- Helper functions -----------

  @After
  fun tearDown() {
    OfficeRepositoryProvider.reset()
  }

  private fun goToManageOffice() {
    waitUntilTestTag(ProfileScreenTestTags.MANAGE_OFFICE_BUTTON)
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.MANAGE_OFFICE_BUTTON).performClick()
  }

  private fun generateFarmerCode() {
    waitUntilTestTag(ProfileScreenTestTags.GENERATE_CODE_BUTTON)
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.GENERATE_CODE_BUTTON).performClick()
    waitUntilTestTag(CodeComposableComponentsTestTags.COPY_CODE)
    composeTestRule.onNodeWithTag(CodeComposableComponentsTestTags.COPY_CODE).performClick()
  }

  private fun generateOfficeCode() {
    waitUntilTestTag(CodeComposableComponentsTestTags.GENERATE_BUTTON)
    composeTestRule.onNodeWithTag(CodeComposableComponentsTestTags.GENERATE_BUTTON).performClick()
    waitUntilTestTag(CodeComposableComponentsTestTags.COPY_CODE)
    composeTestRule.onNodeWithTag(CodeComposableComponentsTestTags.COPY_CODE).performClick()
  }

  private fun farmerJoinOffice(farmerCode: String) {
    waitUntilTestTag(ProfileScreenTestTags.CODE_BUTTON_FARMER)
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.CODE_BUTTON_FARMER).performClick()

    waitUntilTestTag(CodeComposableComponentsTestTags.ADD_CODE_BUTTON)
    composeTestRule
        .onNodeWithTag(CodeComposableComponentsTestTags.CODE_FIELD)
        .assertIsDisplayed()
        .performTextInput(farmerCode)
    composeTestRule.onNodeWithTag(CodeComposableComponentsTestTags.ADD_CODE_BUTTON).performClick()

    goBack()
    waitUntilTestTag(ProfileScreenTestTags.PROFILE_IMAGE)
  }

  private fun vetJoinOffice(vetCode: String) {
    waitUntilTestTag(ManageOfficeScreenTestTags.JOIN_OFFICE_BUTTON)
    composeTestRule.onNodeWithTag(ManageOfficeScreenTestTags.JOIN_OFFICE_BUTTON).performClick()

    waitUntilTestTag(CodeComposableComponentsTestTags.ADD_CODE_BUTTON)
    composeTestRule
        .onNodeWithTag(CodeComposableComponentsTestTags.CODE_FIELD)
        .assertIsDisplayed()
        .performTextInput(vetCode)
    composeTestRule.onNodeWithTag(CodeComposableComponentsTestTags.ADD_CODE_BUTTON).performClick()

    goBack()
  }

  private fun claimReport(reportName: String) {
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText(reportName).isDisplayed()
    }
    composeTestRule.onNodeWithText(reportName).performClick()

    waitUntilTestTag(ReportViewScreenTestTags.ROLE_INFO_LINE)
    composeTestRule
        .onNodeWithTag(ReportViewScreenTestTags.SCROLL_CONTAINER)
        .performScrollToNode(hasTestTag(ReportViewScreenTestTags.CLAIM_BUTTON))
    composeTestRule.onNodeWithTag(ReportViewScreenTestTags.CLAIM_BUTTON).performClick()

    goBack()

    // TODO remove once discard change is fixed
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText("Discard changes").isDisplayed()
    }
    composeTestRule.onNodeWithText("Discard changes").performClick()

    waitUntilTestTag(OverviewScreenTestTags.SCREEN)
  }

  private fun checkAssignedVetDisplayedOnOverview() {
    val vet = "Test Vet"
    val assignedToYou = "Assigned to You"

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText(vet).isDisplayed()
    }
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText(assignedToYou).isDisplayed()
    }
  }

  private fun completeSignUp(
      firstname: String,
      lastname: String,
      email: String,
      password: String,
      isVet: Boolean
  ) {
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_UP_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput(firstname)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD)
        .assertIsDisplayed()
        .performTextInput(lastname)

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

  private fun useCode(officeCode: String?) {
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.CODE_BUTTON_FARMER)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(CodeComposableComponentsTestTags.CODE_FIELD)
        .assertIsDisplayed()
        .performTextInput((officeCode)!!)
    composeTestRule
        .onNodeWithTag(CodeComposableComponentsTestTags.ADD_CODE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
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
    waitUntilTestTag(ProfileScreenTestTags.EDIT_BUTTON)
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
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

    waitUntilTestTag(OverviewScreenTestTags.SCREEN)
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
    waitUntilTestTag(OverviewScreenTestTags.LOGOUT_BUTTON)
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON).performClick()

    waitUntilTestTag(SignInScreenTestTags.SCREEN)
  }

  private fun createReportWithOfficeName(title: String, description: String, officeName: String) {
    waitUntilTestTag(OverviewScreenTestTags.ADD_REPORT_BUTTON)
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).performClick()

    waitUntilTestTag(AddReportScreenTestTags.TITLE_FIELD)
    composeTestRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput(title)
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

    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.LOCATION_BUTTON))
    composeTestRule.onNodeWithTag(AddReportScreenTestTags.LOCATION_BUTTON).performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithTag(LocationPickerTestTags.SELECT_LOCATION_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(LocationPickerTestTags.SELECT_LOCATION_BUTTON).performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithTag(LocationPickerTestTags.CONFIRMATION_PROMPT).isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(LocationPickerTestTags.PROMPT_CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER).isDisplayed()
    }
    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.OFFICE_DROPDOWN))
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithText(officeName).isDisplayed()
    }

    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_SUCCESS).isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.DIALOG_SUCCESS_OK)
        .assertIsDisplayed()
        .performClick()

    waitUntilTestTag(OverviewScreenTestTags.SCREEN)
  }

  private fun createReport(title: String, description: String, officeId: String) {
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

    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.LOCATION_BUTTON))
    composeTestRule.onNodeWithTag(AddReportScreenTestTags.LOCATION_BUTTON).performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithTag(LocationPickerTestTags.SELECT_LOCATION_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(LocationPickerTestTags.SELECT_LOCATION_BUTTON).performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithTag(LocationPickerTestTags.CONFIRMATION_PROMPT).isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(LocationPickerTestTags.PROMPT_CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule.onNodeWithTag(AddReportScreenTestTags.SCROLL_CONTAINER).isDisplayed()
    }
    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.OFFICE_DROPDOWN))
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.OFFICE_DROPDOWN)
        .assertIsDisplayed()
        .performClick()
    runTest {
      val officeName =
          OfficeRepositoryProvider.get().getOffice(officeId).fold({ off -> off.name }) {
            "Deleted office"
          }
      composeTestRule.onAllNodesWithText(officeName).onFirst().performClick()
    }

    scrollContainer.performScrollToNode(hasTestTag(AddReportScreenTestTags.CREATE_BUTTON))
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(AddReportScreenTestTags.DIALOG_SUCCESS).isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(AddReportScreenTestTags.DIALOG_SUCCESS_OK)
        .assertIsDisplayed()
        .performClick()
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
    composeTestRule.onNodeWithTag(CodeComposableComponentsTestTags.CODE_FIELD).assertIsDisplayed()
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
