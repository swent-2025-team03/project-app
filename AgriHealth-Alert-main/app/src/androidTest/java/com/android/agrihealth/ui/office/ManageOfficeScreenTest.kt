package com.android.agrihealth.ui.office

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestTimeout
import com.android.agrihealth.testhelpers.fakes.FakeImageRepository
import com.android.agrihealth.testhelpers.fakes.FakeOfficeRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.profile.CodesViewModel
import org.junit.Rule
import org.junit.Test

class ManageOfficeScreenUiTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun manageOfficeScreen_showsAndHidesLoadingOverlay() {

    val fakeVet =
        Vet(
            uid = "vet-1",
            firstname = "John",
            lastname = "Doe",
            email = "vet@example.com",
            address = null,
            officeId = "office-123",
        )

    val fakeUserViewModel = FakeUserViewModel(fakeVet)

    val fakeOffice =
        Office(
            id = "office-123",
            name = "Fake Office",
            description = "Fake description",
            address = null,
            ownerId = "vet-1",
            vets = listOf("vet-1"))

    val fakeOfficeRepository =
        FakeOfficeRepository(
            initialOffices = listOf(fakeOffice), delayMs = TestTimeout.DEFAULT_TIMEOUT)

    val connectionRepository = ConnectionRepositoryProvider.farmerToOfficeRepository

    val imageViewModel: ImageViewModel = ImageViewModel(FakeImageRepository())

    val manageOfficeViewModel =
        ManageOfficeViewModel(
            officeRepository = fakeOfficeRepository,
            userViewModel = fakeUserViewModel,
            imageViewModel = imageViewModel)

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)

      MaterialTheme {
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
                      userViewModel = fakeUserViewModel,
                      connectionRepository = connectionRepository,
                  )
                      as T
                }
              }
            })
      }
    }

    composeTestRule.assertOverlayDuringLoading(
        isLoading = { manageOfficeViewModel.uiState.value.isLoading },
        timeoutStart = TestTimeout.LONG_TIMEOUT,
        timeoutEnd = TestTimeout.LONG_TIMEOUT,
    )
  }
}
