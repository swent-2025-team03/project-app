package com.android.agrihealth.ui.user

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.android.agrihealth.data.model.user.displayString
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.TestUser.vet1
import com.android.agrihealth.testhelpers.fakes.FakeOfficeRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserRepository
import com.android.agrihealth.testhelpers.templates.UITest
import org.junit.Test

class FakeViewUserViewModel(initial: ViewUserUiState) :
    ViewUserViewModel(
        targetUserId = "fake",
        userRepository = FakeUserRepository(),
        officeRepository = FakeOfficeRepository()) {

  private val _state: MutableState<ViewUserUiState> = mutableStateOf(initial)

  override val uiState: MutableState<ViewUserUiState>
    get() = _state

  override fun load() {}
}

class ViewUserScreenTest : UITest() {
  private fun setScreen(uiState: ViewUserUiState) {
    val vm = FakeViewUserViewModel(uiState)

    setContent { ViewUserScreen(viewModel = vm) }
  }

  @Test
  fun loadingState_displayAllComponents() {
    setScreen(ViewUserUiState.Loading)

    with(ViewUserScreenTestTags) { nodesAreDisplayed(TOP_BAR, BACK_BUTTON, LOADING_INDICATOR) }
  }

  @Test
  fun errorState_displaysErrorText() {
    setScreen(ViewUserUiState.Error("Test error"))

    nodeIsDisplayed(ViewUserScreenTestTags.ERROR_TEXT)
  }

  @Test
  fun successState_displayAllFarmerComponents() {
    val user = farmer1
    setScreen(ViewUserUiState.Success(user))

    with(ViewUserScreenTestTags) {
      nodesAreDisplayed(TOP_BAR, CONTENT_COLUMN, PROFILE_ICON, ADDRESS_FIELD, DESCRIPTION_FIELD)
      textContains(NAME_FIELD, "${user.firstname} ${user.lastname}")
      textContains(ROLE_FIELD, user.role.displayString(), ignoreCase = true)
      nodesNotDisplayed(OFFICE_FIELD)
    }
  }

  @Test
  fun successState_displayAllVetComponents() {
    val user = vet1.copy(address = null, description = null)
    setScreen(ViewUserUiState.Success(user))

    with(ViewUserScreenTestTags) {
      nodesAreDisplayed(TOP_BAR, CONTENT_COLUMN, PROFILE_ICON, NAME_FIELD, ROLE_FIELD, OFFICE_FIELD)
      nodesNotDisplayed(ADDRESS_FIELD, DESCRIPTION_FIELD)
    }
  }

  override fun displayAllComponents() {}
}
