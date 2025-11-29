package com.android.agrihealth.ui.office

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.ui.navigation.NavigationTestTags
import org.junit.Rule
import org.junit.Test

class FakeViewOfficeViewModel(initial: ViewOfficeUiState) :
    ViewOfficeViewModel(targetOfficeId = "fake", officeRepository = FakeOfficeRepository()) {

  private val _state: MutableState<ViewOfficeUiState> = mutableStateOf(initial)

  override val uiState: MutableState<ViewOfficeUiState>
    get() = _state

  override fun load() {}
}

class ViewOfficeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setScreen(vm: FakeViewOfficeViewModel) {
    composeTestRule.setContent { MaterialTheme { ViewOfficeScreen(viewModel = vm, onBack = {}) } }
  }

  private fun setBasicTestScreen() {
    val office =
        Office(
            id = "o1",
            name = "Agri Vet Clinic",
            address = null,
            description = "Providing quality veterinary services for farm animals.",
            vets = listOf("vet1", "vet2"),
            ownerId = "owner1")

    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))
    setScreen(vm)
  }

  @Test
  fun topBar_displaysCorrectly() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Loading)
    setScreen(vm)

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE).assertExists()
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertExists()
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
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_INFO_COLUMN)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun officeIcon_isVisible() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_ICON)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun nameField_displaysCorrectValue() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.NAME_FIELD)
        .assertTextContains("Agri Vet Clinic")
  }

  @Test
  fun descriptionField_showsWhenDescriptionPresent() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.DESCRIPTION_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun vetList_showsWhenVetsPresent() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.VET_LIST)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun allKeyElements_haveTags() {
    setBasicTestScreen()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_ICON).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.NAME_FIELD).assertExists()
    composeTestRule.onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_INFO_COLUMN).assertExists()
  }
}
