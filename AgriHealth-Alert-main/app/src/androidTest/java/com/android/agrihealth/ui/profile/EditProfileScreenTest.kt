package com.android.agrihealth.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test

class EditProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Some Helpers

  private fun fakeFarmerViewModel(): UserViewModel {
    return object : UserViewModel() {
      private val fakeUserFlow =
          MutableStateFlow(
              Farmer(
                  uid = "farmer_1",
                  firstname = "Alice",
                  lastname = "Johnson",
                  email = "alice@farmmail.com",
                  address = Location(0.0, 0.0, "Farm Address"),
                  linkedVets = listOf("vet123", "vet456"),
                  defaultVet = "vet123"))

      override var user: StateFlow<User> = fakeUserFlow.asStateFlow()
    }
  }

  private fun fakeVetViewModel(): UserViewModel {
    return object : UserViewModel() {
      private val fakeUserFlow =
          MutableStateFlow(
              Vet(
                  uid = "vet_1",
                  firstname = "Bob",
                  lastname = "Smith",
                  email = "bob@vetcare.com",
                  address = Location(0.0, 0.0, "Clinic Address"),
                  linkedFarmers = listOf("farmer123", "farmer456"),
                  validCodes = listOf("112233", "445566")))

      override var user: StateFlow<User> = fakeUserFlow.asStateFlow()
    }
  }

  // Test suite

  @Test
  fun editProfileScreen_displaysBasicFields() {
    composeTestRule.setContent { EditProfileScreen(userViewModel = fakeFarmerViewModel()) }

    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.LASTNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.ADDRESS_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun editProfileScreen_showsFarmerSpecificFields() {
    composeTestRule.setContent { EditProfileScreen(userViewModel = fakeFarmerViewModel()) }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.CODE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.ADD_CODE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun editProfileScreen_showsVetSpecificFields() {
    composeTestRule.setContent { EditProfileScreen(userViewModel = fakeVetViewModel()) }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.ACTIVE_CODES_DROPDOWN)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN)
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.ADD_CODE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun saveButton_triggersSaveCallback() {
    var saved = false
    composeTestRule.setContent {
      EditProfileScreen(userViewModel = fakeFarmerViewModel(), onSave = { saved = true })
    }

    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.SAVE_BUTTON).performClick()
    assert(saved)
  }

    @Test
    fun activeCodes_showsListIfExpanded() {
        composeTestRule.setContent { EditProfileScreen(userViewModel = fakeVetViewModel()) }

        val codeNode = composeTestRule.onAllNodesWithTag(EditProfileScreenTestTags.ACTIVE_CODE_ELEMENT).onFirst()
        val codeNodeButton = composeTestRule.onAllNodesWithTag(EditProfileScreenTestTags.COPY_CODE_BUTTON).onFirst()
        val listNode = composeTestRule.onNodeWithTag(EditProfileScreenTestTags.ACTIVE_CODES_DROPDOWN)

        codeNode.assertIsNotDisplayed()
        codeNodeButton.assertIsNotDisplayed()

        listNode.assertIsDisplayed().performClick()

        codeNode.assertIsDisplayed()
        codeNodeButton.assertIsDisplayed().performClick()

        listNode.performClick()

        codeNode.assertIsNotDisplayed()
        codeNodeButton.assertIsNotDisplayed()
    }
}
