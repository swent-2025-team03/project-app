package com.android.agrihealth.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestTimeout
import com.android.agrihealth.testhelpers.TestUser.office1
import com.android.agrihealth.testhelpers.TestUser.vet1
import com.android.agrihealth.testhelpers.fakes.FakeImageRepository
import com.android.agrihealth.testhelpers.fakes.FakeOfficeRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.profile.CodesViewModel
import org.junit.Test

class ManageOfficeScreenUiTest : UITest() {

  override fun displayAllComponents() {}

  @Test
  fun manageOfficeScreen_showsAndHidesLoadingOverlay() {
    val officeRepository =
        FakeOfficeRepository(
            initialOffices = listOf(office1), delayMs = TestTimeout.DEFAULT_TIMEOUT)
    val connectionRepository = ConnectionRepositoryProvider.farmerToOfficeRepository
    val imageViewModel = ImageViewModel(FakeImageRepository())
    val userViewModel = FakeUserViewModel(vet1)

    val manageOfficeViewModel =
        ManageOfficeViewModel(
            officeRepository = officeRepository,
            userViewModel = userViewModel,
            imageViewModel = imageViewModel)

    setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)

      ManageOfficeScreen(
          navigationActions = navigationActions,
          manageOfficeViewModel = manageOfficeViewModel,
          onGoBack = {},
          onCreateOffice = {},
          onJoinOffice = {},
          codesVmFactory = {
            object : ViewModelProvider.Factory {
              override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CodesViewModel(
                    userViewModel = userViewModel,
                    connectionRepository = connectionRepository,
                )
                    as T
              }
            }
          })
    }

    composeTestRule.assertOverlayDuringLoading(
        isLoading = { manageOfficeViewModel.uiState.value.isLoading })
  }
}
