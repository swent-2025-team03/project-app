package com.android.agrihealth.ui.profile

import androidx.compose.ui.test.*
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import junit.framework.TestCase.assertTrue
import org.junit.Test

class EditProfileScreenTest : UITest() {
  val linkedOffices = listOf("off123", "off456")

  val farmer =
      TestUser.FARMER1.copy(linkedOffices = linkedOffices, defaultOffice = linkedOffices.first())
  val vet = TestUser.VET1.copy()

  private fun setContent(role: UserRole, onSave: () -> Unit = {}) {
    val initialUser =
        when (role) {
          UserRole.FARMER -> farmer
          UserRole.VET -> vet
        }

    setContent { EditProfileScreen(FakeUserViewModel(initialUser), onSave = { onSave() }) }
  }

  @Test
  fun editProfileScreen_showsFarmerSpecificFields() {
    val role = UserRole.FARMER
    setContent(role)
    assertComponentsVisibility(role)
  }

  @Test
  fun editProfileScreen_showsVetSpecificFields() {
    val role = UserRole.VET
    setContent(role)
    assertComponentsVisibility(role)
  }

  @Test
  fun saveButton_triggersSaveCallback() {
    var saved = false
    setContent(UserRole.FARMER, onSave = { saved = true })

    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.SAVE_BUTTON).performClick()
    assertTrue(saved)
  }

  @Test
  fun activeCodes_doNotShowIfEmptyList() {
    setContent(UserRole.VET)

    with(EditProfileScreenTestTags) {
      nodeNotDisplayed(dropdownTag("FARMER"))
      nodeNotDisplayed(dropdownTag("VET"))

      composeTestRule.onAllNodesWithTag(dropdownElementTag("FARMER")).assertAreNotDisplayed()
      composeTestRule.onAllNodesWithTag(dropdownElementTag("VET")).assertAreNotDisplayed()

      composeTestRule.onAllNodesWithTag(COPY_CODE_BUTTON).assertAreNotDisplayed()
    }
  }

  private fun SemanticsNodeInteractionCollection.assertAreNotDisplayed():
      SemanticsNodeInteractionCollection {
    fetchSemanticsNodes().forEachIndexed { index, _ -> get(index).assertIsNotDisplayed() }
    return this
  }

  fun assertComponentsVisibility(role: UserRole) {
    with(NavigationTestTags) { nodesAreDisplayed(TOP_BAR_TITLE, GO_BACK_BUTTON, TOP_BAR) }

    with(EditProfileScreenTestTags) {
      nodesAreDisplayed(
          FIRSTNAME_FIELD,
          LASTNAME_FIELD,
          ADDRESS_FIELD,
          PASSWORD_FIELD,
          DESCRIPTION_FIELD,
          SAVE_BUTTON)

      when (role) {
        UserRole.FARMER -> nodesAreDisplayed(DEFAULT_VET_DROPDOWN)
        UserRole.VET -> nodesNotDisplayed(DEFAULT_VET_DROPDOWN, ADD_CODE_BUTTON)
      }
    }
  }

  override fun displayAllComponents() {}
}
