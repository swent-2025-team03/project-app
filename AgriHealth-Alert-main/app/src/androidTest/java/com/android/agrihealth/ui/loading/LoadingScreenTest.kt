package com.android.agrihealth.ui.loading


import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.fakes.FakeReportRepository
import com.android.agrihealth.fakes.FakeUserRepository
import com.android.agrihealth.testutil.FakeLocationProvider
import com.android.agrihealth.ui.map.MapViewModel
import com.android.agrihealth.fakes.FakeAuthProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test



class LoadingScreenTest {

    @get:Rule
    val compose = createComposeRule()

    // -----------------
    // TEST 1 — Not loading
    // -----------------
    @Test
    fun overlay_notLoading_showsOnlyContent() {
        compose.setContent {
            LoadingOverlay(isLoading = false) {
                Box(Modifier.testTag("content"))
            }
        }

        compose.onNodeWithTag("content").assertExists()
        compose.onNodeWithTag("loading_overlay_scrim").assertDoesNotExist()
        compose.onNodeWithTag("loading_overlay_spinner").assertDoesNotExist()
    }

    // -----------------
    // TEST 2 — Loading state
    // -----------------
    @Test
    fun overlay_loading_showsScrimAndSpinner() {
        compose.setContent {
            LoadingOverlay(isLoading = true) {
                Box(Modifier.testTag("content"))
            }
        }

        compose.onNodeWithTag("content").assertExists()
        compose.onNodeWithTag("loading_overlay_scrim").assertExists()
        compose.onNodeWithTag("loading_overlay_spinner").assertExists()
    }

    // -----------------
    // TEST 3 — MapViewModel loading logic
    // -----------------
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setStartingLocation_togglesIsLoading_whenLocationArrives() = runTest {
        // Fake GPS provider
        val fakeLocation = FakeLocationProvider(
            initialLocation = null,
            permissions = true
        )

        val fakeRepo = FakeReportRepository()
        val fakeUserRepo = FakeUserRepository()

        // ViewModel under test
        val vm = MapViewModel(
            reportRepository = fakeRepo,
            userRepository = fakeUserRepo,
            locationViewModel = fakeLocation,
            authProvider = FakeAuthProvider(),
            selectedReportId = null
        )

        vm.setStartingLocation(location = null, useCurrentLocation = true)

        assertTrue(
            vm.uiState.value.isLoading
        )

        fakeLocation.emitLocation(Location(46.5, 6.6, null))

        advanceUntilIdle()

        assertFalse(
            vm.uiState.value.isLoading
        )
    }
}
