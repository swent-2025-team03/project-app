package com.android.agrihealth.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.ui.navigation.NavigationTestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ResetPasswordTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  fun setContent(onBack: () -> Unit = {}, status: EmailSendStatus = EmailSendStatus.Success) {
    val vm = ResetPasswordViewModel(FakeAuthRepository(resetPasswordResult = status))
    composeTestRule.setContent { ResetPasswordScreen(onBack, vm) }
  }

  fun assertFeedBackBox(status: EmailSendStatus) {
    val tagMap =
        mapOf(
            EmailSendStatus.Success to ResetPasswordScreenTestTags.SUCCESS_FEEDBACK,
            EmailSendStatus.Fail to ResetPasswordScreenTestTags.FAIL_FEEDBACK,
            EmailSendStatus.Waiting to ResetPasswordScreenTestTags.WAITING_FEEDBACK,
        )

    tagMap.forEach { (s, tag) ->
      val node = composeTestRule.onNodeWithTag(tag)
      if (s == status) node.assertIsDisplayed() else node.assertDoesNotExist()
    }
  }

  fun SemanticsNodeInteraction.assertIsError() =
      assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))

  fun SemanticsNodeInteraction.assertIsNotError() =
      assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))

  fun sendResetPasswordRequest() {
    composeTestRule
        .onNodeWithTag(ResetPasswordScreenTestTags.EMAIL)
        .performTextInput("email.test@test.com")
    composeTestRule
        .onNodeWithTag(ResetPasswordScreenTestTags.SEND_RESET_EMAIL_BUTTON)
        .performClick()
  }

  fun testFeedbackBoxForStatus(status: EmailSendStatus) {
    setContent(status = status)
    sendResetPasswordRequest()
    assertFeedBackBox(status)
  }

  @Test
  fun componentsAreDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ResetPasswordScreenTestTags.INSTRUCTION_TEXT)
    composeTestRule.onNodeWithTag(ResetPasswordScreenTestTags.EMAIL)
    composeTestRule.onNodeWithTag(ResetPasswordScreenTestTags.SEND_RESET_EMAIL_BUTTON)
    assertFeedBackBox(EmailSendStatus.None)
  }

  @Test
  fun emailShowsErrorOnEmailMalformed() {
    setContent()
    val node = composeTestRule.onNodeWithTag(ResetPasswordScreenTestTags.EMAIL)
    node.assertIsNotError()
    node.performTextInput("mail")
    composeTestRule
        .onNodeWithTag(ResetPasswordScreenTestTags.SEND_RESET_EMAIL_BUTTON)
        .performClick()
    node.assertIsError()
    node.performTextInput("mail@test.com")
    composeTestRule
        .onNodeWithTag(ResetPasswordScreenTestTags.SEND_RESET_EMAIL_BUTTON)
        .performClick()
    node.assertIsNotError()
  }

  @Test
  fun backButtonTest() {
    var goBackCalled = false
    setContent({ goBackCalled = true })
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).performClick()
    assertTrue(goBackCalled)
  }

  @Test
  fun feedbackBoxShowsWaiting() {
    testFeedbackBoxForStatus(EmailSendStatus.Waiting)
  }

  @Test
  fun feedbackBoxShowsSuccess() {
    testFeedbackBoxForStatus(EmailSendStatus.Success)
  }

  @Test
  fun feedbackBoxShowsFail() {
    testFeedbackBoxForStatus(EmailSendStatus.Fail)
  }
}
