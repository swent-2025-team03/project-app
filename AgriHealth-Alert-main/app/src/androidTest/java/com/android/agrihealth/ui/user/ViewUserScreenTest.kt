package com.android.agrihealth.ui.user

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import org.junit.Rule
import org.junit.Test

/** Fake ViewModel for deterministic UI state control. */
class FakeViewUserViewModel(initial: ViewUserUiState) : ViewUserViewModel(targetUserId = "fake") {
  private val _state: MutableState<ViewUserUiState> = mutableStateOf(initial)

  override val uiState: MutableState<ViewUserUiState>
    get() = _state

  override fun load(currentUser: com.android.agrihealth.data.model.user.User) {}

  fun setState(state: ViewUserUiState) {
    _state.value = state
  }
}

class ViewUserScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setScreen(
      vm: FakeViewUserViewModel,
      currentUser: com.android.agrihealth.data.model.user.User =
          Farmer(
              uid = "cur",
              firstname = "Current",
              lastname = "User",
              email = "cur@x.com",
              address = null,
              linkedVets = emptyList(),
              defaultVet = null)
  ) {
    val userVm = UserViewModel(initialUser = currentUser)

    composeTestRule.setContent {
      MaterialTheme { ViewUserScreen(viewModel = vm, userViewModel = userVm, onBack = {}) }
    }
  }

  //  --- Tests ---

  @Test
  fun topBar_displaysCorrectly() {
    val vm = FakeViewUserViewModel(ViewUserUiState.Loading)
    setScreen(vm)

    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.TOP_BAR).assertExists()
    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun loadingState_showsProgressIndicator() {
    val vm = FakeViewUserViewModel(ViewUserUiState.Loading)
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.LOADING_INDICATOR)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun errorState_displaysErrorText() {
    val vm = FakeViewUserViewModel(ViewUserUiState.Error("Test error"))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.ERROR_TEXT)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun successState_displaysContentColumn() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Patrick",
            lastname = "Bateman",
            email = "mail@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null,
            isGoogleAccount = false,
            description = null)

    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.CONTENT_COLUMN)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun profileIcon_isVisible() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Bruce",
            lastname = "Wayne",
            email = "mail@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null)
    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.PROFILE_ICON)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun nameField_displaysCorrectValue() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Ken",
            lastname = "Miles",
            email = "mail@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null)
    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.NAME_FIELD).assertTextContains("Ken Miles")
  }

  @Test
  fun roleField_displaysRoleCorrectly() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Trevor",
            lastname = "Reznik",
            email = "mail@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null)
    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.ROLE_FIELD).assertTextContains("Farmer")
  }

  @Test
  fun vetUser_showsOfficeField() {
    val vet =
        Vet(
            uid = "v1",
            firstname = "Alfred",
            lastname = "Borden",
            email = "vet@vet.com",
            address = null,
            validCodes = emptyList(),
            officeId = null,
            isGoogleAccount = false,
            description = null)

    val vm = FakeViewUserViewModel(ViewUserUiState.Success(vet))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.OFFICE_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun farmerUser_hidesOfficeField() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Christian",
            lastname = "Bale",
            email = "mail@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null)

    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule.onAllNodesWithTag(ViewUserScreenTestTags.OFFICE_FIELD).assertCountEquals(0)
  }

  @Test
  fun addressField_showsWhenAddressExists() {
    val farmer =
        Farmer(
            uid = "1",
            firstname = "No Inspiration",
            lastname = "Anymore",
            email = "mail@mail.com",
            address = Location(latitude = 0.0, longitude = 0.0),
            linkedVets = emptyList(),
            defaultVet = null)

    val vm = FakeViewUserViewModel(ViewUserUiState.Success(farmer))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.ADDRESS_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun descriptionField_showsWhenDescriptionPresent() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Alice",
            lastname = "Smith",
            email = "alice@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null,
            description = "Test description")

    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewUserScreenTestTags.DESCRIPTION_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun allKeyElements_haveTags() {
    val user =
        Farmer(
            uid = "1",
            firstname = "Alice",
            lastname = "Smith",
            email = "alice@mail.com",
            address = null,
            linkedVets = emptyList(),
            defaultVet = null)

    val vm = FakeViewUserViewModel(ViewUserUiState.Success(user))
    setScreen(vm)

    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.TOP_BAR).assertExists()
    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.PROFILE_ICON).assertExists()
    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.NAME_FIELD).assertExists()
    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.ROLE_FIELD).assertExists()
    composeTestRule.onNodeWithTag(ViewUserScreenTestTags.CONTENT_COLUMN).assertExists()
  }
}
