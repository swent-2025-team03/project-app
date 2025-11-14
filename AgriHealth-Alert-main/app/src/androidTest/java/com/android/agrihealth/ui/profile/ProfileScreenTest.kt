package com.android.agrihealth.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.ui.authentification.SignInScreenTestTags.EMAIL_FIELD
import com.android.agrihealth.ui.authentification.SignInScreenTestTags.PASSWORD_FIELD
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.overview.OverviewScreenTestTags.LOGOUT_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.ADDRESS_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.CODE_BUTTON_FARMER
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.DEFAULT_VET_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.EDIT_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.NAME_TEXT
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.PROFILE_IMAGE
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.user.UserViewModel
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest : FirebaseEmulatorsTest() {

  @get:Rule val composeTestRule = createComposeRule()

  // Some Helpers

  private fun makeFakeViewModel(user: User?): UserViewModel {
    return UserViewModel(initialUser = user)
  }

  private fun setScreen(vm: UserViewModel) {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileScreen(userViewModel = vm, onGoBack = {}, onLogout = {}, onEditProfile = {})
      }
    }
  }

  // Test suite

  @Test
  fun topBar_displaysCorrectly() {
    val vm =
        makeFakeViewModel(
            Farmer("1", "Alice", "Johnson", "alice@farmmail.com", null, defaultVet = null))
    setScreen(vm)

    composeTestRule.onNodeWithTag(TOP_BAR).assertExists()
    composeTestRule.onNodeWithTag(GO_BACK_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(LOGOUT_BUTTON).assertExists()
  }

  @Test
  fun profileImage_isVisible() {
    val vm =
        makeFakeViewModel(
            Vet(
                uid = "2",
                firstname = "Bob",
                lastname = "Smith",
                email = "bob@vetcare.com",
                address = null))
    setScreen(vm)

    composeTestRule.onNodeWithTag(PROFILE_IMAGE).assertIsDisplayed()
  }

  @Test
  fun farmer_showsDefaultVetField() {
    val vm =
        makeFakeViewModel(
            Farmer(
                uid = "1",
                firstname = "Alice",
                lastname = "Johnson",
                email = "alice@farmmail.com",
                address = null,
                defaultVet = "vet123"))
    setScreen(vm)

    composeTestRule.onNodeWithTag(DEFAULT_VET_FIELD).assertExists()
  }

  @Test
  fun vet_hidesDefaultVetField() {
    val vm =
        makeFakeViewModel(
            Vet(
                uid = "2",
                firstname = "Bob",
                lastname = "Smith",
                email = "bob@vetcare.com",
                address = null))
    setScreen(vm)

    composeTestRule.onAllNodesWithTag(DEFAULT_VET_FIELD).assertCountEquals(0)
  }

  @Test
  fun addressField_isVisibleAndPopulated() {
    val vm =
        makeFakeViewModel(
            Farmer(
                "1", "Alice", "Johnson", "alice@farmmail.com", address = null, defaultVet = null))
    setScreen(vm)

    composeTestRule.onNodeWithTag(ADDRESS_FIELD).assertExists()
  }

    @Test
    fun descriptionField_isDisplayed() {
        val vm =
            makeFakeViewModel(
                Farmer(
                    uid = "1",
                    firstname = "Alice",
                    lastname = "Johnson",
                    email = "alice@farmmail.com",
                    address = null,
                    defaultVet = null,
                    description = "Test description"
                )
            )
        setScreen(vm)

        composeTestRule.onNodeWithTag(ProfileScreenTestTags.DESCRIPTION_FIELD).assertExists()
    }


    @Test
  fun emailAndPasswordFields_areDisplayed() {
    val vm =
        makeFakeViewModel(
            Farmer("1", "Alice", "Johnson", "alice@farmmail.com", null, defaultVet = null))
    setScreen(vm)

    composeTestRule.onNodeWithTag(EMAIL_FIELD).assertExists()
    composeTestRule.onNodeWithTag(PASSWORD_FIELD).assertExists()
  }

  @Test
  fun editButton_triggersCallback() {
    var clicked = false
    val vm =
        makeFakeViewModel(
            Farmer("1", "Alice", "Johnson", "alice@farmmail.com", null, defaultVet = null))

    composeTestRule.setContent {
      MaterialTheme { ProfileScreen(userViewModel = vm, onEditProfile = { clicked = true }) }
    }

    composeTestRule.onNodeWithTag(EDIT_BUTTON).performClick()
    assert(clicked)
  }

  @Test
  fun nullUser_safeFallbacks() {
    val vm =
        makeFakeViewModel(
            Farmer("1", "Alice", "Johnson", "alice@farmmail.com", null, defaultVet = null))
    setScreen(vm)

    composeTestRule.onNodeWithTag(NAME_TEXT).assertExists()
    composeTestRule.onNodeWithTag(EMAIL_FIELD).assertExists()
  }

  @Test
  fun allAccessibleElements_haveTags() {
    val vm =
        makeFakeViewModel(
            Farmer("1", "Alice", "Johnson", "alice@farmmail.com", null, defaultVet = null))
    setScreen(vm)

    composeTestRule.onNodeWithTag(PROFILE_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(GO_BACK_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(LOGOUT_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(EDIT_BUTTON).assertExists()
  }

  @Test
  fun codeButton_triggersCallbacks() {
    var clicked = false
    val vm =
        makeFakeViewModel(
            Farmer("1", "Alice", "Johnson", "alice@farmmail.com", null, defaultVet = null))

    composeTestRule.setContent {
      MaterialTheme { ProfileScreen(userViewModel = vm, onCodeFarmer = { clicked = true }) }
    }

    composeTestRule.onNodeWithTag(CODE_BUTTON_FARMER).performClick()
    assert(clicked)
  }
}
