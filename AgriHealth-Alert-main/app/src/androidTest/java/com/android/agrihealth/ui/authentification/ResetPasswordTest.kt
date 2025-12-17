package com.android.agrihealth.ui.authentification

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetPasswordTest : UITest() {

  fun setContent(onBack: () -> Unit = {}, status: EmailSendStatus = EmailSendStatus.Success) {
    val vm = ResetPasswordViewModel(FakeAuthRepository(resetPasswordResult = status))
    setContent { ResetPasswordScreen(onBack, vm) }
  }

  fun assertFeedBackBox(status: EmailSendStatus) {
    val tagMap =
        with(ResetPasswordScreenTestTags) {
          mapOf(
              EmailSendStatus.Success to SUCCESS_FEEDBACK,
              EmailSendStatus.Fail to FAIL_FEEDBACK,
              EmailSendStatus.Waiting to WAITING_FEEDBACK,
          )
        }

    tagMap.forEach { (s, tag) ->
      val node = node(tag)
      if (s == status) node.assertIsDisplayed() else node.assertDoesNotExist()
    }
  }

  fun SemanticsNodeInteraction.assertIsError() =
      assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))

  fun SemanticsNodeInteraction.assertIsNotError() =
      assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))

  fun sendResetPasswordRequest() {
    with(ResetPasswordScreenTestTags) {
      writeIn(EMAIL, "email.text@test.com")
      clickOn(SEND_RESET_EMAIL_BUTTON)
    }
  }

  fun testFeedbackBoxForStatus(status: EmailSendStatus) {
    setContent(status = status)
    sendResetPasswordRequest()
    assertFeedBackBox(status)
  }

  @Test
  override fun displayAllComponents() {
    setContent()

    with(ResetPasswordScreenTestTags) {
      nodesAreDisplayed(INSTRUCTION_TEXT, EMAIL, SEND_RESET_EMAIL_BUTTON)
    }

    assertFeedBackBox(EmailSendStatus.None)
  }

  @Test
  fun emailShowsErrorOnEmailMalformed() {
    setContent()

    with(ResetPasswordScreenTestTags) {
      val emailField = node(EMAIL)

      emailField.assertIsNotError().performTextInput("mail")
      clickOn(SEND_RESET_EMAIL_BUTTON)
      emailField.assertIsError()

      emailField.performTextInput("mail@test.com")
      clickOn(SEND_RESET_EMAIL_BUTTON)
      emailField.assertIsNotError()
    }
  }

  @Test
  fun backButtonTest() {
    var goBackCalled = false
    setContent(onBack = { goBackCalled = true })
    clickOn(NavigationTestTags.GO_BACK_BUTTON)
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
