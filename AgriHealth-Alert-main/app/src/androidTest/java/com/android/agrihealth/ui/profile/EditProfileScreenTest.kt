package com.android.agrihealth.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.navigation.NavigationTestTags.TOP_BAR_TITLE
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import org.junit.Rule
import org.junit.Test

class EditProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Some Helpers
  val officeCodes = listOf("112233", "445566")
  val linkedOffices = listOf("off123", "off456")

  private fun fakeFarmerViewModel(): FakeUserViewModel {
    return FakeUserViewModel(
        Farmer(
            uid = "farmer_1",
            firstname = "Alice",
            lastname = "Johnson",
            email = "alice@farmmail.com",
            address = Location(0.0, 0.0, "Farm Address"),
            linkedOffices = linkedOffices,
            defaultOffice = linkedOffices.first()))
  }

  private fun fakeVetViewModel(
      farmerCodes: List<String>,
      vetCodes: List<String>
  ): FakeUserViewModel {
    return FakeUserViewModel(
        Vet(
            uid = "vet_1",
            firstname = "Bob",
            lastname = "Smith",
            email = "bob@vetcare.com",
            address = Location(0.0, 0.0, "Clinic Address"),
            farmerConnectCodes = farmerCodes,
            vetConnectCodes = vetCodes))
  }

  @Test
  fun editProfileScreen_displaysBasicFields() {
    composeTestRule.setContent { EditProfileScreen(userViewModel = fakeFarmerViewModel()) }

    composeTestRule.onNodeWithTag(TOP_BAR_TITLE).assertExists()
    composeTestRule.onNodeWithTag(GO_BACK_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(TOP_BAR).assertExists()

    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.FIRSTNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.LASTNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.ADDRESS_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun editProfileScreen_showsFarmerSpecificFields() {
    composeTestRule.setContent { EditProfileScreen(userViewModel = fakeFarmerViewModel()) }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN)
        .assertIsDisplayed()
  }

  @Test
  fun editProfileScreen_showsVetSpecificFields() {
    composeTestRule.setContent {
      EditProfileScreen(userViewModel = fakeVetViewModel(officeCodes, officeCodes))
    }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.dropdownTag("FARMER"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileScreenTestTags.dropdownTag("VET")).assertIsDisplayed()
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

  private fun SemanticsNodeInteractionCollection.assertAreDisplayed():
      SemanticsNodeInteractionCollection {
    fetchSemanticsNodes().forEachIndexed { index, _ -> get(index).assertIsDisplayed() }
    return this
  }

  private fun SemanticsNodeInteractionCollection.assertAreNotDisplayed():
      SemanticsNodeInteractionCollection {
    fetchSemanticsNodes().forEachIndexed { index, _ -> get(index).assertIsNotDisplayed() }
    return this
  }

  @Test
  fun activeFarmerCodes_showsListIfExpanded() {
    composeTestRule.setContent {
      EditProfileScreen(userViewModel = fakeVetViewModel(officeCodes, officeCodes))
    }

    val codeNodes =
        composeTestRule.onAllNodesWithTag(EditProfileScreenTestTags.dropdownElementTag("FARMER"))
    val codeButtonNodes =
        composeTestRule.onAllNodesWithTag(EditProfileScreenTestTags.COPY_CODE_BUTTON)
    val listNode = composeTestRule.onNodeWithTag(EditProfileScreenTestTags.dropdownTag("FARMER"))

    codeNodes.assertAreNotDisplayed()
    codeButtonNodes.assertAreNotDisplayed()

    listNode.assertIsDisplayed().performClick()

    codeNodes.assertAreDisplayed()
    codeButtonNodes.assertAreDisplayed().onFirst().performClick()

    composeTestRule.waitUntil {
      composeTestRule.onAllNodesWithText("Copied to clipboard").fetchSemanticsNodes().isNotEmpty()
    }

    listNode.performClick()

    codeNodes.assertAreNotDisplayed()
    codeButtonNodes.assertAreNotDisplayed()
  }

  @Test
  fun activeVetCodes_showsListIfExpanded() {
    composeTestRule.setContent {
      EditProfileScreen(userViewModel = fakeVetViewModel(officeCodes, officeCodes))
    }

    val codeNodes =
        composeTestRule.onAllNodesWithTag(EditProfileScreenTestTags.dropdownElementTag("VET"))
    val codeButtonNodes =
        composeTestRule.onAllNodesWithTag(EditProfileScreenTestTags.COPY_CODE_BUTTON)
    val listNode = composeTestRule.onNodeWithTag(EditProfileScreenTestTags.dropdownTag("VET"))

    codeNodes.assertAreNotDisplayed()
    codeButtonNodes.assertAreNotDisplayed()

    listNode.assertIsDisplayed().performClick()

    codeNodes.assertAreDisplayed()
    codeButtonNodes.assertAreDisplayed().onFirst().performClick()

    composeTestRule.waitUntil {
      composeTestRule.onAllNodesWithText("Copied to clipboard").fetchSemanticsNodes().isNotEmpty()
    }

    listNode.performClick()

    codeNodes.assertAreNotDisplayed()
    codeButtonNodes.assertAreNotDisplayed()
  }

  @Test
  fun activeFarmerCodes_doesNotShowIfEmptyList() {
    composeTestRule.setContent {
      EditProfileScreen(userViewModel = fakeVetViewModel(listOf(), officeCodes))
    }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.dropdownTag("FARMER"))
        .assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag(EditProfileScreenTestTags.dropdownElementTag("FARMER"))
        .assertAreNotDisplayed()
    composeTestRule
        .onAllNodesWithTag(EditProfileScreenTestTags.COPY_CODE_BUTTON)
        .assertAreNotDisplayed()
  }

  @Test
  fun activeVetCodes_doesNotShowIfEmptyList() {
    composeTestRule.setContent {
      EditProfileScreen(userViewModel = fakeVetViewModel(officeCodes, listOf()))
    }

    composeTestRule
        .onNodeWithTag(EditProfileScreenTestTags.dropdownTag("VET"))
        .assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag(EditProfileScreenTestTags.dropdownElementTag("VET"))
        .assertAreNotDisplayed()
    composeTestRule
        .onAllNodesWithTag(EditProfileScreenTestTags.COPY_CODE_BUTTON)
        .assertAreNotDisplayed()
  }
}
