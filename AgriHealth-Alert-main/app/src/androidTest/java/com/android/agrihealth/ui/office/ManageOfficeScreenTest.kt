package com.android.agrihealth.ui.office

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.testutil.TestConstants
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
        FakeOfficeRepository(initialOffices = listOf(fakeOffice), delayMs = 300L)

    val connectionRepository = ConnectionRepositoryProvider.farmerToOfficeRepository

    lateinit var vm: ManageOfficeViewModel

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)

      MaterialTheme {
        ManageOfficeScreen(
            navigationActions = navigationActions,
            onGoBack = {},
            onCreateOffice = {},
            onJoinOffice = {},
            manageOfficeVmFactory = {
              object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                  val instance =
                      ManageOfficeViewModel(
                          userViewModel = fakeUserViewModel,
                          officeRepository = fakeOfficeRepository,
                      )
                  vm = instance
                  @Suppress("UNCHECKED_CAST") return instance as T
                }
              }
            },
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
        isLoading = { vm.uiState.value.isLoading },
        timeoutStart = TestConstants.DEFAULT_TIMEOUT,
        timeoutEnd = TestConstants.DEFAULT_TIMEOUT,
    )
  }
}
