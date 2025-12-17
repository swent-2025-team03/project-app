package com.android.agrihealth.ui.profile

import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestPassword.PASSWORD1
import com.android.agrihealth.testhelpers.TestPassword.PASSWORD2
import com.android.agrihealth.testhelpers.TestPassword.WEAK_PASSWORD
import com.android.agrihealth.testhelpers.TestTimeout
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.testhelpers.templates.UITest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class ChangePasswordScreenTest : UITest() {

  var success = false
  val oldPassword = PASSWORD2
  val newPassword = PASSWORD1

  @Before
  fun setup() {
    success = false
  }

  private fun setContentWithVM(
      vm: ChangePasswordViewModelContract = FakeChangePasswordViewModel(oldPassword),
      userEmail: String = ""
  ) {
    setContent {
      ChangePasswordScreen(
          onBack = {},
          userEmail = userEmail,
          onUpdatePassword = { success = true },
          changePasswordViewModel = vm)
    }
  }

  @Test
  override fun displayAllComponents() {
    setContentWithVM()

    with(ChangePasswordScreenTestTags) {
      nodesAreDisplayed(OLD_PASSWORD, NEW_PASSWORD, SAVE_BUTTON)
    }
  }

  @Test
  fun checkFailureAndSuccess_emptyWrongOldWeakNew_validOldAndNew() {
    setContentWithVM()

    with(ChangePasswordFeedbackTexts) {
      fillPasswordFieldsAndSubmit("", "")
      textIsDisplayed(NEW_WEAK)
      textNotDisplayed(OLD_WRONG)
      assertFalse(success)

      fillPasswordFieldsAndSubmit("", newPassword)
      textNotDisplayed(NEW_WEAK)
      textIsDisplayed(OLD_WRONG)
      assertFalse(success)

      composeTestRule.waitForIdle()
      fillPasswordFieldsAndSubmit(oldPassword, WEAK_PASSWORD)
      textIsDisplayed(NEW_WEAK)
      textNotDisplayed(OLD_WRONG)
      assertFalse(success)

      fillPasswordFieldsAndSubmit(oldPassword, newPassword)
      textNotDisplayed(NEW_WEAK)
      textNotDisplayed(OLD_WRONG)
      assertTrue(success)
    }
  }

  @Test
  fun changePassword_showsAndHidesLoadingOverlay() {
    val fakeRepo = FakeAuthRepository(delayMs = TestTimeout.SHORT_TIMEOUT)
    val viewModel = ChangePasswordViewModel(repository = fakeRepo)

    setContentWithVM(vm = viewModel)

    fillPasswordFieldsAndSubmit(oldPassword, newPassword)

    composeTestRule.assertOverlayDuringLoading(isLoading = { viewModel.uiState.value.isLoading })
  }

  private fun fillPasswordFieldsAndSubmit(old: String, new: String) {
    with(ChangePasswordScreenTestTags) {
      writeIn(OLD_PASSWORD, old, reset = true)
      writeIn(NEW_PASSWORD, new, reset = true)
      clickOn(SAVE_BUTTON)
    }
  }
}
