package com.android.agrihealth.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.credentials.Credential
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.loading.LoadingTestTags
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpScreenTest : FirebaseEmulatorsTest() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ---------- Helpers de configuration d’écran ----------

    /** SignUpScreen avec le AuthRepository “normal” (celui des emulators). */
    private fun setDefaultSignUpScreen() {
        composeTestRule.setContent {
            MaterialTheme { SignUpScreen() }
        }
    }

    /** SignUpScreen avec un AuthRepository custom (ex: fake lent). */
    private fun setSignUpScreenWithRepo(authRepository: AuthRepository) {
        val vm = object : SignUpViewModel(authRepository) {}
        composeTestRule.setContent {
            MaterialTheme { SignUpScreen(signUpViewModel = vm) }
        }
    }

    private fun setNetworkEnabled(enabled: Boolean) {
        val state = if (enabled) "enable" else "disable"
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        uiAutomation.executeShellCommand("svc wifi $state").close()
        uiAutomation.executeShellCommand("svc data $state").close()
    }

    private fun completeSignUp(email: String, password: String) {
        // suppose que setDefaultSignUpScreen() ou setSignUpScreenWithRepo() a déjà été appelé
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

    // ---------- Setup emulators (sans setContent) ----------

    @Before
    override fun setUp() {
        super.setUp()
        runTest {
            authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
            authRepository.signOut()
        }
        // IMPORTANT : plus de setContent ici
    }

    // ---------- Tests ----------

    @Test
    fun displayAllComponents() {
        setDefaultSignUpScreen()

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
        setDefaultSignUpScreen()

        composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()
        composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onNodeWithText(SignUpErrorMsg.EMPTY_FIELDS).isDisplayed()
        }
    }

    @Test
    fun signUpWithoutRoleFails() {
        setDefaultSignUpScreen()

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
            .performTextInput(user2.email)

        composeTestRule
            .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
            .assertIsDisplayed()
            .performTextInput(password2)

        composeTestRule
            .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
            .assertIsDisplayed()
            .performTextInput(password2)

        composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).performClick()

        composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onNodeWithText(SignUpErrorMsg.ROLE_NULL).isDisplayed()
        }
    }

    @Test
    fun signUpWithMalformedEmailFails() {
        setDefaultSignUpScreen()

        completeSignUp(user4.email, password4)
        composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onNodeWithText(SignUpErrorMsg.BAD_EMAIL_FORMAT).isDisplayed()
        }
    }

    @Test
    fun signUpWithWeakPasswordFails() {
        setDefaultSignUpScreen()

        completeSignUp(user3.email, password4)
        composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onNodeWithText(SignUpErrorMsg.WEAK_PASSWORD).isDisplayed()
        }
    }

    @Test
    fun signUpWithMismatchedPasswordsFails() {
        setDefaultSignUpScreen()

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
            .performTextInput(user2.email)

        composeTestRule
            .onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
            .assertIsDisplayed()
            .performTextInput(password2)

        composeTestRule
            .onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
            .assertIsDisplayed()
            .performTextInput(password3)

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
        setDefaultSignUpScreen()

        completeSignUp(user1.email, password3)
        composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onNodeWithText(SignUpErrorMsg.ALREADY_USED_EMAIL).isDisplayed()
        }
    }

    @Test
    fun signUpWithoutInternetFails() {
        setDefaultSignUpScreen()

        setNetworkEnabled(false)
        completeSignUp(user2.email, password2)
        composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
            composeTestRule.onNodeWithText(SignUpErrorMsg.TIMEOUT).isDisplayed()
        }
    }

    @Test
    fun signUp_showsLoadingOverlayWhileSigningUp() {
        // Ici on veut un repo lent mais contrôlé -> fake local, pas l’emulator
        val slowRepo = SlowFakeAuthRepository(delayMs = 1200)
        setSignUpScreenWithRepo(slowRepo)

        // Remplir correctement le formulaire
        composeTestRule.onNodeWithTag(SignUpScreenTestTags.FIRSTNAME_FIELD)
            .performTextInput("Nico")
        composeTestRule.onNodeWithTag(SignUpScreenTestTags.LASTNAME_FIELD)
            .performTextInput("Berlin")
        composeTestRule.onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD)
            .performTextInput("test+loading@agrihealth.com")
        composeTestRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD)
            .performTextInput("123456")
        composeTestRule.onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput("123456")
        composeTestRule.onNodeWithTag(SignUpScreenTestTags.FARMER_PILL)
            .performClick()

        composeTestRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON)
            .performClick()

        // 1) Le scrim doit apparaître
        composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
            composeTestRule.onAllNodesWithTag(LoadingTestTags.SCRIM)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()

        // 2) Puis disparaître après la “réponse” du repo
        composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
            composeTestRule.onAllNodesWithTag(LoadingTestTags.SCRIM)
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeTestRule.onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    }

    @After
    fun turnOnInternet() {
        setNetworkEnabled(true)
    }
}

// Helper local si la version de compose-test ne fournit pas assertDoesNotExist
fun SemanticsNodeInteraction.assertDoesNotExist() {
    try {
        this.assertExists()
        org.junit.Assert.fail("Le noeud existe mais ne devrait pas.")
    } catch (_: AssertionError) { }
}

// Fake repo lent utilisé uniquement par signUp_showsLoadingOverlayWhileSigningUp
class SlowFakeAuthRepository(
    private val delayMs: Long = 1200
) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> = TODO("not needed in this test")

    override suspend fun reAuthenticate(
        email: String,
        password: String
    ): Result<Unit> = TODO("not needed in this test")

    override suspend fun changePassword(password: String): Result<Unit> =
        TODO("not needed in this test")

    override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> =
        TODO("not needed in this test")

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        userData: User
    ): Result<FirebaseUser> {
        kotlinx.coroutines.delay(delayMs)          // simulate slow network
        return Result.failure(Exception("network error"))
    }

    override fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> =
        TODO("not needed in this test")
}