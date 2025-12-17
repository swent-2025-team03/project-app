package com.android.agrihealth.ui.profile

import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.authentification.SignInScreenTestTags.EMAIL_FIELD
import com.android.agrihealth.ui.authentification.SignInScreenTestTags.PASSWORD_FIELD
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.overview.OverviewScreenTestTags.LOGOUT_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ProfileScreenTest : UITest() {
  private fun setContentWithVM(role: UserRole, onEdit: () -> Unit = {}, onCode: () -> Unit = {}) {
    val user =
        when (role) {
          UserRole.FARMER -> TestUser.FARMER1.copy()
          UserRole.VET -> TestUser.VET1.copy()
        }

    setContent {
      ProfileScreen(
          userViewModel = FakeUserViewModel(user),
          onEditProfile = { onEdit() },
          onCodeFarmer = { onCode() })
    }
  }

  @Test
  fun profileScreen_showsFarmerSpecificFields_checkButtonReactivity() {
    val role = UserRole.FARMER
    var codeClicked = false

    setContentWithVM(role, onCode = { codeClicked = true })

    assertComponentsVisibility(role)

    clickOn(ProfileScreenTestTags.CODE_BUTTON_FARMER)
    assertTrue(codeClicked)
  }

  @Test
  fun profileScreen_showsVetSpecificFields_checkButtonReactivity() {
    val role = UserRole.VET
    var editClicked = false

    setContentWithVM(role, onEdit = { editClicked = true })

    assertComponentsVisibility(role)

    with(ProfileScreenTestTags) {
      clickOn(EDIT_BUTTON)

      assertTrue(editClicked)
    }
  }

  private fun assertComponentsVisibility(role: UserRole) {
    with(NavigationTestTags) {
      nodesAreDisplayed(TOP_BAR, TOP_BAR_TITLE, GO_BACK_BUTTON, LOGOUT_BUTTON)
    }

    with(ProfileScreenTestTags) {
      nodesAreDisplayed(
          PROFILE_PICTURE,
          ADDRESS_FIELD,
          DESCRIPTION_FIELD,
          EMAIL_FIELD,
          PASSWORD_FIELD,
          EDIT_BUTTON)

      when (role) {
        UserRole.FARMER -> nodesAreDisplayed(DEFAULT_OFFICE_FIELD)
        UserRole.VET -> {
          nodesAreDisplayed(MANAGE_OFFICE_BUTTON)
          nodesNotDisplayed(DEFAULT_OFFICE_FIELD)
        }
      }
    }
  }

  override fun displayAllComponents() {}
}
