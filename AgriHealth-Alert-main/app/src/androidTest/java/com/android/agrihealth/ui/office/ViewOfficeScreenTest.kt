package com.android.agrihealth.ui.office

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.ui.user.UserViewModel
import org.junit.Rule
import org.junit.Test

class FakeViewOfficeViewModel(initial: ViewOfficeUiState) :
    ViewOfficeViewModel(targetOfficeId = "fake", officeRepository = FakeOfficeRepository()) {

  private val _state: MutableState<ViewOfficeUiState> = mutableStateOf(initial)

  override val uiState: MutableState<ViewOfficeUiState>
    get() = _state

  override fun load() {}

  fun setState(state: ViewOfficeUiState) {
    _state.value = state
  }
}

class ViewOfficeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setScreen(vm: FakeViewOfficeViewModel) {
    val userVm = UserViewModel()

    composeTestRule.setContent {
      MaterialTheme { ViewOfficeScreen(viewModel = vm, userViewModel = userVm, onBack = {}) }
    }
  }

  @Test
  fun topBar_displaysCorrectly() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Loading)
    setScreen(vm)

    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.TOP_BAR).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.BACK_BUTTON).assertExists()
  }

  @Test
  fun loadingState_showsProgressIndicator() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Loading)
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.LOADING_INDICATOR)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun errorState_displaysErrorText() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Error("Test error"))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.ERROR_TEXT)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun successState_displaysContentColumn() {
    val office =
        Office(
            id = "o1",
            name = "Test Office",
            address = null,
            description = null,
            vets = emptyList(),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.CONTENT_COLUMN)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun officeIcon_isVisible() {
    val office =
        Office(
            id = "o1",
            name = "Icon Office",
            address = null,
            description = null,
            vets = emptyList(),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_ICON)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun nameField_displaysCorrectValue() {
    val office =
        Office(
            id = "o1",
            name = "Agri Vet Clinic",
            address = null,
            description = null,
            vets = emptyList(),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.NAME_FIELD)
        .assertTextContains("Agri Vet Clinic")
  }

  @Test
  fun addressField_showsWhenAddressExists() {
    val office =
        Office(
            id = "o1",
            name = "WithAddress",
            address = Location(latitude = 0.0, longitude = 0.0),
            description = null,
            vets = emptyList(),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.ADDRESS_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun descriptionField_showsWhenDescriptionPresent() {
    val office =
        Office(
            id = "o1",
            name = "Desc Office",
            address = null,
            description = "Office description",
            vets = emptyList(),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.DESCRIPTION_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun vetList_showsWhenVetsPresent() {
    val office =
        Office(
            id = "o1",
            name = "Vets Office",
            address = null,
            description = null,
            vets = listOf("v1", "v2"),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.VET_LIST)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun allKeyElements_haveTags() {
    val office =
        Office(
            id = "o1",
            name = "Full Office",
            address = null,
            description = null,
            vets = emptyList(),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)

    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.TOP_BAR).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_ICON).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.NAME_FIELD).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.CONTENT_COLUMN).assertExists()
  }
}
